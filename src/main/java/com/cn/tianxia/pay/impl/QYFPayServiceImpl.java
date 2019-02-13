package com.cn.tianxia.pay.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.qyf.util.ToolKit;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class QYFPayServiceImpl implements PayService {

	private final static Logger logger = LoggerFactory.getLogger(QYFPayServiceImpl.class);
	// 版本号，固定值：V3.0.0.0
	private static String version;
	// 商户号
	private static String merNo;
	// 商品名称
	private static String goodsName;
	// 支付结果通知地址
	private static String callBackUrl;
	// 回显地址
	private static String callBackViewUrl;
	// 客户端系统编码格式，UTF-8，GBK
	private static String charset;
	// 支付宝二维码请求地址
	private static String zfbserverUrl;
	// QQ钱包二维码请求地址
	private static String qqserverUrl;
	// 微信二维码请求地址
	private static String wxserverUrl;
	// key值
	private static String key;
	// 公钥
	private static String pay_public_key;
	// 密钥
	private static String private_key;
	// 支付宝二维码请求地址
	private static String zfbwapserverUrl;
	// qq二维码请求地址
	private static String qqwapserverUrl;
	// qq二维码请求地址
	private static String wxwapserverUrl;
	// qq二维码请求地址
	private static String ylserverUrl;

	static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public QYFPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = new JSONObject().fromObject(pmap);
		if (null != pmap) {
			version = jo.get("version").toString();
			merNo = jo.get("merNo").toString();
			goodsName = jo.get("goodsName").toString();
			callBackViewUrl = jo.get("callBackViewUrl").toString();
			charset = jo.get("charset").toString();
			callBackUrl = jo.get("callBackUrl").toString();
			zfbserverUrl = jo.get("zfbserverUrl").toString();
			qqserverUrl = jo.get("qqserverUrl").toString();
			wxserverUrl = jo.get("wxserverUrl").toString();
			key = jo.get("key").toString();
			pay_public_key = jo.get("pay_public_key").toString();
			private_key = jo.get("private_key").toString();
			boolean aa = jo.get("zfbwapserverUrl") == null ? false : true;
			if (aa) {
				zfbwapserverUrl = jo.get("zfbwapserverUrl").toString();
			}
			boolean bb = jo.get("qqwapserverUrl") == null ? false : true;
			if (bb) {
				qqwapserverUrl = jo.get("qqwapserverUrl").toString();
			}
			boolean cc = jo.get("qqwapserverUrl") == null ? false : true;
			if (cc) {
				wxwapserverUrl = jo.get("wxwapserverUrl").toString();
			}
			boolean dd = jo.get("ylserverUrl") == null ? false : true;
			if (dd) {
				ylserverUrl = jo.get("ylserverUrl").toString();
			}
		}
	}

	public String scanPay(Map<String, String> scanMap) {
		// TODO Auto-generated method stub
		String amount = scanMap.get("amount");
		String netway = scanMap.get("netway");
		String orderNum = scanMap.get("orderNum");
		String mobile = scanMap.get("mobile");
		Map<String, String> metaSignMap = new TreeMap<String, String>();
		metaSignMap.put("orderNum", orderNum);
		metaSignMap.put("version", version);
		metaSignMap.put("charset", charset);//
		metaSignMap.put("random", ToolKit.randomStr(4));// 4位随机数
		metaSignMap.put("merNo", merNo);
		if (mobile != null) {
			if (netway.equals("ZFB"))
				metaSignMap.put("netway", "ZFB_WAP");
			if (netway.equals("QQ"))
				metaSignMap.put("netway", "QQ_WAP");
			if (netway.equals("WX"))
				metaSignMap.put("netway", "WX_WAP");
			if (netway.equals("UNION_WALLET"))
				metaSignMap.put("netway", "UNION_WALLET");
		} else {
			metaSignMap.put("netway", netway);// WX:微信支付,ZFB:支付宝支付
		}
		metaSignMap.put("amount", amount);// 单位:分
		metaSignMap.put("goodsName", goodsName);// 商品名称：20位
		metaSignMap.put("callBackUrl", callBackUrl);// 回调地址
		metaSignMap.put("callBackViewUrl", callBackViewUrl);// 回显地址
		String metaSignJsonStr = ToolKit.mapToJson(metaSignMap);
		String sign = ToolKit.MD5(metaSignJsonStr + key, charset);// 32位
		logger.info("sign=" + sign);
		metaSignMap.put("sign", sign);
		try {
			byte[] dataStr = ToolKit.encryptByPublicKey(ToolKit.mapToJson(metaSignMap).getBytes(charset),
					pay_public_key);
			String param = new BASE64Encoder().encode(dataStr);
			String reqParam = "data=" + URLEncoder.encode(param, charset) + "&merchNo=" + metaSignMap.get("merNo")
					+ "&version=" + metaSignMap.get("version");
			String resultJsonStr = "";
			if ("WX".equals(metaSignMap.get("netway"))) {
				resultJsonStr = ToolKit.request(wxserverUrl, reqParam);
			} else if ("ZFB".equals(metaSignMap.get("netway"))) {
				resultJsonStr = ToolKit.request(zfbserverUrl, reqParam);
			} else if ("QQ".equals(metaSignMap.get("netway"))) {
				resultJsonStr = ToolKit.request(qqserverUrl, reqParam);
			} else if ("ZFB_WAP".equals(metaSignMap.get("netway"))) {// "http://zfbwap.qyfpay.com:90/api/pay.action"
				resultJsonStr = ToolKit.request(zfbwapserverUrl, reqParam);
			} else if ("QQ_WAP".equals(metaSignMap.get("netway"))) {// "http://zfbwap.qyfpay.com:90/api/pay.action"
				resultJsonStr = ToolKit.request(qqwapserverUrl, reqParam);
			} else if ("WX_WAP".equals(metaSignMap.get("netway"))) {// "http://zfbwap.qyfpay.com:90/api/pay.action"
				resultJsonStr = ToolKit.request(wxwapserverUrl, reqParam);
			} else if ("UNION_WALLET".equals(metaSignMap.get("netway"))) {
				resultJsonStr = ToolKit.request(ylserverUrl, reqParam);
			}
			// 检查状态
			JSONObject resultJsonObj = JSONObject.fromObject(resultJsonStr);
			String stateCode = resultJsonObj.getString("stateCode");
			if (!stateCode.equals("00")) {
				return "";
			}
			String resultSign = resultJsonObj.getString("sign");
			resultJsonObj.remove("sign");
			String targetString = ToolKit.MD5(resultJsonObj.toString() + key, charset);
			if (targetString.equals(resultSign)) {
				logger.info("请求轻易付Sign签名校验成功");
				return resultJsonObj.getString("qrcodeUrl");
			}
			return "";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * formatString() : 字符串格式化方法
	 */
	public static String formatString(String text) {
		return (text == null) ? "" : text.trim();
	}

	// 获取随机数
	public static String getRandomString(int length) { // length表示生成字符串的长度
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	public static String mapToJson(Map<String, String> map) {
		Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
		StringBuffer json = new StringBuffer();
		json.append("{");
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			json.append("\"").append(key).append("\"");
			json.append(":");
			json.append("\"").append(value).append("\"");
			if (it.hasNext()) {
				json.append(",");
			}
		}
		json.append("}");
		logger.info("mapToJson=" + json.toString());
		return json.toString();
	}

	public final static String MD5(String s, String encoding) {
		try {
			byte[] btInput = s.getBytes(encoding);
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = HEX_DIGITS[byte0 >>> 4 & 0xf];
				str[k++] = HEX_DIGITS[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String request(String url, String params) {
		try {
			logger.info("参数:" + params);
			URL urlObj = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setConnectTimeout(1000 * 5);
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", String.valueOf(params.length()));
			OutputStream outStream = conn.getOutputStream();
			outStream.write(params.toString().getBytes("UTF-8"));
			outStream.flush();
			outStream.close();
			return getResponseBodyAsString(conn.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String getResponseBodyAsString(InputStream in) {
		try {
			BufferedInputStream buf = new BufferedInputStream(in);
			byte[] buffer = new byte[1024];
			StringBuffer data = new StringBuffer();
			int readDataLen;
			while ((readDataLen = buf.read(buffer)) != -1) {
				data.append(new String(buffer, 0, readDataLen, "UTF-8"));
			}
			logger.info("返回参数=" + data);
			return data.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	// /**
	// * 支付方法
	// */
	// public static void pay() throws Exception {
	// String merNo = "QYF201705200001";// 商户号
	// String key = "CC279B16613BD32DD7BA2965CC2BC66A";// 签名MD5密钥,24位
	// String reqUrl = "http://139.199.195.194:8080/api/pay.action";
	// Map<String, String> metaSignMap = new TreeMap<String, String>();
	// String orderNum = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new
	// Date()); // 20位
	// orderNum += ToolKit.randomStr(3);
	// metaSignMap.put("orderNum", orderNum);
	// metaSignMap.put("version", "V3.0.0.0");
	// metaSignMap.put("charset", charset);//
	// metaSignMap.put("random", ToolKit.randomStr(4));// 4位随机数
	//
	// metaSignMap.put("merNo", merNo);
	// metaSignMap.put("netway", "WX");// WX:微信支付,ZFB:支付宝支付
	// metaSignMap.put("amount", "500");// 单位:分
	// metaSignMap.put("goodsName", "笔");// 商品名称：20位
	// metaSignMap.put("callBackUrl", "http://127.0.0.1/");// 回调地址
	// metaSignMap.put("callBackViewUrl", "http://localhost/view");// 回显地址
	//
	// String metaSignJsonStr = ToolKit.mapToJson(metaSignMap);
	// String sign = ToolKit.MD5(metaSignJsonStr + key, charset);// 32位
	// System.out.println("sign=" + sign); // 英文字母大写
	// metaSignMap.put("sign", sign);
	//
	// byte[] dataStr =
	// ToolKit.encryptByPublicKey(ToolKit.mapToJson(metaSignMap).getBytes(charset),pay_public_key);
	// String param = new BASE64Encoder().encode(dataStr);
	// String reqParam = "data=" + URLEncoder.encode(param, charset) +
	// "&merchNo=" + metaSignMap.get("merNo")
	// + "&version=" + metaSignMap.get("version");
	// String resultJsonStr = ToolKit.request(reqUrl, reqParam);
	// // 检查状态
	// JSONObject resultJsonObj = JSONObject.fromObject(resultJsonStr);
	// String stateCode = resultJsonObj.getString("stateCode");
	// if (!stateCode.equals("00")) {
	// return;
	// }
	// String resultSign = resultJsonObj.getString("sign");
	// resultJsonObj.remove("sign");
	// String targetString = ToolKit.MD5(resultJsonObj.toString() + key,
	// charset);
	// if (targetString.equals(resultSign)) {
	// System.out.println("签名校验成功");
	// }
	// }

	public boolean callback(HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		try {
			String data = request.getParameter("data");
			byte[] result = ToolKit.decryptByPrivateKey(new BASE64Decoder().decodeBuffer(data), private_key);
			String resultData = new String(result, charset);// 解密数据

			JSONObject jsonObj = JSONObject.fromObject(resultData);
			Map<String, String> metaSignMap = new TreeMap<String, String>();
			metaSignMap.put("merNo", jsonObj.getString("merNo"));
			metaSignMap.put("netway", jsonObj.getString("netway"));
			metaSignMap.put("orderNum", jsonObj.getString("orderNum"));
			metaSignMap.put("amount", jsonObj.getString("amount"));
			metaSignMap.put("goodsName", jsonObj.getString("goodsName"));
			metaSignMap.put("payResult", jsonObj.getString("payResult"));// 支付状态
			metaSignMap.put("payDate", jsonObj.getString("payDate"));// yyyyMMddHHmmss
			String jsonStr = ToolKit.mapToJson(metaSignMap);
			String sign = ToolKit.MD5(jsonStr.toString() + key, charset);
			if (!sign.equals(jsonObj.getString("sign")) && !"00".equals(jsonObj.getString("payResult"))) {
				return false;
			}
			logger.info("轻易付回调签名校验成功");
			response.getOutputStream().write("0".getBytes());
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		return null;
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
		scanMap.put("amount", String.valueOf(int_amount));// 订单明细金额
		scanMap.put("orderNum", order_no);// 订单号，必须是yyyyMMdd开头（例如：2017041200001）
		scanMap.put("mobile", mobile);
		scanMap.put("netway", pay_code);
		String html = scanPay(scanMap);
		if (StringUtils.isNullOrEmpty(html)) {
			return PayUtil.returnPayJson("error", "1", "支付接口请求失败!", userName, amount, order_no, "");
		}
		if (StringUtils.isNullOrEmpty(mobile)) {
			return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no, html);
		} else {
			return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no, html);
		}
	}

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }
}
