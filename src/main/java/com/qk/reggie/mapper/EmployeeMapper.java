package com.qk.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qk.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
