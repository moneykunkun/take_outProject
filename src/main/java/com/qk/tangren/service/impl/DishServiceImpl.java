package com.qk.tangren.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qk.tangren.dto.DishDto;
import com.qk.tangren.entity.Dish;
import com.qk.tangren.entity.DishFlavor;
import com.qk.tangren.mapper.DishMapper;
import com.qk.tangren.service.DishFlavorService;
import com.qk.tangren.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
        //stream流的方式遍历集合，lambda表达式，为集合中的每个flavors赋值菜品id
        flavors=flavors.stream().map((item) ->{
           item.setDishId(dishId);
           return item;
        }).collect(Collectors.toList());            //相当于对集合做处理后又重新转为list集合

        //保存菜品的口味表到菜品口味表dish_flavor
     dishFlavorService.saveBatch(flavors);          //saveBatch保存集合数据
    }

    /**
     * 根据id查询菜品信息和口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //1.先查询菜品的基本信息 --dish表
         Dish dish = this.getById(id);

         //创建一个Dto拷贝普通的属性
        DishDto dishDto =new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //2.查询当期菜品所对应的口味信息      --dish_flavor表
        //条件构造器
        LambdaQueryWrapper<DishFlavor> queryWrapper =new LambdaQueryWrapper<>();
        //构造条件
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        //最后为dto设置flavors属性
        dishDto.setFlavors(flavors);

        return dishDto;
    }
    //更新菜品信息，同时更新对应的口味信息
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //1.更新菜品dish信息
        this.updateById(dishDto);
        //2.更新dish_flavors口味信息
        //先删除当前菜品对应的口味信息---delete
        LambdaQueryWrapper<DishFlavor> queryWrapper =new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        dishFlavorService.remove(queryWrapper);
        //再添加当前提交的口味信息---insert
         List<DishFlavor> flavors = dishDto.getFlavors();           //获取当前表单中的口味信息

        flavors =flavors.stream().map((item) ->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        //将集合数据存入数据库
        dishFlavorService.saveBatch(flavors);
    }
}

