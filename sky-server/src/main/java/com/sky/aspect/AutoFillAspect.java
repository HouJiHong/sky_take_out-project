package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 用于处理在插入更新时，对重复操作的提取，用aop实现
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    //本来时直接在通知注解上添加切入点表达式，现提取出
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    //前置通知
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始公共字段填充");

        //获取当前被拦截方法上的sql操作类型
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();//由于注解是放在方法上的,使用其实现方法
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class); //获取其注解对象
        OperationType value = annotation.value(); //获取sql操作类型

        //获取当前被拦截方法上的参数-实体对象(默认第一个参数是实体对象)
        Object[] args = joinPoint.getArgs();
        if(args ==null || args.length==0){
            return;
        }
        Object entity = args[0];
        //准备赋值数据
        LocalDateTime time = LocalDateTime.now();
        Long user = BaseContext.getCurrentId();

        //根据当前不同操作类型，通过反射为实体的参数赋值
        if(value == OperationType.INSERT){
            try {
                Method cTi = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method cUT = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method cU = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
                Method cUU = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
                cTi.invoke(entity,time);
                cUT.invoke(entity,time);
                cU.invoke(entity,user);
                cUU.invoke(entity,user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(value == OperationType.UPDATE){
            try {
                Method cUT = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method cUU = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
                cUT.invoke(entity,time);
                cUU.invoke(entity,user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
