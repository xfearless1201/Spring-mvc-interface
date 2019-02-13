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
 * @ClassName BJYXPayServiceImpl
 * @Description 北京易迅支付
 * @author Hardy
 * @Date 2018年10月6日 下午1:47:01
 * @version 1.0.0
 */
public class BJYXPayServiceImpl implements PayService{
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(BJYXPayServiceImpl.class);
    
    private String payUrl;//支付地址
    
    private String customerId;//商户编号
    
    private String notifyUrl;//异步回调地址
    
    private String apiKey;//秘钥

    //构造器，初始化数据
    public BJYXPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("customerId")){
                this.customerId = data.get("customerId");
            }
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
            }
            if(data.containsKey("apiKey")){
                this.apiKey = data.get("apiKey");
            }
        }
    }
    

    /**
     * 网银支付
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[BJYX]北京易迅支付================网银支付开始==================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 1);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign", sign);//md5签名串
            logger.info("[BJYX]北京易迅支付请求参数:"+JSONObject.fromObject(data).toString());
            //生成请求表单
            String formStr = HttpUtils.generatorForm(data, payUrl);
            logger.info("[BJYX]北京易迅支付生成form表单结果:"+formStr);
            return PayUtil.returnWYPayJson("success", "form", formStr, payEntity.getPayUrl(), "pay");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[BJYX]北京易迅支付网银支付异常:"+e.getMessage());
            return PayUtil.returnWYPayJson("error", "form", "", "", "");
        }
    }
    
    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[BJYX]北京易迅支付=====================扫码支付开始=====================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 0);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign", sign);//md5签名串
            logger.info("[BJYX]北京易迅支付请求参数:"+JSONObject.fromObject(data).toString());
            //生成请求表单
            String formStr = HttpUtils.generatorForm(data, payUrl);
            logger.info("[BJYX]北京易迅支付请求报文:"+formStr);    
            return PayUtil.returnPayJson("success", "1", "下单成功!",payEntity.getUsername(),payEntity.getAmount(),
                    payEntity.getOrderNo(),formStr);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[BJYX]北京易迅支付异常:"+e.getMessage());
            return PayUtil.returnPayJson("error", "1", "下单失败", "", 0, "", "");
        }
    }
    
    /**
     * 回调
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[BJYX]北京易迅支付回调验签开始===================START==============");
        try {
            //获取验签原串
            String sourceSign = data.get("sign");
            //回调签名规则:customerid={value}&status={value}&sdpayno={value}&sdorderno={value}
            //&total_fee={value}&paytype={value}&{apikey}
            //使用md5签名上面拼接的字符串即可生成小写的32位密文
            StringBuffer sb = new StringBuffer();
            sb.append("customerid=").append(customerId).append("&");
            sb.append("status=").append(data.get("status")).append("&");
            sb.append("sdpayno=").append(data.get("sdpayno")).append("&");
            sb.append("sdorderno=").append(data.get("sdorderno")).append("&");
            sb.append("total_fee=").append(data.get("total_fee")).append("&");
            sb.append("paytype=").append(data.get("paytype")).append("&");
            sb.append(apiKey);
            //生成待签名串
            String signStr = sb.toString();
            logger.info("[BJYX]北京易迅支付回调验签生成待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[BJYX]北京易迅支付回调验签,服务器签名:["+sourceSign+"],本地签名:["+sign+"]");
            
            //验签
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[BJYX]北京易迅支付回调验签异常:"+e.getMessage());
        }
        return "faild";
    }

    /**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @param type
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,Integer type) throws Exception{
        logger.info("[BJYX]北京易迅支付组装支付请求参数开始===================START==================");
        try {
            //创建存储支付请求参数对象
            Map<String,String> data = new HashMap<>();
            data.put("version", "1.0");//版本号
            data.put("customerid", customerId);//商户号
            data.put("sdorderno",entity.getOrderNo());//订单号，20位
            //精确到小数点后两位，例如10.24
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            String bankcode = "";
            String get_code = "";//如果只想获取被扫二维码，请设置get_code=1
            data.put("total_fee",amount);//订单金额  
            if(type == 1){
                //网银支付
                data.put("paytype","bank");//支付编号
                bankcode = entity.getPayCode();//银行编号
            }else{
                //其他支付方式
                data.put("paytype",entity.getPayCode());//支付编号
                if(StringUtils.isBlank(entity.getMobile())){
                    //PC，扫码
                    get_code = "1";//如果只想获取被扫二维码，请设置get_code=1
                }
            }
            data.put("bankcode", bankcode);
            data.put("get_code", get_code);
            data.put("notifyurl", notifyUrl);//异步通知URL,不能带有任何参数
            data.put("returnurl", entity.getRefererUrl());//同步跳转URL,不能带有任何参数
            data.put("remark", "TOP-UP");//订单备注说明
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[BJYX]北京易迅支付组装支付请求参数异常:"+e.getMessage());
            throw new Exception("组装支付请求参数异常!");
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
        logger.info("[BJYX]北京易迅支付生成签名串开始================START=================");
        try {
            //使用md5签名上面拼接的字符串即可生成小写的32位密文
            StringBuffer sb = new StringBuffer();
            sb.append("version=").append(data.get("version")).append("&");
            sb.append("customerid=").append(customerId).append("&");
            sb.append("total_fee=").append(data.get("total_fee")).append("&");
            sb.append("sdorderno=").append(data.get("sdorderno")).append("&");
            sb.append("notifyurl=").append(notifyUrl).append("&");
            sb.append("returnurl=").append(data.get("returnurl")).append("&");
            sb.append(apiKey);
            String signStr = sb.toString();
            logger.info("[BJYX]北京易迅支付生成待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[BJYX]北京易迅支付生成加密签名串:"+sign);
            
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[BJYX]北京易迅支付生成签名串异常:"+e.getMessage());
            throw new Exception("生成签名串失败!");
        }
    }
}
