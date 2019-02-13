package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.qyf.util.ToolKit;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

public class BFPayServiceImpl implements PayService {
	private String merNo;
	private String key;
	private String reqUrl;
	private String goodsName;
	private String callBackUrl;

	private final static Logger logger = LoggerFactory.getLogger(BFPayServiceImpl.class);

	public static void main(String[] args) {
		Map<String, String> map = new HashMap<>();
		map.put("merNo", "DR180309152240127");
		map.put("key", "D00DA444CB1B62AC2604C217841CF203");
		/** 测试地址 **/
		// map.put("reqUrl", "http://120.79.190.87/api/smPay.action");
		/** 正式地址 **/
		map.put("reqUrl", "http://defray.948pay.com:8188/api/smPay.action");
		map.put("goodsName", "TXWL");
		map.put("callBackUrl", "http://182.16.110.186:8080/XPJ/PlatformPay/BFNotify.do");
		logger.info("JSON配置:" + JSONObject.fromObject(map));
		BFPayServiceImpl bf = new BFPayServiceImpl(map);

		Map<String, String> scanMap = new HashMap<>();
		int int_amount = (int) (1 * 100);
		scanMap.put("payAmount", String.valueOf(int_amount));// 订单金额
		scanMap.put("orderNum", "JCFbl1201803171006151006159572");
		scanMap.put("netwayCode", "QQ");
		scanMap.put("requestIP", "110.164.197.124");// 终端IP地址，传自己本地的IP公网地址
		scanMap.put("frontBackUrl", "http://defray.948pay.com:8188/");// 同步通知地址

		bf.scanPay(scanMap);
	}

	public BFPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			merNo = jo.get("merNo").toString();// 商户号
			key = jo.get("key").toString();// 商户密钥
			reqUrl = jo.get("reqUrl").toString();// 请求网关地址
			goodsName = jo.get("goodsName").toString();// 商品名称
			callBackUrl = jo.get("callBackUrl").toString();// 回调地址
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
		String ip = payEntity.getIp();
		String mobile = payEntity.getMobile();

		Map<String, String> scanMap = new HashMap<>();
		int int_amount = (int) (amount * 100);
		scanMap.put("payAmount", String.valueOf(int_amount));// 订单金额
		scanMap.put("orderNum", order_no);
		scanMap.put("netwayCode", pay_code);
		// TODO
		scanMap.put("requestIP", ip);// 终端IP地址，传自己本地的IP公网地址 "110.164.197.124"
		scanMap.put("frontBackUrl", refereUrl);// 同步通知地址

		JSONObject rjson = null;
		rjson = scanPay(scanMap);
		if ("success".equals(rjson.getString("status"))) {
			/** pc端 **/
			if (StringUtils.isNullOrEmpty(mobile)) {
				return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no,
						rjson.getString("qrCode"));
			} else {
				/** 手机端 **/
				return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
						rjson.getString("qrCode"));
			}
		} else {
			return PayUtil.returnPayJson("error", "4", rjson.getString("msg"), userName, amount, order_no, "");
		}
	}

	/**
	 * 支付方法
	 */
	public JSONObject scanPay(Map<String, String> payMap) {
		Map<String, String> metaSignMap = new TreeMap<String, String>();
		metaSignMap.put("merchantNo", merNo);
		metaSignMap.put("netwayCode", payMap.get("netwayCode"));// 网关代码
		metaSignMap.put("randomNum", ToolKit.randomStr(4));// 4位随机数
		// String orderNum = new
		// SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()); // 20位
		// orderNum += ToolKit.randomStr(3);
		DecimalFormat decimalFormat = new DecimalFormat("###########");
		String payAmount = String.valueOf(decimalFormat.format(Double.parseDouble(payMap.get("payAmount"))));// 订单金额
		logger.info("订单金额:" + payAmount);
		metaSignMap.put("orderNum", payMap.get("orderNum"));
		metaSignMap.put("payAmount", payAmount);// 单位:分
		metaSignMap.put("goodsName", goodsName);// 商品名称：20位
		metaSignMap.put("callBackUrl", callBackUrl);// 回调地址
		metaSignMap.put("frontBackUrl", payMap.get("frontBackUrl"));// 回显地址
		metaSignMap.put("requestIP", payMap.get("requestIP"));// 客户ip地址
		String metaSignJsonStr = JSONObject.fromObject(metaSignMap).toString();
		String sign = ToolKit.MD5(metaSignJsonStr + key, "UTF-8");// 32位
		logger.info("sign=" + sign); // 英文字母大写
		metaSignMap.put("sign", sign);
		String reqparam = "paramData=" + JSONObject.fromObject(metaSignMap).toString();
		String resultJsonStr = ToolKit.request(reqUrl, reqparam);

		// 检查状态
		JSONObject resultJsonObj = JSONObject.fromObject(resultJsonStr);
		String stateCode = resultJsonObj.getString("resultCode");
		if (!stateCode.equals("00")) {
			logger.info("佰富订单提交失败");
			return getReturnJson("error", "", resultJsonStr);
		}
		String resultSign = resultJsonObj.getString("sign");
		resultJsonObj.remove("sign");
		String targetString = ToolKit.MD5(resultJsonObj.toString() + key, "UTF-8");
		if (targetString.equals(resultSign)) {
			logger.info("佰富签名校验成功");
			return getReturnJson("success", resultJsonObj.getString("CodeUrl"), "接口获取成功！");

		} else {
			logger.info("佰富签名校验失败");
			return getReturnJson("error", "", resultJsonStr);
		}
	}

	/**
	 * 结果返回
	 * 
	 * @param status
	 * @param qrCode
	 * @param msg
	 * @return
	 */
	private JSONObject getReturnJson(String status, String qrCode, String msg) {
		JSONObject json = new JSONObject();
		json.put("status", status);
		json.put("qrCode", qrCode);
		json.put("msg", msg);
		return json;
	}

    @Override
    public String callback(Map<String, String> data) {
        
        return "success";
    }

	// public String callback(Map) {
	// String data = request.getParameter("paramData");
	// JSONObject jsonObj = JSONObject.fromObject(data);
	// MaP<String, String> metaSignMaP = new TreeMaP<String, String>();
	// metaSignMaP.Put("merchantNo", jsonObj.getString("merchantNo"));
	// metaSignMaP.Put("netwayCode", jsonObj.getString("netwayCode"));
	// metaSignMaP.Put("orderNum", jsonObj.getString("orderNum"));
	// metaSignMaP.Put("amount", jsonObj.getString("amount"));
	// metaSignMaP.Put("goodsName", jsonObj.getString("goodsName"));
	// metaSignMaP.Put("resultCode", jsonObj.getString("resultCode"));// 支付状态
	// metaSignMaP.Put("payDate", jsonObj.getString("payDate"));// yyyy-MM-dd
	// // HH:mm:ss
	// String jsonStr = maPToJson(metaSignMaP);
	// String sign = MD5(jsonStr.toString() + key, "UTF-8");
	// if (!sign.equals(jsonObj.getString("sign"))) {
	// System.out.Println("签名校验失败");
	// return;
	// }
	// System.out.Println("签名校验成功");
	// resPonse.getOutPutStream().write("000000".getBytes());// 强制要求返回000000
	// }

}
