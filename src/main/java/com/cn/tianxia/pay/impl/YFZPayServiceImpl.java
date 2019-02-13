package com.cn.tianxia.pay.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;
import com.cn.tianxia.pay.ys.util.DateUtil;
import com.smartpay.ops.client.StringUtil;

import net.sf.json.JSONObject;

/**
 * 永付支付 YFZ
 * @author TX
 */

public class YFZPayServiceImpl implements PayService{
	private final static Logger logger = LoggerFactory.getLogger(YFZPayServiceImpl.class);
	private String payUrl;//支付路径
	private String pay_memberid;//商户名称
	private String secret;//密钥
	private String notify_url;//回调路径
	
	public YFZPayServiceImpl(Map<String, String> map){
		if(map.containsKey("pay_memberid")){
			this.pay_memberid = map.get("pay_memberid");
		}
		if(map.containsKey("payUrl")){
			this.payUrl = map.get("payUrl");
		}
		if(map.containsKey("notify_url")){
			this.notify_url = map.get("notify_url");
		}
		if(map.containsKey("secret")){
			this.secret = map.get("secret");
		}
	}
	
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		return null;
	}

	/*
	 * 扫码支付
	 */
	@Override
	public JSONObject smPay(PayEntity payEntity) {
		try{
			Map<String,String> map = sealRequest(payEntity,2);//获取参数
			
			String sign = generatorSign(map);//签名成功
			map.put("pay_md5sign", sign);//签名
			logger.info("[永付支付]全部请求参数名: {}", map);
			
			String result = HttpUtils.toPostForm(map, payUrl);
			logger.info("[永付支付]返回结果:{}" , result);
			
			if(StringUtils.isEmpty(result)){
				return PayResponse.error("[永付支付]下单失败,发起HTTP请求无响应结果");
			}
		
			JSONObject json = JSONObject.fromObject(result);
			if(json.containsKey("code") && "0".equals(json.getString("code"))){//请求成功
				return PayResponse.sm_qrcode(payEntity, json.getString("qrcode"), "[永利支付]扫码下单成功");//支付成功跳转
			}
			
			return PayResponse.error("[永付支付]下单支付出现错误,永付支付返回结果提示:" + result);
		}catch(Exception e){
			e.printStackTrace();
			logger.info("[永付支付]下单支付异常:{}",e.getMessage());
			return PayResponse.error( "[永付支付]下单支付出现错误,错误提示:"+e.getMessage());
		}
	}

	@Override
	public String callback(Map<String, String> data) {
		try{
			String sourceSign = data.get("sign");
			logger.info("[YFZ]永付支付验签原签名串:{}",sourceSign);
			Map<String,String> map = new HashMap<String,String>();
			
			for(Entry<String,String> entry : data.entrySet()){
				map.put(entry.getKey(), entry.getValue());
			}
			String sign = generatorSign(map);//签名
			logger.info("[YFZ]永付支付加密之后签名:{}",sign);
			if(sourceSign.equalsIgnoreCase(sign)) return "success";
		}catch(Exception e){
			e.printStackTrace();
			logger.error("[YFZ]永付支付回调验签异常:{}",e.getMessage());
		}
		
		return "fail";
	}

	/*
	 * type=2 扫码
	 * type=1网银
	 */
	private Map<String,String> sealRequest(PayEntity payEntity,int type){
		
		Map<String,String> map = new LinkedHashMap<String,String>();
		map.put("pay_memberid", pay_memberid);
		map.put("pay_orderid", payEntity.getOrderNo());//订单号
		map.put("pay_amount", String.valueOf(payEntity.getAmount()));//金额
		map.put("pay_applydate", DateUtil.getCurrentDate("YYYYMMDDHHMMSS"));//交易日期
		if(type == 1){//网银支付
			map.put("pay_channelCode", "BANK");
			map.put("pay_bankcode", payEntity.getPayCode());//银行编号
		}else{//支付宝扫码
			map.put("pay_channelCode", "ALIPAY");
			map.put("pay_bankcode", "QRCODE");//如果是 QQ,支付宝  = QRCODE
			if(StringUtils.isNotEmpty(payEntity.getMobile())){
				map.put("isMobile", "true");
			}else{
				map.put("isMobile", "false");
			}
		}
		map.put("pay_notifyurl", this.notify_url);
		logger.info("[永付支付]请求参数名:{} ",JSONObject.fromObject(map));
		return map;
	}
	
	private String generatorSign(Map<String,String> map) throws Exception{
		logger.info("[永付支付] 开始签名...");
		Map<String,String> mapValue = MapUtils.sortByKeys(map);//排序
		Set<Entry<String, String>> set = mapValue.entrySet();
		StringBuilder strBl = new StringBuilder();
		for(Entry<String, String> entry : set){
			if(entry.getKey().equals("isMobile")){
				continue;
			}
			if(entry.getKey().equals("pay_bankcode")){
				continue;
			}
			if(StringUtil.isEmpty(entry.getValue()) || "pay_md5sign".equals(entry.getKey())){
				continue;
			}
			strBl.append(entry.getKey()).append("^").append(entry.getValue()).append("&");
		}
		strBl.append("key=").append(secret);//密钥
		
		logger.info("[永付支付]签名参数:{} ",strBl);
		String md5Value = MD5Utils.md5toUpCase_32Bit(strBl.toString());//md5签名结果
		logger.info("[永付支付]签名结果:{} ",md5Value);
		return md5Value;
	}
}
