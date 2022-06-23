package com.qk.reggie.dto;


import com.qk.reggie.entity.OrderDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单数据转换对象
 */
public class OrdersDto {
    //订单明细
    public List<OrderDetail>  orderDetails =new ArrayList<>();

    //用户名
    public String UserName;

    //手机号
    public String Phone;

    //地址
    public String Address;

    //
    public String Consignee;
}
