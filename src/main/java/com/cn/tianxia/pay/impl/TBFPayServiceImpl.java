package com.cn.tianxia.pay.impl;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
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

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.mkt.util.HttpUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.SSLClient;
import com.itrus.util.sign.RSAWithSoftware;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

public class TBFPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(TBFPayServiceImpl.class);
	
	// 扫码配置
	private static String scanUrl;
	private static String merchant_code;//
	private static String merchant_private_key;
	private static String dinpay_public_key;
	private static String notify_url;// 
	private static String interface_version;// 
	private static String sign_type;// 
	private static String product_name;// 
	
	private static String input_charset;
	// H5配置(手机端，扫码接口)，b2c网银网关
	private static String h5_merchant_code;
	private static String h5_merchant_private_key;
	private static String h5_dinpay_public_key;
	private static String h5_apiUrl;
	private static String h5_interface_version;// 
	private static String h5_notify_url;//
	private static String h5_sign_type;//
	private static String h5_product_name;//
	private static String redo_flag;
	
	public TBFPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = new JSONObject().fromObject(pmap);
		if (jo.containsKey("scan_qrcode")) {
			JSONObject scan_qrcode = jo.getJSONObject("scan_qrcode");
			merchant_code = scan_qrcode.get("merchant_code").toString();
			merchant_private_key = scan_qrcode.get("merchant_private_key").toString();
			dinpay_public_key = scan_qrcode.get("dinpay_public_key").toString();
			notify_url = scan_qrcode.get("notify_url").toString();
			interface_version = scan_qrcode.get("interface_version").toString();
			sign_type = scan_qrcode.get("sign_type").toString();
			product_name = scan_qrcode.get("product_name").toString();
			scanUrl = scan_qrcode.get("scanUrl").toString();
		}
		input_charset = jo.get("input_charset").toString();
		if (jo.containsKey("h5_or_gateway")) {
			JSONObject h5_or_gateway = jo.getJSONObject("h5_or_gateway");
			h5_merchant_code = h5_or_gateway.get("h5_merchant_code").toString();
			h5_merchant_private_key = h5_or_gateway.get("h5_merchant_private_key").toString();
			h5_dinpay_public_key = h5_or_gateway.get("h5_dinpay_public_key").toString();
			h5_apiUrl = h5_or_gateway.get("h5_apiUrl").toString();
			h5_interface_version = h5_or_gateway.get("h5_interface_version").toString();
			h5_notify_url = h5_or_gateway.get("h5_notify_url").toString();
			h5_sign_type = h5_or_gateway.get("h5_sign_type").toString();
			h5_product_name = h5_or_gateway.get("h5_product_name").toString();
			redo_flag = h5_or_gateway.get("redo_flag").toString();
		}
	}
	public static void main(String[] args) {
		Map<String, Object> configMap = new HashMap<String, Object>();
		configMap.put("input_charset", "UTF-8");
		
		Map<String, String> scan_qrcode = new HashMap<String, String>();
		scan_qrcode.put("merchant_code", "100100001389");
		scan_qrcode.put("merchant_private_key", "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAMbnr/rWC3AOiFUF9Xziplpq9z9Oz0SK2oOUFFuoFC8fZU9JLeyEOL+ftK46cn1UUKvAd4c35afxZPlaRc+9nSnPc25xdB7y90WY6oVSY6/FvMquG2rFPVIEIDBXoZ0s35LVqEOk9CSr2z46j2kPyrJ75WAHc1p+DMRNm9s1ouevAgMBAAECgYAdmCVX5fcHsjHYrK1M6pAjzdyBv+EY6qv+fc7VoFl4dWxmcpXxpe6nnESO08VrHltuYpGmPcvexAzpCORwN18lsSu4zFUZ0/+T+lcm0a0RBI+y1XMXWBISdkjbFjd6K45sd/ADGncpnySdCTgiXv6UC8AK7bK55AKytaAENyUC4QJBAO0eYd+Q/5X98LDoTW+0d7H1ymGwDdrbJa6Hnn7DbES4LRgP2JzDt1IQEKZEiQ4lJlIsLVmsesCulEetw5+K/J8CQQDWvlPLdtkUUbqCo8DUU6C7/gt6jJROgRYJHE0WNUO0ITNt0u/CHGtmY6ep16HIIEhXTCy53NU4CTznvtbyByrxAkEA6fxEOphcDggTno7t8yATyXGxbyqK0X/VtF8x4Qc47KsOHKNubmuXmAwwUdJpqKKyKP5dFhNk4oSj4AOxedtuIwJBAMIy3s6ajRfeDUBFXIXnlu1Lgg/yf9A61rWofWH/C1ojyEGiR6aQUuQGbUKgObMk1qbzwWLX/Y6udADTyciYiNECQQDffEjFv/Ox7uz9QVDPJsJk/fUbhCK26COpygDS04iaKOrH7yTsQSk1dYLqLwiFkWqDZQkHDUl0gl7DsgUWRd/M");
		scan_qrcode.put("dinpay_public_key", "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCFFtP+B1pIvwaWsWjoYd067wsZTtRjYi+AovH2BV9gMgKjjgRirg2OrRXU8btOmFSI30O8jjvJOnggOwGlEv5ovgx72BP+h4nrsugKyygOHxK9icf5k+zM4RYgr5uOD4gzHKlXLF6YuBgve7M6M7gEjPmXejQQkhB2sBfkBvKosQIDAQAB");
		scan_qrcode.put("notify_url", "https://localhost/Notify/TBFNotify/bank.do");
		scan_qrcode.put("interface_version", "V3.1");
		scan_qrcode.put("sign_type", "RSA-S");
		scan_qrcode.put("product_name", "游戏充值");
		scan_qrcode.put("scanUrl", "https://api.ztbaofu.com/gateway/api/scanpay");
		configMap.put("scan_qrcode", scan_qrcode);
		
		Map<String, String> h5_or_gateway = new HashMap<String, String>();
		h5_or_gateway.put("h5_merchant_code", "123001002003");
		h5_or_gateway.put("h5_merchant_private_key", "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALIgluJzrKhOw/+sKlUZW+GFISjeXCqNz45rhEd4pRhg92ZDwyJxsIWVMUggCJLjSAke2wmVOiYJB/V9rNwlCzal5BGCSD0y8VckUb8LMv5wnNxr3wjrXf6IbZWsgNOwZg1mo+Cji5LCwoKvYvbZNK33Nb9MwbBh1PHUVP8AsfM3AgMBAAECgYEAr6oyAtse39Dlu+OWz9u1X/+BhyNa82Bs20Au8KkK77LY6NJUw0gpVGOgeUeWDP31kYELdDTlZpMrdS9eZLBnj/QofFTx7GSeod+vV13cgA6rc0yzjTp25Dm7Xzihf15R5JiNIFzlSYC2TLz+HcJoprxY6Pf6I/1qBjZuoC67eEECQQDjDhEI7s010aXXYQy3xwC/RUDosnfMARqRCpYFCYmoyMiUZ7+ohIvWkkCcwHx7VNKnXfmF0ezdXNT2TCKfXj6hAkEAyNXFKkCPtbg+GFqUlxlfta1s7FJuC1b8ZyaA1ygqUK5PJUoEKR9UcDg0uCKx4Zofpm46WCHx8w8M0+Abss8a1wJAA5JqFDDli44zxLKjJ5T63wdw4PhFyDDQQS3gdE3VG5GlDiifrEABjyuX1p90leAcvENPNJq71jOqqgFCni02YQJAQ8q09SA54lNA0qOwyJhOEFtsCxGAB9/i70a18uqh7f4IxUOIyADFVeQDF6zOcqK90EYg96Ltsuf/on1hnCgAnQJBANGvRflfL1Xvelv2jb446Gnq83IwQ6WJvO8z7/awfMmDsC88MI2bE0xcWJ2QPZZEVJkgCmwOXc26G+z0eei/z/U=");
		h5_or_gateway.put("h5_dinpay_public_key", "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC9R4Md8mcLZoSMQUuDLD7f1Rau7x+yfAsvmzPWyc98uI/ZwBbVuS3lGZk+YXy1Kwk+UywDr8vy3o3siymxW8XBzYFYR6CNWl6CEwfa1PwwoyefGH+7P/SVz9XZ+wJR/3fQ8JurscZmVQHrYUOqcCMUPyohzN2FTCz8oWbF3uQ1NwIDAQAB");
		h5_or_gateway.put("h5_apiUrl", "https://pay.ztbaofu.com/gateway");
		h5_or_gateway.put("h5_interface_version", "V3.0");
		h5_or_gateway.put("h5_notify_url", "https://localhost/Notify/TBFNotify/bank.do");
		h5_or_gateway.put("h5_sign_type", "RSA-S");
		h5_or_gateway.put("h5_product_name", "H5端游戏充值");
		h5_or_gateway.put("redo_flag", "1");
		configMap.put("h5_or_gateway", h5_or_gateway);
		
		System.out.println(JSONObject.fromObject(configMap).toString());
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

		Map<String, String> shbReqMap = new HashMap<String, String>();
		shbReqMap.put("client_ip", ip);// ip
		shbReqMap.put("return_url", refereUrl);// 返回连接地址
		String html = PayBank(String.valueOf(amount), pay_code, order_no, shbReqMap);
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
		String topay = payEntity.getTopay();

		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("refereUrl", refereUrl);
		reqMap.put("order_no", order_no);
		java.util.Date now = new java.util.Date();
		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateNow = format.format(now);
		reqMap.put("order_time", dateNow);
		reqMap.put("order_amount", String.valueOf(amount));
		// 支付类型，ip地址
		reqMap.put("scanpayType", pay_code);
		reqMap.put("topay", topay);
		reqMap.put("client_ip", ip);
		// 手机端
		if (!StringUtils.isEmpty(mobile)) {
			String resultstr = ScanPayH5(reqMap);
			XMLSerializer xmlSerializer = new XMLSerializer();
			String result2 = xmlSerializer.read(resultstr).toString();
			JSONObject responseJson = JSONObject.fromObject(JSONObject.fromObject(result2).get("response"));
			if (!("SUCCESS".equals(responseJson.getString("resp_code")) && responseJson.containsKey("payURL"))) {
				if (responseJson.containsKey("result_desc")) {
					return PayUtil.returnPayJson("error", "4", responseJson.get("result_desc").toString(), userName,
							amount, order_no, "");
				} else if (responseJson.containsKey("resp_desc")) {
					return PayUtil.returnPayJson("error", "4", responseJson.get("resp_desc").toString(), userName,
							amount, order_no, "");
				}
			}
			return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
					URLDecoder.decode(responseJson.get("payURL").toString()));
		} else {// PC端扫码
			String resultstr = ScanPay(reqMap);
			// 创建 XMLSerializer对象
			XMLSerializer xmlSerializer = new XMLSerializer();
			// 将xml转为json（注：如果是元素的属性，会在json里的key前加一个@标识）
			String result2 = xmlSerializer.read(resultstr).toString();
			JSONObject responseJson = JSONObject.fromObject(JSONObject.fromObject(result2).get("response"));
			// 获取二维码失败
			if (!("SUCCESS".equals(responseJson.get("resp_code").toString())
					&& "0".equals(responseJson.get("result_code").toString()))) {
				if (responseJson.containsKey("result_desc")) {
					return PayUtil.returnPayJson("error", "2", responseJson.get("result_desc").toString(), userName,
							amount, order_no, "");
				} else if (responseJson.containsKey("resp_desc")) {
					return PayUtil.returnPayJson("error", "2", responseJson.get("resp_desc").toString(), userName,
							amount, order_no, "");
				}
			}
			return PayUtil.returnPayJson("success", "2", " 支付接口请求成功!", userName, amount, order_no, responseJson.getString("qrcode"));
		}
	}

	public String ScanPayH5(Map<String, String> scanMap) {
		String url = h5_apiUrl;  // 
		String result = null;
		
		String service_type = scanMap.get("scanpayType").toString();
		String client_ip = scanMap.get("client_ip").toString();
		String order_no = scanMap.get("order_no").toString();
		String order_time = scanMap.get("order_time").toString();
		String order_amount = scanMap.get("order_amount").toString();
		String return_url = scanMap.get("refereUrl").toString();
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("merchant_code", h5_merchant_code);
		reqMap.put("service_type", service_type);
		reqMap.put("interface_version", h5_interface_version);
		reqMap.put("input_charset", input_charset);
		reqMap.put("notify_url", notify_url);
		reqMap.put("sign_type", sign_type);
		reqMap.put("order_no", order_no);
		reqMap.put("order_time", order_time);
		reqMap.put("order_amount", order_amount);
		reqMap.put("product_name", product_name);
		reqMap.put("return_url", return_url);
		reqMap.put("redo_flag", redo_flag);
		/** 数据签名
		签名规则定义如下：
		（1）参数列表中，除去sign_type、sign两个参数外，其它所有非空的参数都要参与签名，值为空的参数不用参与签名；
		（2）签名参数排序按照参数名a到z的顺序排序，若遇到相同首字母，则看第二个字母，以此类推，组成规则如下：
		参数名1=参数值1&参数名2=参数值2&……&参数名n=参数值n		*/
		StringBuffer signSrc= new StringBuffer();	
		signSrc.append("input_charset=").append(input_charset).append("&");			
		signSrc.append("interface_version=").append(interface_version).append("&");
		signSrc.append("merchant_code=").append(merchant_code).append("&");
		signSrc.append("notify_url=").append(notify_url).append("&");					
		signSrc.append("order_amount=").append(order_amount).append("&");
		signSrc.append("order_no=").append(order_no).append("&");		
		signSrc.append("order_time=").append(order_time).append("&");
		signSrc.append("product_name=").append(product_name).append("&");
		if (null != redo_flag && !"".equals(redo_flag)) {
			signSrc.append("redo_flag=").append(redo_flag).append("&");	
		}
		if (null != return_url && !"".equals(return_url)) {
			signSrc.append("return_url=").append(return_url).append("&");	
		}		
		signSrc.append("service_type=").append(service_type);
		
		String signInfo = signSrc.toString();
		String sign = "";
		if ("RSA-S".equals(sign_type)) { // sign_type = "RSA-S"
			try {
				/** 
				1)merchant_private_key，商户私钥，商户按照《密钥对获取工具说明》操作并获取商户私钥；获取商户私钥的同时，也要获取商户公钥（merchant_public_key）；调试运行
				代码之前首先先将商户公钥上传到商家后台"支付管理"->"公钥管理"（如何获取和上传请查看《密钥对获取工具说明》），不上传商户公钥会导致调试运行代码时报错。
	  			2)demo提供的merchant_private_key是测试商户号123001002003的商户私钥，请自行获取商户私钥并且替换	*/	
				//String merchant_private_key ="MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALIgluJzrKhOw/+sKlUZW+GFISjeXCqNz45rhEd4pRhg92ZDwyJxsIWVMUggCJLjSAke2wmVOiYJB/V9rNwlCzal5BGCSD0y8VckUb8LMv5wnNxr3wjrXf6IbZWsgNOwZg1mo+Cji5LCwoKvYvbZNK33Nb9MwbBh1PHUVP8AsfM3AgMBAAECgYEAr6oyAtse39Dlu+OWz9u1X/+BhyNa82Bs20Au8KkK77LY6NJUw0gpVGOgeUeWDP31kYELdDTlZpMrdS9eZLBnj/QofFTx7GSeod+vV13cgA6rc0yzjTp25Dm7Xzihf15R5JiNIFzlSYC2TLz+HcJoprxY6Pf6I/1qBjZuoC67eEECQQDjDhEI7s010aXXYQy3xwC/RUDosnfMARqRCpYFCYmoyMiUZ7+ohIvWkkCcwHx7VNKnXfmF0ezdXNT2TCKfXj6hAkEAyNXFKkCPtbg+GFqUlxlfta1s7FJuC1b8ZyaA1ygqUK5PJUoEKR9UcDg0uCKx4Zofpm46WCHx8w8M0+Abss8a1wJAA5JqFDDli44zxLKjJ5T63wdw4PhFyDDQQS3gdE3VG5GlDiifrEABjyuX1p90leAcvENPNJq71jOqqgFCni02YQJAQ8q09SA54lNA0qOwyJhOEFtsCxGAB9/i70a18uqh7f4IxUOIyADFVeQDF6zOcqK90EYg96Ltsuf/on1hnCgAnQJBANGvRflfL1Xvelv2jb446Gnq83IwQ6WJvO8z7/awfMmDsC88MI2bE0xcWJ2QPZZEVJkgCmwOXc26G+z0eei/z/U=";
				sign = RSAWithSoftware.signByPrivateKey(signInfo, h5_merchant_private_key);	// 签名   signInfo签名参数排序，  merchant_private_key商户私钥
				reqMap.put("sign", sign);
				//return HttpUtil.HtmlFrom(url, reqMap);
				result = doPost(url, reqMap, "utf-8");
			} catch (Exception e) {
				logger.info("通宝付HR支付签名异常！");
				e.printStackTrace();
			}
		}
//		if("RSA".equals(sign_type)){ // 数字证书加密方式  sign_type = "RSA"
//			
//			// 请在商家后台"支付管理"->"证书下载"处申请和下载pfx数字证书，一般要1~3个工作日才能获取到，123001002003.pfx是测试商户号123001002003的数字证书
//			String webRootPath = request.getSession().getServletContext().getRealPath("/");
//			String merPfxPath = webRootPath + "pfx/123001002003.pfx";						// 商家的pfx证书文件路径
//			String merPfxPass = "123001002003";											// 商家的pfx证书密码，初始密码是商户号
//			RSAWithHardware mh = new RSAWithHardware();						
//			mh.initSigner(merPfxPath, merPfxPass);		  
//			sign = mh.signByPriKey(signInfo);											// 签名   signInfo签名参数排序
//			System.out.println("RSA商户pfx证书文件路径：" + merPfxPath.length() + " -->" + merPfxPath);
//			System.out.println("RSA签名参数排序：" + signInfo.length() + " -->" + signInfo);
//			System.out.println("RSA签名：" + sign.length() + " -->" + sign + "\n");
//		}
		logger.info("signInfo:" + signInfo.length() + " --> " + signInfo);
		logger.info("sign:" + sign.length() + " --> " + sign);
		logger.info("扫码响应:" + result);
		return result;
	}
	
	public String ScanPay(Map<String, String> scanMap) {
		String reqUrl = scanUrl;

		String result = null;
		
		String	service_type = scanMap.get("scanpayType").toString();
		String client_ip = scanMap.get("client_ip").toString();// "0.0.0.0"; //
		// req.getParameter("client_ip");
		// String sign_type = (String) req.getParameter("sign_type");
		String order_no = scanMap.get("order_no").toString(); // (String)
		String order_time = scanMap.get("order_time").toString(); // (String)
		String order_amount = scanMap.get("order_amount").toString(); // (String)
		// String product_name = scanMap.get("product_name").toString();

		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("merchant_code", merchant_code);
		reqMap.put("service_type", service_type);
		reqMap.put("notify_url", notify_url);
		reqMap.put("interface_version", interface_version);
		reqMap.put("client_ip", client_ip);
		reqMap.put("sign_type", sign_type);
		reqMap.put("order_no", order_no);
		reqMap.put("order_time", order_time);
		reqMap.put("order_amount", order_amount);
		reqMap.put("product_name", product_name);

		/** 数据签名
		签名规则定义如下：
		（1）参数列表中，除去sign_type、sign两个参数外，其它所有非空的参数都要参与签名，值为空的参数不用参与签名；
		（2）签名参数排序按照参数名a到z的顺序排序，若遇到相同首字母，则看第二个字母，以此类推，组成规则如下：
		参数名1=参数值1&参数名2=参数值2&……&参数名n=参数值n		*/
		
		StringBuffer signSrc= new StringBuffer();	
		signSrc.append("client_ip=").append(client_ip).append("&");	
		signSrc.append("interface_version=").append(interface_version).append("&");
		signSrc.append("merchant_code=").append(merchant_code).append("&");				
		signSrc.append("notify_url=").append(notify_url).append("&");	
		signSrc.append("order_amount=").append(order_amount).append("&");
		signSrc.append("order_no=").append(order_no).append("&");
		signSrc.append("order_time=").append(order_time).append("&");
		signSrc.append("product_name=").append(product_name).append("&");
		signSrc.append("service_type=").append(service_type);

		String signInfo = signSrc.toString();
		String sign = "";
		if ("RSA-S".equals(sign_type)) { // sign_type = "RSA-S"
			// String merchant_private_key =
			// "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALf/+xHa1fDTCsLYPJLHy80aWq3djuV1T34sEsjp7UpLmV9zmOVMYXsoFNKQIcEzei4QdaqnVknzmIl7n1oXmAgHaSUF3qHjCttscDZcTWyrbXKSNr8arHv8hGJrfNB/Ea/+oSTIY7H5cAtWg6VmoPCHvqjafW8/UP60PdqYewrtAgMBAAECgYEAofXhsyK0RKoPg9jA4NabLuuuu/IU8ScklMQIuO8oHsiStXFUOSnVeImcYofaHmzIdDmqyU9IZgnUz9eQOcYg3BotUdUPcGgoqAqDVtmftqjmldP6F6urFpXBazqBrrfJVIgLyNw4PGK6/EmdQxBEtqqgXppRv/ZVZzZPkwObEuECQQDenAam9eAuJYveHtAthkusutsVG5E3gJiXhRhoAqiSQC9mXLTgaWV7zJyA5zYPMvh6IviX/7H+Bqp14lT9wctFAkEA05ljSYShWTCFThtJxJ2d8zq6xCjBgETAdhiH85O/VrdKpwITV/6psByUKp42IdqMJwOaBgnnct8iDK/TAJLniQJABdo+RodyVGRCUB2pRXkhZjInbl+iKr5jxKAIKzveqLGtTViknL3IoD+Z4b2yayXg6H0g4gYj7NTKCH1h1KYSrQJBALbgbcg/YbeU0NF1kibk1ns9+ebJFpvGT9SBVRZ2TjsjBNkcWR2HEp8LxB6lSEGwActCOJ8Zdjh4kpQGbcWkMYkCQAXBTFiyyImO+sfCccVuDSsWS+9jrc5KadHGIvhfoRjIj2VuUKzJ+mXbmXuXnOYmsAefjnMCI6gGtaqkzl527tw=";
			try {
				sign = RSAWithSoftware.signByPrivateKey(signInfo,merchant_private_key);	// 签名   signInfo签名参数排序，  merchant_private_key商户私钥  				
				reqMap.put("sign", sign);
				result = doPost(reqUrl, reqMap, "utf-8"); // ���Ǹ�����POST����
			} catch (Exception e) {
				logger.info("通宝付扫码签名异常！");
				e.printStackTrace();
			}
		}

//		if("RSA".equals(sign_type)){ // 数字证书加密方式 sign_type = "RSA"
//			
//			// 请在商家后台"支付管理"->"证书下载"处申请和下载pfx数字证书，一般要1~3个工作日才能获取到，123001002003.pfx是测试商户号123001002003的数字证书
//			String webRootPath = request.getSession().getServletContext().getRealPath("/");
//			String merPfxPath = webRootPath + "pfx/588001002211.pfx"; 				// 商家的pfx证书文件路径
//			String merPfxPass = "87654321";			  								// 商家的pfx证书密码,初始密码是商户号
//			RSAWithHardware mh = new RSAWithHardware();						
//			mh.initSigner(merPfxPath, merPfxPass);	  
//			sign = mh.signByPriKey(signInfo);		  								// 签名   signInfo签名参数排序
//			reqMap.put("sign", sign);				
//			result= new HttpClientUtil().doPost(reqUrl, reqMap, "utf-8");			// 向发送POST请求	
//		}

		logger.info("signInfo:" + signInfo.length() + " --> " + signInfo);
		logger.info("sign:" + sign.length() + " --> " + sign);
		logger.info("扫码响应:" + result);
		return result;
	}
	
	public String PayBank(String order_amount, String bank_code, String order_no, Map<String, String> reqMap) {
		// 接收表单提交参数
		Map<String, String> shbMap = new TreeMap<String, String>();
		shbMap.put("merchant_code", h5_merchant_code);// 商 家 号
		shbMap.put("service_type", "direct_pay");// 服务类型
		shbMap.put("interface_version", h5_interface_version);// 接口版本
		shbMap.put("input_charset", input_charset);// 字符编码
		// 服务器异步通知地址
		shbMap.put("notify_url", h5_notify_url);
		// 签名方式
		shbMap.put("sign_type", h5_sign_type);
		// 商户订单号
		shbMap.put("order_no", order_no);
		// 商户订单时间
		java.util.Date now = new java.util.Date();
		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateNow = format.format(now);
		shbMap.put("order_time", dateNow);
		// 商户订单金额
		shbMap.put("order_amount", order_amount);
		// 商品名称
		shbMap.put("product_name", h5_product_name);
		// 页面跳转同步通知地址
		shbMap.put("return_url", reqMap.get("return_url").toString());
		// TODO银行直连代码
		shbMap.put("bank_code", bank_code);
		// TODO订单是否允许重复提交//0是,1否,""空
		shbMap.put("redo_flag", redo_flag);

		// TODO支付类型取值如下（必须小写，多选时请用逗号隔开）b2c,plateform,dcard,express,weixin,alipay
//		if (bank_code.equals("WAP_UNION")) {
//			shbMap.put("pay_type", pay_type + "wap");
//		} else {
//			shbMap.put("pay_type", pay_type);
//		}
		// TODO业务扩展参数
		//shbMap.put("extend_param", extend_param);
		// TODO回传参数
//		shbMap.put("extra_return_param", extra_return_param);
		// TODO商品展示
//		shbMap.put("show_url", show_url);
	
		// 接收表单提交参数
		
		/** 数据签名
		签名规则定义如下：
		（1）参数列表中，除去sign_type、sign两个参数外，其它所有非空的参数都要参与签名，值为空的参数不用参与签名；
		（2）签名参数排序按照参数名a到z的顺序排序，若遇到相同首字母，则看第二个字母，以此类推，组成规则如下：
		参数名1=参数值1&参数名2=参数值2&……&参数名n=参数值n		*/
		StringBuilder sb = new StringBuilder();
		for (String key : shbMap.keySet()) {
		    	if ("sign".equalsIgnoreCase(key)||"sign_type".equalsIgnoreCase(key)) {
		    		continue;
				}
				String value = String.valueOf(shbMap.get(key));
			    if (StringUtils.isBlank(value)) {
				        continue;
				}
				sb.append(key + "=" + value + "&");
		}
		String signInfo = sb.substring(0, sb.length() - 1);
		
		String sign = "";
		if ("RSA-S".equals(sign_type)) {// sign_type = "RSA-S"
			/** 
			1)merchant_private_key，商户私钥，商户按照《密钥对获取工具说明》操作并获取商户私钥；获取商户私钥的同时，也要获取商户公钥（merchant_public_key）；调试运行
			代码之前首先先将商户公钥上传到商家后台"支付管理"->"公钥管理"（如何获取和上传请查看《密钥对获取工具说明》），不上传商户公钥会导致调试运行代码时报错。
  			2)demo提供的merchant_private_key是测试商户号123001002003的商户私钥，请自行获取商户私钥并且替换	*/	
			//String merchant_private_key ="MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALIgluJzrKhOw/+sKlUZW+GFISjeXCqNz45rhEd4pRhg92ZDwyJxsIWVMUggCJLjSAke2wmVOiYJB/V9rNwlCzal5BGCSD0y8VckUb8LMv5wnNxr3wjrXf6IbZWsgNOwZg1mo+Cji5LCwoKvYvbZNK33Nb9MwbBh1PHUVP8AsfM3AgMBAAECgYEAr6oyAtse39Dlu+OWz9u1X/+BhyNa82Bs20Au8KkK77LY6NJUw0gpVGOgeUeWDP31kYELdDTlZpMrdS9eZLBnj/QofFTx7GSeod+vV13cgA6rc0yzjTp25Dm7Xzihf15R5JiNIFzlSYC2TLz+HcJoprxY6Pf6I/1qBjZuoC67eEECQQDjDhEI7s010aXXYQy3xwC/RUDosnfMARqRCpYFCYmoyMiUZ7+ohIvWkkCcwHx7VNKnXfmF0ezdXNT2TCKfXj6hAkEAyNXFKkCPtbg+GFqUlxlfta1s7FJuC1b8ZyaA1ygqUK5PJUoEKR9UcDg0uCKx4Zofpm46WCHx8w8M0+Abss8a1wJAA5JqFDDli44zxLKjJ5T63wdw4PhFyDDQQS3gdE3VG5GlDiifrEABjyuX1p90leAcvENPNJq71jOqqgFCni02YQJAQ8q09SA54lNA0qOwyJhOEFtsCxGAB9/i70a18uqh7f4IxUOIyADFVeQDF6zOcqK90EYg96Ltsuf/on1hnCgAnQJBANGvRflfL1Xvelv2jb446Gnq83IwQ6WJvO8z7/awfMmDsC88MI2bE0xcWJ2QPZZEVJkgCmwOXc26G+z0eei/z/U=";
			try {
				sign = RSAWithSoftware.signByPrivateKey(signInfo, h5_merchant_private_key);
			} catch (Exception e) {
				e.printStackTrace();
			}	// 签名   signInfo签名参数排序，  merchant_private_key商户私钥
			System.out.println("RSA-S商家发送的签名字符串：" + signInfo.length() + " -->" + signInfo);
			System.out.println("RSA-S商家发送的签名：" + sign.length() + " -->" + sign + "\n");

		}
//		if("RSA".equals(sign_type)){ // 数字证书加密方式  sign_type = "RSA"
//			
//			// 请在商家后台"支付管理"->"证书下载"处申请和下载pfx数字证书，一般要1~3个工作日才能获取到，123001002003.pfx是测试商户号123001002003的数字证书
//			String webRootPath = request.getSession().getServletContext().getRealPath("/");
//			String merPfxPath = webRootPath + "pfx/123001002003.pfx";						// 商家的pfx证书文件路径
//			String merPfxPass = "123001002003";											// 商家的pfx证书密码，初始密码是商户号
//			RSAWithHardware mh = new RSAWithHardware();						
//			mh.initSigner(merPfxPath, merPfxPass);		  
//			sign = mh.signByPriKey(signInfo);											// 签名   signInfo签名参数排序
//			System.out.println("RSA商户pfx证书文件路径：" + merPfxPath.length() + " -->" + merPfxPath);
//			System.out.println("RSA签名参数排序：" + signInfo.length() + " -->" + signInfo);
//			System.out.println("RSA签名：" + sign.length() + " -->" + sign + "\n");
//		}

		String url = h5_apiUrl + "?input_charset=" + input_charset;
		shbMap.put("sign", sign);
		String HtmlStr = HttpUtil.HtmlFrom(url, shbMap);
		System.out.println(HtmlStr);
		return HtmlStr;
	}
	/**
	 * post 方法
	 * 
	 * @param url
	 * @param map
	 * @param charset
	 * @return
	 */
	public String doPost(String url, Map<String, String> map, String charset) {
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		try {
			httpClient = new SSLClient();
			httpPost = new HttpPost(url);
			// ���ò���
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			Iterator iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> elem = (Entry<String, String>) iterator.next();
				list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
			}
			if (list.size() > 0) {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, charset);
				//entity.setContentType("application/json");
				httpPost.setEntity(entity);
			}
			HttpResponse response = httpClient.execute(httpPost);
			if (response != null) {
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					result = EntityUtils.toString(resEntity, charset);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
	
	@Override
	public String callback(Map<String, String> infoMap) {
        StringBuilder sb = new StringBuilder();
        String dinpaySign = infoMap.get("sign");
        for (String key : infoMap.keySet()) {
            if ("sign".equalsIgnoreCase(key)||"sign_type".equalsIgnoreCase(key)) {
                continue;
            }
            String value = String.valueOf(infoMap.get(key));
            if (StringUtils.isBlank(value)) {
                continue;
            }
            sb.append(key + "=" + value + "&");
        }
        String signInfo = sb.substring(0, sb.length() - 1);
        logger.info("验签内容signInfo = " + signInfo);
        
        boolean result = false;
		if("RSA-S".equals(sign_type)){ // sign_type = "RSA-S"			
			/**
			1)dinpay_public_key，公钥，每个商家对应一个固定的公钥（不是使用工具生成的商户公钥merchant_public_key，不要混淆），
			     即为商家后台"支付管理"->"公钥管理"->"公钥"里的绿色字符串内容
			2)demo提供的dinpay_public_key是测试商户号123001002003的公钥，请自行复制对应商户号的公钥进行调整和替换	*/		
			//String dinpay_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC9R4Md8mcLZoSMQUuDLD7f1Rau7x+yfAsvmzPWyc98uI/ZwBbVuS3lGZk+YXy1Kwk+UywDr8vy3o3siymxW8XBzYFYR6CNWl6CEwfa1PwwoyefGH+7P/SVz9XZ+wJR/3fQ8JurscZmVQHrYUOqcCMUPyohzN2FTCz8oWbF3uQ1NwIDAQAB";
			try {
				result = RSAWithSoftware.validateSignByPublicKey(signInfo, h5_dinpay_public_key, dinpaySign);
			} catch (Exception e) {
				e.printStackTrace();
			}	// 验签   signInfo返回的签名参数排序， dinpay_public_key公钥， dinpaySign返回的签名
		}
//		if("RSA".equals(sign_type)){ // 数字证书加密方式  sign_type = "RSA"
//			
//			// 请在商家后台"支付管理"->"证书下载"处申请和下载pfx数字证书，一般要1~3个工作日才能获取到，123001002003.pfx是测试商户号123001002003的数字证书
//			String webRootPath = request.getSession().getServletContext().getRealPath("/");
//			String merPfxPath = webRootPath + "pfx/123001002003.pfx"; 									// 商家的pfx证书文件路径
//			String merPfxPass = "123001002003";			  												// 商家的pfx证书密码,初始密码是商户号
//			RSAWithHardware mh = new RSAWithHardware();						
//			mh.initSigner(merPfxPath, merPfxPass);		  							
//			result = mh.validateSignByPubKey(merchant_code, signInfo, dinpaySign);						// 验签    merchant_code为商户号， signInfo返回的签名参数排序， dinpaySign返回的签名
//		}
        if (result) {
            logger.info("验签成功");
            return "success";
        }
        logger.info("验签失败");
        return "fail";
    }
	
}
