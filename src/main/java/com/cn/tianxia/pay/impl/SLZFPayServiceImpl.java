package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;
import com.cn.tianxia.pay.utils.XmlUtils;

import net.sf.json.JSONObject;
/**
 * 速龙支付
 * @author TX
 */
public class SLZFPayServiceImpl implements PayService{
	private final Logger logger = LoggerFactory.getLogger(SLZFPayServiceImpl.class);
	/**支付地址*/
	private String payUrl;
	/**商户编号*/
	private String payMemberid;
	/**商户接收支付成功数据的地址*/
	private String payNotifyUrl;
	/**商户密钥*/
	private String md5Key;
	
	public SLZFPayServiceImpl(Map<String,String> data) {
		if(data!=null){
			if(data.containsKey("payUrl")){
				this.payUrl = data.get("payUrl");
			}
			if(data.containsKey("payMemberid")){
				this.payMemberid = data.get("payMemberid");
			}
			if(data.containsKey("payNotifyUrl")){
				this.payNotifyUrl = data.get("payNotifyUrl");
			}
			if(data.containsKey("md5Key")){
				this.md5Key = data.get("md5Key");
			}
		}
	}
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		logger.info("[SLZF]速龙网银支付开始======================START==================");
		try {
			//封装请求参数
			Map<String, String> data = sealRequest(payEntity,"0");
			//生成签名串
			String sign = generatorSign(data);
			data.put("sign", sign);
			logger.info("[SLZF]速龙网银支付请求参数:"+JSONObject.fromObject(data));
			//生成请求表单
			String resStr = HttpUtils.toPostXml(data, payUrl);
			logger.info("[SLZF]速龙网银支付响应信息:"+resStr);
			if(StringUtils.isBlank(resStr)){
				logger.info("[SLZF]速龙网银支付发起HTTP请求无响应结果");
				return PayResponse.error("[SLZF]速龙网银支付发起HTTP请求无响应结果");
			}
		    return XmlUtils.parseXml(resStr);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("[SLZF]速龙扫码支付生成异常:"+e.getMessage());
			return PayResponse.error("[SLZF]速龙扫码支付下单失败");
		}
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("[SLZF]速龙扫码支付开始======================START==================");
		try {
			//封装请求参数
			Map<String, String> data = sealRequest(payEntity,"1");
			//生成签名串
			String sign = generatorSign(data);
			data.put("sign", sign);
			logger.info("[SLZF]速龙扫码支付请求参数:"+JSONObject.fromObject(data));
			//生成请求表单
			String resStr = HttpUtils.toPostXml(data, payUrl);
			
			logger.info("[SLZF]速龙扫码支付响应信息:"+resStr);
			if(StringUtils.isBlank(resStr)){
				logger.info("[SLZF]速龙扫码支付发起HTTP请求无响应结果");
				return PayResponse.error("[SLZF]速龙扫码支付发起HTTP请求无响应结果");
			}
		    return XmlUtils.parseXml(resStr);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("[SLZF]速龙扫码支付生成异常:"+e.getMessage());
			return PayResponse.error("[SLZF]速龙扫码支付下单失败");
		}
	}

	@Override
	public String callback(Map<String, String> data) {
		try {
			String sourceSign = data.remove("sign");
			String sign = generatorSign(data);
			logger.info("[SLZF]速龙扫码支付回调生成签名串"+sign);
			if(sign.equals(sourceSign)) return "success";
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("[SLZF]速龙扫码支付回调生成签名串异常"+e.getMessage());
		}
		return null;
	}
	/**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @param 
     * @return
     * @throws Exception
     */
	public Map<String, String> sealRequest(PayEntity payEntity,String type){
		DecimalFormat df = new DecimalFormat("0.00");
		Map<String,String> data = new HashMap<>();
		String uid = UUID.randomUUID().toString();
		data.put("customerid", payMemberid);//商户号
		data.put("orderid", payEntity.getOrderNo());//订单号
		data.put("total_fee", df.format(payEntity.getAmount()));//金额
		data.put("notify_url", payNotifyUrl);//异步通知地址
		data.put("nonce_str", uid.replace("-", ""));//随机字符串
		if("0".equals(type)){
			data.put("trade_type", "bank");//交易类型
		}else{
			data.put("trade_type", payEntity.getPayCode());//交易类型
			if("code".equals(payEntity.getPayCode())||"qq".equals(payEntity.getPayCode())){
				data.put("buyername", payEntity.getUsername());//支付类型
				data.put("subject", "Pay");//订单标题
			}
			
			
		}
		return data;
	}
	/**
     * 
     * @Description 生成签名串
     * @param data
     * @return
	 * @throws Exception
     */
    public String generatorSign(Map<String,String> data) throws Exception{
    	Map<String,String> sortmap = MapUtils.sortByKeys(data);
        StringBuffer sb = new StringBuffer();
        Iterator<String> iterator = sortmap.keySet().iterator();
        while(iterator.hasNext()){
            String key = iterator.next();
            String val = sortmap.get(key);
            if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign")) continue;
            sb.append(key).append("=").append(val).append("&");
        }
        sb.append("key=").append(md5Key);
    	//生成待签名串
    	String signStr = sb.toString();
    	logger.info("[SLZF]速龙扫码支付生成待签名串:{}",signStr);
    	String sign = MD5Utils.md5toUpCase_32Bit(signStr);
    	logger.info("[SLZF]速龙扫码支付生成加密签名串:{}",sign);
    	return sign;
    }
}
