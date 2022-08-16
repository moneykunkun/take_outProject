package com.qk.tangren.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qk.tangren.common.CustomException;
import com.qk.tangren.common.R;
import com.qk.tangren.dto.DishDto;
import com.qk.tangren.entity.Category;
import com.qk.tangren.entity.Dish;
import com.qk.tangren.entity.DishFlavor;
import com.qk.tangren.service.CategoryService;
import com.qk.tangren.service.DishFlavorService;
import com.qk.tangren.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 * 以及菜品口味管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 删除菜品
     * @return
     */
    @DeleteMapping("/ids")
    public R<String> deleteByIds(@PathVariable int ids){
        dishService.removeById(ids);
        return R.success("删除成功");
    }
    /**
     * 修改售卖状态（起售，停售）
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> statusWithIds(@PathVariable("status") Integer status,@RequestParam List<Long> ids) {
        //构造一个条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null, Dish::getId, ids);
        queryWrapper.orderByDesc(Dish::getPrice);
        //根据条件进行批量查询
        List<Dish> list = dishService.list(queryWrapper);
        for (Dish dish : list) {
            if (dish != null) {
                //把浏览器传入的status参数赋值给菜品
                dish.setStatus(status);
                dishService.updateById(dish);
            }
        }
        return R.success("售卖状态修改成功");
    }
    /**
     * 批量删除菜品和单独删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @Transactional  //因为多表操作开启事务
    public R<String> delete(@RequestParam List<Long> ids) {
        //构造一个条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //判断浏览器传来的ids是否为空，并和菜品表中的id进行匹配
        queryWrapper.in(ids != null, Dish::getId,ids);
        List<Dish> list = dishService.list(queryWrapper);
        for (Dish dish : list) {
            //判断当前菜品是否在售卖阶段，0停售，1起售
            if (dish.getStatus() == 0) {
                //停售状态直接删除
                dishService.removeById(dish.getId());
                LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
                //根据菜品id匹配口味表中的菜品id
                dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dish.getId());
                //删除菜品id关联的口味表信息
                dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
            }else {
                throw new CustomException("此菜品还在售卖阶段，删除影响销售！");
            }
        }
        return R.success("删除成功");
    }


    /**
     * 菜品信息的分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        //构造分页构造器对象
        Page<Dish> pageInfo =new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage =new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper =new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件:根据更新时间
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //将pageInfo 中的属性拷贝到dishDtoPage,除开records属性
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        //处理records
         List<Dish> records = pageInfo.getRecords();
        //通过stream流的形式处理records集合
        List<DishDto> list = records.stream().map((item)->{
            DishDto dishDto =new DishDto();
            //将item对象普通属性拷贝到dishDto中
            BeanUtils.copyProperties(item,dishDto);
            //1、通过item对象获取菜品分类id
            Long categoryId = item.getCategoryId();
            //2、再通过categoryId拿到菜品名称
            //先拿到分类对象
             Category category = categoryService.getById(categoryId);
            //拿到分类名称
            if (category !=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){           //dto中封装了菜品信息和口味信息

        final DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);

        //修改菜品数据时，清理redis中缓存数据
        String key ="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return R.success("修改菜品成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        List<DishDto> dishDtoList =null;
        //动态构造key
        String key ="dish_"+dish.getCategoryId()+"_"+dish.getStatus();            //dish_12345678994545_1

        //先从redis或获取缓存数据
        dishDtoList= (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList  !=null) {
            //如果存在，直接从缓存中读取数据，不需查询数据库
            return R.success(dishDtoList);
        }

        //如果不存在，则查询数据库，并将数据缓存到redis中
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() !=null,Dish::getCategoryId,dish.getCategoryId());
        //添加查询条件：起售状态为1的
        queryWrapper.eq(Dish::getStatus,1);

        //添加一个排序条件 按sort升序，再按更新时间降序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        //处理业务方法
        final List<Dish> list = dishService.list(queryWrapper);

         dishDtoList  = list.stream().map((item)->{
            DishDto dishDto =new DishDto();
            //将item对象普通属性拷贝到dishDto中
            BeanUtils.copyProperties(item,dishDto);
            //1、通过item对象获取菜品分类id
            Long categoryId = item.getCategoryId();
            //2、再通过categoryId拿到菜品名称
            //先拿到分类对象
            Category category = categoryService.getById(categoryId);
            //拿到分类名称
            if (category !=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //当前菜品的id
             Long dishId = item.getId();
            //根据id查询菜品口味
            LambdaQueryWrapper<DishFlavor> wrapper =new LambdaQueryWrapper<>();
            //查询条件
            wrapper.eq(DishFlavor::getDishId,dishId);
            //SQL:select * from dish_flavor where dish_id =?
             List<DishFlavor> flavorList = dishFlavorService.list(wrapper);
           dishDto.setFlavors(flavorList);
            return dishDto;
        }).collect(Collectors.toList());
        //并将数据缓存到redis中,缓存时间1小时
        redisTemplate.opsForValue().set(key,dishDtoList,1, TimeUnit.HOURS);

        return R.success(dishDtoList);
    }
}
