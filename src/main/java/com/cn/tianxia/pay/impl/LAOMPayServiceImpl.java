package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.RC4;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: LAOMPayServiceImpl
 * @Description: 老马支付
 * @Author: Zed
 * @Date: 2018-12-21 09:27
 * @Version:1.0.0
 **/

public class LAOMPayServiceImpl implements PayService {

    private final static Logger logger = LoggerFactory.getLogger(LAOMPayServiceImpl.class);

    /** 支付地址 **/
    private String api_url = "http://39.98.81.183:8081/gateway/index/checkpoint.do";

    /** 商户号 **/
    private String account_id = "10604";

    /** KEY **/
    private String KEY = "B96A3751178FA7";

    /** notifyUrl **/
    private String notifyUrl = "http://txw8899.com/AMJ/Notify/LAOMNotify.do";

    public LAOMPayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("api_url")) {
                this.api_url = pmap.get("api_url");
            }
            if (pmap.containsKey("account_id")) {
                this.account_id = pmap.get("account_id");
            }
            if (pmap.containsKey("KEY")) {
                this.KEY = pmap.get("KEY");
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
        JSONObject r_json = Pay(payEntity);
        return r_json;
    }

    /**
     * @Description 二维码支付接口
     * @param payEntity
     * @return
     */
    public JSONObject Pay(PayEntity payEntity) {
        try {
            Map<String,String> paramsMap = sealRequest(payEntity);
            String sign  = generatorSign(paramsMap);
            paramsMap.put("sign",sign);
            String postJson = JSONObject.fromObject(paramsMap).toString();
            logger.info("[LAOM]老马支付请求参数:" + postJson);
            String res = HttpUtils.toPostForm(paramsMap,api_url);

            if (StringUtils.isBlank(res)) {
                logger.error("[LAOM]老马支付扫码支付请求异常,返回结果为空!");
                return PayResponse.error("[LAOM]老马支付扫码支付请求异常,返回结果为空!");
            }
            logger.info("[LAOM]老马支付响应参数字符:" + res);
            JSONObject resposeJson = JSONObject.fromObject(res);

            if (resposeJson.containsKey("code") && "200".equals(resposeJson.getString("code"))) {
                String qrCodeUrl = resposeJson.getJSONObject("data").getString("qrcode");
                String link = resposeJson.getJSONObject("data").getString("qrcode_url2");
                if (StringUtils.isNotBlank(payEntity.getMobile())) {
                    return PayResponse.sm_link(payEntity,link,"下单成功");
                }
                return PayResponse.sm_qrcode(payEntity,qrCodeUrl,"下单成功");
            }
            return PayResponse.error("[LAOM]老马支付扫码支付下单失败:"+ resposeJson.getString("msg"));
        } catch (Exception e) {
            e.printStackTrace();
            return PayResponse.error("[LAOM]老马支付扫码支付下单失败"+e.getMessage());
        }
    }

    /**
     *
     * @param payEntity
     * @return
     */
    private Map<String,String> sealRequest(PayEntity payEntity) throws Exception {
        logger.info("[LAOM]老马支付封装请求参数开始=====================START==================");
        try {
            //创建支付请求参数存储对象
            //订单金额
            String amount = new DecimalFormat("0.00").format(payEntity.getAmount());//交易金额 分为单位
            HashMap<String, String> params = new HashMap<>();
            params.put("account_id", account_id);// 商户ID
            params.put("content_type", "json");// 网页类型
            params.put("thoroughfare", "service_auto");// 支付通道
            params.put("out_trade_no", payEntity.getOrderNo());// 订单信息
            params.put("robin", "2");// 轮训状态 //2开启1关闭
            params.put("amount", amount);// 支付金额
            params.put("callback_url", notifyUrl);// 异步通知url
            params.put("success_url", payEntity.getRefererUrl());// 支付成功后跳转到url
            params.put("error_url", payEntity.getRefererUrl());// 支付失败后跳转到url
            params.put("type", payEntity.getPayCode());// 支付类型 //1为微信，2为支付宝
            params.put("keyId", "");// 设备KEY 轮询无需填写
            return params;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[LAOM]老马支付封装请求参数异常:"+e.getMessage());
            throw new Exception("封装支付请求参数异常!");
        }
    }

    /**
     *
     * @Description 生成支付签名串
     * @param data
     * @return
     * @throws Exception
     */
    public String generatorSign(Map<String,String> data) throws Exception{
        logger.info("[LAOM]老马支付生成支付签名串开始==================START========================");
        try {

            String info = data.get("amount") + data.get("out_trade_no");

            logger.info("[LAOM]老马支付加密串data:" + data);

            String md5Crypt = MD5Utils.md5toUpCase_32Bit(info).toLowerCase();

            logger.info("md5Crypt:" + md5Crypt);

            byte[] rc4_string = RC4.encry_RC4_byte(md5Crypt, KEY);

            System.out.println("rc4_string:" + rc4_string);

            String sign = MD5Utils.md5(rc4_string);

            logger.info("[LAOM]老马支付生成sign:" + sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[LAOM]老马支付生成支付签名串异常:"+e.getMessage());
            throw new Exception("生成支付签名串异常!");
        }
    }

    @Override
    public String callback(Map<String, String> map) {

        String sourceSign = map.remove("sign");
        if (StringUtils.isBlank(sourceSign)) {
            logger.info("[LAOM]老马支付回调验签失败：回调签名为空！");
            return "fail";
        }
        //验证key是否正确
        if (!KEY.equalsIgnoreCase(map.get("account_key"))) {
            logger.error("[LAOM]老马支付签名失败:商户key不匹配 回调key：" + map.get("account_key"));
            return "fail";
        }
        //验证签名是否正确
        String localSign;
        try {
            localSign = generatorSign(map);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[LAOM]老马支付签名失败：" + e.getMessage());
            return "fail";
        }
        logger.info("本地签名:" + localSign + "      服务器签名:" + sourceSign);
        if (sourceSign.equalsIgnoreCase(localSign)) {
            return "success";
        }
        return "fail";
    }


    public static void main(String[] args) {
        PayEntity testPay = new PayEntity();
        testPay.setAmount(100);
        testPay.setOrderNo("mk000000005646578");
        testPay.setPayCode("2");
        testPay.setRefererUrl("http://www.baidu.com");
        //testPay.setMobile("mobile");
        LAOMPayServiceImpl service = new LAOMPayServiceImpl(null);
        service.smPay(testPay);
    }

}
