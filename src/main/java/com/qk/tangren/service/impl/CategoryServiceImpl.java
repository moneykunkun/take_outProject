package com.qk.tangren.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qk.tangren.common.CustomException;
import com.qk.tangren.entity.Category;
import com.qk.tangren.entity.Dish;
import com.qk.tangren.entity.Setmeal;
import com.qk.tangren.mapper.CategoryMapper;
import com.qk.tangren.service.CategoryService;
import com.qk.tangren.service.DishService;
import com.qk.tangren.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    /**
     * 根据id删除分类，删除之前先判断是否有关联
     * @param id
     */
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        //查询当前分类是否关联了菜品，如果已关联了，抛出业务异常
        if (count1 > 0) {
            //已关联菜品，抛出业务异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }
        //查询当前分类是否关联了套餐，如果已关联，抛出业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //根据id进行查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int count2 = setmealService.count();
        if (count2 > 0) {
            //已关联了套餐
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }
        //正常情况下，正常删除
        super.removeById(id);
    }
}
