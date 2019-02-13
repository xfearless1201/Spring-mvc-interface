package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.util.StringUtils;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.glzf.util.HttpUtil;
import com.cn.tianxia.pay.glzf.util.Md5Util;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * 咕啦支付
 * 
 * @author hb
 * @date 2018-06-07
 */
public class GLZFPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(GLZFPayServiceImpl.class);

	/** 支付地址 */
	private String payUrl ;//= "https://open.goodluckchina.net/open/pay/buildPayCode";
	/** appid */
	private String appid ;//= "1fbcf2d407ed45b1937ee12de4f5323c";
	/** custNo */
	private String custNo ;//= "gl00034328";
	/** 商户密钥 */
	private String apiKey ;//= "1fbcf2d407ed45b1937ee12de4f5323c";;
	/** 回调地址 */
	private String callBackUrl ;//= "http://www.baidu.com";;
	/** 返回类型 */
	private String returnType ;//= "1";
	
	public GLZFPayServiceImpl(Map<String, String> pmap) {
		if(pmap != null) {
			if(pmap.containsKey("payUrl")) {
				this.payUrl = pmap.get("payUrl");
			}
			if(pmap.containsKey("appid")) {
				this.appid = pmap.get("appid");
			}
			if(pmap.containsKey("custNo")) {
				this.custNo = pmap.get("custNo");
			}
			if(pmap.containsKey("apiKey")) {
				this.apiKey = pmap.get("apiKey");
			}
			if(pmap.containsKey("callBackUrl")) {
				this.callBackUrl = pmap.get("callBackUrl");
			}
			if(pmap.containsKey("returnType")) {
				this.returnType = pmap.get("returnType");
			}
		}
	}

	public GLZFPayServiceImpl() {

	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {

		double amount = payEntity.getAmount();
		String mchOrderNo = payEntity.getOrderNo();
		String returnUrl = payEntity.getRefererUrl();
		String userName = payEntity.getUsername();
		String isMobile = payEntity.getMobile();
		String model = payEntity.getPayCode();

		Map<String, String> params = new TreeMap<>();
		params.put("appid", this.appid);
		params.put("custNo", this.custNo);
		params.put("model", model);
		params.put("money", String.valueOf(amount));
		params.put("attach", "");
		params.put("callBackUrl", this.callBackUrl);
		params.put("mchOrderNo" , mchOrderNo);
		params.put("returnType", this.returnType);
		params.put("returnUrl", returnUrl);
		params.put("sign", generateSign(params));

		String returnStr = HttpUtil.RequestForm(this.payUrl, params);
		logger.info("返回字符串returnStr="+returnStr);
		if(isJson(returnStr)) {
			JSONObject responseJson = JSONObject.fromObject(returnStr);
			logger.info("请求返回responseJson="+responseJson);
			String reqUrl = responseJson.getString("pay_url");
			if("1".equals(responseJson.getString("code"))) {
				if(StringUtils.isEmpty(isMobile)) {//pc端
					return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, mchOrderNo, reqUrl);
				}
				//手机端
				//return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, mchOrderNo, reqUrl);
			}
		}
		return PayUtil.returnPayJson("error", "2", "支付接口请求异常", userName, amount, mchOrderNo, "");
	}

	//生成签名
	private String generateSign(Map<String, String> params) {
		
		StringBuilder sb = new StringBuilder();
		for(String key : params.keySet()) {
			String value = params.get(key);
			if(StringUtils.isEmpty(value)) {
				continue;
			}
			sb.append(key+"="+value+"&");
		}
		String signStr = sb.substring(0, sb.length() - 1)+this.apiKey;
		
		return Md5Util.md5(signStr, "UTF-8");
	}

	/**
	 * 判断是否是json结构
	 */
	public static boolean isJson(String value) {
		try {
			JSONObject.fromObject(value);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	 public static void main(String[] args) throws UnsupportedEncodingException {
			Map<String, String> params = new HashMap<>();
			params.put("attach", null);
			params.put("cust_no", "gl00034328");
			params.put("mch_order_no", "GLZFyhh201806081243441243442409");
			params.put("money", "2000.00");
			params.put("order_id", "a1efe0c82c5744a7a8892e6f9240863e");
			params.put("pay_channel", "01");
			params.put("pay_status", "success");
			params.put("pay_time", "20180608124516");
			params.put("plat_order_no", "");
			params.put("return_code", "SUCCESS");
			params.put("return_msg", new String("支付成功".getBytes(), "ISO8859-1"));
			params.put("sign", "2d39c7702d4d08f2349c359938c284d4");
			params.put("trade_no", "201806081243451017298364");
	    	
	    	String str = HttpUtil.RequestForm("http://localhost:8080/JJF/Notify/GlzfNotify.do", params);
	    	System.out.println("str ="+str);
		}
	 
	@Override
	public String callback(Map<String, String> infoMap) {
		StringBuilder sb = new StringBuilder();
		for(String key : infoMap.keySet()) {
			String value = infoMap.get(key);
			if(StringUtils.isEmpty(value) || "sign".equalsIgnoreCase(key) || "null".equals(value)) {
				continue;
			}
			sb.append(key+"="+value+"&");
		}
		String signStr = sb.substring(0, sb.length() - 1)+this.apiKey;
		
		String sign2 = infoMap.get("sign");
		String sign1 = Md5Util.md5(signStr, "UTF-8");
		if(sign2.equalsIgnoreCase(sign2)) {
			logger.info("验签成功");
			return "success";
		}
		logger.info("验签失败");
		return "fail";
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
}
