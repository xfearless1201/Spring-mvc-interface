package com.cn.tianxia.pay.impl;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import com.cn.tianxia.pay.ys.util.DateUtil;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;

public class LYPayServiceImpl implements PayService {
	// md5key
	private static String md5_key;
	// 支付网关地址
	private static String api_url;
	// 商户号
	private static String mch_id;
	// 商品描述
	private static String body;
	// 附加内容
	private static String attach;
	// 异步通知地址
	private static String notify_url;

	private final static Logger logger = LoggerFactory.getLogger(LYPayServiceImpl.class);

	public LYPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			md5_key = jo.get("md5_key").toString();
			api_url = jo.get("api_url").toString();
			mch_id = jo.get("mch_id").toString();
			body = jo.get("body").toString();
			attach = jo.get("attach").toString();
			notify_url = jo.get("notify_url").toString();
		}
	}

	/**
	 * 跳转支付商收银台网银接口
	 * 
	 * @param bankMap
	 * @return
	 */
	public String bankPay(Map<String, String> bankMap) {
		String time_start = new SimpleDateFormat("yyyymmddhhmmss").format(new Date());
		String nonce_str = DateUtil.getRandom(4) + "";
		String bank_id = bankMap.get("bank_id");
		String trade_type = bankMap.get("trade_type");
		String out_trade_no = bankMap.get("out_trade_no");
		String total_fee = bankMap.get("total_fee");
		String return_url = bankMap.get("return_url");

		String sign = "";
		try {
			Map<String, String> paramsMap = new TreeMap<>();
			paramsMap.put("mch_id", mch_id);
			paramsMap.put("trade_type", trade_type);
			paramsMap.put("out_trade_no", out_trade_no);
			paramsMap.put("total_fee", total_fee);
			paramsMap.put("notify_url", URLEncoder.encode(notify_url, "UTF-8"));
			paramsMap.put("bank_id", bank_id);
			paramsMap.put("return_url", URLEncoder.encode(return_url, "UTF-8"));
			paramsMap.put("time_start", time_start);
			paramsMap.put("nonce_str", nonce_str);
			String signature = FormatBizQueryParaMap(paramsMap, true) + "&key=" + md5_key;
			paramsMap.put("body", body);
			paramsMap.put("attach", attach);
			logger.info("利盈支付待签名字符串:" + signature);

			sign = MD5s.MD5(signature);
			paramsMap.put("sign", sign);
			paramsMap.put("notify_url", notify_url);
			paramsMap.put("return_url", return_url);
			String html = HttpUtil.HtmlFrom(api_url, paramsMap);
			logger.info("利盈支付支付表单:" + html);
			return html;
		} catch (Exception e) {
			logger.info("利盈表单构建异常！");
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 跳转支付商收银台扫码接口(不区分pc和mb)
	 * 
	 * @param scanMap
	 * @return
	 */
	public String scanPay(Map<String, String> scanMap) {
		String time_start = new SimpleDateFormat("yyyymmddhhmmss").format(new Date());
		String nonce_str = DateUtil.getRandom(4) + "";
		String bank_id = "";
		String trade_type = scanMap.get("trade_type");
		String out_trade_no = scanMap.get("out_trade_no");
		String total_fee = scanMap.get("total_fee");
		String return_url = scanMap.get("return_url");

		String sign = "";
		try {
			Map<String, String> paramsMap = new TreeMap<>();
			paramsMap.put("mch_id", mch_id);
			paramsMap.put("trade_type", trade_type);
			paramsMap.put("out_trade_no", out_trade_no);
			paramsMap.put("total_fee", total_fee);
			paramsMap.put("notify_url", URLEncoder.encode(notify_url, "UTF-8"));
			paramsMap.put("bank_id", bank_id);
			paramsMap.put("return_url", URLEncoder.encode(return_url, "UTF-8"));
			paramsMap.put("time_start", time_start);
			paramsMap.put("nonce_str", nonce_str);
			String signature = FormatBizQueryParaMap(paramsMap, true) + "&key=" + md5_key;
			paramsMap.put("body", body);
			paramsMap.put("attach", attach);
			logger.info("利盈支付待签名字符串:" + signature);

			sign = MD5s.MD5(signature);
			paramsMap.put("sign", sign);
			paramsMap.put("notify_url", notify_url);
			paramsMap.put("return_url", return_url);
			String html = HttpUtil.HtmlFrom(api_url, paramsMap);
			logger.info("利盈支付支付表单:" + html);
			return html;
		} catch (Exception e) {
			logger.info("利盈表单构建异常！");
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public String callback(Map<String, String> map) {

		// Map<String, Object> map = JSONUtils.toHashMap(map);
		if (map != null && map.size() != 0) {
			String serverSign = (String) map.remove("sign");

			String signature;
			try {
				signature = FormatBizQueryParaMap(map, true) + "&key=" + md5_key;
				logger.info("利盈支付待签名字符串:" + signature);
				String sign = MD5s.MD5(signature);
				logger.info("支付商签名:" + serverSign + "      本地签名:" + sign);
				if (serverSign.equals(sign)) {
					// 商户在此处处理业务
					return "success";
				}
			} catch (Exception e) {
				logger.info("利盈支付签名异常");
				e.printStackTrace();
				return "";
			}

		}

		return "";
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
	public String FormatBizQueryParaMap(Map<String, String> paraMap, boolean urlencode) throws Exception {
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
					// if ("".equals(val)) {
					// continue;
					// }
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
			response = HttpUtil.doRequest(httpSendModel, "utf-8");
		} catch (Exception e) {
			return e.getMessage();
		}
		return response.getEntityString();
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

		Map<String, String> bankMap = new HashMap<>();
		int int_amount = (int) (amount * 100);
		// bankcode=console 使用支付商收银台
		if ("console".equals(pay_code)) {
			bankMap.put("bank_id", "");
		} else {
			// 直连银行模式 使用银行编码
			bankMap.put("bank_id", pay_code);
		}
		bankMap.put("trade_type", "10");// 支付类型 10 网银支付
		bankMap.put("out_trade_no", order_no);// 订单号
		bankMap.put("total_fee", String.valueOf(int_amount));// 金额 分
		bankMap.put("return_url", refereUrl);
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

		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("trade_type", pay_code);
		scanMap.put("out_trade_no", order_no);// 订单号
		int int_amount = (int) (amount * 100);
		scanMap.put("total_fee", String.valueOf(int_amount));// 金额 分
		scanMap.put("return_url", refereUrl);

		String html = scanPay(scanMap);
		// 状态success表示获取请求成功
		if (StringUtils.isNullOrEmpty(html)) {
			return PayUtil.returnPayJson("error", "1", "支付表单生成失败！", userName, amount, order_no, "");
		}

		return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, amount, order_no, html);
	}
}
