package com.qk.reggie.common;

/**
 * 基于ThreadLocal的封装工具类，用于保存和获取当前登录用户的id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal =new ThreadLocal<>();

    //设置当前线程的id
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    //获取当前线程id
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
