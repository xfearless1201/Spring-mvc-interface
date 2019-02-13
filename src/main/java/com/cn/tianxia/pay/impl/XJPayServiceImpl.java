package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.xm.util.MD5s;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;

public class XJPayServiceImpl implements PayService {
	private static String pcUrl;// pc 扫码接口地址
	private static String mbUrl;// 手机端扫码接口地址
	private static String notifyUrl;// 服务器回调地址
	private static String md5Key;// md5密钥
	private static String merchno;// 商户号
	private static String goodsName;// 商品名称
	private static String remark;// 备注

	private final static Logger logger = LoggerFactory.getLogger(XJPayServiceImpl.class);

	public XJPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			pcUrl = jo.get("pcUrl").toString();
			mbUrl = jo.get("mbUrl").toString();
			notifyUrl = jo.get("notifyUrl").toString();
			md5Key = jo.get("md5Key").toString();
			merchno = jo.get("merchno").toString();
			goodsName = jo.get("goodsName").toString();
			remark = jo.get("remark").toString();
		}
	}

	/**
	 * 此方法带实现，2018年1月6日 14:43:28 支付商尚未实现网银支付
	 * 
	 * @return
	 */
	public String BankPay() {

		return "";
	}

	/**
	 * pc扫码接口
	 * 
	 * @param scanMap
	 * @return
	 */
	public JSONObject pcScanPay(Map<String, String> scanMap) {
		String amount = scanMap.get("amount");
		String traceno = scanMap.get("traceno");
		String payType = scanMap.get("payType");
		String returnUrl = scanMap.get("returnUrl");
		String signature = "";

		Map<String, String> paramsMap = new TreeMap<>();
		paramsMap.put("merchno", merchno);
		paramsMap.put("amount", amount);
		paramsMap.put("traceno", traceno);
		paramsMap.put("payType", payType);
		if (!StringUtils.isNullOrEmpty(remark)) {
			paramsMap.put("remark", remark);
		}
		if (!StringUtils.isNullOrEmpty(remark)) {
			paramsMap.put("goodsName", goodsName);
		}
		paramsMap.put("notifyUrl", notifyUrl);
		paramsMap.put("returnUrl", returnUrl);

		try {
			signature = FormatBizQueryParaMap(paramsMap) + "&" + md5Key;
			logger.info("迅捷支付待签名字符串:" + signature);
			signature = MD5s.MD5(signature);
		} catch (Exception e) {
			logger.info("迅捷支付签名异常！");
			e.printStackTrace();
		}
		paramsMap.put("signature", signature);

		String returnStr = RequestForm(pcUrl, paramsMap);
		logger.info("迅捷支付响应:" + returnStr);
		JSONObject json = null;
		try {
			json = JSONObject.fromObject(returnStr);
		} catch (Exception e) {
			logger.info("迅捷支付响应转换json格式异常信息如下:" + returnStr);
			e.printStackTrace();
			return json;
		}
		return json;
	}

	/**
	 * 手机扫码接口
	 * 
	 * @param scanMap
	 * @return
	 */
	public JSONObject mbScanPay(Map<String, String> scanMap) {
		String amount = scanMap.get("amount");
		String traceno = scanMap.get("traceno");
		String payType = scanMap.get("payType");
		String returnUrl = scanMap.get("returnUrl");
		String signature = "";
		Map<String, String> paramsMap = new TreeMap<>();
		paramsMap.put("merchno", merchno);
		paramsMap.put("amount", amount);
		paramsMap.put("traceno", traceno);
		paramsMap.put("payType", payType);
		if (!StringUtils.isNullOrEmpty(remark)) {
			paramsMap.put("remark", remark);
		}
		if (!StringUtils.isNullOrEmpty(remark)) {
			paramsMap.put("goodsName", goodsName);
		}
		paramsMap.put("notifyUrl", notifyUrl);
		paramsMap.put("returnUrl", returnUrl);
		try {
			signature = FormatBizQueryParaMap(paramsMap) + "&" + md5Key;
			logger.info("迅捷支付待签名字符串:" + signature);
			signature = MD5s.MD5(signature);
		} catch (Exception e) {
			logger.info("迅捷支付签名异常！");
			e.printStackTrace();
		}
		paramsMap.put("signature", signature);
		String returnStr = RequestForm(mbUrl, paramsMap);
		logger.info("迅捷支付响应:" + returnStr);
		JSONObject json = null;
		try {
			json = JSONObject.fromObject(returnStr);
		} catch (Exception e) {
			logger.info("迅捷支付响应转换json格式异常信息如下:" + returnStr);
			e.printStackTrace();
			return json;
		}
		return json;
	}

	/**
	 * 回调验证
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public String callback(Map<String, String> map) {
		String signature = "";
		String serviceSign = map.remove("signature");
		try {
			signature = FormatBizQueryParaMap(map) + "&" + md5Key;
			logger.info("迅捷支付待签名字符串:" + signature);
			signature = MD5s.MD5(signature);
			logger.info("本地sign" + signature + "        服务器sign" + serviceSign);
			if (serviceSign.equals(signature)) {
				logger.info("迅捷支签名成功！" + signature);
				return "success";
			}
		} catch (Exception e) {
			logger.info("迅捷支付回调签名异常！");
			e.printStackTrace();
			return "";
		}
		return "";
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
		httpSendModel.setMethod(HttpMethod.POST);
		SimpleHttpResponse response = null;
		try {
			response = HttpUtil.doRequest(httpSendModel, "GBK");
		} catch (Exception e) {
			return e.getMessage();
		}
		return response.getEntityString();
	}

	/**
	 * 参数排序
	 * 
	 * @param paraMap
	 * @param urlencode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String FormatBizQueryParaMap(Map<String, String> paraMap) throws Exception {
		String buff = "";
		try {
			List infoIds = new ArrayList(paraMap.entrySet());

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
					// if (urlencode) {
					// val = URLEncoder.encode(val, "utf-8");
					// }
					if ("".equals(val)) {
						continue;
					}
					buff = buff + key + "=" + val + "&";
				}
			}
			if (!buff.isEmpty())
				buff = buff.substring(0, buff.length() - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buff;
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

		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("traceno", order_no);// 订单号
		scanMap.put("amount", String.valueOf(amount));// 金额
		scanMap.put("returnUrl", refereUrl);
		scanMap.put("payType", pay_code);
		JSONObject xj_json = null;
		boolean mobileFalg = false;
		if (StringUtils.isNullOrEmpty(mobile)) {
			// pc端
			xj_json = pcScanPay(scanMap);
		} else {
			// 手机端
			mobileFalg = true;
			xj_json = mbScanPay(scanMap);
		}
		String qrcode = "";
		// 状态00表示获取请求成功
		if ("00".equals(xj_json.getString("respCode"))) {
			qrcode = xj_json.getString("barCode");
		} else {
			// 返回错误信息
			return PayUtil.returnPayJson("error", "4", xj_json.toString(), userName, amount, order_no, "");
		}
		// 区别pc和手机端返回类型
		if (mobileFalg) {
			// 手机端返回
			return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no, qrcode);
		} else {
			// pc端返回
			return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no, qrcode);
		}
	}
}
