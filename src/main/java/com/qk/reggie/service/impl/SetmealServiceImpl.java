package com.qk.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qk.reggie.common.CustomException;
import com.qk.reggie.dto.SetmealDto;
import com.qk.reggie.entity.Setmeal;
import com.qk.reggie.entity.SetmealDish;
import com.qk.reggie.mapper.SetmealMapper;
import com.qk.reggie.service.SetmealDishService;
import com.qk.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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

    /**
     * 删除套餐，同时删除套餐和菜品的关联数据
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //查询套餐的状态，仅起售状态的可以删除
        //查询条件 select count（*） from setmetal where id in(1,2,3) and status=1
        LambdaQueryWrapper<Setmeal> queryWrapper =new LambdaQueryWrapper<>();
        //构造查询条件
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        //查询
         int count = this.count(queryWrapper);
        //如果不能删除，抛出业务异常
         if (count>0){
             throw new CustomException("套餐正在售卖中，不能删除！");
         }
        //可以删除，先批量删除套餐表中的数据--- setmeal
        this.removeByIds(ids);
        //删除关系表中的数据--- setmeal_dish
        // delete from  setmetal_dish where setmeal_id in ();
        //构造查询条件
        LambdaQueryWrapper<SetmealDish> dishQueryWrapper =new LambdaQueryWrapper<>();
        dishQueryWrapper.in(SetmealDish::getSetmealId,ids);

        setmealDishService.remove(dishQueryWrapper);
    }
    /**
     * 修改套餐
     * @param setmealDto
     */
    @Override
    public void updateWithSetmeal(SetmealDto setmealDto) {
        // 保存setmeal表中的基本数据。
        this.updateById(setmealDto);
        // 先删除原来的套餐所对应的菜品数据。
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        // 更新套餐关联菜品信息。setmeal_dish表。
        // Field 'setmeal_id' doesn't have a default value] with root cause
        // 所以需要处理setmeal_id字段。
        // 先获得套餐所对应的菜品集合。
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //每一个item为SetmealDish对象。
        setmealDishes = setmealDishes.stream().map((item) -> {
            //设置setmeal_id字段。
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        // 重新保存套餐对应菜品数据
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 通过id查询套餐信息， 同时还要查询关联表setmeal_dish的菜品信息进行回显
     * @param id 待查询的id
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        // 根据id查询setmeal表中的基本信息
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        // 对象拷贝。
        BeanUtils.copyProperties(setmeal, setmealDto);
        // 查询关联表setmeal_dish的菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
        //设置套餐菜品属性
        setmealDto.setSetmealDishes(setmealDishList);
        return setmealDto;
    }
}
