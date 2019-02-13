package com.cn.tianxia.pay.impl;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.json.JSONUtils;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.jh.util.MerchantApiUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;

public class LXPayServiceImpl implements PayService {

	private String paySecret;// 密钥
	private String apiUrl;// 网银支付接口地址
	private String tradeType;// 交易类型
	private String version;// 接口版本
	private String mchNo;// 商户号
	private String currency;// 货币类型
	private String notifyUrl;// 服务器回调地址
	private static String api_method;// 接口方法

	private final static Logger logger = LoggerFactory.getLogger(LXPayServiceImpl.class);

	public LXPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			paySecret = jo.getString("paySecret");// 密钥
			apiUrl = jo.getString("apiUrl");// 网银支付接口地址
			tradeType = jo.getString("tradeType");// 交易类型
			version = jo.getString("version");// 接口版本
			mchNo = jo.getString("mchNo");// 商户号
			currency = jo.getString("currency");// 货币类型
			notifyUrl = jo.getString("notifyUrl");
			api_method = jo.getString("api_method");
		}
	}

	/**
	 * 网银支付
	 * 
	 * @param bankMap
	 * @return
	 */
	public String bankPay(Map<String, String> bankMap) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("tradeType", tradeType);
		params.put("version", version);
		params.put("channel", bankMap.get("channel"));
		params.put("mchNo", mchNo);
		params.put("mchOrderNo", bankMap.get("mchOrderNo"));// 订单号
		params.put("body", "txwl");
		params.put("amount", bankMap.get("amount"));// 金额
		params.put("currency", currency);
		String timePaid = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		params.put("timePaid", timePaid);// 订单提交时间
		params.put("remark", "");// 支付描述

		Map<String, String> payExtra = new HashMap<String, String>();
		payExtra.put("notifyUrl", notifyUrl);
		payExtra.put("callbackUrl", bankMap.get("callbackUrl"));
		payExtra.put("merchantName", "txwl");
		payExtra.put("goodsId", "txwl");
		payExtra.put("goodsDesc", "txwl");
		payExtra.put("showUrl", "");
		payExtra.put("memberId", "123456789");// + System.currentTimeMillis());
		payExtra.put("bankType", bankMap.get("bankType"));
		payExtra.put("cardType", "0");// 卡类标识0:借记卡 1:贷记卡
		payExtra.put("orderPeriod", "30");// 订单有效时间
		Map<String, Object> signMap = new HashMap<String, Object>();
		signMap.putAll(params);
		signMap.putAll(payExtra);
		String sign = MerchantApiUtil.getSign(signMap, paySecret);
		params.put("extra", JSONObject.fromObject(payExtra).toString());
		params.put("sign", sign);
		// logger.info("请求参数MapToString:"+params.toString());
		String ps = params.toString().replaceAll(",", "&");
		String pstemp = ps.substring(1, ps.length());
		logger.info("请求参数String:" + pstemp.substring(0, pstemp.length() - 1));
		String data = postByHttpClient(apiUrl + api_method, params);
		logger.info("利鑫响应:" + data);
		Map<String, Object> map = JSONUtils.toHashMap(data);

		if (map != null && map.size() > 0) {
			if ((int) map.get("status") == 0 && (int) map.get("resultCode") == 0) {
				// 商户在此处处理业务
				String token = (String) map.get("tokenId");
				return apiUrl + api_method + "/token?token=" + token;
			}
		}
		logger.info("利鑫支付表单生成异常！");
		return "";
	}

	/**
	 * 扫码接口
	 * 
	 * @param scanMap
	 * @return
	 */
	public JSONObject scanPay(Map<String, String> scanMap) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("tradeType", tradeType);
		params.put("version", version);
		String channel = scanMap.get("channel");
		params.put("channel", channel);
		params.put("mchNo", mchNo);
		params.put("mchOrderNo", scanMap.get("mchOrderNo"));
		params.put("body", "txwl");
		params.put("amount", scanMap.get("amount"));
		params.put("currency", currency);
		String timePaid = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		params.put("timePaid", timePaid);
		params.put("timeExpire", "30");
		params.put("remark", "");
		Map<String, String> payExtra = new HashMap<String, String>();
		payExtra.put("payUserName", "payUserName");
		payExtra.put("payerId", "123456789");
		payExtra.put("salerId", "");
		payExtra.put("guaranteeAmt", "");
		payExtra.put("notifyUrl", notifyUrl);
		// 银联扫码必填字段
		if ("union_qr".equals(channel)) {
			payExtra.put("clientIp", scanMap.get("clientIp"));
		}
		Map<String, Object> signMap = new HashMap<String, Object>();
		signMap.putAll(params);
		signMap.putAll(payExtra);
		String sign = MerchantApiUtil.getSign(signMap, paySecret);
		params.put("extra", JSONObject.fromObject(payExtra).toString());
		params.put("sign", sign);
		// logger.info("请求参数MapToString:"+params.toString());
		String ps = params.toString().replaceAll(",", "&");
		String pstemp = ps.substring(1, ps.length());
		logger.info("请求参数String:" + pstemp.substring(0, pstemp.length() - 1));
		String data = postByHttpClient(apiUrl + api_method, params);
		logger.info("利鑫响应:" + data);
		Map<String, Object> map = JSONUtils.toHashMap(data);

		if (map != null && map.size() > 0) {
			if ((int) map.get("status") == 0 && (int) map.get("resultCode") == 0) {
				// 商户在此处处理业务
				String codeUrl = (String) map.get("codeUrl");
				logger.info("成功获取二维码地址：" + codeUrl);
				return getReturnJson("success", codeUrl, "获取二维码图片成功！");
			}
		}
		return getReturnJson("error", "", data);
	}

	/**
	 * 返回数据格式Json
	 * 
	 * @param status
	 * @param qrCode
	 * @param msg
	 * @return
	 */
	public JSONObject getReturnJson(String status, String qrCode, String msg) {
		JSONObject json = new JSONObject();
		json.put("status", status);
		json.put("qrCode", qrCode);
		if (StringUtils.isNullOrEmpty(msg)) {
			json.put("msg", "");
		} else {
			json.put("msg", msg);
		}
		return json;
	}

	/**
	 * post请求
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public String postByHttpClient(String url, Map<String, String> params) {
		try {
			HttpPost httpPost = new HttpPost(url);
			CloseableHttpClient client = HttpClients.createDefault();
			String respContent = null;

			// 表单方式
			List<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
			if (params != null && params.size() > 0) {
				for (String key : params.keySet()) {
					pairList.add(new BasicNameValuePair(key, params.get(key)));
				}
			}
			httpPost.setEntity(new UrlEncodedFormEntity(pairList, "utf-8"));

			HttpResponse resp = client.execute(httpPost);
			if (resp.getStatusLine().getStatusCode() == 200) {
				HttpEntity he = resp.getEntity();
				respContent = EntityUtils.toString(he, "UTF-8");
			}
			return respContent;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}

	}

	/**
	 * 回调方法
	 * 
	 * @param infoMap
	 * @return
	 */
	@Override
	public String callback(Map<String, String> infoMap) {
		Map<String, Object> msgMap = JSONUtils.toHashMap(infoMap);
		if (msgMap != null && msgMap.size() != 0) {
			String sign = (String) msgMap.get("sign");
			msgMap.remove("sign");
			String resultSign = MerchantApiUtil.getSign(msgMap, paySecret);
			if (resultSign.equals(sign)) {
				// 商户在此处处理业务
				return "success";
			}
		}
		return "";
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

		Map<String, String> bankMap = new HashMap<>();
		bankMap.put("channel", "union_pay");
		bankMap.put("mchOrderNo", order_no);// 订单号
		int int_amount = (int) (amount * 100);
		bankMap.put("amount", String.valueOf(int_amount));// 金额
		bankMap.put("callbackUrl", refereUrl);
		bankMap.put("bankType", pay_code);
		String html = bankPay(bankMap);
		if (StringUtils.isNullOrEmpty(html)) {
			logger.info("利鑫表单生成异常！");
			// 返回错误信息
			return PayUtil.returnWYPayJson("error", "form", html, pay_url, "");
		}
		return PayUtil.returnWYPayJson("success", "link", html, pay_url, "");
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String userName = payEntity.getUsername();
		String pay_url = payEntity.getPayUrl();
		String mobile = payEntity.getMobile();
		String ip = payEntity.getIp();
		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("clientIp", ip);
		scanMap.put("mchOrderNo", order_no);// 订单号
		int int_amount = (int) (amount * 100);
		scanMap.put("amount", String.valueOf(int_amount));// 金额
		scanMap.put("callbackUrl", refereUrl);
		scanMap.put("channel", pay_code);
		JSONObject rtJson = scanPay(scanMap);

		if (StringUtils.isNullOrEmpty(mobile)) {
			// pc端
			if (!"success".equals(rtJson.getString("status"))) {
				return PayUtil.returnPayJson("error", "2", rtJson.getString("msg"), userName, amount, order_no, "");
			}
			// 银联返回qrcode生成地址
			String qr_type = "3";
			// 默认返回图片连接地址
			if ("union_qr".equals(pay_code)) {
				qr_type = "2";
			}
			return PayUtil.returnPayJson("success", qr_type, "支付接口请求成功!", userName, amount, order_no,
					rtJson.getString("qrCode"));
		} else {
			// 手机端
			if (!"success".equals(rtJson.getString("status"))) {
				return PayUtil.returnPayJson("error", "4", "支付接口请求失败!", userName, amount, order_no, "");
			}
			return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
					rtJson.getString("qrCode"));
		}
	}
}
