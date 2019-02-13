package com.cn.tianxia.pay.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.ly.util.HttpMethod;
import com.cn.tianxia.pay.ly.util.HttpSendModel;
import com.cn.tianxia.pay.ly.util.SimpleHttpResponse;
import com.cn.tianxia.pay.mkt.util.HttpUtil;
import com.cn.tianxia.pay.mkt.util.Log;
import com.cn.tianxia.pay.qyf.util.ToolKit;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

public class YFPayServiceImpl implements PayService {

	private String api_url;

	private String Appkey;

	private String notifyUrl;

	private String merchantCode;

	private final static Logger logger = LoggerFactory.getLogger(YFPayServiceImpl.class);

	// private String bank_url = "http://api.all-linepay.com/pay.do";
	public YFPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			api_url = jo.get("api_url").toString();// 支付地址
			Appkey = jo.get("Appkey").toString();// 商户密钥
			notifyUrl = jo.get("notifyUrl").toString();// 通知地址
			merchantCode = jo.get("merchantCode").toString();// 商户号
		}
	}

	/**
	 * 网银支付
	 * 
	 * @param bankMap
	 * @return
	 */
	public String bankPay(Map<String, String> bankMap) {
		String returnUrl = bankMap.get("returnUrl");
		String payCode = bankMap.get("model");// 固定值
		String orderCode = bankMap.get("orderCode");
		String orderTotal = bankMap.get("orderTotal");

		SimpleDateFormat smd = new SimpleDateFormat("YYYY-mm-dd HH:mm:ss");

		String orderTime = smd.format(new Date());
		// String reqReferer = "http://baidu.com"; //可选参数
		// String customerIp = "192.168.0.1"; //可选参数
		// String returnParams = ""; //可选参数
		String bankCode = bankMap.get("bankCode");// "none";
		String sign = "";

		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("notifyUrl", notifyUrl);
		paramMap.put("returnUrl", returnUrl);
		paramMap.put("payCode", payCode);
		paramMap.put("merchantCode", merchantCode);
		paramMap.put("orderCode", orderCode);
		paramMap.put("orderTotal", orderTotal);
		paramMap.put("orderTime", orderTime);
		paramMap.put("bankCode", bankCode);

		List<String> keys = new ArrayList<String>(paramMap.keySet());
		Collections.sort(keys);

		StringBuilder sb = new StringBuilder();

		String key, value;
		for (int i = 0; i < keys.size(); i++) {
			key = keys.get(i);
			value = paramMap.get(key);

			if (sb.length() == 0) {
				sb.append(key + "=" + value);
			} else {
				sb.append("&" + key + "=" + value);
			}
		}

		if (sb.length() > 0) {
			sb.append("&");
		}
		sb.append("key=");
		sb.append(Appkey);
		sign = ToolKit.MD5(sb.toString(), "UTF-8");

		paramMap.put("sign", sign);
		logger.info("签名字符原串：" + sb.toString());

		String PostParms = "";
		int PostItemTotal = paramMap.keySet().size();
		int Itemp = 0;
		for (String k1 : paramMap.keySet()) {
			PostParms += k1 + "=" + paramMap.get(k1);
			Itemp++;
			if (Itemp < PostItemTotal) {
				PostParms += "&";
			}
		}

		logger.info(api_url + "?" + PostParms);
		return api_url + "?" + PostParms;
	}

	/**
	 * pc端二维码支付
	 * 
	 * @param scanMap
	 * @return
	 */
	public JSONObject scanPay(Map<String, String> scanMap) {
		String returnUrl = scanMap.get("returnUrl");
		String payCode = scanMap.get("payCode");// 固定值
		String orderCode = scanMap.get("orderCode");
		String orderTotal = scanMap.get("orderTotal");

		SimpleDateFormat smd = new SimpleDateFormat("YYYY-mm-dd HH:mm:ss");

		String orderTime = smd.format(new Date());
		// String reqReferer = "http://baidu.com"; //可选参数
		// String customerIp = "192.168.0.1"; //可选参数
		// String returnParams = ""; //可选参数
		String bankCode = "none";// "none";
		String sign = "";

		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("notifyUrl", notifyUrl);
		paramMap.put("returnUrl", returnUrl);
		paramMap.put("payCode", payCode);
		paramMap.put("merchantCode", merchantCode);
		paramMap.put("orderCode", orderCode);
		paramMap.put("orderTotal", orderTotal);
		paramMap.put("orderTime", orderTime);
		paramMap.put("bankCode", bankCode);

		List<String> keys = new ArrayList<String>(paramMap.keySet());
		Collections.sort(keys);

		StringBuilder sb = new StringBuilder();

		String key, value;
		for (int i = 0; i < keys.size(); i++) {
			key = keys.get(i);
			value = paramMap.get(key);

			if (sb.length() == 0) {
				sb.append(key + "=" + value);
			} else {
				sb.append("&" + key + "=" + value);
			}
		}

		if (sb.length() > 0) {
			sb.append("&");
		}
		sb.append("key=");
		sb.append(Appkey);
		sign = ToolKit.MD5(sb.toString(), "UTF-8");

		paramMap.put("sign", sign);
		logger.info("签名字符原串：" + sb.toString());

		String resultstr = "";
		try {
			resultstr = RequestForm(api_url, paramMap);
			logger.info("响应数据：" + resultstr);

			if (isJson(resultstr)) {
				return getReturnJson("error", "", resultstr);
			} else {
				XMLSerializer xmlSerializer = new XMLSerializer();
				String result2 = xmlSerializer.read(resultstr).toString();
				JSONObject responseJson = JSONObject.fromObject(JSONObject.fromObject(result2));
				logger.info(responseJson.toString());
				if (responseJson.containsKey("status") && "SUCCESS".equals(responseJson.getString("status"))) {
					return getReturnJson("success", responseJson.getString("codeUrl"), "接口获取成功");
				} else {
					return getReturnJson("error", "", responseJson.toString());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return getReturnJson("error", "", resultstr);
		}
	}

	/**
	 * HTTP post 请求
	 * 
	 * @param Url
	 * @param Parms
	 * @return
	 */
	public String RequestForm(String Url, Map<String, String> Parms) {
		if (Parms.isEmpty()) {
			return "参数不能为空！";
		}
		String PostParms = "";
		int PostItemTotal = Parms.keySet().size();
		int Itemp = 0;
		for (String key : Parms.keySet()) {
			PostParms += key + "=" + Parms.get(key);
			Itemp++;
			if (Itemp < PostItemTotal) {
				PostParms += "&";
			}
		}
		Log.Write("【请求参数】：" + PostParms);
		HttpSendModel httpSendModel = new HttpSendModel(Url + "?" + PostParms);
		Log.Write("【后端请求】：" + Url + "?" + PostParms);
		httpSendModel.setMethod(HttpMethod.GET);
		SimpleHttpResponse response = null;
		try {
			response = HttpUtil.doRequest(httpSendModel, "UTF-8");
		} catch (Exception e) {
			return e.getMessage();
		}
		return response.getEntityString();
	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String pay_url = payEntity.getPayUrl();
		String mobile = payEntity.getMobile();

		Map<String, String> bankMap = new HashMap<>();
		bankMap.put("orderCode", order_no);
		bankMap.put("orderTotal", String.valueOf(amount));
		bankMap.put("bankCode", pay_code);
		bankMap.put("returnUrl", refereUrl);

		String model = "";
		// pc端
		if (StringUtils.isNullOrEmpty(mobile)) {
			model = "BankPay";// 银付网银网关支付
		} else {
			// 手机端
			model = "BankWap";// 网银手机h5
		}
		bankMap.put("model", model);

		String html = bankPay(bankMap);
		return PayUtil.returnWYPayJson("success", "link", html, pay_url, "");
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String mobile = payEntity.getMobile();
		String userName = payEntity.getUsername();

		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("orderCode", order_no);
		scanMap.put("orderTotal", String.valueOf(amount));
		scanMap.put("payCode", pay_code);
		scanMap.put("returnUrl", refereUrl);

		JSONObject r_json = scanPay(scanMap);

		if ("success".equals(r_json.getString("status"))) {
			// pc端
			if (StringUtils.isNullOrEmpty(mobile)) {
				return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no,
						r_json.getString("qrCode"));
			} else {
				// 手机端
				return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
						r_json.getString("qrCode"));
			}
		} else {
			return PayUtil.returnPayJson("error", "2", r_json.getString("msg"), userName, amount, order_no, "");
		}

	}

	/**
	 * 结果返回
	 * 
	 * @param status
	 * @param qrCode
	 * @param msg
	 * @return
	 */
	private JSONObject getReturnJson(String status, String qrCode, String msg) {
		JSONObject json = new JSONObject();
		json.put("status", status);
		json.put("qrCode", qrCode);
		json.put("msg", msg);
		return json;
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

	/**
	 * 判断是否是xml结构
	 */
	public static boolean isXML(String value) {
		try {
			DocumentHelper.parseText(value);
		} catch (DocumentException e) {
			return false;
		}
		return true;
	}

	@Override
	public String callback(Map<String, String> paramMap) {

		String serviceSign = paramMap.remove("sign");
		String sign = "";

		List infoIds = new ArrayList(paramMap.entrySet());

		String buff = "";

		Collections.sort(infoIds, new Comparator<Map.Entry<String, String>>() {
			public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
				return ((String) o1.getKey()).toString().compareTo((String) o2.getKey());
			}
		});
		for (int i = 0; i < infoIds.size(); i++) {
			Map.Entry item = (Map.Entry) infoIds.get(i);

			if (item.getKey() != "") {
				String key = String.valueOf(item.getKey());
				String val = String.valueOf(item.getValue());

				if ("".equals(val)) {
					continue;
				}
				buff = buff + key + "=" + val + "&";
			}
		}
		if (!buff.isEmpty())
			buff = buff.substring(0, buff.length() - 1);

		buff += "&key=" + Appkey;
		logger.info("待签名字符:" + buff.toString());

		sign = ToolKit.MD5(buff, "UTF-8");

		logger.info("本地签名：" + sign + "      " + serviceSign);
		if (sign.equals(serviceSign)) {
			logger.info("签名成功");
			return "success";
		}

		logger.info("签名失败");
		return "";
	}

}
