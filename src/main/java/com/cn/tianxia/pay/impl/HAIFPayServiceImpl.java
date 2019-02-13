package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @ClassName: HAIFPayServiceImpl
 * @Description: 海付支付
 * @Author: Zed
 * @Date: 2018-12-28 16:26
 * @Version:1.0.0
 **/

public class HAIFPayServiceImpl implements PayService {

    private final static Logger logger = LoggerFactory.getLogger(HAIFPayServiceImpl.class);

    /**
     * 支付地址
     **/
    private String api_url = "http://api559858.linjishangwu.com/paygate/entrance.action";

    /**
     * 商户号
     **/
    private String merchantNo = "559858";

    /**
     * md5key
     **/
    private String md5Key = "e10adc3949ba59abbe56e057f20f883e";

    /**
     * notifyUrl
     **/
    private String notifyUrl = "http://txw8899.com/TXY/Notify/HAIFNotify.do";

    public HAIFPayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("api_url")) {
                this.api_url = pmap.get("api_url");
            }
            if (pmap.containsKey("merchantNo")) {
                this.merchantNo = pmap.get("merchantNo");
            }
            if (pmap.containsKey("md5Key")) {
                this.md5Key = pmap.get("md5Key");
            }
            if (pmap.containsKey("notifyUrl")) {
                this.notifyUrl = pmap.get("notifyUrl");
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
            Map<String, String> paramsMap = sealRequest(payEntity);
            String sign = generatorSign(paramsMap);
            paramsMap.put("sign", sign);
            logger.info("[HAIF]海付支付请求参数:" + JSONObject.fromObject(paramsMap).toString());
            String res = HttpUtils.toPostForm(paramsMap, api_url);

            if (StringUtils.isBlank(res)) {
                logger.error("[HAIF]海付支付扫码支付请求异常,返回结果为空!");
                return PayResponse.error("[HAIF]海付支付扫码支付请求异常,返回结果为空!");
            }
            logger.info("[HAIF]海付支付响应参数字符:" + res);
            JSONObject resposeJson = JSONObject.fromObject(res);

            if (resposeJson.containsKey("respCode") && "0000".equals(resposeJson.getString("respCode"))) {

                String qrCodeUrl = resposeJson.getString("payUrl");
                if (StringUtils.isNotBlank(payEntity.getMobile())) {
                    return PayResponse.sm_link(payEntity, qrCodeUrl, "下单成功");
                }
                return PayResponse.sm_qrcode(payEntity, qrCodeUrl, "下单成功");
            }
            return PayResponse.error("[HAIF]海付支付扫码支付下单失败:" + resposeJson.getString("respInfo"));

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HAIF]海付支付扫码支付下单失败:{}", e.getMessage());
            return PayResponse.error("[HAIF]海付支付扫码支付下单失败:" + e.getMessage());
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        String sourceSign = data.remove("sign");
        if (StringUtils.isBlank(sourceSign)) {
            logger.info("[HAIF]海付支付回调验签失败：回调签名为空！");
            return "fail";
        }
        String localSign;
        try {
            localSign = generatorSign(data);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HAIF]海付支付签名异常：" + e.getMessage());
            return "fail";
        }

        logger.info("本地签名:" + localSign + "      服务器签名:" + sourceSign);
        if (sourceSign.equalsIgnoreCase(localSign)) {
            return "success";
        }
        return "fail";
    }

    /**
     * @param payEntity
     * @return
     */
    private Map<String, String> sealRequest(PayEntity payEntity) throws Exception {
        logger.info("[HAIF]海付支付封装请求参数开始=====================START==================");
        try {
            //创建支付请求参数存储对象
            Map<String, String> data = new HashMap<>();
            //订单金额
            String amount = new DecimalFormat("0.00").format(payEntity.getAmount());//交易金额 分为单位

            data.put("userid",merchantNo);
            data.put("orderCode",payEntity.getPayCode());
            data.put("subject","top_up");	//商品的标题
            data.put("amount",amount);	//订单金额
            data.put("pay_number",payEntity.getOrderNo());	//下游订单号
            data.put("notifyUrl",notifyUrl);	//异步通知url
            data.put("pageNotifyUrl",payEntity.getRefererUrl());	//前台通知

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HAIF]海付支付封装请求参数异常:", e.getMessage());
            throw new Exception("封装支付请求参数异常!");
        }
    }

    /**
     * @param data
     * @return
     * @throws Exception
     * @Description 生成支付签名串
     */
    public String generatorSign(Map<String, String> data) throws Exception {
        logger.info("[HAIF]海付支付生成支付签名串开始==================START========================");
        try {
            //不为验签的字段为:orderCode,sign,pay_number,remark,channel_code
            StringBuilder sb = new StringBuilder();
            SortedMap<String, String> sortedMap = new TreeMap<>(data);
            for (String key : sortedMap.keySet()) {
                if (StringUtils.isBlank(sortedMap.get(key)) || "sign".equalsIgnoreCase(key)
                        || "orderCode".equalsIgnoreCase(key) || "pay_number".equalsIgnoreCase(key)
                        || "remark".equalsIgnoreCase(key) || "channel_code".equalsIgnoreCase(key)) {
                    continue;
                }
                sb.append(key).append("=").append(sortedMap.get(key)).append("&");
            }
            sb.append("key=").append(md5Key);
            logger.info("[HAIF]海付支付待签名字符:" + sb.toString());
            String sign = MD5Utils.md5toUpCase_32Bit(sb.toString());
            if (StringUtils.isBlank(sign)) {
                logger.error("[HAIF]海付支付生成签名串为空！");
                return null;
            }
            logger.info("[HAIF]海付支付生成加密签名串:" + sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HAIF]海付支付生成支付签名串异常:" + e.getMessage());
            throw new Exception("生成支付签名串异常!");
        }
    }

    public static void main(String[] args) {
        PayEntity entity = new PayEntity();
        entity.setPayCode("ali_wapPay");
        entity.setAmount(100);
        entity.setRefererUrl("http://localhost:85/JJF");
        entity.setOrderNo("HAIFbl112345678");
        HAIFPayServiceImpl service = new HAIFPayServiceImpl(null);
        service.smPay(entity);
    }

}
