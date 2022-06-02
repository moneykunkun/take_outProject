package com.qk.reggie.dto;

import com.qk.reggie.entity.Dish;
import com.qk.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据转换对象
 * 处理实体表和数据不一致的情况
 */
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
