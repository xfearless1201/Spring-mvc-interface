package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;
import com.cn.tianxia.pay.utils.RandomUtils;

import net.sf.json.JSONObject;

/**
 * @ClassName CFBPPayServiceImpl
 * @Description 彩富宝支付
 * @author Hardy
 * @Date 2018年12月29日 下午8:10:51
 * @version 1.0.0
 */
public class CFBPPayServiceImpl implements PayService {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(CFBPPayServiceImpl.class);

    private String appId;// 商户号

    private String payUrl;// 支付地址

    private String notifyUrl;// 回调地址

    private String md5Key;// 秘钥

    // 构造器,初始化参数
    public CFBPPayServiceImpl(Map<String, String> data) {
        if (MapUtils.isNotEntity(data)) {
            if (data.containsKey("appId")) {
                this.appId = data.get("appId");
            }
            if (data.containsKey("payUrl")) {
                this.payUrl = data.get("payUrl");
            }
            if (data.containsKey("notifyUrl")) {
                this.notifyUrl = data.get("notifyUrl");
            }
            if (data.containsKey("md5Key")) {
                this.md5Key = data.get("md5Key");
            }
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[CFBP]彩富宝支付网银支付开始================START=================");
        try {
            // 获取支付请求参数
            Map<String, String> data = sealRequest(payEntity,1);
            String sign = generatorSign(data);
            data.put("sign", sign);
            logger.info("[CFBP]彩富宝支付网银支付请求参数报文:{}", JSONObject.fromObject(data).toString());
            // 发起支付请求
            String response = HttpUtils.toPostJsonStr(JSONObject.fromObject(data), payUrl);
            if (StringUtils.isBlank(response)) {
                logger.info("[CFBP]彩富宝支付网银支付发起HTTP请求无响应结果");
                return PayResponse.error("[CFBP]彩富宝支付网银支付发起HTTP请求无响应结果");
            }
            // 解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if (jsonObject.containsKey("return_code") && jsonObject.getBoolean("return_code")) {
                // 成功
                String code_url = jsonObject.getString("code_url");
                return PayResponse.wy_link(code_url);
            }
            return PayResponse.wy_write("下单失败:" + response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[CFBP]彩富宝支付网银支付异常:{}", e.getMessage());
            return PayResponse.wy_write("[CFBP]彩富宝支付网银支付异常");
        }
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[CFBP]彩富宝支付扫码支付开始================START=================");
        try {
            // 获取支付请求参数
            Map<String, String> data = sealRequest(payEntity,0);
            String sign = generatorSign(data);
            data.put("sign", sign);
            String reqParams = JSONObject.fromObject(data).toString();
            logger.info("[CFBP]彩富宝支付扫码支付请求参数报文:{}",reqParams);
            // 发起支付请求
            String response = HttpUtils.toPostJson(reqParams, payUrl);
            if (StringUtils.isBlank(response)) {
                logger.info("[CFBP]彩富宝支付扫码支付发起HTTP请求无响应结果");
                return PayResponse.error("[CFBP]彩富宝支付扫码支付发起HTTP请求无响应结果");
            }
            // 解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if (jsonObject.containsKey("result_code") && jsonObject.getBoolean("result_code")) {
                // 成功
                String code_url = jsonObject.getString("code_url");
                return PayResponse.sm_link(payEntity, code_url, "下单成功");
            }
            return PayResponse.error("下单失败:" + response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[CFBP]彩富宝支付扫码支付异常:{}", e.getMessage());
            return PayResponse.error("[CFBP]彩富宝支付扫码支付异常");
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[CFBP]彩富宝支付回调验签开始============START====================");
        try {
            
            //获取原签名串
            String sourceSign = data.get("sign");
            //生成签名串
            String sign = generatorSign(data);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[CFBP]彩富宝支付回调验签异常:{}",e.getMessage());
        }
        return "faild";
    }

    /**
     * @Description 组装支付请求参数
     * @param entity
     * @return
     * @throws Exception
     */
    private Map<String, String> sealRequest(PayEntity entity,int type) throws Exception {
        logger.info("[CFBP]彩富宝支付组装支付请求参数开始===================START==============");
        try {
            Map<String, String> data = new HashMap<>();
            String amount = new DecimalFormat("##").format(entity.getAmount() * 100);
            data.put("version", "4.1");// 版本号
            data.put("app_id", appId);// 商户APP_ID
            if(type == 1){
                data.put("pay_type", "8");//网关支付
            }else{
                data.put("pay_type", entity.getPayCode());// 充值渠道
            }
            if ("5".equals(entity.getPayCode())) {
                // 快捷支付
                data.put("quick_user_id", entity.getuId());// 快捷支付用户ID
            }
            data.put("nonce_str", RandomUtils.generateString(8));// 随机字符串
            data.put("sign_type", "MD5");// 签名类型
            data.put("body", "TOP-UP");// 商品描述
            // data.put("detail","");//商品详情
            // data.put("attach","");//附加数据
            data.put("out_trade_no", entity.getOrderNo());// 商户订单号
            data.put("fee_type", "CNY");// 标价币种
            data.put("total_fee", amount);// 标价金额
            data.put("return_url", entity.getRefererUrl());// 充值后网页跳转地址
            data.put("notify_url", notifyUrl);// 通知地址
            data.put("system_time", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));// 交易时间
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[CFBP]彩富宝支付组装支付请求参数异常:{}", e.getMessage());
            throw new Exception("[CFBP]彩富宝支付组装支付请求参数异常");
        }
    }

    /**
     * @Description 签名
     * @param data
     * @param type
     *            1 支付 2 回调
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String, String> data) throws Exception {
        logger.info("[CFBP]彩富宝支付生成签名开始===================START==================");
        try {
            // 签名规则:sign=MD5(商户编号APP_ID=xxx&32位随机字符串=xxx&商户订单号=xxx&签名类型=MD5&商户订单金额=0&版本号=4.0&商户密钥APP_SECRET=123456).toUpperCase()
            // sign=MD5(app_id=1000&nonce_str=f3cfac6450c9424ea8ecd5cca85a5148&
            // out_trade_no=180102195626001&sign_type=MD5&total_fee=0&version=4.0&key=123456).toUpperCase()
            StringBuffer sb = new StringBuffer();
            sb.append("app_id=").append(appId).append("&");
            sb.append("nonce_str=").append(data.get("nonce_str")).append("&");
            sb.append("out_trade_no=").append(data.get("out_trade_no")).append("&");
            sb.append("sign_type=").append(data.get("sign_type")).append("&");
            sb.append("total_fee=").append(data.get("total_fee")).append("&");
            sb.append("version=").append(data.get("version")).append("&");
            String signStr = sb.append("key=").append(md5Key).toString();
            logger.info("[CFBP]彩富宝支付生成待签名串:{}", signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[CFBP]彩富宝支付生成加密签名串:{}", sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[CFBP]彩富宝支付生成签名异常:{}", e.getMessage());
            throw new Exception("[CFBP]彩富宝支付生成签名异常");
        }
    }
}
