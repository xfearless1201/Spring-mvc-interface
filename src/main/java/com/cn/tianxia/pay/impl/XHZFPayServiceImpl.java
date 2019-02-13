package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

public class XHZFPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(XHZFPayServiceImpl.class);
	/**支付地址*/
	private String payUrl;
	/**商户编号*/
	private String payMemberid;
	/**商户接收支付成功数据的地址*/
	private String payNotifyUrl;
	/**商户密钥*/
	private String key;
	public XHZFPayServiceImpl(Map<String,String> data) {
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
			if(data.containsKey("key")){
				this.key = data.get("key");
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
		logger.info("[XHZF]新汇一代扫码支付开始======================START==================");
		try {
			//封装请求参数
			Map<String, String> data = sealRequest(payEntity);
			//生成签名串
			String sign = generatorSign(data);
			data.put("sign", sign);
			logger.info("[XHZF]新汇一代扫码支付请求参数:"+JSONObject.fromObject(data));
			//生成请求表单
			String resStr = HttpUtils.toPostForm(data, payUrl);
			logger.info("[XHZF]新汇一代扫码支付响应信息:"+resStr);
			if(StringUtils.isBlank(resStr)){
				logger.info("[XHZF]新汇一代扫码支付发起HTTP请求无响应结果");
				return PayResponse.error("[XHZF]新汇一代扫码支付发起HTTP请求无响应结果");
			}
			JSONObject resObj = JSONObject.fromObject(resStr);
			if(resObj.containsKey("url")&&resObj.containsKey("msg")){
				return PayResponse.sm_qrcode(payEntity, resObj.getString("url"), resObj.getString("msg"));
			}
			return PayResponse.error("[XHZF]新汇一代扫码支付下单失败"+resStr);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("[XHZF]新汇一代扫码支付生成异常:"+e.getMessage());
			return PayResponse.error("[XHZF]新汇一代扫码支付下单失败");
		}
	}

	@Override
	public String callback(Map<String, String> data) {
		try {
			String sourceSign = data.get("sign");
			String sign = generatorSign(data);
			logger.info("[XHZF]新汇一代扫码支付回调生成签名串"+sign);
			if(sign.equals(sourceSign.toUpperCase())) return "success";
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("[XHZF]新汇一代扫码支付回调生成签名串异常"+e.getMessage());
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
		DecimalFormat df = new DecimalFormat("0.00");
		Map<String,String> data = new HashMap<>();
		data.put("mchid", payMemberid);//商户号
		data.put("order_id", payEntity.getOrderNo());//订单号
		data.put("channel_id", payEntity.getPayCode());//通道编码
		data.put("total_amount", df.format(payEntity.getAmount()));//金额 单位：元
		data.put("return_url", payNotifyUrl);
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
			sb.append(key).append(val);
		}
		sb.append(key);
		//生成待签名串
		String signStr = sb.toString();
		logger.info("[XHZF]新汇一代扫码支付生成待签名串:{}",signStr);
		String sign = DigestUtils.sha1Hex(signStr).toUpperCase();
		logger.info("[XHZF]新汇一代扫码支付生成加密签名串:{}",sign);
		return sign;
	}
}
