package com.cn.tianxia.pay.impl;

import java.net.URLEncoder;
import java.text.DecimalFormat;
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
 * @ClassName YFTPPayServiceImpl
 * @Description 易付通支付
 * @author Hardy
 * @Date 2018年12月28日 下午9:25:54
 * @version 1.0.0
 */
public class YFTPPayServiceImpl implements PayService{

    //日志
    private static final Logger logger = LoggerFactory.getLogger(YFTPPayServiceImpl.class);
    
    private String merno;//商户号
    
    private String payUrl;//支付地址
    
    private String notifyUrl;//回调地址
    
    private String secret;//秘钥 

    //构造器,初始化参数
    public YFTPPayServiceImpl(Map<String,String> data) {
        if(MapUtils.isNotEntity(data)){
            if(data.containsKey("merno")){
                this.merno = data.get("merno");
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
        logger.info("[YFTP]易付通支付扫码支付开始=============START=============");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity);
            //签名
            String sign = generatorSign(data,1);
            data.put("sign", sign);
            logger.info("[YFTP]易付通支付扫码支付请求参数报文:{}",JSONObject.fromObject(data).toString());
            //发起HTTP请求
            String response = HttpUtils.toPostForm(data, payUrl);
            if(StringUtils.isBlank(response)){
                logger.info("[YFTP]易付通支付扫码支付发起HTTP请求无响应结果");
                return PayResponse.error("[YFTP]易付通支付扫码支付发起HTTP请求无响应结果");
            }
            logger.info("[YFTP]易付通支付扫码支付发起HTTP请求响应结果:{}",response);
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(jsonObject.containsKey("code") && "0".equals(jsonObject.getString("code"))){
                //成功
                JSONObject json = jsonObject.getJSONObject("data");
                String qrCode = json.getString("qrCode");
                if(StringUtils.isBlank(payEntity.getMobile())){
                    //PC
                    return PayResponse.sm_qrcode(payEntity, qrCode, "下单成功");
                }
                return PayResponse.sm_link(payEntity, qrCode, "下单成功");
            }
            return PayResponse.error("下单失败:"+response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YFTP]易付通支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[YFTP]易付通支付扫码支付异常");
        }
    }

    /**
     * 回调
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[YFTP]易付通支付回调验签开始================START=================");
        try {
            
            String sourceSign = data.get("sign");
            logger.info("[YFTP]易付通支付获取回调原签名串:{}",sourceSign);
            String sign =generatorSign(data,0);
            logger.info("[YFTP]易付通支付生成加密签名串:{}",sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YFTP]易付通支付回调验签异常:{}",e.getMessage());
        }
        return "faild";
    }
    
    /**
     * 
     * @Description 组装支付请求参数
     * @param entity
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity) throws Exception{
        logger.info("[YFTP]易付通支付组装支付请求参数开始==================START================");
        try {
            Map<String,String> data = new HashMap<>();
            String time = String.valueOf(System.currentTimeMillis()/1000);
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            data.put("mercId",merno);//商户编号
            data.put("mercOrderId",entity.getOrderNo());//订单编号,最长 24 位，且同一个商户订单编号不可重复，支持字母和数字。
            data.put("amount",amount);//交易金额,单位：元
            data.put("notifyUrl",notifyUrl);//结果通知 URL,通知地址
            data.put("productName","TOP-UP");//商品名称
//            data.put("productDesc","TOP-UP");//商品描述
            data.put("channel",entity.getPayCode());//支付渠道,alipay/wechat,目前仅支持 alipay
            data.put("time",System.currentTimeMillis()+"");//秒级时间戳,北京时间，示例：1543474258
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YFTP]易付通支付组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[YFTP]易付通支付组装支付请求参数异常");
        }
    }
    
    
    /**
     * 
     * @Description 生成签名串
     * @param data
     * @param type 1 支付 2 回调
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data,int type) throws Exception{
        logger.info("[YFTP]易付通支付生成签名串开始================START=================");
        try {
            StringBuffer sb = new StringBuffer();
            Map<String,String> sortmap = MapUtils.sortByKeys(data);
            Iterator<String> iterator = sortmap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = sortmap.get(key);
                if("notifyUrl".equalsIgnoreCase(key) || "productName".equalsIgnoreCase(key)){
                    val = URLEncoder.encode(val,"UTF-8");
                }
                if(StringUtils.isBlank(val) || "sign".equalsIgnoreCase(key)) continue;
                sb.append("&").append(key).append("=").append(val);
            }
            sb.append(secret);
            String signStr = sb.toString().replaceFirst("&", "");
            logger.info("[YFTP]易付通支付生成待加密串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[YFTP]易付通支付生成加密串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YFTP]易付通支付生成签名串异常:{}",e.getMessage());
            throw new Exception("[YFTP]易付通支付生成签名串异常");
        }
    }
}
