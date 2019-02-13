package com.cn.tianxia.pay.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.json.JSONUtils;
import com.cn.tianxia.pay.dc.util.HttpClientUtil;
import com.cn.tianxia.pay.dc.util.HttpClientUtils;
import com.cn.tianxia.pay.dc.util.MerchSdkSign;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.FileLog;
import com.cn.tianxia.util.IPTools;

/**
 * 
 * @Description:TODO
 * 
 * @author:zouwei
 * 
 * @time:2017年8月6日 下午4:17:20
 * 
 */
public class DCPayServiceImpl implements PayService {

	private static String userId;
	private static String signKey;
	private static String dataKey;
	private static String url;
	private static String characterSet;// 目前只支持 02- UTF-8
	private static String version; // 版本号
	private static String signType; // 签名方式MD5
	private static String callBackUrl;// 网银异步回调地址
	// static String scanCallBackUrl;// 扫码异步回调地址
	private final static Logger logger = LoggerFactory.getLogger(DCPayServiceImpl.class);

	public DCPayServiceImpl(Map<String, String> pmap) {
		net.sf.json.JSONObject jo = new net.sf.json.JSONObject().fromObject(pmap);
		if (null != pmap) {
			userId = jo.get("userId").toString();
			signKey = jo.get("signKey").toString();
			dataKey = jo.get("dataKey").toString();
			url = jo.get("url").toString();
			characterSet = jo.get("characterSet").toString();
			version = jo.get("version").toString();
			signType = jo.get("signType").toString();
			callBackUrl = jo.get("callBackUrl").toString();
		}
	}

	/**
	 * 扫码支付
	 * 
	 * @return
	 */
	public String scanPay(Map<String, String> scanMap) {
		Map<String, String> postParamMap = new HashMap<String, String>();
		postParamMap.put("type", "SaoMa");// 固定数据
		postParamMap.put("characterSet", characterSet);// 目前只支持 02- UTF-8
		// TODO ip需要修改
		// String ip =scanMap.get("ipAddress");
		postParamMap.put("ipAddress", scanMap.get("ipAddress"));// 本地ip地址
		postParamMap.put("version", version);// 版本号
		postParamMap.put("signType", signType);// 签名方式MD5
		/*
		 * WXSM 微信扫码 ZFBSM 支付宝扫码 QQSM QQ钱包扫码 WXTM 微信条码 ZFBTM 支付宝条码 QQTM QQ钱包条码
		 * ZFBONEYARD 支付一码付 QQONEYARD 微信一码付
		 */
		postParamMap.put("tranCode", scanMap.get("tranCode"));//
		postParamMap.put("accountType", "0");// 0 T0、1 T1
		postParamMap.put("requestId", scanMap.get("requestId"));// 用户请求的交易流水号唯一

		postParamMap.put("userId", userId);// 用户id
		postParamMap.put("orderAmount", scanMap.get("orderAmount"));// 单位 分
		postParamMap.put("orderTitle", "天下支付");//
		// 异步回调地址 http 或者https开头的外网服务器地址
		postParamMap.put("callBackUrl", callBackUrl);
		postParamMap.put("authCode", "");// 条码支付必须 由扫描器扫描条形码获取

		// 支付宝一码付、微信一码付必传
		// 支付渠道是支付宝传 buyer_id 微信则 open_id
		postParamMap.put("buyerOpenId", "");//
		postParamMap.put("subAppid", "");// 微信一码付时必填

		String hmac = MerchSdkSign.getSign(postParamMap, signKey);
		postParamMap.put("hmac", hmac);//

		logger.info("签名：" + hmac);
		String postJson = JSON.toJSONString(postParamMap);
		logger.info("请求数据：" + postJson);
		String res = "";
		try {
			res = HttpClientUtil.doPost(url, postJson, "UTF-8", "application/json");
			if (null == res) {
				logger.info("没有返回数据");
				FileLog f = new FileLog();
				Map<String, String> map = new HashMap<>();
				map.put("apiurl", url);
				map.put("data", postJson);
				map.put("msg", "接口响应数据为空");
				map.put("Function", "DCPayScanPay");
				f.setLog("DCPay_scan", map);
				return res;
			}
			logger.info("返回数据：" + res);
			JSONObject jasonObject = JSON.parseObject(res);
			// Map map = (Map) jasonObject;
			// hmac = map.get("hmac") + "";
			// map.remove("hmac");// 返回数据签名不包含签名数据
			// logger.info(map.toString());
			// String hmac1 = MerchSdkSign.getSign(map, signKey);
			// logger.info("签名结果：" + hmac1.equals(hmac));

			if ("000000".equals(jasonObject.get("returnCode").toString())
					&& !StringUtils.isNullOrEmpty(jasonObject.get("qrCodeUrl").toString())) {
				String r_url = jasonObject.get("qrCodeUrl").toString();
				return r_url;
			}

		} catch (Exception e) {
			FileLog f = new FileLog();
			Map<String, String> map = new HashMap<>();
			map.put("apiurl", url);
			map.put("data", postJson);
			map.put("Exception", e.toString());
			map.put("msg", res);
			map.put("Function", "DCPayScanPay");
			f.setLog("DCPay_scan", map);
		}
		return res;
	}

	/**
	 * 网银支付
	 * 
	 * @return
	 */
	public String BankingPay(Map<String, String> bankMap) {
		Map<String, String> postParamMap = new HashMap<String, String>();
		postParamMap.put("type", "WangGuan");// 固定数据
		postParamMap.put("characterSet", characterSet);// 目前只支持 02- UTF-8
		postParamMap.put("ipAddress", bankMap.get("ipAddress"));// 本地ip地址
		postParamMap.put("version", version);// 版本号
		postParamMap.put("signType", signType);// 签名方式MD5
		/*
		 * WXSM 微信扫码 ZFBSM 支付宝扫码 QQSM QQ钱包扫码 WXTM 微信条码 ZFBTM 支付宝条码 QQTM QQ钱包条码
		 * ZFBONEYARD 支付一码付 QQONEYARD 微信一码付
		 */
		postParamMap.put("tranCode", bankMap.get("tranCode"));//
		postParamMap.put("accountType", "1");// 0 T0、1 T1
		postParamMap.put("requestId", bankMap.get("requestId"));// 用户请求的交易流水号唯一

		postParamMap.put("userId", userId);// 用户id
		postParamMap.put("orderAmount", bankMap.get("orderAmount"));// 单位 分
		postParamMap.put("orderTitle", "天下网络");//
		// 异步回调地址 http 或者https开头的外网服务器地址
		postParamMap.put("callBackUrl", callBackUrl);

		String hmac = MerchSdkSign.getSign(postParamMap, signKey);
		postParamMap.put("hmac", hmac);//

		logger.info("签名：" + hmac);
		String postJson = JSON.toJSONString(postParamMap);
		logger.info("请求数据：" + postJson);
		String res = "";
		try {
			res = HttpClientUtil.doPost(url, postJson, "UTF-8", "application/json");

			if (null == res) {
				logger.info("没有返回数据");
				FileLog f = new FileLog();
				Map<String, String> map = new HashMap<>();
				map.put("apiurl", url);
				map.put("data", postJson);
				map.put("msg", "接口响应数据为空");
				map.put("Function", "BankingPay");
				f.setLog("DCPay_bank", map);
				return res;
			}

			logger.info("返回数据：" + res);
			JSONObject jasonObject = JSON.parseObject(res);
			// Map map = (Map) jasonObject;
			// hmac = map.get("hmac") + "";
			// map.remove("hmac");// 返回数据签名不包含签名数据
			// System.out.println(map);
			// String hmac1 = MerchSdkSign.getSign(map, signKey);
			// logger.info("签名结果：" + hmac1.equals(hmac));

			if ("000000".equals(jasonObject.get("returnCode").toString())
					&& !StringUtils.isNullOrEmpty(jasonObject.get("qrCodeUrl").toString())) {
				String r_url = jasonObject.get("qrCodeUrl").toString();
				return r_url;
			}
		} catch (Exception e) {
			FileLog f = new FileLog();
			Map<String, String> map = new HashMap<>();
			map.put("apiurl", url);
			map.put("data", postJson);
			map.put("Exception", e.toString());
			map.put("msg", res);
			map.put("Function", "BankingPay");
			f.setLog("DCPay_bank", map);
		}

		return res;
	}

	/**
	 * 回调
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public boolean callback(HttpServletRequest request, HttpServletResponse response) {

		String[] ParmList = new String[] { "userId", "requestId", "payNo", "returnCode", "message", "characterSet",
				"ipAddress", "signType", "type", "version", "hmac", "amount", "ordersts", "totalFee", "payAmount" };

		Map<String, String> reqMap = new HashMap<String, String>();
		for (String Key : ParmList) {// 接收参数
			if (request.getParameterMap().containsKey(Key)) {
				reqMap.put(Key, request.getParameter(Key));
			}
		}

		String postJson = JSON.toJSONString(reqMap);
		logger.info("得成支付回调请求的参数:" + JSON.toJSONString(reqMap));

		String hmac = reqMap.get("hmac").toString();
		reqMap.remove("hmac");// 剔除hmac
		String hmac1 = MerchSdkSign.getSign(reqMap, signKey);

		if (hmac.equals(hmac1)) {
			return true;
		}

		logger.info("得成支付回调请求签名验证失败 " + hmac.equals(hmac1) + " 本地 hmac:" + hmac + "  请求hmac1:" + hmac1);
		return false;
	}

	public void testCallback() {
		String[] ParmList = new String[] { "userId", "requestId", "payNo", "returnCode", "message", "characterSet",
				"ipAddress", "signType", "type", "version", "hmac", "amount", "ordersts", "totalFee", "payAmount" };
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("userId", "10019191");
		reqMap.put("requestId", "DCbl1201708071020371020373307");
		reqMap.put("payNo", "123456");
		reqMap.put("returnCode", "000000");
		reqMap.put("message", "成功！");
		reqMap.put("characterSet", "UTF-8");
		reqMap.put("ipAddress", "10.0.0.11");
		reqMap.put("signType", "01");
		reqMap.put("type", "SaoMa");
		reqMap.put("version", "1.0.0");

		reqMap.put("amount", "2");
		reqMap.put("ordersts", "S");
		reqMap.put("totalFee", "0");
		reqMap.put("payAmount", "2");
		// Map map = (Map) jasonObject;
		// hmac = map.get("hmac") + "";
		// map.remove("hmac");// 返回数据签名不包含签名数据
		// System.out.println(map);
		// String hmac1 = MerchSdkSign.getSign(map, signKey);
		// logger.info("签名结果：" + hmac1.equals(hmac));
		String hmac = MerchSdkSign.getSign(reqMap, "ffffffffffffffffffffffffffffffff");
		reqMap.put("hmac", hmac);

		String postJson = JSON.toJSONString(reqMap);
		logger.info("请求数据：" + postJson);
		String res = "";
		String p_usrl = "http://182.16.110.186:8080/XPJ/PlatformPay/DCNotify.do";
		try {
			// res=TransferHttpClientUtils.post(p_usrl, reqMap);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/** "application/x-www-form-urlencoded" **/
		res = HttpClientUtil.doPost("http://localhost:8080/XPJ/PlatformPay/DCNotify.do", postJson, "UTF-8",
				"application/json");
		System.out.println(res);

	}

	public static void main(String[] args) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("userId", "10000967");
		map.put("signKey", "8F042223DE4F4F89913344387B64420C");
		map.put("dataKey", "BAF7B30250184E00A6C1C0D90F4FDB7F");
		map.put("url", "http://pos.gzdcdata.com/dcpay");
		map.put("characterSet", "02");// 目前只支持 02- UTF-8
		map.put("version", "1.0.0");// 版本号
		map.put("signType", "01");// 签名方式MD5
		map.put("callBackUrl", "http://182.16.110.186:8080/XPJ/PlatformPay/DCNotify.do");
		// System.out.println(JSONUtils.toJSONObject(map));
		DCPayServiceImpl dcPay = new DCPayServiceImpl(map);

		Map<String, String> scanMap = new HashMap<String, String>();
		scanMap.put("tranCode", "WXSM");
		scanMap.put("ipAddress", "10.0.0.11");// 本地ip地址
		scanMap.put("requestId", System.currentTimeMillis() + "500");// 用户请求的交易流水号唯一

		scanMap.put("orderAmount", "200");// 单位 分
		System.out.println(dcPay.scanPay(scanMap));

		DCPayServiceImpl bankdcPay = new DCPayServiceImpl(map);
		// Map<String, String> bankMap = new HashMap<String, String>();
		// bankMap.put("tranCode", "B2C");
		// bankMap.put("ipAddress", "10.0.0.11");// 本地ip地址
		// bankMap.put("requestId", System.currentTimeMillis() +
		// "500");//用户请求的交易流水号唯一
		//
		//
		// bankMap.put("orderAmount", "200");// 单位 分
		// System.out.println(dcPay.BankingPay(bankMap));
		// bankdcPay.testCallback();
		// System.out.println(dcPay.BankingPay());
	}

	@Override
	public net.sf.json.JSONObject wyPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String userName = payEntity.getUsername();
		String pay_url = payEntity.getPayUrl();
		String mobile = payEntity.getMobile();
		String ip = payEntity.getIp();

		Map<String, String> bankMap = new HashMap<String, String>();
		bankMap.put("tranCode", pay_code);
		bankMap.put("ipAddress", ip);// 本地ip地址
		bankMap.put("requestId", order_no);// 用户请求的交易流水号唯一
		int int_amount = (int) (amount * 100);
		bankMap.put("orderAmount", String.valueOf(int_amount));// 单位 分
		String r_url = BankingPay(bankMap);

		return PayUtil.returnWYPayJson("success", "link", r_url, pay_url, "");
	}

	@Override
	public net.sf.json.JSONObject smPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String userName = payEntity.getUsername();
		String pay_url = payEntity.getPayUrl();
		String mobile = payEntity.getMobile();
		String ip = payEntity.getIp();

		Map<String, String> scanMap = new HashMap<String, String>();
		scanMap.put("tranCode", pay_code);
		scanMap.put("ipAddress", ip);// 本地ip地址
		scanMap.put("requestId", order_no);// 用户请求的交易流水号唯一
		int int_amount = (int) (amount * 100);
		scanMap.put("orderAmount", String.valueOf(int_amount));// 单位 分
		String r_url = scanPay(scanMap);
		if (StringUtils.isNullOrEmpty(r_url)) {
			logger.error("接口异常 获取二维码失败");
			return PayUtil.returnPayJson("error", "2", "支付接口请求失败!", userName, amount, order_no, "");
		}

		return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no, r_url);
	}

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }
}
