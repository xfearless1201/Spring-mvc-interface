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
 * @ClassName ZYPayServiceImpl
 * @Description 中意支付
 * @author Hardy
 * @Date 2018年11月26日 下午6:35:55
 * @version 1.0.0
 */
public class ZYPayServiceImpl implements PayService {
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(ZYPayServiceImpl.class);
    
    private String appid;//申请开户后新建自动生成一个APPID号码
    
    private String notifyUrl;
    
    private String payUrl;
    
    private String sercet;//秘钥 

    public ZYPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
           
            if(data.containsKey("appid")){
                this.appid = data.get("appid");
            }
            
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
            }
            
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            
            if(data.containsKey("sercet")){
                this.sercet = data.get("sercet");
            }
        }
    }

    /**
     * 
     * @Description 网银支付
     * @param payEntity
     * @return
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 
     * @Description 扫码支付
     * @param payEntity
     * @return
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[ZY]中亿扫码支付开始====================START==================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity);
            //生成签名串
            String sign = generatorSign(data, 1);
            data.put("sign", sign);
            logger.info("[ZY]中亿支付请求参数报文:{}",JSONObject.fromObject(data).toString());
            //发起form表单请求
            String formStr = HttpUtils.generatorForm(data, payUrl);
            logger.info("[ZY]中亿支付发起form表单请求报文:{}",formStr);
            return PayResponse.sm_form(payEntity, formStr, "下单成功!");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[ZY]中亿扫码支付异常:{}",e.getMessage());
            return PayResponse.error("中亿扫码支付异常");
        }
    }

    /**
     * 
     * @Description 回调
     * @param data
     * @return
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[ZY]中亿支付回调验签开始=====================START======================");
        try {
            //获取回调原签名串
            String sourceSign = data.get("sign");
            logger.info("[ZY]中亿支付回调原签名串:{}",sourceSign);
            //生成回调签名串
            String sign = generatorSign(data, 0);
            logger.info("[ZY]中亿支付回调生产加密签名串:{}",sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[ZY]中亿支付回调验签异常:{}",e.getMessage());
        }
        return "faild";
    }
    
    
    /**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @return
     */
    private Map<String,String> sealRequest(PayEntity entity) throws Exception{
        logger.info("[ZY]中亿支付封装支付请求参数开始==============START==============");
        try {
            //创建支付请求参数存储对象
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            data.put("appid",appid);//申请开户后新建自动生成一个APPID号码
            data.put("total_fee",amount);//支付金额
            data.put("type",entity.getPayCode());//支付类型 1支付宝，2QQ钱包，3微信
            data.put("out_trade_no",entity.getOrderNo());//商户订单号
            data.put("webname","pay");//网站名称
            data.put("subject","top-up");//商品名
            data.put("sign","");//接口请求校验码
            data.put("return_url",entity.getRefererUrl());//支付成功返回网址
            data.put("notify_url",notifyUrl);//支付成功异步通知
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[ZY]中亿支付封装支付请求参数异常:{}",e.getMessage());
            throw new Exception("中亿支付封装支付请求参数异常");
        }
    }
    
    /**
     * 
     * @Description 生成签名
     * @param data
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data,int signType) throws Exception{
        logger.info("[ZY]中亿支付生成签名串开始=================START================");
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(appid).append(sercet);
            if(signType == 1){
                //支付签名
                sb.append(data.get("out_trade_no")).append(data.get("total_fee"));
            }else{
                //回调签名
                sb.append(data.get("out_trade_no")).append(data.get("money"));
            }
            String signStr = sb.toString();
            logger.info("[ZY]中亿支付生成待签名串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[ZY]中亿支付生成加密签名串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[ZY]中亿支付生成签名串异常:{}",e.getMessage());
            throw new Exception("中亿支付生成签名串异常");
        }
    }

}
