package com.cn.tianxia.pay.bft.util;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class Mobo360Merchant {

	private Mobo360Merchant() {

	}

	/**
	 * 发送请求到支付网关并接受回复
	 * 
	 * @param paramsStr
	 *            请求参数字符串
	 * @param serverUrl
	 *            支付网关地址
	 * @return
	 * @throws Exception
	 */

	public static String getClientIP(HttpServletRequest request) {
		if (request == null)
		return null;
	    String s = request.getHeader("X-Forwarded-For");
		if (s == null || s.length() == 0 || "unknown".equalsIgnoreCase(s))
		s = request.getHeader("Proxy-Client-IP");
		 if (s == null || s.length() == 0 || "unknown".equalsIgnoreCase(s))
	    s = request.getHeader("WL-Proxy-Client-IP");
		 if (s == null || s.length() == 0 || "unknown".equalsIgnoreCase(s))
		s = request.getHeader("HTTP_CLIENT_IP");
		 if (s == null || s.length() == 0 || "unknown".equalsIgnoreCase(s))
		s = request.getHeader("HTTP_X_FORWARDED_FOR");
	    if (s == null || s.length() == 0 || "unknown".equalsIgnoreCase(s))
		s = request.getRemoteAddr();
	    if ("127.0.0.1".equals(s) || "0:0:0:0:0:0:0:1".equals(s))
	    	try {
		      s = InetAddress.getLocalHost().getHostAddress();}
			catch (UnknownHostException unknownhostexception) { 
				throw new NullPointerException("请求地址或请求数据为空!");
		}
	     //System.out.println("777777777777777777777777777777777777777777"+s);
	    // System.out.println("111111111111111111111111111111111111111111"+request);
	     return s;
			}

	
	
	
	public static String generatePayRequestap(Map<String, String> paramsMap)
			throws Exception {

		// 验证输入数据合法性
		if (!paramsMap.containsKey("apiName")
				|| StringUtils.isBlank(paramsMap.get("apiName"))) {
			throw new Exception("apiName不能为空");
		}
		if (!paramsMap.containsKey("apiVersion")
				|| StringUtils.isBlank(paramsMap.get("apiVersion"))) {
			throw new Exception("apiVersion不能为空");
		}
		if (!paramsMap.containsKey("platformID")
				|| StringUtils.isBlank(paramsMap.get("platformID"))) {
			throw new Exception("platformID不能为空");
		}
		if (!paramsMap.containsKey("merchNo")
				|| StringUtils.isBlank(paramsMap.get("merchNo"))) {
			throw new Exception("merchNo不能为空");
		}
		if (!paramsMap.containsKey("orderNo")
				|| StringUtils.isBlank(paramsMap.get("orderNo"))) {
			throw new Exception("orderNo不能为空");
		}
		if (!paramsMap.containsKey("tradeDate")
				|| StringUtils.isBlank(paramsMap.get("tradeDate"))) {
			throw new Exception("tradeDate不能为空");
		}
		if (!paramsMap.containsKey("amt")
				|| StringUtils.isBlank(paramsMap.get("amt"))) {
			throw new Exception("amt不能为空");
		}
		if (!paramsMap.containsKey("merchUrl")
				|| StringUtils.isBlank(paramsMap.get("merchUrl"))) {
			throw new Exception("merchUrl不能为空");
		}
		if (!paramsMap.containsKey("merchParam")) {
			throw new Exception("merchParam可以为空，但必须存在！");
		}
		if (!paramsMap.containsKey("tradeSummary")
				|| StringUtils.isBlank(paramsMap.get("tradeSummary"))) {
			throw new Exception("tradeSummary不能为空");
		}
		if (!paramsMap.containsKey("customerIP")
				|| StringUtils.isBlank(paramsMap.get("customerIP"))) {
			throw new Exception("customerIP不能为空");
		}
		// 输入数据组织成字符串
		String paramsStr = String
				.format("apiName=%s&apiVersion=%s&platformID=%s&merchNo=%s&orderNo=%s&tradeDate=%s&amt=%s&merchUrl=%s&merchParam=%s&tradeSummary=%s&customerIP=%s",
						paramsMap.get("apiName"), paramsMap.get("apiVersion"),
						paramsMap.get("platformID"), paramsMap.get("merchNo"),
						paramsMap.get("orderNo"), paramsMap.get("tradeDate"),
						paramsMap.get("amt"), paramsMap.get("merchUrl"),
						paramsMap.get("merchParam"),
						paramsMap.get("tradeSummary"),
						paramsMap.get("customerIP"));
		 System.out.println("（防钓鱼支付  签名源串）"+paramsStr);
		return paramsStr;
	}

	
	
	
	
	public static String generateDirectPay(
			Map<String, String> paramsMap) throws Exception {
		  //System.out.println("222222222222222222222222222222222"+paramsMap);
		// 验证输入数据合法性
		if (!paramsMap.containsKey("apiName")
				|| StringUtils.isBlank(paramsMap.get("apiName"))) {
			throw new Exception("apiName不能为空");
		}
		if (!paramsMap.containsKey("apiVersion")
				|| StringUtils.isBlank(paramsMap.get("apiVersion"))) {
			throw new Exception("apiVersion不能为空");
		}
		if (!paramsMap.containsKey("platformID")
				|| StringUtils.isBlank(paramsMap.get("platformID"))) {
			throw new Exception("platformID不能为空");
		}
		if (!paramsMap.containsKey("merchNo")
				|| StringUtils.isBlank(paramsMap.get("merchNo"))) {
			throw new Exception("merchNo不能为空");
		}
		if (!paramsMap.containsKey("orderNo")
				|| StringUtils.isBlank(paramsMap.get("orderNo"))) {
			throw new Exception("orderNo不能为空");
		}
		if (!paramsMap.containsKey("tradeDate")
				|| StringUtils.isBlank(paramsMap.get("tradeDate"))) {
			throw new Exception("tradeDate不能为空");
		}
		if (!paramsMap.containsKey("Amt")
				|| StringUtils.isBlank(paramsMap.get("Amt"))) {
			throw new Exception("Amt不能为空");
		}
		if (!paramsMap.containsKey("merchUrl")
				|| StringUtils.isBlank(paramsMap.get("merchUrl"))) {
			throw new Exception("merchUrl不能为空");
		}
		if (!paramsMap.containsKey("merchParam")) {
			throw new Exception("merchParam可以为空，但必须存在！");
		}
		if (!paramsMap.containsKey("bankAccName")
				|| StringUtils.isBlank(paramsMap.get("bankAccName"))) {
			throw new Exception("bankAccName不能为空");
		}
		if (!paramsMap.containsKey("bankName")
				|| StringUtils.isBlank(paramsMap.get("bankName"))) {
			throw new Exception("bankName不能为空");
		}
		if (!paramsMap.containsKey("bankCode")
				|| StringUtils.isBlank(paramsMap.get("bankCode"))) {
			throw new Exception("bankCode不能为空");
		}
		if (!paramsMap.containsKey("bankAccNo")
				|| StringUtils.isBlank(paramsMap.get("bankAccNo"))) {
			throw new Exception("bankAccNo不能为空");
		}
		if (!paramsMap.containsKey("tradeSummary")
				|| StringUtils.isBlank(paramsMap.get("tradeSummary"))) {
			throw new Exception("tradeSummary不能为空");
		}
		
		// 输入数据组织成字符串
		String paramsStr = String
				.format("apiName=%s&apiVersion=%s&platformID=%s&merchNo=%s&orderNo=%s&tradeDate=%s&merchUrl=%s&merchParam=%s&bankAccNo=%s&bankAccName=%s&bankCode=%s&bankName=%s&Amt=%s&tradeSummary=%s",
						paramsMap.get("apiName"), paramsMap.get("apiVersion"),
						paramsMap.get("platformID"), paramsMap.get("merchNo"),
						paramsMap.get("orderNo"), paramsMap.get("tradeDate"),
						paramsMap.get("merchUrl"),
						paramsMap.get("merchParam"),
						paramsMap.get("bankAccNo"),
						paramsMap.get("bankAccName"),
						paramsMap.get("bankCode"),
						paramsMap.get("bankName"),
						paramsMap.get("Amt"),
						paramsMap.get("tradeSummary"));
		 System.out.println("(委托结算  签名源串)"+paramsStr);
		return paramsStr;
	}
	/*备注：签名顺序为apiName,apiVersion,platformID,merchNo,orderNo，tradeDate，merchUrl,merchParam,
	 * bankAccNo,bankAccName, bankCode, bankName, Amt, tradeSummary*/
	
	
	/**
	 * 将由支付请求参数构成的map转换成支付串，并对参数做合法验证
	 * 
	 * @param paramsMap
	 *            由支付请求参数构成的map
	 * @return
	 * @throws Exception
	 */
	public static String generateCustRealPayRequestap(
			Map<String, String> paramsMap) throws Exception {
		 //System.out.println("888888888888888888"+paramsMap);
		// 验证输入数据合法性
		if (!paramsMap.containsKey("apiName")
				|| StringUtils.isBlank(paramsMap.get("apiName"))) {
			throw new Exception("apiName不能为空");
		}
		if (!paramsMap.containsKey("apiVersion")
				|| StringUtils.isBlank(paramsMap.get("apiVersion"))) {
			throw new Exception("apiVersion不能为空");
		}
		if (!paramsMap.containsKey("platformID")
				|| StringUtils.isBlank(paramsMap.get("platformID"))) {
			throw new Exception("platformID不能为空");
		}
		if (!paramsMap.containsKey("merchNo")
				|| StringUtils.isBlank(paramsMap.get("merchNo"))) {
			throw new Exception("merchNo不能为空");
		}
		if (!paramsMap.containsKey("orderNo")
				|| StringUtils.isBlank(paramsMap.get("orderNo"))) {
			throw new Exception("orderNo不能为空");
		}
		if (!paramsMap.containsKey("tradeDate")
				|| StringUtils.isBlank(paramsMap.get("tradeDate"))) {
			throw new Exception("tradeDate不能为空");
		}
		if (!paramsMap.containsKey("amt")
				|| StringUtils.isBlank(paramsMap.get("amt"))) {
			throw new Exception("amt不能为空");
		}
		if (!paramsMap.containsKey("merchUrl")
				|| StringUtils.isBlank(paramsMap.get("merchUrl"))) {
			throw new Exception("merchUrl不能为空");
		}
		if (!paramsMap.containsKey("merchParam")) {
			throw new Exception("merchParam可以为空，但必须存在！");
		}
		if (!paramsMap.containsKey("tradeSummary")
				|| StringUtils.isBlank(paramsMap.get("tradeSummary"))) {
			throw new Exception("tradeSummary不能为空");
		}
		if (!paramsMap.containsKey("cardName")
				|| StringUtils.isBlank(paramsMap.get("cardName"))) {
			throw new Exception("cardName不能为空");
		}
		if (!paramsMap.containsKey("idCardNo")
				|| StringUtils.isBlank(paramsMap.get("idCardNo"))) {
			throw new Exception("idCardNo不能为空");
		}
		if (!paramsMap.containsKey("cardBankCode")
				|| StringUtils.isBlank(paramsMap.get("cardBankCode"))) {
			throw new Exception("cardBankCode不能为空");
		}
		if (!paramsMap.containsKey("cardType")
				|| StringUtils.isBlank(paramsMap.get("cardType"))) {
			throw new Exception("cardType不能为空");
		}
		if (!paramsMap.containsKey("cardNo")
				|| StringUtils.isBlank(paramsMap.get("cardNo"))) {
			throw new Exception("cardNo不能为空");
		}
		if (!paramsMap.containsKey("customerIP")
				|| StringUtils.isBlank(paramsMap.get("customerIP"))) {
			throw new Exception("customerIP");
		}

		// 输入数据组织成字符串
		String paramsStr = String
				.format("apiName=%s&apiVersion=%s&platformID=%s&merchNo=%s&orderNo=%s&tradeDate=%s&amt=%s&merchUrl=%s&merchParam=%s&tradeSummary=%s&cardName=%s&idCardNo=%s&cardBankCode=%s&cardType=%s&cardNo=%s",
						paramsMap.get("apiName"), paramsMap.get("apiVersion"),
						paramsMap.get("platformID"), paramsMap.get("merchNo"),
						paramsMap.get("orderNo"), paramsMap.get("tradeDate"),
						paramsMap.get("amt"), paramsMap.get("merchUrl"),
						paramsMap.get("merchParam"),
						paramsMap.get("tradeSummary"),
						paramsMap.get("cardName"), paramsMap.get("idCardNo"),
						paramsMap.get("cardBankCode"),
						paramsMap.get("cardType"), paramsMap.get("cardNo"),
					    paramsMap.get("customerIP"));
		// System.out.println("（weizhi）"+paramsStr);
		return paramsStr;
	}

	
	
	
	
	
	
	
	
	public static String transact(String paramsStr, String serverUrl)
			throws Exception {

		if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(paramsStr)) {
			throw new NullPointerException("请求地址或请求数据为空!");
		}

		myX509TrustManager xtm = new myX509TrustManager();
		myHostnameVerifier hnv = new myHostnameVerifier();
		ByteArrayOutputStream respData = new ByteArrayOutputStream();

		byte[] b = new byte[8192];
		String result = "";
		try {
			SSLContext sslContext = null;
			try {
				sslContext = SSLContext.getInstance("TLS");
				X509TrustManager[] xtmArray = new X509TrustManager[] { xtm };
				sslContext.init(null, xtmArray,
						new java.security.SecureRandom());
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			}

			if (sslContext != null) {
				HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
						.getSocketFactory());
			}
			HttpsURLConnection.setDefaultHostnameVerifier(hnv);

			// 匹配http或者https请求
			URLConnection conn = null;
			if (serverUrl.toLowerCase().startsWith("https")) {
				HttpsURLConnection httpsUrlConn = (HttpsURLConnection) (new URL(
						serverUrl)).openConnection();
				httpsUrlConn.setRequestMethod("POST");
				conn = httpsUrlConn;
			} else {
				HttpURLConnection httpUrlConn = (HttpURLConnection) (new URL(
						serverUrl)).openConnection();
				httpUrlConn.setRequestMethod("POST");
				conn = httpUrlConn;
			}

			conn.setConnectTimeout(5000);
			conn.setReadTimeout(30000);
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.8.1.1) Gecko/20061204 Firefox/2.0.0.3");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			conn.setRequestProperty("connection", "close");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.getOutputStream().write(paramsStr.getBytes("utf-8"));
			conn.getOutputStream().flush();

			int len = 0;
			try {
				while (true) {
					len = conn.getInputStream().read(b);
					if (len <= 0) {
						conn.getInputStream().close();
						break;
					}
					respData.write(b, 0, len);
				}
			} catch (SocketTimeoutException ee) {
				throw new RuntimeException("读取响应数据出错！" + ee.getMessage());
			}

			// 返回给商户的数据
			result = respData.toString("utf-8");
			if (StringUtils.isBlank(result)) {
				throw new RuntimeException("返回参数错误！");
			}
		} catch (Exception e) {
			throw new RuntimeException("发送POST请求出现异常！" + e.getMessage());
		}

		// 验签返回数据后返回支付平台回复数据
		checkResult(result);
		return result;
	}

	/**
	 * 如果数据被篡改 则抛出异常
	 * 
	 * @param result
	 * @throws Exception
	 */
	private static void checkResult(String result) throws Exception {

		if (StringUtils.isBlank(result)) {
			throw new NullPointerException("返回数据为空!");
		}

		try {
			Document resultDOM = DocumentHelper.parseText(result);
			Element root = resultDOM.getRootElement();
			String responseData = root.element("respData").asXML();
			String signMsg = root.element("signMsg").getStringValue();

			if (StringUtils.isBlank(responseData)
					|| StringUtils.isBlank(signMsg)) {
				throw new Exception("解析返回验签或原数据错误！");
			}

			if (!Mobo360SignUtil.verifyData(signMsg, responseData)) {
				throw new Exception("系统效验返回数据失败！");
			}

		} catch (DocumentException e) {
			throw new RuntimeException("xml解析错误：" + e);
		}
	}

	/**
	 * 将由支付请求参数构成的map转换成支付串，并对参数做合法验证
	 * 
	 * @param paramsMap
	 *            由支付请求参数构成的map
	 * @return
	 * @throws Exception
	 */
	public static String generatePayRequest(Map<String, String> paramsMap)
			throws Exception {

		// 验证输入数据合法性
		if (!paramsMap.containsKey("apiName")
				|| StringUtils.isBlank(paramsMap.get("apiName"))) {
			throw new Exception("apiName不能为空");
		}
		if (!paramsMap.containsKey("apiVersion")
				|| StringUtils.isBlank(paramsMap.get("apiVersion"))) {
			throw new Exception("apiVersion不能为空");
		}
		if (!paramsMap.containsKey("platformID")
				|| StringUtils.isBlank(paramsMap.get("platformID"))) {
			throw new Exception("platformID不能为空");
		}
		if (!paramsMap.containsKey("merchNo")
				|| StringUtils.isBlank(paramsMap.get("merchNo"))) {
			throw new Exception("merchNo不能为空");
		}
		if (!paramsMap.containsKey("orderNo")
				|| StringUtils.isBlank(paramsMap.get("orderNo"))) {
			throw new Exception("orderNo不能为空");
		}
		if (!paramsMap.containsKey("tradeDate")
				|| StringUtils.isBlank(paramsMap.get("tradeDate"))) {
			throw new Exception("tradeDate不能为空");
		}
		if (!paramsMap.containsKey("amt")
				|| StringUtils.isBlank(paramsMap.get("amt"))) {
			throw new Exception("amt不能为空");
		}
		if (!paramsMap.containsKey("merchUrl")
				|| StringUtils.isBlank(paramsMap.get("merchUrl"))) {
			throw new Exception("merchUrl不能为空");
		}
		if (!paramsMap.containsKey("merchParam")) {
			throw new Exception("merchParam可以为空，但必须存在！");
		}
		if (!paramsMap.containsKey("tradeSummary")
				|| StringUtils.isBlank(paramsMap.get("tradeSummary"))) {
			throw new Exception("tradeSummary不能为空");
		}

		// 输入数据组织成字符串
		String paramsStr = String
				.format("apiName=%s&apiVersion=%s&platformID=%s&merchNo=%s&orderNo=%s&tradeDate=%s&amt=%s&merchUrl=%s&merchParam=%s&tradeSummary=%s",
						paramsMap.get("apiName"), paramsMap.get("apiVersion"),
						paramsMap.get("platformID"), paramsMap.get("merchNo"),
						paramsMap.get("orderNo"), paramsMap.get("tradeDate"),
						paramsMap.get("amt"), paramsMap.get("merchUrl"),
						paramsMap.get("merchParam"),
						paramsMap.get("tradeSummary"));
		 System.out.println("（网关支付  签名源串）"+paramsStr);
		return paramsStr;
	}
	public static String generateWeChatPay(Map<String, String> paramsMap)
			throws Exception {

		// 验证输入数据合法性
		if (!paramsMap.containsKey("apiName")
				|| StringUtils.isBlank(paramsMap.get("apiName"))) {
			throw new Exception("apiName不能为空");
		}
		if (!paramsMap.containsKey("apiVersion")
				|| StringUtils.isBlank(paramsMap.get("apiVersion"))) {
			throw new Exception("apiVersion不能为空");
		}
		if (!paramsMap.containsKey("platformID")
				|| StringUtils.isBlank(paramsMap.get("platformID"))) {
			throw new Exception("platformID不能为空");
		}
		if (!paramsMap.containsKey("merchNo")
				|| StringUtils.isBlank(paramsMap.get("merchNo"))) {
			throw new Exception("merchNo不能为空");
		}
		if (!paramsMap.containsKey("orderNo")
				|| StringUtils.isBlank(paramsMap.get("orderNo"))) {
			throw new Exception("orderNo不能为空");
		}
		if (!paramsMap.containsKey("tradeDate")
				|| StringUtils.isBlank(paramsMap.get("tradeDate"))) {
			throw new Exception("tradeDate不能为空");
		}
		if (!paramsMap.containsKey("amt")
				|| StringUtils.isBlank(paramsMap.get("amt"))) {
			throw new Exception("amt不能为空");
		}
		if (!paramsMap.containsKey("merchUrl")
				|| StringUtils.isBlank(paramsMap.get("merchUrl"))) {
			throw new Exception("merchUrl不能为空");
		}
		if (!paramsMap.containsKey("merchParam")) {
			throw new Exception("merchParam可以为空，但必须存在！");
		}
		if (!paramsMap.containsKey("tradeSummary")
				|| StringUtils.isBlank(paramsMap.get("tradeSummary"))) {
			throw new Exception("tradeSummary不能为空");
		}
		if (!paramsMap.containsKey("overTime")
				|| StringUtils.isBlank(paramsMap.get("overTime"))) {
			throw new Exception("overTime不能为空");
		}
		if (!paramsMap.containsKey("customerIP")
				|| StringUtils.isBlank(paramsMap.get("customerIP"))) {
			throw new Exception("customerIP不能为空");
		}
        
		// 输入数据组织成字符串
		String paramsStr = String
				.format("apiName=%s&apiVersion=%s&platformID=%s&merchNo=%s&orderNo=%s&tradeDate=%s&amt=%s&merchUrl=%s&merchParam=%s&tradeSummary=%s&overTime=%s&customerIP=%s",
						paramsMap.get("apiName"), paramsMap.get("apiVersion"),
						paramsMap.get("platformID"), paramsMap.get("merchNo"),
						paramsMap.get("orderNo"), paramsMap.get("tradeDate"),
						paramsMap.get("amt"), paramsMap.get("merchUrl"),
						paramsMap.get("merchParam"),
						paramsMap.get("tradeSummary"),paramsMap.get("overTime"),paramsMap.get("customerIP"));
		 System.out.println("（直连扫码支付  签名源串）"+paramsStr);
		return paramsStr;
	}
	/**
	 * 将由支付请求参数构成的map转换成支付串，并对参数做合法验证
	 * 
	 * @param paramsMap
	 *            由支付请求参数构成的map
	 * @return
	 * @throws Exception
	 */
	public static String generateCustRealPayRequest(
			Map<String, String> paramsMap) throws Exception {
               
		// 验证输入数据合法性
		if (!paramsMap.containsKey("apiName")
				|| StringUtils.isBlank(paramsMap.get("apiName"))) {
			throw new Exception("apiName不能为空");
		}
		if (!paramsMap.containsKey("apiVersion")
				|| StringUtils.isBlank(paramsMap.get("apiVersion"))) {
			throw new Exception("apiVersion不能为空");
		}
		if (!paramsMap.containsKey("platformID")
				|| StringUtils.isBlank(paramsMap.get("platformID"))) {
			throw new Exception("platformID不能为空");
		}
		if (!paramsMap.containsKey("merchNo")
				|| StringUtils.isBlank(paramsMap.get("merchNo"))) {
			throw new Exception("merchNo不能为空");
		}
		if (!paramsMap.containsKey("orderNo")
				|| StringUtils.isBlank(paramsMap.get("orderNo"))) {
			throw new Exception("orderNo不能为空");
		}
		if (!paramsMap.containsKey("tradeDate")
				|| StringUtils.isBlank(paramsMap.get("tradeDate"))) {
			throw new Exception("tradeDate不能为空");
		}
		if (!paramsMap.containsKey("amt")
				|| StringUtils.isBlank(paramsMap.get("amt"))) {
			throw new Exception("amt不能为空");
		}
		if (!paramsMap.containsKey("merchUrl")
				|| StringUtils.isBlank(paramsMap.get("merchUrl"))) {
			throw new Exception("merchUrl不能为空");
		}
		if (!paramsMap.containsKey("merchParam")) {
			throw new Exception("merchParam可以为空，但必须存在！");
		}
		if (!paramsMap.containsKey("tradeSummary")
				|| StringUtils.isBlank(paramsMap.get("tradeSummary"))) {
			throw new Exception("tradeSummary不能为空");
		}
		if (!paramsMap.containsKey("cardName")
				|| StringUtils.isBlank(paramsMap.get("cardName"))) {
			throw new Exception("cardName不能为空");
		}
		if (!paramsMap.containsKey("idCardNo")
				|| StringUtils.isBlank(paramsMap.get("idCardNo"))) {
			throw new Exception("idCardNo不能为空");
		}
		if (!paramsMap.containsKey("cardBankCode")
				|| StringUtils.isBlank(paramsMap.get("cardBankCode"))) {
			throw new Exception("cardBankCode不能为空");
		}
		if (!paramsMap.containsKey("cardType")
				|| StringUtils.isBlank(paramsMap.get("cardType"))) {
			throw new Exception("cardType不能为空");
		}
		if (!paramsMap.containsKey("cardNo")
				|| StringUtils.isBlank(paramsMap.get("cardNo"))) {
			throw new Exception("cardNo不能为空");
		}

		// 输入数据组织成字符串
		String paramsStr = String
				.format("apiName=%s&apiVersion=%s&platformID=%s&merchNo=%s&orderNo=%s&tradeDate=%s&amt=%s&merchUrl=%s&merchParam=%s&tradeSummary=%s&cardName=%s&idCardNo=%s&cardBankCode=%s&cardType=%s&cardNo=%s",
						paramsMap.get("apiName"), paramsMap.get("apiVersion"),
						paramsMap.get("platformID"), paramsMap.get("merchNo"),
						paramsMap.get("orderNo"), paramsMap.get("tradeDate"),
						paramsMap.get("amt"), paramsMap.get("merchUrl"),
						paramsMap.get("merchParam"),
						paramsMap.get("tradeSummary"),
						paramsMap.get("cardName"), paramsMap.get("idCardNo"),
						paramsMap.get("cardBankCode"),
						paramsMap.get("cardType"), paramsMap.get("cardNo"));
		System.out.println("（实名无卡代扣  签名源串）"+paramsStr);
		return paramsStr;
	}

	/**
	 * 将由查询请求参数组成的map组织成字符串，并对参数做合法性验证
	 * 
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public static String generateQueryRequest(Map<String, String> paramsMap)
			throws Exception {
		if (!paramsMap.containsKey("apiName")
				|| StringUtils.isBlank(paramsMap.get("apiName"))) {
			throw new Exception("apiName不能为空");
		}
		if (!paramsMap.containsKey("apiVersion")
				|| StringUtils.isBlank(paramsMap.get("apiVersion"))) {
			throw new Exception("apiVersion不能为空");
		}
		if (!paramsMap.containsKey("platformID")
				|| StringUtils.isBlank(paramsMap.get("platformID"))) {
			throw new Exception("platformID不能为空");
		}
		if (!paramsMap.containsKey("merchNo")
				|| StringUtils.isBlank(paramsMap.get("merchNo"))) {
			throw new Exception("merchNo不能为空");
		}
		if (!paramsMap.containsKey("orderNo")
				|| StringUtils.isBlank(paramsMap.get("orderNo"))) {
			throw new Exception("orderNo不能为空");
		}
		if (!paramsMap.containsKey("tradeDate")
				|| StringUtils.isBlank(paramsMap.get("tradeDate"))) {
			throw new Exception("tradeDate不能为空");
		}
		if (!paramsMap.containsKey("amt")
				|| StringUtils.isBlank(paramsMap.get("amt"))) {
			throw new Exception("amt不能为空");
		}

		String resultStr = String
				.format("apiName=%s&apiVersion=%s&platformID=%s&merchNo=%s&orderNo=%s&tradeDate=%s&amt=%s",
						paramsMap.get("apiName"), paramsMap.get("apiVersion"),
						paramsMap.get("platformID"), paramsMap.get("merchNo"),
						paramsMap.get("orderNo"), paramsMap.get("tradeDate"),
						paramsMap.get("amt"));
		 System.out.println("（订单查询  签名源串）"+resultStr);
		return resultStr;
	}
	/**
	 * 扫码付
	 * 将由查询请求参数组成的map组织成字符串，并对参数做合法性验证
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public static String generateScanPayRequest(Map<String, String> paramsMap)
			throws Exception {
		if (!paramsMap.containsKey("apiName")
				|| StringUtils.isBlank(paramsMap.get("apiName"))) {
			throw new Exception("apiName不能为空");
		}
		if (!paramsMap.containsKey("apiVersion")
				|| StringUtils.isBlank(paramsMap.get("apiVersion"))) {
			throw new Exception("apiVersion不能为空");
		}
		if (!paramsMap.containsKey("platformID")
				|| StringUtils.isBlank(paramsMap.get("platformID"))) {
			throw new Exception("platformID不能为空");
		}
		if (!paramsMap.containsKey("merchNo")
				|| StringUtils.isBlank(paramsMap.get("merchNo"))) {
			throw new Exception("merchNo不能为空");
		}
		if (!paramsMap.containsKey("orderNo")
				|| StringUtils.isBlank(paramsMap.get("orderNo"))) {
			throw new Exception("orderNo不能为空");
		}
		if (!paramsMap.containsKey("tradeDate")
				|| StringUtils.isBlank(paramsMap.get("tradeDate"))) {
			throw new Exception("tradeDate不能为空");
		}
		if (!paramsMap.containsKey("amt")
				|| StringUtils.isBlank(paramsMap.get("amt"))) {
			throw new Exception("amt不能为空");
		}
		if (!paramsMap.containsKey("merchUrl")
				|| StringUtils.isBlank(paramsMap.get("merchUrl"))) {
			throw new Exception("merchUrl不能为空");
		}
		if (!paramsMap.containsKey("tradeSummary")
				|| StringUtils.isBlank(paramsMap.get("amt"))) {
			throw new Exception("tradeSummary不能为空");
		}
		if (!paramsMap.containsKey("customerIP")
				|| StringUtils.isBlank(paramsMap.get("customerIP"))) {
			throw new Exception("customerIP不能为空");
		}
		
		
		String data = "";
		data = appendParam(data, "apiName", paramsMap.get("apiName"));
		data = appendParam(data, "apiVersion", paramsMap.get("apiVersion"));
		data = appendParam(data, "platformID", paramsMap.get("platformID"));
		data = appendParam(data, "merchNo", paramsMap.get("merchNo"));
		data = appendParam(data, "orderNo", paramsMap.get("orderNo"));
		data = appendParam(data, "tradeDate", paramsMap.get("tradeDate"));
		data = appendParam(data, "amt", paramsMap.get("amt"));
		data = appendParam(data, "merchUrl", paramsMap.get("merchUrl"));
		data = appendParam(data, "merchParam", paramsMap.get("merchParam"));
		data = appendParam(data, "tradeSummary", paramsMap.get("tradeSummary"));
		data = appendParam(data, "overTime", paramsMap.get("overTime"));
		data = appendParam(data, "customerIP", paramsMap.get("customerIP"));
		
		System.out.println("（扫码付查询  签名源串）"+data);
		return data;
	}
	
	/**
	 * 直连余额查询
	 * 将由查询请求参数组成的map组织成字符串，并对参数做合法性验证
	 * @param paramsMap
	 * @return
	 * @throws Exception
	 */
	public static String generateQueryAmtRequest(Map<String, String> paramsMap)
			throws Exception {
		if (!paramsMap.containsKey("apiName")
				|| StringUtils.isBlank(paramsMap.get("apiName"))) {
			throw new Exception("apiName不能为空");
		}
		if (!paramsMap.containsKey("apiVersion")
				|| StringUtils.isBlank(paramsMap.get("apiVersion"))) {
			throw new Exception("apiVersion不能为空");
		}
		if (!paramsMap.containsKey("platformID")
				|| StringUtils.isBlank(paramsMap.get("platformID"))) {
			throw new Exception("platformID不能为空");
		}
		if (!paramsMap.containsKey("merchNo")
				|| StringUtils.isBlank(paramsMap.get("merchNo"))) {
			throw new Exception("merchNo不能为空");
		}
		if (!paramsMap.containsKey("txnType")
				|| StringUtils.isBlank(paramsMap.get("txnType"))) {
			throw new Exception("txnType不能为空");
		}
		if (!paramsMap.containsKey("tradeDate")
				|| StringUtils.isBlank(paramsMap.get("tradeDate"))) {
			throw new Exception("tradeDate不能为空");
		}
		
		
		
		String data = "";
		data = appendParam(data, "apiName", paramsMap.get("apiName"));
		data = appendParam(data, "apiVersion", paramsMap.get("apiVersion"));
		data = appendParam(data, "platformID", paramsMap.get("platformID"));
		data = appendParam(data, "merchNo", paramsMap.get("merchNo"));
		data = appendParam(data, "txnType", paramsMap.get("txnType"));
		data = appendParam(data, "tradeDate", paramsMap.get("tradeDate"));
		
		System.out.println("（直连余额查询   签名源串）"+data);
		return data;
	}
	
	
	/**
	 * 拼接签名
	 * @param returns
	 * @param paramId
	 * @param paramValue
	 * @return  
	 */
	private static String appendParam(String returns, String paramId, String paramValue) {

		paramValue = paramValue == null ? "" : paramValue.trim();

		if (returns != null && !"".equals(returns)) {
			returns += "&" + paramId + "=" + paramValue;
		} else {
			returns = paramId + "=" + paramValue;
		}
		return returns;
	}
	public static String generateRefundRequest(Map<String, String> paramsMap)
			throws Exception {
		if (!paramsMap.containsKey("apiName")
				|| StringUtils.isBlank(paramsMap.get("apiName"))) {
			throw new Exception("apiName不能为空");
		}
		if (!paramsMap.containsKey("apiVersion")
				|| StringUtils.isBlank(paramsMap.get("apiVersion"))) {
			throw new Exception("apiVersion不能为空");
		}
		if (!paramsMap.containsKey("platformID")
				|| StringUtils.isBlank(paramsMap.get("platformID"))) {
			throw new Exception("platformID不能为空");
		}
		if (!paramsMap.containsKey("merchNo")
				|| StringUtils.isBlank(paramsMap.get("merchNo"))) {
			throw new Exception("merchNo不能为空");
		}
		if (!paramsMap.containsKey("orderNo")
				|| StringUtils.isBlank(paramsMap.get("orderNo"))) {
			throw new Exception("orderNo不能为空");
		}
		if (!paramsMap.containsKey("tradeDate")
				|| StringUtils.isBlank(paramsMap.get("tradeDate"))) {
			throw new Exception("tradeDate不能为空");
		}
		if (!paramsMap.containsKey("amt")
				|| StringUtils.isBlank(paramsMap.get("amt"))) {
			throw new Exception("amt不能为空");
		}
		if (!paramsMap.containsKey("tradeSummary")
				|| StringUtils.isBlank(paramsMap.get("tradeSummary"))) {
			throw new Exception("tradeSummary不能为空");
		}

		String resultStr = String
				.format("apiName=%s&apiVersion=%s&platformID=%s&merchNo=%s&orderNo=%s&tradeDate=%s&amt=%s&tradeSummary=%s",
						paramsMap.get("apiName"), paramsMap.get("apiVersion"),
						paramsMap.get("platformID"), paramsMap.get("merchNo"),
						paramsMap.get("orderNo"), paramsMap.get("tradeDate"),
						paramsMap.get("amt"), paramsMap.get("tradeSummary"));
		System.out.println("（退款操作  签名源串）"+resultStr);
		return resultStr;
	}

}

class myX509TrustManager implements X509TrustManager {

	public void checkClientTrusted(X509Certificate[] chain, String authType) {
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType) {
	}

	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}
}

class myHostnameVerifier implements HostnameVerifier {

	public boolean verify(String hostname, SSLSession session) {
		return true;
	}

}