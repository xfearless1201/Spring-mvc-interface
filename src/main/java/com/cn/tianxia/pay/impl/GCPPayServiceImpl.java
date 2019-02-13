package com.cn.tianxia.pay.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

import net.sf.json.JSONObject;

/**
 * 港创个人收款码
 * 
 * @author john
 *
 */
public class GCPPayServiceImpl implements PayService {
	/** 商户ID ***/
	private String mid;
	/** 异步通知的地址 ***/
	private String notifyUrl;
	/** 订单标题 ***/
	private String subject;
	/** 支付地址 ***/
	private String payUrl ;
	/** 测试密钥 ***/
	private String singKey ;

	private final static Logger logger = LoggerFactory.getLogger(GCPPayServiceImpl.class);

	public GCPPayServiceImpl(Map<String, String> pmap) {
		net.sf.json.JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			mid = jo.get("mid").toString();
			singKey = jo.get("singKey").toString();
			notifyUrl = jo.get("notifyUrl").toString();
			payUrl = jo.get("payUrl").toString();
			subject = jo.get("subject").toString();
		}
	}

	/**
	 * 个人收款码
	 * 
	 * @param map
	 * @return
	 */
	public JSONObject PersonalcodeScan(Map<String, String> scanMap) {

		String returnUrl = scanMap.get("returnUrl");

		String type = scanMap.get("type");
		// 订单号
		String orderNo = scanMap.get("orderNo");
		// 随机数
		String noise = TfcpayUtil.nextUUID();
		Map<String, Object> pay = new HashMap<>();

		// 金额位元,如需转换参考BigDecimalUtil工具类
		pay.put("amount", scanMap.get("amount"));
		// 商户号，通过配置获取，只做参考
		pay.put("mid", mid);
		// 订单号随机一下，防止订单重复
		pay.put("orderNo", orderNo);
		pay.put("subject", subject);
		pay.put("payType", type);
		// 通知地址
		pay.put("notifyUrl", notifyUrl);
		// 通知URL
		pay.put("returnUrl", returnUrl);
		// 随机字符串,
		pay.put("noise", noise);
		Map<String, String> data = null;

		data = TfcpayUtil.flattenParamsAndSign(pay, singKey);
		String result;
		try {
			result = HttpUtil.post(payUrl + "/personalCode/qrCode", data);
			logger.info("请求返回内容\n" + result);
			Map<String, String> resultMap = TfcpayUtil.parseResult(result);
			// 输入结果
			return ResultMain(resultMap);
		} catch (IOException e) {
			e.printStackTrace();
			return getReturnJson("error", "", "请求异常");
		}

	}

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

		Map<String, String> scanMap = new HashMap<>();

		scanMap.put("returnUrl", refereUrl);
		scanMap.put("type", pay_code);
		scanMap.put("orderNo", order_no);
		scanMap.put("amount", String.valueOf(amount));
		JSONObject rjson = null;
		rjson = PersonalcodeScan(scanMap);

		if (!"success".equals(rjson.getString("status"))) {
			return PayUtil.returnPayJson("error", "1", rjson.getString("msg"), userName, amount, order_no, "");
		}
		return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no,
				rjson.getString("qrCode"));
	}

}
