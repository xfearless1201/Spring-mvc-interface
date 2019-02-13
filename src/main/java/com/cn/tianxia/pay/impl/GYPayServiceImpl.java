package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.entity.QueryOrderVO;
import com.cn.tianxia.pay.po.OrderResponse;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MapUtils;
import com.cn.tianxia.util.Https;
import com.cn.tianxia.util.MD5Encoder;

import net.sf.json.JSONObject;

/**
 * 共赢支付   支付宝
 * @author TX
 */
public class GYPayServiceImpl implements PayService{
	private final static Logger logger = LoggerFactory.getLogger(GYPayServiceImpl.class);
	
	private String uid;//商户编号
	private String notify_url;
	private String secret;
	private String payUrl;
	private String queryUrl;
	public GYPayServiceImpl(Map<String,String> map){
		if(map.containsKey("uid")){
			this.uid = map.get("uid");
		}
		if(map.containsKey("notify_url")){
			this.notify_url = map.get("notify_url");
		}
		if(map.containsKey("secret")){
			this.secret = map.get("secret");
		}
		if(map.containsKey("payUrl")){
			this.payUrl = map.get("payUrl");
		}
		if(map.containsKey("queryUrl")){
			this.queryUrl = map.get("queryUrl");
		}
	}
	
	/**
	 * 银联支付
	 */
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		
		return null;
	}

	/**
	 * 扫码支付
	 */
	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("[共赢支付] 开始扫码支付.............");
		Map<String,String> map = parameter(payEntity);
		String sign = generatorSign(map);
		map.put("sign", sign);
		logger.info("共赢支付扫码支付参数:{}",map);
		String result = "";
		try {
			//共赢支付第一种支付 form 表单提交
			//String response = HttpUtils.generatorForm(map, payUrl);
			//logger.info("共赢支付表单提交参数:{}",response);
			//return PayResponse.sm_form(payEntity, response, "下单成功!");
			
			//共赢支付 第二种支付，获取路径跳转
			result = Https.doPost(payUrl, JSONObject.fromObject(map));
			logger.info("共赢支付扫码支付返回值:{}",result);
			
			if(result.isEmpty()){
				PayResponse.error("共赢支付扫码支付 返回值为空!");
			}
			
			JSONObject json = JSONObject.fromObject(result);
		
			if(json.containsKey("qrcode")){
				String qrcode = json.getString("qrcode");
				if(StringUtils.isBlank(payEntity.getMobile())){
					return PayResponse.sm_qrcode(payEntity, qrcode, "扫码支付成功");
				}else{
					return PayResponse.sm_link(payEntity, qrcode, "手机支付成功");
				}
			}
			
			return PayResponse.error("共赢支付失败,失败原因:"+result);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("共赢支付失败,出现问题,问题提示:{}",result);
			return PayResponse.error("共赢支付失败,出现问题,问题提示:"+result);
		}
	}

	@Override
	public String callback(Map<String, String> data) {
		logger.info("共赢支付 开始回调函数签名");
		String sourceKey = data.get("sign");
		logger.info("共赢支付 原 sourceKey = {}",sourceKey);
		
		String sign = generatorSign(data);
		if(sourceKey.equals(sign)){
			return "success";
		}
		
		return "fail";
	}

	/**
	 * 查询接口
	 * @param vo
	 * @return
	 * 只能 get 请求 ，不能post 请求
	 */
	public JSONObject query(QueryOrderVO vo){
		logger.info("共赢支付 查询接口开始");
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("uid", uid);
		map.put("order_id", vo.getOrderNo());//商家编号
		String sign = generatorSign(map);
		
		StringBuilder sb = new StringBuilder();
		sb.append(queryUrl).append("?").append("uid=").append(uid)
		.append("&order_id=").append(vo.getOrderNo())
		.append("&sign=").append(sign);
		
		String result = Https.doGet(sb.toString());
		
		if(result.isEmpty()){
			return OrderResponse.error("[共赢支付]查询订单状态 返回值为空!");
		}
		
		JSONObject object = JSONObject.fromObject(result);
		if(object.containsKey("status") && "3".equals(object.getString("status"))){
			return OrderResponse.success("查询支付成功", result);
		}
		
		return OrderResponse.error("[共赢支付]查询订单 支付失败，订单号为:"+vo.getOrderNo());
	}
	
	private Map<String,String> parameter(PayEntity payEntity){
		logger.info("[共赢支付] 扫码支付开始组装参数....");
		Map<String,String> map = new HashMap<String,String>();
		String amount = new DecimalFormat("#.##").format(payEntity.getAmount());
		map.put("uid", uid);
		map.put("amount", amount);
		map.put("pay_type", payEntity.getPayCode());///0：支付宝；1：微信支付;
		map.put("order_id", payEntity.getOrderNo());
		map.put("notify_url", notify_url);
		logger.info("[共赢支付] 扫码支付 参数值:{}", map);
		return map;
	}
	
	//签名
	private String generatorSign(Map<String,String> map){
		logger.info("共赢支付 扫码支付开始签名");
		try {
			Map<String,String> sortMap = MapUtils.sortByKeys(map);
			StringBuilder sb = new StringBuilder();
			
			for(Entry<String,String> entry:sortMap.entrySet()){
				if("sign".equals(entry.getKey()) || "".equals(entry.getValue())){
					continue;
				}
				sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
			}
			
			sb.append(secret);
			logger.info("共赢支付 扫码支付 签名参数:{}",sb);
			String md5Value = MD5Encoder.encode(sb.toString());//md5 小写
			logger.info("共赢支付 扫码支付加密后:{}",md5Value);//加密后
			
			return md5Value;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("共赢支付扫码支付 签名报错!");
		}
		
		return "";
	}
}
