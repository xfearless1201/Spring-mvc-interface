package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.util.StringUtils;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.mob.util.HttpUtil;
import com.cn.tianxia.pay.mob.util.MD5;
import com.cn.tianxia.pay.mob.util.MobPayUtil;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * MO宝支付
 * 
 * @author hb
 * @date 2018-06-29
 */
public class MOBPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(MOBPayServiceImpl.class);

	// 正式支付地址
	private JSONObject payUrls ;//= "http://115.182.202.23:8880/ks_smpay/netsm/pay.sm";//"http://115.182.202.23:8880/ks_smpay/netsm/pay.sm";//;
	// 密钥
	private String md5Key ;//= "1FDD2547FA4FB61F";//"5FE4CD58BA6AB98D";// "1FDD2547FA4FB61F";
	// 商户号
	private String merId ;//= "818310048160000";// "936956374620000";// ;
	// 异步回调地址
	private String backNotifyUrl ;//= "http://182.16.110.186:8080/XPJ/Notify/MOBNotify.do";
	// 商品描述
	private String orderDesc ;//= "pay";
	// 签名类型
	private String signType ;//= "MD5";
	// 版本号
	private String versionId ;//= "001";
	// 交易业务类型
	private String businessType ;//= "1100";

	public MOBPayServiceImpl() {
	}

	public MOBPayServiceImpl(Map<String, String> pmap) {
		JSONObject config = JSONObject.fromObject(pmap);
		
		this.payUrls = config.getJSONObject("payUrls");
		this.md5Key = config.getString("md5Key");
		this.merId = config.getString("merId");
		this.backNotifyUrl = config.getString("backNotifyUrl");
		this.orderDesc = config.getString("orderDesc");
		this.signType = config.getString("signType");
		this.versionId = config.getString("versionId");
		this.businessType = config.getString("businessType");
	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		String userName = payEntity.getUsername();

		String transChanlName = payEntity.getPayCode();// "0008";//银联扫码
		String orderId = payEntity.getOrderNo();// new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());//订单号
		String transDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());// 交易日期
		double transAmount = payEntity.getAmount();// 交易金额

		// 银联扫码
		if ("0008".equals(transChanlName)) {
			String responseStr = ylscan(payEntity);

			try {
				responseStr = URLDecoder.decode(responseStr, "gbk");
				logger.info("返回参数串responseStr = "+responseStr);
			} catch (UnsupportedEncodingException e) {
			}
			JSONObject responseJson = JSONObject.fromObject(responseStr);
			logger.info("返回json=" + responseJson);
			String status = responseJson.getString("status");
			// 获取支付地址成功
			if ("00".equals(status)) {
				String codeUrl = responseJson.getString("codeUrl");
				logger.info("获取二维码地址成功:" + codeUrl);
				return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, transAmount, orderId, codeUrl);
			}
			// 获取支付地址失败
			logger.error("获取二维码地址失败:" + responseJson.getString("refMsg"));
			return PayUtil.returnPayJson("error", "2", responseJson.getString("refMsg"), userName, transAmount, orderId,"");
		}
		// 快捷支付
		if ("UNIONPAY".equalsIgnoreCase(transChanlName)) {
			String html = kjscan(payEntity);
			logger.info("获取form表单："+html);
			return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, transAmount, orderId, html);
		}

		return PayUtil.returnPayJson("error", "2", "只对接了银联扫码与快捷两个通道", userName, transAmount, orderId,"");
	}

	// 快捷支付
	private String kjscan(PayEntity payEntity) {
		String transChanlName = payEntity.getPayCode();// 快捷
		String payUrl = this.payUrls.getString(transChanlName);
		String orderId = payEntity.getOrderNo();// 订单支付
		String transDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());// 交易日期
		double transAmount = payEntity.getAmount();// 交易金额

		Map<String, String> params = new LinkedHashMap<>();
		params.put("versionId", this.versionId);
		params.put("businessType", this.businessType);
		params.put("insCode", ""); // 机构号
		params.put("merId", this.merId);
		params.put("orderId", orderId);
		params.put("transDate", transDate);
		params.put("transAmount", String.valueOf(transAmount));
		params.put("transCurrency", "156");// 交易币种
		params.put("transChanlName", transChanlName);
		params.put("pageNotifyUrl", payEntity.getRefererUrl());// 页面通知地址
		params.put("backNotifyUrl", this.backNotifyUrl);
		params.put("orderDesc", this.orderDesc);
		params.put("dev", "");
		params.put("signData", genKjSign(params));// 签名

		String requestForm = buildForm(params, payUrl);
		return requestForm;
	}

	// 银联扫码
	private String ylscan(PayEntity payEntity) {
		//"http://115.182.202.23:8880/ks_smpay/netsm/pay.sm";//"http://newpay.kspay.net:8181/ks_smpay/netsm/pay.sm";
		String transChanlName = payEntity.getPayCode();// "0008";//银联扫码
		String payUrl = this.payUrls.getString(transChanlName);
		String orderId = payEntity.getOrderNo();// new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());//订单号
		String transDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());// 交易日期
		double transAmount = payEntity.getAmount();// 交易金额

		Map<String, String> params = new TreeMap<>();
		params.put("versionId", this.versionId);
		params.put("businessType", this.businessType);
		params.put("transChanlName", transChanlName);
		params.put("merId", this.merId);
		params.put("orderId", orderId);
		params.put("transDate", transDate);
		params.put("transAmount", String.valueOf(transAmount));
		params.put("backNotifyUrl", this.backNotifyUrl);
		params.put("orderDesc", this.orderDesc);
		params.put("signType", this.signType);
		params.put("signData", genYlsmSign(params));// 签名

		String responseStr = HttpUtil.RequestForm(payUrl, params);
		return responseStr;
	}

	// 创建银联扫码签名
	private String genYlsmSign(Map<String, String> params) {

		StringBuilder builder = new StringBuilder();
		for (String key : params.keySet()) {
			String value = params.get(key);
			if (StringUtils.isEmpty(value) || "signType".equalsIgnoreCase(key) || "signData".equalsIgnoreCase(key)) {
				continue;
			}
			builder.append(key + "=" + value + "&");
		}
		builder.append("key=" + this.md5Key);

		logger.info("签名字符串=" + builder.toString());
		String sign = MD5.MD5(builder.toString());
		logger.info("签名sign=" + sign);

		return sign;
	}

	// 创建快捷支付签名
	private String genKjSign(Map<String, String> params) {
		String signstr = MobPayUtil.getUrlStr(params);
		logger.info("需要签名的明文:" + signstr);
		String sign = MD5.MD5(signstr + this.md5Key);

		return sign;
	}

	public String buildForm(Map<String, String> paramMap, String payUrl) {
		// 待请求参数数组
		String FormString = 
		"<body onLoad=\"document.actform.submit()\">正在处理请稍候....................."
				+ "<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""+ payUrl + "\">";
		for (String key : paramMap.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramMap.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";

		return FormString;
	}

	//回调验签
	/**
	 *  {businessType=1100, 
	 *  ksPayOrderId=20180630128212, 
	 *  orderDesc=pay, 
	 *  orderId=MOBbl1201806301700101700109891, 
	 *  refMsg=%BD%BB%D2%D7%B3%C9%B9%A6, 
	 *  refcode=00, 
	 *  signData=27E74498EE190DE7024F21F41730CAFF, 
	 *  transAmount=0.2, 
	 *  transChanlName=0008, 
	 *  transDate=20180630165721, 
	 *  versionId=001} 
	 * @param infoMap
	 * @return
	 */
	@Override
	public String callback(Map<String, String> params) {
		
		String channel = params.get("transChanlName");
		//银联扫码
		if("0008".equals(channel)) {
			String remoteSign = params.get("signData");
			String localSign = genYlsmSign(params);
			if(localSign.equalsIgnoreCase(remoteSign)) {
				logger.info("验签成功");
				return "success";
			}
			logger.info("验签失败");
			return "fail";
		}
		return "success";
	}
	
	public static void main(String[] args) {

		MOBPayServiceImpl service = new MOBPayServiceImpl();
		service.merId="818310048160000";
		service.md5Key = "1FDD2547FA4FB61F";
		
		Map<String, String> params = new TreeMap<>();
		
		params.put("businessType","1100");
		params.put("ksPayOrderId","20180630128212");
		params.put("orderDesc","pay");
		params.put("orderId","MOBbl1201806301700101700109891");
		params.put("refMsg","%BD%BB%D2%D7%B3%C9%B9%A6");
		params.put("refcode","00");
		params.put("signData","27E74498EE190DE7024F21F41730CAFF");
		params.put("transAmount","0.2");
		params.put("transChanlName","0008");
		params.put("transDate","20180630165721");
		params.put("versionId","001");
		
		service.callback(params);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
