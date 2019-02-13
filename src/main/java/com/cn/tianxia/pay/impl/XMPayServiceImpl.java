package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.mkt.util.HttpUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.xm.util.HttpsUtil;
import com.cn.tianxia.pay.xm.util.MD5s;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;

public class XMPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(XMPayServiceImpl.class);

	private static String messageid;//
	private static String wapmessageid;//
	private static String branch_id;// 服务方平台分配的商户号
	private static String prod_name;// 订单标题，这个可能会显示在客户支付的页面，但不同的支付方式显示会有差异
	private static String prod_desc;// 产品描述
	private static String back_notify_url;// 后台通知url，
	private static String appKey;// 秘钥
	private static String url;// 请求地址

	public XMPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = new JSONObject().fromObject(pmap);
		if (null != pmap) {
			messageid = jo.get("messageid").toString();
			wapmessageid = jo.get("wapmessageid").toString();
			branch_id = jo.get("branch_id").toString();
			prod_name = jo.get("prod_name").toString();
			prod_desc = jo.get("prod_desc").toString();
			back_notify_url = jo.get("back_notify_url").toString();
			appKey = jo.get("appKey").toString();
			url = jo.get("url").toString();
		}
	}

	public static String CreateNoncestr(int length) {
		String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String res = "";
		for (int i = 0; i < length; i++) {
			Random rd = new Random();
			res = res + chars.charAt(rd.nextInt(chars.length() - 1));
		}
		return res;
	}

	public String ScanPay(Map<String, String> scanMap) {
		// String AppKey = "d61899da3d2f4b97a346e0ef7b3d51c6";//注：这个需要换成实际的商户密钥
		// String url = "https://www.xinmapay.com:7301/jhpayment";
		com.alibaba.fastjson.JSONObject reqData = new com.alibaba.fastjson.JSONObject();
		if ("true".equals(scanMap.get("mobile"))) {
			reqData.put("messageid", wapmessageid);
			reqData.put("client_ip", scanMap.get("payip"));
			reqData.put("pay_type",scanMap.get("pay_type") );//getPayType(scanMap.get("pay_type"))
			reqData.put("front_notify_url", scanMap.get("returnurl"));
		} else {
			reqData.put("messageid", messageid);
			reqData.put("pay_type", scanMap.get("pay_type"));
		}
		reqData.put("out_trade_no", scanMap.get("orderId")); // 商户方的流水号，在同个商户号下必须唯一
		reqData.put("back_notify_url", back_notify_url); // 商户的回调地址
		reqData.put("branch_id", branch_id); // 注：这个需要换成实际的商户号
		reqData.put("prod_name", prod_name);
		reqData.put("prod_desc", prod_desc);
		reqData.put("total_fee", Integer.parseInt(scanMap.get("total_fee")));
		reqData.put("nonce_str", CreateNoncestr(32));
		reqData = MD5s.sign(reqData, appKey);
		System.out.println("发送报文" + reqData.toString());
		byte[] resByte = HttpsUtil.httpsPost(url, reqData.toString());
		if (null == resByte) {
			return null;
		} else {
			try {
				System.out.println("返回数据:" + new String(resByte, "UTF-8"));
				com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(new String(resByte, "UTF-8"));
				if (jsonObject == null) {
					return null;
				}
				if ("00".equals(jsonObject.get("resultCode")) && "00".equals(jsonObject.get("resCode"))) {
					return jsonObject.get("payUrl").toString();
				} else {
					return null;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private Object getPayType(String str) {
		if ("10".equals(str))
			return "61";
		if ("20".equals(str))
			return "62";
		if ("50".equals(str))
			return "63";
		if ("40".equals(str))
			return "64";
		return null;
	}

	public static void main(String[] args) throws UnsupportedEncodingException {

		String AppKey = "d61899da3d2f4b97a346e0ef7b3d51c6";// 注：这个需要换成实际的商户密钥
		String url = "https://www.xinmapay.com:7301/jhpayment";
		com.alibaba.fastjson.JSONObject reqData = new com.alibaba.fastjson.JSONObject();
		reqData.put("messageid", "200001");
		reqData.put("out_trade_no", "XM"+System.currentTimeMillis()); // 商户方的流水号，在同个商户号下必须唯一
//		reqData.put("front_notify_url", "http://182.16.110.186:82");
		reqData.put("branch_id", "170600027527"); // 注：这个需要换成实际的商户号
		reqData.put("pay_type", "21");
		reqData.put("total_fee", 100000);
		reqData.put("prod_name", "测试支付");
		reqData.put("prod_desc", "测试支付描述");
		reqData.put("back_notify_url", "http://182.16.110.186:82/XPJ/PlatformPay/bankingNotify.do"); // 商户的回调地址
//		reqData.put("client_ip", "192.168.0.1");
		reqData.put("nonce_str", CreateNoncestr(32));
		reqData = MD5s.sign(reqData, AppKey);
		System.out.println("发送报文" + reqData.toString());
		byte[] resByte = HttpsUtil.httpsPost(url, reqData.toString());
		if (null == resByte) {
			System.out.println("返回报文为空");
		} else {
			System.out.println("返回数据:" + new String(resByte, "UTF-8"));
			com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(new String(resByte, "UTF-8"));
			if ("00".equals(jsonObject.get("resultCode")) && "00".equals(jsonObject.get("resCode"))) {
				System.out.println(jsonObject.get("payUrl").toString());
			}
		}
		// Map<String, String> infoMap = new HashMap<String,String>();
		// infoMap.put("createTime", "20170503180443");
		// infoMap.put("status", "02");
		// infoMap.put("nonceStr", "L7AaafgKohiQzsrmjKYFoegsfCI4YSlp");
		// infoMap.put("resultDesc", "成功");
		// infoMap.put("outTradeNo", "81205669007812");
		// infoMap.put("sign", "AB963F88B5D4BAD62CC991FF92061E8E");
		// infoMap.put("productDesc", "alipay");
		// infoMap.put("orderNo", "p2017050318044500004771");
		// infoMap.put("branchId", "170600027527");
		// infoMap.put("resultCode", "00");
		// infoMap.put("resCode", "00");
		// infoMap.put("payType", "20");
		// infoMap.put("resDesc", "成功");
		// infoMap.put("orderAmt", "1000");
		// String localsign = infoMap.remove("sign");
		// String noSignStr;
		// try {
		// noSignStr = MD5s.FormatBizQueryParaMap(infoMap, false);
		// String sign = MD5s.Sign(noSignStr, appKey);// 签名
		// // 制作签名
		// if (sign.equals(localsign)) {
		//// return "success";
		// System.out.println("校验成功");
		// }
		// System.out.println("校验失败"+sign+"==="+localsign);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }// 未签名的字符串
		//// return "";
	}

	@Override
	public String callback(Map<String, String> infoMap) {
		String localsign = infoMap.remove("sign");
		String noSignStr;
		try {
			noSignStr = MD5s.FormatBizQueryParaMap(infoMap, false);
			String sign = MD5s.Sign(noSignStr, appKey);// 签名
			// 制作签名
			if (sign.equals(localsign)) {
				return "success";
			}
		} catch (Exception e) {
			e.printStackTrace();
		} // 未签名的字符串
		return "";
	}

	public String bankPay(Map<String, String> payMap) {
		com.alibaba.fastjson.JSONObject reqData = new com.alibaba.fastjson.JSONObject();
		reqData.put("messageid", "200002");
		reqData.put("out_trade_no", payMap.get("orderId")); // 商户方的流水号，在同个商户号下必须唯一
		reqData.put("branch_id", branch_id); // 注：这个需要换成实际的商户号
		reqData.put("pay_type", payMap.get("pay_type"));
		reqData.put("total_fee", Integer.parseInt(payMap.get("total_fee")));
		reqData.put("prod_name", prod_name);
		reqData.put("prod_desc", prod_desc);
		reqData.put("back_notify_url", back_notify_url); // 商户的回调地址
		reqData.put("front_notify_url", payMap.get("returnurl"));
		reqData.put("bank_code", payMap.get("bank_code"));
		reqData.put("bank_flag", "0");
		reqData.put("nonce_str", CreateNoncestr(32));
		reqData = MD5s.sign(reqData, appKey);
		Map<String, String> myJson = JSONObject.fromObject(reqData);
		return HttpUtil.HtmlFrom(url, myJson);

		// System.out.println("发送报文" + reqData.toString());
		// byte[] resByte = HttpsUtil.httpsPost(url, reqData.toString());
		// if (null == resByte) {
		// return null;
		// } else {
		// try {
		// System.out.println("返回数据:" + new String(resByte, "UTF-8"));
		// com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(new
		// String(resByte, "UTF-8"));
		// if(jsonObject == null){
		// return null;
		// }
		// if("00".equals(jsonObject.get("resultCode")) &&
		// "00".equals(jsonObject.get("resCode"))){
		// return jsonObject.get("payUrl").toString();
		// }else{
		// return null;
		// }
		// } catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// }
		// }
		// return null;
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
		int int_amount = (int) (amount * 100);
		payMap.put("out_trade_no", order_no);
		payMap.put("total_fee", String.valueOf(int_amount));
		payMap.put("payip", ip);
		payMap.put("returnurl", refereUrl);
		payMap.put("bank_code", pay_code);
		payMap.put("pay_type", "30");
		String html = bankPay(payMap);
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
		int int_amount = (int) (amount * 100);
		payMap.put("orderId", order_no);
		payMap.put("total_fee", String.valueOf(int_amount));
		payMap.put("payip", ip);
		payMap.put("returnurl", refereUrl);
		payMap.put("pay_type", pay_code);
		payMap.put("mobile", mobile != null ? "true" : "false");// 是否WAP
		String qrcode = ScanPay(payMap);
		if (StringUtils.isNullOrEmpty(qrcode)) {
			logger.info("新马扫码接口获取二维码异常！");
			return PayUtil.returnPayJson("error", "2", "支付接口请求失败！", userName, amount, order_no, "");
		}
		if (StringUtils.isNullOrEmpty(mobile)) {
			return PayUtil.returnPayJson("success", "2", "支付接口请求成功！", userName, amount, order_no, qrcode);
		} else {
			return PayUtil.returnPayJson("success", "4", "支付接口请求成功！", userName, amount, order_no, qrcode);
		}
	}
}
