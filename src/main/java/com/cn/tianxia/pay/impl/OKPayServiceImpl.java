package com.cn.tianxia.pay.impl;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.jf.util.MD5;
import com.cn.tianxia.pay.mkt.util.HttpUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;

public class OKPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(OKPayServiceImpl.class);
	// 支付地址
	private static String gateway;
	// 版本号
	private static String version;
	// 异步通知地址
	private static String notifyurl;
	// md5 key
	private static String key;
	// 商户号
	private static String partner;

	public OKPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			gateway = jo.get("gateway").toString();
			version = jo.get("version").toString();
			notifyurl = jo.get("notifyurl").toString();
			key = jo.get("key").toString();
			partner = jo.get("partner").toString();
		}
	}

	/**
	 * ok支付
	 * 
	 * @param payMap
	 * @return
	 */
	public String pay(Map<String, String> payMap) {
		String orderid = formatString(payMap.get("orderid"));
		String payamount = formatString(payMap.get("payamount"));
		String payip = formatString(payMap.get("payip"));
		String returnurl = formatString(payMap.get("returnurl"));
		String paytype = formatString(payMap.get("paytype"));
		String remark = formatString("");

		StringBuffer paramsStr = new StringBuffer();
		paramsStr.append("version=").append(version).append("&");
		paramsStr.append("partner=").append(partner).append("&");
		paramsStr.append("orderid=").append(orderid).append("&");
		paramsStr.append("payamount=").append(payamount).append("&");
		paramsStr.append("payip=").append(payip).append("&");
		paramsStr.append("notifyurl=").append(notifyurl).append("&");
		paramsStr.append("returnurl=").append(returnurl).append("&");
		paramsStr.append("paytype=").append(paytype).append("&");
		paramsStr.append("remark=").append(remark).append("&");
		paramsStr.append("key=").append(key);

		logger.info("签名字符:" + paramsStr.toString());
		String sign = MD5.GetMD5Code(paramsStr.toString());

		Map<String, String> paramMap = new LinkedHashMap<>();
		paramMap.put("version", version);
		paramMap.put("partner", partner);
		paramMap.put("orderid", orderid);
		paramMap.put("payamount", payamount);
		paramMap.put("payip", payip);
		paramMap.put("notifyurl", notifyurl);
		paramMap.put("returnurl", returnurl);
		paramMap.put("paytype", paytype);
		paramMap.put("remark", remark);
		paramMap.put("sign", sign);
		return HttpUtil.HtmlFrom(gateway, paramMap);
	}

	/**
	 * formatString() : 字符串格式化方法
	 */
	public static String formatString(String text) {
		return (text == null) ? "" : text.trim();
	}

	/**
	 * 异步通知
	 * 
	 * @param reqMap
	 */
	@Override
	public String callback(Map<String, String> reqMap) {
		String version = formatString(reqMap.get("version"));
		String partner = formatString(reqMap.get("partner"));
		String orderid = formatString(reqMap.get("orderid"));
		String payamount = formatString(reqMap.get("payamount"));
		String opstate = formatString(reqMap.get("opstate"));
		String orderno = formatString(reqMap.get("orderno"));
		String okfpaytime = formatString(reqMap.get("okfpaytime"));
		String message = formatString(reqMap.get("message"));
		String paytype = formatString(reqMap.get("paytype"));
		String remark = formatString(reqMap.get("remark"));
		String sign = formatString(reqMap.get("sign"));

		StringBuffer paramsStr = new StringBuffer();
		paramsStr.append("version=").append(version).append("&");
		paramsStr.append("partner=").append(partner).append("&");
		paramsStr.append("orderid=").append(orderid).append("&");
		paramsStr.append("payamount=").append(payamount).append("&");
		paramsStr.append("opstate=").append(opstate).append("&");
		paramsStr.append("orderno=").append(orderno).append("&");
		paramsStr.append("okfpaytime=").append(okfpaytime).append("&");
		paramsStr.append("message=").append(message).append("&");
		paramsStr.append("paytype=").append(paytype).append("&");
		paramsStr.append("remark=").append(remark).append("&");
		paramsStr.append("key=").append(key);

		logger.info("回调签名字符:" + paramsStr.toString());
		try {
			String localSign = MD5.GetMD5Code(paramsStr.toString());
			logger.info("本地sign:" + localSign + "   支付商sign:" + sign);
			if (sign.equals(localSign)) {
				logger.info("ok支付签名验证成功！");
				return "success";
			}
		} catch (Exception e) {
			logger.info("ok支付签名验证失败！");
			e.printStackTrace();
			return "";
		}
		logger.info("ok支付签名验证失败！");
		return "";
	}

	public static void main(String[] args) {
		String gateway = "https://gateway.okfpay.com/Gate/payindex.aspx";
		String version = "1.0";
		String partner = "6662";
		String notifyurl = "http://182.16.110.186:8080/XPJ/PlatformPay/OKNotify.do";
		String key = "5af562c403194b908b75511c914ab3eb";
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("gateway", gateway);
		paramMap.put("version", version);
		paramMap.put("notifyurl", notifyurl);
		paramMap.put("key", key);
		paramMap.put("partner", partner);
		logger.info("JSON配置:" + JSONObject.fromObject(paramMap));
		OKPayServiceImpl ok = new OKPayServiceImpl(paramMap);
		String orderid = formatString("TX" + System.currentTimeMillis());
		String payamount = formatString("2");
		String payip = formatString("58.64.40.22");
		String returnurl = formatString("https://www.baidu.com/");
		String paytype = formatString("TENPAY");
		Map<String, String> payMap = new HashMap<>();
		payMap.put("orderid", orderid);
		payMap.put("payamount", payamount);
		payMap.put("payip", payip);
		payMap.put("returnurl", returnurl);
		payMap.put("paytype", paytype);
		logger.info(ok.pay(payMap));
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

		Map<String, String> payMap = new HashMap<>();
		payMap.put("orderid", order_no);
		payMap.put("payamount", String.valueOf(amount));
		payMap.put("payip", ip);
		payMap.put("returnurl", refereUrl);
		payMap.put("paytype", pay_code);
		String html = pay(payMap);
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

		Map<String, String> payMap = new HashMap<>();
		payMap.put("orderid", order_no);
		payMap.put("payamount", String.valueOf(amount));
		payMap.put("payip", ip);
		payMap.put("returnurl", refereUrl);
		payMap.put("paytype", pay_code);
		String html = pay(payMap);

		return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, amount, order_no, html);
	}
}
