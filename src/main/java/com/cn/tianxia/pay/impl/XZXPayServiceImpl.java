package com.cn.tianxia.pay.impl;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.mkt.util.HttpUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.xzx.util.HttpConnectionUtil;
import com.cn.tianxia.pay.xzx.util.ServletUtils;
import com.cn.tianxia.pay.xzx.util.SignTool;
import com.cn.tianxia.pay.xzx.util.XMLSecurityProcess;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;

public class XZXPayServiceImpl implements PayService {

	private final static Logger logger = LoggerFactory.getLogger(XZXPayServiceImpl.class);

	private static String RESP_MSG = "<result>1</result>";

	// 会员账号
	private static String memberCode;
	// 终端号
	private static String terminalId;
	// 加密密码
	private static String keyPass;
	// 页面通知地址
	// private static String RETURN_URL;
	// 服务器通知地址
	private static String NOTICE_URL;
	// cer证书
	private static String cerFilePath;
	// pfx证书
	private static String pfxFilePath;

	public static final String SIGNATUREPARAMS = "merchantAcctId,terminalId,payType,"
			+ "orderId,orderTime,orderAmount,dealId,dealTime," + "bankDealId,payResult,errCode,errMsg";
	// 支付服务器地址
	private static String serviceUrl;

	public XZXPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = new JSONObject().fromObject(pmap);
		if (null != pmap) {
			memberCode = jo.get("memberCode").toString();
			terminalId = jo.get("terminalId").toString();
			keyPass = jo.get("keyPass").toString();
			NOTICE_URL = jo.get("NOTICE_URL").toString();
			serviceUrl = jo.get("serviceUrl").toString();

			cerFilePath = jo.get("cerFilePath").toString();
			pfxFilePath = jo.get("pfxFilePath").toString();
		}
	}

	public String Pay(Map<String, String> scanMap) {
		// String memberCode = merchantAcctId;
		String merchantAcctId = memberCode + "01";
		String aliasName = "";
		String bgUrl = NOTICE_URL;
		String pageUrl = scanMap.get("pageUrl");
		// 商户订单号
		String orderId = scanMap.get("orderId");
		// 商户订单金额 整型数字
		// 以分为单位。比方10元，提交时金额应为1000,商户页面显示金额可以转换成以元为单位显示
		String orderAmount = scanMap.get("orderAmount");
		// 商户订单提交时间
		String orderTime = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
		// 1：pc端支付 2：移动端支付
		String deviceType = scanMap.get("deviceType");

		String productDesc = "productDesc";
		/*
		 * 当deviceType=1时 1：微信扫码 2：支付宝扫码 6：聚合网关，包含多种支付方式 1：支付宝扫码 2：微信扫码
		 ** 聚合网关可指定一个或多个支付方式，形如:6-12或6-2
		 **
		 ** 当deviceType=2时 1：微信H5 2：支付宝H5 5：聚合网关，包含多种支付方式 1：微信H5或公众号 2：支付宝H5
		 ** 聚合网关可指定一个或多个支付方式，形如:5-12或5-1
		 */
		String payType = scanMap.get("payType");

		// signMsg
		String signMsgVal = "";
		signMsgVal = appendParam(signMsgVal, "pageUrl", pageUrl);
		signMsgVal = appendParam(signMsgVal, "bgUrl", bgUrl);
		signMsgVal = appendParam(signMsgVal, "merchantAcctId", merchantAcctId);
		signMsgVal = appendParam(signMsgVal, "terminalId", terminalId);
		signMsgVal = appendParam(signMsgVal, "orderId", orderId);
		signMsgVal = appendParam(signMsgVal, "orderAmount", orderAmount);
		signMsgVal = appendParam(signMsgVal, "orderTime", orderTime);
		signMsgVal = appendParam(signMsgVal, "productDesc", productDesc);
		signMsgVal = appendParam(signMsgVal, "deviceType", deviceType);
		signMsgVal = appendParam(signMsgVal, "payType", payType);

		SignTool pki = new SignTool(memberCode, pfxFilePath, keyPass, aliasName);
		String signMsg = pki.signMsg(signMsgVal);

		TreeMap<String, String> sendParams = new TreeMap<String, String>();
		sendParams.put("pageUrl", pageUrl);
		sendParams.put("bgUrl", bgUrl);
		sendParams.put("merchantAcctId", merchantAcctId);
		sendParams.put("terminalId", terminalId);
		sendParams.put("orderId", orderId);
		sendParams.put("orderAmount", orderAmount);
		sendParams.put("orderTime", orderTime);
		sendParams.put("productDesc", productDesc);
		sendParams.put("deviceType", deviceType);
		sendParams.put("payType", payType);
		sendParams.put("signMsg", signMsg);

		String result = null;
		try {
			HttpConnectionUtil http = new HttpConnectionUtil(serviceUrl);
			http.init();
			byte[] bys = http.postParams(sendParams, true);
			result = new String(bys, "UTF-8");
			logger.info("新棕熊响应结果:" + result);
			if (StringUtils.isBlank(result)) {
				logger.info("新棕熊响应结果异常！");
				return "";
			}

			Map<String, String> respParams = parseParamString(result);
			String respCode = respParams.get("respCode");
			if ("000000".equals(respCode)) {
				logger.info("新棕熊获取二维码成功！" + result);
				// 置单成功
				// 1：url是二维码，商户需将url转换成二维码展示
				// 2：url跳转地址，商户需将用户的浏览器跳转至该url
				String qrUrl = "";
				if ("1".equals(respParams.get("type"))) {
					// 二维码
					qrUrl = "1," + URLDecoder.decode(respParams.get("url"), "utf-8");
					return qrUrl;
				} else {
					// URL 跳转连接
					qrUrl = "2," + URLDecoder.decode(respParams.get("url"), "utf-8");
					return qrUrl;
				}
			} else {
				// writeResp(result, httpResp);
				return "";
			}

		} catch (Exception e) {
			e.printStackTrace();
			// writeResp("通讯失败", httpResp);
			return "";
		}

	}

	/**
	 * 
	 * @Title: parseParamString @Description: 解析参数串 @param params @return Map
	 *         <String,String> @throws
	 */
	public static Map<String, String> parseParamString(String params) {
		Map<String, String> result = new HashMap<String, String>();
		if (StringUtils.contains(params, "?")) {
			// 包含? 可能是url,截取参数
			params = StringUtils.substringAfterLast(params, "?");
		}

		String[] arrays = StringUtils.split(params, "&");

		if (arrays != null) {
			String[] keyvalue = null;
			for (String param : arrays) {
				keyvalue = StringUtils.split(param, "=");
				if (keyvalue != null && keyvalue.length > 1) {
					if (StringUtils.isNotBlank(keyvalue[1])) {
						result.put(keyvalue[0], keyvalue[1]);
					}
				}
			}
		}
		return result;
	}

	/**
	 * 拼接参数
	 * 
	 * @param returns
	 * @param paramId
	 * @param paramValue
	 * @return
	 */
	public String appendParam(String returns, String paramId, String paramValue) {
		if (returns != "" && returns != null) {
			if (paramValue != "" && paramValue != null) {

				returns += "&" + paramId + "=" + paramValue;
			}

		} else {

			if (paramValue != "" && paramValue != null) {
				returns = paramId + "=" + paramValue;
			}
		}

		return returns;
	}

	/**
	 * 回调
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String callback(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Map<String, Object> map = ServletUtils.getRequestParameters(request);
		logger.info("=========================新棕熊验签=========================");
		// System.out.println("bg params:" + request.getRequestURI());
		String dataReceived = "";
		List<String> keyList = new ArrayList<String>();
		Set<String> set = map.keySet();
		for (String key : set) {
			if (SIGNATUREPARAMS.indexOf(key) >= 0 && map.get(key) != null
					&& !StringUtils.isEmpty((String) map.get(key))) {
				keyList.add(key);
			}
		}
		Collections.sort(keyList);
		for (String key : keyList) {
			dataReceived = appendParam(dataReceived, key, (String) map.get(key));
		}
		logger.info("bg dataReceived:" + dataReceived);
		String signMsg = (String) map.get("signMsg");
		logger.info("signMsg===" + signMsg);
		boolean veryfyResult = false;
		try {
			logger.info("==================================================");
			logger.info("bg data:" + URLDecoder.decode(dataReceived));
			logger.info("signMsg data:" + URLDecoder.decode(signMsg));
			logger.info("==================================================");
			// BG通知未转义，需要先转义
			veryfyResult = XMLSecurityProcess.veryfySignature(dataReceived, URLDecoder.decode(signMsg), cerFilePath);
		} catch (Exception e) {
			logger.info("回调解析参数异常:" + e.getMessage());
			e.printStackTrace();
		}
		logger.info("验签结果:" + veryfyResult);
		if (veryfyResult) {
			logger.info("验签成功");
			return "success";
		}
		logger.info("验签失败");
		return "";
	}

	public static void main(String[] args) {
		Map<String, String> params = new HashMap<>();
		String memberCode = "20180110001";
		String terminalId = "999999";
		String NOTICE_URL = "http://182.16.110.186:8080/XPJ/PlatformPay/XZXNotify.do";
		String keyPass = "872u97";
		String serviceUrl = "https://scc.gavspay.com/order/unifiedorder.htm";

		String cerFilePath = "C:\\cert\\VIP\\pgistar.rsa.cer";
		String pfxFilePath = "C:\\cert\\VIP\\20180110001.pfx";

		params.put("memberCode", memberCode);
		params.put("terminalId", terminalId);
		params.put("NOTICE_URL", NOTICE_URL);
		params.put("keyPass", keyPass);
		params.put("serviceUrl", serviceUrl);
		params.put("cerFilePath", cerFilePath);
		params.put("pfxFilePath", pfxFilePath);

		System.out.println(JSONObject.fromObject(params).toString());

		XZXPayServiceImpl xzx = new XZXPayServiceImpl(params);

		Map<String, String> scanMap = new HashMap<>();
		String orderId = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
		String orderAmount = 1 * 1000 + "";
		String orderTime = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
		String deviceType = "2";
		String payType = "5";
		scanMap.put("orderId", orderId);
		scanMap.put("orderAmount", orderAmount);
		scanMap.put("deviceType", deviceType);
		scanMap.put("payType", payType);
		System.out.println(xzx.Pay(scanMap));

		// testCallback();
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

		Map<String, String> scanMap = new HashMap<String, String>();
		scanMap.put("orderId", order_no);// 订单号
		// 以分为单位
		int int_amount = (int) (amount * 100);
		// TODO 1：pc端支付2：移动端支付
		String deviceType = "1";
		scanMap.put("deviceType", deviceType);
		scanMap.put("payType", pay_code);// 网银固定模式 跳转第三方收银台
		scanMap.put("orderAmount", String.valueOf(int_amount));// 订单明细金额
																// 以分为单位
		scanMap.put("pageUrl", refereUrl);

		String qrurl = Pay(scanMap);
		if ("".equals(qrurl)) {
			logger.info("新棕熊请求异常");
			return PayUtil.returnWYPayJson("error", "form", "", pay_url, "");
		}
		// 通过“,”区分支付连接类型
		// 1：url是二维码，商户需将url转换成二维码展示
		// 2：url跳转地址，商户需将用户的浏览器跳转至该url
		String[] urlType = qrurl.split(",");
		if ("2".equals(urlType[0])) {
			return PayUtil.returnWYPayJson("success", "link", urlType[1], pay_url, "");
		} else {
			logger.info("新棕熊不支持的处理类型");
			return PayUtil.returnWYPayJson("error", "form", "", pay_url, "");
		}
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

		Map<String, String> scanMap = new HashMap<String, String>();
		scanMap.put("orderId", order_no);// 订单号
		scanMap.put("payType", pay_code);
		// 以分为单位
		int int_amount = (int) (amount * 100);

		String deviceType = "";
		// pc 端
		if (StringUtils.isBlank(mobile)) {
			deviceType = "1";
		} else {
			// 手机端
			deviceType = "2";
			// 暂时使用聚合方式=5
			// scanMap.put("payType", "5");
		}
		scanMap.put("pageUrl", refereUrl);
		scanMap.put("deviceType", deviceType);
		scanMap.put("orderAmount", String.valueOf(int_amount));
		String qrurl = Pay(scanMap);
		if ("".equals(qrurl)) {
			return PayUtil.returnPayJson("error", "4", "支付接口请求失败!", userName, amount, order_no, "");
		}
		// 通过“,”区分支付连接类型
		// 1：url是二维码，商户需将url转换成二维码展示
		// 2：url跳转地址，商户需将用户的浏览器跳转至该url
		String[] urlType = qrurl.split(",");
		if ("1".equals(urlType[0])) {
			return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no, urlType[1]);
		} else if ("2".equals(urlType[0])) {
			return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no, urlType[1]);
		} else {
			return PayUtil.returnPayJson("error", "4", "支付接口请求失败!", userName, amount, order_no, "");
		}
	}

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }

}
