package com.cn.tianxia.pay.impl;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.XTUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName XTPayServiceImpl
 * @Description 信通支付
 * @author Hardy
 * @Date 2018年10月2日 下午4:28:39
 * @version 1.0.0
 */
public class XTPayServiceImpl implements PayService {

	// 日志
	private static final Logger logger = LoggerFactory.getLogger(XTPayServiceImpl.class);

	private String merId;// 商户在系统的唯一身份标识

	private String secret;// 秘钥

	private String notifyUrl;// 回调地址

	private String payUrl;// 支付地址

	// 构造器
	public XTPayServiceImpl(Map<String, String> data) {
		if (data != null && !data.isEmpty()) {
			if (data.containsKey("merId")) {
				this.merId = data.get("merId");
			}
			if (data.containsKey("secret")) {
				this.secret = data.get("secret");
			}
			if (data.containsKey("notifyUrl")) {
				this.notifyUrl = data.get("notifyUrl");
			}
			if (data.containsKey("payUrl")) {
				this.payUrl = data.get("payUrl");
			}
		}
	}

	/**
	 * 网银支付
	 */
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		logger.info("[XT]信通支付网银支付开始======================START=================");
		try {
			// 生成请求参数
			Map<String, String> data = sealRequest(payEntity);
			// 生成签名串
			String sign = generatorSign(data);
			data.put("hmac", sign);// 签名数据
			logger.info("[XT]信通支付生成最终请求参数:" + JSONObject.fromObject(data).toString());
			// 发起HTTP请求
			String response = HttpUtils.generatorForm(data, payUrl);
			return PayUtil.returnWYPayJson("success", "form", response, payEntity.getPayUrl(), "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("[XT]信通支付网银支付异常:" + e.getMessage());
			return PayUtil.returnPayJson("error", "2", "下单异常!", "", 0, "", "");
		}
	}

	/**
	 * 扫码支付
	 */
	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("[XT]信通支付扫码支付开始===============START========================");
		try {
			String mobile = payEntity.getMobile();
			String username = payEntity.getUsername();
			double amount = payEntity.getAmount();
			String order_no = payEntity.getOrderNo();

			// 生成请求参数
			Map<String, String> data = sealRequest(payEntity);
			// 生成签名串
			String sign = generatorSign(data);
			data.put("hmac", sign);// 签名数据
			logger.info("[XT]信通支付生成最终请求参数:" + JSONObject.fromObject(data).toString());
			// 发起HTTP请求
			if (StringUtils.isBlank(mobile)) { // 电脑PC版需要请求
				String response = HttpUtils.toPostForm(data, payUrl);
				logger.info("[XT]信通支付PC扫码端 请求返回结果:{}", response);
				if (StringUtils.isBlank(response)) {
					logger.error("[XT]信通支付发起HTTP请求无响应结果");
					return PayUtil.returnPayJson("error", "2", "下单失败，无响应结果", username, amount, order_no, response);
				}
				// 解析响应结果
				JSONObject jsonObject = JSONObject.fromObject(response);
				if (jsonObject.containsKey("status") && jsonObject.getString("status").equals("0")) {
					// 下单成功
					String payImg = jsonObject.getString("payImg");
					return PayUtil.returnPayJson("success", "2", "下单成功，生成二维码扫码图片", username, amount, order_no, payImg);
				}
				String Msg = jsonObject.getString("Msg");
				return PayUtil.returnPayJson("error", "2", "下单失败:" + Msg, username, amount, order_no, response);
			} else {
				// 手机支付 提交表单
				logger.info("[XT]信通支付 手机端参数:{},手机端请求路径:{}", data, payUrl);
				String formStr = HttpUtils.generatorForm(data, payUrl);
				return PayResponse.sm_form(payEntity, formStr, "下单成功!");
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("[XT]信通支付扫码支付异常:" + e.getMessage());
			return PayUtil.returnPayJson("error", "2", "下单异常!", "", 0, "", "");
		}
	}

	/**
	 * 回调
	 */
	@Override
	public String callback(Map<String, String> data) {
		logger.info("[XT]信通支付回调验签开始===============START=========================");
		try {
			// 获取回调验签原签名串
			String sourceSign = data.get("hmac");
			logger.info("[XT]信通支付回调验签原签名串:" + sourceSign);
			String regex = ".*\\d+.*";
			StringBuffer sb = new StringBuffer();

			Map<String, String> treemap = new TreeMap<>(new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});
			treemap.putAll(data);
			Iterator<String> iterator = treemap.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String val = treemap.get(key);
				if (key.matches(regex)) {
					sb.append(val);
				}
			}
			String signStr = sb.toString();
			logger.info("[XR]信通支付回调验签待签名串:" + signStr);
			String sign = XTUtils.hmacSign(signStr, secret);
			logger.info("[XT]信通支付回调验签生成加密签名串:" + sign);
			if (sourceSign.equalsIgnoreCase(sign))
				return "success";
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("[XT]信通支付回调验签异常:" + e.getMessage());
		}
		logger.info("[XT]信通支付回调验签失败!");
		return "";
	}

	/**
	 * 
	 * @Description 组装支付请求参数
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	private Map<String, String> sealRequest(PayEntity entity) throws Exception {
		logger.info("[XT]信通支付组装请求参数开始====================START===================");
		try {
			// 创建存储信通支付请求参数对象
			String amount = new BigDecimal(entity.getAmount()).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
			Map<String, String> data = new HashMap<>();
			data.put("p0_Cmd", "Buy");// 固定值“Buy”.
			data.put("p1_MerId", merId);// 商户在系统的唯一身份标识.获取方式请联系客服
			data.put("p2_Order", entity.getOrderNo());// 提交的订单号必须在自身账户交易中唯一。
			data.put("p3_Amt", amount);// 单位:元，精确到分.此参数为空则无法直连(如直连会报错：抱歉，交易金额太小。)
			data.put("p4_Cur", "CNY");// 固定值“CNY”.
			data.put("p5_Pid", "TOP-UP");// 用于支付时显示在网关左侧的订单产品信息.此参数如用到中文，请注意转码.
			data.put("p6_Pcat", "1");// 商品种类.
			data.put("p7_Pdesc", "TOP-UP");// 商品描述.此参数如用到中文，请注意转码.
			data.put("p8_Url", notifyUrl);// 支付成功后本系统会向该地址发送两次成功通知，该地址可以带参数，注意：如不填p8_Url的参数值支付成功后您将得不到支付成功的通知。
			data.put("pa_MP", "XR");// 返回时原样返回，此参数如用到中文，请注意转码.
			data.put("pd_FrpId", entity.getPayCode());// 该字段可依照附录:支付通道编码列表设置参数值.
			// 固定值为“1”:需要应答机制;收到服务器点对点支付成功通知，必须回写以“success”（无关大小写）开头的字符串，
			// 即使您收到成功通知时发现该订单已经处理过，也要正确回写“success”，
			// 否则将认为您的系统没有收到通知，启动重发机制，直到收到“success”为止。注：成功发送通知
			data.put("pr_NeedResponse", "1");
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("[XT]信通支付组装请求参数异常:" + e.getMessage());
			throw new Exception("组装信通请求参数异常!");
		}
	}

	/**
	 * 
	 * @Description 生成签名
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private String generatorSign(Map<String, String> data) throws Exception {
		logger.info("[XT]信通支付生成签名串开始======================START=====================");
		try {
			// 签名规则:
			// 参数1:STR，列表中的参数值按照签名顺序拼接所产生的字符串，注意null要转换为“”，并确保无乱码.
			// 参数2:商户密钥.请联系客服人员
			// 各语言范例已经提供封装好了的方法用于生成此参数。
			// 如果以上两个参数有错误，则该参数必然错误
			Map<String, String> treemap = new TreeMap<>(new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});
			treemap.putAll(data);
			// 生成待签名串
			StringBuffer sb = new StringBuffer();
			Iterator<String> iterator = treemap.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String val = treemap.get(key);
				if (StringUtils.isBlank(val) || key.equals("hmac"))
					continue;
				sb.append("&").append(key).append("=").append(val);
			}
			String signStr = sb.toString().replaceFirst("&", "");
			logger.info("[XT]信通支付生成待签名串:" + signStr);
			String sign = XTUtils.hmacSign(signStr, secret);
			logger.info("[XT]信通支付生成加密签名串:" + sign);
			return sign;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("[XT]信通支付生成签名串异常:" + e.getMessage());
			throw new Exception("生成签名串异常!");
		}
	}
}
