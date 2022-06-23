package com.qk.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qk.reggie.common.R;
import com.qk.reggie.service.OrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.qk.reggie.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.qk.reggie.dto.OrdersDto;
@Slf4j
@RestController
@RequestMapping("/orderDetail")
public class OrderDetailController {

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private OrderService orderService;
    /**
     * 后台查询订单明细
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page<OrdersDto>> empPage(int page, int pageSize, String number,
                                      String beginTime,
                                      String endTime) {
        Page<OrdersDto> empPage = orderService.empPage(page, pageSize, number, beginTime, endTime);
        return R.success(empPage);
    }

}
