package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName FLZFPayServiceImpl
 * @Description 富乐支付
 * @author Hardy
 * @Date 2018年11月7日 下午8:11:10
 * @version 1.0.0
 */
public class FLZFPayServiceImpl implements PayService{

    //日志 
    private static final Logger logger = LoggerFactory.getLogger(FLZFPayServiceImpl.class);
    
    private String uid;//商户号
    
    private String payUrl;//支付请求地址
    
    private String notifyUrl;//回调请求地址
    
    private String secret;//秘钥
    
    //构造器,初始化参数
    public FLZFPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("uid")){
                this.uid = data.get("uid");
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
        logger.info("[FLZF]富乐支付扫码支付开始==========================START=======================");
        try {
            //组装支付请求参数
            Map<String,String> data = sealRequest(payEntity);
            //生成签名
            String sign = generatorSign(data, 1);
            //秘钥,必填。把使用到的所有参数，连Token一起，按参数1+2+3+4+5+key排序。把参数值拼接在一起。做md5-32位加密，取字符串小写。得到sign。商户密匙key请与我方技术人员联系索取
            data.put("sign",sign);
            logger.info("[FLZF]富乐支付组装支付请求参数报文:{}",JSONObject.fromObject(data).toString());
            //发起支付请求
            String repParams = getParams(data);
            repParams = payUrl + repParams;
            return PayResponse.sm_link(payEntity, repParams, "下单成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[FLZF]富乐支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[FLZF]富乐支付扫码支付异常:"+e.getMessage());
        }
    }

    /**
     * 回调
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[FLZF]富乐支付回调验签开始======================START===================");
        try {
            
            //获取回调原签名串
            String sourceSign = data.get("sign");
            logger.info("[FLZF]富乐支付回调验签原签名串:{}",sourceSign);
            //生成签名串
            String sign = generatorSign(data, 2);
            logger.info("[FLZF]富乐支付回调验生成加密签名串:{}",sign);
            
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[FLZF]富乐支付回调验签异常:{}",e.getMessage());
        }
        return "faild";
    }
    
    private Map<String,String> sealRequest(PayEntity entity) throws Exception{
        logger.info("[FLZF]富乐支付组装支付请求参数开始======================START================");
        try {
            //创建支付请求参数存储对象
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(entity.getAmount());//订单金额
            data.put("uid",uid);//商户uid,必填。您的商户唯一标识，通过我方技术人员处获取。
            data.put("price",amount);//请求支付金额,选填（我方以实际支付金额入帐）。单位：元。精确小数点后2位
            data.put("order_id",entity.getOrderNo());//商户自定义订单号,必填。我们会据此判别是同一笔订单还是新订单。我们回调时，会带上这个参数。例：201810192541 这个订单必须是唯一的，使用一次后自动失效。
            data.put("notify_url",notifyUrl);//通知回调网址,必填。用户支付成功后，我们服务器会主动发送一个GET消息到这个网址。
            data.put("return_url",entity.getRefererUrl());//跳转网址,必填。用户支付成功后，我们会让用户浏览器自动跳转到这个网址。由您自定义。例：http://www.aaa .com/qpay_return
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[FLZF]富乐支付组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("富乐支付组装支付请求参数异常");
        }
    }
    
    /**
     * 
     * @Description 生成签名
     * @param data
     * @param signType 签名类型  1 支付签名 2 回调验签签名
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data,Integer signType) throws Exception{
        logger.info("[FLZF]富乐支付生成支付签名开始====================START====================");
        try {
            StringBuffer sb = new StringBuffer();
            if(signType == 1){
                sb.append(data.get("uid")).append(data.get("price"));
                sb.append(data.get("order_id")).append(data.get("notify_url"));
                sb.append(data.get("return_url")).append(secret);
            }else{
                sb.append(data.get("order_id")).append(data.get("price"));
                sb.append(data.get("txnTime")).append(secret);
            }
            //生成待签名串
            String signStr = sb.toString();
            logger.info("[FLZF]富乐支付生成待签名串:{}",signStr);
            //进行MD5加密,小写
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[FLZF]富乐支付生成加密签名串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[FLZF]富乐支付生成支付签名异常:{}",e.getMessage());
            throw new Exception("[FLZF]富乐支付生成支付签名异常");
        }
    }
    
    /**
     * 
     * @Description 生成请求参数
     * @param data
     * @return
     * @throws Exception
     */
    private String getParams(Map<String,String> data) throws Exception{
        logger.info("[FLZF]富乐支付生成支付请求开始===================START==================");
        try {
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = data.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = data.get(key);
                sb.append("&").append(key).append("=").append(val);
            }
            String repParams = sb.toString().replaceFirst("&", "?");
            logger.info("[FLZF]富乐支付生成支付请求报文:{}",repParams);
            return repParams;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[FLZF]富乐支付生成支付请求异常:{}",e.getMessage());
            throw new Exception("[FLZF]富乐支付生成支付请求异常!");
        }
    }

}
