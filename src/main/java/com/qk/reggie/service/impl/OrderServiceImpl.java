package com.qk.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qk.reggie.common.BaseContext;
import com.qk.reggie.entity.Orders;
import com.qk.reggie.mapper.OrderMapper;
import com.qk.reggie.service.OrderService;
import com.qk.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;
    /**
     * 用户下单
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        //先获得用户id
        final Long currentId = BaseContext.getCurrentId();

        //查询当前用户的购物车数据
        //向订单表中添加数据
        //向订单明细表插入数据，多条数据
        //下单完成后，清空购物车数据
    }
}
