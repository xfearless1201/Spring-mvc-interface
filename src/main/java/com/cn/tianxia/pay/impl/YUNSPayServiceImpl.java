package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.*;

public class YUNSPayServiceImpl implements PayService {

    private final static Logger logger = LoggerFactory.getLogger(YUNSPayServiceImpl.class);

    private String uid;    //商户uid
    private String token;
    private String formPayUrl;    //form表单跳转方式支付地址
    private String jsonPayUrl;    //json请求方式支付地址
    private String queryUrl;      //订单查询地址
    private String notifyUrl;     //异步通知地址

    public YUNSPayServiceImpl(Map<String, String> data) {
        if (data != null && !data.isEmpty()) {
            if (data.containsKey("uid")) {
                this.uid = data.get("uid");
            }
            if (data.containsKey("token")) {
                this.token = data.get("token");
            }
            if (data.containsKey("formPayUrl")) {
                this.formPayUrl = data.get("formPayUrl");
            }
            if (data.containsKey("jsonPayUrl")) {
                this.jsonPayUrl = data.get("jsonPayUrl");
            }
            if (data.containsKey("notifyUrl")) {
                this.notifyUrl = data.get("notifyUrl");
            }
            if (data.containsKey("queryUrl")) {
                this.queryUrl = data.get("queryUrl");
            }
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        return null;
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        try {
            LinkedHashMap<String, String> params = sealRequest(payEntity);
            String key = generatorSign(params);
            params.put("key", key);
            JSONObject requestJson = JSONObject.fromObject(params);
            logger.info("[YUNS]云商扫码支付请求参数:" + requestJson.toString());
            //发起HTTP-POST请求
            String response = HttpUtils.toPostJson(requestJson.toString(), jsonPayUrl);
            if (StringUtils.isBlank(response)) {
                logger.error("[YSPay]云商支付失败,请求无响应结果!");
                return PayUtil.returnPayJson("error", "2", "下单失败,发起HTTP请求无响应结果!", "", 0, "", "展示请求响应结果:" + response);
            }

            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if (jsonObject.containsKey("code") && jsonObject.getString("code").equalsIgnoreCase("1")) {
                //下单成功
                String qrCodeURL = jsonObject.getString("qrcode"); //二维码链接
                return PayUtil.returnPayJson("success", "2", "下单成功!", payEntity.getUsername(),
                        payEntity.getAmount(), payEntity.getOrderNo(), qrCodeURL);
            }
            //下单失败
            String respMsg = jsonObject.getString("msg");
            return PayUtil.returnPayJson("error", "2", "下单失败:" + respMsg, payEntity.getUsername(), payEntity.getAmount(), payEntity.getOrderNo(), response);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YSPay]云商支付扫码支付异常:" + e.getMessage());
            return PayUtil.returnPayJson("error", "2", "[YSPay]云商扫码支付异常!", "", 0, "", e.getMessage());
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[YUNS]云商支付回调验签开始===================START===================");
        try {
            if (data == null || data.isEmpty()) {
                logger.error("[YUNS]云商支付回调验签参数为空！");
                return "fail";
            } else if (!data.containsKey("key")) {
                logger.error("[YUNS]云商支付验签原签名为空");
                return "fail";
            }
            String sourceSign = data.get("key");

            logger.info("[YUNS]云商支付验签原签名串:{}", sourceSign);

            String sign = checkCallbackSign(data);
            logger.info("[YUNS]云商支付验签生成签名串:{}", sign);
            if (sourceSign.equalsIgnoreCase(sign))
                return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YUNS]云商支付回调验签异常:{}", e.getMessage());
        }
        return "fail";
    }

    private String checkCallbackSign(Map<String, String> data) throws Exception {
        //orderid + orderuid + platform_trade_no + price + realprice + token
        try {
            StringBuffer signStr = new StringBuffer();
            signStr.append(data.get("orderid"));
            signStr.append(data.get("orderuid"));
            signStr.append(data.get("platform_trade_no"));
            signStr.append(data.get("price"));
            signStr.append(data.get("realprice"));
            signStr.append(token);
            return MD5Utils.md5toUpCase_32Bit(signStr.toString()).toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YUNS]云商支付回调验签签名失败");
            throw new Exception(e.getMessage());
        }
    }

    private LinkedHashMap<String, String> sealRequest(PayEntity payEntity) {
        String amount = new DecimalFormat("0.00").format(payEntity.getAmount());
        LinkedHashMap<String, String> requestMap = new LinkedHashMap<>();
        //goodsname + istype + notify_url + orderid + orderuid + price + return_url + token + uid 组装签名参数顺序
        requestMap.put("goodsname", "top_up");
        requestMap.put("istype", payEntity.getPayCode());
        requestMap.put("notify_url", notifyUrl);
        requestMap.put("orderid", payEntity.getOrderNo());
        requestMap.put("price", amount);
        requestMap.put("return_url", payEntity.getRefererUrl());
        requestMap.put("token", token);
        requestMap.put("uid", uid);
        return requestMap;
    }

    /**
     * @param data
     * @return
     * @throws Exception
     * @Description 生成支付签名串
     */
    public String generatorSign(LinkedHashMap<String, String> data) throws Exception {
        logger.info("[YUNS]云商支付生成支付签名串开始==================START========================");
        try {

            //生成待签名串
            StringBuffer sb = new StringBuffer();
            for (String key : data.keySet()) {
                String val = data.get(key);
                if (StringUtils.isBlank(val) || "null".equals(val) || key.equalsIgnoreCase("key") || key.equalsIgnoreCase("signature"))
                    continue;
                sb.append(val);
            }

            logger.info("[YUNS]云商支付生成待签名串:" + sb.toString());
            String sign = MD5Utils.md5toUpCase_32Bit(sb.toString()).toLowerCase();
            logger.info("[YUNS]云商支付生成加密签名串:" + sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YUNS]支付生成支付签名串异常:" + e.getMessage());
            throw new Exception("生成支付签名串异常!");
        }
    }
}
