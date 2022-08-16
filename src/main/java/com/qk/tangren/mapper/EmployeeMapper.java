package com.qk.tangren.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qk.tangren.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
