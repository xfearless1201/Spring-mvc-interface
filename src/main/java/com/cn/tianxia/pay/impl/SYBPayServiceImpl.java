package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName SYBPayServiceImpl
 * @Description 收盈宝支付
 * @author Hardy
 * @Date 2018年10月26日 上午9:06:49
 * @version 1.0.0
 */
public class SYBPayServiceImpl implements PayService{
    
    private static final Logger logger = LoggerFactory.getLogger(SYBPayServiceImpl.class);
    
    private String fxid;//商户号
    
    private String payUrl;//支付地址
    
    private String notifyUrl;//回调地址
    
    private String secret;//秘钥

    //构造器，初始化数据
    public SYBPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("fxid")){
                this.fxid = data.get("fxid");
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
        logger.info("[SYB]收盈宝支付网银支付开始=======================START=====================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity,1);
            //生成签名串
            String sign = generatorSign(data,1);
            data.put("fxsign", sign);
            //发起支付请求
            logger.info("[SYB]收盈宝支付网银请求报文:{},[SYB]收盈宝支付网银支付地址:{}",JSONObject.fromObject(data).toString(),payUrl);
            String response = HttpUtils.toPostForm(data, payUrl);
            if(StringUtils.isBlank(response)){
                logger.info("[SYB]收盈宝支付发起HTTP请求无响应结果:[请求报文:"+JSONObject.fromObject(data).toString()+"],[请求地址:"+payUrl+"]");
                return PayUtil.returnWYPayJson("error","form","[SYB]收盈宝支付发起HTTP请求无响应结果","","");
            }
            logger.info("[SYB]收盈宝支付发起HTTP请求响应结果:{}",response);
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            //状态【1代表正常】【0代表错误】
            if(jsonObject.containsKey("status") && jsonObject.getString("status").equals("1")){
                String payurl = jsonObject.getString("payurl").replace("\\", "");
                
                return PayUtil.returnWYPayJson("success","link",payurl,"","");
            }
            return PayUtil.returnWYPayJson("error","link",response,"","");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[SYB]收盈宝支付网银支付异常:"+e.getMessage());
            return PayUtil.returnWYPayJson("error","form","[SYB]收盈宝支付网银支付异常:"+e.getMessage(),payEntity.getPayUrl(),"pay");
        }
    }

    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[SYB]收盈宝支付扫码支付开始=======================START=====================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity,0);
            //生成签名串
            String sign = generatorSign(data,1);
            data.put("fxsign", sign);
            //发起支付请求
            String response = HttpUtils.toPostForm(data, payUrl);
            if(StringUtils.isBlank(response)){
                logger.info("[SYB]收盈宝支付发起HTTP请求无响应结果:[请求报文:"+JSONObject.fromObject(data).toString()+"],[请求地址:"+payUrl+"]");
                return PayUtil.returnPayJson("error","2","[SYB]收盈宝支付发起HTTP请求无响应结果","",0,"",response);
            }
            logger.info("[SYB]收盈宝支付发起HTTP请求响应结果:{}",response);
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            //状态【1代表正常】【0代表错误】
            if(jsonObject.containsKey("status") && jsonObject.getString("status").equals("1")){
                String payurl = jsonObject.getString("payurl").replace("\\", "");
                String type = "4";//默认为移动端
                if(StringUtils.isBlank(payEntity.getMobile()) && payEntity.getPayCode().equalsIgnoreCase("wxwap")){
                    type = "2";
                }
                return PayUtil.returnPayJson("success",type,"下单成功!",payEntity.getUsername(),payEntity.getAmount(),payEntity.getOrderNo(),payurl);
            }
            return PayUtil.returnPayJson("error","2","下单失败","",0,"",response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[SYB]收盈宝支付扫码支付异常:"+e.getMessage());
            return PayUtil.returnPayJson("error","2","[SYB]收盈宝扫码支付异常:"+e.getMessage(),"",0,"","[SYB]收盈宝扫码支付异常:"+e.getMessage());
            
        }
        
    }

    /**
     * 回调
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[SYB]收盈宝支付回调验签开始====================START=======================");
        try {
            //获取回调验签原签名串
            String sourceSign = data.get("fxsign");
            //生成回调验签签名串
            String sign = generatorSign(data, 0);
            logger.info("[SYB]收盈宝支付回调原签名串:{},[SYB]收盈宝支付回调生产验签签名串:{}",sourceSign,sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[SYB]收盈宝支付回调验签异常:{}",e.getMessage());
        }
        return "faild";
    }

    /**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @param type 1 网银 0 扫码
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,Integer type) throws Exception{
        logger.info("[SYB]收盈宝支付组装支付请求参数开始====================START======================");
        try {
            //创建支付请求参数存储对象
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            data.put("fxid",fxid);//商户号
            data.put("fxddh",entity.getOrderNo());//商户订单号
            data.put("fxdesc","TOP-UP");//商品名
            data.put("fxfee",amount);//支付金额 单位元
            data.put("fxnotifyurl",notifyUrl);//异步回调 , 支付结果以异步为准
            data.put("fxbackurl",entity.getRefererUrl());//同步回调 不作为最终支付结果为准，请以异步回调为准
            //【微信wap：wxwap】【支付宝wap：zfbwap】【支付宝扫码：zfbsm】【网银：bank】【银联wap：ylwap】【京东跳转扫码：jdewm】【银联跳转扫码：ylewm】
            if(type == 1){
                data.put("fxpay","bank");//支付类型 
                data.put("fxbankcode",entity.getPayCode());//
            }else{
                if(entity.getPayCode().equalsIgnoreCase("zfbsm")){
                    data.put("fxsmstyle", "1");
                }
                data.put("fxpay",entity.getPayCode());//支付类型 
            }
            data.put("fxip",entity.getIp());//支付端ip地址
            data.put("fxattch", "TOP-UP");
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[SYB]收盈宝支付组装支付请求参数异常:"+e.getMessage());
            throw new Exception("[SYB]收盈宝支付组装支付请求参数异常");
        }
    }
    
    /**
     * 
     * @Description 生成签名串
     * @param data
     * @param type 1 支付  0 回调
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data,Integer type) throws Exception{
        logger.info("[SYB]收盈宝支付生成支付签名串开始======================START========================");
        try {
            StringBuffer sb = new StringBuffer();
            if(type == 1){
                //生成支付待签名串,签名【md5(商务号+商户订单号+支付金额+异步通知地址+商户秘钥)】
                sb.append(fxid).append(data.get("fxddh")).append(data.get("fxfee")).append(notifyUrl);
            }else{
                //签名【md5(订单状态+商务号+商户订单号+支付金额+商户秘钥)】
                sb.append(data.get("fxstatus")).append(fxid).append(data.get("fxddh")).append(data.get("fxfee"));
            }
            sb.append(secret);
            String signStr = sb.toString();
            logger.info("[SYB]收盈宝支付生成待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[SYB]收盈宝支付生成加密签名串:"+sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[SYB]收盈宝支付生成支付签名串异常:"+e.getMessage());
            throw new Exception("[SYB]收盈宝支付生成支付签名串异常");
        }
    }
}
