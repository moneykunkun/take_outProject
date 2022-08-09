package com.qk.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qk.reggie.common.R;
import com.qk.reggie.entity.Employee;
import com.qk.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

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
        //查询用户 ，数据库中的唯一索引 所以使用getOne
         Employee emp = employeeService.getOne(queryWrapper);
        //3.处理查询结果
        if (emp ==null){
            return R.error("登录失败！");
        }
        //4.密码比对，如果不一致则返回登录失败
        if (!emp.getPassword().equals(password)){
            return R.error("密码错误，登录失败！");
        }
        //5.查看员工状态，看是否被禁用，0表示禁用
        if (emp.getStatus()==0){
            return R.error("该账号已禁用！");
        }
        //6.登录成功，将员工id存入session并返回登录结果
         request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 退出功能
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //1.清理session中保存员工id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功！");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("员工信息：{}",employee.toString());
        //设置用户的初始密码 12345，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //获取当前用户id
         Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);
        //mp提供的方法
         employeeService.save(employee);
         return R.success("新增员工成功");
    }

    /**
     * 员工信息的分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public  R<Page> page(int page,int pageSize,String name){        //mp提供的Page对象，包含相关的属性
        log.info("page ={},pageSize={},name={}",page,pageSize,name);
        //构造分页构造器
        Page pageInfo =new Page(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Employee> queryWrapper =new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.hasText(name),Employee::getName,name);
      //添加排序条件，按更新时间排序
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());
        //获取登录用户的id
//        Long empId =(Long)request.getSession().getAttribute("employee");
//        employee.setUpdateTime(LocalDateTime.now());        //设置更新时间
//        employee.setUpdateUser(empId);              //设置更新用户
        //调用业务层方法（MP提供的）修改员工信息
        employeeService.updateById(employee);
        return R.success("员工信息修改成功！");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id ){
        log.info("根据id查询员工信息......");
         Employee employee = employeeService.getById(id);
         if (employee != null){
             return R.success(employee);
         }
         return R.error("没有查询到员工信息！");
    }
}
