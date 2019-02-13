package com.cn.tianxia.pay.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.MapUtils;
import com.cn.tianxia.pay.wtzf.util.HttpUtils;

import net.sf.json.JSONObject;

/**
 * 新鑫支付
 * @author TX
 */
public class XXZFPayServiceImpl implements PayService{
	private final static Logger logger = LoggerFactory.getLogger(XXZFPayServiceImpl.class);
	private String secret;
	private String notify_url;
	private String seller_id;
	private String payUrl;
	
	public XXZFPayServiceImpl(Map<String,String> map){
		if(map.containsKey("secret")){
			this.secret = map.get("secret");
		}
		if(map.containsKey("notify_url")){
			this.notify_url = map.get("notify_url");
		}
		if(map.containsKey("seller_id")){
			this.seller_id = map.get("seller_id");
		}
		if(map.containsKey("payUrl")){
			this.payUrl = map.get("payUrl");
		}
	}
	
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("[新鑫支付] 开始扫码支付了..........................");
		JSONObject json = parameter(payEntity);
		
		logger.info("新鑫支付 扫码支付参数:{}",json);//请求参数
		
		String result = HttpUtils.doPostJson(payUrl, json);
		logger.info("新鑫支付 扫码支付返回结果:{}",result);
		
		if(result.isEmpty()){
			PayResponse.error("新鑫支付 扫码支付 返回值为空!");
		}
		
		JSONObject object = JSONObject.fromObject(result);
		if("ok".equals(object.getString("state"))){ //ok 表示成功
			JSONObject dataJson = object.getJSONObject("data");
			return PayResponse.sm_link(payEntity, dataJson.getString("url"), "支付成功");
		}
		
		return PayResponse.error("新鑫支付 扫码支付出现错误,"+result);
	}

	@Override
	public String callback(Map<String, String> data) {
		
		String sourceSign = data.get("sign");
		
		return null;
	}

	private JSONObject parameter(PayEntity payEntity){
		logger.info("[新鑫支付]扫码支付 参数");
		JSONObject json = new JSONObject();
		json.put("key", "new");
		json.put("seller_id", seller_id);
		json.put("notify_url", notify_url);
		json.put("trade_no", payEntity.getOrderNo());//订单号
		json.put("money", payEntity.getAmount());
		json.put("subject", "TOP-UP");
		json.put("pay_type", payEntity.getPayCode());
		json.put("get_qr", true);
		json.put("sign", secret);
		logger.info("[新鑫支付]扫码支付 参数:{}",json);
		return json;
	}
}
