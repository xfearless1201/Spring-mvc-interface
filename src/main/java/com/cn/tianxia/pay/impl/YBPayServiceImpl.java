package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.json.JSONUtils;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.jfk.util.Base64Local;
import com.cn.tianxia.pay.jfk.util.Config;
import com.cn.tianxia.pay.jfk.util.GsonUtil;
import com.cn.tianxia.pay.jfk.util.HttpSendResult;
import com.cn.tianxia.pay.jfk.util.SecurityRSAPay;
import com.cn.tianxia.pay.jfk.util.SimpleHttpsClient;
import com.cn.tianxia.pay.mkt.util.HttpUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.FileLog;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;

/**
 * 
 * @Description:TODO
 * 
 * @author:zouwei
 * 
 * @time:2017年8月15日 上午9:08:32
 * 
 */
public class YBPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(YBPayServiceImpl.class);

	private static String merId;// 商户号
	private static String terId;// 终端号
	// 服务端公钥
	private static String serverPublicKey;
	// 商户私钥
	private static String privateKey;
	private static String version;// 接口版本
	private static String serverUrl;// 请求地址
	private static String asynURL;// 异步地址

	public YBPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = new JSONObject().fromObject(pmap);
		if (null != pmap) {
			merId = jo.get("merId").toString();
			terId = jo.get("terId").toString();
			serverPublicKey = jo.get("serverPublicKey").toString();
			privateKey = jo.get("privateKey").toString();
			version = jo.get("version").toString();
			serverUrl = jo.get("serverUrl").toString();
			asynURL = jo.get("asynURL").toString();
		}
	}

	/**
	 * 跳转到支付网关（PC和H5网关使用此接口）
	 * 
	 * @Description:
	 * @param bankMap
	 *            void
	 */
	public String bankPay(Map<String, String> bankMap) {
		try {
			String tradeMoney = bankMap.get("tradeMoney");
			String payType = bankMap.get("payType");
			String appSence = bankMap.get("appSence");
			String syncURL = bankMap.get("syncURL");
			// String asynURL = bankMap.get("asynURL");

			String payUrl = serverUrl + "/gateway/orderPay";
			Map<String, Object> map = new HashMap<String, Object>();

			map.put("businessOrdid", bankMap.get("businessOrdid"));
			map.put("orderName", "天下充值");
			map.put("merId", merId);
			map.put("terId", terId);
			map.put("tradeMoney", tradeMoney);
			map.put("selfParam", "");
			map.put("payType", payType); // 1000默认支持所有支付方式
			map.put("appSence", appSence);
			map.put("syncURL", syncURL);
			map.put("asynURL", asynURL);

			String json = GsonUtil.toJson(map);

			// 服务器公钥加密
			byte by[] = SecurityRSAPay.encryptByPublicKey(json.getBytes("utf-8"), Base64Local.decode(serverPublicKey));

			String baseStrDec = Base64Local.encodeToString(by, true);

			// 自己的私钥签名
			byte signBy[] = SecurityRSAPay.sign(by, Base64Local.decode(privateKey));
			String sign = Base64Local.encodeToString(signBy, true);

			Map<String, String> maps = new HashMap<String, String>();
			maps.put("encParam", baseStrDec);
			maps.put("sign", sign);
			maps.put("merId", merId);
			maps.put("version", version);
			String html = HttpUtil.HtmlFrom(payUrl, maps);
			logger.info("银邦网银支付表单:" + html);
			return html;
			// String htmlBuild = buildRequest(maps,"post","gateway",payUrl);
			// logger.info(htmlBuild);
			// out.print(htmlBuild);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 建立请求，以表单HTML形式构造（默认）
	 * 
	 * @param sParaTemp
	 *            请求参数数组
	 * @param strMethod
	 *            提交方式。两个值可选：post、get
	 * @param strButtonName
	 *            确认按钮显示文字
	 * @return 提交表单HTML文本
	 */
	public String buildRequest(Map<String, String> sParaTemp, String strMethod, String strButtonName,
			String serverUrl) {
		// 待请求参数数组

		List<String> keys = new ArrayList<String>(sParaTemp.keySet());

		StringBuffer sbHtml = new StringBuffer();

		sbHtml.append("<form id=\"paysubmit\" name=\"3weidupaysubmit\" action=\"" + serverUrl + "\" method=\""
				+ strMethod + "\">");

		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) sParaTemp.get(name);
			sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
		}

		// submit按钮控件请不要含有name属性
		sbHtml.append("<input type=\"submit\" value=\"" + strButtonName + "\" style=\"display:none;\"></form>");
		sbHtml.append("<script>document.forms['paysubmit'].submit();</script>");

		return sbHtml.toString();
	}

	/**
	 * 获取二维码地址
	 * 
	 * @Description:
	 * @param response
	 * @param request
	 *            void
	 */
	public JSONObject scanPay(Map<String, String> payParamsMap) {
		// 返回json
		JSONObject r_json = new JSONObject();
		r_json.put("status", "error");
		Map<String, String> mapss = new HashMap<String, String>();
		try {

			String tradeMoney = payParamsMap.get("tradeMoney");// 交易金额
			String payType = payParamsMap.get("payType");// 支付方式
			// String asynURL = payParamsMap.get("asynURL");// 异步地址

			String payUrl = serverUrl + "/gateway/orderPaySweepCode";

			String businessOrdid = payParamsMap.get("businessOrdid");
			String orderName = "天下充值";

			Map<String, Object> map = new HashMap<String, Object>();

			map.put("businessOrdid", businessOrdid);
			map.put("orderName", orderName);
			map.put("merId", merId);
			map.put("terId", terId);
			map.put("tradeMoney", tradeMoney);
			map.put("selfParam", "");// 商户自己传递
			map.put("payType", payType); // 支付方式编号 1005 微信扫码支付 1006 支付宝扫码支付
			map.put("asynURL", asynURL);

			if (payParamsMap.containsKey("mobile")) {
				map.put("appSence", "1002");
			}

			String json = GsonUtil.toJson(map);
			logger.info("encParam铭文参数" + json.toString());

			// 服务端公钥加密
			byte by[] = SecurityRSAPay.encryptByPublicKey(json.getBytes("utf-8"), Base64Local.decode(serverPublicKey));

			String baseStrDec = Base64Local.encodeToString(by, true);

			// 自己的私钥签名
			byte signBy[] = SecurityRSAPay.sign(by, Base64Local.decode(privateKey));
			String sign = Base64Local.encodeToString(signBy, true);

			// 组装请求参数
			Map<String, String> synParam = new HashMap<String, String>();
			synParam.put("encParam", baseStrDec);
			synParam.put("merId", merId);
			synParam.put("sign", sign);
			synParam.put("version", version);
			logger.info("银邦扫码" + payType + "请求参数:" + synParam.toString());
			SimpleHttpsClient httpClient = new SimpleHttpsClient();
			HttpSendResult result = null;
			String respTxt = "";
			try {
				result = httpClient.postRequest(payUrl, synParam, 30 * 000);
				respTxt = result.getResponseBody();

				logger.info("银邦扫码" + payType + "服务器返回:" + respTxt);
				if (result.getStatus() != 200) {
					// 请求服务器失败
					logger.info("银邦扫码http请求失败:" + result.getStatus());
					WriteInFile(payType, result.getStatus() + "", respTxt);
					return r_json;
				}

				if (respTxt.contains("</html>")) {
					// 服务器返回异常
					logger.info("银邦扫码服务器返回异常:" + result.getStatus());
					WriteInFile(payType, result.getStatus() + "", respTxt);
					return r_json;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("银邦扫码异常:" + result.getStatus());
				WriteInFile(payType, e.getMessage() + "", "请求状态:" + result.getStatus() + "返回数据:" + respTxt);
				return r_json;
			}

			// 服务器没有返回数据
			if (respTxt == null || "".equals(respTxt)) {
				logger.info("银邦扫码服务器返回数据为空:" + result.getStatus());
				WriteInFile(payType, result.getStatus() + "", respTxt);
				return r_json;
			}
			Map<String, String> maps = GsonUtil.fromJson(respTxt, Map.class);
			String respCode = maps.get("respCode"); // 返回码

			// 没有返回respCode 说明查询到了订单信息
			if (respCode != null && !"".equals(respCode)) {
				if (!"1000".equals(respCode)) {// 返回1000表示成功。当dq_code为1000时，订单状态才有效。
					logger.info("银邦扫码订单无效respCode：" + respCode);
					WriteInFile(payType, result.getStatus() + "", respTxt);
					return r_json;
				}
			}

			String encParam = maps.get("encParam");
			String signs = maps.get("sign");
			boolean flag = SecurityRSAPay.verify(Base64Local.decode(encParam), Base64Local.decode(serverPublicKey),
					Base64Local.decode(signs));

			// 验签失败
			if (!flag) {
				// 商户出错处理
				logger.info("验签失败");
				WriteInFile(payType, result.getStatus() + "", respTxt);
				return r_json;
			}

			String date = new String(
					SecurityRSAPay.decryptByPrivateKey(Base64Local.decode(encParam), Base64Local.decode(privateKey)),
					"utf-8");

			try {
				mapss = GsonUtil.fromJson(date, Map.class);
			} catch (Exception e) {
				// TODO: handle exception
				logger.info("转换json格式错误");
				// 商户错误处理
				WriteInFile(payType, result.getStatus() + "", respTxt);
				return r_json;
			}

			logger.info("服务器返回json数据:" + date);
			r_json.put("msg", date.toString());

			String code = mapss.get("respCode"); // 返回码
			String respDesc = mapss.get("respDesc"); // 返回描述信息
			String payOrderId = mapss.get("payOrderId");// 支付订单号
			String orderId = mapss.get("orderId");// 业务订单号
			String money = mapss.get("money");// 金额
			String code_img_url = mapss.get("code_img_url");// 订单状态
			String code_url = mapss.get("code_url");// 付款时间

			if (!"1000".equals(code)) {
				logger.info("返回code错误" + code);
				WriteInFile(payType, result.getStatus() + "", respTxt);
				return r_json;
			}
			String jsonOut = GsonUtil.toJson(mapss);
			logger.info(jsonOut);
			r_json.put("status", "success");
			r_json.put("code_img_url", code_img_url);
			r_json.put("code_url", code_url);
			return r_json;

		} catch (Exception e) {
			Map<String, String> map = new HashMap<>();
			FileLog f = new FileLog();
			map.put("scanType", "scanPay");
			map.put("status", "error");
			map.put("response", e.toString());
			f.setLog("JFK", map);
			return r_json;
		}
	}

	public void WriteInFile(String scanType, String status, String response) {
		Map<String, String> map = new HashMap<>();
		FileLog f = new FileLog();
		map.put("scanType", scanType);
		map.put("status", status);
		map.put("response", response);
		f.setLog("JFK" + scanType, map);
	}

	/**
	 * 异步通知解析(同步类似)
	 * 
	 * @param merId
	 * @param ordId
	 * @param busId
	 * @param amount
	 * @param payState
	 * @param payTime
	 * @param selfParam
	 * @param synTyep
	 * @return
	 * @throws Exception
	 */
	public String JFKNotify(HttpServletResponse response, HttpServletRequest request) throws Exception {
		// out= response.getWriter();
		String encParam = request.getParameter("encParam");
		String merId = request.getParameter("merId");
		String version = request.getParameter("version");
		String sign = request.getParameter("sign");
		String rt_success = "error";
		// 验签
		boolean flag = SecurityRSAPay.verify(Base64Local.decode(encParam), Base64Local.decode(Config.serverPublicKey),
				Base64Local.decode(sign));
		if (!flag) {
			// 验签失败
			return rt_success;
		}
		String respData = new String(SecurityRSAPay.decryptByPrivateKey(Base64Local.decode(encParam),
				Base64Local.decode(Config.privateKey)));

		Map<String, String> map = new HashMap<String, String>();
		map = GsonUtil.fromJson(respData, Map.class);

		String respCode = map.get("respCode"); // 返回码返回1000表示成功。当dq_code为1000时，订单状态才有效。
		String orderId = map.get("orderId"); // 商户订单号 字符串 商户订单号
		String payOrderId = map.get("payOrderId"); // 支付订单号 字符串 支付订单号
		String order_state = map.get("order_state"); // 订单状态
		String money = map.get("money"); // 交易金额
		String payReturnTime = map.get("payReturnTime"); // 付款时间
		String selfParam = map.get("selfParam"); // 自定义参数
		String payType = map.get("payType");// 支付方式
		String payTypeDesc = map.get("payTypeDesc"); // 支付方式描述

		rt_success = "success";
		/************** 商户业务开始 **************/
		// 注意避免重复通知导致业务出现异常

		/************** 商户业务结束 **************/

		// out.print("SUCCESS"); //商户记得一定要回写
		return rt_success;
	}

	public static void testCallbeak() {
		Map<String, String> map = new HashMap<String, String>();
		String respCode = "1000"; // 返回码返回1000表示成功。当dq_code为1000时，订单状态才有效。
		String orderId = "YB201802242022362022364416"; // 商户订单号 字符串 商户订单号
		String payOrderId = "1234123"; // 支付订单号 字符串 支付订单号
		String order_state = "1003"; // 订单状态 1001
										// 初始状态，1002交易受理成功,1003支付成功，1004已经申请退款
		String money = "10"; // 交易金额
		String payReturnTime = "2017-08-15"; // 付款时间
		String selfParam = ""; // 自定义参数
		String payType = "1006";// 支付方式
		String payTypeDesc = ""; // 支付方式描述
		map.put("respCode", respCode); // 返回码返回1000表示成功。当dq_code为1000时，订单状态才有效。
		map.put("orderId", orderId); // 商户订单号 字符串 商户订单号
		map.put("payOrderId", payOrderId); // 支付订单号 字符串 支付订单号
		map.put("order_state", order_state); // 订单状态
		map.put("money", money); // 交易金额
		map.put("payReturnTime", payReturnTime); // 付款时间
		map.put("selfParam", selfParam); // 自定义参数
		map.put("payType", payType);// 支付方式
		map.put("payTypeDesc", payTypeDesc); // 支付方式描述
		String json = GsonUtil.toJson(map);
		logger.info("本地业务参数json:" + json);

		String s_public = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC08h2EC7aGli6QJTLuAx5NidOR7G1kW88GQ/cvXqksSkzY+Di/HHKqT98IGtvA0/HwW9lDBq4NGqq8DldOXpMf2eqzq8dJ9RmPE0KC17Q2jhb3eILvHm7M0XuJfiOrnY95c4Czs3u3bCAmv97Lk1kb3ySTEHVQNbgN3GLaOziwyQIDAQAB";
		String p_private = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALTyHYQLtoaWLpAlMu4DHk2J05HsbWRbzwZD9y9eqSxKTNj4OL8ccqpP3wga28DT8fBb2UMGrg0aqrwOV05ekx/Z6rOrx0n1GY8TQoLXtDaOFvd4gu8ebszRe4l+I6udj3lzgLOze7dsICa/3suTWRvfJJMQdVA1uA3cYto7OLDJAgMBAAECgYBu6IBi8XL9Z4w+2i4vaUXnrPU7Wjlq7EMmyf320QTMmvYjf3H44gz+i1pfc2cC+GYxN3sh9DQ24AOKpReqd5mhQdYBIUtNhX/MruSbnwpRrFyewf2hu/Q1J+7p6KIhlpjyqgXqUOg68PONTJy9t1ateSdU2Ll+5NkTyhw36+d3SQJBAOY2BmFPESj2tbxpdtSTd3mTox7aN/X+BAV+yo2g4718blzyredL2mannDf7KyWtsQjklhNdNDerLPsgtrM3nl8CQQDJN0O4vmhvq2GTNDBnX87n9ubLwdocE9WulQv6agOixrnaTTH4K1owzbUJgFJimoynFdM1HCOzkFj9MnlV3bHXAkEAzcX0pf5axFbh02whv5mg7Zc981HzO3q2dYMD4tyxNKqIq6RwNZLjwWaIiLGFu8Qpvl00mFJzXl8vcbmoFUHd2QJAJ6u+rVjVDFc0uKoMdQUdrPwvyrJghzKd6Xq3TtrB9sDJebHIjU42zPJxo/rpw0kHPBYAezMeEtP9x4XBCF3aJwJBANfjkUbsBByZdARGctkQjDLGESixxOBGlvqLls/GXpJtdOmzICiFyHfVgSx5L8OL2THyHOf8esgpS/9szLFrYcU=";
		try {
			// 服务器公钥加密
			byte by[] = SecurityRSAPay.encryptByPublicKey(json.getBytes("utf-8"), Base64Local.decode(s_public));

			String baseStrDec = Base64Local.encodeToString(by, true);
			logger.info("本地baseStrDec:" + baseStrDec);
			// 自己的私钥签名
			byte signBy[] = SecurityRSAPay.sign(by, Base64Local.decode(p_private));
			String sign = Base64Local.encodeToString(signBy, true);
			logger.info("本地sign:" + sign);

			Map<String, String> maps = new HashMap<String, String>();
			maps.put("encParam", baseStrDec);
			maps.put("sign", sign);
			maps.put("merId", "201709141416232");
			maps.put("version", "1.0.9");

			boolean flag = SecurityRSAPay.verify(Base64Local.decode(baseStrDec), Base64Local.decode(s_public),
					Base64Local.decode(sign));
			if (!flag) {
				logger.info("失败");
			}

			String respData = new String(
					SecurityRSAPay.decryptByPrivateKey(Base64Local.decode(baseStrDec), Base64Local.decode(p_private)));
			Map<String, String> map1 = new HashMap<String, String>();
			map1 = GsonUtil.fromJson(respData, Map.class);
			logger.info("解密map:" + map1.toString());

			String ss = HttpUtil.RequestForm("http://192.168.0.61:324/api/PlatformPay/YBNotify.do", maps);

			// System.out.println(ss);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void aa() {
		String publickey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCabJDz/66tGW6J0SBHI3zTqz+vB7lkBwEcSnnaNJ6mAZ64Garc4Ax9lcFV9aUI3/v/w7LRnhPRnMCHc9HeBFS66jPixlvk3cB/TYsVoxuQInTE/VmQDv+9cRlKYpemULGr6VoeOzAoEHz68g/YUZCjFBxbhTyOKutBoCorsAmQeQIDAQAB";
		String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAID5MehVCHEcYaOSEEo8LsKsHE/uAbmjjQDk79GhpLyS6SriKaXHgkBm+8eDHiZsJtUjz9B166cjamo6U6Hf6VIky+BrmhzN/pIhgLsi8AHns3KPboK6QPtFWVq7RSQgNPVQAn4XoUXn53Bt2l9R7q9Q+HAKEmc6SECOYByrmznlAgMBAAECgYBtU8/sg/hN+aMCxxQr+Wwh9UdisfygYnVqZqar6vv8JgSlJ2Xqc36EHUgczZbHNzKLxnmo2ezSl4DjX2H5fBcJJPjTK4PruhDL9JqZ8613OuEIV+YpQyXkqw/f7u9rpby1/4LCV09AS5rgLu0EFeATwA2W+daTHWTDfyBEWk/oWQJBANRrs0ecblB2DXi3PpuQPvBQZgftRTsxT0nFG9wVNU9qWDEcG/qMVRlbrDKtBwU1Na3pgnUFv3mDlvHpB7LitgcCQQCbbttv2E3P11z6FMMkeiJj0KJC9hHCWALVFIy8e1Ztu8SAMUZ26h+tQF6KNdG4RK3iINI/2z4WuDdsGW2/xbWzAkBQD8vBmN8nKmeHcSGCxopCTzs1j6NBXSNyPX1zpPaz2PQmhl6xP0UvypKLgxWbS+PQiatm6eSyKvuwb9E8BiDvAkEAiOo4z6eouzlJBF0/nCUdn/EKzZM2B4xBSnMkmW7HrjelGwrMuDj5mEJIRNStnPSaesNQkMFOSvMQlt87Zp8AswJASJmtjBNAqtXg9XCkQAgb0KqmxTZvUO8A2avtQekZbnlo8iK/QAVCbR/eV/wpLWVQD31INK3YAJwzKJmUwekScA==";
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("payTypeDesc", "1"); // 支付方式描述
			String json = GsonUtil.toJson(map);
			// 服务端公钥加密
			byte by[] = SecurityRSAPay.encryptByPublicKey(json.getBytes("utf-8"), Base64Local.decode(publickey));

			String baseStrDec = Base64Local.encodeToString(by, true);

			// 自己的私钥签名
			byte signBy[] = SecurityRSAPay.sign(by, Base64Local.decode(privateKey));
			String sign = Base64Local.encodeToString(signBy, true);

			logger.info("本地baseStrDec:" + baseStrDec);
			// 自己的私钥签名
			logger.info("本地sign:" + sign);

			Map<String, String> maps = new HashMap<String, String>();
			maps.put("encParam", baseStrDec);
			maps.put("sign", sign);
			maps.put("merId", "201609212111571");
			maps.put("version", "1.0.9");

			boolean flag = SecurityRSAPay.verify(Base64Local.decode(baseStrDec), Base64Local.decode(publickey),
					Base64Local.decode(sign));

			logger.info(flag + "");
		} catch (Exception e) {

		}

	}

	public static void main(String[] args) {
		// JFKPayServiceImpl.aa();
		YBPayServiceImpl.testCallbeak();
		// String merId = "201712060617075";// 商户号
		// String terId = "201712061721558";// 终端号
		// // 服务端公钥
		// String serverPublicKey =
		// "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCabJDz/66tGW6J0SBHI3zTqz+vB7lkBwEcSnnaNJ6mAZ64Garc4Ax9lcFV9aUI3/v/w7LRnhPRnMCHc9HeBFS66jPixlvk3cB/TYsVoxuQInTE/VmQDv+9cRlKYpemULGr6VoeOzAoEHz68g/YUZCjFBxbhTyOKutBoCorsAmQeQIDAQAB";
		// // 商户私钥
		// String privateKey =
		// "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAOOCJfrdC296/Xz91tMr8QoWEEwUAYkHZzwwQd15JwaBSk847bZ+HMEkWB+l1AO2vhdNorRO91JCPdeFAVO3/KldqpDrxYNZ2K3rsr7FDcGhdd+ZF7N+EArQKOXcmQdCdKB1JOkvnpmRH8LWVwICKeuwIeFKaIcZD+PhTHMe6W/DAgMBAAECgYA01P1i40ALlGEXTI89nL+t2u/P6vlsS9bSaFZzSvYgEZgjC5JQRbsBsyHvv3+bmS1Nlg7JlmjiplVcHhvvyBMZBVNsPEkn525wJwIB9o07BVINyyPyfhDxlBOo6vAjX6WqZCt5uP1fPAlrida2vBKwRUSH0o8vWrFEjpBBLmBCgQJBAP6IuvbjwgQFoGjPChtQahONinWf5TX8tcOvAYJkdMDB7gTRQiwd2O9FNGBv7uAPslKf/xtGqNi2qg5PBvBe2cECQQDk0ZKtWh+ocZ2CUJsaOArKXHrup3dQ+7z8X9YT+fZggmcvHRp7iVxwY4VW1HG0dJ1iVp5CUzSyayt2OY/uzIKDAkEAtdI4NCrIKhVW2+ehoZdU3vjJwnJgyqrlsI4v9tQJrrQZcamFqnv7vhfibU0oBzhPOsR9+B8GeCAr5CLBoy0uQQJAVF7mK59QjwadomBgYHGLoQQqm/cJ6sV38MLRp/oCG6HZtCiSDD0g5Zv8nYCmEHjzU0BHCIOvqZ4Un3ooFVA5SwJAe0scZZ6vm39bmS8/9IlZw9ebnUxgAxXiemKLVqprcevH5XI8q9Xy4vKOkRWDfXcEMnQudMOyiUgE6i/Hxnu22Q==";
		// String version = "1.0.9";
		// String serverUrl = "http://www.goldenpay88.com";
		// String asynURL = "http://www.goldenpay88.com";
		// String order_no="VIP"+System.currentTimeMillis()+"";
		// Map<String, String> map = new HashMap<String, String>();
		// map.put("merId", merId);
		// map.put("terId", terId);
		// map.put("serverPublicKey", serverPublicKey);
		// map.put("privateKey", privateKey);
		// map.put("version", version);
		// map.put("serverUrl", serverUrl);
		// map.put("asynURL", asynURL);
		// JSONObject json = JSONObject.fromObject(map);
		//// logger.info("银邦json配置:" + json.toString());
		// JFKPayServiceImpl jfk = new JFKPayServiceImpl(map);
		// Map<String, String> scanMap = new HashMap<String, String>();
		// scanMap.put("tradeMoney", "200");// 交易金额
		// scanMap.put("payType", "1016");// 支付方式
		// scanMap.put("asynURL", "http://www.baidu.com");// 同步地址
		// scanMap.put("businessOrdid", order_no);
		//// scanMap.put("mobile", "mobile");
		// logger.info(scanMap.toString());
		// logger.info("返回是否成功:" + JSONObject.fromObject(jfk.scanPay(scanMap)));

		// String
		// s="https://pay.swiftpass.cn/pay/qrcode?uuid=weixin%3A%2F%2Fwxpay%2Fbizpayurl%3Fpr%3DMWdT5kE";
		//
		// System.out.println(URLDecoder.decode(s));

		// Map<String, String> bankMap=new HashMap<String,String>();
		// bankMap.put("tradeMoney", "200");// 交易金额
		// bankMap.put("payType", "1003");// 支付方式
		// bankMap.put("asynURL", "http://www.baidu.com");// 同步地址
		// jbf.bankPay(bankMap);
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

		Map<String, String> bankMap = new HashMap<String, String>();
		int int_amount = (int) (amount * 100);
		bankMap.put("tradeMoney", String.valueOf(int_amount));// 交易金额 单位 分
		bankMap.put("payType", pay_code);// 支付方式 网银支付
		bankMap.put("businessOrdid", order_no);
		String appSence = mobile;// 应用场景 默认pc 1001
									// ,1002 H5
		bankMap.put("appSence", "1001");// 应用场景 pc
		if (!StringUtils.isNullOrEmpty(appSence)) {
			bankMap.put("appSence", "1002");// 应用场景 h5
		}
		bankMap.put("syncURL", refereUrl);// 同步地址
		String html = bankPay(bankMap);

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

		JSONObject jfkjson = null;
		Map<String, String> scanMap = new HashMap<String, String>();
		int int_amount = (int) (amount * 100);
		scanMap.put("tradeMoney", String.valueOf(int_amount));// 交易金额 单位 分
		scanMap.put("asynURL", refereUrl);// 同步地址
		scanMap.put("businessOrdid", order_no);
		scanMap.put("payType", pay_code);
		// 手机端
		if (!StringUtils.isNullOrEmpty(mobile)) {
			scanMap.put("mobile", mobile);
			jfkjson = scanPay(scanMap);
			if (jfkjson.containsKey("status") && "success".equals(jfkjson.get("status"))) {
				return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
						jfkjson.getString("code_url"));
			}
		} else {// pc端
			jfkjson = scanPay(scanMap);
			if (jfkjson.containsKey("status") && "success".equals(jfkjson.get("status"))) {
				return PayUtil.returnPayJson("success", "3", "支付接口请求成功!", userName, amount, order_no,
						jfkjson.getString("code_img_url"));
			}
		}

		return PayUtil.returnPayJson("error", "4", jfkjson.getString("msg"), userName, amount, order_no,
				"");
	}

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }
}
