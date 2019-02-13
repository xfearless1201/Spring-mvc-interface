package com.cn.tianxia.pay.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.json.JSONUtils;
import com.cn.tianxia.pay.gst.util.AES;
import com.cn.tianxia.pay.gst.util.AppConstants;
import com.cn.tianxia.pay.gst.util.DateUtils;
import com.cn.tianxia.pay.gst.util.KeyValue;
import com.cn.tianxia.pay.gst.util.KeyValues;
import com.cn.tianxia.pay.gst.util.MD5Encoder;
import com.cn.tianxia.pay.gst.util.URLUtils;
import com.cn.tianxia.pay.ht.util.HttpRequestUtil;
import com.cn.tianxia.pay.mkt.util.HttpUtil;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * 
 * @Description:TODO
 * 
 * @author:zouwei
 * 
 * @time:2017年7月25日 下午5:55:39
 * 
 */
public class MKTPayServiceImpl implements PayService {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private static String input_charset = "UTF-8";// 参数字符集编码
	private String inform_url;// 服务器异步通知地址
	private String return_url;// 页面同步跳转通知地址
	private String merchant_code; // 商户号
	private String req_referer;// 来路域名
	private String customer_ip;// 消费者IP
	private String return_params;// 回传参数
	private String pay_url;// 支付网关地址
	private String merchant_key ; // 商户key

	// 商户订单号

	public MKTPayServiceImpl(Map<String, String> pmap, String type) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			if ("bank".equals(type)) {
				JSONObject bankJson = (JSONObject) jo.get("bank");
				pay_url = bankJson.get("pay_url").toString();
				merchant_key = bankJson.get("merchant_key").toString();
				inform_url = bankJson.get("inform_url").toString();
				// return_url=jo.get("return_url").toString();
				return_params = bankJson.get("return_params").toString();
				merchant_code = bankJson.get("merchant_code").toString();
			}
			if ("wx".equals(type)) {
				JSONObject wxJson = (JSONObject) jo.get("wx");
				pay_url = wxJson.get("pay_url").toString();
				merchant_key = wxJson.get("merchant_key").toString();
				inform_url = wxJson.get("inform_url").toString();
				// return_url=jo.get("return_url").toString();
				return_params = wxJson.get("return_params").toString();
				merchant_code = wxJson.get("merchant_code").toString();
			}
			if ("ali".equals(type)) {
				JSONObject aliJson = (JSONObject) jo.get("ali");
				pay_url = aliJson.get("pay_url").toString();
				merchant_key = aliJson.get("merchant_key").toString();
				inform_url = aliJson.get("inform_url").toString();
				// return_url=jo.get("return_url").toString();
				return_params = aliJson.get("return_params").toString();
				merchant_code = aliJson.get("merchant_code").toString();
			}
			if ("cft".equals(type)) {
				JSONObject wxJson = (JSONObject) jo.get("cft");
				pay_url = wxJson.get("pay_url").toString();
				merchant_key = wxJson.get("merchant_key").toString();
				inform_url = wxJson.get("inform_url").toString();
				// return_url=jo.get("return_url").toString();
				return_params = wxJson.get("return_params").toString();
				merchant_code = wxJson.get("merchant_code").toString();
			}
			if ("jd".equals(type)) {
				JSONObject aliJson = (JSONObject) jo.get("jd");
				pay_url = aliJson.get("pay_url").toString();
				merchant_key = aliJson.get("merchant_key").toString();
				inform_url = aliJson.get("inform_url").toString();
				// return_url=jo.get("return_url").toString();
				return_params = aliJson.get("return_params").toString();
				merchant_code = aliJson.get("merchant_code").toString();
			}
		}

	}

	/**
	 * 国盛通网银支付接口
	 * 
	 * @param map
	 * @return
	 */
	public String GSTpay(Map<String, String> map) {
		String pay_type = "1";// 固定值支付方式 1.网银支付
		String amount = AES.encrypt(map.get("amount"), merchant_key); // 商户密钥
		String bank_code = map.get("bank_code");// 银行编码
		String order_no = map.get("order_no");// 订单号
		String currentDate = DateUtils.format(new Date());// 订单时间
		return_url = map.get("return_url");// 返回URL
		customer_ip = map.get("customer_ip");
		req_referer = map.get("req_referer");

		KeyValues kvs1 = new KeyValues();
		kvs1.add(new KeyValue(AppConstants.INPUT_CHARSET, input_charset));
		kvs1.add(new KeyValue(AppConstants.NOTIFY_URL, inform_url));
		kvs1.add(new KeyValue(AppConstants.RETURN_URL, return_url));
		kvs1.add(new KeyValue(AppConstants.PAY_TYPE, pay_type));
		kvs1.add(new KeyValue(AppConstants.BANK_CODE, bank_code));
		kvs1.add(new KeyValue(AppConstants.MERCHANT_CODE, merchant_code));
		kvs1.add(new KeyValue(AppConstants.ORDER_NO, order_no));
		kvs1.add(new KeyValue(AppConstants.ORDER_AMOUNT, amount));
		kvs1.add(new KeyValue(AppConstants.ORDER_TIME, currentDate));
		kvs1.add(new KeyValue(AppConstants.REQ_REFERER, req_referer));
		kvs1.add(new KeyValue(AppConstants.CUSTOMER_IP, customer_ip));
		kvs1.add(new KeyValue(AppConstants.RETURN_PARAMS, return_params));
		String sign1 = kvs1.sign(merchant_key, input_charset);
		Map<String, String> map1 = new HashMap<String, String>();
		map1.put(AppConstants.INPUT_CHARSET, input_charset);
		map1.put(AppConstants.RETURN_URL, return_url);
		map1.put(AppConstants.NOTIFY_URL, inform_url);
		map1.put(AppConstants.PAY_TYPE, pay_type);
		map1.put(AppConstants.BANK_CODE, bank_code);
		map1.put(AppConstants.MERCHANT_CODE, merchant_code);
		map1.put(AppConstants.ORDER_NO, order_no);
		map1.put(AppConstants.ORDER_AMOUNT, amount);
		map1.put(AppConstants.ORDER_TIME, currentDate);
		map1.put(AppConstants.REQ_REFERER, req_referer);
		map1.put(AppConstants.CUSTOMER_IP, customer_ip);
		map1.put(AppConstants.RETURN_PARAMS, return_params);
		map1.put(AppConstants.SIGN, sign1);
		String htmlFrom = HttpUtil.HtmlFrom(pay_url, map1);
		logger.info("GST_banking_post_url:" + htmlFrom);
		return htmlFrom;
	}

	/**
	 * 回调验证
	 * 
	 * @param req
	 * @return
	 */
	public String validPageNotify(Map<String, String> req) {
		String merchantCode = req.get(AppConstants.MERCHANT_CODE);
		String orderNo = req.get(AppConstants.ORDER_NO);
		String orderAmount = req.get(AppConstants.ORDER_AMOUNT);
		String orderTime = req.get(AppConstants.ORDER_TIME);
		String returnParams = req.get(AppConstants.RETURN_PARAMS);
		String tradeNo = req.get(AppConstants.TRADE_NO);
		String tradeStatus = req.get(AppConstants.TRADE_STATUS);
		String sign = req.get(AppConstants.SIGN);
		KeyValues kvs = new KeyValues();
		kvs.add(new KeyValue("merchant_code", merchantCode));
		kvs.add(new KeyValue("order_no", orderNo));
		kvs.add(new KeyValue("order_time", orderTime));
		kvs.add(new KeyValue("order_amount", orderAmount));
		kvs.add(new KeyValue("trade_status", tradeStatus));
		kvs.add(new KeyValue("trade_no", tradeNo));
		kvs.add(new KeyValue("return_params", returnParams));
		String thizSign = kvs.sign(merchant_key, input_charset);
		logger.info("MKT_sign:" + sign + "----------" + "local_sign:" + thizSign);
		if (thizSign.equalsIgnoreCase(sign))
			return "SUCCESS";
		else
			return "";
	}

	public static void TestGSTpayCallback(Map<String, String> map) {
		String pay_url = "http://localhost:8087/JJF/PlatformPay/scanPayNotify.do";
		// 填key
		String merchant_key = "96e0285fac3968ff428443e0459b2edc";
		String retrun_params = "";
		String merchantCode = "83896067";
		String orderNo = "MKTtxk201802161427391427396160";
		String orderAmount = "100";
		String orderTime = "2017-07-26 11:12:24";
		String tradeNo = "7891221";
		String tradeStatus = "success";

		KeyValues kvs = new KeyValues();
		kvs.add(new KeyValue("merchant_code", merchantCode));
		kvs.add(new KeyValue("order_no", orderNo));
		kvs.add(new KeyValue("order_time", orderTime));
		kvs.add(new KeyValue("order_amount", orderAmount));
		kvs.add(new KeyValue("trade_status", tradeStatus));
		kvs.add(new KeyValue("trade_no", tradeNo));
		kvs.add(new KeyValue("return_params", retrun_params));
		String sign = kvs.sign(merchant_key, input_charset);
		StringBuilder sb = new StringBuilder();
		sb.append(pay_url);
		URLUtils.appendParam(sb, AppConstants.MERCHANT_CODE, merchantCode, false);
		URLUtils.appendParam(sb, AppConstants.ORDER_NO, orderNo);
		URLUtils.appendParam(sb, AppConstants.ORDER_TIME, orderTime);
		URLUtils.appendParam(sb, AppConstants.ORDER_AMOUNT, orderAmount);
		URLUtils.appendParam(sb, AppConstants.TRADE_STATUS, tradeStatus);
		URLUtils.appendParam(sb, AppConstants.TRADE_NO, tradeNo);
		URLUtils.appendParam(sb, AppConstants.RETURN_PARAMS, retrun_params, input_charset);
		URLUtils.appendParam(sb, AppConstants.SIGN, sign);
		System.out.println("GST:" + sb.toString());
	}

	/**
	 * 扫码支付
	 * 
	 * @param map
	 * @return
	 */
	public String GSTScanpay(Map<String, String> map) {
		String pay_type = map.get("pay_type");// 固定值支付方式 1.网银支付 2.微信，3支付宝
		String amount = AES.encrypt(map.get("amount"), merchant_key); // 商户密钥
		String bank_code = ""; // map.get("bank_code");// 扫码支付银行可空
		String order_no = map.get("order_no");// 订单号
		String currentDate = DateUtils.format(new Date());// 订单时间
		return_url = map.get("return_url");// 返回URL
		customer_ip = map.get("customer_ip");
		req_referer = map.get("req_referer");

		KeyValues kvs1 = new KeyValues();
		kvs1.add(new KeyValue(AppConstants.INPUT_CHARSET, input_charset));
		kvs1.add(new KeyValue(AppConstants.NOTIFY_URL, inform_url));
		kvs1.add(new KeyValue(AppConstants.RETURN_URL, return_url));
		kvs1.add(new KeyValue(AppConstants.PAY_TYPE, pay_type));
		kvs1.add(new KeyValue(AppConstants.BANK_CODE, bank_code));
		kvs1.add(new KeyValue(AppConstants.MERCHANT_CODE, merchant_code));
		kvs1.add(new KeyValue(AppConstants.ORDER_NO, order_no));
		kvs1.add(new KeyValue(AppConstants.ORDER_AMOUNT, amount));
		kvs1.add(new KeyValue(AppConstants.ORDER_TIME, currentDate));
		kvs1.add(new KeyValue(AppConstants.REQ_REFERER, req_referer));
		kvs1.add(new KeyValue(AppConstants.CUSTOMER_IP, customer_ip));
		kvs1.add(new KeyValue(AppConstants.RETURN_PARAMS, return_params));
		String sign1 = kvs1.sign(merchant_key, input_charset);
		StringBuilder sb = new StringBuilder();
		sb.append(pay_url);
		URLUtils.appendParam(sb, AppConstants.INPUT_CHARSET, input_charset, false);

		URLUtils.appendParam(sb, AppConstants.NOTIFY_URL, inform_url, input_charset);
		URLUtils.appendParam(sb, AppConstants.RETURN_URL, return_url, input_charset);
		URLUtils.appendParam(sb, AppConstants.PAY_TYPE, pay_type);
		URLUtils.appendParam(sb, AppConstants.BANK_CODE, bank_code);
		URLUtils.appendParam(sb, AppConstants.MERCHANT_CODE, merchant_code);

		URLUtils.appendParam(sb, AppConstants.ORDER_NO, order_no);
		URLUtils.appendParam(sb, AppConstants.ORDER_AMOUNT, amount);
		URLUtils.appendParam(sb, AppConstants.ORDER_TIME, currentDate);
		URLUtils.appendParam(sb, AppConstants.REQ_REFERER, req_referer, input_charset);
		URLUtils.appendParam(sb, AppConstants.CUSTOMER_IP, customer_ip);
		URLUtils.appendParam(sb, AppConstants.RETURN_PARAMS, return_params, input_charset);
		URLUtils.appendParam(sb, AppConstants.SIGN, sign1);

		// 表单提交
		/*
		 * Map<String, String> map1 = new HashMap<String, String>();
		 * map1.put(AppConstants.INPUT_CHARSET, input_charset);
		 * map1.put(AppConstants.RETURN_URL, URLUtils.encode(return_url,
		 * input_charset)); map1.put(AppConstants.NOTIFY_URL,
		 * URLUtils.encode(inform_url, input_charset));
		 * map1.put(AppConstants.PAY_TYPE, pay_type);
		 * map1.put(AppConstants.BANK_CODE, bank_code);
		 * map1.put(AppConstants.MERCHANT_CODE, merchant_code);
		 * map1.put(AppConstants.ORDER_NO, order_no);
		 * map1.put(AppConstants.ORDER_AMOUNT, amount);
		 * map1.put(AppConstants.ORDER_TIME, currentDate);
		 * map1.put(AppConstants.REQ_REFERER, URLUtils.encode(req_referer,
		 * input_charset)); map1.put(AppConstants.CUSTOMER_IP, customer_ip);
		 * map1.put(AppConstants.RETURN_PARAMS, URLUtils.encode(return_params,
		 * input_charset)); map1.put(AppConstants.SIGN, sign1); String htmlFrom
		 * = com.Payworth.util.HttpUtil.HtmlFrom(pay_url, map1);
		 */
		logger.info("GST_scanPay_post_url:" + sb.toString());
		// System.out.println(htmlFrom);
		return sb.toString();
	}

	public void df_pay() {
		String url = "http://df.mktpay.vip/gateway/dfm.html";
		String input_charset = "UTF-8";
		KeyValues kvs = new KeyValues();
		kvs.add(new KeyValue("input_charset", input_charset));
		kvs.add(new KeyValue("amount", "0.01"));
		kvs.add(new KeyValue("transid", "20161208102420106741"));
		kvs.add(new KeyValue("bitch_no", "201612081022221177706961"));
		kvs.add(new KeyValue("currentDate", "2016-12-05 21:45:11"));
		kvs.add(new KeyValue("bank_name", "ICBC"));
		kvs.add(new KeyValue("account_name", "周冠荣"));
		kvs.add(new KeyValue("account_number", "6222031001000682901"));
		kvs.add(new KeyValue("remark", "1111"));
		// 商户号
		kvs.add(new KeyValue("merchant_code", merchant_code));
		String param = kvs.getSortUrl(input_charset);
		// 填key
		// merchant_key = "4223de494ba973e3ff755a2d8f44912d";
		System.out.println(param);
		String sign = kvs.sign(merchant_key, "UTF-8").toUpperCase();
		for (int i = 0; i < 2; i++) {
			sign = MD5Encoder.encode(sign, input_charset);
		}
		param = param + "&sign=" + sign;
		String result = HttpRequestUtil.sendPost(url, param);
		System.out.println("结果:" + result);
	}

	public void df_query() {
		String url = "http://df.mktpay.vip/gateway/df_query.html";
		String input_charset = "UTF-8";
		KeyValues kvs = new KeyValues();
		kvs.add(new KeyValue("input_charset", input_charset));
		// 商户号
		kvs.add(new KeyValue("merchant_code", merchant_code));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentDate = sdf.format(new Date());
		kvs.add(new KeyValue("currentDate", currentDate));
		String order_id = "20161208102420106741";
		kvs.add(new KeyValue("order_id", order_id));

		String param = kvs.getSortUrl(input_charset);
		// 填key
		// merchant_key = "4223de494ba973e3ff755a2d8f44912d";
		System.out.println(param);
		String sign = kvs.sign(merchant_key, "UTF-8").toUpperCase();
		for (int i = 0; i < 2; i++) {
			sign = MD5Encoder.encode(sign, input_charset);
		}
		param = param + "&sign=" + sign;
		String result = HttpRequestUtil.sendPost(url, param);
		System.out.println("结果:" + result);

	}

	public static void main(String[] args) {
		JSONObject json = new JSONObject();
		Map<String, String> aliMap = new HashMap<String, String>();
		aliMap.put("pay_url", "http://pay.gstpay.vip/gstpay/gateway/pay.html");
		aliMap.put("merchant_key", "4223de494ba973e3ff755a2d8f44912d");
		aliMap.put("inform_url", "http://localhost:8083/fastRechargeConsumer/PlatformPay/MKTNotify.do");
		aliMap.put("merchant_code", "83468923");
		aliMap.put("return_params", "");
		json.put("cft", aliMap);

//		 System.out.println(json.toString());
//		 Map<String, String> pmapsconfig = JSONUtils.toHashMap(json);
//		 System.out.println("---------------------test
//		 start----------------------");
//		 TestGSTpayCallback(null);

		MKTPayServiceImpl mkt = new MKTPayServiceImpl(json, "cft");
		// mkt.df_pay();
//		// mkt.df_query();
//		String url = "http://df.mktpay.vip/gateway/df_query.html";
//		String input_charset = "UTF-8";
//		KeyValues kvs = new KeyValues();
//		kvs.add(new KeyValue("input_charset", input_charset));
//		// 商户号
//		kvs.add(new KeyValue("merchant_code", "83468923"));
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String currentDate = sdf.format(new Date());
//		kvs.add(new KeyValue("currentDate", currentDate));
//		String order_id = "20161208102420106741";
//		kvs.add(new KeyValue("order_id", order_id));
//
//		String param = kvs.getSortUrl(input_charset);
//		// 填key
//		// merchant_key = "4223de494ba973e3ff755a2d8f44912d";
//		System.out.println(param);
//		String sign = kvs.sign("4223de494ba973e3ff755a2d8f44912d", "UTF-8").toUpperCase();
//		for (int i = 0; i < 2; i++) {
//			sign = MD5Encoder.encode(sign, input_charset);
//		}
//		param = param + "&sign=" + sign;
//		String result = HttpRequestUtil.sendPost(url, param);
//		System.out.println("结果:" + result);

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

		Map<String, String> map = new HashMap<String, String>();
		map.put("amount", String.valueOf(amount));
		map.put("bank_code", pay_code);
		map.put("order_no", order_no);
		map.put("return_url", refereUrl);
		map.put("customer_ip", ip);
		map.put("req_referer", pay_url);
		/* 国盛通支付时使用单独商户号，需区别微信支付宝或者网银支付key配置 */
		String html = GSTpay(map);
		/* 国盛通表单提交方式不需要对form参数进行URLencoding，如用http请求则需要对参数URL编码 */
		return PayUtil.returnWYPayJson("success", "form", html, pay_url, "");
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

		Map<String, String> map = new HashMap<String, String>();
		map.put("pay_type", pay_code);// 支付类型，2微信，3支付宝
		map.put("amount", String.valueOf(amount));
		map.put("bank_code", "");
		map.put("order_no", order_no);
		map.put("return_url", refereUrl);
		map.put("customer_ip", ip);
		// String pay_url = rMap.get("pay_url").toString();// 支付跳转地址
		map.put("req_referer", refereUrl);
		String type = "";
		if (pay_code.equals("2")) {
			type = "wx";
		} else if (pay_code.equals("3")) {
			type = "ali";
		} else if (pay_code.equals("5")) {
			type = "cft";
		} else if (pay_code.equals("6")) {
			type = "jd";
		}
		/* 国盛通支付时使用单独商户号，需区别微信支付宝或者网银支付key配置 */
		String HTML = GSTScanpay(map);
		return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no, HTML);
	}

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }

}
