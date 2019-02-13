package com.cn.tianxia.pay.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.json.JSONUtils;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.ys.util.DateUtil;

import net.sf.json.JSONObject;

/**
 * @ClassName QYZFPayServiceImpl
 * @Description jsg对接全银支付
 * @author zw
 * @Date 2018年7月21日 下午2:00:49
 * @version 1.0.0
 */
public class QYZFPayServiceImpl implements PayService {

    private String payWayCode;// 支付方式编码,固定为ZITOPAY

    private String payKey;// 商户的支付key,用于标识商户身份

    private String Md5Key;// 密钥

    private String productName;

    private String orderPeriodStr = "5"; // 订单有效期 分钟

    private String notifyUrl;// 支付成功结果异步通知url地址

    private String payUrl;// 支付地址

    private final static Logger logger = LoggerFactory.getLogger(QYZFPayServiceImpl.class);

    private static final String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e",
            "f" };

    public QYZFPayServiceImpl(Map<String, String> pmap) {
        net.sf.json.JSONObject jo = JSONObject.fromObject(pmap);
        if (null != pmap) {
            payWayCode = jo.get("payWayCode").toString();
            payKey = jo.get("payKey").toString();
            Md5Key = jo.get("Md5Key").toString();
            productName = jo.get("productName").toString();
            orderPeriodStr = jo.get("orderPeriodStr").toString();
            notifyUrl = jo.get("notifyUrl").toString();
            payUrl = jo.get("payUrl").toString();
        }
    }

    public String bankPay(PayEntity payEntity) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        String orderPriceStr = String.valueOf(payEntity.getAmount()); // 订单金额 , 单位:元

        String currT = DateUtil.getCurrentDate("yyyyMMddHHmmss");
        // 四位随机数
        String strRandom = DateUtil.getRandom(4) + "";
        // 订单号，此处用时间加随机数生成，商户根据自己情况调整，只要保持全局唯一就行
        String order_no = payEntity.getTopay().toLowerCase() + currT + strRandom;

        String orderIp = payEntity.getIp();
        String returnUrl = payEntity.getRefererUrl();
        String payTypeCode = payEntity.getPayCode();

        paramMap.put("orderPrice", orderPriceStr);
        paramMap.put("payWayCode", payWayCode);// 支付方式编码
        paramMap.put("payTypeCode", payTypeCode);// 网关支付编码 "ZITOPAY_BANK_SCAN"
        paramMap.put("orderNo", order_no);// 订单号

        payEntity.setOrderNo(order_no);
        Date orderDate = new Date();// 订单日期
        String orderDateStr = new SimpleDateFormat("yyyyMMdd").format(orderDate);// 订单日期
        paramMap.put("orderDate", orderDateStr);

        Date orderTime = new Date();// 订单时间
        String orderTimeStr = new SimpleDateFormat("yyyyMMddHHmmss").format(orderTime);// 订单时间
        paramMap.put("orderTime", orderTimeStr);

        paramMap.put("payKey", payKey);
        paramMap.put("productName", productName);
        paramMap.put("orderIp", orderIp);

        // String orderPeriodStr = "5"; // 订单有效期
        paramMap.put("orderPeriod", orderPeriodStr);
        // String returnUrl = "http://127.0.0.1/test/pageReturn.jsp"; // 页面通知返回url
        paramMap.put("returnUrl", returnUrl);
        // String notifyUrl = "http://127.0.0.1/test/notify.jsp"; // 后台消息通知Url
        paramMap.put("notifyUrl", notifyUrl);
        // String remark = "TXWL"; // 支付备注
        // paramMap.put("remark", remark);
        //////////// 扩展字段,选填,原值返回///////////
        // 预留字段（该字段在支付结果通知中和页面回掉中被传送）
        // 当需要指定具体银行的时候，此字段为银行编码，银行编码可见下面 银行编码表,开通此功能需和全银平台确认
        // String field5 = "扩展字段5"; // 扩展字段5
        // paramMap.put("field5", field5);
        ///// 签名及生成请求API的方法///
        String sign = getSign(paramMap, Md5Key);
        paramMap.put("sign", sign);

        StringBuffer debug = new StringBuffer();
        String content = toParams(paramMap, debug);
        String resultStr = httpCall(payUrl, debug.toString(), content, "POST");
        logger.info("全银支付响应:" + resultStr);
        Map<String, Object> resultMap = JSON.parseObject(resultStr, Map.class);

        if ("success".equals(resultMap.get("result")))
            return resultMap.get("code_url").toString();
        return "";
    }

    public JSONObject scanPay(PayEntity payEntity) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        String orderPriceStr = String.valueOf(payEntity.getAmount()); // 订单金额 , 单位:元

        String currT = DateUtil.getCurrentDate("yyyyMMddHHmmss");
        // 四位随机数
        String strRandom = DateUtil.getRandom(4) + "";
        // 订单号，此处用时间加随机数生成，商户根据自己情况调整，只要保持全局唯一就行
        String order_no = payEntity.getTopay().toLowerCase() + currT + strRandom;

        String orderIp = payEntity.getIp();
        String returnUrl = payEntity.getRefererUrl();
        String payTypeCode = payEntity.getPayCode();

        paramMap.put("orderPrice", orderPriceStr);
        paramMap.put("payWayCode", payWayCode);// 支付方式编码
        paramMap.put("payTypeCode", payTypeCode);// 网关支付编码 "ZITOPAY_BANK_SCAN"
        paramMap.put("orderNo", order_no);// 订单号

        payEntity.setOrderNo(order_no);
        Date orderDate = new Date();// 订单日期
        String orderDateStr = new SimpleDateFormat("yyyyMMdd").format(orderDate);// 订单日期
        paramMap.put("orderDate", orderDateStr);

        Date orderTime = new Date();// 订单时间
        String orderTimeStr = new SimpleDateFormat("yyyyMMddHHmmss").format(orderTime);// 订单时间
        paramMap.put("orderTime", orderTimeStr);

        paramMap.put("payKey", payKey);
        paramMap.put("productName", productName);
        paramMap.put("orderIp", orderIp);

        // String orderPeriodStr = "5"; // 订单有效期
        paramMap.put("orderPeriod", orderPeriodStr);
        // String returnUrl = "http://127.0.0.1/test/pageReturn.jsp"; // 页面通知返回url
        paramMap.put("returnUrl", returnUrl);
        // String notifyUrl = "http://127.0.0.1/test/notify.jsp"; // 后台消息通知Url
        paramMap.put("notifyUrl", notifyUrl);
        // String remark = "TXWL"; // 支付备注
        // paramMap.put("remark", remark);
        //////////// 扩展字段,选填,原值返回///////////
        // 预留字段（该字段在支付结果通知中和页面回掉中被传送）
        // 当需要指定具体银行的时候，此字段为银行编码，银行编码可见下面 银行编码表,开通此功能需和全银平台确认
        // String field5 = "扩展字段5"; // 扩展字段5
        // paramMap.put("field5", field5);
        ///// 签名及生成请求API的方法///
        String sign = getSign(paramMap, Md5Key);
        paramMap.put("sign", sign);

        String resultStr = "";
        try {
            StringBuffer debug = new StringBuffer();
            String content = toParams(paramMap, debug);
            resultStr = httpCall(payUrl, debug.toString(), content, "POST");
            logger.info("全银支付响应:" + resultStr);
            Map<String, Object> resultMap = JSON.parseObject(resultStr, Map.class);
            if ("success".equals(resultMap.get("result"))) {
                return getReturnJson("success", resultMap.get("code_url").toString(), "二维码连接获取成功！");
            } else {
                return getReturnJson("error", resultMap.get("msg").toString(), "二维码连接获取成功！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getReturnJson("error", resultStr, "二维码连接获取成功！");
    }

    /**
     * 结果返回
     * 
     * @param status
     * @param qrCode
     * @param msg
     * @return
     */
    private JSONObject getReturnJson(String status, String qrCode, String msg) {
        JSONObject json = new JSONObject();
        json.put("status", status);
        json.put("qrCode", qrCode);
        json.put("msg", msg);
        return json;
    }

    public static String toParams(Map<String, Object> params, StringBuffer debugStr) {
        StringBuffer sbResult = new StringBuffer();

        Set<String> names = params.keySet();
        for (String name : names) {
            String value = params.get(name).toString();
            try {
                if (sbResult.length() > 0) {
                    sbResult.append("&");
                    debugStr.append("&");
                }
                sbResult.append(name + "=" + URLEncoder.encode(value, "utf-8"));
                debugStr.append(name + "=" + value);
            } catch (Throwable e) {
            }
        }
        return sbResult.toString();
    }

    public static String encode(String password) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] byteArray = md5.digest(password.getBytes("utf-8"));
            String passwordMD5 = byteArrayToHexString(byteArray);
            return passwordMD5;
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return password;
    }

    private static String byteArrayToHexString(byte[] byteArray) {
        StringBuffer sb = new StringBuffer();
        for (byte b : byteArray) {
            sb.append(byteToHexChar(b));
        }
        return sb.toString();
    }

    private static Object byteToHexChar(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hex[d1] + hex[d2];
    }

    /**
     * @Description 字符签名
     * @param paramMap
     * @param paySecret
     * @return
     */
    public static String getSign(Map<String, Object> paramMap, String paySecret) {
        Map<String, Object> smap = new TreeMap<String, Object>();
        smap.putAll(paramMap);
        StringBuffer stringBuffer = new StringBuffer();
        Set<String> keys = smap.keySet();
        for (String key : keys) {
            Object value = smap.get(key);
            if (value != null && StringUtils.isNotBlank(String.valueOf(value))) {
                stringBuffer.append(key).append("=").append(value).append("&");
            }
        }
        stringBuffer.delete(stringBuffer.length() - 1, stringBuffer.length());

        String argPreSign = stringBuffer.append("&paySecret=").append(paySecret).toString();
        logger.warn("签名前：" + argPreSign);
        String signStr = encode(argPreSign).toUpperCase();
        logger.warn("签名值：" + signStr);
        return signStr;
    }

    /**
     * @Description http Post
     * @param addr
     * @param debugStr
     * @param content
     * @param method
     * @return
     */
    public static String httpCall(String addr, String debugStr, String content, String method) {
        HttpURLConnection urlCon = null;
        try {
            URL url = new URL(addr);
            Object con = url.openConnection();
            if (HttpsURLConnection.class.isInstance(con))
                urlCon = (HttpsURLConnection) con;
            else
                urlCon = (HttpURLConnection) con;

            urlCon.setConnectTimeout(15000);
            urlCon.setReadTimeout(40000);

            urlCon.setDoOutput(true);
            urlCon.setDoInput(true);
            if (StringUtils.isNotBlank(content)) {
                method = "POST";
            }
            urlCon.setRequestMethod(method);
            urlCon.setUseCaches(false);
            urlCon.setInstanceFollowRedirects(true);
            if ("POST".equalsIgnoreCase(method))
                urlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            urlCon.connect();

            if (StringUtils.isNotBlank(content)) {
                logger.warn("ready call[" + addr + "],send content[" + debugStr + "]");
                OutputStream out = urlCon.getOutputStream();
                out.write(content.getBytes());
                out.flush();
                out.close();
            } else {
                logger.warn("ready call[" + addr + "] for " + method + ",and params=[" + debugStr + "]");
            }

            int state = urlCon.getResponseCode();
            if (state != 200) {
                logger.warn("callservice visit addr[" + addr + "] fail:http rsp code[" + state + "]");
                throw new RuntimeException(
                        "agentPay callservice visit addr[" + addr + "] fail:http rsp code[" + state + "]");
            }
            String charSet = "UTF-8";
            String recvContentType = urlCon.getContentType();
            if (StringUtils.isNotBlank(recvContentType)) {
                Pattern pattern = Pattern.compile("charset=\\S*");
                Matcher matcher = pattern.matcher(recvContentType);
                if (matcher.find()) {
                    charSet = matcher.group().replace("charset=", "");
                }
            }
            InputStream in = urlCon.getInputStream();

            byte[] temp = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int readBytes = in.read(temp);
            while (readBytes > 0) {
                baos.write(temp, 0, readBytes);
                readBytes = in.read(temp);
            }
            String resultString = new String(baos.toByteArray(), charSet);
            baos.close();
            in.close();
            urlCon.disconnect();
            urlCon = null;

            logger.warn("call[" + addr + "],send content[" + content + "],recv[" + resultString + "]");
            return resultString;
        } catch (Throwable e) {
            if (RuntimeException.class.isInstance(e))
                throw (RuntimeException) e;
            logger.warn("callservice visit addr[" + addr + "] fail", e);
            throw new RuntimeException(e.toString());
        } finally {
            if (urlCon != null) {
                urlCon.disconnect();
            }
        }
    }

    /**
     * @Description 回调验签方法
     * @param map
     * @return
     */
    @Override
    public String callback(Map<String, String> map) {
        Map<String, Object> paramMap = JSONUtils.toHashMap(map);

        String ServenSign = paramMap.remove("sign").toString();

        String localSign = getSign(paramMap, Md5Key);

        logger.info("本地签名:" + localSign + "      服务器签名:" + ServenSign);
        if (localSign.equalsIgnoreCase(ServenSign)) {
            return "success";
        }
        return "";
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        String url = bankPay(payEntity);
        return PayUtil.returnWYPayJson("success", "link", url, payEntity.getPayUrl(), "");
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        String mobile = payEntity.getMobile();
        Double amount = payEntity.getAmount();
        String pay_code = payEntity.getPayCode();
        // String order_no = payEntity.getOrderNo();
        String userName = payEntity.getUsername();

        JSONObject r_json = scanPay(payEntity);

        if ("success".equals(r_json.getString("status"))) {
            // pc端
            if (StringUtils.isBlank(mobile)) {
                // pc 快捷支付
                if (pay_code.equals("OPSPAY_QUICKPAY_PC")) {
                    return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, payEntity.getOrderNo(),
                            r_json.getString("qrCode"));
                }
                return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, payEntity.getOrderNo(),
                        r_json.getString("qrCode"));
            } else {
                // 手机端
                return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, payEntity.getOrderNo(),
                        r_json.getString("qrCode"));
            }
        } else {
            return PayUtil.returnPayJson("error", "2", r_json.getString("msg"), userName, amount,
                    payEntity.getOrderNo(), "");
        }
    }
}
