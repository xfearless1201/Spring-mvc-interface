package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.mkt.util.HttpUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.ys.util.StringUtils;
import com.cn.tianxia.util.IPTools;
import com.cn.tianxia.util.SSLClient;
import com.itrus.util.sign.RSAWithHardware;
import com.itrus.util.sign.RSAWithSoftware;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

/**
 * 
 * @Description:TODO
 * 
 * @author:zouwei
 * 
 * @time:2017年7月6日 下午7:22:39
 * 
 */
public class WFPayServiceImpl implements PayService {
	private static String merchant_code;// 商 家 号
	private static String service_type;// 服务类型
	private static String interface_version;// 扫描支付接口版本
	private static String b2c_interface_version; // 网银支付接口版本
	private static String input_charset;// 字符编码
	private static String notify_url;// 服务器异步通知地址
	private static String sign_type;
	private static String return_url;
	private static String payurl;// 支付地址
	private static String merchant_private_key;// 商户私钥
	private static String dinpay_public_key;// 商户公钥
	private static String h5_version;// h5接口版本
	private static String h5_apiUrl;
	// 合作商家私钥pkcs12证书密码
	private static String pfxPass;

	// 扫描支付地址
	private static String scanPay;

	// private static String ret_str_success = "SUCCESS";
	// private static String ret_str_failed = "Signature Error";

	private static String product_name;// 商品名称
	private static String redo_flag;// 订单是否允许重复提交//0是,1否,""空
	private static String product_code;// 商品编号
	private static String product_num; // 商品数量
	private static String product_desc; // 商品描述
	private static String pay_type;// 支付类型取值如下（必须小写，多选时请用逗号隔开）b2c,plateform,dcard,express,weixin,alipay
	private static String extend_param;// 业务扩展参数
	private static String extra_return_param;// 回传参数
	private static String show_url;// 商品展示
	private static String scan_notify_url; // 扫描回调接口

	private final static Logger logger = LoggerFactory.getLogger(WFPayServiceImpl.class);

	public WFPayServiceImpl(Map<String, String> pmap) {
		// PlatFromConfig pf = new PlatFromConfig();
		// pf.InitData(pmap, "HS");
		JSONObject jo = new JSONObject().fromObject(pmap);
		merchant_code = jo.get("merchant_code").toString();
		service_type = jo.get("service_type").toString();
		interface_version = jo.get("interface_version").toString();
		input_charset = jo.get("input_charset").toString();
		notify_url = jo.get("notify_url").toString();
		sign_type = jo.get("sign_type").toString();
		return_url = jo.get("return_url").toString();
		pfxPass = jo.get("pfxPass").toString();
		payurl = jo.get("payurl").toString();
		scanPay = jo.get("scanPay").toString();
		merchant_private_key = jo.getString("merchant_private_key");
		dinpay_public_key = jo.getString("dinpay_public_key");
		b2c_interface_version = jo.getString("b2c_interface_version");
		/**** 新增参数 ******/
		product_name = jo.get("product_name").toString();// 商品名称
		redo_flag = jo.get("redo_flag").toString();// 订单是否允许重复提交//0是,1否,""空
		product_code = jo.get("product_code").toString();// 商品编号
		product_num = jo.get("product_num").toString(); // 商品数量
		product_desc = jo.get("product_desc").toString();// 商品描述
		pay_type = jo.get("pay_type").toString();// 支付类型取值如下（必须小写，多选时请用逗号隔开）b2c,plateform,dcard,express,weixin,alipay
		extend_param = jo.get("extend_param").toString();// 业务扩展参数
		extra_return_param = jo.get("extra_return_param").toString();// 回传参数
		show_url = jo.get("show_url").toString();// 商品展示
		scan_notify_url = jo.get("scan_notify_url").toString();// 扫描支付回调接口
		if (jo.containsKey("h5_version")) {
			h5_version = jo.get("h5_version").toString();// h5接口版本号
			h5_apiUrl = jo.get("h5_apiUrl").toString();// h5接口地址
		}
	}

	public String PayBank(String order_amount, String bank_code, String order_no, Map<String, String> reqMap) {
		// 接收表单提交参数
		Map<String, String> shbMap = new HashMap<String, String>();
		shbMap.put("merchant_code", merchant_code);// 商 家 号
		shbMap.put("service_type", service_type);// 服务类型
		shbMap.put("interface_version", b2c_interface_version);// 接口版本
		shbMap.put("input_charset", input_charset);// 字符编码
		// 服务器异步通知地址
		shbMap.put("notify_url", notify_url);
		// 签名方式
		shbMap.put("sign_type", sign_type);
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
		shbMap.put("product_name", product_name);
		// 页面跳转同步通知地址
		shbMap.put("return_url", reqMap.get("return_url").toString());
		// TODO银行直连代码
		shbMap.put("bank_code", bank_code);
		// TODO订单是否允许重复提交//0是,1否,""空
		shbMap.put("redo_flag", redo_flag);
		// 分割线------
		// TODO商品编号 可选项目
		shbMap.put("product_code", product_code);
		// TODO商品数量
		shbMap.put("product_num", product_num);
		// TODO商品描述
		shbMap.put("product_desc", product_desc);
		// TODO支付类型取值如下（必须小写，多选时请用逗号隔开）b2c,plateform,dcard,express,weixin,alipay
		if (bank_code.equals("WAP_UNION")) {
			shbMap.put("pay_type", pay_type + "wap");
		} else {
			shbMap.put("pay_type", pay_type);
		}
		// TODO客户端ip
		shbMap.put("client_ip", reqMap.get("client_ip").toString());
		// TODO业务扩展参数
		shbMap.put("extend_param", extend_param);
		// TODO回传参数
		shbMap.put("extra_return_param", extra_return_param);
		// TODO商品展示
		shbMap.put("show_url", show_url);
		// 接收表单提交参数
		// String bank_code = shbMap.get("bank_code");
		String client_ip = shbMap.get("client_ip");
		String extend_param = shbMap.get("extend_param");
		String extra_return_param = shbMap.get("extra_return_param");

		// String input_charset = shbMap.get("input_charset");
		String merchant_code = shbMap.get("merchant_code");
		String interface_version = shbMap.get("interface_version");
		String notify_url = shbMap.get("notify_url");
		// String order_amount = shbMap.get("order_amount");
		// String order_no = shbMap.get("order_no");
		String order_time = shbMap.get("order_time");
		String pay_type = shbMap.get("pay_type");
		String product_code = shbMap.get("product_code");
		String product_desc = shbMap.get("product_desc");
		String product_name = shbMap.get("product_name");
		String product_num = shbMap.get("product_num");
		String redo_flag = shbMap.get("redo_flag");
		String return_url = shbMap.get("return_url");
		String service_type = shbMap.get("service_type");
		String show_url = shbMap.get("show_url");

		// String sign_type = shbMap.get("sign_type");
		StringBuffer signSrc = new StringBuffer();
		if (!"".equals(bank_code)) {
			signSrc.append("bank_code=").append(bank_code).append("&");
		}
		if (!"".equals(client_ip)) {
			signSrc.append("client_ip=").append(client_ip).append("&");
		}
		if (!"".equals(extend_param)) {
			signSrc.append("extend_param=").append(extend_param).append("&");
		}
		if (!"".equals(extra_return_param)) {
			signSrc.append("extra_return_param=").append(extra_return_param).append("&");
		}

		signSrc.append("input_charset=").append(input_charset).append("&");
		signSrc.append("interface_version=").append(interface_version);
		signSrc.append("&merchant_code=").append(merchant_code);
		signSrc.append("&notify_url=").append(notify_url);
		signSrc.append("&order_amount=").append(order_amount);
		signSrc.append("&order_no=").append(order_no);
		signSrc.append("&order_time=").append(order_time);

		if (!"".equals(pay_type)) {
			signSrc.append("&pay_type=").append(pay_type);
		}
		if (!"".equals(product_code)) {
			signSrc.append("&product_code=").append(product_code);
		}
		if (!"".equals(product_desc)) {
			signSrc.append("&product_desc=").append(product_desc);
		}
		signSrc.append("&product_name=").append(product_name);
		if (!"".equals(product_num)) {
			signSrc.append("&product_num=").append(product_num);
		}
		if (!"".equals(redo_flag)) {
			signSrc.append("&redo_flag=").append(redo_flag);
		}
		if (!"".equals(return_url)) {
			signSrc.append("&return_url=").append(return_url);
		}

		signSrc.append("&service_type=").append(service_type);

		if (!"".equals(show_url)) {
			signSrc.append("&show_url=").append(show_url);
		}

		String signInfo = signSrc.toString();
		String sign = "";

		if ("RSA-S".equals(sign_type)) {// sign_type = "RSA-S"

			/**
			 * 1)merchant_private_key，商户私钥，商户按照《密钥对获取工具说明》操作并获取商户私钥。获取商户私钥的同时，也要
			 * 获取商户公钥（merchant_public_key）并且将商户公钥上传到速汇宝商家后台"公钥管理"（如何获取和上传请看《
			 * 密钥对获取工具说明》）， 不上传商户公钥会导致调试的时候报错“签名错误”。
			 * 2)demo提供的merchant_private_key是测试商户号1111110166的商户私钥，请自行获取商户私钥并且替换
			 */
			// testkey
			// String merchant_private_key =
			// "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALf/+xHa1fDTCsLYPJLHy80aWq3djuV1T34sEsjp7UpLmV9zmOVMYXsoFNKQIcEzei4QdaqnVknzmIl7n1oXmAgHaSUF3qHjCttscDZcTWyrbXKSNr8arHv8hGJrfNB/Ea/+oSTIY7H5cAtWg6VmoPCHvqjafW8/UP60PdqYewrtAgMBAAECgYEAofXhsyK0RKoPg9jA4NabLuuuu/IU8ScklMQIuO8oHsiStXFUOSnVeImcYofaHmzIdDmqyU9IZgnUz9eQOcYg3BotUdUPcGgoqAqDVtmftqjmldP6F6urFpXBazqBrrfJVIgLyNw4PGK6/EmdQxBEtqqgXppRv/ZVZzZPkwObEuECQQDenAam9eAuJYveHtAthkusutsVG5E3gJiXhRhoAqiSQC9mXLTgaWV7zJyA5zYPMvh6IviX/7H+Bqp14lT9wctFAkEA05ljSYShWTCFThtJxJ2d8zq6xCjBgETAdhiH85O/VrdKpwITV/6psByUKp42IdqMJwOaBgnnct8iDK/TAJLniQJABdo+RodyVGRCUB2pRXkhZjInbl+iKr5jxKAIKzveqLGtTViknL3IoD+Z4b2yayXg6H0g4gYj7NTKCH1h1KYSrQJBALbgbcg/YbeU0NF1kibk1ns9+ebJFpvGT9SBVRZ2TjsjBNkcWR2HEp8LxB6lSEGwActCOJ8Zdjh4kpQGbcWkMYkCQAXBTFiyyImO+sfCccVuDSsWS+9jrc5KadHGIvhfoRjIj2VuUKzJ+mXbmXuXnOYmsAefjnMCI6gGtaqkzl527tw=";
			// 自己获取的key
			// String merchant_private_key =
			// "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMM8dYl4MewcDPpsd4f4ICFMlyQJQqpCwTbAaTqkIP1DfcRDacTcCyjTRGEhhL/Jc4Dzl4woJnvt9brShIJKyjwrC8EUIDuPjrDG40neiMECdrFutZi2ECJR2Tod3v6dJj7HOkSiWu6l5mbP1GfWodfsAOuXORXT/1A/tDolj0yDAgMBAAECgYAnvj36/wHCm680pzHp9uOrg6jcyBnPCQnF+IMzUcf5ZmzKsCJu1ZBb/i3US/t3Ay5FGNFH0cCLx9QW9NXOpCTooZSTqs8Eo2oq/ydj6Cq9TFzegksQQMpOT92LxOvct8OvwQdJXRER1hu5tpFnm+a2WAmnm4Qv4s9UXm7npdPq+QJBAOccIIhHbwsiWs8ERrUU3lxjln6kD4RHyPfekfjg9eu8VbJMvovAJ+nQimU1Oc9WZtTBWdeAfTDuO9ZKsW6iqGUCQQDYQ0Rf0HFwKdGndkI0x+yRRUSEY2K0RqsENQlinXtkhAkwu1s2n2cX7qRl9OS9biF2Hlwrh1SXzHThFqyuB27HAkEAtFDIvStm4zwJt+1xMYW3vEPjPqnIhX/wuBxKrfDvTt8hybyBCs6BFbek0zRFB5U1JBg7fZ19j/jAbQDYEScN1QJAH8IBEHDzt5zshMMBBe819j9NyIMvw3l9M6j2L90geapKDzX0NxlBcmpBChfkX00E7OuYbWv6KDCGGWfTA9tjkQJBALwb+Noy+srFOrWdkev7PbMvkbN7GXVVuv4dq/JF+XY6ha7gdWh3wi7ckjYWrst8XZBG2oDZDXA2Ok/3H6S9TSI=";
			try {
				sign = RSAWithSoftware.signByPrivateKey(signInfo, merchant_private_key);
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("RSA-S商家发送的签名字符串：" + signInfo.length() + " -->" + signInfo);
			System.out.println("RSA-S商家发送的签名：" + sign.length() + " -->" + sign + "\n");

		}

		if ("RSA".equals(sign_type)) {// 数字证书加密方式 sign_type = "RSA"

			String rootPath = this.getClass().getResource("/").toString();

			//
			// 请在商家后台证书下载处申请和下载pfx数字证书，一般要1~3个工作日才能获取到,1111110166.pfx是测试商户号1111110166的数字证书
			String path = rootPath.substring(rootPath.indexOf("/") + 1, rootPath.length() - 8) + "1111110166.pfx";
			System.out.println("rootPath:" + path);
			// String pfxPass = "87654321"; // 证书密钥，初始密码是商户号
			RSAWithHardware mh = new RSAWithHardware();
			try {
				mh.initSigner(path, pfxPass);
			} catch (Exception e) {
				e.printStackTrace();
			}
			sign = mh.signByPriKey(signInfo);
			System.out.println("path：" + path);
			System.out.println("RSA商家发送的签名字符串：" + signInfo.length() + " -->" + signInfo);
			System.out.println("RSA商家发送的签名：" + sign.length() + " -->" + sign + "\n");
		}

		String url = payurl + "?input_charset=" + input_charset;
		shbMap.put("sign", sign);
		String HtmlStr = HttpUtil.HtmlFrom(url, shbMap);
		System.out.println(HtmlStr);
		return HtmlStr;
	}

	public String offlineNotify(HttpServletRequest request) {
		// 接收速汇宝返回的参数
		try {
			request.setCharacterEncoding("utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		String interface_version = (String) request.getParameter("interface_version");
		String merchant_code = (String) request.getParameter("merchant_code");
		String notify_type = (String) request.getParameter("notify_type");
		String notify_id = (String) request.getParameter("notify_id");
		String sign_type = (String) request.getParameter("sign_type");
		String dinpaySign = (String) request.getParameter("sign");
		String order_no = (String) request.getParameter("order_no");
		String order_time = (String) request.getParameter("order_time");
		String order_amount = (String) request.getParameter("order_amount");
		String extra_return_param = (String) request.getParameter("extra_return_param");
		String trade_no = (String) request.getParameter("trade_no");
		String trade_time = (String) request.getParameter("trade_time");
		String trade_status = (String) request.getParameter("trade_status");
		String bank_seq_no = (String) request.getParameter("bank_seq_no");

		/**
		 * 数据签名 签名规则定义如下：
		 * （1）参数列表中，除去sign_type、sign两个参数外，其它所有非空的参数都要参与签名，值为空的参数不用参与签名；
		 * （2）签名顺序按照参数名a到z的顺序排序，若遇到相同首字母，则看第二个字母，以此类推，组成规则如下：
		 * 参数名1=参数值1&参数名2=参数值2&……&参数名n=参数值n
		 */

		StringBuilder signStr = new StringBuilder();
		if (null != bank_seq_no && !bank_seq_no.equals("")) {
			signStr.append("bank_seq_no=").append(bank_seq_no).append("&");
		}
		if (null != extra_return_param && !extra_return_param.equals("")) {
			signStr.append("extra_return_param=").append(extra_return_param).append("&");
		}
		signStr.append("interface_version=").append(interface_version).append("&");
		signStr.append("merchant_code=").append(merchant_code).append("&");
		signStr.append("notify_id=").append(notify_id).append("&");
		signStr.append("notify_type=").append(notify_type).append("&");
		signStr.append("order_amount=").append(order_amount).append("&");
		signStr.append("order_no=").append(order_no).append("&");
		signStr.append("order_time=").append(order_time).append("&");
		signStr.append("trade_no=").append(trade_no).append("&");
		signStr.append("trade_status=").append(trade_status).append("&");
		signStr.append("trade_time=").append(trade_time);

		String signInfo = signStr.toString();

		// 验证参数
		if (StringUtils.isEmpty(order_no) || StringUtils.isEmpty(order_amount) || StringUtils.isEmpty(trade_no)
				|| StringUtils.isEmpty(trade_status)) {
			return "Signature Error"; // Signature Error
		}
		// System.out.println("速汇宝返回的签名字符串：" + signInfo.length() + " -->" +
		// signInfo);
		// System.out.println("速汇宝返回的签名：" + dinpaySign.length() + " -->" +
		// dinpaySign);

		boolean result = false;

		if ("RSA-S".equals(sign_type)) { // sign_type = "RSA-S"

			/**
			 * 1)dinpay_public_key，速汇宝公钥，每个商家对应一个固定的速汇宝公钥（
			 * 不是使用工具生成的密钥merchant_public_key，不要混淆），
			 * 即为速汇宝商家后台"公钥管理"->"速汇宝公钥"里的绿色字符串内容
			 * 2)demo提供的dinpay_public_key是测试商户号1111110166的速汇宝公钥，
			 * 请自行复制对应商户号的速汇宝公钥进行调整和替换
			 */
			// String dinpay_public_key =
			// "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDPHWJeDHsHAz6bHeH+CAhTJckCUKqQsE2wGk6pCD9Q33EQ2nE3Aso00RhIYS/yXOA85eMKCZ77fW60oSCSso8KwvBFCA7j46wxuNJ3ojBAnaxbrWYthAiUdk6Hd7+nSY+xzpEolrupeZmz9Rn1qHX7ADrlzkV0/9QP7Q6JY9MgwIDAQAB";
			try {
				logger.info("RSA-S商家发送的签名字符串：" + signInfo.length() + " -->" + signInfo);
				logger.info("RSA-S商家发送的签名：" + dinpaySign.length() + " -->" + dinpaySign + "\n");
				result = RSAWithSoftware.validateSignByPublicKey(signInfo, dinpay_public_key, dinpaySign);
			} catch (Exception e) {
				logger.info("签名异常！");
				e.printStackTrace();
			}

		}

		if ("RSA".equals(sign_type)) {// 数字证书加密方式 sign_type = "RSA"

			String rootPath = this.getClass().getResource("/").toString();

			// 请在商家后台证书下载处申请和下载pfx数字证书，一般要1~3个工作日才能获取到,1111110166.pfx是测试商户号1111110166的数字证书
			String path = rootPath.substring(rootPath.indexOf("/") + 1, rootPath.length() - 8)
					+ "certification/1111110166.pfx";
			// String pfxPass = "87654321"; // 证书密钥，初始密码是商户号

			RSAWithHardware mh = new RSAWithHardware();
			try {
				mh.initSigner(path, pfxPass);
			} catch (Exception e) {
				e.printStackTrace();
			}
			result = mh.validateSignByPubKey(merchant_code, signInfo, dinpaySign);
		}

		// PrintWriter pw = response.getWriter();

		if (result) {
			logger.info("速汇宝网银签名成功！");
			return "SUCCESS"; // 验签成功，响应SUCCESS
			// System.out.println("SUCCESS");
		} else {
			logger.info("速汇宝网银签名成功！");
			return "Signature Error"; // 验签失败，业务结束
			// System.out.println("Signature Error");
		}
	}

	public String ScanPay(Map<String, String> scanMap) {
		// try {
		// req.setCharacterEncoding("utf-8");
		// } catch (UnsupportedEncodingException e1) {
		// e1.printStackTrace();
		// }
		// res.setContentType("text/html;charset=utf-8");

		// ֧�������ַ
		String reqUrl = scanPay;

		// ֧�����󷵻ؽ��
		String result = null;

		// ���ձ?�ύ����
		// String merchant_code = (String) req.getParameter("merchant_code");
		String service_type = "";
		if ("WF".equals(scanMap.get("topay").toString()) && "qq_scan".equals(scanMap.get("scanpayType").toString())) {// tenpay_scan
			service_type = "tenpay_scan";
		} else {
			service_type = scanMap.get("scanpayType").toString();
		}

		// String notify_url = (String) req.getParameter("notify_url");
		// String interface_version = (String)
		// req.getParameter("interface_version");
		String client_ip = scanMap.get("client_ip").toString();// "0.0.0.0"; //
																// IPTools.getIp(req);
																// // (String)
		// req.getParameter("client_ip");
		// String sign_type = (String) req.getParameter("sign_type");
		String order_no = scanMap.get("order_no").toString(); // (String)
																// req.getParameter("order_no");
		String order_time = scanMap.get("order_time").toString(); // (String)
																	// req.getParameter("order_time");
		String order_amount = scanMap.get("order_amount").toString(); // (String)
																		// req.getParameter("order_amount");
		// String product_name = scanMap.get("product_name").toString();

		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("merchant_code", merchant_code);
		reqMap.put("service_type", service_type);
		reqMap.put("notify_url", scan_notify_url);
		reqMap.put("interface_version", interface_version);
		reqMap.put("client_ip", client_ip);
		reqMap.put("sign_type", sign_type);
		reqMap.put("order_no", order_no);
		reqMap.put("order_time", order_time);
		reqMap.put("order_amount", order_amount);
		reqMap.put("product_name", product_name);

		StringBuffer signSrc = new StringBuffer();
		signSrc.append("client_ip=").append(client_ip).append("&");
		signSrc.append("interface_version=").append(interface_version).append("&");
		signSrc.append("merchant_code=").append(merchant_code).append("&");
		signSrc.append("notify_url=").append(scan_notify_url).append("&");
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
				sign = RSAWithSoftware.signByPrivateKey(signInfo, merchant_private_key); // ǩ��
																							// signInfoǩ���������
																							// merchant_private_key�̻�˽Կ
				reqMap.put("sign", sign);
				result = doPost(reqUrl, reqMap, "utf-8"); // ���Ǹ�����POST����
			} catch (Exception e) {
				logger.info("速汇宝扫码签名异常！");
				e.printStackTrace();
			}
		}

		// if ("RSA".equals(sign_type)) { // ����֤����ܷ�ʽ sign_type = "RSA"
		//
		// String webRootPath =
		// req.getSession().getServletContext().getRealPath("/");
		// String merPfxPath = webRootPath + "pfx/1111110166.pfx"; //
		// �̼ҵ�pfx֤���ļ�·��
		// String merPfxPass = "87654321"; // �̼ҵ�pfx֤������,��ʼ�������̻���
		// RSAWithHardware mh = new RSAWithHardware();
		// try {
		// mh.initSigner(merPfxPath, merPfxPass);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// sign = mh.signByPriKey(signInfo); // ǩ�� signInfoǩ���������
		// reqMap.put("sign", sign);
		// result = doPost(reqUrl, reqMap, "utf-8"); // ���Ǹ�����POST����
		// }

		logger.info("signInfo:" + signInfo.length() + " --> " + signInfo);
		logger.info("sign:" + sign.length() + " --> " + sign);
		logger.info("扫码响应:" + result);
		return result;
	}

	public String scanPayNotity(HttpServletRequest request) {
		// 接收速汇宝返回的参数
		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String interface_version = (String) request.getParameter("interface_version");
		String merchant_code = (String) request.getParameter("merchant_code");
		String notify_type = (String) request.getParameter("notify_type");
		String notify_id = (String) request.getParameter("notify_id");
		String sign_type = (String) request.getParameter("sign_type");
		String dinpaySign = (String) request.getParameter("sign");
		String order_no = (String) request.getParameter("order_no");
		String order_time = (String) request.getParameter("order_time");
		String order_amount = (String) request.getParameter("order_amount");
		String extra_return_param = (String) request.getParameter("extra_return_param");
		String trade_no = (String) request.getParameter("trade_no");
		String trade_time = (String) request.getParameter("trade_time");
		String trade_status = (String) request.getParameter("trade_status");
		String bank_seq_no = (String) request.getParameter("bank_seq_no");

		logger.info("interface_version = " + interface_version + "\n" + "merchant_code = " + merchant_code + "\n"
				+ "notify_type = " + notify_type + "\n" + "notify_id = " + notify_id + "\n" + "sign_type = " + sign_type
				+ "\n" + "dinpaySign = " + dinpaySign + "\n" + "order_no = " + order_no + "\n" + "order_time = "
				+ order_time + "\n" + "order_amount = " + order_amount + "\n" + "extra_return_param = "
				+ extra_return_param + "\n" + "trade_no = " + trade_no + "\n" + "trade_time = " + trade_time + "\n"
				+ "trade_status = " + trade_status + "\n" + "bank_seq_no = " + bank_seq_no + "\n");

		/**
		 * 数据签名 签名规则定义如下：
		 * （1）参数列表中，除去sign_type、sign两个参数外，其它所有非空的参数都要参与签名，值为空的参数不用参与签名；
		 * （2）签名参数排序按照参数名a到z的顺序排序，若遇到相同首字母，则看第二个字母，以此类推，组成规则如下：
		 * 参数名1=参数值1&参数名2=参数值2&……&参数名n=参数值n
		 */

		StringBuilder signStr = new StringBuilder();
		if (null != bank_seq_no && !bank_seq_no.equals("")) {
			signStr.append("bank_seq_no=").append(bank_seq_no).append("&");
		}
		if (null != extra_return_param && !extra_return_param.equals("")) {
			signStr.append("extra_return_param=").append(extra_return_param).append("&");
		}
		signStr.append("interface_version=").append(interface_version).append("&");
		signStr.append("merchant_code=").append(merchant_code).append("&");
		signStr.append("notify_id=").append(notify_id).append("&");
		signStr.append("notify_type=").append(notify_type).append("&");
		signStr.append("order_amount=").append(order_amount).append("&");
		signStr.append("order_no=").append(order_no).append("&");
		signStr.append("order_time=").append(order_time).append("&");
		signStr.append("trade_no=").append(trade_no).append("&");
		signStr.append("trade_status=").append(trade_status).append("&");
		signStr.append("trade_time=").append(trade_time);

		String signInfo = signStr.toString();
		logger.info("速汇宝返回的签名参数排序：" + signInfo.length() + " -->" + signInfo);
		logger.info("速汇宝返回的签名：" + dinpaySign.length() + " -->" + dinpaySign);
		boolean result = false;

		if ("RSA-S".equals(sign_type)) { // sign_type = "RSA-S"

			/**
			 * 1)dinpay_public_key，速汇宝公钥，每个商家对应一个固定的速汇宝公钥（
			 * 不是使用工具生成的商户公钥merchant_public_key，不要混淆），
			 * 即为速汇宝商家后台"支付管理"->"公钥管理"->"速汇宝公钥"里的绿色字符串内容
			 * 2)demo提供的dinpay_public_key是测试商户号1111110166的速汇宝公钥，
			 * 请自行复制对应商户号的速汇宝公钥进行调整和替换
			 */

			// String dinpay_public_key =
			// "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDPHWJeDHsHAz6bHeH+CAhTJckCUKqQsE2wGk6pCD9Q33EQ2nE3Aso00RhIYS/yXOA85eMKCZ77fW60oSCSso8KwvBFCA7j46wxuNJ3ojBAnaxbrWYthAiUdk6Hd7+nSY+xzpEolrupeZmz9Rn1qHX7ADrlzkV0/9QP7Q6JY9MgwIDAQAB";
			try {
				result = RSAWithSoftware.validateSignByPublicKey(signInfo, dinpay_public_key, dinpaySign);
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("签名异常！");
			} // 验签 signInfo速汇宝返回的签名参数排序， dinpay_public_key速汇宝公钥，
				// dinpaySign速汇宝返回的签名
		}

		// if("RSA".equals(sign_type)){ // 数字证书加密方式 sign_type = "RSA"
		//
		// //
		// 请在商家后台"支付管理"->"证书下载"处申请和下载pfx数字证书，一般要1~3个工作日才能获取到，1111110166.pfx是测试商户号1111110166的数字证书
		// String webRootPath =
		// request.getSession().getServletContext().getRealPath("/");
		// String merPfxPath = webRootPath + "pfx/1111110166.pfx"; //
		// 商家的pfx证书文件路径
		// String merPfxPass = "87654321"; // 商家的pfx证书密码,初始密码是商户号
		// RSAWithHardware mh = new RSAWithHardware();
		// mh.initSigner(merPfxPath, merPfxPass);
		// result = mh.validateSignByPubKey(merchant_code, signInfo,
		// dinpaySign); // 验签 merchant_code为商户号， signInfo速汇宝返回的签名参数排序，
		// dinpaySign速汇宝返回的签名
		// }
		//
		// PrintWriter pw = response.getWriter();
		// if(result){
		// pw.write("SUCCESS"); // 验签成功，响应SUCCESS
		// System.out.println("验签结果result的值：" + result + " -->SUCCESS");
		// }else{
		// pw.write("Signature Error"); // 验签失败，业务结束
		// System.out.println("验签结果result的值：" + result + " -->Signature Error");
		// }
		// pw.flush();
		// pw.close();

		if (result) {
			logger.info("速汇宝扫码签名成功！");
			return "SUCCESS";
		} else {
			logger.info("速汇宝扫码签名失败！");
			return "Signature Error";
		}
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

	public static String TestdoPost(String url, Map<String, String> map, String charset) {
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

	/**
	 * 测试网银或者扫描支付回调接口
	 */
	public static void testbankingNotify() {
		String order_no = "SHBbl12017070910015010015066286628";
		String total_fee = "10";
		String sign_type = "RSA-S";
		String interface_version = "V3.1";
		String merchant_code = "1111110166";
		String notify_type = "offline_notify";
		String notify_id = "12345343";
		// TODO
		String dinpaySign = "";
		String order_time = "2017-06-0112:00:00";
		String order_amount = "10";
		String extra_return_param = "";
		String trade_no = "12345678";
		String trade_time = "2017-06-0112:00:00";
		String trade_status = "SUCCESS";
		String bank_seq_no = "";
		StringBuilder signStr = new StringBuilder();
		if (null != bank_seq_no && !bank_seq_no.equals("")) {
			signStr.append("bank_seq_no=").append(bank_seq_no).append("&");
		}
		if (null != extra_return_param && !extra_return_param.equals("")) {
			signStr.append("extra_return_param=").append(extra_return_param).append("&");
		}
		signStr.append("interface_version=").append(interface_version).append("&");
		signStr.append("merchant_code=").append(merchant_code).append("&");
		signStr.append("notify_id=").append(notify_id).append("&");
		signStr.append("notify_type=").append(notify_type).append("&");
		signStr.append("order_amount=").append(order_amount).append("&");
		signStr.append("order_no=").append(order_no).append("&");
		signStr.append("order_time=").append(order_time).append("&");
		signStr.append("trade_no=").append(trade_no).append("&");
		signStr.append("trade_status=").append(trade_status).append("&");
		signStr.append("trade_time=").append(trade_time);
		String signInfo = signStr.toString();
		String sign = "";
		// b2c测试key
		String merchant_private_key = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMM8dYl4MewcDPpsd4f4ICFMlyQJQqpCwTbAaTqkIP1DfcRDacTcCyjTRGEhhL/Jc4Dzl4woJnvt9brShIJKyjwrC8EUIDuPjrDG40neiMECdrFutZi2ECJR2Tod3v6dJj7HOkSiWu6l5mbP1GfWodfsAOuXORXT/1A/tDolj0yDAgMBAAECgYAnvj36/wHCm680pzHp9uOrg6jcyBnPCQnF+IMzUcf5ZmzKsCJu1ZBb/i3US/t3Ay5FGNFH0cCLx9QW9NXOpCTooZSTqs8Eo2oq/ydj6Cq9TFzegksQQMpOT92LxOvct8OvwQdJXRER1hu5tpFnm+a2WAmnm4Qv4s9UXm7npdPq+QJBAOccIIhHbwsiWs8ERrUU3lxjln6kD4RHyPfekfjg9eu8VbJMvovAJ+nQimU1Oc9WZtTBWdeAfTDuO9ZKsW6iqGUCQQDYQ0Rf0HFwKdGndkI0x+yRRUSEY2K0RqsENQlinXtkhAkwu1s2n2cX7qRl9OS9biF2Hlwrh1SXzHThFqyuB27HAkEAtFDIvStm4zwJt+1xMYW3vEPjPqnIhX/wuBxKrfDvTt8hybyBCs6BFbek0zRFB5U1JBg7fZ19j/jAbQDYEScN1QJAH8IBEHDzt5zshMMBBe819j9NyIMvw3l9M6j2L90geapKDzX0NxlBcmpBChfkX00E7OuYbWv6KDCGGWfTA9tjkQJBALwb+Noy+srFOrWdkev7PbMvkbN7GXVVuv4dq/JF+XY6ha7gdWh3wi7ckjYWrst8XZBG2oDZDXA2Ok/3H6S9TSI=";
		boolean result = false;
		try {
			sign = RSAWithSoftware.signByPrivateKey(signInfo, merchant_private_key);
			// b2c测试key
			String dinpay_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDPHWJeDHsHAz6bHeH+CAhTJckCUKqQsE2wGk6pCD9Q33EQ2nE3Aso00RhIYS/yXOA85eMKCZ77fW60oSCSso8KwvBFCA7j46wxuNJ3ojBAnaxbrWYthAiUdk6Hd7+nSY+xzpEolrupeZmz9Rn1qHX7ADrlzkV0/9QP7Q6JY9MgwIDAQAB";
			result = RSAWithSoftware.validateSignByPublicKey(signInfo, dinpay_public_key, sign); // 验签
																									// signInfo速汇宝返回的签名参数排序，
																									// dinpay_public_key速汇宝公钥，
																									// dinpaySign速汇宝返回的签名
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(result);
		System.out.println("RSA-S商家发送的签名字符串：" + signInfo.length() + " -->" + signInfo);
		System.out.println("RSA-S商家发送的签名：" + sign.length() + " -->" + sign + "\n");
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("interface_version", interface_version);
		reqMap.put("merchant_code", merchant_code);
		reqMap.put("notify_type", notify_type);
		reqMap.put("notify_id", notify_id);
		reqMap.put("sign_type", sign_type);
		reqMap.put("sign", sign);
		reqMap.put("order_time", order_time);
		reqMap.put("order_amount", order_amount);
		reqMap.put("trade_no", trade_no);
		reqMap.put("trade_time", trade_time);
		reqMap.put("trade_status", trade_status);
		reqMap.put("bank_seq_no", bank_seq_no);
		reqMap.put("trade_status", trade_status);
		reqMap.put("order_no", order_no);
		// btc回调测试
		String resultstr = WFPayServiceImpl.TestdoPost("http://localhost:8080/XPJ/PlatformPay/bankingNotify.do",
				reqMap, "utf-8");
		System.out.println(resultstr);
	}

	/**
	 * 测试扫描支付回调接口
	 */
	public static void testScanPayNotify() {
		String order_no = "DDBbl1201709061517121517128675";
		String total_fee = "10";
		String sign_type = "RSA-S";
		String interface_version = "V3.1";
		String merchant_code = "1111110166";
		String notify_type = "offline_notify";
		String notify_id = "12345343";
		// TODO
		String dinpaySign = "";
		String order_time = "2017-06-0112:00:00";
		String order_amount = "10";
		String extra_return_param = "";
		String trade_no = "12345678";
		String trade_time = "2017-06-0112:00:00";
		String trade_status = "SUCCESS";
		String bank_seq_no = "";
		StringBuilder signStr = new StringBuilder();
		if (null != bank_seq_no && !bank_seq_no.equals("")) {
			signStr.append("bank_seq_no=").append(bank_seq_no).append("&");
		}
		if (null != extra_return_param && !extra_return_param.equals("")) {
			signStr.append("extra_return_param=").append(extra_return_param).append("&");
		}
		signStr.append("interface_version=").append(interface_version).append("&");
		signStr.append("merchant_code=").append(merchant_code).append("&");
		signStr.append("notify_id=").append(notify_id).append("&");
		signStr.append("notify_type=").append(notify_type).append("&");
		signStr.append("order_amount=").append(order_amount).append("&");
		signStr.append("order_no=").append(order_no).append("&");
		signStr.append("order_time=").append(order_time).append("&");
		signStr.append("trade_no=").append(trade_no).append("&");
		signStr.append("trade_status=").append(trade_status).append("&");
		signStr.append("trade_time=").append(trade_time);
		String signInfo = signStr.toString();
		String sign = "";
		// b2c测试key
		// String merchant_private_key =
		// "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMM8dYl4MewcDPpsd4f4ICFMlyQJQqpCwTbAaTqkIP1DfcRDacTcCyjTRGEhhL/Jc4Dzl4woJnvt9brShIJKyjwrC8EUIDuPjrDG40neiMECdrFutZi2ECJR2Tod3v6dJj7HOkSiWu6l5mbP1GfWodfsAOuXORXT/1A/tDolj0yDAgMBAAECgYAnvj36/wHCm680pzHp9uOrg6jcyBnPCQnF+IMzUcf5ZmzKsCJu1ZBb/i3US/t3Ay5FGNFH0cCLx9QW9NXOpCTooZSTqs8Eo2oq/ydj6Cq9TFzegksQQMpOT92LxOvct8OvwQdJXRER1hu5tpFnm+a2WAmnm4Qv4s9UXm7npdPq+QJBAOccIIhHbwsiWs8ERrUU3lxjln6kD4RHyPfekfjg9eu8VbJMvovAJ+nQimU1Oc9WZtTBWdeAfTDuO9ZKsW6iqGUCQQDYQ0Rf0HFwKdGndkI0x+yRRUSEY2K0RqsENQlinXtkhAkwu1s2n2cX7qRl9OS9biF2Hlwrh1SXzHThFqyuB27HAkEAtFDIvStm4zwJt+1xMYW3vEPjPqnIhX/wuBxKrfDvTt8hybyBCs6BFbek0zRFB5U1JBg7fZ19j/jAbQDYEScN1QJAH8IBEHDzt5zshMMBBe819j9NyIMvw3l9M6j2L90geapKDzX0NxlBcmpBChfkX00E7OuYbWv6KDCGGWfTA9tjkQJBALwb+Noy+srFOrWdkev7PbMvkbN7GXVVuv4dq/JF+XY6ha7gdWh3wi7ckjYWrst8XZBG2oDZDXA2Ok/3H6S9TSI=";
		// scan测试key
		String merchant_private_key = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKPRv/4KBfknvr2NmliWwE0XZ/KxKyM5rT/iUfZgq9qR+/SMH2yfVIdZ7RpAj30v1waLgWkypmrYlT9f1dYN/nznEZ7cTAmHdYvdr+/iSuuKvufexvtPM4znEDy+I/BpunmuUbStxa6oGuVZVMqZTp+fkCRyuAti6YTxYPrL1MTvAgMBAAECgYAPOGi3KKU/bfLp16M0geUiBH+y68UPQ8eV/OgSlQ7Cgve+09oDlJYc8Tz3SHJMdkprIEEIzhzqJm9PcQ8EEU7Ik+yCO7dd3Df1jJw0Ibz7JfmbQL/xXTo+4mymJRQp1W+TShKydjyRFvdhPR7DVZpqqGqGnGbS+icPbqmq6zvTsQJBANlMmlWLq672ZZuGU0nvzOg+5jeg4IRTMRDWowOpuHFq+lDupI6uQOw/8wpCzFhzTHDofLcLBaQxyznaCaErAPcCQQDA/tH7L3ZKRdABFx+lTOn06ifQIfAHDm7lbBH7oZMYE+8hRBjI/HchCHhyeG7URSLhwB+ESXLY/jT/qzNjeVXJAkEAoNz4k8OmAQwfBBdYqS+AvZ1yyFOsYclEVXbPjvKhvDS5whgocgfFwB9HSJ1SsVgJirxRNyBNuNOz4svqvQkc2QJAIkcgFHUBlX+AyjDJNhrLEuzj1Vuxvg8aMzVWnFK+RXCE8Guf21K79eoDOpGZXapJ37632k8RTukXkLFflzNdwQJATUfrkFIYMgBGfD42MXI3KbvWfU2mnkuxaWBVIWhzWWUxD+DqevocKs37kf82g0L3FahBXbwI1+jdmwhcx4tCKw==";

		boolean result = false;
		try {
			sign = RSAWithSoftware.signByPrivateKey(signInfo, merchant_private_key);
			// b2c测试key
			// String dinpay_public_key =
			// "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDPHWJeDHsHAz6bHeH+CAhTJckCUKqQsE2wGk6pCD9Q33EQ2nE3Aso00RhIYS/yXOA85eMKCZ77fW60oSCSso8KwvBFCA7j46wxuNJ3ojBAnaxbrWYthAiUdk6Hd7+nSY+xzpEolrupeZmz9Rn1qHX7ADrlzkV0/9QP7Q6JY9MgwIDAQAB";
			// scan测试key
			// String dinpay_public_key =
			// "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCWOq5aHSTvdxGPDKZWSl6wrPpnMHW+8lOgVU71jB2vFGuA6dwa/RpJKnz9zmoGryZlgUmfHANnN0uztkgwb+5mpgmegBbNLuGqqHBpQHo2EsiAhgvgO3VRmWC8DARpzNxknsJTBhkUvZdy4GyrjnUrvsARg4VrFzKDWL0Yu3gunQIDAQAB";
			// result = RSAWithSoftware.validateSignByPublicKey("1234",
			// dinpay_public_key, sign);
			String dinpay_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDBKQNC3KBXaFZ3vRGlOLAT1fdFxPRVP3m9lV/iKP+7kM4LNPiQsmDBzT6nkWC1cqpIzKcL72tXqgfYrPxFtwzvYlKuJHgr34PGQIiOIb/RxJPrwd1OYILUsnuNvHiGEKvAiz9clohC1aZ0XhIagy2ySGrlgZCByBOI9VIxZw+jGQIDAQAB";
			result = RSAWithSoftware.validateSignByPublicKey(signInfo, dinpay_public_key, sign); // 验签
																									// signInfo速汇宝返回的签名参数排序，
																									// dinpay_public_key速汇宝公钥，
																									// dinpaySign速汇宝返回的签名
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(result);
		System.out.println("RSA-S商家发送的签名字符串：" + signInfo.length() + " -->" + signInfo);
		System.out.println("RSA-S商家发送的签名：" + sign.length() + " -->" + sign + "\n");
		// btc回调测试
		// System.out.println(BGHttpClientUtil.post("http://localhost:8080/XPJ/SHB/offlineNotify.do",
		// ss));
		// scan回调测试
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("interface_version", interface_version);
		reqMap.put("merchant_code", merchant_code);
		reqMap.put("notify_type", notify_type);
		reqMap.put("notify_id", notify_id);
		reqMap.put("sign_type", sign_type);
		reqMap.put("sign", sign);
		reqMap.put("order_time", order_time);
		reqMap.put("order_amount", order_amount);
		reqMap.put("trade_no", trade_no);
		reqMap.put("trade_time", trade_time);
		reqMap.put("trade_status", trade_status);
		reqMap.put("bank_seq_no", bank_seq_no);
		reqMap.put("trade_status", trade_status);
		reqMap.put("order_no", order_no);

		String resultstr = WFPayServiceImpl.TestdoPost("http://localhost:8080/XPJ/PlatformPay/scanPayNotify.do",
				reqMap, "utf-8");
		System.out.println(resultstr);

	}

	public String ScanPayH5(Map<String, String> scanMap) {
		String url = h5_apiUrl;
		String result = null;
		String service_type = scanMap.get("scanpayType").toString();
		String client_ip = scanMap.get("client_ip").toString();
		String order_no = scanMap.get("order_no").toString();
		String order_time = scanMap.get("order_time").toString();
		String order_amount = scanMap.get("order_amount").toString();
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("merchant_code", merchant_code);
		reqMap.put("service_type", service_type);
		reqMap.put("notify_url", scan_notify_url);
		reqMap.put("interface_version", h5_version);
		reqMap.put("client_ip", client_ip);
		reqMap.put("sign_type", sign_type);
		reqMap.put("order_no", order_no);
		reqMap.put("order_time", order_time);
		reqMap.put("order_amount", order_amount);
		reqMap.put("product_name", product_name);

		StringBuffer signSrc = new StringBuffer();
		signSrc.append("client_ip=").append(client_ip).append("&");
		signSrc.append("interface_version=").append(h5_version).append("&");
		signSrc.append("merchant_code=").append(merchant_code).append("&");
		signSrc.append("notify_url=").append(scan_notify_url).append("&");
		signSrc.append("order_amount=").append(order_amount).append("&");
		signSrc.append("order_no=").append(order_no).append("&");
		signSrc.append("order_time=").append(order_time).append("&");
		signSrc.append("product_name=").append(product_name).append("&");
		signSrc.append("service_type=").append(service_type);

		String signInfo = signSrc.toString();
		String sign = "";
		if ("RSA-S".equals(sign_type)) { // sign_type = "RSA-S"
			try {
				sign = RSAWithSoftware.signByPrivateKey(signInfo, merchant_private_key);
				reqMap.put("sign", sign);

				// return HttpUtil.HtmlFrom(url, reqMap);
				result = doPost(url, reqMap, "utf-8");
			} catch (Exception e) {
				logger.info("速汇宝扫码签名异常！");
				e.printStackTrace();
			}
		}
		logger.info("signInfo:" + signInfo.length() + " --> " + signInfo);
		logger.info("sign:" + sign.length() + " --> " + sign);
		logger.info("扫码响应:" + result);
		return result;
	}

	public static void main(String[] args) {
		// testWF();
		// testbankingNotify();
		// testScanPayNotify();
		// Map<String, String> shbConfigMap = new HashMap<String, String>();
		// /********** 速汇宝配置参数 ***********************/
		// shbConfigMap.put("b2c_interface_version", "V3.0");// 网银支付接口版本
		// // 接口版本固定值：V3.0(必须大写)
		// shbConfigMap.put("interface_version", "V3.3");
		//
		// shbConfigMap.put("h5_version", "V3.3");
		// shbConfigMap.put("sign_type", "RSA-S");// 签名方式
		// // 参数名称：服务类型固定值：direct_pay
		// shbConfigMap.put("service_type", "weixin_h5api");
		// // 服务器异步通知地址
		// shbConfigMap.put("notify_url",
		// "http://182.16.110.186:8080/XPJ/PlatformPay/bankingNotify.do");
		// // 参数编码字符集取值：UTF-8、GBK(必须大写)
		// shbConfigMap.put("input_charset", "UTF-8");
		// shbConfigMap.put("merchant_code", "1000491183");// 商家号
		// // 页面跳转同步通知地址
		// shbConfigMap.put("return_url", "http://182.16.110.186:8080/XPJ");
		// shbConfigMap.put("payurl",
		// "https://api.ddbill.com/gateway/api/h5apipay");//
		//
		// shbConfigMap.put("h5_apiUrl",
		// "https://api.ddbill.com/gateway/api/h5apipay");//
		// // 网银支付网关
		// shbConfigMap.put("pfxPass", "87654321");// 证书密钥
		// shbConfigMap.put("scanPay",
		// "https://api.ddbill.com/gateway/api/scanpay");// 扫描支付网关
		// shbConfigMap.put("merchant_private_key",
		// "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANZbJAetILObOYEALpYeU/OfuBivTNqTrFwe4/kHg1zUlAD9U3WgAY+gQ6LEhwxpqKD11mvsmwND+pJdBRtqbstazzk79nkrYfn9c4Sr0K0ABPxh2l6oBGqjoylya1KUlTvl3sQiBteh7r4wpY/ksloylm+jl2ZNlLPyQPUnfWIFAgMBAAECgYEAgxRAuCD6elpVk7kUw9/P3o6w+QdZq9/Z6otJRSN/BOlJiEFhI/Cwg6+xLHuZSdHiuGscr+qD8D9FnvMbCGTRUpIMQw1oRSSNL284B6upVWJDhLSRPLcJ8RxW6b3BqX08fOA1t950aqCWJO9po8PgGhVB2/CjyuCtvW6tavxF4QECQQD67mTqjvy1BSJEjlBUCZ6xg1qWgfI31S1frDDEcY8kVVyEtrviMNNDThKnlNq13MSKBz+6S5O6IXFpUv7ZRDdVAkEA2q+cOM1yG9g0hcUyEh4t/jfLfmXHX8N2W8DDc56z749MbbyWfkGiBARpyBWRWaJDyGwfdzx4Hu6BsgYL6Uof8QJAEtd9umMcNJd7GDy7aWQDh36+eZiuUGTwcEOksct5HRhPxyNVfkl+Q4qEOhMiTQ+Trm2W6m0sXYiPVIJH4paNtQJBAJZxxYmWXPAm90sn9GBefsmrXMViA50v/Rb2MAA0qBkgfjXoTagiQvF8j8FkhRAi/3q8V5FR+lldP8BXRI6DviECQB4xmusZJmivLf87TNgBWmq71S2meJqsPqEKbAtCQRfUX7om8UVRqfFSogH6kiRg1mpSBjJiDK1ScCy0Yc7vw4o=");//
		// // rsa-s私钥
		// shbConfigMap.put("dinpay_public_key",
		// "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCfP0QJwJ90pw4CxShfVfbkUN8JvZTSA5jEQ4byf62UviD15MTba0ja39VWTqK5nHWS15/5s78l9jHJA8qEEZ9Dg5epz9l4nLT//ZF7enDB2sDGSnOmasAP8XEYErvg52pdrgFJVkPbLzO5sCtd0SXbyqfrrMKng+ovXV+tTYyPTwIDAQAB");//
		// // rsa-s公钥
		// // /******添加参数*****/
		// // 扫描支付回调接口
		// shbConfigMap.put("scan_notify_url",
		// "http://15l0549c66.iask.in:45191/bankPay/offline_notify.jsp");
		// // 商品名称
		// shbConfigMap.put("product_name", "txcz");
		// // 订单是否允许重复提交//0是,1否,""空
		// shbConfigMap.put("redo_flag", "1");
		// // 商品编号 可选项目
		// shbConfigMap.put("product_code", "pay");
		// // 商品数量
		// shbConfigMap.put("product_num", "1");
		// // 商品描述
		// shbConfigMap.put("product_desc", "天下充值");
		// // 支付类型取值如下（必须小写，多选时请用逗号隔开）b2c,plateform,dcard,express,weixin,alipay
		// shbConfigMap.put("pay_type", "b2c");
		// // 业务扩展参数
		// shbConfigMap.put("extend_param", "");
		// // 回传参数
		// shbConfigMap.put("extra_return_param", "");
		// // 商品展示
		// shbConfigMap.put("show_url", "");
		// /****** 添加参数 *****/
		//// System.out.println(JSONObject.fromObject(shbConfigMap).toString());
		//
		// SHBPayGameServiceImpl shb = new SHBPayGameServiceImpl(shbConfigMap);
		//
		// HashMap<String, String> scanMap = new HashMap<>();
		// java.util.Date now = new java.util.Date();
		// java.text.SimpleDateFormat format = new
		// java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// String dateNow = format.format(now);
		// scanMap.put("scanpayType", "weixin_h5api");
		// scanMap.put("client_ip", "58.64.40.22");
		// scanMap.put("order_no", System.currentTimeMillis() + "");
		// scanMap.put("order_time", dateNow);
		// scanMap.put("order_amount", "10.0");
		// String html=shb.ScanPayH5( scanMap);
		//
		// System.out.println(URLDecoder.decode(html));

		/********** 速汇宝 ***********************/

		// /********** 智慧付配置参数 ***********************/
		// shbConfigMap.put("b2c_interface_version", "V3.0");// 网银支付接口版本
		// // 接口版本固定值：V3.0(必须大写)
		// shbConfigMap.put("interface_version", "V3.1");
		// shbConfigMap.put("sign_type", "RSA-S");// 签名方式
		// // 参数名称：服务类型固定值：direct_pay
		// shbConfigMap.put("service_type", "direct_pay");
		// // 服务器异步通知地址
		// shbConfigMap.put("notify_url",
		// "http://182.16.110.186:8080/XPJ/PlatformPay/bankingNotify.do");
		// // 参数编码字符集取值：UTF-8、GBK(必须大写)
		// shbConfigMap.put("input_charset", "UTF-8");
		// shbConfigMap.put("merchant_code", "800001001002");// 商家号
		// // 页面跳转同步通知地址
		// shbConfigMap.put("return_url",
		// "http://pay.faka99.com");
		// shbConfigMap.put("payurl", "https://pay.zhihpay.com/gateway");//
		// // 网银支付网关
		// shbConfigMap.put("pfxPass", "87654321");// 证书密钥
		// shbConfigMap.put("scanPay",
		// "https://api.zhihpay.com/gateway/api/scanpay");// 扫描支付网关
		// shbConfigMap.put("merchant_private_key",
		// "MIICeQIBADANBgkqhkiG9w0BAQEFAASCAmMwggJfAgEAAoGBAJwNWwyhampDty0L6juZkQvM7TM9LmwrZtajNOByK7FCcWXhBfONSKI3Wne18w/aQNVX4BumtwQbiZ4mtDflSiqVi6ljK/K00jBXEBpmhwk8+nhx5GBEObRnDMJ4Swv67/9AWjkYEik0g5b5P6PxwMJd8Jx7N60q7vj3G8r8SSHdAgMBAAECgYEAj759sBS/wUK+6G+hQ2Uhho0tsZUZven8wJl1i4mfVZiue/nbUY1C6qmyW7LuQhv4vIxFkzLuQopbKaLdPt+mgEHvZqbfHnEpeLHZpza8rMN8Bn70HY5wXM/O9FbFRONQb7A/ZFZlpj+PUPpwyg3ukBdoHgVv+YqoOed0xkN83sECQQDYzdxC06EY5e4S/hG8IxIprQ54ZR6hICSKYIGdnFdDATCdRfRg1npWluFBp0NTgE+3SBnLEuDF8G0esbgLQ7/nAkEAuEPFBomORCiaoIpuGbd5amKb3bOEHqoajDhY2VpAHWoljPa755F12bG+1eq6l+e1L+KRSUp1tE3YHFacTcJnmwJBALZzB5l1X20EkoWJKoOZP/+ykyrebhhKZHXPIUTx/L12kuLHrBCSbgHQl2AY0p/Cq1MiahJroJzy5+GczbUtHqECQQCOx4R94kyop8XZLCrQJokgVDRuf9GaaTiU+nxuPy57HGroZa+IagAlpUM4QuA/IL4W0rq9TcMS6VbR7pLC9iaNAkEA1UFXbRxcUO5N+ESixJ2P8yqr+Z7gkpPBKBT9W7QEZ9V2R8xEvUirBoVb4VzK1VpGQaRa90c64QHhH93B+bWWHw==");//
		// // rsa-s私钥
		// shbConfigMap.put("dinpay_public_key",
		// "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDPHWJeDHsHAz6bHeH+CAhTJckCUKqQsE2wGk6pCD9Q33EQ2nE3Aso00RhIYS/yXOA85eMKCZ77fW60oSCSso8KwvBFCA7j46wxuNJ3ojBAnaxbrWYthAiUdk6Hd7+nSY+xzpEolrupeZmz9Rn1qHX7ADrlzkV0/9QP7Q6JY9MgwIDAQAB");//
		// //rsa-s公钥
		// /******添加参数*****/
		// //扫描支付回调接口
		// shbConfigMap.put("scan_notify_url",
		// "http://182.16.110.186:8080/XPJ/PlatformPay/scanPayNotify.do");
		// // 商品名称
		// shbConfigMap.put("product_name", "天下充值");
		// // 订单是否允许重复提交//0是,1否,""空
		// shbConfigMap.put("redo_flag", "1");
		// // 商品编号 可选项目
		// shbConfigMap.put("product_code", "pay");
		// // 商品数量
		// shbConfigMap.put("product_num", "1");
		// // 商品描述
		// shbConfigMap.put("product_desc", "天下充值");
		// // 支付类型取值如下（必须小写，多选时请用逗号隔开）b2c,plateform,dcard,express,weixin,alipay
		// shbConfigMap.put("pay_type", "b2c");
		// // 业务扩展参数
		// shbConfigMap.put("extend_param", "");
		// // 回传参数
		// shbConfigMap.put("extra_return_param", "");
		// // 商品展示
		// shbConfigMap.put("show_url", "");
		// /******添加参数*****/
		// System.out.println(JSONObject.fromObject(shbConfigMap).toString());
		/********** 智慧付 **********************/
	}

	private static void testWF() {
		Map<String, Object> scanMap = new HashMap<String, Object>();
		String url = "https://api.5wpay.net/gateway/api/scanpay";// "https://api.5wpay.net/gateway/api/h5apipay";
		String result = null;
		String service_type = "jdpay_scan";// scanMap.get("scanpayType").toString();
		String client_ip = "110.164.197.124";// scanMap.get("client_ip").toString();
		java.util.Date now = new java.util.Date();
		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateNow = format.format(now);
		String order_no = System.currentTimeMillis() + "";// scanMap.get("order_no").toString();
		String order_time = dateNow; // scanMap.get("order_time").toString();
		String order_amount = "101";// scanMap.get("order_amount").toString();
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("merchant_code", "501502018011");
		reqMap.put("service_type", service_type);
		reqMap.put("notify_url", "https://api.wefupay.com/Notify_Url.jsp");
		reqMap.put("interface_version", "V3.1");
		reqMap.put("client_ip", client_ip);
		reqMap.put("sign_type", "RSA-S");
		reqMap.put("order_no", order_no);
		reqMap.put("order_time", order_time);
		reqMap.put("order_amount", order_amount);
		reqMap.put("product_name", "天下支付");

		StringBuffer signSrc = new StringBuffer();
		signSrc.append("client_ip=").append(client_ip).append("&");
		signSrc.append("interface_version=").append("V3.1").append("&");
		signSrc.append("merchant_code=").append("501502018011").append("&");
		signSrc.append("notify_url=").append("https://api.wefupay.com/Notify_Url.jsp").append("&");
		signSrc.append("order_amount=").append(order_amount).append("&");
		signSrc.append("order_no=").append(order_no).append("&");
		signSrc.append("order_time=").append(order_time).append("&");
		signSrc.append("product_name=").append("天下支付").append("&");
		signSrc.append("service_type=").append(service_type);

		String signInfo = signSrc.toString();
		String sign = "";
		if ("RSA-S".equals("RSA-S")) { // sign_type = "RSA-S"
			try {
				sign = RSAWithSoftware.signByPrivateKey(signInfo,
						"MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAMz1AY+6pngSiG6m9ni14VTQe+cYezNYFOFvko9Q6woN835ohFl8Fy0FFSnqn+dHSs9NhwA5lM1+op1jyF3wMmoPJZwxMYlET1rww2NsDLwNf/dHFqrYUPt6s3VuyqPqXzv5PoQOVg2+oPKQ3yg222SUUs3i/oFeRYS9tk0dxhSNAgMBAAECgYEAlfAHHGansd+c/5YVaWllVu7ONdmrB3zAictdpIrF560dHLTJXc1yBvVY7jk+kauOBunogTmeqlaiEBIXWuGFftIOdOilwPoJoQVy+aOIt+JyYerNbXysHivcj7yYRA2H21azFpYGjxLdXqdElWeRU9XXTcKLkdP55KZ0tijSvwECQQDs/roYKADDMagEzdLULeSU/ty3dKc9kvVgJbRDyONXCchhgJn0EXAggwaMrJuYfOpG8pGV2LjAJ6YcQYMk9BpZAkEA3WSSQ/P+1IcFf2UwGsiyP2xHC0IHT0DvSxCpU3WYj9pWgrDmgit4nhfZHGaCpL2W12BNP/va/PhceL13wX9dVQJBANq3EoMsl9eMQiV81e8fE2817fgY3icDMxSydzR/dt9ildz11B7c4QAAK9Ezkr0duxAS2KzTv10GulQlWaSG6rECQQCwAfKef25KfAWM4F88C+5ZqbVZZ+bzxQX4wYg8R7NAOi6ovAal2fWMih0bDQwz+F3hYDhJ8+qHTtQFUWvRs3X5AkARNadmB6D2cVTyI1r0kKL6CejOU9eBGcad8Bgc/jmlTxvQgArhhC+AyMSrDv55wkPJa6JjI1/cAVrwV2XselHn");
				reqMap.put("sign", sign);

				// return HttpUtil.HtmlFrom(url, reqMap);
				// result = doPost(url, reqMap, "utf-8");
			} catch (Exception e) {
				logger.info("WF扫码签名异常！");
				e.printStackTrace();
			}
		}
		logger.info("signInfo:" + signInfo.length() + " --> " + signInfo);
		logger.info("sign:" + sign.length() + " --> " + sign);
		logger.info("扫码响应:" + result);
		XMLSerializer xmlSerializer = new XMLSerializer();
		String result2 = xmlSerializer.read(result).toString();
		JSONObject responseJson = JSONObject.fromObject(JSONObject.fromObject(result2).get("response"));
		if (!("SUCCESS".equals(responseJson.getString("resp_code")))) {
			logger.info("SHB or ZF error:" + result);
		}
		String payURL = URLDecoder.decode(responseJson.get("payURL").toString());
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
		} else {
			// PC端扫码
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

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }
}
