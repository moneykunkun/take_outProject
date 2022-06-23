package com.qk.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qk.reggie.common.R;
import com.qk.reggie.dto.DishDto;
import com.qk.reggie.entity.Category;
import com.qk.reggie.entity.Dish;
import com.qk.reggie.entity.DishFlavor;
import com.qk.reggie.service.CategoryService;
import com.qk.reggie.service.DishFlavorService;
import com.qk.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
        return R.success("修改菜品成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
/*    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() !=null,Dish::getCategoryId,dish.getCategoryId());
        //添加查询条件：起售状态为1的
        queryWrapper.eq(Dish::getStatus,1);

        //添加一个排序条件 按sort升序，再按更新时间降序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        //处理业务方法
        final List<Dish> list = dishService.list(queryWrapper);
        return R.success(list);
    }*/
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() !=null,Dish::getCategoryId,dish.getCategoryId());
        //添加查询条件：起售状态为1的
        queryWrapper.eq(Dish::getStatus,1);

        //添加一个排序条件 按sort升序，再按更新时间降序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        //处理业务方法
        final List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> dishDtoList  = list.stream().map((item)->{
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

        return R.success(dishDtoList);
    }
}
