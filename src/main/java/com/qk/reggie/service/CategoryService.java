package com.qk.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qk.reggie.entity.Category;
import org.springframework.stereotype.Service;

@Service
public interface CategoryService extends IService<Category> {
    //根据id删除，自定义方法
    public void remove(Long id);
}
