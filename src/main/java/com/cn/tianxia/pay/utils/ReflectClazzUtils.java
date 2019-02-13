package com.cn.tianxia.pay.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.pay.service.PayService;

/**
 * 
 * @ClassName ReflectClazzUtils
 * @Description 获取反射接口工具类
 * @author Hardy
 * @Date 2018年9月29日 下午2:18:56
 * @version 1.0.0
 */
public class ReflectClazzUtils {
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(ReflectClazzUtils.class);
    
    //支付包名
    static final String PAY_PACKNAME = "com.cn.tianxia.pay.impl";
    //支付类名
    static final String PAY_CLAZZNAME = "PayServiceImpl";

    /**
     * 
     * @Description 获取支付反射接口
     * @param data
     * @param clazzName
     * @return
     * @throws Exception
     */
    public static PayService getPayService(Map<String,String> data,String clazzName) throws Exception{
        PayService payService = null;
        Constructor<?> constructor = null;
        StringBuffer sb = new StringBuffer();
        sb.append(PAY_PACKNAME.trim()).append(".");// 包名
        sb.append(clazzName.trim());//支付商名称
        sb.append(PAY_CLAZZNAME.trim());//类名
        try {
            //创建构造器
            constructor = Class.forName(sb.toString()).getConstructor(Map.class);
            payService = (PayService)constructor.newInstance(data);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            logger.error("获取支付反射接口异常:"+e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.error("获取支付反射接口异常:"+e.getMessage());
        } catch (InstantiationException e) {
            e.printStackTrace();
            logger.error("获取支付反射接口异常:"+e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            logger.error("获取支付反射接口异常:"+e.getMessage());
        }catch (InvocationTargetException e) {
            e.printStackTrace();
            logger.error("获取支付反射接口异常:"+e.getMessage());
        }
        return payService;
    }
    
    /**
     * 
     * @Description 获取支付反射接口
     * @param data
     * @param type
     * @param clazzName
     * @return
     * @throws Exception
     */
    public static PayService getPayService(Map<String,String> data,String type,String clazzName) throws Exception{
        PayService payService = null;
        Constructor<?> constructor = null;
        StringBuffer sb = new StringBuffer();
        sb.append(PAY_PACKNAME.trim()).append(".");// 包名
        sb.append(clazzName.trim());//支付商名称
        sb.append(PAY_CLAZZNAME.trim());//类名
        try {
            //创建构造器
            constructor = Class.forName(sb.toString()).getConstructor(Map.class,String.class);
            payService = (PayService)constructor.newInstance(data,type);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            logger.error("获取支付反射接口异常:"+e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.error("获取支付反射接口异常:"+e.getMessage());
        } catch (InstantiationException e) {
            e.printStackTrace();
            logger.error("获取支付反射接口异常:"+e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            logger.error("获取支付反射接口异常:"+e.getMessage());
        }catch (InvocationTargetException e) {
            e.printStackTrace();
            logger.error("获取支付反射接口异常:"+e.getMessage());
        }
        return payService;
    }
}
