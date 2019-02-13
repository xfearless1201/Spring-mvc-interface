package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.tjf.utils.Config;
import com.cn.tianxia.pay.tx.util.CreateWapOrderReqDto;
import com.cn.tianxia.pay.tx.util.CreateWapOrderRespDto;
import com.cn.tianxia.pay.tx.util.HttpClientUtil;
import com.cn.tianxia.pay.tx.util.SignUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sf.json.JSONObject;

public class TXPayServiceImpl implements PayService {
	private String merId;
	private String version;
	private String orderTitle;
	private String orderDesc;
	private String notifyUrl;
	private String payUrl;
	private String mercKey;

	private final static Logger logger = LoggerFactory.getLogger(TXPayServiceImpl.class);

	public TXPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			merId = jo.get("merId").toString();// MD5
			version = jo.get("version").toString();// 商户密钥
			orderTitle = jo.get("orderTitle").toString();// 商户号
			orderDesc = jo.get("orderDesc").toString();// 通知地址
			notifyUrl = jo.get("notifyUrl").toString();// 支付网关
			payUrl = jo.get("payUrl").toString();// api版本
			mercKey = jo.get("mercKey").toString();// 商品名称
		}
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
		String topay = payEntity.getTopay();

		// 精确到小数点后两位，例如 100000 圆
		DecimalFormat df = new DecimalFormat("#########");
		String pay_amount = df.format(amount);

		JSONObject r_json = null;

		Map<String, String> Map = new HashMap<>();
		Map.put("merOrderNo", order_no);// 订单号
		Map.put("payPlat", pay_code);// 支付类型
		Map.put("orderAmt", pay_amount);// 金额
		Map.put("callbackUrl", refereUrl);
		// 手机端
		if (StringUtils.isNotEmpty(mobile)) {
			r_json = wapPay(Map);
		} else {
			// pc端
			r_json = scanPay(Map);
		}

		if ("success".equals(r_json.getString("status"))) {
			return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
					r_json.getString("qrCode"));
		} else {
			return PayUtil.returnPayJson("error", "4", r_json.getString("msg"), userName, amount, order_no, "");
		}
	}

	/**
	 * 手机接口
	 * 
	 * @param wapMap
	 * @return
	 */
	public JSONObject wapPay(Map<String, String> wapMap) {
		String merOrderNo = wapMap.get("merOrderNo");
		String orderAmt = wapMap.get("orderAmt");
		String payPlat = wapMap.get("payPlat");
		String callbackUrl = wapMap.get("callbackUrl");
		CreateWapOrderReqDto reqDto = new CreateWapOrderReqDto();
		reqDto.setMerId(merId);
		reqDto.setMerOrderNo(merOrderNo);
		reqDto.setVersion(version);
		reqDto.setOrderAmt(orderAmt);
		reqDto.setPayPlat(payPlat);
		reqDto.setOrderTitle(orderTitle);
		reqDto.setOrderDesc(orderDesc);
		reqDto.setNotifyUrl(notifyUrl);
		reqDto.setCallbackUrl(callbackUrl);
		// 商户接入秘钥
		// String mercKey = "cc29c6633d4159c4a0bad1141da3337c";

		String sign = SignUtil.signData(reqDto.toTreeMap(), mercKey);
		reqDto.setSign(sign);
		Gson gson = new GsonBuilder().create();

		String postStr = gson.toJson(reqDto);

		// String url = request.getParameter("reqUrl");
		logger.info("请求参数:" + postStr);
		String postResultStr = HttpClientUtil.doPost(payUrl + "createWapOrder.do", postStr);

		logger.info("天下支付wap响应数据:" + postResultStr);

		CreateWapOrderRespDto resp = gson.fromJson(postResultStr, CreateWapOrderRespDto.class);

		return resp.getJumpUrl() == null ? getReturnJson("error", "", postResultStr)
				: getReturnJson("success", resp.getJumpUrl(), "连接获取成功");
	}

	/**
	 * pc接口
	 * 
	 * @param wapMap
	 * @return
	 */
	public JSONObject scanPay(Map<String, String> wapMap) {
		String merOrderNo = wapMap.get("merOrderNo");
		String orderAmt = wapMap.get("orderAmt");
		String payPlat = wapMap.get("payPlat");
		String callbackUrl = wapMap.get("callbackUrl");
		CreateWapOrderReqDto reqDto = new CreateWapOrderReqDto();
		reqDto.setMerId(merId);
		reqDto.setMerOrderNo(merOrderNo);
		reqDto.setVersion(version);
		reqDto.setOrderAmt(orderAmt);
		reqDto.setPayPlat(payPlat);
		reqDto.setOrderTitle(orderTitle);
		reqDto.setOrderDesc(orderDesc);
		reqDto.setNotifyUrl(notifyUrl);
		reqDto.setCallbackUrl(callbackUrl);
		// 商户接入秘钥
		// String mercKey = request.getParameter("mercKey");

		String sign = SignUtil.signData(reqDto.toTreeMap(), mercKey);
		reqDto.setSign(sign);
		Gson gson = new GsonBuilder().create();

		String postStr = gson.toJson(reqDto);

		// String url="http://127.0.0.1:8080/grmApp/createWapOrder.do";
		// String url = request.getParameter("reqUrl");
		logger.info("请求参数:" + postStr);
		String postResultStr = HttpClientUtil.doPost(payUrl + "createScanOrder.do", postStr);

		CreateWapOrderRespDto resp = gson.fromJson(postResultStr, CreateWapOrderRespDto.class);

		logger.info("天下支付scan响应数据:" + postResultStr);

		return resp.getJumpUrl() == null ? getReturnJson("error", "", postResultStr)
				: getReturnJson("success", resp.getJumpUrl(), "连接获取成功");

	}

	/**
	 * 回调接口
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public String callback(Map<String, String> map) {

		TreeMap<String, String> TreeMap = new TreeMap<String, String>(map);

		String localSign = SignUtil.signData(TreeMap, mercKey);
		String serviceSign = TreeMap.get("sign");

		logger.info("localSign:" + localSign + "        serviceSign:" + serviceSign);
		if (serviceSign.equals(localSign)) {
			logger.info("天下支付验签成功!");
			return "success";
		}

		logger.info("天下支付验签失败!");
		return "";
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
}
