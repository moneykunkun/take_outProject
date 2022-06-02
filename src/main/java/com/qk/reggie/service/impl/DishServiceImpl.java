package com.qk.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qk.reggie.dto.DishDto;
import com.qk.reggie.entity.Dish;
import com.qk.reggie.entity.DishFlavor;
import com.qk.reggie.mapper.DishMapper;
import com.qk.reggie.service.DishFlavorService;
import com.qk.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 新增菜品同时保存口味数据
     * @param dishDto
     */
    @Override
    @Transactional      //对多张表操作需要开启事务，同时需要再启动类加注解配置
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到dish表
        this.save(dishDto);
         Long dishId = dishDto.getId();     //菜品id

        //获得菜品口味的集合
         List<DishFlavor> flavors = dishDto.getFlavors();
        //lambda表达式，为集合中的每个flavors赋值菜品id
        flavors=flavors.stream().map((item) ->{
           item.setDishId(dishId);
           return item;
        }).collect(Collectors.toList());            //相当于对集合做处理后又重新转为list集合

        //保存菜品的口味表到菜品口味表dish_flavor
     dishFlavorService.saveBatch(flavors);          //saveBatch保存集合数据
    }
    }

