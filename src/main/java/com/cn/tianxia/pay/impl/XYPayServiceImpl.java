package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.ly.util.HttpMethod;
import com.cn.tianxia.pay.ly.util.HttpSendModel;
import com.cn.tianxia.pay.ly.util.SimpleHttpResponse;
import com.cn.tianxia.pay.mkt.util.HttpUtil;
import com.cn.tianxia.pay.mkt.util.Log;
import com.cn.tianxia.pay.qyf.util.ToolKit;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * 轩辕支付（只有微信支付宝渠道）
 * 
 * @author zw
 *
 */
public class XYPayServiceImpl implements PayService {

	private String version;
	private String merId;
	private String notifyUrl; 
	private String md5_key;
	private String api_url;

	private final static Logger logger = LoggerFactory.getLogger(XYPayServiceImpl.class);

	public XYPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			version = jo.get("version").toString();
			merId = jo.get("merId").toString();
			notifyUrl = jo.get("notifyUrl").toString();
			md5_key = jo.get("md5_key").toString();
			api_url = jo.get("api_url").toString();
		}
	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		// TODO 支付商目前只有微信支付宝渠道
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String mobile = payEntity.getMobile();
		String userName = payEntity.getUsername();

		DecimalFormat df = new DecimalFormat("###########");
		String df_amount = df.format(amount);

		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("merOrderNo", order_no);
		scanMap.put("orderAmt", df_amount);
		scanMap.put("payPlat", pay_code);
		scanMap.put("callbackUrl", refereUrl);

		String r_json = scanPay(scanMap);

		return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no, r_json);
//		if ("success".equals(r_json.getString("status"))) {
//			// pc端
//			if (StringUtils.isNullOrEmpty(mobile)) {
//				// 返回二维码图片连接地址
//				return PayUtil.returnPayJson("success", "3", "支付接口请求成功!", userName, amount, order_no,
//						r_json.getString("qrCode"));
//			} else {
//				// 手机端
//				return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
//						r_json.getString("qrCode"));
//			}
//		} else {
//			return PayUtil.returnPayJson("error", "2", r_json.getString("msg"), userName, amount, order_no, "");
//		}
	}

	/**
	 * pc扫码
	 * 
	 * @param scanMap
	 * @return
	 */
	public String scanPay(Map<String, String> scanMap) {

		String merOrderNo = scanMap.get("merOrderNo");// 订单号

		String orderAmt = scanMap.get("orderAmt");// 金额

		String payPlat = scanMap.get("payPlat");// 支付平台

		String callbackUrl = scanMap.get("callbackUrl");

		String sign = "";

		Map<String, String> payMap = new HashMap<>();
		payMap.put("version", version);
		payMap.put("merId", merId);
		payMap.put("merOrderNo", merOrderNo);
		payMap.put("orderAmt", orderAmt);
		payMap.put("payPlat", payPlat);
		payMap.put("notifyUrl", notifyUrl);
		payMap.put("callbackUrl", callbackUrl);

		sign = getSign(payMap);
		payMap.put("sign", sign);

		Map<String, String> postMap = new HashMap<>();
		postMap.put("param", JSONObject.fromObject(payMap).toString());
		logger.info("轩辕支付链接：" +api_url+"?param="+postMap.get("param"));
		String url = api_url+"?param="+postMap.get("param");
		return url;
//		String res_str = RequestForm(api_url, postMap);
//		logger.info("轩辕支付响应：" + res_str);
//
//		try {
//			JSONObject resJson = JSONObject.fromObject(res_str);
//			if (resJson.containsKey("respCode") && "0000".equals(resJson.getString("respCode"))) {
//				if (resJson.containsKey("qrcode")) {
//					return getReturnJson("success", resJson.getString("qrcode"), "二维码图片地址获取成功！");
//				}
//			}
//			return getReturnJson("error", "", res_str);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return getReturnJson("error", "", res_str);
//		}
	}

	/**
	 * 结果返回
	 * 
	 * @param status
	 * @param qrCode
	 * @param msg
	 * @return
	 */
	private JSONObject getReturnJson(String status, String qrCode, String msg) {
		JSONObject json = new JSONObject();
		json.put("status", status);
		json.put("qrCode", qrCode);
		json.put("msg", msg);
		return json;
	}

	/**
	 * HTTP post 请求
	 * 
	 * @param Url
	 * @param Parms
	 * @return
	 */
	public String RequestForm(String Url, Map<String, String> Parms) {
		if (Parms.isEmpty()) {
			return "参数不能为空！";
		}
		String PostParms = "";
		int PostItemTotal = Parms.keySet().size();
		int Itemp = 0;
		for (String key : Parms.keySet()) {
			PostParms += key + "=" + Parms.get(key);
			Itemp++;
			if (Itemp < PostItemTotal) {
				PostParms += "&";
			}
		}
		Log.Write("【请求参数】：" + PostParms);
		HttpSendModel httpSendModel = new HttpSendModel(Url + "?" + PostParms);
		Log.Write("【后端请求】：" + Url + "?" + PostParms);
		httpSendModel.setMethod(HttpMethod.GET);
		SimpleHttpResponse response = null;
		try {
			response = HttpUtil.doRequest(httpSendModel, "UTF-8");
		} catch (Exception e) {
			return e.getMessage();
		}
		return response.getEntityString();
	}

	/**
	 * 验签
	 * 
	 * @param paramMap
	 * @return
	 */
	public String getSign(Map<String, String> paramMap) {
		String serviceSign = paramMap.remove("sign");
		String sign = "";

		List infoIds = new ArrayList(paramMap.entrySet());

		String buff = "";

		Collections.sort(infoIds, new Comparator<Map.Entry<String, String>>() {
			public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
				return ((String) o1.getKey()).toString().compareTo((String) o2.getKey());
			}
		});
		for (int i = 0; i < infoIds.size(); i++) {
			Map.Entry item = (Map.Entry) infoIds.get(i);

			if (item.getKey() != "") {
				String key = String.valueOf(item.getKey());
				String val = String.valueOf(item.getValue());

				if ("".equals(val)) {
					continue;
				}
				buff = buff + key + "=" + val + "&";
			}
		}
		if (!buff.isEmpty())
			buff = buff.substring(0, buff.length() - 1);

		buff += /* "&key=" + */
				md5_key;
		logger.info("轩辕支付待签名字符:" + buff.toString());
		return sign = ToolKit.MD5(buff, "UTF-8").toLowerCase();
	}

	/**
	 * 回调验签方法
	 * 
	 * @param signMap
	 * @return
	 */
	@Override
	public String callback(Map<String, String> signMap) {
		String serviceSign = signMap.remove("sign");
		String sign = "";

		sign = getSign(signMap);

		logger.info("本地签名：" + sign + "      支付商sign:" + serviceSign);
		if (sign.equals(serviceSign.toLowerCase())) {
			logger.info("签名成功");
			return "success";
		}

		logger.info("签名失败");
		return "";
	}
}
