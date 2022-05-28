package com.qk.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qk.reggie.common.R;
import com.qk.reggie.entity.Employee;
import com.qk.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

@Slf4j      //日志注解
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request   获取员工登录后的id存入session
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){       //requestBody接收json数据
        //1.将页面提交的密码进行MD5加密处理
         String password = employee.getPassword();     //获取密码
         password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2.根据用户提交的用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        //查询用户
         Employee emp = employeeService.getOne(queryWrapper);       //数据库中的唯一索引 所有getOne
        //3.处理查询结果
        if (emp ==null){
            return R.error("登录失败！");
        }
        //4.密码比对，如果不一致则返回登录失败
        if (!emp.getPassword().equals(password)){
            return R.error("登录失败！");
        }
        //5.查看员工状态，看是否被禁用，0表示禁用
        if (emp.getStatus()==0){
            return R.error("账号已禁用");
        }
        //6.登录成功，将员工id存入session并返回登录结果
         request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }
}
