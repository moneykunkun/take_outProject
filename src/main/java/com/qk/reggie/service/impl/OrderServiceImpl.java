package com.qk.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qk.reggie.common.BaseContext;
import com.qk.reggie.common.CustomException;
import com.qk.reggie.entity.AddressBook;
import com.qk.reggie.entity.Orders;
import com.qk.reggie.entity.ShoppingCart;
import com.qk.reggie.entity.User;
import com.qk.reggie.mapper.OrderMapper;
import com.qk.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;          //订单明细
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
        LambdaQueryWrapper<ShoppingCart> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        //查询到购物车数据
        final List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        //对购物车数据进行判断
        if (shoppingCarts ==null){
            throw  new CustomException("购物车为空，不能下单");
        }
        //查询用户信息
        final User user = userService.getById(currentId);

         Long addressBookId = orders.getAddressBookId();
         AddressBook addressBook = addressBookService.getById(addressBookId);

         if (addressBook ==null){
             throw new CustomException("地址信息为空，不能下单");
         }
        //向订单表中添加数据
        final long orderId = IdWorker.getId();
        //设置订单实体的其他属性
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);        //待派送
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(currentId);        //用户id
        orders.setNumber(String.valueOf(orderId));      //订单号
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());            //收货人
        orders.setPhone(addressBook.getPhone());            //手机号
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        this.save(orders);
        //向订单明细表插入数据，多条数据
        //下单完成后，清空购物车数据
    }
}
