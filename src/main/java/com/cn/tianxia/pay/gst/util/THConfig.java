package com.cn.tianxia.pay.gst.util;


import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by thinkpad on 2015/3/28.
 */
public class THConfig {


    private static String GATEWAY_URL = ""; //支付网关地址
    private static String MER_NO = ""; //这里填写商户号
    private static String MER_KEY = ""; //这里填写签名时需要的私钥key
    private static String CHARSET = "UTF-8"; //这里填写当前系统字符集编码，取值UTF-8或者GBK
    private static String BACK_NOTIFY_URL = ""; // 这里填写支付完成后，支付平台后台通知当前支付是否成功的URL
    private static String PAGE_NOTIFY_URL = ""; // 这里填写支付完成后，页面跳转到商户页面的URL，同时告知支付是否成功
    private static String PAY_TYPE = "1"; //支付方式，目前暂只支持网银支付，取值为1
    private static String REQ_REFERER = "116.226.209.138";//请指定当前系统的域名，用来防钓鱼，例如www.mer.com
    private static String REQ_CUSTOMER_IP = "116.226.209.138";
    public static String getUrl(HttpServletRequest request)
    {
        try {
            request.setCharacterEncoding(CHARSET);
        } catch (UnsupportedEncodingException e) {
        }
        String bankCode = request.getParameter(AppConstants.BANK_CODE);
        String orderNo = request.getParameter(AppConstants.ORDER_NO);
      //  String orderAmount =request.getParameter(AppConstants.ORDER_AMOUNT);;
        String orderAmount=request.getParameter(AppConstants.ORDER_AMOUNT);
        	orderAmount= AES.encrypt(orderAmount, MER_KEY);
        if (StringUtils.isNullOrEmpty(orderNo))
        {
            throw new RuntimeException("请求的参数订单号为空");
        }
        if (StringUtils.isNullOrEmpty(orderAmount))
        {
            throw new RuntimeException("请求的参数订单金额为空");
        }
      
        String referer = REQ_REFERER;
        String customerIp = HttpUtils.getAddr(request);
        if (REQ_CUSTOMER_IP != null)
            customerIp = REQ_CUSTOMER_IP;
        String returnParams = request.getParameter(AppConstants.RETURN_PARAMS);
        String currentDate = DateUtils.format(new Date());

        KeyValues kvs = new KeyValues();
        kvs.add(new KeyValue(AppConstants.INPUT_CHARSET, CHARSET));
        kvs.add(new KeyValue(AppConstants.NOTIFY_URL, BACK_NOTIFY_URL));
        kvs.add(new KeyValue(AppConstants.RETURN_URL, PAGE_NOTIFY_URL));
        kvs.add(new KeyValue(AppConstants.PAY_TYPE, PAY_TYPE));
        kvs.add(new KeyValue(AppConstants.BANK_CODE, bankCode));
        kvs.add(new KeyValue(AppConstants.MERCHANT_CODE, MER_NO));
        kvs.add(new KeyValue(AppConstants.ORDER_NO, orderNo));
        kvs.add(new KeyValue(AppConstants.ORDER_AMOUNT, orderAmount));
        kvs.add(new KeyValue(AppConstants.ORDER_TIME, currentDate));
        kvs.add(new KeyValue(AppConstants.REQ_REFERER, referer));
        kvs.add(new KeyValue(AppConstants.CUSTOMER_IP, customerIp));
        kvs.add(new KeyValue(AppConstants.RETURN_PARAMS, returnParams));
        String sign = kvs.sign(MER_KEY, CHARSET);

        StringBuilder sb = new StringBuilder();
        sb.append(GATEWAY_URL);
        URLUtils.appendParam(sb, AppConstants.INPUT_CHARSET, CHARSET, false);
        URLUtils.appendParam(sb, AppConstants.RETURN_URL, PAGE_NOTIFY_URL, CHARSET);
        URLUtils.appendParam(sb, AppConstants.NOTIFY_URL, BACK_NOTIFY_URL, CHARSET);
        URLUtils.appendParam(sb, AppConstants.PAY_TYPE, PAY_TYPE);
        URLUtils.appendParam(sb, AppConstants.BANK_CODE, bankCode);
        URLUtils.appendParam(sb, AppConstants.MERCHANT_CODE, MER_NO);
        URLUtils.appendParam(sb, AppConstants.ORDER_NO, orderNo);
        URLUtils.appendParam(sb, AppConstants.ORDER_AMOUNT, orderAmount);
        URLUtils.appendParam(sb, AppConstants.ORDER_TIME, currentDate);
        URLUtils.appendParam(sb, AppConstants.REQ_REFERER, referer, CHARSET);
        URLUtils.appendParam(sb, AppConstants.CUSTOMER_IP, customerIp);
        URLUtils.appendParam(sb, AppConstants.RETURN_PARAMS, returnParams, CHARSET);
        URLUtils.appendParam(sb, AppConstants.SIGN, sign);
        System.out.println(sb.toString());
        return sb.toString();
    }

    public static boolean validPageNotify(HttpServletRequest req)
    {
        String merchantCode = req.getParameter(AppConstants.MERCHANT_CODE);
        String orderNo = req.getParameter(AppConstants.ORDER_NO);
        String orderAmount = req.getParameter(AppConstants.ORDER_AMOUNT);
        String orderTime = req.getParameter(AppConstants.ORDER_TIME);
        String returnParams = req.getParameter(AppConstants.RETURN_PARAMS);
        String tradeNo = req.getParameter(AppConstants.TRADE_NO);
        String tradeStatus = req.getParameter(AppConstants.TRADE_STATUS);
        String sign = req.getParameter(AppConstants.SIGN);
        
        
        KeyValues kvs = new KeyValues();
        kvs.add(new KeyValue("merchant_code", merchantCode));
        kvs.add(new KeyValue("order_no", orderNo));
        kvs.add(new KeyValue("order_time", orderTime));
        kvs.add(new KeyValue("order_amount", orderAmount));
        kvs.add(new KeyValue("trade_status", tradeStatus));
        kvs.add(new KeyValue("trade_no", tradeNo));
        kvs.add(new KeyValue("return_params", returnParams));
        String thizSign = kvs.sign(MER_KEY, CHARSET);
        if (thizSign.equalsIgnoreCase(sign))
            return true;
        else
            return false;
    }

    public static String getGatewayUrl() {
        return GATEWAY_URL;
    }

    public static void setGatewayUrl(String gatewayUrl) {
        GATEWAY_URL = gatewayUrl;
    }

    public static String getMerNo() {
        return MER_NO;
    }

    public static void setMerNo(String merNo) {
        MER_NO = merNo;
    }

    public static String getMerKey() {
        return MER_KEY;
    }

    public static void setMerKey(String merKey) {
        MER_KEY = merKey;
    }

    public static String getCharset() {
        return CHARSET;
    }

    public static void setCharset(String charset) {
        THConfig.CHARSET = charset;
    }

    public static String getBackNotifyUrl() {
        return BACK_NOTIFY_URL;
    }

    public static void setBackNotifyUrl(String backNotifyUrl) {
        BACK_NOTIFY_URL = backNotifyUrl;
    }

    public static String getPageNotifyUrl() {
        return PAGE_NOTIFY_URL;
    }

    public static void setPageNotifyUrl(String pageNotifyUrl) {
        PAGE_NOTIFY_URL = pageNotifyUrl;
    }

    public static String getPayType() {
        return PAY_TYPE;
    }

    public static void setPayType(String payType) {
        PAY_TYPE = payType;
    }

    public static String getReqReferer() {
        return REQ_REFERER;
    }

    public static void setReqReferer(String reqReferer) {
        REQ_REFERER = reqReferer;
    }

    public static void setReqCustomerIp(String reqCustomerIp) {
        REQ_CUSTOMER_IP = reqCustomerIp;
    }
}
