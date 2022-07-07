package com.qk.reggie;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.qk.reggie.common.R;
import com.qk.reggie.entity.Category;
import com.qk.reggie.entity.Employee;
import com.qk.reggie.service.CategoryService;
import com.qk.reggie.service.DishService;
import com.qk.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;



import java.time.LocalDateTime;

import static org.junit.Assert.*;

@Slf4j
@SpringBootTest(classes = {ReggieApplication.class})
@RunWith(SpringRunner.class)
public class EmployeeTest {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录功能测试
     */
    @Test
    public void loginTest(){
        Employee employee =new Employee();
        //username主键唯一，长度20，不能重复，重复插入失败
        employee.setUsername("qk35890226");
        employee.setPassword("1234567");
        //MD5加密
         String password = DigestUtils.md5DigestAsHex(employee.getPassword().getBytes());
        employee.setPassword(password);
        //根据用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        //查询用户
        Employee emp = employeeService.getOne(queryWrapper);
        //1.查询用户是否存在
        assertNotNull("查询到用户,测试失败",emp);
        // 2.密码验证
        assertNotEquals("密码匹配",employee.getPassword(),emp.getPassword());
        //3.处理员工状态是否为禁用
       assertEquals("该账户已禁用",(int)emp.getStatus(),1);
        //4.用户存在，密码正确且用户状态未禁用，登录成功
        System.out.println("登录测试完毕！");
    }

    /**
     * 新增员工测试
     * @param
     * @return
     */
    @Test
    public void saveEmployee(){
        Employee employee =new Employee();
        //id主键唯一，不能重复，属性不能为空，否则插入失败
        employee.setId((long)10);
        employee.setName("color");
        employee.setUsername("qwert");
        //设置用户的初始密码 12345，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //employee.setPassword("112233");
        employee.setPhone("112233");
        employee.setSex("1");
        employee.setIdNumber("qk");
        employee.setStatus(1);
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        log.info("员工信息：{}",employee.toString());
        //新增员工的时候，创建用户对象，调用接口方法，将数据插入到数据库
         boolean save = employeeService.save(employee);
        assertEquals("新增失败",true,save);
        System.out.println("新增成功");
    }
    /**
     * 分页功能测试
     */
    @Test
    public void pageTest(){        //mp提供的Page对象，包含相关的属性
        int page=1;
        int pageSize=10;
        String name="qk";
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
         Page page1 = employeeService.page(pageInfo, queryWrapper);
        assertNotNull("分页测试失败",page1);
        System.out.println("分页测试成功!");
    }

    @Autowired
    private CategoryService categoryService;



    /**
     * 根据id删除分类菜品
     * @return
     * 备注：数据库create_user/update_user 设置一更给为可以为空
     */
    @Test
    public void deleteById(){
        //菜品id（20）必须是菜品数据库中有的数据，否则删除失败
        Long ids =(long)123456789;
        log.info("删除菜品，id为：{}",ids);
        final boolean flag = categoryService.removeById(ids);
        assertEquals("删除成功，测试失败",false,flag);
        System.out.println("测试删除通过");
    }

    /**
     * 测试根据id修改分类信息
     * @return
     */
    @Test
    public void updateCategory(){
        Category category =new Category();
        //根据id删除分类信息，id（20）必须为数据库存在的，否则删除失败 测试id 12345678
        category.setId((long)12345678);
        category.setType(1);
        category.setName("北京烤鸭");
        category.setSort(8);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        log.info("修改分类信息：{}",category);

        final boolean update = categoryService.updateById(category);
        assertEquals("修改失败",true,update);
        System.out.println("修改成功");
    }

}
