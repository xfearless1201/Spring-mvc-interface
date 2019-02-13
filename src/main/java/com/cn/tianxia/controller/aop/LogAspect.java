/******************************************************************
 *
 *    Powered By tianxia-online.
 *
 *    Copyright (c) 2018-2020 Digital Telemedia 天下网络
 *    http://www.d-telemedia.com/
 *
 *    Package:     com.tianxia.business.api.service.aop
 *
 *    Filename:    GetpidAspect.java
 *
 *    Description: 通过切面获取平台ID
 *
 *    Copyright:   Copyright (c) 2018-2020
 *
 *    Company:     天下网络科技
 *
 *    @author: HH
 *
 *    @version: 1.0.0
 *
 *    Create at:   2018年7月6日 下午4:52:29
 *
 *    Revision:
 *
 *    2018年7月6日 下午4:52:29
 *        - first revision
 *
 *****************************************************************/
package com.cn.tianxia.controller.aop;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;

import net.sf.json.JSONObject;

/**
 * @author HH
 * @version 1.0.0
 * @ClassName GetpidAspect
 * @Description 通过切面获取平台ID
 * @Date 2018年7月6日 下午4:52:29
 */

@Aspect
@Component
public class LogAspect {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    @Pointcut("execution(public * com.cn.tianxia.controller.*.*(..))")
    public void aspect() {
    }

    @Before("aspect()")
    public void deBefore(JoinPoint joinPoint) throws Throwable {
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        request = attributes.getRequest();
        // 记录下请求内容

        String CLASS_METHOD=joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

        Map<String, String> maps = new HashMap<String, String>();
        maps.put("URL",request.getRequestURL().toString());
        maps.put("HTTP_METHOD",request.getMethod());
        maps.put("IP",request.getRemoteAddr());

        Enumeration enu = request.getParameterNames();
        while (enu.hasMoreElements()) {
            String paraName = (String) enu.nextElement();
            maps.put(paraName, request.getParameter(paraName));
        }

        logger.info("\n 执行方法：[{}] \n 请求参数：{}",CLASS_METHOD,JSON.toJSONString(maps));
    }

    @AfterReturning(returning = "ret", pointcut = "aspect()")
    public void doAfterReturning(JoinPoint joinPoint,Object ret) throws Throwable {
        // 处理完请求，返回内容
        System.out.println("方法的返回值 : " + ret);
        logger.info("\n 执行方法：[{}] \n 返回结果：{}", joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName(),
                JSONObject.fromObject(ret).toString());
    }

}
