package com.cn.tianxia.pay.impl;

import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.util.StringUtils;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.shzf.util.HttpGetClient;
import com.cn.tianxia.pay.shzf.util.HttpUtil;
import com.cn.tianxia.pay.shzf.util.MD5;

import net.sf.json.JSONObject;

/**
 * 速汇支付
 * @author tx001
 * @date 2018-07-03
 */
public class SHZFPayServiceImpl implements PayService{
	private final static Logger logger = LoggerFactory.getLogger(SHZFPayServiceImpl.class);
	
	private String payUrl ;//= "https://gateway.suhui.org/api/v1/order";//支付地址
	private String merchant_code ;//= "10051";//商户号
	private String md5Key ;//= "3bf0fb6b-f42a-327f-7329-f194e8cf5b42";//密钥
	private String notify_url ;//= "http://182.16.110.186:8080/XPJ/Notify/SHZFNotify.do";//异步通知地址
	
	public SHZFPayServiceImpl(Map<String, String> pmap) {
		JSONObject json = JSONObject.fromObject(pmap);
		this.payUrl = json.getString("payUrl");
		this.merchant_code = json.getString("merchant_code");
		this.md5Key = json.getString("md5Key");
		this.notify_url = json.getString("notify_url");
	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		String userName = payEntity.getUsername();
		double order_amount = payEntity.getAmount();//123.00;//金额
		String order_no = payEntity.getOrderNo();
		
		String responseStr = requestPayUrl(payEntity, true);
		JSONObject responseJson = JSONObject.fromObject(responseStr);
		logger.info("请求返回数据responseJson="+responseJson);
		String isSuccess = responseJson.getString("is_success");
		if(!StringUtils.isEmpty(isSuccess) && "TRUE".equalsIgnoreCase(isSuccess)) {
			String url = responseJson.getString("url");
			logger.info("获取下单地址成功url="+responseJson);
			return PayUtil.returnWYPayJson("success", "link", url, null, null);
		}
		
		logger.error("获取下单地址错误:"+responseJson.getString("msg"));
		return PayUtil.returnPayJson("error", "2", responseJson.getString("msg"), userName, order_amount, order_no,"");
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		
		String userName = payEntity.getUsername();
		double order_amount = payEntity.getAmount();//123.00;//金额
		String order_no = payEntity.getOrderNo();
		
		String responseStr = requestPayUrl(payEntity, false);
		JSONObject responseJson = JSONObject.fromObject(responseStr);
		logger.info("请求返回数据responseJson="+responseJson);
		
		String isSuccess = responseJson.getString("is_success");
		if(!StringUtils.isEmpty(isSuccess) && "TRUE".equalsIgnoreCase(isSuccess)) {
			String url = responseJson.getString("url");
			logger.info("获取下单地址成功url="+responseJson);
			return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, order_amount, order_no, url);
		}
		logger.error("获取下单地址错误:"+responseJson.getString("msg"));
		return PayUtil.returnPayJson("error", "2", responseJson.getString("msg"), userName, order_amount, order_no,"");
	}
	
	//请求支付地址
	private String requestPayUrl(PayEntity payEntity,boolean isWy) {
		String order_no = payEntity.getOrderNo();//new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());//订单号
		double order_amount = payEntity.getAmount();//123.00;//金额
		String pay_type = null;//通道类型
		if(isWy) {
			pay_type="WY";
		}else {
			pay_type = payEntity.getPayCode();
		}
		String bank_code = payEntity.getPayCode();//银行编码
		String order_time = String.valueOf(Calendar.getInstance().getTimeInMillis() / 1000);//订单时间戳
		String customer_ip = payEntity.getIp();//消费者 IP
		String return_url = payEntity.getRefererUrl();//"http://www.baidu.com";
		
		Map<String, String> params = new TreeMap<>();
		params.put("merchant_code", this.merchant_code );
		params.put("order_no", order_no);
		params.put("order_amount", String.valueOf(order_amount));
		params.put("pay_type", pay_type);
		params.put("bank_code", bank_code);
		params.put("order_time", order_time);
		params.put("customer_ip", customer_ip);
		params.put("notify_url", this.notify_url);
		params.put("return_url", return_url);
		params.put("sign",  genSign(params));
		
		String responseStr = HttpGetClient.send(this.payUrl, params);
		return responseStr;
	}

	//生成签名
	private String genSign(Map<String, String> params) {
		StringBuilder builder = new StringBuilder();
		for(String key:params.keySet()) {
			String value = params.get(key);
			if(StringUtils.isEmpty(value) || "sign".equalsIgnoreCase(key)) {
				continue;
			}
			builder.append(key+"="+value+"&");
		}
		builder.append("key="+this.md5Key);
		logger.info("生成签名串:"+builder);
		String sign = MD5.MD5(builder.toString());
		logger.info("生成签名sign="+sign);
		return sign;
	}

	public static void main(String[] args) throws Exception {
		/*
		 * {"merchant_code":10051,
		 * "order_no":"SHZFbl1201807041616001616009730",
		 * "order_amount":"123.00",
		 * "order_time":1530692161,
		 * "return_params":"",
		 * "trade_no":"118070416160159783",
		 * "trade_time":1530692416,
		 * "trade_status":"success",
		 * "notify_type":"back_notify",
		 * "sign":"9c111570f915800cf802548f2fb400c7"} 
		 */
		Map< String, String> params = new TreeMap<>();
		params.put("merchant_code","10051");
		params.put("order_no","SHZFbl1201807041616001616009730" );
		params.put("order_amount","123.00");
		params.put("order_time","1530692161");
		params.put("return_params","");
		params.put("trade_no","118070416160159783");
		params.put("trade_time","1530692416");
		params.put("trade_status","success");
		params.put("notify_type","back_notify");
		params.put("sign","9c111570f915800cf802548f2fb400c7");
		
		
		String rest = HttpUtil.httpPostWithJSON("http://182.16.110.186:8080/XPJ/Notify/SHZFNotify.do", params);
		System.out.println(rest);
		
		/*SHZFPayServiceImpl  service = new SHZFPayServiceImpl();
		service.md5Key = "3bf0fb6b-f42a-327f-7329-f194e8cf5b42";
		
		String rstr = service.callback(params);
		System.out.println(rstr);*/
	}
	
	
	public SHZFPayServiceImpl() {
		
	}

	/**
	 * 回调验签
	 * @param infoMap
	 * @return
	 */
	@Override
	public String callback(Map<String, String> params) {
		
		String remoteSign = params.get("sign");
		String localSign = genSign(params);
		if(remoteSign.equals(localSign)) {
			logger.info("验签成功");
			return "success";
		}
		logger.info("验签失败");
		return "success";
	}
}






























