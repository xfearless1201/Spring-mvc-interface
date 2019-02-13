package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.*;

public class MTPayServiceImpl implements PayService {

    private final static Logger logger = LoggerFactory.getLogger(MTPayServiceImpl.class);

    private String customerid;    //商户id
    private String version;    //商户uid
    private String key;        //加密密钥
    private String payUrl;    //支付地址
    private String queryUrl;      //订单查询地址
    private String notifyUrl;     //异步通知地址

    public MTPayServiceImpl(Map<String, String> data) {
        if (data != null && !data.isEmpty()) {
            if (data.containsKey("customerid")) {
                this.customerid = data.get("customerid");
            }
            if (data.containsKey("version")) {
                this.version = data.get("version");
            }
            if (data.containsKey("key")) {
                this.key = data.get("key");
            }
            if (data.containsKey("payUrl")) {
                this.payUrl = data.get("payUrl");
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
        try {
            Map<String, String> requestMap = sealRequest(payEntity, 1);
            String sign = generatorSign(requestMap);
            requestMap.put("sign", sign);

//            JSONObject reqJson = JSONObject.fromObject(requestMap);
//            logger.info("[MTPay]摩通支付扫码支付请求参数:" + reqJson.toString());
//            //发起HTTP-POST请求
//            String response = HttpUtils.toPostJson(reqJson.toString(), payUrl);
//            if (StringUtils.isBlank(response)) {
//                logger.error("[MTPay]摩通支付支付失败,请求无响应结果!");
//                return PayUtil.returnPayJson("error", "2", "下单失败,发起HTTP请求无响应结果!", "", 0, "", "展示请求响应结果:" + response);
//            }
//
//            //解析响应结果
//            JSONObject jsonObject = JSONObject.fromObject(response);
//            if (jsonObject.containsKey("ret_code") && jsonObject.getString("ret_code").equalsIgnoreCase("0000")) {
//                //ret_code  0000成功
//                //下单成功
//                String qrCodeURL = jsonObject.getString("dimension_url"); //二维码链接
//                return PayUtil.returnPayJson("success", "2", "下单成功!", payEntity.getUsername(),
//                        payEntity.getAmount(), payEntity.getOrderNo(), qrCodeURL);
//            }
//            //下单失败
//            String respMsg = jsonObject.getString("ret_msg");
//            return PayUtil.returnPayJson("error", "2", "下单失败:" + respMsg, payEntity.getUsername(), payEntity.getAmount(), payEntity.getOrderNo(), response);
            String formStr = HttpUtils.generatorForm(requestMap, payUrl);
            System.out.println("[MT]摩通支付支付表单:" + formStr);
            logger.info("[MT]摩通支付生成请求表单:{}", formStr);

            return PayUtil.returnWYPayJson("success", "form", formStr, payEntity.getPayUrl(), "");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[MT]摩通支付网银支付异常:" + e.getMessage());
            return PayUtil.returnWYPayJson("error", "[MT]摩通支付网银支付异常!", "", "", "");
        }
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[MTPay]摩通支付支付扫码支付开始---------------START---------------");
        try {
            //获取支付请求参数
            Map<String, String> data = sealRequest(payEntity, 2);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign", sign);//验签字段 是   MD5加密

            String formStr = HttpUtils.generatorForm(data, payUrl);
            System.out.println("[MTPay]摩通支付扫码支付表单:" + formStr);
            logger.info("[MTPay]摩通支付扫码支付生成请求表单:{}", formStr);
            return PayResponse.sm_form(payEntity, formStr, "下单成功!");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[MTPay]摩通支付扫码支付异常:" + e.getMessage());
            return PayUtil.returnPayJson("error", "2", "[MTPay]摩通支付扫码支付异常!", "", 0, "", e.getMessage());
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[MT]摩通支付回调验签开始===================START===================");
        try {
            if (data == null || data.isEmpty()) {
                logger.error("[MT]摩通支付回调验签参数为空！");
                return "fail";
            } else if (!data.containsKey("sign")) {
                logger.error("[MT]摩通支付验签原签名为空");
                return "fail";
            }
            String sourceSign = data.get("sign");

            logger.info("[MT]摩通支付验签原签名串:{}", sourceSign);

            String sign = checkCallbackSign(data);
            logger.info("[MT]摩通支付验签生成签名串:{}", sign);
            if (sourceSign.equalsIgnoreCase(sign))
                return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[MT]摩通支付回调验签异常:{}", e.getMessage());
        }
        return "fail";
    }

    private String checkCallbackSign(Map<String, String> data) throws Exception {
        try {
            //签名规则:
//            {value}要替换成接收到的只,{apikey}要替换成平台分配的接入密钥
//                    通知参数 customerid={value}&status={value}&sdpayno={value}&sdorderno={value}&total_fee={value}&paytype={value}&{apikey}
//            使用md5加密上面字符串生成32位小写密文

            //生成待签名串
            StringBuffer sb = new StringBuffer();

            sb.append("customerid").append("=").append(data.get("customerid")).append("&");
            sb.append("status").append("=").append(data.get("status")).append("&");
            sb.append("sdorderno").append("=").append(data.get("sdorderno")).append("&");
            sb.append("total_fee").append("=").append(data.get("total_fee")).append("&");
            sb.append("paytype").append("=").append(data.get("paytype")).append("&");
            sb.append(key);

            logger.info("[MT]摩通支付支付生成待签名串:" + sb.toString());
            String sign = MD5Utils.md5toUpCase_32Bit(sb.toString()).toLowerCase();
            logger.info("[MT]摩通支付支付生成加密签名串:" + sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[MT]支付生成支付签名串异常:" + e.getMessage());
            throw new Exception("生成支付签名串异常!");
        }
    }

    /**
     * @param entity
     * @return
     * @throws Exception
     * @Description 封装支付请求参数
     */
    private Map<String, String> sealRequest(PayEntity entity, int payType) throws Exception {
        logger.info("[MT]摩通支付支付封装请求参数开始=====================START==================");
        try {
            //创建支付请求参数存储对象
            Map<String, String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(entity.getAmount());//交易金额
            data.put("version", version);    //版本号
            data.put("customerid", customerid);//商户号
//          paytype   支付宝扫码	qzfzfb 支付宝H5/WAP	qzfzfbh5// 微信扫码	jhbwxsm// 在线网银	bank//QQ钱包扫码	qqrcode// QQ钱包H5	qqwallet//快捷支付	quickbank//快捷支付H5	quickwap
            data.put("get_code", "0");    //如果只想获取被扫二维码，请设置get_code=1
            if (payType == 1) {
                data.put("paytype", "bank");
                data.put("bankcode", entity.getPayCode());
            } else {
                data.put("paytype", entity.getPayCode());
            }
            data.put("sdorderno", entity.getOrderNo());//订单号
            data.put("total_fee", amount);//交易1金额 是   元为单位
            data.put("notifyurl", notifyUrl);//后台回调通知地址
            data.put("returnurl", entity.getPayUrl());//页面通知地址
            data.put("remark", "remark");//备注
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[MT]摩通支付支付封装请求参数异常:" + e.getMessage());
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
        logger.info("[MT]摩通支付支付生成支付签名串开始==================START========================");
        try {
            //签名规则:
//            {value}要替换成接收到的只,{apikey}要替换成平台分配的接入密钥
//                    version={value}&customerid={value}&total_fee={value}&sdorderno={value}&notifyurl={value}&returnurl={value}&{apikey}
//            使用md5加密上面字符串生成32位小写密文

            //生成待签名串
            StringBuffer sb = new StringBuffer();

            sb.append("version").append("=").append(data.get("version")).append("&");
            sb.append("customerid").append("=").append(data.get("customerid")).append("&");
            sb.append("total_fee").append("=").append(data.get("total_fee")).append("&");
            sb.append("sdorderno").append("=").append(data.get("sdorderno")).append("&");
            sb.append("notifyurl").append("=").append(data.get("notifyurl")).append("&");
            sb.append("returnurl").append("=").append(data.get("returnurl")).append("&");
            sb.append(key);

            logger.info("[MT]摩通支付支付生成待签名串:" + sb.toString());
            String sign = MD5Utils.md5toUpCase_32Bit(sb.toString()).toLowerCase();
            logger.info("[MT]摩通支付支付生成加密签名串:" + sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[MT]支付生成支付签名串异常:" + e.getMessage());
            throw new Exception("生成支付签名串异常!");
        }
    }
}
