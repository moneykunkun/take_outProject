package com.qk.tangren.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qk.tangren.dto.OrdersDto;
import com.qk.tangren.entity.Orders;
import org.springframework.stereotype.Service;

@Service
public interface OrderService extends IService<Orders> {
    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);

    /**
     * 订单明细
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    public Page<OrdersDto> empPage(int page, int pageSize, String number, String beginTime, String endTime) ;
}
