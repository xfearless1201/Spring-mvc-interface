package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.json.JSONUtils;
import com.cn.tianxia.pay.bft.util.Mobo360Config;
import com.cn.tianxia.pay.bft.util.Mobo360Merchant;
import com.cn.tianxia.pay.bft.util.Mobo360SignUtil;
import com.cn.tianxia.pay.bft.util.ScanPayResponseEntity;
import com.cn.tianxia.pay.dc.util.HttpClientUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.mkt.util.HttpUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;
import sun.java2d.pipe.SpanShapeRenderer.Simple;
import sun.util.logging.resources.logging;

import org.apache.commons.codec.binary.Base64;

/**
 * 
 * @Description:TODO
 * 
 * @author:zouwei
 * 
 * @time:2017年8月24日 下午9:25:48
 * 
 */
public class BFTPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(BFTPayServiceImpl.class);
	// 商户ID
	public static String PLATFORM_ID;
	// 商户帐号
	public static String MERCHANT_ACC;
	// MD5签名的key值
	public static String MD5_KEY;
	// 接口版本
	public static String MOBAOPAY_API_VERSION;
	// 通知地址
	public static String MERCHANT_NOTIFY_URL;
	// 支付系统网关
	public static String MOBAOPAY_GETWAY;
	// 请选择签名类型， MD5、CER(证书文件)、RSA
	public static String SIGN_TYPE;
	// 超时时间
	public static String overTime;
	// 扩展参数
	public static String merchParam;

	// 商品名称|商品数量
	public static String tradeSummary;

	// 选择支付方式 入或选择支付方式不存在则认为是该商户所拥有的全部方式。1.网银 4.支付宝扫码5. 微信扫码6. QQ 扫码 8.京东扫码
	public static String choosePayType;

	public BFTPayServiceImpl(Map<String, String> pmap) {
		net.sf.json.JSONObject jo = new net.sf.json.JSONObject().fromObject(pmap);
		if (null != pmap) {
			PLATFORM_ID = jo.get("PLATFORM_ID").toString();
			MERCHANT_ACC = jo.get("MERCHANT_ACC").toString();
			MD5_KEY = jo.get("MD5_KEY").toString();
			MOBAOPAY_API_VERSION = jo.get("MOBAOPAY_API_VERSION").toString();
			MERCHANT_NOTIFY_URL = jo.get("MERCHANT_NOTIFY_URL").toString();
			MOBAOPAY_GETWAY = jo.get("MOBAOPAY_GETWAY").toString();
			SIGN_TYPE = jo.get("SIGN_TYPE").toString();
			PLATFORM_ID = jo.get("PLATFORM_ID").toString();
			merchParam = jo.get("merchParam").toString();
			overTime = jo.get("overTime").toString();
			tradeSummary = jo.getString("tradeSummary");
			choosePayType = jo.getString("choosePayType");
		}
	}

	/**
	 * 网银支付
	 * 
	 * @param request
	 * @return
	 */
	public String bankPay(Map<String, String> request) {

		try {
			Mobo360SignUtil.init(MD5_KEY, SIGN_TYPE);
		} catch (Exception e1) {
			logger.info("佰付通网银初支付初始化参数异常!");
			e1.printStackTrace();
		}
		// 组织请求数据
		Map<String, String> paramsMap = new HashMap<String, String>();
		// request.setCharacterEncoding("UTF-8");
		paramsMap.put("apiName", request.get("apiName"));
		paramsMap.put("apiVersion", MOBAOPAY_API_VERSION);
		paramsMap.put("platformID", PLATFORM_ID);
		paramsMap.put("merchNo", MERCHANT_ACC);
		paramsMap.put("orderNo", request.get("orderNo"));
		paramsMap.put("tradeDate", request.get("tradeDate"));
		paramsMap.put("amt", request.get("amt"));
		paramsMap.put("merchUrl", MERCHANT_NOTIFY_URL);
		paramsMap.put("merchParam", merchParam);
		paramsMap.put("tradeSummary", tradeSummary);
		/**
		 * bankCode为空，提交表单后浏览器在新窗口显示支付系统收银台页面，在这里可以通过账户余额支付或者选择银行支付；
		 * bankCode不为空，取值只能是接口文档中列举的银行代码，提交表单后浏览器将在新窗口直接打开选中银行的支付页面。
		 * 无论选择上面两种方式中的哪一种，支付成功后收到的通知都是同一接口。
		 **/
		paramsMap.put("bankCode", request.get("bankCode"));
		paramsMap.put("choosePayType", choosePayType);

		String paramsStr;
		String signMsg = null;
		try {
			paramsStr = Mobo360Merchant.generatePayRequest(paramsMap);
			signMsg = Mobo360SignUtil.signData(paramsStr); // 签名数据
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 签名源数据

		String epayUrl = MOBAOPAY_GETWAY; // 支付网关地址
		paramsMap.put("signMsg", signMsg);
		logger.info("佰付通" + "(网关支付  签名后数据)" + paramsMap);
		// 生成表单并自动提交到支付网关。
		// StringBuffer sbHtml = new StringBuffer();
		String html = HttpUtil.HtmlFrom(epayUrl, paramsMap);
		logger.info("佰付通" + "(网关支付 from)" + html);
		return html;
	}

	public String scanPay(Map<String, String> request) {
		// 签名初始化
		String returnStr = "";
		try {
			Mobo360SignUtil.init(MD5_KEY, SIGN_TYPE);
		} catch (Exception e1) {
			logger.info("佰付通" + "初始化签名异常！");
			e1.printStackTrace();
		}
		// 组织输入数据
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put("apiName", request.get("payType"));
		requestMap.put("apiVersion", MOBAOPAY_API_VERSION);
		requestMap.put("platformID", PLATFORM_ID);
		requestMap.put("merchNo", MERCHANT_ACC);
		requestMap.put("orderNo", request.get("orderNo"));
		requestMap.put("tradeDate", request.get("tradeDate"));
		requestMap.put("amt", request.get("amt"));
		requestMap.put("merchUrl", MERCHANT_NOTIFY_URL);
		requestMap.put("merchParam", merchParam);
		requestMap.put("tradeSummary", tradeSummary);
		requestMap.put("overTime", overTime);
		requestMap.put("customerIP", request.get("customerIP"));

		try {
			String paramsStr = Mobo360Merchant.generateScanPayRequest(requestMap);
			String signStr = Mobo360SignUtil.signData(paramsStr);
			paramsStr = paramsStr + "&signMsg=" + signStr;
			logger.info("佰付通" + "(" + request.get("payType") + "扫码后   签名后数据)" + paramsStr);
			// 发起请求并获取返回数据
			String responseMsg = Mobo360Merchant.transact(paramsStr, MOBAOPAY_GETWAY);
			logger.info("响应xml数据:" + responseMsg);

			if (StringUtils.isNullOrEmpty(responseMsg)) {
				logger.info("佰付通" + request.get("payType") + "扫码请求二维码失败！");
				return returnStr;
			}

			// 处理返回数据
			ScanPayResponseEntity entity = new ScanPayResponseEntity();
			entity.parse(responseMsg);
			logger.info("佰付通" + "响应码:" + entity.getRespCode() + "  响应描述:" + entity.getRespDesc() + "  返回码:"
					+ entity.getRespCode() + "二维码生成码" + entity.getCodeUrl());
			if (!"00".equals(entity.getRespCode())) {
				logger.info("佰付通" + request.get("payType") + "扫码响应二维码数据解析异常！");
				return returnStr;
			}

			String codeUrl = new String(Base64.decodeBase64(entity.getCodeUrl()));
			logger.info("佰付通" + "二维码获取成功！:" + codeUrl);
			return codeUrl;

		} catch (Exception e) {
			e.printStackTrace();
			logger.info("佰付通" + "扫码接口异常！");
			return returnStr;
		}

	}

	public boolean BFTCallback(HttpServletRequest request) {
		// 签名初始化
		try {
			Mobo360SignUtil.init(MD5_KEY, SIGN_TYPE);
		} catch (Exception e) {
			logger.info("佰付通" + "初始化签名异常！");
			e.printStackTrace();
		}

		// 获取请求参数，并将数据组织成前面验证源字符串
		// request.setCharacterEncoding("utf-8");
		String apiName = request.getParameter("apiName");
		String notifyTime = request.getParameter("notifyTime");
		String tradeAmt = request.getParameter("tradeAmt");
		String merchNo = request.getParameter("merchNo");
		String merchParam = request.getParameter("merchParam");
		String orderNo = request.getParameter("orderNo");
		String tradeDate = request.getParameter("tradeDate");
		String accNo = request.getParameter("accNo");
		String accDate = request.getParameter("accDate");
		String orderStatus = request.getParameter("orderStatus");
		String signMsg = request.getParameter("signMsg");
		signMsg.replaceAll(" ", "\\+");

		String srcMsg = String.format(
				"apiName=%s&notifyTime=%s&tradeAmt=%s&merchNo=%s&merchParam=%s&orderNo=%s&tradeDate=%s&accNo=%s&accDate=%s&orderStatus=%s",
				apiName, notifyTime, tradeAmt, merchNo, merchParam, orderNo, tradeDate, accNo, accDate, orderStatus);

		// 验证签名
		boolean verifyRst = false;
		try {
			verifyRst = Mobo360SignUtil.verifyData(signMsg, srcMsg);
		} catch (Exception e) {
			logger.info("佰付通" + "签名异常！");
			e.printStackTrace();
		}
		if (verifyRst) {
			logger.info("佰付通" + "验签通过！");
			/**
			 * 验证通过后，请在这里加上商户自己的业务逻辑处理代码. 比如： 1、根据商户订单号取出订单数据
			 * 2、根据订单状态判断该订单是否已处理（因为通知会收到多次），避免重复处理 3、比对一下订单数据和通知数据是否一致，例如金额等
			 * 4、接下来修改订单状态为已支付或待发货 5、...
			 */

			// 判断通知类型，若为后台通知需要回写"SUCCESS"给支付系统表明已收到支付通知
			// 否则支付系统将按一定的时间策略在接下来的24小时内多次发送支付通知。
			if (request.getParameter("notifyType").equals("1")) {
				// 回写‘SUCCESS’方式一：
				// 重定向到一个专门用于处理回写‘SUCCESS’的页面，这样可以保证输出内容中只有'SUCCESS'这个字符串。
				// response.setContentType("text/html; charset=UTF-8");
				// response.sendRedirect("notify.jsp");
				// 回写‘SUCCESS’方式二： 直接让当前输出流中包含‘SUCCESS’字符串。两种方式都可以，但建议采用第一种。
				// out.println("SUCCESS");
				logger.info("验证通过 notifyType=1！");
				return verifyRst;
			}
		}

		logger.info("佰付通" + "验签失败！");
		return verifyRst;

	}

	public void testBFTCallback() {

		// 签名初始化
		try {
			Mobo360SignUtil.init("7d251b5a532731b0447d2ca0a8ea3212", "MD5");
		} catch (Exception e) {
			logger.info("佰付通" + "初始化签名异常！");
			e.printStackTrace();
		}

		String apiName = "aa";
		String notifyTime = "bb";
		String tradeAmt = "cc";
		String merchNo = "1234";
		String merchParam = "";
		String orderNo = "BFTbl1201708290918460918468987";
		String tradeDate = "ss";
		String accNo = "12345";
		String accDate = "12345";
		String orderStatus = "1";
		String signMsg = "";
		String notifyType = "1";

		String srcMsg = String.format(
				"apiName=%s&notifyTime=%s&tradeAmt=%s&merchNo=%s&merchParam=%s&orderNo=%s&tradeDate=%s&accNo=%s&accDate=%s&orderStatus=%s",
				apiName, notifyTime, tradeAmt, merchNo, merchParam, orderNo, tradeDate, accNo, accDate, orderStatus);

		try {
			signMsg = Mobo360SignUtil.signData(srcMsg);
			logger.info("本地sign" + signMsg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, String> postMap = new HashMap<String, String>();
		postMap.put("apiName", apiName);
		postMap.put("notifyTime", notifyTime);
		postMap.put("tradeAmt", tradeAmt);
		postMap.put("merchNo", merchNo);
		postMap.put("merchParam", merchParam);
		postMap.put("orderNo", orderNo);
		postMap.put("tradeDate", tradeDate);
		postMap.put("accNo", accNo);
		postMap.put("accDate", accDate);
		postMap.put("orderStatus", orderStatus);
		postMap.put("signMsg", signMsg);
		postMap.put("notifyType", notifyType);
		// http://182.16.110.186:8180/XPJ/PlatformPay/bankingNotify.do
		String Url = "http://localhost:8080/XPJ/PlatformPay/bankingNotify.do";
		String resp = HttpUtil.RequestForm(Url, postMap);

	}

	public static void main(String[] args) {
		// 商户ID
		String PLATFORM_ID = "210001110012875";
		// 商户帐号
		String MERCHANT_ACC = "210001110012875";
		// MD5签名的key值
		String MD5_KEY = "a6956840facb30af80603fd76314d084";
		// 接口版本
		String MOBAOPAY_API_VERSION = "1.0.0.0";
		// 通知地址
		String MERCHANT_NOTIFY_URL = "http://localhost/Pay/MFT_notify_url";
		// 支付系统网关
		String MOBAOPAY_GETWAY = "http://epay.zapwka.top/cgi-bin/netpayment/pay_gate.cgi";
		// 请选择签名类型， MD5、CER(证书文件)、RSA
		String SIGN_TYPE = "MD5";
		String merchParam = "";
		String overTime = "60";
		String tradeSummary = "天下支付";
		String choosePayType = "1";// 网银

		Map<String, String> params = new HashMap<String, String>();
		params.put("PLATFORM_ID", PLATFORM_ID);
		params.put("MERCHANT_ACC", MERCHANT_ACC);
		params.put("MD5_KEY", MD5_KEY);
		params.put("MOBAOPAY_API_VERSION", MOBAOPAY_API_VERSION);
		params.put("MERCHANT_NOTIFY_URL", MERCHANT_NOTIFY_URL);
		params.put("MOBAOPAY_GETWAY", MOBAOPAY_GETWAY);
		params.put("SIGN_TYPE", SIGN_TYPE);
		params.put("merchParam", merchParam);
		params.put("overTime", overTime);
		params.put("tradeSummary", tradeSummary);
		params.put("choosePayType", choosePayType);

		// json配置
		// logger.info("JSON:" +
		// JSONUtils.toJSONString(JSONObject.fromObject(params)));

		BFTPayServiceImpl bft = new BFTPayServiceImpl(params);

		// Map<String, String> bankMap = new HashMap<String, String>();
		// bankMap.put("apiName", "WEB_PAY_B2C");
		// bankMap.put("choosePayType", choosePayType);
		// bankMap.put("bankCode", "ICBC");
		// SimpleDateFormat dateFormater = new SimpleDateFormat("yyyyMMdd");
		// Date date = new Date();
		// String sdfDate = dateFormater.format(date);
		// bankMap.put("tradeDate", sdfDate);
		// bankMap.put("orderNo", "BL1" + System.currentTimeMillis());
		// bankMap.put("tradeSummary", "天下支付");
		// bankMap.put("amt", "1.00");
		// bft.bankPay(bankMap);

		// Map<String, String> scanMap = new HashMap<String, String>();
		// scanMap.put("payType", "AL_SCAN_PAY");
		// SimpleDateFormat dateFormater1 = new SimpleDateFormat("yyyyMMdd");
		// Date date1 = new Date();
		// String sdfDate1 = dateFormater1.format(date1);
		// scanMap.put("tradeDate", sdfDate1);
		// scanMap.put("orderNo", "BL1" + System.currentTimeMillis());
		// scanMap.put("tradeSummary", "天下支付");
		// scanMap.put("amt", "-1");
		// scanMap.put("merchParam", "TXWL");
		// scanMap.put("overTime", "60");
		// scanMap.put("customerIP", "103.94.147.3");
		// bft.scanPay(scanMap);

		bft.testBFTCallback();

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
		// 必输，取值：WAP 方式：“WAP_PAY_B2C”（手机支付） WEB 方式：“WEB_PAY_B2C”（pc 浏览器）

		String pc_web = "WEB_PAY_B2C";
		bankMap.put("apiName", pc_web);
		if (!StringUtils.isNullOrEmpty(mobile) && mobile.equals("WAP_PAY_B2C")) {
			logger.info("佰付通手机端调用支付接口");
			bankMap.put("apiName", mobile);
		}
		bankMap.put("bankCode", pay_code);
		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
		String sdfDate = dateFormater.format(date);
		bankMap.put("tradeDate", sdfDate);
		bankMap.put("orderNo", order_no);
		bankMap.put("amt", String.valueOf(amount));

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

		Map<String, String> scanMap = new HashMap<String, String>();
		scanMap.put("payType", pay_code);
		SimpleDateFormat dateFormater1 = new SimpleDateFormat("yyyyMMdd");
		Date date1 = new Date();
		String sdfDate1 = dateFormater1.format(date1);
		scanMap.put("tradeDate", sdfDate1);
		scanMap.put("orderNo", order_no);
		scanMap.put("amt", String.valueOf(amount));
		scanMap.put("customerIP", ip);
		String msg = scanPay(scanMap);

		if (StringUtils.isNullOrEmpty(msg)) {
			return PayUtil.returnPayJson("error", "2", "支付接口请求失败!", userName, amount, order_no, "");
		}
		return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no, msg);
	}

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }

}
