package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.MD5Util;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.qyf.util.ToolKit;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName NWTPayServiceImpl
 * @Description 新万通支付
 * @author Hardy
 * @Date 2018年9月20日 下午6:45:38
 * @version 1.0.0
 */
public class NWTPayServiceImpl implements PayService{
    
    private String MerId;//万通支付分配给您的商户号
    
    private String NotifyUrl;//异步回调地址
    
    private String payUrl;//支付URL
    
    private String sercet;//签名key
    
    public NWTPayServiceImpl(Map<String,String> data) {
        if(data != null){
            if(data.containsKey("MerId")){
                this.MerId = data.get("MerId");
            }
            if(data.containsKey("NotifyUrl")){
                this.NotifyUrl = data.get("NotifyUrl");
            }
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("sercet")){
                this.sercet = data.get("sercet");
            }
        }
    }

    //日志
    private static final Logger logger = LoggerFactory.getLogger(NWTPayServiceImpl.class);
    
    /**
     * 网银支付
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[NWT]新万通网银支付开始=============START=================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 1);
            //生成加密串
            String sign = generatorSign(data,1);
            data.put("sign", sign);
            String response = generatorForm(data);
            return PayUtil.returnWYPayJson("success", "form", response, payEntity.getPayUrl(), "");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[NWT]新万通网银支付异常:"+e.getMessage());
            return PayUtil.returnWYPayJson("error", "form", "", "", "");
        }
    }

    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[NWT]新万通扫码支付开始=============START=================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 0);
            //生成加密串
            String sign = generatorSign(data,1);
            data.put("sign", sign);
            String response = generatorForm(data);
            return PayUtil.returnPayJson("success", "1", "生成form表单成功", "", 0, "", response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[NWT]新万通扫码支付异常:"+e.getMessage());
            return PayUtil.returnWYPayJson("error", "form", "", "", "");
        }
    }
    
    /**
     * 
     * @Description 回调
     * @param data
     * @return
     */
    @Override
    public String callback(Map<String,String> data){
        logger.info("[NWT]新万通回调开始====================START=====================");
        try {
            //获取回调签名
            String sourceSign = data.get("Sjt_Sign");
            //获取签名
            String sign = generatorSign(data,0);
            if(sourceSign.equals(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[NWT]新万通回调异常:"+e.getMessage());
        }
        
        return "";
    }
    
    /**
     * 
     * @Description 组装支付请求参数
     * @param payEntity
     * @return
     */
    private Map<String,String> sealRequest(PayEntity payEntity,Integer type) throws Exception{
        logger.info("[NWT]新万通支付组装请求参数开始==================start====================");
        try {
            //创建存储参数对象
            Map<String,String> data = new HashMap<>();
            String orderId = payEntity.getOrderNo();
            String amount = new DecimalFormat("0.00").format(payEntity.getAmount());
            
            data.put("MerId",MerId);//万通支付分配给您的商户号
            data.put("OrderId",orderId);//商户订单号，请保证唯一
            data.put("Amt",amount);//金额：单位（元）   
            data.put("NotifyUrl",NotifyUrl);//异步回调地址
            data.put("Returnurl",payEntity.getRefererUrl());//同步返回地址
            data.put("Paytype",payEntity.getPayCode());//请求类型 1 网银网关 2 快捷支付 3 支付宝网页 4 支付宝wap 5 支付宝扫码6微信扫码7微信H5
            if(type == 1){
                //网银支付
                data.put("Paytype","1");//请求类型 1 网银网关 2 快捷支付 3 支付宝网页 4 支付宝wap 5 支付宝扫码6微信扫码7微信H5
                data.put("BankCode",payEntity.getPayCode());//银行名称缩写 如果Paytype填1，此项具体内容参考另一个文档
            }else{
                //其他支付方式
                data.put("Paytype",payEntity.getPayCode());//请求类型 1 网银网关 2 快捷支付 3 支付宝网页 4 支付宝wap 5 支付宝扫码6微信扫码7微信H5
            }
//            data.put("PayBank","");//支付编码
            
//            data.put("Pid","");//商品id
//            data.put("Pdesc","");//商品名称
//            data.put("attach","");//商户传值，原样返回
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[NWT]新万通支付组装请求参数异常:"+e.getMessage());
            throw new Exception("组装支付请求参数异常!");
        }
    }


    /**
     * 
     * @Description 生成签名
     * @param data
     * @param type 1 支付  0 回调
     * @return
     * @throws Exception
     */
    public String generatorSign(Map<String,String> data,Integer type) throws Exception{
        logger.info("[NWT]新万通支付生成签名开始====================START================");
        try {
            //参数排序
            Map<String,String> treemap = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            
            treemap.putAll(data);
            logger.info("[NWT]新万通排序后的参数:"+JSONObject.fromObject(treemap).toString());
            //生成待签名穿
            StringBuffer sb = new StringBuffer();
            Iterator<String> keys = treemap.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                String val = treemap.get(key);
                
                if(StringUtils.isBlank(val) || key.equals("sign") || key.equals("Sjt_Sign")) continue;
                if(type == 1){
                    sb.append(val).append("+");
                }else{
                    sb.append(val);
                }
            }
            sb.append(sercet);
            String signStr = sb.toString();
            logger.info("[NWT]新万通支付生成待签名串:"+signStr);
            String sign = ToolKit.MD5(signStr, "UTF-8").toLowerCase();
            logger.info("[NWT]新万通支付生成MD5加密签名串:"+sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[NWT]新万通支付生成签名异常:"+e.getMessage());
            throw new Exception("生成签名异常!");
        }
    }
    
    /**
     * 
     * @Description 生成form表单
     * @param data
     * @return
     * @throws Exception
     */
    private String generatorForm(Map<String,String> data) throws Exception{
        String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
                + payUrl + "\">";
        for (String key : data.keySet()) {
            if (StringUtils.isNotBlank(data.get(key)))
                FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + data.get(key) + "'>\r\n";
        }
        FormString += "</form></body>";
        return FormString;
    }
    
    public static void main(String[] args) {
//        JSONObject data = new JSONObject();
//        data.put("MerId", "10437");
//        data.put("NotifyUrl", "http://www.baidu.com");
//        data.put("sercet", "ba828a940ce75468d5cce004a12611f9b4c8e30e96eee2bdf7f1b66d6cbbba0a");
//        data.put("payUrl", "http://www.wtzfpay.com/Payapi_Index_Pay.html");
//        System.err.println(data.toString());
        String src = "2+437+NWTtyc201809221535301+1+20180922153637+20180922153535195724+10437+20.000+ba828a940ce75468d5cce004a12611f9b4c8e30e96eee2bdf7f1b66d6cbbba0a";
        
        src = src.replace("+", "");
        
        String sign = MD5Util.encode(src);
        String sourceSign = "4e4ee5b89c04ed96319ec9dff2fbea58";
        if(sign.equals(sourceSign)){
            System.err.println("验签正确");
        }else{
            System.err.println("验签失败");
        }
    }
}
