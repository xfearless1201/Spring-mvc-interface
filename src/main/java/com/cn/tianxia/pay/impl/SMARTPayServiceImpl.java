package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.FrameworkServlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.json.JSONUtils;
import com.cn.tianxia.pay.dc.util.HttpClientUtil;
import com.cn.tianxia.pay.dc.util.HttpClientUtils;
import com.cn.tianxia.pay.dc.util.MD5;
import com.cn.tianxia.pay.dc.util.MerchSdkSign;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.FileLog;
import com.cn.tianxia.util.IPTools;
import com.cn.tianxia.util.SSLClient;

/**
 * 
 * @Description:TODO
 * 
 * @author:zouwei
 * 
 * @time:2017年8月6日 下午4:17:20
 * 
 */
public class SMARTPayServiceImpl implements PayService {

	private String version;
	private String customerid;
	private String url;
	private String notifyurl;// 目前只支持 02- UTF-8
	private String key; // 版本号
	private String rmk; // 版本号

	private final static Logger logger = LoggerFactory.getLogger(SMARTPayServiceImpl.class);

	public SMARTPayServiceImpl(Map<String, String> pmap) {
		net.sf.json.JSONObject jo = new net.sf.json.JSONObject().fromObject(pmap);
		if (null != pmap) {
			version = jo.get("version").toString();
			customerid = jo.get("customerid").toString();
			url = jo.get("url").toString();
			notifyurl = jo.get("notifyurl").toString();
			key = jo.get("key").toString();
			try {
				rmk = jo.get("rmk").toString();
			} catch (Exception e) {
				rmk = "0";
			}
		}
	}

	/**
	 * 扫码支付
	 * 
	 * @return
	 */
	public String scanPay(Map<String, String> scanMap) {
		String sdorderno = scanMap.get("sdorderno");// "XM"+new
													// Date().getTime();
		String userid = sdorderno.substring(sdorderno.length() - 6, sdorderno.length() - 1);
		String total_fee = scanMap.get("total_fee");
		String paytype = "";
		if ("1".equals(rmk)) {
			paytype = "alipay";
		} else {
			paytype = scanMap.get("paytype");
		}
		String returnurl = scanMap.get("returnUrl");
		String bankcode = "";
		String remark = "";
		String sign = "";
		String s = "version=" + version + "&customerid=" + customerid + "&userid=" + userid + "&total_fee=" + total_fee
				+ "&sdorderno=" + sdorderno + "&notifyurl=" + notifyurl + "&returnurl=" + returnurl + "&" + key;
		sign = md5(s);
		System.out.println("生成sign值：" + sign);
		url = url + "?version=" + version + "&customerid=" + customerid + "&sdorderno=" + sdorderno + "&userid="
				+ userid + "&total_fee=" + total_fee + "&paytype=" + paytype + "&bankcode=" + bankcode + "&notifyurl="
				+ notifyurl + "&returnurl=" + returnurl + "&remark=" + remark + "&sign=" + sign;
		return url;
	}

	public boolean check(String int_amount) {
		String am = "10,20,50,100,200,500,800,1000,1200,1200,1500,2000,3000";
		String amStr[] = am.split(",");
		for (int i = 0; i < amStr.length; i++) {
			if (int_amount.equals(amStr[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String callback(Map<String, String> infoMap) {
		String customerid = infoMap.get("customerid");
		String status = infoMap.get("status");
		String sdpayno = infoMap.get("sdpayno");
		String sdorderno = infoMap.get("sdorderno");
		String total_fee = infoMap.get("total_fee");
		String paytype = infoMap.get("paytype");
		String signStr = "customerid=" + customerid + "&status=" + status + "&sdpayno=" + sdpayno + "&sdorderno="
				+ sdorderno + "&total_fee=" + total_fee + "&paytype=" + paytype + "&" + key;
		String sign = md5(signStr);
		if (sign.equals(infoMap.get("sign"))) {
			logger.info("SMART支付验签成功");
			return "success";
		}
		logger.info("SMART支付验签失败");
		return "";
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static void main(String[] args) {
		// String customerid ="10882";
		// String status = "1";
		// String sdpayno = "123456789";
		// String sdorderno = "SMARTtxk201802132112512112516540";
		// String total_fee = "10.00";
		// String paytype = "alipay";
		// String signStr =
		// "customerid="+customerid+"&status="+status+"&sdpayno="+sdpayno+"&sdorderno="+sdorderno+"&total_fee="
		// +total_fee+"&paytype="+paytype+"&"+"e121d257e1cf655ec63742d8252a220e6aefad1f";
		// String sign = md5(signStr);
		// System.out.println(sign);
	}

	public String bankPay(Map<String, String> scanMap) {
		String sdorderno = scanMap.get("sdorderno");// "XM"+new
													// Date().getTime();
		String userid = sdorderno.substring(sdorderno.length() - 6, sdorderno.length() - 1);
		String total_fee = scanMap.get("total_fee");
		String paytype = "bank";
		String returnurl = scanMap.get("returnurl");
		String bankcode = scanMap.get("bankcode");
		String remark = "";
		String sign = "";
		String s = "version=" + version + "&customerid=" + customerid + "&userid=" + userid + "&total_fee=" + total_fee
				+ "&sdorderno=" + sdorderno + "&notifyurl=" + notifyurl + "&returnurl=" + returnurl + "&" + key;
		sign = md5(s);
		System.out.println("生成sign值：" + sign);
		// 构建请求参数
		Map<String, String> resquestMap = new LinkedHashMap<>();
		resquestMap.put("version", version);
		resquestMap.put("customerid", customerid);
		resquestMap.put("sdorderno", sdorderno);
		resquestMap.put("userid", userid);
		resquestMap.put("total_fee", total_fee);
		resquestMap.put("paytype", paytype);
		resquestMap.put("bankcode", bankcode);
		resquestMap.put("notifyurl", notifyurl);
		resquestMap.put("returnurl", returnurl);
		resquestMap.put("remark", remark);
		resquestMap.put("sign", sign);

		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ url + "\">";
		for (String key : resquestMap.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + resquestMap.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";

		String html = FormString;
		// String html = HttpUtil.HtmlFrom(url, resquestMap);
		logger.info("smart支付网银支付表单:" + html);
		return html;
	}

	@Override
	public net.sf.json.JSONObject wyPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String userName = payEntity.getUsername();
		String pay_url = payEntity.getPayUrl();

		String int_amount = String.valueOf(amount);
		int index = int_amount.indexOf(".");
		int_amount = int_amount.substring(0, index);
		if (!check(int_amount)) {
			logger.info("输入金额不正常");
			return PayUtil.returnWYPayJson("error", "", "", "", "");
		}
		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("sdorderno", order_no);
		scanMap.put("total_fee", String.valueOf(int_amount + ".00"));
		scanMap.put("paytype", pay_code);
		scanMap.put("returnUrl", refereUrl);

		String html = bankPay(scanMap);
		return PayUtil.returnWYPayJson("success", "form", html, pay_url, "");
	}

	@Override
	public net.sf.json.JSONObject smPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String userName = payEntity.getUsername();
		String pay_url = payEntity.getPayUrl();

		String int_amount = String.valueOf(amount);
		int index = int_amount.indexOf(".");
		int_amount = int_amount.substring(0, index);
		if (!check(int_amount)) {
			return PayUtil.returnPayJson("error", "1",
					"输入金额有误，不能识别,只支持10,20,50,100,200,500,800,1000,1200,1200,1500,2000,3000", userName, amount, order_no,
					"");
		}
//		String mobile = payEntity.getMobile();
		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("sdorderno", order_no);
		scanMap.put("total_fee", String.valueOf(int_amount + ".00"));
		scanMap.put("paytype", pay_code);
		scanMap.put("returnUrl", refereUrl);
		String urlStr = scanPay(scanMap);
		// pc端
		return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no, urlStr);
	}
}
