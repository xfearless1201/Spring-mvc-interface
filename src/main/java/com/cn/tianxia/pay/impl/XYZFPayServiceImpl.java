package com.cn.tianxia.pay.impl;

import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;

public class XYZFPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(XYZFPayServiceImpl.class);
	/**支付地址*/
	private String payUrl;
	/**商户编号*/
	private String payMemberid;
	/**商户接收支付成功数据的地址*/
	private String payNotifyUrl;
	/**商户密钥*/
	private String md5Key;
	
	public XYZFPayServiceImpl(Map<String,String> data) {
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
		logger.info("[XYZF]新艺支付扫码支付开始======================START==================");
		try {
			//封装请求参数
			Map<String, String> data = sealRequest(payEntity);
			//生成签名串
			String sign = generatorSign(data);
			data.put("sign", sign);
			logger.info("[XYZF]新艺支付请求参数:"+data);
			//生成请求表单
			logger.info("支付地址："+payUrl);
			String resStr = HttpUtils.generatorForm(data, payUrl);
			logger.info("[XYZF]新艺支付支付响应信息:"+resStr);
			if(StringUtils.isBlank(resStr)){
				logger.info("[XYZF]新艺支付支付扫码支付发起HTTP请求无响应结果");
				return PayResponse.error("[XYZF]新艺支付支付扫码支付发起HTTP请求无响应结果");
			}
			return PayResponse.sm_form(payEntity, resStr, "下单成功");
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("[XYZF]新艺支付生成异常:"+e.getMessage());
			return PayResponse.error("[XYZF]新艺支付下单失败");
		}
	}

	@Override
	public String callback(Map<String, String> data) {
		try {
			String sourceSign = data.get("sign");
			StringBuilder sb = new StringBuilder();
			sb.append("status=").append(data.get("status")).append("&");
			sb.append("shid=").append(data.get("shid")).append("&");
			sb.append("bb=").append(data.get("bb")).append("&");
			sb.append("zftd=").append(data.get("zftd")).append("&");
			sb.append("ddh=").append(data.get("ddh")).append("&");
			sb.append("je=").append(data.get("je")).append("&");
			sb.append("ddmc=").append(data.get("ddmc")).append("&");
			sb.append("ddbz=").append(data.get("ddbz")).append("&");
			sb.append("ybtz=").append(data.get("ybtz")).append("&");
			sb.append("tbtz=").append(data.get("tbtz")).append("&");
			sb.append(md5Key);
			String signStr = sb.toString();
			logger.info("[XYZF]新艺支付回调生成待签名串"+signStr);
			String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
			logger.info("[XYZF]新艺支付回调生成签名串"+sign);
			if(sign.equals(sourceSign)) return "success";
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			logger.info("[XYZF]新艺支付回调生成签名串异常"+e.getMessage());
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
		try {
			DecimalFormat df = new DecimalFormat("0.00");
			Map<String,String> data = new HashMap<>();
			data.put("bb", "1.0");//版本号
			data.put("shid", payMemberid);//商户号
			data.put("ddh", payEntity.getOrderNo());//订单号
			data.put("je", df.format(payEntity.getAmount()));//总金额,单位：分 
			data.put("zftd", payEntity.getPayCode());//支付渠道
			data.put("ybtz", payNotifyUrl);//异步回调地址
			data.put("tbtz", payNotifyUrl);//同步跳转地址
			data.put("ddmc", "Pay");//订单名称
			data.put("ddbz", "Pay");//订单备注
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("[XYZF]新艺支付获取请求参数异常"+e.getMessage());
			return null;
		}
	}
	/**
     * 
     * @Description 生成签名串
     * @param data
     * @return
	 * @throws Exception
     */
    public String generatorSign(Map<String,String> data) throws Exception{
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("shid=").append(data.get("shid")).append("&");
			sb.append("bb=").append(data.get("bb")).append("&");
			sb.append("zftd=").append(data.get("zftd")).append("&");
			sb.append("ddh=").append(data.get("ddh")).append("&");
			sb.append("je=").append(data.get("je")).append("&");
			sb.append("ddmc=").append(data.get("ddmc")).append("&");
			sb.append("ddbz=").append(data.get("ddbz")).append("&");
			sb.append("ybtz=").append(data.get("ybtz")).append("&");
			sb.append("tbtz=").append(data.get("tbtz")).append("&");
			sb.append(md5Key);
			//生成待签名串
			String signStr = sb.toString();
			logger.info("[XYZF]新艺支付生成待签名串:{}",signStr);
			String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
			logger.info("[XYZF]新艺支付生成加密签名串:{}",sign);
			return sign;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("[XYZF]新艺支付生成加密签名串失败"+e.getMessage());
			return null;
		}
    }
    
   /* public static void main(String[] args) throws NoSuchAlgorithmException {
    	StringBuilder sb = new StringBuilder();
		Map<String, String> data = new HashMap<>();
		data.put("status", "success");
		data.put("shid", "10692");
		data.put("bb", "1.0");
		data.put("zftd", "alipay");
		data.put("ddh", "XYZFbl1201901051524411524417922");
		data.put("je", "100.00");
		data.put("ddmc", "Pay");
		data.put("ddbz", "Pay");
		data.put("ybtz", "http://txw.tx8899.com/JJF/Notify/XYZFNotify.do");
		data.put("tbtz", "http://txw.tx8899.com/JJF/Notify/XYZFNotify.do");
		data.put("md5Key", "9jt1byts6hcp7l7v6fiqy5lv967atzfji8k9nu2t");
		XYZFPayServiceImpl xyzfPayServiceImpl = new XYZFPayServiceImpl(data);
		String sign = xyzfPayServiceImpl.callback(data);
        System.out.println(sign);
	}*/
}
