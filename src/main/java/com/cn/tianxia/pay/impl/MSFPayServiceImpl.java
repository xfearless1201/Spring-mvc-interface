package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName MSFPayServiceImpl
 * @Description 马上付支付
 * @author Hardy
 * @Date 2018年11月19日 上午10:14:26
 * @version 1.0.0
 */
public class MSFPayServiceImpl implements PayService{
    
    //日志 
    private static final Logger logger = LoggerFactory.getLogger(MSFPayServiceImpl.class);
    
    private String appid;//商户号
    
    private String payUrl;//支付请求地址
    
    private String notifyUrl;//回调请求地址
    
    private String secret;//秘钥
    
    //构造器,初始化参数
    public MSFPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("appid")){
                this.appid = data.get("appid");
            }
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
            }
            if(data.containsKey("secret")){
                this.secret = data.get("secret");
            }
        }
    }
    
    
    /**
     * 网银支付
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[MSF]马上付支付扫码支付开始=================START===================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity);
            //生成签名串
            String sign = generator(data);
            data.put("sign", sign);
            logger.info("[MSF]马上付支付扫码支付请求报文:{}",JSONObject.fromObject(data).toString());
            //发起支付请求,表单格式
            String formStr = HttpUtils.generatorForm(data, payUrl);
            logger.info("[MSF]马上付支付扫码请求form表单:{}",formStr);
            return PayResponse.sm_form(payEntity, formStr, "下单成功!");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[MSF]马上付支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[MSF]马上付支付扫码支付异常");
        }
    }

    /**
     * 回调
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[MSF]马上付回调通知验签开始====================START===============");
        try {
            //获取回调原签名串
            String sourceSign = data.get("sign");
            logger.info("[MSF]马上付回调通知原签名串:{}",sourceSign);
            //生成回调签名串
            String sign = generator(data);
            logger.info("[MSF]马上付回调通知生成加密签名串:{}",sign);
            
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[MSF]马上付回调通知验签异常:{}",e.getMessage());
        }
        return "faild";
    }
    
    /**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity) throws Exception{
        logger.info("[MSF]马上付支付封装支付请求参数开始=======================START==================");
        try {
            //创建支付请求参数存储对象
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            data.put("bty_appid",appid);//申请开户后新建自动生成的一个APPID号码    1005
            data.put("bty_total_fee",amount);//支付金额100.00
            data.put("bty_type",entity.getPayCode());//支付类型 1支付宝，2QQ钱包，3微信   1
            data.put("bty_out_trade_no",entity.getOrderNo());//商户订单号baiteyun_201633225454
//            data.put("bty_webname","");//网站名称立马付
            data.put("bty_subject","TOP-UP");//商品名立马付产品
            data.put("bty_notify_url",notifyUrl);//异步通知接口  http://www.limafukuan.com/test/notify_url.php
            data.put("bty_return_url",entity.getRefererUrl());//同步跳转接口  http://www.limafukuan.com/test/return_url.php
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[MSF]马上付支付封装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[MSF]马上付支付封装支付请求参数异常!");
        }
    }
    
    private String generator(Map<String,String> data) throws Exception{
        logger.info("[MSF]马上付支付生成签名串开始======================START=========================");
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(appid).append(secret).append(data.get("bty_out_trade_no")).append(data.get("bty_total_fee"));
            String signStr = sb.toString();
            logger.info("[MSF]马上付支付生成待签名串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[MSF]马上付支付生成加密签名串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[MSF]马上付支付生成签名串异常:{}",e.getMessage());
            throw new Exception("[MSF]马上付支付生成签名串异常");
        }
    }

}
