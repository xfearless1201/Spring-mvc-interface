package com.cn.tianxia.pay.jh.util;

import java.io.IOException;
import java.util.Properties;

/**
 * <b>功能说明:龙果支付属性配置工具类
 * </b>
 * @author 
 */
public class PayConfigUtil {

    /**
     * 通过静态代码块读取上传文件的验证格式配置文件,静态代码块只执行一次(单例)
     */
    private static Properties properties = new Properties();

    private PayConfigUtil() {

    }

    // 通过类装载器装载进来
    static {
        try {
            // 从类路径下读取属性文件
            properties.load(PayConfigUtil.class.getClassLoader()
                    .getResourceAsStream("pay_config.properties"));
        } catch (IOException e) {
            
        }
    }

    /**
     * 函数功能说明 ：读取配置项 Administrator 2012-12-14 修改者名字 ： 修改日期 ： 修改内容 ：
     *
     * @参数：
     * @return void
     * @throws
     */
    public static String readConfig(String key) {
        return (String) properties.get(key);
    }
}
