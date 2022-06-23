package com.qk.reggie.dto;


import com.qk.reggie.entity.OrderDetail;
import com.qk.reggie.entity.Orders;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单数据转换对象
 */
@Data
public class OrdersDto extends Orders {
    //订单明细
    public List<OrderDetail>  OrderDetails =new ArrayList<>();
}
