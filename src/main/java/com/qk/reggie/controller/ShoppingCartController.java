package com.qk.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qk.reggie.common.BaseContext;
import com.qk.reggie.common.R;
import com.qk.reggie.entity.ShoppingCart;
import com.qk.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据：{}",shoppingCart);

        //1.设置用户id，指定是哪个用户的购物车
        final Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //2.查询当前菜品或套餐是否已经存在于购物车中
        final Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper =new LambdaQueryWrapper<>();
        //根据用户id查询
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        if (dishId != null){
            //添加菜品信息到购物车
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            //添加套餐到购物车
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //SQL:select * from shopping_cart where user_id =? and dish_id/setmeal_id =?
         ShoppingCart cartOne = shoppingCartService.getOne(queryWrapper);

        if (cartOne != null){
            //已经存在，就在原来的基础上+1
            final Integer number = cartOne.getNumber();         //原来的数量
            cartOne.setNumber(number+1);
            shoppingCartService.updateById(cartOne);            //更新操作
        }else {
            //不存在，添加到购物车，数量默认1
            cartOne.setNumber(1);
            shoppingCartService.save(cartOne);
            //重新把shoppingCart赋给cartOne
            cartOne =shoppingCart;
        }
        return R.success(cartOne);
    }
}
