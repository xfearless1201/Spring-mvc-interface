package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName HLFPayServiceImpl
 * @Description 惠拉付支付
 * @author Hardy
 * @Date 2018年10月23日 下午5:37:01
 * @version 1.0.0
 */
public class HLFPayServiceImpl implements PayService{
    
    private static final Logger logger = LoggerFactory.getLogger(HLFPayServiceImpl.class);
    
    private String merchantId;//商户号ID
    
    private String notifyUrl;//服务器通知地址
    
    private String payUrl;//支付地址
    
    private String payWapMark;//商户支付通道标示
    
    private String secret;//签名秘钥
    
    public HLFPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("merchantId")){
                this.merchantId = data.get("merchantId");
            }
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
            }
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("payWapMark")){
                this.payWapMark = data.get("payWapMark");
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
        logger.info("[HLF]惠拉付支付扫码支付开始======================START====================");
        try {
            //获取支付请求参数
            Map<String,Object> data = sealRequest(payEntity);
            //签名（将sign以外所有参数按照第一个字符的键值ASCII码递增排序,组合成“参数=参数值”的格式，并且把这些参数用&字符连接起来,此时生成的字符串为待签名字符串。MD5签名的商户需要将key的值拼接在字符串后面，调用MD5算法生成sign）
            String sign = generatorSign(data);
            data.put("sign",sign);
            logger.info("[HLF]惠拉付支付请求报文:"+JSONObject.fromObject(data).toString());
            //发起支付请求
            String response = HttpUtils.toPostJsonStr(JSONObject.fromObject(data), payUrl);
            if(StringUtils.isBlank(response)){
                logger.info("[HLF]惠拉付支付发起HTTP请求失败,无响应结果!");
                return PayUtil.returnPayJson("error", "2", "下单失败:[发起HTTP请求无响应结果!]","",0,"",response);
            }
            logger.info("[HLF]惠拉付支付发起HTTP请求响应结果:"+response);
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(jsonObject.containsKey("mark") && jsonObject.getString("mark").equals("0")){
                //支付结果
                String pay_url = jsonObject.getString("pay_url");
                return PayUtil.returnPayJson("success", "4", "下单成功!", payEntity.getUsername(), payEntity.getAmount(), payEntity.getOrderNo(), pay_url);
            }
            return PayUtil.returnPayJson("error","2","下单失败","",0,"",response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HLF]惠拉付支付扫码支付异常:"+e.getMessage());
            return PayUtil.returnPayJson("error","2","下单异常!","",0,"",e.getMessage());
        }
    }

    /**
     * 回调
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[HLF]惠拉付支付回调验签开始======================START=======================");
        try {
            //获取验签原签名串
            String sourceSign = data.get("sign");
            logger.info("[HLF]惠拉付支付回调验签服务器签名串:"+sourceSign);
            //生成签名串
            Map<String,Object> map = new HashMap<>();
            for(Map.Entry<String,String> entry : data.entrySet()){
                map.put(entry.getKey(), entry.getValue());
            }
            String sign = generatorSign(map);
            logger.info("[HLF]惠拉付支付回调验签生成签名串:"+sign);
            
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HLF]惠拉付支付回调验签异常:"+e.getMessage());
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
    private Map<String,Object> sealRequest(PayEntity entity) throws Exception{
        logger.info("[HLF]惠拉付支付组装支付请求参数开始==================START===================");
        try {
            //创建支付请求参数存储对象
            Map<String,Object> data = new HashMap<>();
            String amount = new DecimalFormat("##").format(entity.getAmount()*100);//订单金额，单位为分
            Integer cope_pay_amount = Integer.parseInt(amount);
            Integer pay_type = Integer.parseInt(entity.getPayCode());
            data.put("cope_pay_amount",cope_pay_amount);//应付金额（单位分 订单金额-优惠金额）
            data.put("merchant_open_id",merchantId);//商户open_id
            data.put("merchant_order_number",entity.getOrderNo());//商户订单号(不可重复)
            data.put("notify_url",notifyUrl);//服务器通知地址
            data.put("pay_type",pay_type);//支付方式1支付宝2微信
            data.put("pay_wap_mark",payWapMark);//商户支付通道标示
            data.put("subject", "TOP-UP");//订单名称
            data.put("timestamp",System.currentTimeMillis()+"");//当前时间戳
            return data;
        } catch (Exception e) {
            logger.error("[HLF]惠拉付支付组装支付请求参数异常:"+e.getMessage());
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
    private String generatorSign(Map<String,Object> data) throws Exception{
        logger.info("[HLF]惠拉付支付生成支付签名串开始====================START=========================");
        try {
            //签名规则:（将sign以外所有参数按照第一个字符的键值ASCII码递增排序,组合成“参数=参数值”的格式，
            //并且把这些参数用&字符连接起来,此时生成的字符串为待签名字符串。MD5签名的商户需要将key的值拼接在字符串后面，调用MD5算法生成sign）
            Map<String,Object> treemap = MapUtils.sortMapByKeys(data);
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = treemap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = treemap.get(key)+"";
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign")) continue;
                sb.append("&").append(key).append("=").append(val);
            }
            sb.append(secret);
            String signStr = sb.toString().replaceFirst("&", "");
            logger.info("[HLF]惠拉付支付生成支付待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[HLF]惠拉付支付生成支付加密签名串:"+sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("生成支付签名串异常!");
        }
    }
}
