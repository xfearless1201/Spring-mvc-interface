package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.json.JSONUtils;
import com.cn.tianxia.pay.mkt.util.HttpUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.xq.util.CharsetTypeEnum;
import com.cn.tianxia.pay.xq.util.ClientSignature;
import com.cn.tianxia.pay.xq.util.StringUtils;
import com.cn.tianxia.pay.xq.util.XQHttpUtil;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;

/**
 * 
 * @Description:TODO
 * 
 * @author:zouwei
 * 
 * @time:2017年8月29日 下午3:50:20
 * 
 */
public class XQPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(XQPayServiceImpl.class);

	private String version;// 版本
	private String failureTime;// 订单失效时间
	private String type;// 交易类型
	private String buyerMarked;// 付款方聚合账户号
	private String currencyCode;// 交易币种
	private String borrowingMarked;// 资金来源借贷标识
	private String couponFlag;// 是否直连
	private String charset;// 编码方式
	private String signType;// 签名类型（系统目前只支持MD5）
	private String key;// md5key
	private String url;// 支付地址
	private String platformID;// 平台id

	private String goodsCount;// 商品数量
	private static String displayName;// 下单商户显示名
	private static String goodsName;// 商品名称
	private String directFlag;// 是否直连
	private String partnerID;// 商户ID
	private String noticeUrl;// 回调通知地址
	private String remark;// 备注remark

	public XQPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = new JSONObject().fromObject(pmap);
		if (null != pmap) {
			version = jo.get("version").toString();// 版本
			failureTime = jo.get("failureTime").toString();// 订单失效时间
			type = jo.get("type").toString();// 交易类型
			buyerMarked = jo.get("buyerMarked").toString();// 付款方聚合账户号
			currencyCode = jo.get("currencyCode").toString();// 交易币种
			borrowingMarked = jo.get("borrowingMarked").toString();// 资金来源借贷标识
			couponFlag = jo.get("couponFlag").toString();// 是否直连
			charset = jo.get("charset").toString();// 编码方式
			signType = jo.get("signType").toString();// 签名类型（系统目前只支持MD5）
			key = jo.get("key").toString();// md5key
			url = jo.get("url").toString();// 支付地址
			platformID = jo.getString("platformID").toString();
			goodsCount = jo.getString("goodsCount").toString();// 商品数量
			displayName = jo.getString("displayName").toString();// 下单商户显示名
			goodsName = jo.getString("goodsName").toString();// 商品名称
			directFlag = jo.getString("directFlag").toString();// 是否直连
			partnerID = jo.getString("partnerID").toString();// 商户ID
			noticeUrl = jo.getString("noticeUrl").toString();// 回调通知地址
			remark = jo.getString("remark").toString();// 备注remark
		}
	}

	public String bankPay(Map<String, String> req) {
		String serialID = StringUtils.produceOrderNo("sn");// 序列号
		String submitTime = StringUtils.getDateymdhms(new Date());
		String customerIP = req.get("customerIP");
		String orderAmount = req.get("orderAmount");
		// 获取订单详情
		String orderDetails = getOrderDetails(req, orderAmount, goodsCount);
		String totalAmount = String.valueOf(Integer.parseInt(orderAmount) * Integer.parseInt(goodsCount));
		String payType = req.get("payType");
		String orgCode = req.get("orgCode");
		String returnUrl = req.get("returnUrl");// 支付完成地址
		// 组装签名字符串
		String signStr = getSignstr(version, serialID, submitTime, failureTime, customerIP, orderDetails, totalAmount,
				type, buyerMarked, payType, orgCode, currencyCode, directFlag, borrowingMarked, couponFlag, platformID,
				returnUrl, noticeUrl, partnerID, remark, charset, signType);
		// MD5签名
		String signMsg = null;
		try {
			signMsg = ClientSignature.genSignByMD5(signStr, CharsetTypeEnum.UTF8, key);
		} catch (Exception e) {
			logger.info("小强支付验签异常！");
			e.printStackTrace();
		}
		// 构建请求参数
		Map<String, String> resquestMap = new LinkedHashMap<>();
		resquestMap.put("version", version);
		resquestMap.put("serialID", serialID);
		resquestMap.put("submitTime", submitTime);
		resquestMap.put("failureTime", failureTime);
		resquestMap.put("customerIP", customerIP);
		resquestMap.put("orderDetails", orderDetails);
		resquestMap.put("totalAmount", totalAmount);
		resquestMap.put("type", type);
		resquestMap.put("buyerMarked", buyerMarked);
		resquestMap.put("payType", payType);
		resquestMap.put("orgCode", orgCode);
		resquestMap.put("currencyCode", currencyCode);
		resquestMap.put("directFlag", directFlag);
		resquestMap.put("borrowingMarked", borrowingMarked);
		resquestMap.put("couponFlag", couponFlag);
		resquestMap.put("platformID", platformID);
		resquestMap.put("returnUrl", returnUrl);
		resquestMap.put("noticeUrl", noticeUrl);
		resquestMap.put("partnerID", partnerID);
		resquestMap.put("remark", remark);
		resquestMap.put("charset", charset);
		resquestMap.put("signType", signType);
		resquestMap.put("signMsg", signMsg);
		// form表单请求到接口地址
		List<String> keys = new ArrayList<String>(resquestMap.keySet());
		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) resquestMap.get(name);
			if (com.cn.tianxia.pay.gst.util.StringUtils.isNullOrEmpty(value)) {
				logger.info("删除:" + name);
				resquestMap.remove(name);
			}
		}

		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ url + "\">";
		for (String key : resquestMap.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + resquestMap.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";

		String html = FormString;
		// String html = HttpUtil.HtmlFrom(url, resquestMap);
		logger.info("小强支付网银支付表单:" + html);
		return html;
	}
	
	/**
	 * 表单扫码接口
	 * @param req
	 * @return
	 */
	public String scanPay(Map<String, String> req) {
		String serialID = StringUtils.produceOrderNo("sn");// 序列号
		String submitTime = StringUtils.getDateymdhms(new Date());
		String customerIP = req.get("customerIP");
		String orderAmount = req.get("orderAmount");
		// 获取订单详情
		String orderDetails = getOrderDetails(req, orderAmount, goodsCount);
		String totalAmount = String.valueOf(Integer.parseInt(orderAmount) * Integer.parseInt(goodsCount));
		String payType = req.get("payType");
		// 银联扫码borrowingMarked=1
		if ("B2C_SCAN".equals(payType)) {
			borrowingMarked = "1";
		} else {
			borrowingMarked = "0";
		}

		String orgCode = req.get("orgCode");
		String returnUrl = req.get("returnUrl");// 支付完成地址
		// 组装签名字符串
		String signStr = getSignstr(version, serialID, submitTime, failureTime, customerIP, orderDetails, totalAmount,
				type, buyerMarked, payType, orgCode, currencyCode, directFlag, borrowingMarked, couponFlag, platformID,
				returnUrl, noticeUrl, partnerID, remark, charset, signType);
		// MD5签名
		String signMsg = null;
		try {
			signMsg = ClientSignature.genSignByMD5(signStr, CharsetTypeEnum.UTF8, key);
		} catch (Exception e) {
			logger.info("小强支付验签异常！");
			e.printStackTrace();
		}
		// 构建请求参数
		Map<String, String> resquestMap = new LinkedHashMap<>();
		resquestMap.put("version", version);
		resquestMap.put("serialID", serialID);
		resquestMap.put("submitTime", submitTime);
		resquestMap.put("failureTime", failureTime);
		resquestMap.put("customerIP", customerIP);
		resquestMap.put("orderDetails", orderDetails);
		resquestMap.put("totalAmount", totalAmount);
		resquestMap.put("type", type);
		resquestMap.put("buyerMarked", buyerMarked);
		resquestMap.put("payType", payType);
		resquestMap.put("orgCode", orgCode);
		resquestMap.put("currencyCode", currencyCode);
		resquestMap.put("directFlag", directFlag);
		resquestMap.put("borrowingMarked", borrowingMarked);
		resquestMap.put("couponFlag", couponFlag);
		resquestMap.put("platformID", platformID);
		resquestMap.put("returnUrl", returnUrl);
		resquestMap.put("noticeUrl", noticeUrl);
		resquestMap.put("partnerID", partnerID);
		resquestMap.put("remark", remark);
		resquestMap.put("charset", charset);
		resquestMap.put("signType", signType);
		resquestMap.put("signMsg", signMsg);
		// form表单请求到接口地址
		List<String> keys = new ArrayList<String>(resquestMap.keySet());
		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) resquestMap.get(name);
			if (com.cn.tianxia.pay.gst.util.StringUtils.isNullOrEmpty(value)) {
				logger.info("删除:" + name);
				resquestMap.remove(name);
			}
		}

		if (resquestMap.isEmpty()) {
			return "参数不能为空！";
		}

		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ url + "\">";
		for (String key : resquestMap.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + resquestMap.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";

		String html = FormString;
		// String html = HttpUtil.HtmlFrom(url, resquestMap);
		logger.info("小强支付扫码支付表单:" + html);
		return html;

	}

	/**
	 * 纯二维码链接的模式
	 * 
	 * @param req
	 * @return
	 */
	public String pureScanPay(Map<String, String> req) {
		String serialID = StringUtils.produceOrderNo("sn");// 序列号
		String submitTime = StringUtils.getDateymdhms(new Date());
		String customerIP = req.get("customerIP");
		String orderAmount = req.get("orderAmount");
		// 获取订单详情
		String orderDetails = getOrderDetails(req, orderAmount, goodsCount);
		String totalAmount = String.valueOf(Integer.parseInt(orderAmount) * Integer.parseInt(goodsCount));
		String payType = req.get("payType");
		// 银联扫码borrowingMarked=1
		if ("B2C_SCAN".equals(payType)) {
			borrowingMarked = "1";
		} else {
			borrowingMarked = "0";
		}
		String orgCode = req.get("orgCode");
		String returnUrl = req.get("returnUrl");// 支付完成地址
		// 组装签名字符串
		String signStr = getSignstr(version, serialID, submitTime, failureTime, customerIP, orderDetails, totalAmount,
				type, buyerMarked, payType, orgCode, currencyCode, directFlag, borrowingMarked, couponFlag, platformID,
				returnUrl, noticeUrl, partnerID, remark, charset, signType);
		// MD5签名
		String signMsg = null;
		try {
			signMsg = ClientSignature.genSignByMD5(signStr, CharsetTypeEnum.UTF8, key);
		} catch (Exception e) {
			logger.info("小强支付验签异常！");
			e.printStackTrace();
		}
		// 构建请求参数
		Map<String, String> resquestMap = new LinkedHashMap<>();
		resquestMap.put("version", version);
		resquestMap.put("serialID", serialID);
		resquestMap.put("submitTime", submitTime);
		resquestMap.put("failureTime", failureTime);
		resquestMap.put("customerIP", customerIP);
		resquestMap.put("orderDetails", orderDetails);
		resquestMap.put("totalAmount", totalAmount);
		resquestMap.put("type", type);
		resquestMap.put("buyerMarked", buyerMarked);
		resquestMap.put("payType", payType);
		resquestMap.put("orgCode", orgCode);
		resquestMap.put("currencyCode", currencyCode);
		resquestMap.put("directFlag", directFlag);
		resquestMap.put("borrowingMarked", borrowingMarked);
		resquestMap.put("couponFlag", couponFlag);
		resquestMap.put("platformID", platformID);
		resquestMap.put("returnUrl", returnUrl);
		resquestMap.put("noticeUrl", noticeUrl);
		resquestMap.put("partnerID", partnerID);
		resquestMap.put("remark", remark);
		resquestMap.put("charset", charset);
		resquestMap.put("signType", signType);
		resquestMap.put("signMsg", signMsg);
		// form表单请求到接口地址
		List<String> keys = new ArrayList<String>(resquestMap.keySet());
		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) resquestMap.get(name);
			if (com.cn.tianxia.pay.gst.util.StringUtils.isNullOrEmpty(value)) {
				logger.info("删除:" + name);
				resquestMap.remove(name);
			}
		}

		if (resquestMap.isEmpty()) {
			return "参数不能为空！";
		}

		String PostParms = "";

		int PostItemTotal = resquestMap.keySet().size();
		int Itemp = 0;
		for (String key : resquestMap.keySet()) {
			PostParms += key + "=" + resquestMap.get(key);
			Itemp++;
			if (Itemp < PostItemTotal) {
				PostParms += "&";
			}
		}
		// 新增pureQr=true 纯二维码链接的模式
		PostParms += "&pureQr=true";
		logger.info("【小强支付请求参数】：" + PostParms);
		String html = XQHttpUtil.sendPost(url, PostParms);
		try {
			logger.info("小强支付响应:" + html);
			if (JSONUtils.toJSONObject(html).containsKey("codeUrl")
					&& !"null".equals(JSONUtils.toJSONObject(html).get("codeUrl"))) {
				logger.info("小强支付获取二维码成功!");
				return JSONUtils.toJSONObject(html).get("codeUrl").toString();
			}
		} catch (Exception e) {
			logger.info("小强支付解析json异常！");
			e.printStackTrace();
			return "";
		}
		return "";
	}

	/**
	 * 获取签名字符串
	 * 
	 * @param version
	 * @param serialID
	 * @param submitTime
	 * @param failureTime
	 * @param customerIP
	 * @param orderDetails
	 * @param totalAmount
	 * @param type
	 * @param buyerMarked
	 * @param payType
	 * @param orgCode
	 * @param currencyCode
	 * @param directFlag
	 * @param borrowingMarked
	 * @param couponFlag
	 * @param platformID
	 * @param returnUrl
	 * @param noticeUrl
	 * @param partnerID
	 * @param remark
	 * @param charset
	 * @param signType
	 * @return signStr
	 */
	private static String getSignstr(String version, String serialID, String submitTime, String failureTime,
			String customerIP, String orderDetails, String totalAmount, String type, String buyerMarked, String payType,
			String orgCode, String currencyCode, String directFlag, String borrowingMarked, String couponFlag,
			String platformID, String returnUrl, String noticeUrl, String partnerID, String remark, String charset,
			String signType) {
		StringBuffer signBuffer = new StringBuffer();
		signBuffer.append("version=" + version);
		signBuffer.append("&serialID=" + serialID);
		signBuffer.append("&submitTime=" + submitTime);
		signBuffer.append("&failureTime=" + failureTime);
		signBuffer.append("&customerIP=" + customerIP);
		signBuffer.append("&orderDetails=" + orderDetails);
		signBuffer.append("&totalAmount=" + totalAmount);
		signBuffer.append("&type=" + type);
		signBuffer.append("&buyerMarked=" + buyerMarked);
		signBuffer.append("&payType=" + payType);
		signBuffer.append("&orgCode=" + orgCode);
		signBuffer.append("&currencyCode=" + currencyCode);
		signBuffer.append("&directFlag=" + directFlag);
		signBuffer.append("&borrowingMarked=" + borrowingMarked);
		signBuffer.append("&couponFlag=" + couponFlag);
		signBuffer.append("&platformID=" + platformID);
		signBuffer.append("&returnUrl=" + returnUrl);
		signBuffer.append("&noticeUrl=" + noticeUrl);
		signBuffer.append("&partnerID=" + partnerID);
		signBuffer.append("&remark=" + remark);
		signBuffer.append("&charset=" + charset);
		signBuffer.append("&signType=" + signType);
		return signBuffer.toString();
	}

	/**
	 * 获取订单明细信息
	 * 
	 * @return
	 */
	private static String getOrderDetails(Map<String, String> req, String orderAmount, String goodsCount) {
		String orderID = req.get("orderID");
		// String displayName = req.get("displayName");
		// String goodsName = req.get("goodsName");
		StringBuffer orderBuffer = new StringBuffer();
		orderBuffer.append(orderID + ",");
		orderBuffer.append(orderAmount + ",");
		orderBuffer.append(displayName + ",");
		orderBuffer.append(goodsName + ",");
		orderBuffer.append(goodsCount);
		return orderBuffer.toString();
	}

	public String XqCallback(HttpServletRequest req) {
		logger.info("=========进入小强回调方法==========");
		// String result = StringUtils.parseRequst(req);
		// logger.info("回调内容：" + result);
		// Map<String, String> resultMap = JSON.parseObject(result, Map.class);
		Map<String, String> resultMap = new LinkedHashMap<>();
		Enumeration enu = req.getParameterNames();
		while (enu.hasMoreElements()) {
			String paraName = (String) enu.nextElement();
			logger.info("key:" + paraName + "  value:" + req.getParameter(paraName).toString());
			resultMap.put(paraName, req.getParameter(paraName).toString());
		}

		String result = "";
		// logger.info("回调内容：" + result);

		String stateCode = resultMap.get("stateCode");
		if (!StringUtils.isEmpty(stateCode) && "2".equals(stateCode)) {
			// 验签
			String signMsg = resultMap.remove("signMsg");
			// resultMap.remove("signMsg");
			String signStr = StringUtils.createRetStr(resultMap, null);
			logger.info("本地服务器signStr:" + signStr);
			logger.info("支付商服务器signMsg:" + signMsg);

			try {
				String newsignMsg = ClientSignature.genSignByMD5(signStr, CharsetTypeEnum.UTF8, key);
				logger.info("本地服务器signMsg:" + newsignMsg);
				if (signMsg.equals(newsignMsg)) {
					return result = "200";
				} else {
					logger.info("小强支付签名验证失败");
					return result = "sign fail";
				}
			} catch (Exception e) {
				logger.info("小强支付签名验证异常");
				e.printStackTrace();
			}
		} else {
			logger.info("小强支付参数格式错误");
			return result = "fail";
		}
		logger.info("小强支付fail");
		return "fail";
	}

	public void TestXqCallback() {
		String orderID = "XQttc201711301529331529335371";// 商户订单号
		String resultCode = "0000";// 处理结果码
		String stateCode = "2";// 状态码
		String orderAmount = "200000";// 商户订单金额
		String payAmount = "200000";// 实际支付金额
		String acquiringTime = "20171130152933";// 收单时间
		String completeTime = "20171130152933";// 处理完成时间
		String orderNo = "1051711301529066314";// 支付流水号
		String partnerID = "10003250838";// 商户ID
		String remark = "ces";// 扩展字段
		String charset = "1";// 编码方式
		String signType = "2";// 签名类型
		String signMsg = "f8c38928b41b7485177dd7a8845af659"; // 签名字符串

		Map<String, String> params = new LinkedHashMap<>();
		params.put("orderID", orderID);
		params.put("resultCode", resultCode);
		params.put("stateCode", stateCode);
		params.put("orderAmount", orderAmount);
		params.put("payAmount", payAmount);
		params.put("acquiringTime", acquiringTime);
		params.put("completeTime", completeTime);
		params.put("orderNo", orderNo);
		params.put("partnerID", partnerID);
		params.put("remark", remark);
		params.put("charset", charset);

		String signStr = StringUtils.createRetStr(params);
		logger.info("本地signStr:" + signStr);

		try {
			// signMsg = ClientSignature.genSignByMD5(signStr,
			// CharsetTypeEnum.UTF8, key);
			logger.info("本地sign:" + signMsg);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		params.put("signMsg", signMsg);
		try {
			String ss = HttpUtil.RequestForm("http://localhost:8087/JJF/PlatformPay/XQNotify.do", params);
		} catch (Exception e) {
			logger.info("小强支付签名验证异常");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String orderID = "XQ" + System.currentTimeMillis();
		Map<String, String> param = new LinkedHashMap<>();
		String version = "1.0";
		String failureTime = "";
		// 获取订单详情
		String type = "1000";
		String buyerMarked = "";
		String currencyCode = "1";
		String borrowingMarked = "0";
		String couponFlag = "1";
		String platformID = "";// 平台商户ID
		String charset = "1";
		String signType = "2";
		String key = "30820122300d06092a864886f70d01010105000382010f003082010a0282010100992ab59986eb55c8fe7e3f113a3f37e760aad43e40674bb0ec22607b2c1018b1b6259477c960e8325264ca64067de32b1263de773f06bdb981e81a094387b3ccc99dddbce2fb34264455a047e85d0adb224c83712ee57bbac6ec08512d1d1050c527be3fee1f21230d1631564aa96f1eaa21995734cbee824161bf6b7d8a1c38cec2cac0b08c60be8e1055420fb289bab5d0bdb31b516608f28d644b2500c0e6413c06de118c2bdaa2bfbda84a2a95e57fdf1e36df0753bec1a89ebf12ced95c287c30afd7a02e272c4bec52f5d84c09faadd27405bbffd4dc1a15bb1f8abd1da97fc15b5763494af98a0981b328bd0830c95777698ca899c5d880e8cf2f90550203010001";
		String url = "https://www.xqiangpay.net/website/pay.htm";
		String goodsCount = "1";
		String displayName = "txcz";
		String goodsName = "txcz";
		String directFlag = "1";
		String partnerID = "10000079540";
		String noticeUrl = "http://119.23.200.153/Demo/xQingPayNotifyAction";
		String remark = "ces";

		param.put("version", version);
		param.put("failureTime", failureTime);
		param.put("type", type);
		param.put("buyerMarked", buyerMarked);
		param.put("currencyCode", currencyCode);
		param.put("borrowingMarked", borrowingMarked);
		param.put("couponFlag", couponFlag);
		param.put("charset", charset);
		param.put("platformID", platformID);
		param.put("signType", signType);
		param.put("key", key);
		param.put("url", url);
		param.put("goodsCount", goodsCount);// 商品数量
		param.put("displayName", displayName);// 下单商户显示名
		param.put("goodsName", goodsName);// 商品名称
		param.put("directFlag", directFlag);// 是否直连
		param.put("partnerID", partnerID);// 商户ID
		param.put("noticeUrl", noticeUrl);// 回调通知地址
		param.put("remark", remark);// 备注remark
		// logger.info("json配置:" + JSONObject.fromObject(param).toString());
		XQPayServiceImpl xq = new XQPayServiceImpl(param);
		/********************* 微信支付 ******************/
		Map<String, String> scanMap = new HashMap<String, String>();
		scanMap.put("orderAmount", "1000");// 订单明细金额
		scanMap.put("customerIP", "127.0.0.1");// 客户端ip
		scanMap.put("orderID", orderID);// 订单号
		scanMap.put("payType", "WX_H5");// 付款方支付方式 BANK_B2CB2C网银 WX微信
		scanMap.put("orgCode", "wx_h5");// 目标资金机构代码
		scanMap.put("returnUrl", "http://119.23.200.153/Demo/pay-success.jsp");// 支付完成地址
		// xq.bankPay(scanMap);
		xq.pureScanPay(scanMap);
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
		int int_amount = (int) (amount * 100);
		scanMap.put("orderAmount", String.valueOf(int_amount));// 订单明细金额
		scanMap.put("customerIP", ip);// 客户端ip
		scanMap.put("orderID", order_no);// 订单号
		scanMap.put("payType", "BANK_B2C");// 付款方支付方式 BANK_B2CB2C网银 WX微信
		scanMap.put("orgCode", pay_code);// 目标资金机构代码
		scanMap.put("returnUrl", refereUrl);// 支付完成地址
		String html = bankPay(scanMap);
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

		Map<String, String> scanMap = new HashMap<String, String>();
		int int_amount = (int) (amount * 100);
		scanMap.put("orderAmount", String.valueOf(int_amount));// 订单明细金额
		scanMap.put("customerIP", ip);// 客户端ip
		scanMap.put("orderID", order_no);// 订单号
		scanMap.put("payType", pay_code); // 付款方支付方式 BANK_B2C网银 WX微信
		scanMap.put("orgCode", pay_code.toLowerCase());// 目标资金机构代码
		scanMap.put("returnUrl", refereUrl);// 支付完成地址
		if (!StringUtils.isEmpty(mobile) && mobile.equals("mobile")) {
			logger.info("小强手机端调用支付接口");
			// 快捷支付
			if ("QUICK_PAY".equals(pay_code)) {
				String form = bankPay(scanMap);
				return PayUtil.returnPayJson("success", "1", "小强快捷表单获取成功！", userName, amount, order_no, form);
			}
			// 手机银联扫码
			if ("B2C_SCAN".equals(pay_code)) {
				scanMap.put("payType", pay_code);
				scanMap.put("orgCode", pay_code.toLowerCase());
			} else if ("WX".equals(pay_code)) {
				scanMap.put("payType", pay_code + "_H5");
				scanMap.put("orgCode", pay_code.toLowerCase() + "_h5");
				String form = scanPay(scanMap);
				return PayUtil.returnPayJson("success", "1", "小强微信H5表单获取成功！", userName, amount, order_no, form);
			} else {
				// 手机微信支付宝QQ扫码 手机京东h5
				scanMap.put("payType", pay_code + "_H5");
				scanMap.put("orgCode", pay_code.toLowerCase() + "_h5");
			}
		}
		
		//pc快捷支付
		if ("QUICK_PAY".equals(pay_code)) {
			String form = bankPay(scanMap);
			return PayUtil.returnPayJson("success", "1", "小强快捷表单获取成功！", userName, amount, order_no, form);
		}
		
		String qrcode = pureScanPay(scanMap);
		// 请求失败返回数据
		if (StringUtils.isEmpty(qrcode)) {
			return PayUtil.returnPayJson("error", "4", "支付接口请求失败!", userName, amount, order_no, "");
		}
		// 手机端返回格式
		if (!StringUtils.isEmpty(mobile) && mobile.equals("mobile")) {
			return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no, qrcode);
		} else {
			// pc端返回
			return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no, qrcode);
		}
	}

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }

}
