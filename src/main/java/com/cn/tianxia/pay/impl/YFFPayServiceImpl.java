package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.json.JSONUtils;
import com.cn.tianxia.pay.dc.util.HttpClientUtil;
import com.cn.tianxia.pay.qyf.util.ToolKit;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * @ClassName YFFPayServiceImpl
 * @Description 溢发支付
 * @author zw
 * @Date 2018年8月18日 下午7:11:13
 * @version 1.0.0
 */
public class YFFPayServiceImpl implements PayService {

    private final static Logger logger = LoggerFactory.getLogger(YFFPayServiceImpl.class);

    /** 支付地址 **/
    private String api_url;

    /** h5支付地址 **/
    private static String h5_url;

    /** 商户号 **/
    private String merCode;

    /** md5key **/
    private String md5Key;

    /** 商品名 **/
    private String productDesc;

    /** 有效时间 **/
    private String validityNum;

    /** 通知回调地址 **/
    private String callbackUrl;

    public YFFPayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("api_url")) {
                this.api_url = pmap.get("api_url");
            }
            if (pmap.containsKey("h5_url")) {
                this.h5_url = pmap.get("h5_url");
            }
            if (pmap.containsKey("merCode")) {
                this.merCode = pmap.get("merCode");
            }
            if (pmap.containsKey("md5Key")) {
                this.md5Key = pmap.get("md5Key");
            }
            if (pmap.containsKey("productDesc")) {
                this.productDesc = pmap.get("productDesc");
            }
            if (pmap.containsKey("validityNum")) {
                this.validityNum = pmap.get("validityNum");
            }
            if (pmap.containsKey("callbackUrl")) {
                this.callbackUrl = pmap.get("callbackUrl");
            }
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        // String form = scanPay(payEntity);
        // return PayUtil.returnWYPayJson("success", "form", form, pay_url, "");
        return null;
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        String mobile = payEntity.getMobile();
        JSONObject r_json = null;
        // pc端接口
        if (StringUtils.isBlank(mobile)) {
            r_json = Pay(payEntity);
        } else {
            // 手机端接口
            r_json = h5Pay(payEntity);
        }

        if ("success".equals(r_json.getString("status"))) {
            if (StringUtils.isBlank(mobile)) {
                return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", payEntity.getUsername(),
                        payEntity.getAmount(), payEntity.getOrderNo(), r_json.getString("qrCode"));
            } else {
                return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", payEntity.getUsername(),
                        payEntity.getAmount(), payEntity.getOrderNo(), r_json.getString("qrCode"));
            }
        } else {
            return PayUtil.returnPayJson("error", "2", r_json.getString("msg"), payEntity.getUsername(),
                    payEntity.getAmount(), payEntity.getOrderNo(), "");
        }
    }

    /**
     * @Description 二维码支付接口
     * @param payEntity
     * @return
     */
    public JSONObject Pay(PayEntity payEntity) {
        DecimalFormat df = new DecimalFormat("#############");
        String orderAmount = df.format(payEntity.getAmount() * 100);// 分为单位

        String payType = payEntity.getPayCode();// 支付方式

        String showUrl = payEntity.getRefererUrl();// 前台回调地址

        String orderNo = payEntity.getOrderNo();// 商户订单号

        String dateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        Map<String, Object> paramsMap = new HashMap<>();

        String sign = "";

        paramsMap.put("orderNo", orderNo);
        paramsMap.put("orderAmount", orderAmount);
        paramsMap.put("callbackUrl", callbackUrl);
        paramsMap.put("showUrl", showUrl);
        paramsMap.put("payType", payType);
        paramsMap.put("productDesc", productDesc);
        paramsMap.put("merCode", merCode);
        paramsMap.put("dateTime", dateTime);
        paramsMap.put("validityNum", Integer.valueOf(validityNum));
        // 签名
        sign = ToolKit.MD5(buildPrePayParams(paramsMap, md5Key), "UTF-8").toLowerCase();
        paramsMap.put("sign", sign);

        String res = "";
        try {
            String postJson = JSONObject.fromObject(paramsMap).toString();
            logger.info("溢发支付请求参数:" + postJson);
            res = HttpClientUtil.doPost(api_url, postJson, "UTF-8", "application/json");

            logger.info("溢发支付响应参数字符:" + res);
            JSONObject resposeJson = JSONObject.fromObject(res);

            if (resposeJson.containsKey("resultCode") && "000000".equals(resposeJson.getString("resultCode"))
                    && resposeJson.containsKey("resultStatus")
                    && "SUCCESS".equals(resposeJson.getString("resultStatus"))) {
                String qrCodeUrl = resposeJson.getString("qrCodeUrl");
                return getReturnJson("success", qrCodeUrl, "二维码连接获取成功！");
            }
            return getReturnJson("error", "", res);
        } catch (Exception e) {
            e.printStackTrace();
            return getReturnJson("error", "", res);
        }
    }

    /**
     * @Description h5接口
     * @param payEntity
     * @return
     */
    public JSONObject h5Pay(PayEntity payEntity) {
        DecimalFormat df = new DecimalFormat("#############");
        String orderAmount = df.format(payEntity.getAmount() * 100);// 分为单位

        String payType = payEntity.getPayCode();// 支付方式

        String showUrl = payEntity.getRefererUrl();// 前台回调地址

        String orderNo = payEntity.getOrderNo();// 商户订单号

        String dateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        Map<String, Object> paramsMap = new HashMap<>();

        String sign = "";

        paramsMap.put("orderNo", orderNo);
        paramsMap.put("orderAmount", orderAmount);
        paramsMap.put("callbackUrl", callbackUrl);
        paramsMap.put("showUrl", showUrl);
        paramsMap.put("cancelUrl", showUrl);
        paramsMap.put("payType", payType);
        paramsMap.put("productDesc", productDesc);
        paramsMap.put("merCode", merCode);
        paramsMap.put("dateTime", dateTime);
//        paramsMap.put("validityNum", Integer.valueOf(validityNum));
        // 签名
        sign = ToolKit.MD5(buildPrePayParams(paramsMap, md5Key), "UTF-8").toLowerCase();
        paramsMap.put("sign", sign);

        String res = "";
        try {
            String postJson = JSONObject.fromObject(paramsMap).toString();
            logger.info("溢发h5支付请求参数:" + postJson);
            res = HttpClientUtil.doPost(h5_url, postJson, "UTF-8", "application/json");

            logger.info("溢发h5支付响应参数字符:" + res);
            JSONObject resposeJson = JSONObject.fromObject(res);

            if (resposeJson.containsKey("resultCode") && "000000".equals(resposeJson.getString("resultCode"))
                    && resposeJson.containsKey("resultStatus")
                    && "SUCCESS".equals(resposeJson.getString("resultStatus"))) {
                String qrCodeUrl = resposeJson.getString("qrCodeUrl");
                return getReturnJson("success", qrCodeUrl, "二维码连接获取成功！");
            }
            return getReturnJson("error", "", res);
        } catch (Exception e) {
            e.printStackTrace();
            return getReturnJson("error", "", res);
        }
    }

    /**
     * @Description (TODO这里用一句话描述这个方法的作用)
     * @param sb
     * @param payParams
     * @param md5Key
     */
    public static String buildPrePayParams(Map<String, Object> payParams, String md5Key) {
        StringBuilder sb = new StringBuilder((payParams.size() + 1) * 10);
        List<String> keys = new ArrayList<String>(payParams.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            String str = String.valueOf(payParams.get(key));
            if (str == null || str.length() == 0) {
                // 空串不参与sign计算
                continue;
            }
            sb.append(key).append("=");
            sb.append(str);
            sb.append("&");
        }
        sb.setLength(sb.length() - 1);
        sb.append(md5Key);
        logger.info("溢发待签名字符:" + sb.toString());
        return sb.toString();
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
    
    @Override
    public String callback(Map<String, String> map) {
        String serverSign = map.remove("sign");

        Map<String, Object> paramsMap = JSONUtils.toHashMap(map);

        String localSign = ToolKit.MD5(buildPrePayParams(paramsMap, md5Key), "UTF-8").toLowerCase();

        logger.info("本地签名:" + localSign + "      服务器签名:" + serverSign);
        if (serverSign.equalsIgnoreCase(localSign)) {
            return "success";
        }
        return "";
    }

}
