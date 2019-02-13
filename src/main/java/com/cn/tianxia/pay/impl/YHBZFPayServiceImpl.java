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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: YHBZFPayServiceImpl
 * @Description: 亿汇宝支付
 * @Author: Zed
 * @Date: 2018-12-18 16:43
 * @Version:1.0.0
 **/

public class YHBZFPayServiceImpl implements PayService {

    private final static Logger logger = LoggerFactory.getLogger(YHBZFPayServiceImpl.class);

    private String memberid = "11060";
    private String key = "73gna5kbbhbn60lpli1fix675jolahby";
    private String payUrl = "http://www.qaqapay.com/Pay_Index.html";
    private String notifyUrl = "http://txw.tx8899.com/YLH/Notify/YHBZFNotify.do";

    public YHBZFPayServiceImpl(Map<String,String> map) {
        if(map != null && !map.isEmpty()){
            if(map.containsKey("memberid")){
                this.memberid = map.get("memberid");
            }
            if(map.containsKey("notifyUrl")){
                this.notifyUrl = map.get("notifyUrl");
            }
            if(map.containsKey("key")){
                this.key = map.get("key");
            }
            if(map.containsKey("payUrl")){
                this.payUrl = map.get("payUrl");
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
            Map<String,String> param = sealRequest(payEntity);

            String sign = generatorSign(param);

            param.put("pay_md5sign",sign);
            logger.info("[YHBZF]亿汇宝支付扫码请求参数:{}",JSONObject.fromObject(param).toString());
            String response = HttpUtils.post(param,payUrl);

            if (StringUtils.isBlank(response)) {
                logger.error("[YHBZF]亿汇宝支付下单失败：请求返回结果为空");
                PayResponse.error("[YHBZF]亿汇宝支付下单失败：请求返回结果为空");
            }

            JSONObject resposeJson = JSONObject.fromObject(response);

            if (resposeJson.containsKey("status") && "success".equals(resposeJson.getString("status"))) {
                String qrCodeUrl = resposeJson.getJSONObject("data").getString("qr_code_url");
                String payUrl = resposeJson.getJSONObject("data").getString("pay_url");
                if (StringUtils.isNotBlank(payEntity.getMobile())) {
                    return PayResponse.sm_link(payEntity,payUrl,"下单成功");
                }
                return PayResponse.sm_qrcode(payEntity,qrCodeUrl,"下单成功");
            }
            return PayResponse.error("[TTZF]踢踢扫码支付下单失败:"+ resposeJson.getString("msg"));

        } catch (Exception e) {
           e.printStackTrace();
           return PayResponse.error("[TTZF]踢踢扫码支付下单失败"+e.getMessage());
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        try {
            String serverSign = data.remove("sign");

            String localSign = generatorSign(data);

            logger.info("本地签名:" + localSign + "      服务器签名:" + serverSign);
            if (serverSign.equalsIgnoreCase(localSign)) {
                return "success";
            }
            return "fail";
        } catch (Exception e) {
            logger.error("[YHBZF]发家支付回调验签异常:" + e.getMessage());
            return "fail";
        }
    }

    /**
     *
     * @Description 封装支付请求参数
     * @param entity
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity) throws Exception{
        logger.info("[YHBZF]亿汇宝支付封装请求参数开始=====================START==================");
        try {
            //创建支付请求参数存储对象
            Map<String,String> data = new HashMap<>();
            //订单金额
            String amount = new DecimalFormat("0.00").format(entity.getAmount());//交易金额 分为单位

            Date date = new Date();

            data.put("pay_memberid",memberid);//商户号
            data.put("pay_orderid",entity.getOrderNo());//订单号
            data.put("pay_amount",amount);// 交易金额
            data.put("pay_applydate",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
            data.put("pay_bankcode",entity.getPayCode());// 交易金额
            data.put("pay_notifyurl",notifyUrl);//后台回调通知地址
            data.put("pay_callbackurl",entity.getRefererUrl());//页面通知地址
            data.put("pay_returntype","2");//缺省值为1;    1页面直接跳转  2 json格式返回

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YHBZF]亿汇宝支付封装请求参数异常:"+e.getMessage());
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
        logger.info("[YHBZF]亿汇宝支付生成支付签名串开始==================START========================");
        try {
            //签名规则:
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("pay_amount=").append(data.get("pay_amount"));
            strBuilder.append("&pay_applydate=").append(data.get("pay_applydate"));
            strBuilder.append("&pay_bankcode="+data.get("pay_bankcode"));
            strBuilder.append("&pay_callbackurl=").append(data.get("pay_callbackurl"));
            strBuilder.append("&pay_memberid="+data.get("pay_memberid"));
            strBuilder.append("&pay_notifyurl="+data.get("pay_notifyurl"));
            strBuilder.append("&pay_orderid="+data.get("pay_orderid"));
            strBuilder.append("&key="+key);
            logger.info("[YHBZF]亿汇宝支付生成待签名串:"+strBuilder.toString());
            String md5Value = MD5Utils.md5toUpCase_32Bit(strBuilder.toString());
            logger.info("[YHBZF]亿汇宝支付生成加密签名串:"+md5Value);
            return md5Value;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YHBZF]亿汇宝支付生成支付签名串异常:"+e.getMessage());
            throw new Exception("生成支付签名串异常!");
        }
    }

    public static void main(String[] args) {
        PayEntity testPay = new PayEntity();
        testPay.setAmount(100);
        testPay.setOrderNo("mk00000000123456");
        testPay.setPayCode("912");
        testPay.setRefererUrl("http://localhost:8080/xxx");
        //testPay.setMobile("mobile");
        YHBZFPayServiceImpl service = new YHBZFPayServiceImpl(null);
        service.smPay(testPay);
    }




}
