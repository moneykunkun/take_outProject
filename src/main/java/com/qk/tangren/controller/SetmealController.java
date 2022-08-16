package com.qk.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qk.tangren.common.R;
import com.qk.tangren.dto.SetmealDto;
import com.qk.tangren.entity.Category;
import com.qk.tangren.entity.Setmeal;
import com.qk.tangren.service.CategoryService;
import com.qk.tangren.service.SetmealDishService;
import com.qk.tangren.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;


    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }


    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //分页构造器对象
        Page<Setmeal> pageInfo =new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage =new Page<>();

        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper =new LambdaQueryWrapper<>();
        //添加查询条件，根据name进行like模糊查询
        queryWrapper.like(name !=null,Setmeal::getName,name);
        //添加排序条件    按更新时间降序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");
        //设置records属性
        final List<Setmeal> records = pageInfo.getRecords();

        //将输入流中对象属性再拷贝到新的集合中
        List<SetmealDto> list =records.stream().map((item)->{
            SetmealDto setmealDto =new SetmealDto();
            //将item上的普通属性拷贝到setmealDto上
            BeanUtils.copyProperties(item,setmealDto);
            //拿到菜品分类id
             Long categoryId = item.getCategoryId();
            //再根据分类id查询分类名称
             Category category = categoryService.getById(categoryId);
            if (category !=null){
                //拿到分类名称
                 String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        //将集合对象重新设到setmealDto中
        setmealDtoPage.setRecords(list);
        return R.success(setmealDtoPage);
    }

    /**
     * 删除套餐功能
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids：{}",ids);
        setmealService.removeWithDish(ids);
        return R.success("套餐删除成功");
    }

    /**
     * 修改套餐的售卖状态
     *
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> statusWithIds(@PathVariable("status") Integer status, @RequestParam List<Long> ids) {
        //构造一个条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null, Setmeal::getId, ids);
        queryWrapper.orderByDesc(Setmeal::getPrice);
        //根据条件进行批量查询
        List<Setmeal> list = setmealService.	list(queryWrapper);
        for (Setmeal setmeal : list) {
            if (list != null) {
                //把浏览器传入的status参数复制给套餐
                setmeal.setStatus(status);
                setmealService.updateById(setmeal);
            }
        }
        return R.success("售卖状态修改成功");
    }


    /**
     * 移动端
     * 套餐查询
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //构造查询条件
        LambdaQueryWrapper<Setmeal> queryWrapper =new LambdaQueryWrapper<>();
        //菜品id
        queryWrapper.eq(setmeal.getCategoryId()!= null,Setmeal::getCategoryId,setmeal.getCategoryId());
        //售卖状态
        queryWrapper.eq(setmeal.getStatus()!= null,Setmeal::getStatus,setmeal.getStatus());
        //添加一个排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

         List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }
    /**
     * 修改套餐
     * @param setmealDto
     * @return R<String>
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info("修改套餐信息{}", setmealDto);
        // 执行更新。
        setmealService.updateWithSetmeal(setmealDto);
        return R.success("套餐修改成功");
    }

    /**
     * 根据id查询套餐信息
     *(套餐信息的回显)
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id) {
        log.info("根据id查询套餐信息:{}", id);
        // 调用service执行查询
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }
}
