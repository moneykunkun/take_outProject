package com.qk.reggie.controller;

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

import javax.servlet.http.HttpSession;

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

}
