package com.cn.tianxia.pay.impl;

import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.util.StringUtils;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;

/**
 * FF91PAY 支付
 * @author TX
 */
public class FFPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(FFPayServiceImpl.class);
	//版本号
	private String version;
	//商户号
	private String partner;
	//支付地址
	private String payUrl;
	//密钥
	private String secret;
	//回调路径
	private String notifyUrl;
	
	public FFPayServiceImpl(Map<String,String> map){
		if(map.containsKey("version")){
			this.version = map.get("version");
		}
		if(map.containsKey("partner")){
			this.partner = map.get("partner");
		}
		if(map.containsKey("payUrl")){
			this.payUrl = map.get("payUrl");
		}
		if(map.containsKey("secret")){
			this.secret = map.get("secret");
		}
		if(map.containsKey("notifyUrl")){
			this.notifyUrl = map.get("notifyUrl");
		}
	}
	
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("FF91Pay 支付扫码开始........");
		try {
			Map<String,String> map = sealRequest(payEntity);
			String sign = generatorSign(map);
			map.put("sign", sign);//签名
			
			logger.info("91支付请求参数:{}",map);
			
			String formStr = HttpUtils.generatorForm(map, payUrl);
			logger.info("FF91 支付生产的form 表单:{}",formStr);
			
			//return PayResponse.sm_form(payEntity, formStr, "支付成功!");
			
			String response = HttpUtils.toPostForm(map, payUrl);
			logger.info("91支付请求返回结果:{}",response);
			if(response.isEmpty()){
				return PayResponse.error("91Pay请求结果为null, 无响应");
			}
			
			return PayResponse.sm_form(payEntity, response, "支付成功！");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String callback(Map<String, String> data) {
		logger.info("FF91Pay 支付回调开始.......:{}",data);
		String sourceSign = data.get("sign");
		logger.info("FF91Pay 原来签名:{}",sourceSign);
		
		try {
			String sign = generatorSign(data);
			if(sign.equals(sourceSign)){
				return "success";
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return "fail";
	}
	
	private Map<String,String> sealRequest(PayEntity payEntity){
		logger.info("91PAY支付组装参数开始....");
		String amount = new DecimalFormat("#.##").format(payEntity.getAmount());
		
		Map<String,String> map = new LinkedHashMap<String,String>();//排序进行签名
		map.put("version", version);
		map.put("partner", partner);
		map.put("banktype", payEntity.getPayCode());
		map.put("paymoney", amount);
		map.put("ordernumber", payEntity.getOrderNo());
		map.put("callbackurl", notifyUrl);
		logger.info("91PAY支付组装参数完成,结果:{}",map);
		return map;
	}
	
	/**
	 * 支付请求加密 排序位置:version={0}&partner={2}&banktype={3}&paymoney={4}&ordernumber={5}&callbackurl={7}key
	 * 回调请求加密排序位置 :partner={1}&ordernumber={2}&orderstatus={3}&paymoney={4}&sysnumber={4}key
	 * @param map
	 * @param type
	 * @throws NoSuchAlgorithmException 
	 */
	private String generatorSign(Map<String,String> map) throws NoSuchAlgorithmException{
		logger.info("FF91支付 签名开始.............");
		StringBuilder sb = new StringBuilder();
		for(Entry<String,String> entry: map.entrySet()){
			
			if("sign".equals(entry.getKey()) || StringUtils.isEmpty(entry.getValue())){
				continue;
			}
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		
		String result = sb.substring(0,sb.length()-1);
		result += secret;
		logger.info("FF91支付 签名参数:{}",result);
		String md5Value = MD5Utils.md5toUpCase_32Bit(result);
		logger.info("91支付 签名结果:{}",md5Value);
		return md5Value;
	}
}
