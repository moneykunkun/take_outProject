package com.qk.tangren.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice(annotations = {RestController.class, Controller.class})           //拦截异常的类
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常处理方法
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {      //捕获到的异常
            log.error(ex.getMessage());

        if (ex.getMessage().contains("Duplicate entry")){       //表明用户已存在
            String[] split =ex.getMessage().split(" ");         //获取异常信息中的用户名
            String msg =split[2] +"已存在";
            return R.error(msg);
        }
            return R.error("未知错误！");
    }

    /**
     * 处理自定义异常
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex) {      //捕获到的异常
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }
}
