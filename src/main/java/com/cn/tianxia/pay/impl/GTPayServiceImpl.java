package com.cn.tianxia.pay.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.gt.util.MD5Util;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;

public class GTPayServiceImpl implements PayService {

	private final static Logger log = LoggerFactory.getLogger(GTPayServiceImpl.class);

	private static String partner;// 商户ID
	private static String callbackurl;// 下行异步通知的地址，需要以http://开头且没有任何参数，
	private static String keyValue; // 商户密钥
									//
	private static String annulCardReqURL; // 请求地址https://wgtj.gaotongpay.com/PayBank.aspx

	public GTPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = new JSONObject().fromObject(pmap);
		if (null != pmap) {
			partner = jo.get("partner").toString();
			callbackurl = jo.get("callbackurl").toString();
			keyValue = jo.get("keyValue").toString();
			annulCardReqURL = jo.get("annulCardReqURL").toString();
		}
	}

	/**
	 * 消费请求 该方法是根据《易宝支付非银行卡支付专业版接口文档 v3.0》对发起支付请求进行的封装
	 * 
	 * @return
	 */
	public String ScanPay(Map<String, String> scanMap) {
//		if ("true".equals(scanMap.get("mobile"))) {
//			if ("WEIXIN".equals(scanMap.get("banktype"))) {
//				scanMap.put("banktype", "WEIXINWAP");
//			} else if ("ALIPAY".equals(scanMap.get("banktype"))) {
//				scanMap.put("banktype", "ALIPAYWAP");
//			} else if ("QQPAY".equals(scanMap.get("banktype"))) {
//				scanMap.put("banktype", "QQPAYWAP");
//			}
//		}
		// 生成hmac，保证交易信息不被篡改,关于hmac详见《易宝支付非银行卡支付专业版接口文档 v3.0》
		String sign = "";
		// 拼接字符串
		String tempStr = "partner=" + partner + "&banktype=" + scanMap.get("banktype") + "&paymoney="
				+ scanMap.get("paymoney") + "&ordernumber=" + scanMap.get("ordernumber") + "&callbackurl=" + callbackurl
				+ keyValue;
		sign = MD5Util.string2MD5(tempStr);
		// 封装请求参数，参数说明详见《易宝支付非银行卡支付专业版接口文档 v3.0》
		Map<String, String> reqParams = new HashMap<String, String>();
		reqParams.put("partner", partner);
		reqParams.put("banktype", scanMap.get("banktype"));
		reqParams.put("paymoney", scanMap.get("paymoney"));
		reqParams.put("ordernumber", scanMap.get("ordernumber"));
		reqParams.put("callbackurl", callbackurl);
		reqParams.put("hrefbackurl", scanMap.get("returnurl"));
		reqParams.put("sign", sign);
		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ annulCardReqURL + "\">";
		for (String key : reqParams.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + reqParams.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";

		String html = FormString;
		log.info("高通支付表单:" + html);
		return html;
	}

	@Override
	public String callback(Map<String, String> infoMap) {
		// 校验MD5码是不是正确的
		String tempStr = "partner=" + partner + "&ordernumber=" + infoMap.get("ordernumber") + "&orderstatus="
				+ infoMap.get("orderstatus") + "&paymoney=" + infoMap.get("paymoney") + keyValue;
		String newMD5 = MD5Util.string2MD5(tempStr);
		if (infoMap.get("sign").equals(newMD5)) {
			log.info("sign校验成功");
			return "success";
		}
		return null;
	}

	public static void main(String[] args) {
		Map<String, String> map = new HashMap<>();
		String ss = "partner=" + partner + "&ordernumber=10171225161614096286&orderstatus=1&paymoney=200.0000";
		System.out.println(ss);
		String newMD5 = MD5Util.string2MD5(ss + keyValue);

		System.out.println(newMD5);
		System.out.println(ss + "&sysnumber=1234&sign=" + newMD5);
		//
		// String str[] = ss.split("&");
		//
		// for (String string : str) {
		// String[] strValue = string.split("=");
		// map.put(strValue[0], strValue[1]);
		// System.out.println("name:"+strValue[0] +" value:"+strValue[1]);
		// }
		//
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
		payMap.put("ordernumber", order_no);
		payMap.put("paymoney", String.valueOf(amount));
		payMap.put("payip", ip);
		payMap.put("returnurl", refereUrl);
		payMap.put("banktype", pay_code);
		String html = ScanPay(payMap);

		return PayUtil.returnWYPayJson("success", "jsp", html, pay_url, "paytest");
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
		payMap.put("banktype", pay_code);
		payMap.put("ordernumber", order_no);
		payMap.put("paymoney", String.valueOf(amount));
		payMap.put("payip", ip);
		payMap.put("returnurl", refereUrl);
//		payMap.put("mobile", mobile != null ? "true" : "false");// 是否WAP
		String html = ScanPay(payMap);
		if (StringUtils.isNullOrEmpty(html)) {
			return PayUtil.returnPayJson("error", "1", "支付接口请求失败！", userName, amount, order_no, "");
		}
		return PayUtil.returnPayJson("success", "1", "支付接口请求成功！", userName, amount, order_no, html);
	}
}
