package com.qk.tangren.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qk.tangren.entity.ShoppingCart;
import com.qk.tangren.mapper.ShoppingCartMappper;
import com.qk.tangren.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMappper, ShoppingCart> implements ShoppingCartService {
}
