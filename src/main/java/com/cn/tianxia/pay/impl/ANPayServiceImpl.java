package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.SSLClient;
import com.jnewsdk.util.Base64;
import com.jnewsdk.util.SignUtil;

import net.sf.json.JSONObject;

public class ANPayServiceImpl implements PayService {
	
	// 需内容做Base64加密
	private static String[] base64Keys = { "subject", "body", "remark" };
	// 需内容做Base64加密,并所有子域采用json数据格式
	private static String[] base64JsonKeys = { "customerInfo", "accResv", "riskRateInfo", "billQueryInfo",
			"billDetailInfo" };
	
	private String version;

	private String txnType;

	private String txnSubType;

	private String bizType;

	private String accessType;

	private String accessMode;

	private String merId;

	private String currency;

	private String payType;

	private String backUrl;

	private String url;

	private String md5_key;

	private String signMethod;
	
	private String queryUrl;
	
	private String ylUrl;

	public ANPayServiceImpl(Map<String, String> pmap) {
		net.sf.json.JSONObject jo = new net.sf.json.JSONObject().fromObject(pmap);
		if (null != pmap) {
			version = jo.get("version").toString();
			txnType = jo.get("txnType").toString();
			txnSubType = jo.get("txnSubType").toString();
			bizType = jo.get("bizType").toString();
			accessType = jo.get("accessType").toString();
			accessMode = jo.get("accessMode").toString();
			merId = jo.get("merId").toString();
			currency = jo.get("currency").toString();
			payType = jo.get("payType").toString();
			backUrl = jo.get("backUrl").toString();
			url = jo.getString("url");
			md5_key = jo.getString("md5_key");
			signMethod = jo.getString("signMethod");
			queryUrl = jo.getString("queryUrl");
			ylUrl=jo.getString("ylUrl");
		}
	}


	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String userName = payEntity.getUsername();
		String pay_url = payEntity.getPayUrl();
		String mobile = payEntity.getMobile();
		String ip = payEntity.getIp();
		String topay = payEntity.getTopay();

		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("merOrderId", payEntity.getOrderNo());// 订单号
		scanMap.put("txnTime", DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
		Double txnAmt = amount * 100;
		DecimalFormat df = new DecimalFormat("#########");
		String amt = df.format(txnAmt);

		scanMap.put("txnAmt", amt);
		scanMap.put("bankId", payEntity.getPayCode());
		scanMap.put("frontUrl", payEntity.getRefererUrl());

		String html = bankPay(scanMap);
		return PayUtil.returnWYPayJson("success", "form", html, pay_url, "");
	}

	private String bankPay(Map<String, String> scanMap) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("version", version);
		paramMap.put("txnType", txnType);
		paramMap.put("txnSubType", txnSubType);
		paramMap.put("bizType", bizType);
		paramMap.put("accessType", accessType);
		paramMap.put("accessMode", accessMode);
		paramMap.put("merId", merId);
		paramMap.put("merOrderId", scanMap.get("merOrderId"));
		paramMap.put("txnTime", scanMap.get("txnTime"));
		paramMap.put("txnAmt", scanMap.get("txnAmt"));
		paramMap.put("currency", currency);
		paramMap.put("payType", payType);//网银编码0201
		paramMap.put("frontUrl", scanMap.get("frontUrl"));
		paramMap.put("backUrl", backUrl);
		paramMap.put("bankId", scanMap.get("bankId"));
		paramMap.put("signMethod", "MD5");

		Set<String> removeKey = new HashSet<String>();
		removeKey.add("signMethod");
		removeKey.add("signature");
		String signMsg = SignUtil.getSignMsg(paramMap, removeKey);
		String signature = SignUtil.sign(signMethod, signMsg, md5_key, "UTF-8");
		System.out.println("signature:" + signature);
		paramMap.put("signature", signature);
		converData(paramMap);

		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ url + "\">";
		for (String key : paramMap.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramMap.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";

		System.out.println("爱农表单:" + FormString);
		return FormString;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String userName = payEntity.getUsername();
		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("merOrderId", payEntity.getOrderNo());// 订单号
		scanMap.put("txnTime", DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
		String txnAmt = String.valueOf(payEntity.getAmount() * 100);
		scanMap.put("txnAmt", txnAmt.substring(0, txnAmt.indexOf(".")));
		scanMap.put("payCode", payEntity.getPayCode());
		scanMap.put("frontUrl", payEntity.getRefererUrl());

		String html = scanPay(scanMap);
		return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no, html);
	}

	private String scanPay(Map<String, String> scanMap) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("version", version);
		paramMap.put("txnType", txnType);
		paramMap.put("payType", scanMap.get("payCode"));//编码0203
		paramMap.put("txnSubType", "00");
		paramMap.put("bizType", bizType);
		paramMap.put("accessType", accessType);
		paramMap.put("accessMode", accessMode);
		paramMap.put("merId", merId);
		paramMap.put("merOrderId", scanMap.get("merOrderId"));
		paramMap.put("txnTime", scanMap.get("txnTime"));
		paramMap.put("txnAmt", scanMap.get("txnAmt"));
		paramMap.put("currency", currency);
		
		paramMap.put("frontUrl",scanMap.get("frontUrl"));
		paramMap.put("backUrl", backUrl);
		paramMap.put("signMethod", "MD5");

		Set<String> removeKey = new HashSet<String>();
		removeKey.add("signMethod");
		removeKey.add("signature");
		String signMsg = SignUtil.getSignMsg(paramMap, removeKey);
		String signature = SignUtil.sign(signMethod, signMsg, md5_key, "UTF-8");
		System.out.println("signature:" + signature);
		paramMap.put("signature", signature);
		converData(paramMap);

		String FormString = "<body onLoad=\"document.actform.submit()\"><form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ url + "\">";
		for (String key : paramMap.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramMap.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";

		System.out.println("爱农快捷表单:" + FormString);
		
		String  form="";
		try {
			form = URLEncoder.encode(FormString, "utf-8");
		} catch (Exception e) {
			System.out.println("URL编码异常！");
			e.printStackTrace();
		}
		
		String ss=ylUrl+form;
		System.out.println("http:"+ss);
		return ss;
		
		
	}

	public static void yinlian() {
		// 消息版本号
		String version = "1.0.0";
		// 交易类型
		String txnType = "01";
		// 交易子类型
		String txnSubType = "00"; // 区分直连 或者收银台
		// 产品类型
		String bizType = "000000";
		// 接入类型
		String accessType = "0";
		// 接入方式
		String accessMode = "01";
		// 商户号
		String merId = "929040095023522";
		// 商户订单号
		String merOrderId = "TX" + System.currentTimeMillis();
		// 订单发送时间
		String txnTime = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
		// 交易金额(分)
		String txnAmt = "1";
		// 交易金额(分)
		String currency = "CNY";
		// 支付方式
		String payType = "0203";
		// (可选)前台通知地址
		// String frontUrl =
		// "http://192.168.11.212:8780/payTest/CallBackServlet";
		// (可选)前台通知地址
		String backUrl = "http://192.168.11.212:8780/payTest/CallBackServlet";
		// (可选)前台通知地址
		// String bankId = "03080000";
		// (可选)商品标题
		String subject = "测试商品标题";
		// (可选)商品描述
		String body = "测试商品描述";
		// (可选)商品描述
		String merResv1 = "";

		// String dcType = "1";
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("version", version);
		paramMap.put("txnType", txnType);
		paramMap.put("txnSubType", txnSubType);
		paramMap.put("bizType", bizType);
		paramMap.put("accessType", accessType);
		paramMap.put("accessMode", accessMode);
		paramMap.put("merId", merId);
		paramMap.put("merOrderId", merOrderId);
		paramMap.put("txnTime", txnTime);
		paramMap.put("txnAmt", txnAmt);
		paramMap.put("currency", currency);
		paramMap.put("payType", payType);
		paramMap.put("backUrl", backUrl);
		// paramMap.put("frontUrl", frontUrl);
		// paramMap.put("bankId", bankId);
		// paramMap.put("subject", subject);
		// paramMap.put("body", body);
		// paramMap.put("merResv1", merResv1);
		// paramMap.put("dcType", dcType);
		paramMap.put("signMethod", "MD5");

		String url = "http://gpay.chinagpay.com/bas/FrontTrans";
		Set<String> removeKey = new HashSet<String>();
		removeKey.add("signMethod");
		removeKey.add("signature");
		String signMsg = SignUtil.getSignMsg(paramMap, removeKey);
		String signMethod = "MD5";
		String md5_key = "WbVz375J4sGwFaUfwsB33KHgPYez887g";
		String signature = SignUtil.sign(signMethod, signMsg, md5_key, "UTF-8");
		System.out.println("signature:" + signature);
		paramMap.put("signature", signature);
		converData(paramMap);

		// String rusult = doPost(url, paramMap, "UTF-8");
		// System.out.println(rusult);
		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ url + "\">";
		for (String key : paramMap.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramMap.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";

		System.out.println("爱农表单:" + FormString);
	}

	public static void pay() {
		// 消息版本号
		String version = "1.0.0";
		// 交易类型
		String txnType = "01";
		// 交易子类型
		String txnSubType = "01"; // 区分直连 或者收银台
		// 产品类型
		String bizType = "000000";
		// 接入类型
		String accessType = "0";
		// 接入方式
		String accessMode = "01";
		// 商户号
		String merId = "929040095023522";
		// 商户订单号
		String merOrderId = "TX" + System.currentTimeMillis();
		// 订单发送时间
		String txnTime = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
		// 交易金额(分)
		String txnAmt = "1";
		// 交易金额(分)
		String currency = "CNY";
		// 支付方式
		String payType = "0201";
		// (可选)前台通知地址
		String frontUrl = "http://192.168.11.212:8780/payTest/CallBackServlet";
		// (可选)前台通知地址
		String backUrl = "http://192.168.11.212:8780/payTest/CallBackServlet";
		// (可选)前台通知地址
		String bankId = "03080000";
		// (可选)商品标题
		String subject = "测试商品标题";
		// (可选)商品描述
		String body = "测试商品描述";
		// (可选)商品描述
		String merResv1 = "";

		// String dcType = "1";
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("version", version);
		paramMap.put("txnType", txnType);
		paramMap.put("txnSubType", txnSubType);
		paramMap.put("bizType", bizType);
		paramMap.put("accessType", accessType);
		paramMap.put("accessMode", accessMode);
		paramMap.put("merId", merId);
		paramMap.put("merOrderId", merOrderId);
		paramMap.put("txnTime", txnTime);
		paramMap.put("txnAmt", txnAmt);
		paramMap.put("currency", currency);
		paramMap.put("payType", payType);
		paramMap.put("frontUrl", frontUrl);
		paramMap.put("backUrl", backUrl);
		paramMap.put("bankId", bankId);
		paramMap.put("subject", subject);
		paramMap.put("body", body);
		paramMap.put("merResv1", merResv1);
		// paramMap.put("dcType", dcType);
		paramMap.put("signMethod", "MD5");

		String url = "http://gpay.chinagpay.com/bas/FrontTrans";
		Set<String> removeKey = new HashSet<String>();
		removeKey.add("signMethod");
		removeKey.add("signature");
		String signMsg = SignUtil.getSignMsg(paramMap, removeKey);
		String signMethod = "MD5";
		String md5_key = "WbVz375J4sGwFaUfwsB33KHgPYez887g";
		String signature = SignUtil.sign(signMethod, signMsg, md5_key, "UTF-8");
		System.out.println("signature:" + signature);
		paramMap.put("signature", signature);
		converData(paramMap);

		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ url + "\">";
		for (String key : paramMap.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramMap.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";

		System.out.println("爱农表单:" + FormString);
	}

	private static void converData(Map<String, String> paramMap) {
		for (int i = 0; i < base64Keys.length; i++) {
			String key = base64Keys[i];
			String value = (String) paramMap.get(key);
			if (StringUtils.isNotEmpty(value))
				try {
					String text = new String(Base64.encode(value.getBytes("UTF-8")));

					paramMap.put(key, text);
				} catch (Exception localException) {
				}
		}
		for (int i = 0; i < base64JsonKeys.length; i++) {
			String key = base64JsonKeys[i];
			String value = (String) paramMap.get(key);
			if (StringUtils.isNotEmpty(value))
				try {
					String text = new String(Base64.encode(value.getBytes("UTF-8")));
					paramMap.put(key, text);
				} catch (Exception localException1) {
				}
		}
	}
	
	
	public static void main(String[] args) {
//		yinlian();
		// pay();
		String str = "txnSubType=01&signature=uNJ6BFA5JfnpP91j+mZUrQ==&respMsg=5p+l5peg5q2k5Lqk5piT&txnType=00&merId=929040095023522&version=1.0.0&respCode=2002&signMethod=MD5&merOrderId=ANtxk201804051715031715039430";
		str = "{\""+str.replace("&","\",\"").replace("==", "--").replace("=", "\":\"").replace("--", "==")+"\"}";
		System.out.println(JSONObject.fromObject(str));
		JSONObject ji = JSONObject.fromObject(str);
		System.out.println(ji.get("respCode"));

		String signMethod = "MD5";
		String version = "1.0.0";
		String txnType ="00";
		String txnSubType = "01";
		String merId = "929040095023522";
		String merOrderId = "ANbl1201804062123562123562248";
		String url = "http://gpay.chinagpay.com/bas/BgTrans";
		Map<String,String>paramMap = new HashMap<String,String>();
		paramMap.put("signMethod", signMethod);
		paramMap.put("version", version);
		paramMap.put("txnType", txnType);
		paramMap.put("txnSubType", txnSubType);
		paramMap.put("merId", merId);
		paramMap.put("merOrderId", merOrderId);
		
		Set<String> removeKey = new HashSet<String>();
		removeKey.add("signMethod");
		removeKey.add("signature");
		String signMsg = SignUtil.getSignMsg(paramMap, removeKey);
		String md5_key = "WbVz375J4sGwFaUfwsB33KHgPYez887g";
		String signature = SignUtil.sign(signMethod, signMsg, md5_key, "UTF-8");
		System.out.println("signature:" + signature);
		paramMap.put("signature", signature);
		converData(paramMap);
		String retult = TestdoPost(url, paramMap, "UTF-8");
		System.out.println("retult:"+retult);
		if(retult.indexOf("respCode=1001") != -1){
			System.out.println(true);
		}else{
			System.out.println(false);
		}
		
	}
	public static String TestdoPost(String url, Map<String, String> map, String charset) {
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		try {
			httpClient = new SSLClient();
			httpPost = new HttpPost(url);
			// ���ò���
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			Iterator iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> elem = (Entry<String, String>) iterator.next();
				list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
			}
			if (list.size() > 0) {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, charset);
				httpPost.setEntity(entity);
			}
			HttpResponse response = httpClient.execute(httpPost);
			if (response != null) {
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					result = EntityUtils.toString(resEntity, charset);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	/**
	 * 验签
	 * 
	 * @param paramMap
	 * @return
	 */
	public static boolean verifySign(Map<String, String> paramMap) {
		// 计算签名
		Set<String> removeKey = new HashSet<String>();
		removeKey.add("signMethod");
		removeKey.add("signature");
//		String respMsg=paramMap.get("respMsg");
//		String text=null;
//		try {
//			text = new String(Base64.encode(respMsg.getBytes("UTF-8")));
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//			return false;
//		}
//		paramMap.put("respMsg", text);
		
		String signedMsg = SignUtil.getSignMsg(paramMap, removeKey);
		System.out.println(signedMsg);
		String signMethod = (String) paramMap.get("signMethod");
		String signature = (String) paramMap.get("signature");
		
		// 密钥
		return SignUtil.verifySign(signMethod, signedMsg, signature, "WbVz375J4sGwFaUfwsB33KHgPYez887g", "UTF-8");
	}


	@Override
	public String callback(Map<String, String> infoMap) {
		String txnType ="00";
		String merOrderId = infoMap.get("merOrderId");
		Map<String,String>paramMap = new HashMap<String,String>();
		paramMap.put("signMethod", signMethod);
		paramMap.put("version", version);
		paramMap.put("txnType", txnType);
		paramMap.put("txnSubType", txnSubType);
		paramMap.put("merId", merId);
		paramMap.put("merOrderId", merOrderId);
		
		Set<String> removeKey = new HashSet<String>();
		removeKey.add("signMethod");
		removeKey.add("signature");
		String signMsg = SignUtil.getSignMsg(paramMap, removeKey);
		String signature = SignUtil.sign(signMethod, signMsg, md5_key, "UTF-8");
		System.out.println("signature:" + signature);
		paramMap.put("signature", signature);
		converData(paramMap);
		String retult = TestdoPost(queryUrl, paramMap, "UTF-8");
		System.out.println("retult:"+retult);
		if(retult.indexOf("respCode=1001") != -1){
			return "success";
		}
		return "";
	}
	
}
