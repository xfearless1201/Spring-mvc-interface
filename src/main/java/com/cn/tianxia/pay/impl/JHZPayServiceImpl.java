package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.jhz.util.RSASignature;
import com.cn.tianxia.pay.jhz.util.RequestUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;

public class JHZPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(JHZPayServiceImpl.class);

	private static String privateKey;// 私钥
	private static String publicKey;// 服务器公钥
	private static String merchantNo;// 商户号
	// private static String pageUrl;// 页面返回URL
	private static String backUrl; // 服务器返回URL
	private static String agencyCode;// 分支机构号
	private static String remark1;// 商品描述
	private static String remark2;// 备用字段2，此字段参与验签，不返回
	private static String remark3;// 备用字段3，此字段参与验签，不返回
	private static String url;// 请求地址

	public JHZPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = new JSONObject().fromObject(pmap);
		if (null != pmap) {
			privateKey = jo.get("privateKey").toString();
			publicKey = jo.get("publicKey").toString();
			merchantNo = jo.get("merchantNo").toString();
			backUrl = jo.get("backUrl").toString();
			agencyCode = jo.get("agencyCode").toString();
			remark1 = jo.get("remark1").toString();
			remark2 = jo.get("remark2").toString();
			remark3 = jo.get("remark3").toString();
			url = jo.get("url").toString();
		}
	}

	/**
	 * 网银支付
	 * 
	 * @param scanMap
	 * @return
	 */
	public String bankPay(Map<String, String> bankMap) {
		String requestNo = bankMap.get("requestNo");// 请求流水
		String amount = bankMap.get("amount");// 金额
		// 产品类型 网银支付 6002  微信h5 6005  QQ扫码 6011 QQH5 6016
		String payMethod = bankMap.get("payMethod");
		String payDate = "" + System.currentTimeMillis();// 时间戳
		String signature = "";// 签名
		String pageUrl = bankMap.get("pageUrl");

		// 按参数固定格式排序后的值用“|”线连接起来，然后用rsa签名的值
		String content = merchantNo + "|" + requestNo + "|" + amount + "|" + pageUrl + "|" + backUrl + "|" + payDate
				+ "|" + agencyCode + "|" + remark1 + "|" + remark2 + "|" + remark3;// 拼接数据
		logger.info("sign格式化：" + content);
		signature = URLEncoder.encode(RSASignature.sign(content, privateKey));

		Map<String, String> m = new HashMap();
		m.put("merchantNo", merchantNo);
		m.put("requestNo", requestNo);
		m.put("amount", amount);
		m.put("payMethod", payMethod);
		m.put("pageUrl", pageUrl);// 页面返回地址
		m.put("backUrl", backUrl);
		m.put("payDate", payDate);
		m.put("agencyCode", agencyCode);
		m.put("remark1", remark1);
		m.put("remark2", remark2);
		m.put("remark3", remark3);
		m.put("signature", signature);

		if ("6002".equals(payMethod)) {
			String cur = "CNY";// 币种
			// TODO银行代码
			String bankType = bankMap.get("bankType");// 银行行别
			// TODO付款方银行账户类型 B2c 个人网银支付
			String bankAccountType = "11";
			String timeout = "";// 订单有效时间
			m.put("cur", cur);
			m.put("bankType", bankType);
			m.put("bankAccountType", bankAccountType);
			m.put("timeout", timeout);
		}

		StringBuffer paramstr = new StringBuffer();
		for (Map.Entry<String, String> entry : m.entrySet()) {

			paramstr.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		// 请求
		try {
			String responseData = RequestUtil.doPostStr(url, paramstr.toString());
			logger.info("金海哲支付网银响应:" + responseData);
			return responseData;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

	}

	/**
	 * 扫码支付
	 * 
	 * @param scanMap
	 * @return
	 */
	public String ScanPay(Map<String, String> scanMap) {
		String requestNo = scanMap.get("requestNo");// 请求流水
		String amount = scanMap.get("amount");// 金额
		// 产品类型 网银支付 6002  微信h5 6005  QQ扫码 6011 QQH5 6016
		String payMethod = scanMap.get("payMethod");
		String payDate = "" + System.currentTimeMillis();// 时间戳
		String signature = "";// 签名
		String pageUrl = scanMap.get("pageUrl");

		// 按参数固定格式排序后的值用“|”线连接起来，然后用rsa签名的值
		String content = merchantNo + "|" + requestNo + "|" + amount + "|" + pageUrl + "|" + backUrl + "|" + payDate
				+ "|" + agencyCode + "|" + remark1 + "|" + remark2 + "|" + remark3;// 拼接数据
		logger.info("sign格式化：" + content);
		signature = URLEncoder.encode(RSASignature.sign(content, privateKey));

		Map<String, String> m = new HashMap();
		m.put("merchantNo", merchantNo);
		m.put("requestNo", requestNo);
		m.put("amount", amount);
		m.put("payMethod", payMethod);
		m.put("pageUrl", pageUrl);
		m.put("backUrl", backUrl);
		m.put("payDate", payDate);
		m.put("agencyCode", agencyCode);
		m.put("remark1", remark1);
		m.put("remark2", remark2);
		m.put("remark3", remark3);
		m.put("signature", signature);

		StringBuffer paramstr = new StringBuffer();
		for (Map.Entry<String, String> entry : m.entrySet()) {

			paramstr.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		// 请求
		String responseData = RequestUtil.doPostStr(url, paramstr.toString());
		logger.info("金海哲支付响应:" + responseData);

		try {
			JSONObject json = JSONObject.fromObject(responseData);
			String sign = json.getString("sign");
			// 去掉sign 再进行验签
			json.remove("sign");
			String result = json.toString().trim();
			// 返回的二维码
			String qrurl = json.getString("backQrCodeUrl");
			logger.info("返回二维码:" + qrurl);
			return qrurl;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 回调
	 * 
	 * @param request
	 * @return
	 */
	public String callback(HttpServletRequest request) {
		String msg = request.getParameter("msg").toString();
		String ret = request.getParameter("ret").toString();
		String sign = request.getParameter("sign").toString();
		// 公钥
		// String shPublicKey = "";
		String content = ret + "|" + msg;
		System.out.println(msg + "|" + ret + "|" + sign);
		boolean boo = false;
		try {
			boo = RSASignature.doCheck(content, sign, publicKey);
			logger.info("金海哲验证状态:" + boo);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("金海哲回调异常");
			return "";
		}

		if (boo) {
			logger.info("金海哲验证状态SUCCESS");
			return "SUCCESS";
		}
		return "";
	}

	public static String getParamSrc(Map<String, String> paramsMap) {
		StringBuffer paramstr = new StringBuffer();
		TreeMap<String, Object> sortMap = new TreeMap();
		sortMap.putAll(paramsMap);
		for (String pkey : sortMap.keySet()) {
			Object pvalue = sortMap.get(pkey);

			if (null != pvalue && "" != pvalue && !pkey.equals("sign")) {// 空值不传递，不签名

				paramstr.append(pkey + "=" + pvalue + "&"); // 签名原串，不url编码
			}
		}
		// 去掉最后一个&
		String result = paramstr.substring(0, paramstr.length() - 1);
		// 原串转码
		try {
			result = new String(result.getBytes("utf-8"), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void main(String[] args) {
		String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIJAZbKz/fhI6/CiHXYirAx8q7Zz594VyBEklopQU+8jKq/7xWvQSeTEdgcpqqkX9CFyMgK2pVFQ8l6FklYrylRtixzNzlSfR28q39RhfmjSZPTy+1LU4tow99GOpxkHl/64vaphJJRp8AZt1WGtCAjG5ogIbfRKSPX/15tvoN6xAgMBAAECgYAh6vcQpAJcp1CZZv3ZB4pZ7hUg++nDUVS58hcpiWVx/mxdS2jLfH/hjK9XiiggxFYUZ5JSVpGSLUwQBSRoQBr9onW5+aw06+d9rLQ1RL4CI1Yn0xsQOsjsKFA3uikaL0MH6IsVoA9U5LFyjn+vxETSCOahPynPB3BIrHYH3tCD6QJBAPGexBTMtZDivt5jfgH8Hu4MhNk3ziMxXo3Dgm8TPF4K33cq7TUGXypAUR9HGDjPPy7SVHEf8NlwJs73PRrO6FcCQQCKANxgeFtm4/1tyeu8EsqETquLRl0I617/c8p7hMvTgTGu6Z6sieXdhHUx9gbpfNNZPxnaZsqhP8WMo7N98iw3AkBAP9qeOU0yqMjedBBm0Lccz+FnrYo4G4GsKsf9Z10PGM8Rbi3Dgt09a/rmWU5clOeVOMLRjg/KkiMPt/jcrxZjAkBtYdtK13rS5biUkaw4SMid7+EGJkJNdIm6fiOqnYoU3Vzs35z18uLwHjHp5LB71oWKQnqSYlEPWAWTwJjfJdknAkEAzs4lsVc3skYpVETvosDwTq74JDcuaetDunHe6V7M6fMAiIu+zcfQGf5763PuWI6zKmFQ9wdjSRadu6s/6Cx3jw==";
		String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCL4nMv6qK7Lt1MzfK20LrVd/0g0pXIvV281sT16s4xIWEg/Hfv0su0MHdbTobZfHcziyO/xdmItCzkcJOIIskuC3QukNrWnt7kf1wZ1OmIMWAcS5s9wnMd0QcpDpcyfZfJvlZgFDtgJtApXvCBBVIEX65W1FnmlZ7wccO3Ca+J8QIDAQAB";
		String merchantNo = "500005541963";// 商户号
		String pageUrl = "http://www.baidu.com";// 页面返回URL
		String backUrl = "182.16.110.186:8080/XPJ/PlatformPay/JHZNotify.do"; // 服务器返回URL
		String agencyCode = "";// 分支机构号
		String remark1 = "";// 商品描述
		String remark2 = "";// 备用字段2，此字段参与验签，不返回
		String remark3 = "";// 备用字段3，此字段参与验签，不返回
		String url = "http://zf.szjhzxxkj.com/ownPay/pay";// 请求地址

		Map<String, String> m = new HashMap();
		m.put("merchantNo", merchantNo);
		m.put("privateKey", privateKey);
		m.put("publicKey", publicKey);
		m.put("pageUrl", pageUrl);
		m.put("backUrl", backUrl);
		m.put("agencyCode", agencyCode);
		m.put("remark1", remark1);
		m.put("remark2", remark2);
		m.put("remark3", remark3);
		m.put("url", url);

		logger.info("Json 配置:" + JSONObject.fromObject(m).toString());

		JHZPayServiceImpl jhz = new JHZPayServiceImpl(m);

		String requestNo = "TX" + System.currentTimeMillis();// 请求流水
		String amount = "1";// 金额
		// 产品类型 网银支付 6002  微信h5 6005  QQ扫码 6011 QQH5 6016
		String payMethod = "6002";
		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("requestNo", requestNo);
		scanMap.put("amount", amount);
		scanMap.put("payMethod", payMethod);
		String bankType = "1021000";
		scanMap.put("bankType", bankType);
		// String msg = jhz.ScanPay(scanMap);
		String msg = jhz.bankPay(scanMap);
		if (StringUtils.isNullOrEmpty(msg)) {
			logger.info("异常");
		} else {
			logger.info(msg);
		}

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
		// 订单明细金额 分为单位
		bankMap.put("pageUrl", refereUrl);
		bankMap.put("amount", String.valueOf(int_amount));
		bankMap.put("requestNo", order_no);// 订单号
		bankMap.put("payMethod", "6002");// 网银类型支付
		bankMap.put("bankType", pay_code);// 银行编码
		String html = bankPay(bankMap);
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

		Map<String, String> scanMap = new HashMap<String, String>();
		int int_amount = (int) (amount * 100);
		// 订单明细金额 分为单位
		scanMap.put("pageUrl", refereUrl);// 页面返回地址
		scanMap.put("amount", String.valueOf(int_amount));
		scanMap.put("requestNo", order_no);// 订单号
		scanMap.put("payMethod", pay_code);// 网银类型支付
		String qrcode = ScanPay(scanMap);
		if (StringUtils.isNullOrEmpty(qrcode)) {
			return PayUtil.returnPayJson("error", "2", "支付接口获取二维码失败！", userName, amount, order_no, "");
		}
		return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no, qrcode);
	}

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }
}
