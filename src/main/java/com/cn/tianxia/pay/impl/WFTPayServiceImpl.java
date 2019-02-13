package com.cn.tianxia.pay.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.wft.util.MD5;
import com.cn.tianxia.pay.wft.util.SignUtils;
import com.cn.tianxia.pay.wft.util.XmlUtils;

import net.sf.json.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;

public class WFTPayServiceImpl {
	private final static Logger logger = LoggerFactory.getLogger(WFTPayServiceImpl.class);

	private String key;// md5key
	private String req_url;// 扫码支付地址

	private String charset;// 字符集编码
	private String body;// 商品名称
	private String mch_id;// 商品名称
	private String notify_url;// 服务器通知地址
	private String sign_type;// 加密方式
	private String version;// 接口版本 默认2.0

	public WFTPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = new JSONObject().fromObject(pmap);
		if (null != pmap) {
			key = jo.getString("key");
			req_url = jo.getString("req_url");
			charset = jo.getString("charset");
			body = jo.getString("body");// 商品名称
			mch_id = jo.getString("mch_id");// 商品名称
			notify_url = jo.getString("notify_url");// 服务器通知地址
			sign_type = jo.getString("sign_type");// 加密方式
			version = jo.getString("version");// 接口版本 默认2.0
		}
	}

	/**
	 * pc扫码接口
	 * 
	 * @param reqMap
	 * @return
	 */
	public JSONObject ScanPay(Map<String, String> reqMap) {
		String service = reqMap.get("service");
		// String attach = "附加信息";
		String mch_create_ip = reqMap.get("mch_create_ip");
		String nonce_str = System.currentTimeMillis() + "";
		// String notify_url =
		// "http://zhangwei.dev.swiftpass.cn/native-pay/testPayResult";
		String out_trade_no = reqMap.get("out_trade_no");
		String total_fee = reqMap.get("total_fee");
		// String version = "2.0";

		SortedMap<String, String> map = new TreeMap<String, String>();
		map.put("service", service);
		// map.put("attach", attach);
		map.put("body", body);
		map.put("charset", charset);
		map.put("mch_create_ip", mch_create_ip);
		map.put("mch_id", mch_id);
		map.put("nonce_str", nonce_str);
		map.put("notify_url", notify_url);
		map.put("out_trade_no", out_trade_no);
		map.put("sign_type", sign_type);
		map.put("total_fee", total_fee);
		map.put("version", version);

		Map<String, String> params = SignUtils.paraFilter(map);
		StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
		SignUtils.buildPayParams(buf, params, false);
		String preStr = buf.toString();
		String sign = MD5.sign(preStr, "&key=" + key, "utf-8");
		map.put("sign", sign);

		logger.info("reqParams:" + XmlUtils.parseXML(map));

		CloseableHttpResponse response = null;
		CloseableHttpClient client = null;
		String res = null;
		String code_img_url = "";
		try {
			HttpPost httpPost = new HttpPost(req_url);
			StringEntity entityParams = new StringEntity(XmlUtils.parseXML(map), "utf-8");
			httpPost.setEntity(entityParams);
			client = HttpClients.createDefault();
			response = client.execute(httpPost);
			if (response != null && response.getEntity() != null) {
				Map<String, String> resultMap = XmlUtils.toMap(EntityUtils.toByteArray(response.getEntity()), "utf-8");
				res = XmlUtils.toXml(resultMap);
				logger.info("请求结果：" + res);

				if (resultMap.containsKey("sign")) {
					if (!SignUtils.checkParam(resultMap, key)) {
						logger.info("验证签名不通过");
						return getReturnJson("error", "", res);
					} else {
						if ("0".equals(resultMap.get("status")) && "0".equals(resultMap.get("result_code"))) {
							code_img_url = resultMap.get("code_img_url");
							logger.info("code_img_url:" + code_img_url);
							return getReturnJson("success", code_img_url, resultMap.get("message"));
						} else {
							logger.info("result:" + res);
							return getReturnJson("error", "", res);
						}
					}
				}
			} else {
				logger.info("操作失败");
				return getReturnJson("error", "", "操作失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("系统异常");
			return getReturnJson("error", "", "系统异常01");
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.info("系统异常");
					return getReturnJson("error", "", "系统异常02");
				}
			}
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.info("系统异常");
					return getReturnJson("error", "", "系统异常03");
				}
			}
		}
		return getReturnJson("error", "", "获取二维码图片连接失败！");
	}

	/**
	 * mb扫码接口
	 * 
	 * @param reqMap
	 * @return
	 */
	public JSONObject mbScanPay(Map<String, String> reqMap) {
		String service = reqMap.get("service");
		// String attach = "附加信息";
		String mch_create_ip = reqMap.get("mch_create_ip");
		String nonce_str = System.currentTimeMillis() + "";
		// String notify_url =
		// "http://zhangwei.dev.swiftpass.cn/native-pay/testPayResult";
		String out_trade_no = reqMap.get("out_trade_no");
		String total_fee = reqMap.get("total_fee");
		// String version = "2.0";

		SortedMap<String, String> map = new TreeMap<String, String>();
		map.put("service", service);
		map.put("callback_url", reqMap.get("callback_url"));
		map.put("version", version);
		map.put("charset", charset);
		map.put("sign_type", sign_type);
		map.put("mch_id", mch_id);
		map.put("out_trade_no", out_trade_no);
		map.put("body", body);
		map.put("total_fee", total_fee);
		map.put("mch_create_ip", mch_create_ip);
		map.put("nonce_str", nonce_str);
		map.put("notify_url", notify_url);
		// String mobile_type = reqMap.get("mobile_type");
		/** 微信wap service=pay.weixin.wappay & device_info=AND_WAP **/
		if ("pay.weixin.wappay".equals(service)) {
			map.put("device_info", "AND_WAP");
			map.put("mch_app_name", "王者荣耀");
			map.put("mch_app_id", "com.tencent.tmgp.sgame");
		}

		Map<String, String> params = SignUtils.paraFilter(map);
		StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
		SignUtils.buildPayParams(buf, params, false);
		String preStr = buf.toString();
		String sign = MD5.sign(preStr, "&key=" + key, "utf-8");
		map.put("sign", sign);

		logger.info("reqParams:" + XmlUtils.parseXML(map));

		CloseableHttpResponse response = null;
		CloseableHttpClient client = null;
		String res = null;
		String pay_info = "";
		try {
			HttpPost httpPost = new HttpPost(req_url);
			StringEntity entityParams = new StringEntity(XmlUtils.parseXML(map), "utf-8");
			httpPost.setEntity(entityParams);
			client = HttpClients.createDefault();
			response = client.execute(httpPost);
			if (response != null && response.getEntity() != null) {
				Map<String, String> resultMap = XmlUtils.toMap(EntityUtils.toByteArray(response.getEntity()), "utf-8");
				res = XmlUtils.toXml(resultMap);
				logger.info("请求结果：" + res);

				if (resultMap.containsKey("sign")) {
					if (!SignUtils.checkParam(resultMap, key)) {
						logger.info("验证签名不通过");
						return getReturnJson("error", "", res);
					} else {
						if ("0".equals(resultMap.get("status")) && "0".equals(resultMap.get("result_code"))) {
							pay_info = resultMap.get("pay_info");
							logger.info("pay_info:" + pay_info);
							return getReturnJson("success", pay_info, resultMap.get("message"));
						} else {
							logger.info("result:" + res);
							return getReturnJson("error", "", res);
						}
					}
				}
			} else {
				logger.info("操作失败");
				return getReturnJson("error", "", res);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("系统异常01");
			return getReturnJson("error", "", res);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.info("系统异常02");
					return getReturnJson("error", "", "系统异常01");
				}
			}
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.info("系统异常03");
					return getReturnJson("error", "", "系统异常03");
				}
			}
		}
		return getReturnJson("error", "", "未获取到支付连接");
	}

	/**
	 * 返回数据格式Json
	 * 
	 * @param status
	 * @param qrCode
	 * @param msg
	 * @return
	 */
	public JSONObject getReturnJson(String status, String qrCode, String msg) {
		JSONObject json = new JSONObject();
		json.put("status", status);
		json.put("qrCode", qrCode);
		if (StringUtils.isNullOrEmpty(msg)) {
			json.put("msg", "");
		} else {
			json.put("msg", msg);
		}
		return json;
	}

	
	public String callback(String resString) {
		try {
			// String resString = XmlUtils.parseRequst(req);
			logger.info("通知内容：" + resString);

			String respString = "fail";
			if (resString != null && !"".equals(resString)) {
				Map<String, String> map = XmlUtils.toMap(resString.getBytes(), "utf-8");
				String res = XmlUtils.toXml(map);
				logger.info("通知内容：" + res);
				if (map.containsKey("sign")) {
					if (!SignUtils.checkParam(map, key)) {
						res = "验证签名不通过";
						respString = "fail";
					} else {
						String status = map.get("status");
						if (status != null && "0".equals(status)) {
							String result_code = map.get("result_code");
							if (result_code != null && "0".equals(result_code)) {
								logger.info("威富通支付通知成功！");
								return "success";
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("威富通支付通知失败！");
		return "";
	}

	public static void main(String[] args) {
		String req_url = "https://pay.swiftpass.cn/pay/gateway";// 扫码支付地址
		String key = "53771b3177a00590fb5a5ebc3f28adb9";
		String body = "Super bargains";
		String charset = "UTF-8";
		String mch_id = "105550063421";
		String notify_url = "http://zhangwei.dev.swiftpass.cn/native-pay/testPayResult";
		String sign_type = "MD5";
		String version = "2.0";

		Map<String, String> initMap = new HashMap<>();
		initMap.put("key", key);
		initMap.put("req_url", req_url);
		initMap.put("body", body);
		initMap.put("charset", charset);
		initMap.put("mch_id", mch_id);
		initMap.put("notify_url", notify_url);
		initMap.put("sign_type", sign_type);
		initMap.put("version", version);
		logger.info("json配置:" + JSONObject.fromObject(initMap).toString());
		WFTPayServiceImpl wft = new WFTPayServiceImpl(initMap);

		String mch_create_ip = "127.0.0.1";
		String total_fee = "1";
		String out_trade_no = "TX" + System.currentTimeMillis() + "";

		/**
		 * 支付类型1微信pay.weixin.native 2支付宝pay.alipay.native 3qq扫码
		 * pay.tenpay.native
		 **/

		/** 微信wap **/
		// String service = "pay.weixin.wappay";
		/** qq钱包 **/
		String service = "pay.tenpay.wappay";

		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("service", service);
		scanMap.put("mch_create_ip", mch_create_ip);
		scanMap.put("total_fee", total_fee);
		scanMap.put("out_trade_no", out_trade_no);
		// scanMap.put("mobile_type", "wx_wap");
		scanMap.put("callback_url", "http://localhost:8080/");

		// String qrImg = wft.ScanPay(scanMap);

		logger.info("**************获取二维码图片地址***************");
		logger.info(wft.mbScanPay(scanMap).toString());
	}

}
