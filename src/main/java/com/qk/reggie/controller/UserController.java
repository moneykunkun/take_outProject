package com.qk.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qk.reggie.common.R;
import com.qk.reggie.entity.User;
import com.qk.reggie.service.UserService;
import com.qk.reggie.utils.SMSUtils;
import com.qk.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送短信验证码
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //1.获取手机号
         String phone = user.getPhone();
        //验证手机号是否为空
        if (StringUtils.isNotEmpty(phone)){
            //2.生成4位随机的验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("验证码：{}",code);

            //3.调用阿里云提供的短信服务API完成短信发送
            //SMSUtils.sendMessage("阿里云已申请到的签名","自定义的模板",phone,code);

            //4.将生成的验证码保存到session中，用于验证验证码
            session.setAttribute(phone,code);
            return R.success("手机短信验证码发送成功");
        }
     return R.error("短信发送失败");
    }


    /**
     * 移动端登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {

        log.info(map.toString());
        //获取手机号
         String phone = map.get("phone").toString();
        //获取验证码
         String code = map.get("code").toString();
        //从session中获取保存的验证码
         Object codeInSession = session.getAttribute(phone);
        //页面提交的验证码和session中保存的验证码比对
        if (codeInSession !=null && codeInSession.equals(code)){
            //比对成功，说明登录成功
            LambdaQueryWrapper<User> queryWrapper =new LambdaQueryWrapper<>();
            //查询数据库中的手机号
            queryWrapper.eq(User::getPhone,phone);

             User user = userService.getOne(queryWrapper);
            if (user ==null){
                //判断当前手机号是否为新用户，如果是新用户就自动完成注册
                user =new User();
                user.setPhone(phone);
                user.setStatus(1);
                //完成注册
                userService.save(user);
            }
            //登录成功后，将userID存入session中
            session.setAttribute("user",user.getId());
            return  R.success(user);
        }
        return R.error("登录失败");
    }
    /**
     * 退出功能
     * ①在controller中创建对应的处理方法来接受前端的请求，请求方式为post；
     * ②清理session中的用户id
     * ③返回结果（前端页面会进行跳转到登录页面）
     * @return
     */
    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request){
        //清理session中的用户id
        request.getSession().removeAttribute("user");
        return R.success("退出成功");
    }
}
