package com.qk.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qk.reggie.dto.SetmealDto;
import com.qk.reggie.entity.Setmeal;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐，同时保存菜品和套餐的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时删除套餐和菜品的关联数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);
}
