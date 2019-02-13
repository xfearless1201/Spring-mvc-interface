package com.cn.tianxia.pay.tjf.utils;


/** Title: Config 基础参数配置类
 *  Description:
 *  设置商户相关信息及证书文件和通知地址等
 *  以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *  该代码仅供学习和研究接口使用，只是提供一个参考。
 *  Copyright: Copyright (c) 2017 91cloud, All Rights Reserved
 *  
 *
 *  @author Java Development Group
 *  @version 1.0
 */
public class Config {

	// 请选择签名类型，MD5或RSA， 默认MD5
    public static  String SIGN_TYPE = "";
   
    // 商户的MD5密钥
    public static  String KEY = "";

    public static  String MERCHANT_ID = "";

    // 商户的通知地址
    public static  String MERCHANT_NOTIFY_URL = "";

    // 商户的网关地址
    public static  String GATEWAY_URL = "";

    // 商户的版本号
    public static String API_VERSION = "1.0.0.0";
    
    public static String SUMMARY = "";

    // 商户的配置
    public static  final String APINAME_PAY = "TRADE.B2C";
    public static  final String APINAME_SCANPAY = "TRADE.SCANPAY";
    public static  final String APINAME_H5PAY = "TRADE.H5PAY";
 
    public static final String APINAME_NOTIFY = "TRADE.NOTIFY";
    
    
    
}
