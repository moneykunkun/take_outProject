package com.qk.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qk.reggie.dto.SetmealDto;
import com.qk.reggie.entity.Setmeal;
import com.qk.reggie.entity.SetmealDish;
import com.qk.reggie.mapper.SetmealMapper;
import com.qk.reggie.service.SetmealDishService;
import com.qk.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增套餐，同时保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作Setmeal，执行insert操作
        this.save(setmealDto);

        final List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //为集合上的每个对象赋予setmealId
        setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联关系，操作Setmeal_dish，执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }
}
