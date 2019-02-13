package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.PayConstant;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class YJFPayServiceImpl implements PayService {

    private final static Logger logger = LoggerFactory.getLogger(YJFPayServiceImpl.class);

    private String oid_partner;//商户号
    private String key;//结算类型
    private String bankPayUrl;//网银支付地址
    private String scanPayUrl;//扫码支付地址
    private String queryUrl;//订单查询地址
    private String notifyUrl;//异步通知地址

    public YJFPayServiceImpl(Map<String, String> data) {
        if (data != null && !data.isEmpty()) {
            if (data.containsKey("oid_partner")) {
                this.oid_partner = data.get("oid_partner");
            }
            if (data.containsKey("key")) {
                this.key = data.get("key");
            }
            if (data.containsKey("bankPayUrl")) {
                this.bankPayUrl = data.get("bankPayUrl");
            }
            if (data.containsKey("scanPayUrl")) {
                this.scanPayUrl = data.get("scanPayUrl");
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
        logger.info("[YJF]易极付支付网银支付开始===================START=================");
        try {
            //获取支付请求参数
            Map<String, String> data = sealRequest(payEntity,1);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign", sign);
            String formStr = HttpUtils.generatorForm(data, bankPayUrl);
            System.out.println("[YJF]易极付支付表单:" + formStr);
            logger.info("[YJF]易极付支付生成请求表单:{}", formStr);

            return PayUtil.returnWYPayJson("success", "form", formStr, payEntity.getPayUrl(), "");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YJF]易极付支付网银支付异常:" + e.getMessage());
            return PayUtil.returnWYPayJson("error", "[YJF]易极付网银支付异常!", "", "", "");
        }
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[YJFPay]易极付支付扫码支付开始---------------START---------------");
        try {
            //获取支付请求参数
            Map<String, String> data = sealRequest(payEntity,2);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign", sign);//验签字段 是   MD5加密

            if ((StringUtils.isNotBlank(payEntity.getMobile()) && PayConstant.CHANEL_ALI.equals(payEntity.getPayType()))         //支付宝手机端扫码和快捷走页面直接跳转
                    || PayConstant.CHANEL_KJ.equalsIgnoreCase(payEntity.getPayType())) {
                String formStr = HttpUtils.generatorForm(data, bankPayUrl);
                System.out.println("[YJF]易极付支付表单:" + formStr);
                logger.info("[YJF]易极付支付生成请求表单:{}", formStr);
                return PayResponse.sm_form(payEntity, formStr, "下单成功!");
            }

            JSONObject reqJson = JSONObject.fromObject(data);
            logger.info("[YJFPay]易极付扫码支付请求参数:" + reqJson.toString());
            //发起HTTP-POST请求
            String response = HttpUtils.toPostJson(reqJson.toString(), scanPayUrl);
            if (StringUtils.isBlank(response)) {
                logger.error("[YJFPay]易极付支付失败,请求无响应结果!");
                return PayUtil.returnPayJson("error", "2", "下单失败,发起HTTP请求无响应结果!", "", 0, "", "展示请求响应结果:" + response);
            }

            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if (jsonObject.containsKey("ret_code") && jsonObject.getString("ret_code").equalsIgnoreCase("0000")) {
                //ret_code  0000成功
                //下单成功
                String qrCodeURL = jsonObject.getString("dimension_url"); //二维码链接
                return PayUtil.returnPayJson("success", "2", "下单成功!", payEntity.getUsername(),
                        payEntity.getAmount(), payEntity.getOrderNo(), qrCodeURL);
            }
            //下单失败
            String respMsg = jsonObject.getString("ret_msg");
            return PayUtil.returnPayJson("error", "2", "下单失败:" + respMsg, payEntity.getUsername(), payEntity.getAmount(), payEntity.getOrderNo(), response);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YJFPay]易极付支付扫码支付异常:" + e.getMessage());
            return PayUtil.returnPayJson("error", "2", "[YJFPay]易极付扫码支付异常!", "", 0, "", e.getMessage());
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[YJF]易极付支付回调验签开始===================START===================");
        try {
            //获取验签原签名串
            String sourceSign = data.remove("sign");

            logger.info("[YJF]易极付支付验签原签名串:{}", sourceSign);

            //生成验签签名
            String sign = generatorSign(data);
            logger.info("[YJF]易极付支付验签生成签名串:{}", sign);
            if (sourceSign.equalsIgnoreCase(sign))
                return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YJF]易极付支付回调验签异常:{}", e.getMessage());
        }
        return "fail";
    }

    public String query(Map<String, String> data) {
        logger.info("[YJF]易极付支付查询开始===================START=====================");
        try {
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("oid_partner", oid_partner);
            queryMap.put("sign_type", "MD5");
            queryMap.put("no_order", data.get("order_no"));
            //生成签名串
            String sign = generatorSign(data);
            queryMap.put("sign", sign);
            String response = HttpUtils.post(data, queryUrl);
            if (StringUtils.isBlank(response)) {
                logger.error("[YJF]易极付支付查询失败,请求无响应结果!");
                return "failed";
            }

            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if (jsonObject.containsKey("ret_code") && jsonObject.getString("ret_code").equals("1")) {
                return "success";
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YJF]易极付支付查询异常:{}", e.getMessage());
        }
        return null;
    }

    /**
     * @param entity
     * @return
     * @throws Exception
     * @Description 封装支付请求参数
     */
    private Map<String, String> sealRequest(PayEntity entity,int payType) throws Exception {
        logger.info("[YJF]易极付支付封装请求参数开始=====================START==================");
        try {
            //创建支付请求参数存储对象
            Map<String, String> data = new HashMap<>();
            //订单时间
            Date orderTime = new Date();
            //订单金额
            String amount = new DecimalFormat("0.00").format(entity.getAmount());//交易金额
            data.put("oid_partner", oid_partner);//商户号
            data.put("user_id", entity.getuId());//用户id
            data.put("sign_type", "MD5");
            if (payType == 1) {
                data.put("pay_type", "11");   //网银借记卡付款
                data.put("bank_code", entity.getPayCode());
                //data.put("bank_code", "1020000");
            } else {
                data.put("pay_type",entity.getPayCode());
            }

            data.put("no_order", entity.getOrderNo());//订单号
            data.put("time_order", new SimpleDateFormat("yyyyMMddHHmmss").format(orderTime));
            data.put("money_order", amount);//交易1金额 是   元为单位
            data.put("name_goods", "TOP-UP");//商品名称
            data.put("notify_url", notifyUrl);//后台回调通知地址
            data.put("return_url", entity.getPayUrl());//页面通知地址
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YJF]易极付支付封装请求参数异常:" + e.getMessage());
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
        logger.info("[YJF]易极付支付生成支付签名串开始==================START========================");
        try {
            //签名规则:
            //把除签名字段和集合字段以外的所有字段（不包括值为null的）内容按照报文字段字典顺序，
            //依次按照“字段名=字段值”的方式用“&”符号连接，最后加上机构工作密钥，使用MD5算法计算数字签名，填入签名字段。接受方应按响应步骤验证签名。
            Map<String, String> treemap = new TreeMap<>();
            treemap.putAll(data);

            //生成待签名串
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = treemap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String val = treemap.get(key);

                if (StringUtils.isBlank(val) || key.equalsIgnoreCase("sign") || key.equalsIgnoreCase("signature"))
                    continue;
                if (iterator.hasNext()) {
                    sb.append(key).append("=").append(val).append("&");
                } else {
                    sb.append(key).append("=").append(val);
                }
            }

            //生成待签名串
            String signStr = sb.toString() + key;
            logger.info("[YJF]易极付支付生成待签名串:" + signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[YJF]易极付支付生成加密签名串:" + sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YJF]支付生成支付签名串异常:" + e.getMessage());
            throw new Exception("生成支付签名串异常!");
        }
    }
}
