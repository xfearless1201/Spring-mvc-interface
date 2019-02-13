package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
 * @ClassName YXINPayServiceImpl
 * @Description 银鑫支付
 * @author Hardy
 * @Date 2019年1月25日 下午12:05:19
 * @version 1.0.0
 */
public class YXINPayServiceImpl implements PayService {
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(YXINPayServiceImpl.class);
    
    private String merchId;//商户号
    
    private String scanPayUrl;//扫码支付地址
    
    private String h5PayUrl;//H5支付地址
    
    private String notifyUrl;//回调地址
    
    private String md5Key;//签名key

    //构造器,初始化参数
    public YXINPayServiceImpl(Map<String,String> data) {
        if(MapUtils.isNotEntity(data)){
            if(data.containsKey("merchId")){
                this.merchId = data.get("merchId");
            }
            if(data.containsKey("scanPayUrl")){
                this.scanPayUrl = data.get("scanPayUrl");
            }
            if(data.containsKey("h5PayUrl")){
                this.h5PayUrl = data.get("h5PayUrl");
            }
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
            }
            if(data.containsKey("md5Key")){
                this.md5Key = data.get("md5Key");
            }
        }
        
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[YXIN]银鑫支付扫码支付开始==============START=============");
        try {
            
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign", sign);
            
            logger.info("[YXIN]银鑫支付扫码支付请求参数报文:{}",JSONObject.fromObject(data).toString());
            
            //发起HTTP请求
            String payUrl = "";
            if(StringUtils.isBlank(payEntity.getMobile())){
                //PC扫码
                payUrl = this.scanPayUrl;
            }else{
                payUrl = this.h5PayUrl;
            }
            
            String response = HttpUtils.toPostForm(data, payUrl);
            
            if(StringUtils.isBlank(response)){
                logger.info("[YXIN]银鑫支付扫码支付发起HTTP请求无响应结果");
                return PayResponse.error("[YXIN]银鑫支付扫码支付发起HTTP请求无响应结果");
            }
            
            logger.info("[YXIN]银鑫支付扫码支付发起HTTP请求响应结果:{}",response);
            
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(jsonObject.containsKey("code") && "0".equals(jsonObject.getString("code"))){
                String qcurl = jsonObject.getJSONObject("data").getString("url");
                return PayResponse.sm_link(payEntity, qcurl, "支付下单成功");
            }
            return PayResponse.error("下单失败:" + response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YXIN]银鑫支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[YXIN]银鑫支付扫码支付异常");
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[YXIN]银鑫支付回调验签开始==============START==============");
        try {
            
            String sourceSign = data.get("sign");
            logger.info("[YXIN]银鑫支付回调验签获取原签名串:{}",sourceSign);
            String sign = generatorSign(data);
            logger.info("[YXIN]银鑫支付回调验签生成签名串:{}",sign);
            
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YXIN]银鑫支付回调验签异常:{}",e.getMessage());
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
        logger.info("[YXIN]银鑫支付组装支付请求参数开始==================START=================");
        try {
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            data.put("merchantName",merchId);//商户号
//            data.put("channelId","");//通道ID,如果不传自动获取通道
            data.put("orderId",entity.getOrderNo());//订单号,确保订单唯一性标志,16-32位的数字或字母
            data.put("amount",amount);//扫码入款金额,实际付款金额以返回为准
            data.put("noticeUrl",notifyUrl);//通知地址,比如:http://www.google.com/
            data.put("signType","MD5");//MD5
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YXIN]银鑫支付组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[YXIN]银鑫支付组装支付请求参数异常");
        }
    }

    /**
     * 
     * @Description 生成签名
     * @param data
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data) throws Exception{
        logger.info("[YXIN]银鑫支付生成签名开始=================START=================");
        try {
            StringBuffer sb = new StringBuffer();
            //排序
            Map<String,String> map = MapUtils.sortByKeys(data);
            Iterator<String> iterator = map.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = map.get(key);
                
                if(StringUtils.isBlank(val) || "sign".equalsIgnoreCase(key) 
                        || "signType".equalsIgnoreCase(key) || "state".equals(key)) continue;
                
                sb.append(key).append("=").append(val).append("&");
            }
            
            String signStr = sb.append("key=").append(md5Key).toString();
            logger.info("[YXIN]银鑫支付生成待签名串:{}",signStr);
            
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[YXIN]银鑫支付生成签名串:{}",sign);    
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YXIN]银鑫支付生成签名异常:{}",e.getMessage());
            throw new Exception("[YXIN]银鑫支付生成签名异常");
        }
    }
}
