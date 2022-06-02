package com.qk.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qk.reggie.dto.DishDto;
import com.qk.reggie.entity.Dish;
import org.springframework.stereotype.Service;

@Service
public interface DishService extends IService<Dish>  {
    //新增菜品，同时插入菜品对应的口味数据，需要同时操作两张表
    public  void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息以及口味信息
    public  DishDto getByIdWithFlavor(Long id);
}
