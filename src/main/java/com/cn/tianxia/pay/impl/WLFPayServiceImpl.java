package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName WLFPayServiceImpl
 * @Description 威力付支付
 * @author Hardy
 * @Date 2019年1月1日 下午3:02:21
 * @version 1.0.0
 */
public class WLFPayServiceImpl implements PayService {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(WLFPayServiceImpl.class);
    
    private String merId;//商户号
    
    private String payUrl;//支付地址
    
    private String notifyUrl;//回调通知地址
    
    private String md5Key;//秘钥
    
    private String secpVer;//安全协议版本
    
    private String macKeyId;//密钥编号，由平台提供，现与商户号相同
    
    //构造器,初始化参数
    public WLFPayServiceImpl(Map<String,String> data) {
        if(MapUtils.isNotEntity(data)){
            if(data.containsKey("merId")){
                this.merId = data.get("merId");
            }
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
            }
            if(data.containsKey("md5Key")){
                this.md5Key = data.get("md5Key");
            }
            if(data.containsKey("secpVer")){
                this.secpVer = data.get("secpVer");
            }else{
                this.secpVer = "icp3-1.1";
            }
            
            if(data.containsKey("macKeyId")){
                this.macKeyId = data.get("macKeyId");
            }else{
                this.macKeyId = this.merId;
            }
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[WLF]威力付支付网银支付开始==============START===============");
        try {
            
            //获取支付请求报文
            Map<String,String> data = sealRequest(payEntity, 1);
            //生成签名
            String sign = generatorSign(data);
            
            data.put("mac", sign);
            
            logger.info("[WLF]威力付支付网银支付请求参数报文:{}",JSONObject.fromObject(data).toString());
            
            //发起支付请求,生成form表单请求模式
            String formStr = HttpUtils.generatorForm(data, payUrl);
            logger.info("[WLF]威力支付网银支付生成form表单请求结果:{}",formStr);
            return PayResponse.sm_form(payEntity, formStr, "下单成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[WLF]威力付支付网银支付异常:{}",e.getMessage());
            return PayResponse.error("[WLF]威力付支付网银支付异常");
        }
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[WLF]威力付支付扫码支付开始==============START===============");
        try {
            
            //获取支付请求报文
            Map<String,String> data = sealRequest(payEntity, 0);
            //生成签名
            String sign = generatorSign(data);
            
            data.put("mac", sign);
            
            logger.info("[WLF]威力付支付扫码支付请求参数报文:{}",JSONObject.fromObject(data).toString());
            
            //发起支付请求,生成form表单请求模式
            String formStr = HttpUtils.generatorForm(data, payUrl);
            logger.info("[WLF]威力支付扫码支付生成form表单请求结果:{}",formStr);
            return PayResponse.sm_form(payEntity, formStr, "下单成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[WLF]威力付支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[WLF]威力付支付扫码支付异常");
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[WLF]威力付支付回调验签开始=============START==============");
        try {
            String sourceSign = data.get("mac");
            logger.info("[WLF]威力付支付回调验签获取原签名串:{}",sourceSign);
            String sign = generatorSign(data);
            logger.info("[WLF]威力付支付回调验签生成加密签名串:{}",sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[WLF]威力付支付回调验签异常:{}",e.getMessage());
        }
        return "faild";
    }
    
    
    /**
     * 
     * @Description 组装支付请求参数
     * @param entity
     * @param type 1 网银 2 扫码支付
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,int type) throws Exception{
        logger.info("[WLF]威力付支付组装支付请求参数开始===============START================");
        try {
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("##").format(entity.getAmount() * 100);//订单金额,单位:分
            String timeStamp = new SimpleDateFormat("YYYYMMDDhhmmss").format(new Date());
            data.put("txnType","01");//报文类型   N2  M   01
            data.put("secpVer",secpVer);//安全协议版本 AN3..16 M   icp3-1.1   （注意，旧版本的secpver = icp3-1.0）
            data.put("secpMode","perm");//安全协议类型    AN4..8  M   固定值 perm
            data.put("macKeyId",macKeyId);//密钥识别  ANS1..16    M   密钥编号，由平台提供，现与商户号相同
            data.put("orderDate",timeStamp.substring(0, 8));//下单日期 N8  M   YYYYMMDD
            data.put("orderTime",timeStamp.substring(8,timeStamp.length()));//下单时间 N6  M   hhmmss
            data.put("merId",merId);//商户代号 AN1..15 M   由平台分配的商户号
            data.put("orderId",entity.getOrderNo());//商户订单号  AN8..32 M   商户系统产生，同一商户同一交易日唯一
            data.put("pageReturnUrl",entity.getRefererUrl());//交易结果页面通知地址   ANS1..256   M   
            data.put("notifyUrl",notifyUrl);//交易结果后台通知地址   ANS1..128   M   交易结果以后台通知为准
            data.put("productTitle","TOP-UP");//商品名称  ANS1..64    M   用已标注在支付页面主要的商品说明
            data.put("txnAmt",amount);//交易金额    N1..12  M   单位为分，实际交易金额
            data.put("currencyCode","156");//交易币种  NS3 M   默认：156
//            data.put("cardType","");//卡类型   AN1..16 O   DT01(借记卡)/CR01(贷记卡)暂只支持借记卡
//            data.put("accNum","");//银行卡号    N8..20  O   交易的银行卡卡号
            if(type == 1){
                //网银支付请求参数选项
                data.put("txnSubType","21");//报文子类    N2  M   21
                data.put("bankNum",entity.getPayCode());//联行号    N8  O   用户支付卡所属银行所对应的英文代号，详见联行号对照表：网银支付：不跳转收银台，直连银行必传
            }else{
                data.put("txnSubType",entity.getPayCode());//报文子类
//                data.put("productDesc","");//商品说明   ANS1.128    O   商品辅助说明
//                data.put("payLimit","");//支付方式限制    N12 O   0-接受信用卡(默认，不限制)1-限定不能使用信用卡
                data.put("clientIp",entity.getIp());//客户端ip N1..15  M   微信H5支付时，必填。为用户真实IP地址
                data.put("sceneBizType","WAP");//场景业务类型    AN3..11 M   WAP|IOS_APP|ANDROID_APP
                data.put("wapUrl",entity.getRefererUrl());//WAP网址   ANS1..256   C   当sceneBizType =WAP时必填
                data.put("wapName","tianxia");//WAP名称  ANS1..48    C   当sceneBizType =WAP时必填
//                data.put("appName","");//应用名    ANS1..48    C   当sceneBizType =IOS_APP|ANDROID_APP时必填
//                data.put("appPackage","");//应用包名    ANS1..48    C   当sceneBizType =IOS_APP|ANDROID_APP时必填
            }
            data.put("timeStamp",timeStamp);//时间戳  N14 M   请带入报文(目前)时间，格式：YYYYMMDDhhmmss
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[WLF]威力付支付组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[WLF]威力付支付组装支付请求参数异常");
        }
    }
    
    /**
     * 
     * @Description 生成签名串
     * @param data
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data) throws Exception{
        logger.info("[WLF]威力付支付生成签名串开始===============START===================");
        try {
            
            StringBuffer sb = new StringBuffer();
            //对签名参数进行排序
            Map<String,String> sortmap = MapUtils.sortByKeys(data);
            Iterator<String> iterator = sortmap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = sortmap.get(key);
                        
                if(StringUtils.isBlank(val) || "mac".equalsIgnoreCase(key)) continue;
                
                sb.append(key).append("=").append(val).append("&");
            }
            
            String signStr = sb.append("key=").append(md5Key).toString();
            logger.info("[WLF]威力付支付生成待签名串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[WLF]威力付支付生成签名串异常:{}",e.getMessage());
            throw new Exception("[WLF]威力付支付生成签名串异常");
        }
    }

}
