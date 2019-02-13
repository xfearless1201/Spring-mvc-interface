package com.cn.tianxia.pay.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gc.util.TfcpayBussinessException;
import com.cn.tianxia.pay.gc.util.TfcpaySignException;
import com.cn.tianxia.pay.gcc.util.HttpUtil;
import com.cn.tianxia.pay.gcc.util.TfcpayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;

public class GCPayServiceImpl implements PayService {
	/** 测试商户号 **/
	private String mid;
	/** 测试密钥 ***/
	private String singKey;
	/***
	 * 通知地址,运行本项目起来以后会有一个接受通知的测试，请修改为外网可访问地址
	 */
	private String NOTIFYURL;
	/*** 测试地址 */
	private String DEV_DOMAIN;
	/** 网银支付请求地址 */
	private String NETPAY_URL = DEV_DOMAIN + "/netpay";
	/** 扫码支付地址 **/
	private String PAYCODE_URL;
	/** 卡类型，01（储蓄卡）、02（信用卡） **/
	private String cardType;
	/** 01（B2C-API）、02（B2C-收银台）、 03（B2B-API）、 04（B2B-收银台）、默认：01 **/
	private String businessType;
	/** 商品的具体描述 **/
	private String body;
	/** 商品名称 **/
	private String subject;
	/*** 备注 **/
	private String remark;

	/** 微信H5支付请求地址 */
	private String WECHAT_H5_URL = DEV_DOMAIN + "/pay/wap/wechat";

	/*** 代付类型 **/
	private String type;

	/** 卡帐户类型 */
	private String cardAccountType;

	/** 代付请求地址 */
	private String generationType = DEV_DOMAIN + "/defray";

	private final static Logger logger = LoggerFactory.getLogger(GCPayServiceImpl.class);

	public GCPayServiceImpl(Map<String, String> pmap) {
		net.sf.json.JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			mid = jo.get("mid").toString();
			singKey = jo.get("singKey").toString();
			NOTIFYURL = jo.get("NOTIFYURL").toString();
			DEV_DOMAIN = jo.get("DEV_DOMAIN").toString();
			PAYCODE_URL = jo.get("PAYCODE_URL").toString();
			NETPAY_URL = jo.get("NETPAY_URL").toString();
			cardType = jo.get("cardType").toString();
			businessType = jo.get("businessType").toString();
			body = jo.get("body").toString();
			subject = jo.get("subject").toString();
			remark = jo.get("remark").toString();
			WECHAT_H5_URL = jo.get("WECHAT_H5_URL").toString();
			boolean aa = jo.get("type") == null ? false : true;
			if (aa) {
				type = jo.get("type").toString();
			}
			boolean bb = jo.get("cardAccountType") == null ? false : true;
			if (bb) {
				cardAccountType = jo.get("cardAccountType").toString();
			}
			boolean cc = jo.get("generationType") == null ? false : true;
			if (cc) {
				generationType = jo.get("generationType").toString();
			}
		}
	}

	/**
	 * 网银接口
	 * 
	 * @param banMap
	 * @return
	 */
	public String bankPay(Map<String, String> bankMap) {
		// String mid = MID; // 商户号
		String orderNo = bankMap.get("orderNo"); // 合作伙伴系统中的订单号
		String returnUrl = bankMap.get("returnUrl"); // 用于支付完成后跳转到商户网站指定的地址
		String amount = bankMap.get("amount"); // 订单金额
		String channel = bankMap.get("channel"); // 来源类型
		String bankCode = bankMap.get("bankCode"); // 业务类型为收银台,为空。其他必填
		String notifyUrl = NOTIFYURL; // 针对该交易的交易状态同步通知接收URL
		String currencyType = "CNY"; // 货币类型
		String noise = TfcpayUtil.nextUUID(); // 随机字符串

		Map<String, String> data = new HashMap<String, String>();
		data.put("mid", mid);
		data.put("orderNo", orderNo);
		data.put("amount", amount);
		data.put("bankCode", bankCode);
		data.put("notifyUrl", notifyUrl);
		data.put("returnUrl", returnUrl);
		data.put("currencyType", currencyType);
		data.put("subject", subject);
		data.put("body", body);
		data.put("cardType", cardType);
		data.put("channel", channel);
		data.put("businessType", businessType);
		data.put("remark", remark);
		data.put("noise", noise);
		String sign = TfcpayUtil.generateMD5(data, singKey);
		data.put("sign", sign);

		if (data.isEmpty()) {
			return "参数不能为空！";
		}
		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ NETPAY_URL + "\">";
		for (String key : data.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + data.get(key) + "'>\r\n";
		}

		FormString += "</form></body>";

		logger.info("GC表单:\n" + FormString);
		return FormString;
	}

	/**
	 * 扫码接口
	 * 
	 * @param scanMap
	 * @return
	 */
	public JSONObject scanPay(Map<String, String> scanMap) {
		// 支付类型 wechat:微信 alipay:支付宝 QQwallet:QQ钱包
		String type = scanMap.get("type");
		// 订单号
		String orderNo = scanMap.get("orderNo");
		// 随机数
		String noise = TfcpayUtil.nextUUID();
		Map<String, Object> pay = new HashMap<>();
		// 商户号，通过配置获取，只做参考
		pay.put("mid", mid);
		// 订单号随机一下，防止订单重复
		pay.put("orderNo", orderNo);
		pay.put("subject", subject);
		pay.put("body", body);
		// 金额位元,如需转换参考BigDecimalUtil工具类
		pay.put("amount", scanMap.get("amount"));
		pay.put("type", type);
		// 通知地址
		pay.put("notifyUrl", NOTIFYURL);
		// 随机字符串,
		pay.put("noise", noise);
		Map<String, String> data = null;
		try {
			// 生成sign, 并转Map<String,Object> 为Map<String,String>
			data = TfcpayUtil.flattenParamsAndSign(pay, singKey);
			String result = HttpUtil.post(PAYCODE_URL, data);
			logger.info("请求返回内容\n" + result);
			Map<String, String> resultMap = TfcpayUtil.parseResult(result);
			// 输入结果
			return ResultMain(resultMap);

		} catch (Exception e) {
			logger.info("请求异常");
			e.printStackTrace();
			return getReturnJson("error", "", "请求异常");
		}
	}

	/**
	 * 微信h5接口
	 * 
	 * @param wxMap
	 * @return
	 */
	public JSONObject wechatH5(Map<String, String> wxMap) {
		// 随机数
		String noise = TfcpayUtil.nextUUID();
		Map<String, Object> params = new TreeMap<>();
		params.put("orderNo", wxMap.get("orderNo"));
		params.put("amount", wxMap.get("amount"));
		params.put("notifyUrl", NOTIFYURL);
		params.put("ip", wxMap.get("ip"));
		params.put("callbackUrl", wxMap.get("returnUrl"));
		params.put("deviceInfo", wxMap.get("deviceInfo"));
		params.put("mid", mid);
		params.put("subject", subject);
		params.put("body", body);
		params.put("mchAppId", "https://m.jd.com");
		params.put("mchAppName", "https://m.jd.com");
		params.put("noise", noise);// noise
		// TfcpayUtil.sendTo(params, WECHAT_H5_URL, singKey); // 发送HTTP请求

		try {
			Map<String, String> data = TfcpayUtil.flattenParamsAndSign(params, singKey);
			String result = HttpUtil.post(WECHAT_H5_URL, data);
			logger.info("请求返回内容\n" + result);
			Map<String, String> resultMap = TfcpayUtil.parseResult(result);

			// 先判断通信状态
			if (!"SUCCESS".equals(resultMap.get("code"))) {
				logger.info("通信状态失败,");
				return getReturnJson("error", "", resultMap.get("errCodeDes"));
			}
			// 校验签名
			try {
				TfcpayUtil.VerifySign(resultMap, singKey);
			} catch (TfcpaySignException e) {
				logger.info(e.getMessage());
				return getReturnJson("error", "", resultMap.get("errCodeDes"));
			} catch (TfcpayBussinessException e) {
				logger.info("业务异常" + e.getMessage());
				e.printStackTrace();
				return getReturnJson("error", "", resultMap.get("errCodeDes"));
			}
			// 判断业务状态
			if ("SUCCESS".equals(resultMap.get("resultCode"))) {
				logger.info("操作成功!!");
				String payInfo = resultMap.get("payInfo");
				return getReturnJson("success", payInfo, "获取二维码连接成功！");
			} else {
				logger.info("操作失败!!!\n失败原因:" + resultMap.get("errCodeDes"));
				return getReturnJson("error", "", resultMap.get("errCodeDes"));
			}

		} catch (Exception e) {
			logger.info("请求异常");
			e.printStackTrace();
			return getReturnJson("error", "", "请求异常");
		}
	}

	/***
	 * 扫码接口返回
	 * 
	 * @param resultMap
	 */
	public JSONObject ResultMain(Map<String, String> resultMap) {
		try {
			// 对返回结果进行验签
			if (TfcpayUtil.VerifySign(resultMap, singKey)) {
				// 验签成功
				String code = resultMap.get("code");
				String resultCode = resultMap.get("resultCode");
				if ("SUCCESS".equals(code) && "SUCCESS".equals(resultCode)) {
					// 业务正常，巴拉巴拉获取想要的内容
					// 二维码
					String qrCode = resultMap.get("qrCode");
					// 交易金额:注意,交易金额为元.角分格式，转换需注意,可参考转换工具类BigDecimalUtil
					// BigDecimal amount = new
					// BigDecimal(String.valueOf(resultMap.get("amount")));
					logger.info("----------------成功输出-------------");
					logger.info("内容:\n" + JSONObject.fromObject(resultMap));
					logger.info("-----------------------------");
					return getReturnJson("success", qrCode, "二维码获取成功！");

				} else {
					String errCode = resultMap.get("errCode");
					String errCodeDes = resultMap.get("errCodeDes");
					logger.info("----------------失败输出-------------");
					logger.info("--errCode:" + errCode);
					logger.info("--errCodeDes:" + errCodeDes);
					logger.info("内容\n" + JSONObject.fromObject(resultMap));
					logger.info("-----------------------------------");
					return getReturnJson("error", "", errCodeDes);
				}
			} else {
				logger.info("验签失败-------------");
				return getReturnJson("error", "", "验签失败");
			}
		} catch (TfcpaySignException e) {
			logger.info(e.getMessage());
			logger.info(e.getParam());
			e.printStackTrace();
			return getReturnJson("error", "", "TfcpaySignException");
		} catch (TfcpayBussinessException e) {
			logger.info("业务异常-------------" + e.getMessage());
			e.printStackTrace();
			return getReturnJson("error", "", "TfcpayBussinessException");
		}
	}

	/**
	 * 回调验证签名
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public String callback(Map<String, String> map) {
		try {
			if (TfcpayUtil.VerifySign(map, singKey)) {
				return "success";
			} else {
				return "";
			}
		} catch (TfcpaySignException | TfcpayBussinessException e) {
			logger.info("GC签名异常");
			e.printStackTrace();
			return "";
		}

	}

	/**
	 * 返回数据格式Json
	 * 
	 * @param status
	 * @param qrCode
	 * @param msg
	 * @return
	 */
	public JSONObject getReturnJson(String status, String qrCode, String msg) {
		JSONObject json = new JSONObject();
		json.put("status", status);
		json.put("qrCode", qrCode);
		json.put("msg", msg);
		return json;
	}

	/**
	 * 数据转换
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> flattenParams(Map<String, Object> params) throws Exception {
		if (params == null) {
			return new HashMap<String, String>();
		}
		Map<String, String> flatParams = new HashMap<String, String>();
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof Map<?, ?>) {
				Map<String, Object> flatNestedMap = new HashMap<String, Object>();
				Map<?, ?> nestedMap = (Map<?, ?>) value;
				for (Map.Entry<?, ?> nestedEntry : nestedMap.entrySet()) {
					flatNestedMap.put(String.format("%s[%s]", key, nestedEntry.getKey()), nestedEntry.getValue());
				}
				flatParams.putAll(flattenParams(flatNestedMap));
			} else if (value instanceof ArrayList<?>) {
				ArrayList<?> ar = (ArrayList<?>) value;
				Map<String, Object> flatNestedMap = new HashMap<String, Object>();
				int size = ar.size();
				for (int i = 0; i < size; i++) {
					flatNestedMap.put(String.format("%s[%d]", key, i), ar.get(i));
				}
				flatParams.putAll(flattenParams(flatNestedMap));
			} else if ("".equals(value)) {

			} else if (value == null) {
				flatParams.put(key, "");
			} else {
				flatParams.put(key, value.toString());
			}
		}
		return flatParams;
	}

	public static void main(String[] args) {
		Map<String, Object> pay = new HashMap<>();
		pay.put("mid", "812017050323777"); // 商户号，通过配置获取，只做参考
		pay.put("orderNo", TfcpayUtil.nextUUID()); // 商户订单号(订单号必须保证唯一性)
													// 这里用随机数随机生成一个
		pay.put("amount", "10.00"); // 订单金额 代付金额 单位为元 格式为0.00 生产环境有代付最小金额限制
									// 详情咨询运营
		pay.put("receiveName", "张三"); // 收款人姓名
		pay.put("openProvince", "广西"); // 开户省
		pay.put("openCity", "北海市"); // 开户市
		pay.put("bankCode", "105"); // 收款银行编码 见 银行简码
		pay.put("bankBranchName", "中国工商银行北海分行云南路支行"); // 支行名称
		pay.put("cardNo", "8011101509400060000"); // 卡号
		pay.put("type", "02"); // 01 普通 02 额度
		pay.put("noise", TfcpayUtil.nextUUID()); // 随机字符串,不长于32位
		pay.put("cardAccountType", "01"); // 01 个人 02 企业 01即为对私 02即为对公
		// pay.put("bankLinked", "102623053001"); //联行号 对私账户可以不传,对公账户必传
		// 当然对私传了也无所谓 http://posp.cn可做参考

		Map<String, String> data = null;
		try {
			// 生成sign, 并转Map<String,Object> 为Map<String,String>
			data = TfcpayUtil.flattenParamsAndSign(pay, "ddbax6n4cg8qj958ytt6");
			// 发送HTTP请求
			String result = HttpUtil.post("https://devapi.tfcpay.com/v2/defray", data);
			System.out.println("代付申请请求返回内容如下:\n" + result);
			// 将返回结果的JSON字符串转为Map方便取数据
			Map<String, String> resultMap = TfcpayUtil.parseResult(result);
			// 根据返回的内容进行业务处理
			// bussinessHandle(resultMap);
		} catch (IOException e) {
			System.out.println("请求异常");
			e.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 业务处理
	 * 
	 * @param resultMap
	 */
	public JSONObject bussinessHandle(Map<String, String> resultMap) {
		try {
			// 对返回结果进行验签 验签失败会丢出异常
			TfcpayUtil.VerifySign(resultMap, singKey);
			// 如果代码执行到这里说明验签成功
			String code = resultMap.get("code");
			String resultCode = resultMap.get("resultCode");
			if ("SUCCESS".equals(code) && "SUCCESS".equals(resultCode)) {
				// 业务正常，巴拉巴拉获取想要的内容
				System.out.println("----------------正常业务输出-------------");
				System.out.println("内容\n" + resultMap.toString());
				System.out.println("-----------------------------");
				// 内容已经有了 要做什么请发挥你的想象力
				return null;
			}
		} catch (TfcpaySignException e) {
			System.out.println(e.getMessage());
			System.out.println(e.getParam());
			e.printStackTrace();
		} catch (TfcpayBussinessException e) {
			System.out.println("业务异常-------------" + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public JSONObject defray(Map<String, String> gcMap) {
		Map<String, Object> pay = new HashMap<>();
		pay.put("mid", mid); // 商户号，通过配置获取，只做参考
		pay.put("orderNo", gcMap.get("orderNo")); // 商户订单号(订单号必须保证唯一性)
													// 这里用随机数随机生成一个
		pay.put("amount", gcMap.get("amount")); // 订单金额 代付金额 单位为元 格式为0.00
												// 生产环境有代付最小金额限制 详情咨询运营
		pay.put("receiveName", gcMap.get("receiveName")); // 收款人姓名
		pay.put("openProvince", gcMap.get("openProvince")); // 开户省
		pay.put("openCity", gcMap.get("openCity")); // 开户市
		pay.put("bankCode", gcMap.get("bankCode")); // 收款银行编码 见 银行简码
		pay.put("bankBranchName", gcMap.get("bankBranchName")); // 支行名称
		pay.put("cardNo", gcMap.get("cardNo")); // 卡号
		pay.put("type", type); // 01 普通 02 额度
		pay.put("noise", gcMap.get("noise")); // 随机字符串,不长于32位
		pay.put("cardAccountType", cardAccountType); // 01 个人 02 企业 01即为对私
														// 02即为对公

		Map<String, String> data = null;
		try {
			// 生成sign, 并转Map<String,Object> 为Map<String,String>
			data = TfcpayUtil.flattenParamsAndSign(pay, singKey);
			// 发送HTTP请求
			String result = HttpUtil.post(generationType, data);
			System.out.println("代付申请请求返回内容如下:\n" + result);
			// 将返回结果的JSON字符串转为Map方便取数据
			Map<String, String> resultMap = TfcpayUtil.parseResult(result);
			// 根据返回的内容进行业务处理
			return bussinessHandle(resultMap);
		} catch (IOException e) {
			System.out.println("请求异常");
			e.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
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
		// String orderNo = TfcpayUtil.nextUUID();
		String orderNo = order_no;
		bankMap.put("bankCode", pay_code);
		bankMap.put("orderNo", orderNo);
		bankMap.put("amount", String.valueOf(amount));
		bankMap.put("returnUrl", refereUrl);
		// 手机端 or pc端 01（PC端）、 02（手机端）
		if (StringUtils.isNullOrEmpty(mobile)) {
			bankMap.put("channel", "01");
		} else {
			bankMap.put("channel", "02");
		}
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
		scanMap.put("orderNo", order_no);
		scanMap.put("amount", String.valueOf(amount));
		JSONObject rjson = null;
		// pc端
		if (StringUtils.isNullOrEmpty(mobile)) {
			scanMap.put("type", pay_code);
			rjson = scanPay(scanMap);
		} else {
			// 手机端 只支持微信h5
			scanMap.put("ip", ip);
			scanMap.put("returnUrl", refereUrl);
			// 应用类型OS_SDK,AND_SDK,iOS_WAP,AND_WAP
			// 暂时只支持iOS_WAP/AND_WAP
			scanMap.put("deviceInfo", "AND_WAP");
			rjson = wechatH5(scanMap);
		}
		if (!"success".equals(rjson.getString("status"))) {
			return PayUtil.returnPayJson("error", "1", rjson.getString("msg"), userName, amount, order_no, "");
		}
		// 手机 or pc 返回类型
		if (StringUtils.isNullOrEmpty(mobile)) {
			return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no,
					rjson.getString("qrCode"));
		} else {
			return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
					rjson.getString("qrCode"));
		}
	}
}
