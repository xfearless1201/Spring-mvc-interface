package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;

import net.sf.json.JSONObject;

public class XHEIPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(XHEIPayServiceImpl.class);
	/**支付地址*/
	private String payUrl;
	/**商户编号*/
	private String payMemberid;
	/**商户接收支付成功数据的地址*/
	private String payNotifyUrl;
	/**商户密钥*/
	private String md5Key;
	public XHEIPayServiceImpl(Map<String,String> data) {
		if(data!=null){
			if(data.containsKey("payUrl")){
				this.payUrl = data.get("payUrl");
			}
			if(data.containsKey("payMemberid")){
				this.payMemberid = data.get("payMemberid");
			}
			if(data.containsKey("payNotifyUrl")){
				this.payNotifyUrl = data.get("payNotifyUrl");
			}
			if(data.containsKey("md5Key")){
				this.md5Key = data.get("md5Key");
			}
		}
	}
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("[XHEI]新嘿扫码支付开始======================START==================");
		try {
			//封装请求参数
			Map<String, String> data = sealRequest(payEntity);
			//生成签名串
			String sign = generatorSign(data);
			data.put("sign", sign);
			String reqData = JSONObject.fromObject(data).toString();
			logger.info("[XHEI]新嘿扫码支付请求参数:"+reqData);
			//生成请求表单
			String resStr = HttpUtils.toPostForm(data, payUrl);
			logger.info("[XHEI]新嘿扫码支付响应信息:"+resStr);
			if(StringUtils.isBlank(resStr)){
				logger.info("[XHEI]新嘿扫码支付发起HTTP请求无响应结果");
				return PayResponse.error("[XHEI]新嘿扫码支付发起HTTP请求无响应结果");
			}
			JSONObject resObj = JSONObject.fromObject(resStr);
			if(resObj.containsKey("code")&&0==resObj.getInt("code")){
				resObj = resObj.getJSONObject("data");
				return PayResponse.sm_qrcode(payEntity, resObj.getString("data"), "下单成功");
			}
			return PayResponse.error("[XHEI]新嘿扫码支付下单失败"+resStr);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("[XHEI]新嘿扫码支付生成异常:"+e.getMessage());
			return PayResponse.error("[XHEI]新嘿扫码支付下单失败");
		}
	}

	@Override
	public String callback(Map<String, String> data) {
		try {
			String sourceSign = data.remove("sign");
			String sign = generatorSign(data);
			logger.info("[XHEI]新嘿扫码支付回调生成签名串"+sign);
			if(sign.equals(sourceSign)) return "success";
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("[XHEI]新嘿扫码支付回调生成签名串异常"+e.getMessage());
		}
		return null;
	}
	/**
	 * 
	 * @Description 封装支付请求参数
	 * @param entity
	 * @param 
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> sealRequest(PayEntity payEntity){ 
		DecimalFormat df = new DecimalFormat("0");
		Map<String,String> data = new HashMap<>();
		data.put("merchantNo", payMemberid);//商户号
		data.put("outOrderNo", payEntity.getOrderNo());//订单号
		data.put("amount", df.format(payEntity.getAmount()*100));//金额 单位：分
		data.put("userId", payEntity.getuId());
		data.put("payMethod", payEntity.getPayCode());//支付方式
		data.put("justPayUrl", "1");//支付方式
		data.put("callbackUrl", payNotifyUrl);
		return data;
	}
	/**
	 * 
	 * @Description 生成签名串
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public String generatorSign(Map<String,String> data) throws Exception{
		Map<String,String> sortmap = MapUtils.sortByKeys(data);
		StringBuffer sb = new StringBuffer();
		Iterator<String> iterator = sortmap.keySet().iterator();
		while(iterator.hasNext()){
			String key = iterator.next();
			String val = sortmap.get(key);
			if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign")) continue;
			sb.append("&").append(key).append("=").append(val);
		}
		sb.append(md5Key);
		String signStr = sb.toString();
		signStr = signStr.substring(1);
		//生成待签名串
		logger.info("[XHEI]新嘿扫码支付生成待签名串:{}",signStr);
		String sign = MD5Utils.md5toUpCase_32Bit(signStr);
		logger.info("[XHEI]新嘿扫码支付生成加密签名串:{}",sign);
		return sign;
	}
}
