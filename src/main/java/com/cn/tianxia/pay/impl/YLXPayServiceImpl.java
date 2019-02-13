package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.glx.tuil.SignUtil;
import com.cn.tianxia.pay.glx.tuil.SimpleHttpUtils;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

public class YLXPayServiceImpl implements PayService {
	/** 支付接口地址 **/
	private String url;
	/** appKey **/
	private String appKey;
	/** appId **/
	private String appId;
	/** 商户号 **/
	private String custNo;
	/** 附加参数 **/
	private String attach;
	/** 回调地址 **/
	private String callBackUrl;

	private final static Logger logger = LoggerFactory.getLogger(YLXPayServiceImpl.class);

	public YLXPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			url = jo.get("url").toString();
			appKey = jo.get("appKey").toString();
			appId = jo.get("appId").toString();
			custNo = jo.get("custNo").toString();
			attach = jo.get("attach").toString();
			callBackUrl = jo.get("callBackUrl").toString();
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
		String topay = payEntity.getTopay();
		// 精确到小数点后两位，例如 1000
		DecimalFormat df = new DecimalFormat("#########");
		String pay_amount = df.format(amount);

		Map<String, Object> map = new HashMap<>();
		map.put("payChannel", pay_code);
		map.put("money", pay_amount);
		map.put("mchOrderNo", order_no);// 金额
		JSONObject r_json = pay(map);
		return PayUtil.returnWYPayJson("success", "link", r_json.getString("qrCode"), pay_url, "");
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
		String topay = payEntity.getTopay();

		// 精确到小数点后两位，例如 1000
		DecimalFormat df = new DecimalFormat("#########");
		String pay_amount = df.format(amount);
		Map<String, Object> map = new HashMap<>();
		map.put("payChannel", pay_code);
		map.put("money", pay_amount);
		map.put("mchOrderNo", order_no);// 金额
		JSONObject r_json = pay(map);

		if ("success".equals(r_json.getString("status"))) {
			if (StringUtils.isNotEmpty(mobile)) {
				return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
						r_json.getString("qrCode"));
			} else {
				// 快捷支付试用连接方式
				String type = "2";
				if ("05".equals(pay_code)) {
					type = "4";
				}
				return PayUtil.returnPayJson("success", type, "支付接口请求成功!", userName, amount, order_no,
						r_json.getString("qrCode"));
			}

		} else {
			return PayUtil.returnPayJson("error", "4", r_json.getString("msg"), userName, amount, order_no, "");
		}
	}

	/**
	 * 获取支付url
	 */
	public JSONObject pay(Map<String, Object> map) {
		// Map<String, Object> map = new TreeMap<String, Object>();
		map.put("appId", appId);
		map.put("custNo", custNo);
		map.put("payChannel", map.get("payChannel"));
		map.put("money", map.get("money"));
		map.put("attach", attach);
		// 回调地址
		map.put("callBackUrl", callBackUrl);
		map.put("mchOrderNo", map.get("mchOrderNo"));

		String signValue = SignUtil.sign(map, appKey);
		map.put("sign", signValue);

		String msg = SimpleHttpUtils.httpPost(url + "/open/pay/scanCodePayChannel", map, 1000 * 15);

		JSONObject mapReturn = JSONObject.fromObject(msg);

		logger.info("返回值完整报文:" + mapReturn.toString());

		if (mapReturn.containsKey("code") && 1 == mapReturn.getDouble("code") && mapReturn.containsKey("pay_url")) {
			Double codeDouble = mapReturn.getDouble("code");
			int code = codeDouble.intValue();
			logger.info("返回code:" + code);
			logger.info("返回支付url:" + mapReturn.get("pay_url"));
			logger.info("返回支付orderId:" + mapReturn.get("orderId"));
			return getReturnJson("success", mapReturn.getString("pay_url"), "接口数据获取成功");
		}

		return getReturnJson("error", "", mapReturn.toString());
	}

	/**
	 * 有进行验证签名的回调方案
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public String callback(Map<String, String> data) {
	    Map<String,Object> map = new HashMap<>();
	    Iterator<String> iterator = data.keySet().iterator();
	    while(iterator.hasNext()){
	        String key = iterator.next();
	        Object val = data.get(key);
	        
	        map.put(key, val);
	    }
	    
	    
		logger.info("进入回调");
		// 回调带过来的 参数签名值
		String signValue = SignUtil.mapGetString(map, "sign");
		// 自己加密的签名值
		String mySignValue = SignUtil.sign(map, appKey);
		if (StringUtils.isBlank(signValue)) {
			logger.info("签名不存在!");
			return "FAIL";
		}
		if (StringUtils.equals(signValue, mySignValue)) {
			logger.info("验签成功!");
			return "success";// 处理成功返回success字符串
		}
		logger.info("验签失败");
		return "FAIL";
	}

	/**
	 * 结果返回
	 * 
	 * @param status
	 * @param qrCode
	 * @param msg
	 * @return
	 */
	private static JSONObject getReturnJson(String status, String qrCode, String msg) {
		JSONObject json = new JSONObject();
		json.put("status", status);
		json.put("qrCode", qrCode);
		json.put("msg", msg);
		return json;
	}

}
