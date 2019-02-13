package com.cn.tianxia.pay.impl;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.dx.util.StringUtil;
import com.cn.tianxia.pay.dc.util.HttpClientUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.qft.util.Md5;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.DESEncrypt;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;

/**
 * 
 * @Description:TODO
 * 
 * @author:zouwei
 * 
 * @time:2017年8月6日 下午4:17:20
 * 
 */
public class QFTPayServiceImpl implements PayService {

	private String merchantNo;
	private String appNo;
	private String goodsTitle;
	private String currency;
	private String notifyUrl;//
	private String url;//
	private String urlh5;//
	private String wyurl;//
	private String key;//
	private final static Logger logger = LoggerFactory.getLogger(QFTPayServiceImpl.class);

	public QFTPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			merchantNo = jo.get("merchantNo").toString();
			appNo = jo.get("appNo").toString();
			goodsTitle = jo.get("goodsTitle").toString();
			currency = jo.get("currency").toString();
			notifyUrl = jo.get("notifyUrl").toString();
			url = jo.get("url").toString();
			key = jo.get("key").toString();
			urlh5 = jo.get("urlh5").toString();
			wyurl = jo.get("wyurl").toString();
		}
	}

	/**
	 * 扫码支付
	 * 
	 * @return
	 */
	public JSONObject scanPay(Map<String, String> scanMap) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("merchantNo", merchantNo);
		map.put("orderNo", scanMap.get("orderNo"));
		map.put("appNo", appNo);
		map.put("currency", currency);
		map.put("goodsTitle", goodsTitle);
		map.put("amount", scanMap.get("amount"));
		map.put("payType", scanMap.get("payType"));
		map.put("payTime", scanMap.get("payTime"));
		map.put("notifyUrl", notifyUrl);
		map.put("timestamp", scanMap.get("timestamp"));
		String signStr = getSign(map, key);
		String sign = DESEncrypt.getMd5(signStr);
		Map<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("amount", map.get("amount"));
		hashMap.put("appNo", map.get("appNo"));
		hashMap.put("currency", map.get("currency"));
		hashMap.put("goodsTitle", map.get("goodsTitle"));
		hashMap.put("merchantNo", map.get("merchantNo"));
		hashMap.put("notifyUrl", map.get("notifyUrl"));
		hashMap.put("orderNo", map.get("orderNo"));
		hashMap.put("payTime", map.get("payTime"));
		hashMap.put("payType", map.get("payType"));
		hashMap.put("sign", sign);
		hashMap.put("timestamp", map.get("timestamp"));
		JSONObject jsonObject = JSONObject.fromObject(hashMap);
		System.out.println(jsonObject.toString());
		String res = HttpClientUtil.doPost(url, jsonObject.toString(), "UTF-8", "application/json");
		System.out.println("响应参数：" + res);
		JSONObject json = JSONObject.fromObject(res);
		if ("000000".equals(json.get("code"))) {
			logger.info("启付通接口调用成功");
			JSONObject jsonto = JSONObject.fromObject(json.get("data"));
			return getReturnJson("success", jsonto.get("codeUrl").toString(), "二维码获取成功！");
		} else {
			return getReturnJson("error", "", json.get("errMsg").toString());
		}
	}

	public String scanPayH5(Map<String, String> scanMap) {
		// TODO Auto-generated method stub
		Map<String, String> map = new HashMap<String, String>();
		map.put("merchantNo", merchantNo);
		map.put("orderNo", scanMap.get("orderNo"));
		map.put("appNo", appNo);
		map.put("currency", currency);
		map.put("goodsTitle", goodsTitle);
		map.put("amount", scanMap.get("amount"));
		map.put("payType", scanMap.get("payType"));
		map.put("notifyUrl", notifyUrl);
		map.put("timestamp", scanMap.get("timestamp"));
		map.put("clientIp", scanMap.get("clientIp"));
		String signStr = getSign(map, key);
		String sign = DESEncrypt.getMd5(signStr);
		Map<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("amount", map.get("amount"));
		hashMap.put("appNo", map.get("appNo"));
		hashMap.put("currency", map.get("currency"));
		hashMap.put("clientIp", map.get("clientIp"));
		hashMap.put("goodsTitle", map.get("goodsTitle"));
		hashMap.put("merchantNo", map.get("merchantNo"));
		hashMap.put("notifyUrl", map.get("notifyUrl"));
		hashMap.put("orderNo", map.get("orderNo"));
		hashMap.put("payType", map.get("payType"));
		hashMap.put("sign", sign);
		hashMap.put("timestamp", map.get("timestamp"));

		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ urlh5 + "\">";
		for (String key : hashMap.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + hashMap.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";
		String html = FormString;
		System.out.println(html);
		return html;

	}

	public String bankPay(Map<String, String> scanMap) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("amount", scanMap.get("amount"));
		map.put("appNo", appNo);
		map.put("bankCode", scanMap.get("bankCode"));
		map.put("callBackUrl", scanMap.get("returnUrl"));
		map.put("cardType", "1");
		map.put("currency", currency);
		map.put("goodsTitle", goodsTitle);
		map.put("merchantNo", merchantNo);
		map.put("notifyUrl", notifyUrl);
		map.put("orderNo", scanMap.get("orderNo"));
		map.put("payType", "gateway");
		String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		map.put("timestamp", timestamp);
		String signStr = getSign(map, key);
		String sign = DESEncrypt.getMd5(signStr);
		Map<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("amount", map.get("amount"));
		hashMap.put("appNo", map.get("appNo"));
		hashMap.put("bankCode", map.get("bankCode"));
		hashMap.put("callBackUrl", map.get("callBackUrl"));
		hashMap.put("cardType", map.get("cardType"));
		hashMap.put("currency", map.get("currency"));
		hashMap.put("goodsTitle", map.get("goodsTitle"));
		hashMap.put("merchantNo", map.get("merchantNo"));
		hashMap.put("notifyUrl", map.get("notifyUrl"));
		hashMap.put("orderNo", map.get("orderNo"));
		hashMap.put("payType", map.get("payType"));
		hashMap.put("sign", sign);
		hashMap.put("timestamp", map.get("timestamp"));
		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ wyurl + "\">";
		for (String key : hashMap.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + hashMap.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";
		String html = FormString;
		System.out.println(html);
		return html;
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
		json.put("msg", msg);
		return json;
	}

	public static String getSign(Map<String, String> map, String sign) {
		Set<String> set = new TreeSet<String>();
		StringBuffer sb = new StringBuffer();
		for (String s : map.keySet()) {
			set.add(s);
		}
		int i = 0;
		for (String s : set) {
			if (i < 1) {
				sb.append(s + "=" + map.get(s));
			} else {
				sb.append("&" + s + "=" + map.get(s));
			}
			i++;
		}
		sb.append(sign);
		System.out.println(sb.toString());
		return sb.toString();
	}

	
	@Override
	public String callback(Map<String, String> infoMap) {
		String sign = infoMap.get("sign");
		infoMap.remove("sign");
		String signStr = getSign(infoMap, key);
		String sig = DESEncrypt.getMd5(signStr);
		if (sign.equals(sig)) {
			return "success";
		}
		return "";
	}

	public static void main(String[] args) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("merchantNo", "ME0000000151");
		map.put("orderNo", "QFThg1201802171616041616045629");
		map.put("amount", "100");
		map.put("status", "1");
		map.put("transTime", "20180217161548");
		String signStr = getSign(map, "a5834f6a4577b8377df982b3ee6094a2");
		String sig = DESEncrypt.getMd5(signStr);
		System.out.println(sig);
		// http://localhost:82/JJF/PlatformPay/QFTNotify.do?merchantNo=ME0000000151&orderNo=QFTtxk201802151533121533129182&amount=1000&status=1&transTime=2018-02-15
		// 16:33:12&sign=8BA5D63158E6F21B96109A82E7A130EB
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

		String int_amount = String.valueOf(amount * 100);
		if (int_amount.indexOf(".") >= 0) {
			int_amount = int_amount.substring(0, int_amount.indexOf("."));
		}
		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("orderNo", order_no);
		scanMap.put("amount", int_amount);
		scanMap.put("bankCode", pay_code);
		scanMap.put("returnUrl", refereUrl);

		String html = bankPay(scanMap);
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

		int int_amount = (int) (amount * 100);
		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("orderNo", order_no);
		scanMap.put("amount", String.valueOf(int_amount));
		scanMap.put("payType", pay_code);
		String payTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		scanMap.put("payTime", payTime);
		scanMap.put("timestamp", timestamp);
		scanMap.put("returnUrl", refereUrl);
		JSONObject rjson = null;
		String html = "";
		// pc端
		if (StringUtils.isNullOrEmpty(mobile)) {
			rjson = scanPay(scanMap);
		} else {
			// 手机端 只支持微信h5
			scanMap.put("clientIp", ip);
			html = scanPayH5(scanMap);
		}
		// 手机 or pc 返回类型
		if (!StringUtils.isNullOrEmpty(mobile)) {
			return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, amount, order_no, html);
		} else {
			if (!"success".equals(rjson.getString("status"))) {
				return PayUtil.returnPayJson("error", "2", rjson.getString("msg"), userName, amount, order_no, "");
			}
			String qrcode = rjson.getString("qrCode");
			if (rjson.containsKey("qrCode") && !"null".equals(qrcode) && !StringUtils.isNullOrEmpty(qrcode)) {
				return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no, qrcode);
			} else {
				return PayUtil.returnPayJson("error", "2", rjson.toString(), userName, amount, order_no, "");
			}

		}
	}

	// public static void main(String[] args) {
	// Map<String, String> map = new HashMap<String, String>();
	// map.put("merchantNo", "ME0000000151");
	// map.put("orderNo", "QFT" + System.currentTimeMillis() + "");
	// map.put("appNo", "8a614939cabbb243420ecbbfce73b5e0");
	// map.put("currency", "CNY");
	// map.put("goodsTitle", "电脑");
	// map.put("amount", "100");
	// map.put("payType", "wechat_h5");
	// map.put("notifyUrl","http://182.16.110.186:8080/XPJ/PlatformPay/GCNotify.do");
	// String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new
	// Date());
	// map.put("timestamp", timestamp);
	// map.put("clientIp","110.164.197.124");
	// String signStr = getSign(map,"a5834f6a4577b8377df982b3ee6094a2");
	// String sign = Md5.utf8(signStr);
	// Map<String, String> hashMap = new HashMap<String, String>();
	// hashMap.put("amount", map.get("amount"));
	// hashMap.put("appNo", map.get("appNo"));
	// hashMap.put("currency", map.get("currency"));
	// hashMap.put("clientIp",map.get("clientIp"));
	// hashMap.put("goodsTitle", map.get("goodsTitle"));
	// hashMap.put("merchantNo", map.get("merchantNo"));
	// hashMap.put("notifyUrl", map.get("notifyUrl"));
	// hashMap.put("orderNo", map.get("orderNo"));
	// hashMap.put("payType", map.get("payType"));
	// hashMap.put("sign", sign);
	// hashMap.put("timestamp", map.get("timestamp"));
	//
	// String FormString = "<body
	// onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form
	// id=\"actform\" name=\"actform\" method=\"post\" action=\""
	// + "http://www.dulpay.com/api/pay/h5" + "\">";
	// for (String key : hashMap.keySet()) {
	// FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" +
	// hashMap.get(key) + "'>\r\n";
	// }
	// FormString += "</form></body>";
	// String html = FormString;
	// System.out.println(html);
	//
	//
	//

	// Map<String, String> map = new HashMap<String, String>();
	// map.put("amount", "100");
	// map.put("appNo", "8a614939cabbb243420ecbbfce73b5e0");
	// map.put("bankCode","102");
	// map.put("callBackUrl", "http://182.16.110.186:8080");
	// map.put("cardType","1");
	// map.put("currency", "CNY");
	// map.put("goodsTitle", "电脑");
	// map.put("merchantNo", "ME0000000151");
	// map.put("notifyUrl",
	// "http://182.16.110.186:8080/XPJ/PlatformPay/GCNotify.do");
	// map.put("orderNo", "QFT" + System.currentTimeMillis() + "");
	// map.put("payType", "gateway");
	// String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new
	// Date());
	// map.put("timestamp", timestamp);
	// String signStr = getSign(map,"a5834f6a4577b8377df982b3ee6094a2");
	// String sign = DESEncrypt.getMd5(signStr);
	// Map<String, String> hashMap = new HashMap<String, String>();
	// hashMap.put("amount", map.get("amount"));
	// hashMap.put("appNo", map.get("appNo"));
	// hashMap.put("bankCode", map.get("bankCode"));
	// hashMap.put("callBackUrl", map.get("callBackUrl"));
	// hashMap.put("cardType", map.get("cardType"));
	// hashMap.put("currency", map.get("currency"));
	// hashMap.put("goodsTitle", map.get("goodsTitle"));
	// hashMap.put("merchantNo",map.get("merchantNo"));
	// hashMap.put("notifyUrl", map.get("notifyUrl"));
	// hashMap.put("orderNo", map.get("orderNo"));
	// hashMap.put("payType", map.get("payType"));
	// hashMap.put("sign", sign);
	// hashMap.put("timestamp", map.get("timestamp"));
	// String FormString = "<body
	// onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form
	// id=\"actform\" name=\"actform\" method=\"post\" action=\""
	// + "http://www.dulpay.com/api/pay/net" + "\">";
	// for (String key : hashMap.keySet()) {
	// FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" +
	// hashMap.get(key) + "'>\r\n";
	// }
	// FormString += "</form></body>";
	// String html = FormString;
	// System.out.println(html);
	//
	// }
	// public static void main1(String[] args) {
	// Map<String, String> map = new HashMap<String, String>();
	// map.put("merchantNo", "ME0000000151");
	// map.put("orderNo", "QFT" + System.currentTimeMillis() + "");
	// map.put("appNo", "8a614939cabbb243420ecbbfce73b5e0");
	// map.put("currency", "CNY");
	// map.put("goodsTitle", "电脑");
	// map.put("amount", "100");
	// map.put("payType", "wechat");
	// String payTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new
	// Date());
	// map.put("payTime", payTime);
	// map.put("notifyUrl",
	// "http://182.16.110.186:8080/XPJ/PlatformPay/GCNotify.do");
	// String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new
	// Date());
	// map.put("timestamp", timestamp);
	// map.put("randomStr", "5niugh");
	// map.put("remark", "remark");
	// String signStr = getSign(map,key);
	// String sign = DESEncrypt.getMd5(signStr);
	// Map<String, String> hashMap = new HashMap<String, String>();
	// hashMap.put("amount", map.get("amount"));
	// hashMap.put("appNo", map.get("appNo"));
	// hashMap.put("currency", map.get("currency"));
	// hashMap.put("goodsTitle", map.get("goodsTitle"));
	// hashMap.put("merchantNo", map.get("merchantNo"));
	// hashMap.put("notifyUrl", map.get("notifyUrl"));
	// hashMap.put("orderNo", map.get("orderNo"));
	// hashMap.put("payTime",map.get("payTime"));
	// hashMap.put("payType", map.get("payType"));
	// hashMap.put("randomStr", map.get("randomStr"));
	// hashMap.put("remark", map.get("remark"));
	// hashMap.put("sign", sign);
	// hashMap.put("timestamp", map.get("timestamp"));
	// JSONObject jsonObject = JSONObject.fromObject(hashMap);
	// System.out.println(jsonObject.toString());
	// String res = HttpClientUtil.doPost("http://www.dulpay.com/api/pay/code",
	// jsonObject.toString(), "UTF-8",
	// "application/json");
	// JSONObject json = JSONObject.fromObject(res);
	// if("000000".equals(json.get("code"))){
	// JSONObject jsonto = JSONObject.fromObject(json.get("data"));
	// System.out.println(jsonto.get("codeUrl"));
	// }
	//
	// System.out.println("响应参数："+res);
	// }
}
