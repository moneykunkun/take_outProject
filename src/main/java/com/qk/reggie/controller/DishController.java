package com.qk.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qk.reggie.common.R;
import com.qk.reggie.dto.DishDto;
import com.qk.reggie.entity.Category;
import com.qk.reggie.entity.Dish;
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
        final List<Dish> records = pageInfo.getRecords();
        //通过stream流的形式处理records集合
        List<DishDto> list = records.stream().map((item)->{
            DishDto dishDto =new DishDto();
            //将item对象普通属性拷贝到dishDto中
            BeanUtils.copyProperties(item,dishDto);
            //1、通过item对象获取菜品分类id
            Long categoryId = item.getCategoryId();
            //2、再通过categoryId拿到菜品名称
            //先拿到分类对象
            final Category category = categoryService.getById(categoryId);
            //拿到分类名称
            final String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }
}
