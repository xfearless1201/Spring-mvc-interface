package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * 
 * @Description:TODO
 * 
 * @author:zouwei
 * 
 * @time:2017年8月29日 下午3:50:20
 * 
 */
public class SYFPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(XQPayServiceImpl.class);

	private String parter;// 商户号
	private String callbackurl;// 回调地址
	private String key;// md5key
	private String url;// 支付地址

	public SYFPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = new JSONObject().fromObject(pmap);
		if (null != pmap) {
			key = jo.get("key").toString();// md5key
			url = jo.get("url").toString();// 支付地址
			parter = jo.getString("parter").toString();
			callbackurl = jo.getString("callbackurl").toString();// 商品数量
		}
	}

	public String bankPay(Map<String, String> req) {
		String str = getSignstr(parter, req.get("type"), req.get("value"), req.get("orderid"), callbackurl);
		String sign = md5(str + key);
		Map<String, String> returnMap = new LinkedHashMap<>();
		returnMap.put("parter", parter);
		returnMap.put("type", req.get("type"));
		returnMap.put("value", req.get("value"));
		returnMap.put("orderid", req.get("orderid"));
		returnMap.put("callbackurl", callbackurl);
		returnMap.put("parerIp", req.get("payerIp"));
		returnMap.put("hrefbackurl", req.get("returnUrl"));
		returnMap.put("sign", sign);
		// // 构建请求参数
		// Map<String, String> resquestMap = new LinkedHashMap<>();
		// form表单请求到接口地址
		List<String> keys = new ArrayList<String>(returnMap.keySet());
		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) returnMap.get(name);
			if (StringUtils.isNullOrEmpty(value)) {
				logger.info("删除:" + name);
				returnMap.remove(name);
			}
		}
		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ url + "\">";
		for (String key : returnMap.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + returnMap.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";

		String html = FormString;
		// String html = HttpUtil.HtmlFrom(url, resquestMap);
		logger.info("尚云付支付网银支付表单:" + html);
		return html;
	}

	/**
	 * 纯二维码链接的模式
	 * 
	 * @param req
	 * @return
	 */
	public String pureScanPay(Map<String, String> req) {
		String str = getSignstr(parter, req.get("type"), req.get("value"), req.get("orderid"), callbackurl);
		String sign = md5(str + key);
		StringBuffer sb = new StringBuffer();
		sb.append("parter=" + parter);
		sb.append("&type=" + req.get("type"));
		sb.append("&value=" + req.get("value"));
		sb.append("&orderid=" + req.get("orderid"));
		sb.append("&callbackurl=" + callbackurl);
		sb.append("&parerIp=" + req.get("payerIP"));
		sb.append("&hrefbackurl=" + req.get("returnUrl"));
		sb.append("&sign=" + sign);
		url = url + "?" + sb.toString();
		return url;
	}

	/**
	 * 获取签名字符串
	 * 
	 * @param parter
	 * @param type
	 * @param value
	 * @param orderid
	 * @param callbackurl
	 */
	private static String getSignstr(String parter, String type, String value, String orderid, String callbackurl) {
		StringBuffer signBuffer = new StringBuffer();
		signBuffer.append("parter=" + parter);
		signBuffer.append("&type=" + type);
		signBuffer.append("&value=" + value);
		signBuffer.append("&orderid=" + orderid);
		signBuffer.append("&callbackurl=" + callbackurl);
		return signBuffer.toString();
	}

	// 回调验签校验
	@Override
	public String callback(Map<String, String> request) {
		// TODO Auto-generated method stub
		StringBuffer signBuffer = new StringBuffer();
		signBuffer.append("orderid=" + request.get("orderid"));
		signBuffer.append("&opstate=" + request.get("opstate"));
		signBuffer.append("&ovalue=" + request.get("ovalue"));
		signBuffer.append(key);
		String sign = md5(signBuffer.toString());
		if (sign.equals(request.get("sign"))) {
			return "success";
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

		Map<String, String> map = new HashMap<String, String>();
		map.put("value", String.valueOf(payEntity.getAmount()));// 订单明细金额
		map.put("payerIP", payEntity.getIp());// 客户端ip
		map.put("orderid", payEntity.getOrderNo());// 订单号
		map.put("type", payEntity.getPayCode()); // 付款方支付方式 BANK_B2C网银
		map.put("returnUrl", payEntity.getRefererUrl());// 支付完成地址
		String html = bankPay(map);

		return PayUtil.returnWYPayJson("success", "form", html, pay_url, "");
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String userName = payEntity.getUsername();

		Map<String, String> scanMap = new HashMap<String, String>();
		Map<String, Object> json = new HashMap<String, Object>();
		scanMap.put("value", String.valueOf(payEntity.getAmount()));// 订单明细金额
		scanMap.put("payerIP", payEntity.getIp());// 客户端ip
		scanMap.put("orderid", payEntity.getOrderNo());// 订单号
		scanMap.put("type", payEntity.getPayCode()); // 付款方支付方式 BANK_B2C网银
		scanMap.put("returnUrl", payEntity.getRefererUrl());// 支付完成地址
		String retJson = pureScanPay(scanMap);
		return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no, retJson);
	}

	public static String md5(String strSrc) {
		String result = "";
		try {
			MessageDigest md5;
			try {
				md5 = MessageDigest.getInstance("MD5");
				md5.update((strSrc).getBytes("UTF-8"));
				byte b[] = md5.digest();

				int i;
				StringBuffer buf = new StringBuffer("");

				for (int offset = 0; offset < b.length; offset++) {
					i = b[offset];
					if (i < 0) {
						i += 256;
					}
					if (i < 16) {
						buf.append("0");
					}
					buf.append(Integer.toHexString(i));
				}
				result = buf.toString();
				return result;
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void main(String[] args) {
		String parter = "1787";
		String key = "fbe787a8def14ef98c62fa0e91312e9c";
		String type = "1007";
		String value = "100";
		String orderid = "SYF" + System.currentTimeMillis();
		String callbackurl = "http://localhost:85/JJF/PlatformPay/SYFNotify.do";
		String returnUrl = "http://localhost:81/JJF";
		String payerIp = "110.164.197.124";
		String url = "http://localhost:85/JJF/PlatformPay/SYFNotify.do";
		// String str = getSignstr(parter, type, value, orderid, callbackurl);
		// String sign = md5(str + key);
		StringBuffer sb = new StringBuffer();
		// sb.append("parter=" + parter);
		// sb.append("&type=" + type);
		// sb.append("&value=" + value);
		// sb.append("&orderid=" + orderid);
		// sb.append("&callbackurl=" + callbackurl);
		// sb.append("&parerIp=" + payerIp);
		// sb.append("&hrefbackurl=" + returnUrl);
		// sb.append("&sign=" + sign);
		// url = url + "?" + sb.toString();
		System.out.println(url);
		StringBuffer signBuffer = new StringBuffer();
		signBuffer.append("orderid=" + "SYFbl1201803092038382038388977");
		signBuffer.append("&opstate=" + "0");
		signBuffer.append("&ovalue=" + "20.09");
		// signBuffer.append("fbe787a8def14ef98c62fa0e91312e9c");
		String sign = md5(signBuffer.toString() + "fbe787a8def14ef98c62fa0e91312e9c");
		System.out.println(sign);
		signBuffer.append("&sign=" + sign);
		System.out.println(url + "?" + signBuffer.toString());
	}
}
