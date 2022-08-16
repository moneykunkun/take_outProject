package com.qk.tangren.dto;

import com.qk.tangren.entity.Dish;
import com.qk.tangren.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据转换对象
 * 处理实体表和数据不一致的情况
 */
@Data
public class DishDto extends Dish {

    //菜品对应的口味数据
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
