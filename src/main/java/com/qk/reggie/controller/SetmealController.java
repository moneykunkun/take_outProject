package com.qk.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qk.reggie.common.R;
import com.qk.reggie.dto.SetmealDto;
import com.qk.reggie.entity.Category;
import com.qk.reggie.entity.Setmeal;
import com.qk.reggie.service.CategoryService;
import com.qk.reggie.service.SetmealDishService;
import com.qk.reggie.service.SetmealService;
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
            final Long categoryId = item.getCategoryId();
            //再根据分类id查询分类名称
            final Category category = categoryService.getById(categoryId);
            if (category !=null){
                //拿到分类名称
                final String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        //将集合对象重新设到setmealDto中
        setmealDtoPage.setRecords(list);
        return R.success(setmealDtoPage);
    }
}
