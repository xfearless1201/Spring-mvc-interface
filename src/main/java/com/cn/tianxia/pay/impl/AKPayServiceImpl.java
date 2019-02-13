package com.cn.tianxia.pay.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.IPTools;
import com.opentech.cloud.server.component.api.sdk.ApiClient;
import com.opentech.cloud.server.component.api.sdk.ApiClientFactory;
import com.opentech.cloud.server.component.api.sdk.ApiRequestBuilder;
import com.opentech.cloud.server.component.api.sdk.ApiResponse;

import net.sf.json.JSONObject;

public class AKPayServiceImpl implements PayService {
	/**
	 * 
	 */
	// 支付网关地址
	private static String apiUrl;
	// 商户号
	private static String merchantNo;
	// 货币类型
	private static String currency;
	// 接口版本
	private static String version;
	// 交易内容
	private static String content;
	// 证书编号
	private static String certNo;
	// 回调地址
	private static String callbackURL;
	// api接口方法
	private static String apiMethod;

	// api查询接口方法
	private static String apiQueryMethod;

	private final static Logger logger = LoggerFactory.getLogger(AKPayServiceImpl.class);

	private ApiClient apiClient;

	public AKPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			apiUrl = jo.getString("apiUrl");
			merchantNo = jo.getString("merchantNo");
			currency = jo.getString("currency");
			version = jo.getString("version");
			content = jo.getString("content");
			certNo = jo.getString("certNo");
			callbackURL = jo.getString("callbackURL");
			apiMethod = jo.getString("apiMethod");
			apiQueryMethod = jo.getString("apiQueryMethod");
		}
		apiClient = ApiClientFactory.newClient(apiUrl, merchantNo, certNo);
	}

	/**
	 * pc扫码接口
	 * 
	 * @return
	 */
	public JSONObject pcScanPay(Map<String, String> scanMap) {
		JSONObject json = new JSONObject();
		// this.apiClient = ApiClientFactory.newClient(apiUrl, merchantNo,
		// certNo);
		// 创建交易
		// 构建API请求, API名称, API版本号
		ApiRequestBuilder builder = ApiRequestBuilder.newInstance(apiMethod, version);
		// 添加参数 商户号 必须
		builder.addParameter("merchantNo", merchantNo);
		// 添加参数 外部订单号 必须
		builder.addParameter("outTradeNo", scanMap.get("outTradeNo"));
		// 添加参数 货币类型 非必须, CNY 会USD
		builder.addParameter("currency", currency);
		// 添加参数 交易金额 必须
		builder.addParameter("amount", Long.parseLong(scanMap.get("amount")));
		// 支付类型
		builder.addParameter("payType", scanMap.get("payType"));
		// 添加参数 交易内容 必须
		builder.addParameter("content", content);
		// 添加参数 外部上下文 非必须
		// builder.addParameter("outContext", "my_context");
		// 添加参数 交易状态回调地址 必须
		builder.addParameter("callbackURL", callbackURL);
		builder.addParameter("returnURL", scanMap.get("returnURL"));

		ApiResponse response = null;
		// 同步调用
		response = this.apiClient.invoke(builder.build());
		// 判断是否成功
		if (response.isSucceed()) {
			// 调用成功 response.getData(Map.class) 返回结果
			json = JSONObject.fromObject(response.getData(Map.class));
			json.put("ak47", "success");
		} else {
			// 发生错误
			json.put("ak47", "error");
			json.put("msg", "error: " + response.getErrorCode() + "/" + response.getMsg());
		}
		return json;
	}

	/**
	 * 手机扫码接口
	 * 
	 * @return
	 */
	public JSONObject mbScanPay(Map<String, String> scanMap) {
		JSONObject json = new JSONObject();
		// this.apiClient = ApiClientFactory.newClient(apiUrl, merchantNo,
		// certNo);
		// 创建交易
		// 构建API请求, API名称, API版本号
		ApiRequestBuilder builder = ApiRequestBuilder.newInstance(apiMethod, version);
		// 添加参数 商户号 必须
		builder.addParameter("merchantNo", merchantNo);
		// 添加参数 外部订单号 必须
		builder.addParameter("outTradeNo", scanMap.get("outTradeNo"));
		// 添加参数 货币类型 非必须, CNY 会USD
		builder.addParameter("currency", currency);
		// 添加参数 交易金额 必须
		builder.addParameter("amount", Long.parseLong(scanMap.get("amount")));
		// 支付类型
		builder.addParameter("payType", scanMap.get("payType"));
		// 添加参数 交易内容 必须
		builder.addParameter("content", content);
		// 添加参数 外部上下文 非必须
		// builder.addParameter("outContext", "my_context");
		// 添加参数 交易状态回调地址 必须
		builder.addParameter("callbackURL", callbackURL);
		builder.addParameter("returnURL", scanMap.get("returnURL"));

		ApiResponse response = null;
		// 同步调用
		response = this.apiClient.invoke(builder.build());
		// 判断是否成功
		if (response.isSucceed()) {
			// 调用成功 response.getData(Map.class) 返回结果

			json = JSONObject.fromObject(response.getData(Map.class));
			json.put("ak47", "success");
		} else {
			// 发生错误
			json.put("ak47", "error");
			json.put("msg", "error: " + response.getErrorCode() + "/" + response.getMsg());
		}
		return json;
	}

	public JSONObject orderQuery(String outTradeNo) {

		JSONObject json = new JSONObject();
		// 查询交易 API名称, API版本
		ApiRequestBuilder builder = ApiRequestBuilder.newInstance(apiQueryMethod, version);
		// 添加参数 商户号
		builder.addParameter("merchantNo", merchantNo);
		// 添加参数 外部订单号
		builder.addParameter("outTradeNo", outTradeNo);

		ApiResponse response = null;
		// 同步调用
		response = this.apiClient.invoke(builder.build());
		// 判断是否成功
		if (response.isSucceed()) {
			// 调用成功 response.getData(Map.class) 返回结果
			json = JSONObject.fromObject(response.getData(Map.class));
			json.put("ak47", "success");
			logger.info("订单查询成功" + json);
		} else {

			// 发生错误
			json.put("ak47", "error");
			json.put("msg", "error: " + response.getErrorCode() + "/" + response.getMsg());
			logger.info("订单查询失败:" + json);
		}
		return json;
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

		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("outTradeNo", order_no);// 订单号
		int int_amount = (int) (amount * 100);
		scanMap.put("amount", String.valueOf(int_amount));// 金额
		scanMap.put("returnURL", refereUrl);
		scanMap.put("payType", pay_code);
		JSONObject xj_json = null;
		if (StringUtils.isNullOrEmpty(mobile)) {
			// pc端
			xj_json = pcScanPay(scanMap);
		} else {
			// 手机端
			xj_json = mbScanPay(scanMap);
		}
		String html = "";
		// 状态success表示获取请求成功
		if ("success".equals(xj_json.getString("ak47"))) {
			html = xj_json.getString("paymentInfo");
		} else {
			// 返回错误信息
			return PayUtil.returnWYPayJson("error", "form", html, pay_url, "");
		}

		return PayUtil.returnWYPayJson("success", "link", html, pay_url, "");
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
		scanMap.put("outTradeNo", order_no);// 订单号
		int int_amount = (int) (amount * 100);
		scanMap.put("amount", String.valueOf(int_amount));// 金额
		scanMap.put("returnURL", refereUrl);
		scanMap.put("payType", pay_code);

		JSONObject xj_json = null;
		boolean mobileFalg = false;
		if (StringUtils.isNullOrEmpty(mobile)) {
			// pc端
			xj_json = pcScanPay(scanMap);
		} else {
			// 手机端
			mobileFalg = true;
			xj_json = mbScanPay(scanMap);
		}
		String qrcode = "";
		// 状态success表示获取请求成功
		if ("success".equals(xj_json.getString("ak47"))) {
			qrcode = xj_json.getString("paymentInfo");
		} else {
			// 返回错误信息
			return PayUtil.returnPayJson("error", "4", xj_json.toString(), userName, amount, order_no, "");
		}
		// 区别pc和手机端返回类型
		if (mobileFalg) {
			// 手机端返回
			return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no, qrcode);
		} else {
			// pc端返回
			return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no, qrcode);
		}
	}

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }

}
