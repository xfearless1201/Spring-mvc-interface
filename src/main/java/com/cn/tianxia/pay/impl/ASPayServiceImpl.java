package com.cn.tianxia.pay.impl;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.as.util.Rsaencrypt;
import com.cn.tianxia.pay.bfb.util.HttpUtils;
import com.cn.tianxia.pay.dd.util.RSA;
import com.cn.tianxia.pay.dd.util.RSAEncrypt2;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.MapUtils;

import net.sf.json.JSONObject;

/**
 * 安盛支付
 * @author TX
 */
public class ASPayServiceImpl implements PayService{
	private final static Logger logger = LoggerFactory.getLogger(ASPayServiceImpl.class);
	
	private String payUrl;
	private String secret;
	private String notify_url;
	private String mchid;
	private String submchid;//子商户号
	
	public ASPayServiceImpl(Map<String,String> map){
		if(map.containsKey("payUrl")){
			this.payUrl = map.get("payUrl");
		}
		if(map.containsKey("secret")){
			this.secret = map.get("secret");
		}
		if(map.containsKey("notify_url")){
			this.notify_url = map.get("notify_url");
		}
		if(map.containsKey("mchid")){
			this.mchid = map.get("mchid");
		}
		if(map.containsKey("submchid")){
			this.submchid = map.get("submchid");
		}
	}
	
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		logger.info("[安盛支付]网银支付开始........");
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("[安盛支付]扫码支付开始........");
		
		Map<String,String> map = sealRequest(payEntity);
		logger.info("[安盛支付]扫码支付请求的参数:{}",map);
		String sign = generatorSign(map);
		
		map.put("sign", sign);
		
		String result = HttpUtils.doPost(payUrl, map);
		logger.info("[安盛支付]扫码支付返回结果:{}",result);
		
		if(StringUtils.isEmpty(result)){
			return PayResponse.error("[安盛支付]扫码支付 请求无响应..");
		}
		try{
			JSONObject obj = JSONObject.fromObject(result);
			if("1".equals(obj.getString("code"))){
				JSONObject data = obj.getJSONObject("data");
				if(data.containsKey("url")){
					return PayResponse.sm_qrcode(payEntity, data.getString("url"), "[安盛支付]扫码支付成功!");
				}
			}
			return PayResponse.error("[安盛支付]扫码支付出现错误:" + obj);
		}catch(Exception e){
			e.printStackTrace();
			logger.info("[安盛支付]扫码支付出现问题:{}",e.getMessage());
			return PayResponse.error("[安盛支付]扫码支付出现问题:" + e.getMessage());
		}
	}

	@Override
	public String callback(Map<String, String> data) {
		logger.info("[安盛支付]回调函数支付开始........");
		
		String sourceSign = data.get("sign");
		logger.info("[安盛支付]回调函数原签名:{}",sourceSign);
		
		String sign = generatorSign(data);
		if(sign.equalsIgnoreCase(sourceSign)){
			return "success";
		}
		return "fail";
	}
	
	private Map<String,String> sealRequest(PayEntity payEntity){
		Map<String,String> map = new HashMap<String,String>();
		map.put("mchid", mchid);
		map.put("submchid", submchid);//子商户号
		map.put("orderno", payEntity.getOrderNo());
		map.put("amount", String.valueOf(payEntity.getAmount()));
		map.put("waytype", payEntity.getPayCode());
		map.put("notifyurl", notify_url);
		
		return map;
	}
	
	//签名
	private String generatorSign(Map<String,String> map){
		try {
			
			Map<String,String> data = MapUtils.sortByKeys(map);
			StringBuilder strBuilder = new StringBuilder();
			for(Entry<String,String> entry:data.entrySet()){
				if(StringUtils.isEmpty(entry.getValue()) || "sign".equals(entry.getKey())){
					continue;
				}
				
				strBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
			}
			strBuilder.append("key=").append(secret);
			
			logger.info("[安盛支付]签名前的值:{}",strBuilder);
			
			String result = Rsaencrypt.sign(strBuilder.toString());
			logger.info("[安盛支付]签名加密后:{}",result);
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
