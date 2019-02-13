package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.jhz.util.MD5Utils;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.wtzf.util.HttpUtils;

import net.sf.json.JSONObject;

/**
 * 新免签支付
 * @author TX
 */
public class XMQZFPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(XMQZFPayServiceImpl.class);
	/**
	 * 商家编号
	 */
	private String uid;
	/**
	 * 回调函数路径
	 */
	private String notify_url;
	/**
	 * 支付请求路径
	 */
	private String pay_url;
	/**
	 * 密钥
	 */
	private String secret;
	
	public XMQZFPayServiceImpl(Map<String,String> map) {
		if(map.containsKey("uid")){
			this.uid = map.get("uid");
		}
		if(map.containsKey("notify_url")){
			this.notify_url = map.get("notify_url");
		}
		if(map.containsKey("pay_url")){
			this.pay_url = map.get("pay_url");
		}
		if(map.containsKey("secret")){
			this.secret = map.get("secret");
		}
	}
	
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("新免签支付开始扫码支付.........");
		Map<String,String> map = sealRequest(payEntity);
		String sign = generatorSign(map);
		map.put("sign", sign);
		logger.info("新免签支付请求参数:{}", map);
		
		String response = HttpUtils.doPostJson(pay_url, map);
		logger.info("新免签支付请求地址:{},请求地址:{}",response,pay_url);
		if(StringUtils.isEmpty(response)){
			logger.info("新免签支付请求第三方返回null");
			return PayResponse.error("新免签支付请求第三方返回null,请联系第三方支付");
		}
		
		JSONObject json = JSONObject.fromObject(response);
		if(json.containsKey("Code") && "1".equals(json.getString("Code"))){
			return PayResponse.sm_link(payEntity, json.getString("QRCodeLink"), "支付成功");
		}
		
		return PayResponse.error("新免签出现错误,错误提示为:"+response);
	}

	@Override
	public String callback(Map<String, String> data) {
		logger.info("新免签开始回调,参数为:{}",data);
		String sourceSign = data.get("sign");
		logger.info("免签回调函数签名:{}",sourceSign);
		String sign = generatorSign(data);
		
		if(sign.equals(sourceSign)){
			return "success";
		}
		return "fail";
	}
	
	private Map<String,String> sealRequest(PayEntity payEntity){
		logger.info("新免签支付开始组装参数..........");
		String money = new DecimalFormat("#.##").format(payEntity.getAmount());
		
		Map<String,String> map = new LinkedHashMap<String,String>();
		map.put("uid", uid);
		map.put("price", money);
		map.put("paytype", payEntity.getPayCode());
		map.put("notify_url", notify_url);
 		map.put("return_url", "1");
 		map.put("user_order_no", payEntity.getOrderNo());
 		logger.info("新免签支付组织参数完成:{}",map);
 		return map;
	}
	/**
	 * 签名
	 */
	private String generatorSign(Map<String,String> map){
		logger.info("新免签支付开始签名前参数值:{}",map);
		StringBuilder sb = new StringBuilder();
		for(Entry<String, String> entry : map.entrySet()){
			if("sign".equals(entry.getKey()) || StringUtils.isEmpty(entry.getValue())){
				continue;
			}
			sb.append(entry.getValue());
		}
		sb.append(secret);
		logger.info("新免签支付签名参数:{}",sb);
		String md5Value = MD5Utils.md5(sb.toString());
		logger.info("新免签支付签名之后结果:{}",md5Value);
		return md5Value;
	}
}
