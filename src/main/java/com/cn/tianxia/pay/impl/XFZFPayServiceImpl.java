package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.HttpClient;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.xfzf.util.RSAUtil;

import net.sf.json.JSONObject;

/**
 * 兴付支付
 * 
 * @author hb
 * @date 2018-06-01
 */
public class XFZFPayServiceImpl implements PayService {

    private final static Logger logger = LoggerFactory.getLogger(XFZFPayServiceImpl.class);
    /** 支付地址 */
    private String payUrl;
    /** 密钥 */
    private String apiKey;
    /** 私钥 */
    private String privateKey;// ="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIFH3aeTZNJdIuBZJwxX2Z+XC+wtr3jGS4IZEharaWPow6NBxC2c9E1pyl1DuSa8kj6Tnj84E+NGbR5VMht5R7NjGn1QMeh5DcZUcRf5p3432wMvpA9VhfC2YDbAt8bTCJnsUD5w1Yfg2Wa7MBXn9L+/JkieFbPR1x4/TxzIUOSNAgMBAAECgYAd2BJCMhNvQJLhiZMrFUimv76/2dgNIHGj6de0JgPhTYWENZVtFjOLf2V43D5sw9Fa4scAPxjQOZzNCMvVbczwN2SMwpdhGtuseAhrXxC2nrJDydRWtHQ7YcbW/d2lwjYETPo5vDJhDaJc2bUlhCB7/4N5LrVjGX3b2lA0BH/BvQJBALskvXRBbwOVum9eLVF7yQlkeEElmdbrLBVDCg+9nx/qCPx0dvXPDT+Xx1NLj1JFFUPLcywpYzQxLW54itD/sfMCQQCw2PbIZKBW8momiy9u5CdOxHkprpcwJGyjpfxnQgI0b0EjDZDrbjfkhxwQDhjhW4gqcmSlROZ9CreaO5EgBC9/AkA3cLfrc+MGZdn4WicEx64T6T6y1gfQIpVJqzWU2jEEzFljKMGBKpibKUS0iWLpFWwqTGBPSeeIs7To0C8XoVg/AkAtJnd7GhXFnQZ06Lwnd6CBf6/fcG+xHtuNvGcAhd3CPVVH+cKyGOW0Nrp3buHzR0cwbxw7BahC7GWLvwGCRw9JAkB7R6nclXncPB/ZLhIwFAsEvTtanZcG2Eupps4PMRn9USwe5MiguDtUK/5hw1366JX2mEs8msK6NZ5F0Q3+WNRT";
    /** 公钥 */
    private String publicKey;// ="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCYAMw/HxLwR0E8sVBHivet5o84jFhu58aYvqQzHbVompHOsVYYW2oqS2h6OMFSPdgNsK96bRkNf2LAEhB5t5tsBjqU9r629i5/0u5c9UoY0ymk/FOqyoAnaUDR1Li4QUJaSXq9pnGBMxv5xs3MmpTgoFwv+gskoiQliZj8keOWyQIDAQAB";
    /** 商户号 */
    private String mcNo;
    /** 通知地址 */
    private String notifyUrl;

    public XFZFPayServiceImpl() {
    }

    public XFZFPayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("payUrl")) {
                this.payUrl = pmap.get("payUrl");
            }
            if (pmap.containsKey("apiKey")) {
                this.apiKey = pmap.get("apiKey");
            }
            if (pmap.containsKey("publicKey")) {
                this.publicKey = pmap.get("publicKey");
            }
            if (pmap.containsKey("privateKey")) {
                this.privateKey = pmap.get("privateKey");
            }
            if (pmap.containsKey("mcNo")) {
                this.mcNo = pmap.get("mcNo");
            }
            if (pmap.containsKey("notifyUrl")) {
                this.notifyUrl = pmap.get("notifyUrl");
            }
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {

        String outTradeNo = payEntity.getOrderNo();// new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());//
        double amount = payEntity.getAmount();// "8.02";// 订单金额
        String returnurl = payEntity.getRefererUrl();// "http://www.baidu.com";
        String pay_id = payEntity.getPayCode();// "zhonghang";
        String userName = payEntity.getUsername();
        // 2位小数
        DecimalFormat df = new DecimalFormat("0.00");
        String price = String.valueOf(df.format(amount));
        Map<String, String> params = new TreeMap<>();
        params.put("mc_no", this.mcNo);
        params.put("apikey", this.apiKey);
        params.put("outTradeNo", outTradeNo);
        params.put("amount", price);
        params.put("returnurl", returnurl);
        params.put("notifyurl", this.notifyUrl);
        params.put("channel", "gateway");// 支付类型:详见通道列表, 快捷支付（qpay）
        params.put("body", "charge"); // 商品描述, 商品或支付单简要描述
        params.put("bankCode", payEntity.getPayCode()); // 支付类型:详见通道列表
        // params.put("bankCode", "9021");
        params.put("card_type", "D");
        params.put("accessType", "1"); // 1:PC;2:手机 支付类型为gateway

        params.put("timeExpire", "5"); // 订单失效时间
        params.put("sign", generateSign(params, this.privateKey));

        String s = "";
        // Map<String, String> headers = new HashMap<String, String>();
        // headers.put("Content-Type", "application/json;charset=" + "UTF-8");
        JSONObject jsonObject = JSONObject.fromObject(params);

        JSONObject r_json = HttpClient.doPost(payUrl, jsonObject);
        logger.info("支付响应内容：" + r_json.toString());

        return PayUtil.returnWYPayJson("success", "link", r_json.getString("payCode"), "", "");

    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {

        String outTradeNo = payEntity.getOrderNo();// new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());//
                                                   // 商户系统订单号
        double amount = payEntity.getAmount();// "8.02";// 订单金额
        String returnurl = payEntity.getRefererUrl();// "http://www.baidu.com";
        String pay_id = payEntity.getPayCode();// "zhonghang";
        String userName = payEntity.getUsername();
        String payCode = payEntity.getPayCode();
        String mobile = payEntity.getMobile();
        payEntity.getExtendMap();
        // 2位小数
        DecimalFormat df = new DecimalFormat("0.00");
        String price = String.valueOf(df.format(amount));
        Map<String, String> params = new TreeMap<>();
        params.put("mc_no", this.mcNo);
        params.put("apikey", this.apiKey);
        params.put("outTradeNo", outTradeNo);
        params.put("amount", price);
        params.put("returnurl", returnurl);
        params.put("notifyurl", this.notifyUrl);
        params.put("channel", payCode);// 支付类型:详见通道列表, 快捷支付（qpay）
        params.put("body", "charge"); // 商品描述, 商品或支付单简要描述
        // params.put("bankCode", payEntity.getPayCode()); //支付类型:详见通道列表
        // params.put("bankCode", "9021");
        params.put("card_type", "D"); // D:储蓄卡；C:信用卡

        if (StringUtils.isBlank(mobile)) {
            params.put("accessType", "1"); // 1:PC;2:手机 支付类型为gateway
        } else {
            params.put("accessType", "2"); // 1:PC;2:手机 支付类型为gateway
        }

        params.put("timeExpire", "5"); // 订单失效时间
        params.put("sign", generateSign(params, this.privateKey));

        String s = "";
        JSONObject jsonObject = JSONObject.fromObject(params);
        JSONObject r_json = HttpClient.doPost(payUrl, jsonObject);

        logger.info("扫码支付响应：" + r_json.toString());
        return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, outTradeNo,
                r_json.getString("payCode"));
    }

    private String generateSign(Map<String, String> params, String privateKey) {
        StringBuilder buf = new StringBuilder();

        for (String key : params.keySet()) {
            String value = params.get(key);
            if (value == null || value.trim().length() == 0) {
                continue;
            }
            buf.append(key).append("=").append(value).append("&");
        }
        String signatureStr = buf.substring(0, buf.length() - 1);
        logger.info("生成签名串signatureStr = " + signatureStr);
        String signature = null;
        try {
            signature = RSAUtil.getMd5Sign(signatureStr, privateKey);
            logger.info("生成签名串：" + signature);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("签名生成失败");
        }
        return signature;
    }

    /**
     * 回调验签
     * 
     * @param infoMap
     * @return
     * @throws Exception
     */
    @Override
    public String callback(Map<String, String> infoMap) {
        try {
            // 加入
            infoMap.put("apikey", this.apiKey);

            StringBuilder sb = new StringBuilder();
            for (String key : infoMap.keySet()) {
                if ("sign".equalsIgnoreCase(key)) {
                    continue;
                }
                String value = infoMap.get(key);
                /*
                 * if(StringUtils.isEmpty(value)) { continue; }
                 */
                sb.append(key + "=" + value + "&");
            }

            String signatureStr = sb.substring(0, sb.length() - 1);
            logger.info("验签内容signatureStr = " + signatureStr);
            String sign = infoMap.get("sign");

            boolean result = RSAUtil.verifySign(signatureStr, sign, publicKey);

            if (result) {
                logger.info("验签成功");
                return "success";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("验签失败");
        return "fail";
    }

    public static void main(String[] args) throws Exception {
        HashMap<String, String> pmap = new HashMap<String, String>();
        pmap.put("payUrl", "http://www.edu-tengyi.com/dzQpay");
        pmap.put("privateKey",
                "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIFH3aeTZNJdIuBZJwxX2Z+XC+wtr3jGS4IZEharaWPow6NBxC2c9E1pyl1DuSa8kj6Tnj84E+NGbR5VMht5R7NjGn1QMeh5DcZUcRf5p3432wMvpA9VhfC2YDbAt8bTCJnsUD5w1Yfg2Wa7MBXn9L+/JkieFbPR1x4/TxzIUOSNAgMBAAECgYAd2BJCMhNvQJLhiZMrFUimv76/2dgNIHGj6de0JgPhTYWENZVtFjOLf2V43D5sw9Fa4scAPxjQOZzNCMvVbczwN2SMwpdhGtuseAhrXxC2nrJDydRWtHQ7YcbW/d2lwjYETPo5vDJhDaJc2bUlhCB7/4N5LrVjGX3b2lA0BH/BvQJBALskvXRBbwOVum9eLVF7yQlkeEElmdbrLBVDCg+9nx/qCPx0dvXPDT+Xx1NLj1JFFUPLcywpYzQxLW54itD/sfMCQQCw2PbIZKBW8momiy9u5CdOxHkprpcwJGyjpfxnQgI0b0EjDZDrbjfkhxwQDhjhW4gqcmSlROZ9CreaO5EgBC9/AkA3cLfrc+MGZdn4WicEx64T6T6y1gfQIpVJqzWU2jEEzFljKMGBKpibKUS0iWLpFWwqTGBPSeeIs7To0C8XoVg/AkAtJnd7GhXFnQZ06Lwnd6CBf6/fcG+xHtuNvGcAhd3CPVVH+cKyGOW0Nrp3buHzR0cwbxw7BahC7GWLvwGCRw9JAkB7R6nclXncPB/ZLhIwFAsEvTtanZcG2Eupps4PMRn9USwe5MiguDtUK/5hw1366JX2mEs8msK6NZ5F0Q3+WNRT");
        pmap.put("publicKey",
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCYAMw/HxLwR0E8sVBHivet5o84jFhu58aYvqQzHbVompHOsVYYW2oqS2h6OMFSPdgNsK96bRkNf2LAEhB5t5tsBjqU9r629i5/0u5c9UoY0ymk/FOqyoAnaUDR1Li4QUJaSXq9pnGBMxv5xs3MmpTgoFwv+gskoiQliZj8keOWyQIDAQAB");
        pmap.put("mcNo", "2018FA569897");
        pmap.put("apiKey", "NWGzFoElDALUKHBYsiyTJFyuJvluGZwh");
        pmap.put("notifyUrl", "http://182.16.110.186:8080/XPJ/Notify/XfzfNotify.do");
        System.out.println(JSONObject.fromObject(pmap).toString());
    }

}
