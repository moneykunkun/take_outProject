package com.qk.tangren.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qk.tangren.entity.Employee;
import com.qk.tangren.mapper.EmployeeMapper;
import com.qk.tangren.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper,Employee> implements EmployeeService {
}
