package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.util.StringUtils;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.jhz.util.MD5Utils;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;

import net.sf.json.JSONObject;

/**
 * DS 鼎盛支付
 * @author TX
 */
public class DSZFPayServiceImpl implements PayService{
	private final static Logger logger = LoggerFactory.getLogger(DSZFPayServiceImpl.class);
	private String fxid;
	private String notifyUrl;
	private String secret;
	private String payUrl;//支付地址
	
	public DSZFPayServiceImpl(Map<String,String> map){
		if(map.containsKey("fxid")){
			this.fxid = map.get("fxid");
		}
		if(map.containsKey("notifyUrl")){
			this.notifyUrl = map.get("notifyUrl");
		}
		if(map.containsKey("secret")){
			this.secret = map.get("secret");
		}
		if(map.containsKey("payUrl")){
			this.payUrl = map.get("payUrl");
		}
	}
	
	/**
	 * 银联
	 */
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		logger.info("鼎盛支付银联支付开始......................");
		Map<String,String> map = parameter(payEntity,1);
		logger.info("鼎盛支付银联支付参数值:{}",map);
		
		String sign = generatorSign(map,1);//签名
		map.put("fxsign", sign);
		
		String response;
		try {
			response = HttpUtils.toPostForm(map, payUrl);
			logger.info("鼎盛支付银联支付返回参数:{}",response);
			JSONObject object = JSONObject.fromObject(response);
			if(object.containsKey("status") && "1".equals(object.getString("status"))){
				logger.info("鼎盛支付网银跳转:{}" , object.getString("payurl"));
				//return PayResponse.sm_link(payEntity, object.getString("payurl"), "银联扫码成功!");
				return PayUtil.returnWYPayJson("success", "link", object.getString("payurl"), object.getString("payurl"), "pay");
			}
			return PayResponse.error("鼎盛支付");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * 扫码
	 */
	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("鼎盛支付扫码支付开始......................");
		Map<String,String> map = parameter(payEntity,0);
		logger.info("鼎盛支付扫码支付参数值:{}",map);
		try{
			String sign = generatorSign(map,1);//签名
			map.put("fxsign", sign);
			
			String response = HttpUtils.post(map, payUrl);
			logger.info("鼎盛支付银联支付返回参数:{}",response);
			JSONObject json = JSONObject.fromObject(response);
			if(json.containsKey("status") && "1".equals(json.getString("status"))){
				String payurl = json.getString("payurl");
				if(StringUtils.isEmpty(payEntity.getMobile())){
					return PayResponse.sm_qrcode(payEntity, payurl, "支付成功!");
				}else{
					return PayResponse.sm_link(payEntity, payurl, "支付成功!");
				}
			}
			
			return PayResponse.error("鼎盛支付扫码支付失败, 错误提示信息:"+response);
		}catch(Exception e){
			e.printStackTrace();
			logger.info("鼎盛支付扫码支付出现问题,错误信息:{}",e.getMessage());
			return PayResponse.error("鼎盛支付扫码支付程序错误");
		}
	}

	/**
	 * 回调
	 */
	@Override
	public String callback(Map<String, String> data) {
		logger.info("鼎盛支付回调开始............");
		String sourceSign = data.get("fxsign");
		logger.info("鼎盛支付原 sign 值 :{}",sourceSign);
		
		String sign = generatorSign(data, 0);
		logger.info("鼎盛支付回调 生成的 sign:{}", sign);
		if(sourceSign.equals(sign)){
			return "success";
		}
		
		return "fail";
	}
	
	/**
	 * 参数组装
	 * @param payEntity
	 * type = 1 银联支付
	 */
	private Map<String,String> parameter(PayEntity payEntity,Integer type){
		logger.info("鼎盛支付开始 组装参数.....");
		Map<String,String> map = new HashMap<String,String>();
		String amount = new DecimalFormat("#.##").format(payEntity.getAmount());
		map.put("fxid", fxid);
		map.put("fxddh", payEntity.getOrderNo());//订单号
		map.put("fxdesc", "TOP-UP");//商品名称
		map.put("fxfee", amount);//金额
		map.put("fxnotifyurl", notifyUrl);//回调
		map.put("fxbackurl", "http://www.baidu.com");
		
		if("ylkj".equals(payEntity.getPayCode())){//用于识别用户绑卡信息，仅快捷接口可用
			map.put("fxuserid", fxid);
		}
		////银联支付
		if(1 == type){
			map.put("fxpay", "bank");
			//map.put("fxbankcode", payEntity.getPayCode());
		}else{
			map.put("fxpay", payEntity.getPayCode());//支付类型
		}
	
		map.put("fxip", "58.64.40.26");
		logger.info("鼎盛支付开始 组装参数结果:{}",map);
		return map;
	}
	
	/**
	 * 签名md5(商务号+商户订单号+支付金额+异步通知地址+商户秘钥)支付加密
	 * 签名md5(订单状态+商务号+商户订单号+支付金额+商户秘钥)  回调加密
	 * @param map
	 */
	private String generatorSign(Map<String,String> map,Integer type){
		logger.info("鼎盛支付开始 签名.....");
		StringBuilder sb = new StringBuilder();
		if(type == 1){
			sb.append(fxid).append(map.get("fxddh"))
			.append(map.get("fxfee")).append(map.get("fxnotifyurl"));
		}else{
			sb.append(map.get("fxstatus")).append(fxid).append(map.get("fxddh"))
			.append(map.get("fxfee"));
		}
		
		sb.append(secret);
		
		logger.info("鼎盛支付签名参数:{}",sb);
		
		String md5Value = MD5Utils.md5(sb.toString());
		logger.info("鼎盛支付签名:{}",md5Value);
		return md5Value;
	}
}
