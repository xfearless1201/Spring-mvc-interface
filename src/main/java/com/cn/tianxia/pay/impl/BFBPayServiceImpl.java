package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.bfb.util.HttpUtils;
import com.cn.tianxia.pay.bfb.util.MD5Utils;
import com.cn.tianxia.pay.bfb.util.PayChannel;
import com.cn.tianxia.pay.bfb.util.SignUtils;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName: BFBPayServiceImpl
 * @Description:必付宝支付
 * @author: Hardy
 * @date: 2018年8月18日 下午8:44:13
 * 
 * @Copyright: 天下科技
 *
 */
public class BFBPayServiceImpl implements PayService {

	/**
	 * 日志
	 */
	protected final static Logger logger = LoggerFactory.getLogger(BFBPayServiceImpl.class);

	private static final String RET_CODE = "SUCCESS";

	private String merchantId;// 商户号,必付宝发配唯一标示

	private String noUrl;// 异步回调地址

	private String retUrl;// 同步

	private String payUrl;// pc端支付地址
	
	private String mPayUrl;//手机端支付url

	private String version;// 版本号

	private String sercet;// 签名秘钥
	
	private String type;//支付类型

	public BFBPayServiceImpl(Map<String, String> pmap, String type) {
		
		this.type = type;
		
		if (pmap != null) {
			for (PayChannel payChannel : PayChannel.values()) {
				if (payChannel.getCode().equals(type.toUpperCase())) {
					if (pmap.containsKey(type)) {
						JSONObject data = JSONObject.fromObject(pmap.get(type));

						if (data.containsKey("noUrl")) {
							this.noUrl = data.getString("noUrl");
						}

						if (data.containsKey("retUrl")) {
							this.retUrl = data.getString("retUrl");
						}

						if (data.containsKey("version")) {
							this.version = data.getString("version");
						}
						
						if (data.containsKey("merchantId")) {
							this.merchantId = data.getString("merchantId");
						}

						if (data.containsKey("sercet")) {
							this.sercet = data.getString("sercet");
						}
						
						if (data.containsKey("payUrl")) {
							this.payUrl = data.getString("payUrl");
						}
						//不是网银或快捷的情况下,支持H5
						if(!PayChannel.BANK.getCode().equals(type.toUpperCase()) && !PayChannel.KJ.getCode().equals(type.toUpperCase())){
							if (data.containsKey("mPayUrl")) {
								this.mPayUrl = data.getString("mPayUrl");
							}
						}
					}
				}
			}
		}
	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		logger.info("==============================必付宝网页支付start=====================================");
		String username = payEntity.getUsername();
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		try {
			// 封装支付参数
			Map<String, String> data = getPayDataMap(payEntity, type);
			// 将Map物件转换成字串
			String paramSrc = SignUtils.mapToString(data);
			logger.info("BFB支付代签名字符串:" + paramSrc);
			// 路径+上MD5金钥制作签名
			String sign = MD5Utils.md5(paramSrc + sercet, "UTF-8");
			logger.info("BFB支付签名字符串:" + paramSrc);
			// URLEncoder.encode(utf-8)参数的值
//			paramSrc = SignUtils.mapToURLEncodeString(data);
			// 结尾加上签名
//			paramSrc = paramSrc + "&SIGNED_MSG=" + sign;
			data.put("SIGNED_MSG", sign);
			logger.info("BFB扫码支付URL:" + payUrl);
//			String result = HttpUtils.post(payUrl, paramSrc);
			String result = createPayForm(JSONObject.fromObject(data));
			logger.info("BFB网关支付结果返回值:" + result);
			logger.info("==============================必付宝网页支付end=====================================");
			return PayUtil.returnWYPayJson("success", "form", result, payEntity.getPayUrl(), "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("BFB网页支付异常:" + e.getMessage());
			return PayUtil.returnPayJson("error", "1", "BFB网页支付异常", username, amount, order_no, "");
		}
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("==============================必付宝扫码支付start=====================================");
		String mobile = payEntity.getMobile();
		String username = payEntity.getUsername();
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String resType = "2";// 默认为PC端
		String msg = "二维码图片生成";// 默认为二维码图片
		if(StringUtils.isNoneBlank(mobile)){
			//手机端
			resType = "4";// 默认为移动端
			msg = "二维码图片链接";// 默认为二维码图片
			this.payUrl = mPayUrl;//手机端支付url
		}
		try {
			// 封装支付参数
			Map<String, String> data = getPayDataMap(payEntity, type);
			// 将Map物件转换成字串
			String paramSrc = SignUtils.mapToString(data);
			logger.info("BFB支付代签名字符串:" + paramSrc);
			// 路径+上MD5金钥制作签名
			String sign = MD5Utils.md5(paramSrc + sercet, "UTF-8");
			logger.info("BFB支付签名字符串:" + paramSrc);
			// URLEncoder.encode(utf-8)参数的值
			paramSrc = SignUtils.mapToURLEncodeString(data);
			// 结尾加上签名
			paramSrc = paramSrc + "&SIGNED_MSG=" + sign;
			logger.info("BFB扫码支付URL:" + payUrl);
			String result = HttpUtils.post(payUrl, paramSrc);
			logger.info("BFB扫码支付结果返回值:" + result);
			try {
				JSONObject payResult = JSONObject.fromObject(result);
				String retCode = payResult.getString("RET_CODE");
				String qrCode = payResult.getString("QR_CODE");
				if (RET_CODE.equals(retCode)) {
					return PayUtil.returnPayJson(retCode.toLowerCase(), resType, msg, username, amount, order_no, qrCode);
				}
				logger.info("==============================必付宝扫码支付end=====================================");
				return PayUtil.returnPayJson("error", resType, "BFB扫码支付请求失败", username, amount, order_no, "");
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("BFB支付结果JSON解析异常" + e.getMessage());
				return PayUtil.returnPayJson("error", resType, result, username, amount, order_no, "");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("BFB扫码支付异常:" + e.getMessage());
			return PayUtil.returnPayJson("error", resType, "BFB扫码支付异常", username, amount, order_no, "");
		}
	}

	/**
	 * 
	 * @Title: callback   
	 * @Description: 必付宝支付回调 
	 * @param: @param map
	 * @param: @return      
	 * @return: String      
	 * @throws
	 */
	@Override
	public String callback(Map<String,String> map){
		logger.info("BFB支付回调=============================start=============================");
		//获取签名
		String signMsg = map.get("SIGNED_MSG").toString();
		//从请求参数中去掉签名
		map.remove("SIGNED_MSG");
		//获取代签名字符串
		String paramSrc = SignUtils.mapToString(map);
		logger.info("BFB回调请求参数:" + map);
		// 路径+上MD5金钥制作签名
		String sign = MD5Utils.md5(paramSrc + sercet, "UTF-8");
		logger.info("BFB支付签名字符串:" + paramSrc);
		
		logger.info("BFB支付回调=============================end=============================");
		
		if(signMsg.equals(sign)){
			return "success";
		}
		return null;
	} 
	
	/**
	 * 
	 * @Title: getPayDataMap   
	 * @Description:支付参数  
	 * @param: @param payEntity
	 * @param: @param type
	 * @param: @return      
	 * @return: Map<String,String>      
	 * @throws
	 */
	private Map<String, String> getPayDataMap(PayEntity payEntity, String type) {
		String tranCode = payEntity.getOrderNo();// 订单号
		double amount = payEntity.getAmount();
		String tranAmt = new DecimalFormat("#").format(amount * 100);
		Map<String, String> data = new HashMap<String, String>();
		data.put("MERCHANT_ID", merchantId);// 商户号
		data.put("TRAN_CODE", tranCode);// 订单号
		data.put("TRAN_AMT", tranAmt);// 订单金额
		data.put("REMARK", "必付宝支付");// 商品描述
		data.put("NO_URL", noUrl);// 通知地址
		data.put("RET_URL", retUrl);// 返回地址
		data.put("SUBMIT_TIME", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));// 调用时间戳
		data.put("VERSION", version);// 版本
		
		if(PayChannel.BANK.getCode().equals(type.toUpperCase())){
			// 快捷支付或者网关支付
			data.put("BANK_ID", payEntity.getPayCode());// 银行代码
//			data.put("BANK_ID", "1002");// 银行代码
		}else{
			data.put("TYPE", payEntity.getPayCode());// 支付类型
			
//			if(PayType.WXBS.getCode().equals(payEntity.getPayCode())){
//				// 微信反扫时,该字段必填
//				data.put("AUTHCODE", "");
//			}
		}
		// 参数排序
		Map<String, String> treemap = new TreeMap<>(new Comparator<String>() {
			@Override
			public int compare(String key1, String key2) {
				return key1.compareTo(key2);
			}
		});
		treemap.putAll(data);

		return treemap;
	}
	
	/**
	 * 
	 * @Title: createPayForm @Description:创建支付form表单 @param: @return @return:
	 * String @throws
	 */
	private String createPayForm(JSONObject json) {
		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ payUrl + "\">";
		Iterator iterator = json.keys();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			String value = json.getString(key);// 这里可以根据实际类型去获取
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + value + "'>\r\n";
		}
		FormString += "</form></body>";
		System.out.println("天下科技支付表单:" + FormString);
		return FormString;
	}
}
