package com.cn.tianxia.pay.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.util.Md5Utils;
import com.cn.tianxia.bg.api.util.HashUtil;
import com.cn.tianxia.pay.dc.util.HttpClientUtils;
import com.cn.tianxia.pay.dc.util.SignUtil;
import com.google.gson.Gson;

import net.sf.json.JSONObject;

/**
 * 
 * @Description:TODO
 * 
 * @author:zouwei
 * 
 * @time:2017年7月15日 下午3:49:30
 * 
 */
public class TCPPayServiceImpl {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private static String productNo;// 通财平台填写的商品简码
	private static String appID;// 应用id
	private static String openKey;// 签名
	private static String userName;// 帐号
	private static String pay_url;// 支付网关地址

	public TCPPayServiceImpl(Map<String, String> pmap, String type) {
		if ("".equals(type) || null == type) {
			return;
		}
		JSONObject jo = new JSONObject().fromObject(pmap);
		if ("bank".equals(type)) {
			JSONObject bankJson = (JSONObject) jo.get(type);
			productNo = bankJson.get("productNo").toString();
			appID = bankJson.get("appID").toString();
			openKey = bankJson.get("openKey").toString();
			userName = bankJson.get("userName").toString();
			pay_url = bankJson.get("pay_url").toString();
		}
		if ("scan".equals(type)) {
			JSONObject bankJson = (JSONObject) jo.get(type);
			productNo = bankJson.get("productNo").toString();
			appID = bankJson.get("appID").toString();
			openKey = bankJson.get("openKey").toString();
			userName = bankJson.get("userName").toString();
			pay_url = bankJson.get("pay_url").toString();
		}
	}

	public static String pay(Map<String, String> map) {
		String return_str;
		Map<String, Object> payparam = new HashMap<String, Object>();
		Gson gson = new Gson();
		// 支付类型.网银、微信、支付宝
		String[] payType = new String[] { "BGATWAY", "WQR", "ZQR" };

		payparam.put("payType", map.get("payTpye").toString());
		payparam.put("productNo", productNo);
		payparam.put("amount", Double.parseDouble(map.get("amount").toString()));
		payparam.put("orderNo", map.get("orderNo").toString());
		payparam.put("userName", userName);
		payparam.put("returnurl", map.get("returnurl").toString());

		Map<String, String> extParam = new HashMap<>();
		// 网银支付添加参数
		if (payType[0].equals(map.get("payTpye").toString())) {
			extParam.put("bankType", map.get("bankType").toString());
			extParam.put("BankAcctType", "11");
		}
		// String appID = appID;
		// String openKey = openKey;
		String ext = gson.toJson(extParam);
		String json = gson.toJson(payparam);
		String sign = SignUtil.getSign(SignUtil.getHmacSHA1(appID + "&" + json + "&" + ext, openKey));

		Map<String, String> param = new HashMap<>();

		param.put("appID", appID);
		param.put("ext", ext);
		param.put("json", json);
		param.put("sign", sign);

		// System.out.println(appID);
		// System.out.println(ext);
		// System.out.println(json);
		// System.out.println(sign);
		// System.out.println("http://testpay.thinksway.com/pay/order?appID=" +
		// appID +
		// "&json=" + json + "&ext=" + ext + "&sign=" + sign);

		try {
			return_str = HttpClientUtils.post(pay_url, param);
		} catch (Exception e) {
			return_str = "error";
		}
		return return_str;
	}

	public String tcpPaycallback(HttpServletRequest req, HttpServletResponse resp) {
		try {
			req.setCharacterEncoding("utf-8");
			resp.setCharacterEncoding("utf-8");
			resp.setHeader("Content-type", "text/html;charset=UTF-8");

			String resString = req.getQueryString();

			String respString = "fail";
			String payNo = req.getParameter("payNo");
			String orderNo = req.getParameter("orderNo");
			String userName = req.getParameter("userName");
			String result = req.getParameter("result");
			String amount = req.getParameter("amount");
			String pay_time = req.getParameter("pay_time");
			String pay_channel = req.getParameter("pay_channel");
//			String openkey = req.getParameter("openkey");
			String sign = req.getParameter("sign");
			String sign2 = HashUtil.md5Hex(orderNo + userName + result + amount + pay_time + pay_channel + openKey);
			logger.info("verify_sign :" + sign + "  verify_MD5:" + sign2);
			if (!sign2.equalsIgnoreCase(sign)) {
				respString = "fail";
				return respString;
			}

			if (result != null && "1".equals(result)) {
				respString = "ok";
				return respString;
			}

		} catch (Exception e) {
			return "fail";
		}
		return "fail";
	}

	public static void testCallback() {
		String payNo = "1234343";
		String orderNo = "TCPbl1201708041036281036283039";
		String userName = "123";
		String result = "1";
		String amount = "100";
		String pay_time = "2017-09-01";
		String pay_channel = "123";
		String openkey = "961f921879ca6650bf9e6b03bbecd43e";
		String sign = "";
		Map<String, String> map = new HashMap<String, String>();
		map.put("payNo", payNo);
		map.put("orderNo", orderNo);
		map.put("userName", userName);
		map.put("result", result);
		map.put("amount", amount);
		map.put("pay_time", pay_time);
		map.put("pay_channel", pay_channel);
		map.put("openkey", openkey);
		sign = HashUtil.md5Hex(orderNo + userName + result + amount + pay_time + pay_channel + openkey);
		map.put("sign", sign);
		try {
			String url = "http://localhost:8080/XPJ/PlatformPay/bankingNotify.do";
			HttpClientUtils.post(url, map);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// Map<String, String> payparam = new HashMap<String, String>();
		// payparam.put("payTpye", "BGATWAY");
		// payparam.put("bankType", "ICBC");
		// pay(payparam);
		testCallback();
		// 网银网关支付 配置参数设置
		// Map<String, String> map = new HashMap<String, String>();
		// map.put("productNo", "P000134");
		// map.put("appID", "A5C6680BF6668A438AE4533ED300ABD09");
		// map.put("openKey", "961f921879ca6650bf9e6b03bbecd43e");
		// map.put("userName", "hgyx13@gmail.com");
		// map.put("pay_url", "https://pay.tongcaipay.com/pay/order");
		// // 主动扫码支付  配置参数设置
		// Map<String, String> map1 = new HashMap<String, String>();
		// map1.put("productNo", "P000135");
		// map1.put("appID", "AB8A415E823FE7420B4E4806C89213656");
		// map1.put("openKey", "900cd89eb8325b376d08a4e1e3e78d62");
		// map1.put("userName", "hgyx13@gmail.com");
		// map1.put("pay_url", "https://pay.tongcaipay.com/pay/order");
		// JSONObject json1 = new JSONObject();
		// json1.put("bank", map);
		// json1.put("scan", map1);
		// System.out.println(json1.toString());
	}
}
