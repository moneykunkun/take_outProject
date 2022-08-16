package com.qk.tangren.dto;

import com.qk.tangren.entity.Setmeal;
import com.qk.tangren.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
