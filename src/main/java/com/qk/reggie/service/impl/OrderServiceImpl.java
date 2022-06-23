package com.qk.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qk.reggie.common.BaseContext;
import com.qk.reggie.common.CustomException;
import com.qk.reggie.dto.OrdersDto;
import com.qk.reggie.entity.*;
import com.qk.reggie.mapper.OrderMapper;
import com.qk.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
        if (shoppingCarts ==null || shoppingCarts.size()==0){
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
         long orderId = IdWorker.getId();       //订单号

        //原子操作
        AtomicInteger amount =new AtomicInteger(0);
        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item)->{
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        //设置订单实体的其他属性
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);        //待派送
        orders.setAmount(new BigDecimal(amount.get()));     //总金额
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
        orderDetailService.saveBatch(orderDetails);

        //下单完成后，清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }

    /**
     * 后台查询订单明细
     *
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    public Page<OrdersDto> empPage(int page, int pageSize, String number, String beginTime, String endTime) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> pageDto = new Page<>();

        //创建条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件，根据number进行like模糊查询
        queryWrapper.like(number != null, Orders::getNumber, number);
        queryWrapper.gt(StringUtils.isNotEmpty(beginTime), Orders::getOrderTime, beginTime);
        queryWrapper.lt(StringUtils.isNotEmpty(endTime), Orders::getOrderTime, endTime);
        //添加排序条件(根据更新时间降序排序)
        queryWrapper.orderByDesc(Orders::getOrderTime);
         this.page(pageInfo, queryWrapper);
        //将其除了records中的内存复制到pageDto中
        BeanUtils.copyProperties(pageInfo, pageDto, "records");

        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> collect = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            //对象拷贝
            BeanUtils.copyProperties(item, ordersDto);
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            //根据订单id查询订单详细信息
            lambdaQueryWrapper.eq(OrderDetail::getOrderId, item.getId());

            List<OrderDetail> orderDetails = orderDetailService.list(lambdaQueryWrapper);
            ordersDto.setOrderDetails(orderDetails);

            //根据userId查询用户姓名
            Long userID = item.getUserId();
            User user = userService.getById(userID);
            ordersDto.setUserName(user.getName());
            ordersDto.setPhone(user.getPhone());

            //获取地址信息
            Long addressBookId = item.getAddressBookId();
            AddressBook addressBook = addressBookService.getById(addressBookId);
            ordersDto.setAddress(addressBook.getDetail());
            ordersDto.setConsignee(addressBook.getConsignee());

            return ordersDto;
        }).collect(Collectors.toList());

        pageDto.setRecords(collect);
        return pageDto;
    }


}
