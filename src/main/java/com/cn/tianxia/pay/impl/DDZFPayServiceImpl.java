package com.cn.tianxia.pay.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.ddzf.util.ConfigUtils;
import com.cn.tianxia.pay.ddzf.util.HttpUtil;
import com.cn.tianxia.pay.ddzf.util.RSAUtil;
import com.cn.tianxia.pay.ddzf.util.SignUtils;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * @ClassName DDZFPayServiceImpl
 * @Description 咚咚支付
 * @author zw
 * @Date 2018年5月27日 下午5:16:24
 * @version 1.0.0
 */
public class DDZFPayServiceImpl implements PayService {

	private final static Logger logger = LoggerFactory.getLogger(DDZFPayServiceImpl.class);

	/** 私钥 */
	private String privateKey;// =
								// "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMIgO3BitJ7NRuif3P9ywPb1/Dhy7CovHvD+bQe4mx1+tbvESmu9TqBs3+2psYZ/jRXlNo7ogsMq4E4wtPGXjalYQW+ooW7eWwtDiBDMYBdj916uzjfOWRIZDJ8rRU1A0SfWYhxPAkq27MhZm3A91j0nMxPUEGsJmoOijIG9iK4HAgMBAAECgYBxP86NRPgsQINiiIir+e065cxrvheqkGzTmQpQW9EaRuaMkPn9mqX5DysgAkRPu6+6G0tV2a0XYLcUxnN5EboREhEFTtLV+rPy+vj4uIWUc+zhdmxHIKIWDTekvjiJl27Du5064/Dfx5pW3CAlf4doNMp5eozRY9O9d/YqEXneIQJBAN/aGrG2LDfdYBZbvvqwDEe5cVXs521GpFBFDIeHkMiZ8+1S72JUHA/UjPR5zSDuxvdtao5UTypzs1OdUhU5EbECQQDeAUCLHakt6C4/O2MeIaamafZ1R1juKFASs/6d5NPU1ghMVoQFrWaUVQ9k1+tIQo9C0Stif3cthKqjzJa90zE3AkEAmHp3Tm/ZpN/9UJ2D9Dyw0LZnlfD+HvhMeoTeKP9Vxt7fQdwJRYAncT0GGo1RBcq/6tA0Eekp16/iCeWSgPRTsQJBAM1tNS9Fg/3RwbGLH32LP+zvUjpVienebbHQ0oOCca2ZW9ZfnAyw1qHdT8BNuA6GYJBAgzfCoSICh+/H6Zi/auMCQEa9/qtJHuTiEGFQTdbYneS2vHig4JNBvDq/tpfWuoW29KQJ6GedB2b1kxuiuILi2k6pJZGsZCRMvtNGv+A8hF8=";
	/** 公钥 */
	private String publicKey;// =
								// "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDfxHx1NPsTg7+UFKuLZ/2mghSmZcjFz73zgiIQIoygKDal3xL4lXjDIGC5UKq45qO3wBHC1aEp2mG5s9UUO8yy0B16yAoyTovh/bEFVFJB4x4Rqom1yrs7L9ULATqF0OfWoORGQMFJCTmssxOyfesz5kHZlJtnLZIId5mO4olj8QIDAQAB";
	/** 支付地址 */
	private String payUrl;// = "http://open.whrcpx.com/api/index";

	/** 版本号 */
	private String version;// = "V1.0";
	/** 商户号 */
	private String merNo;// = "8800345000211";
	/** 异步通知 */
	private String notifyUrl;// = "http://www.baidu.com";
	/** 商品名称 */
	private String commodityName;// = "testpay";

	public DDZFPayServiceImpl() {

	}

	public DDZFPayServiceImpl(Map<String, String> pmap) {
		if (pmap != null) {
			if (pmap.containsKey("privateKey")) {
				this.privateKey = pmap.get("privateKey");
			}
			if (pmap.containsKey("publicKey")) {
				this.publicKey = pmap.get("publicKey");
				;
			}
			if (pmap.containsKey("payUrl")) {
				this.payUrl = pmap.get("payUrl");
				;
			}
			if (pmap.containsKey("version")) {
				this.version = pmap.get("version");
				;
			}
			if (pmap.containsKey("merNo")) {
				this.merNo = pmap.get("merNo");
				;
			}
			if (pmap.containsKey("notifyUrl")) {
				this.notifyUrl = pmap.get("notifyUrl");
				;
			}
			if (pmap.containsKey("commodityName")) {
				this.commodityName = pmap.get("commodityName");
				;
			}
		}
	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) throws Exception {
		DDZFPayServiceImpl service = new DDZFPayServiceImpl();
		PayEntity payEntity = new PayEntity();
		payEntity.setUsername("测试用户");
		payEntity.setAmount(123.0);
		service.smPay(payEntity);
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {

		String userName = payEntity.getUsername();
		double amount = payEntity.getAmount();
		String mobile = payEntity.getMobile();

		String returnUrl = payEntity.getRefererUrl();// "http://www.baidu.com";
		String requestNo = payEntity.getOrderNo();// new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		String productId = payEntity.getPayCode();// "1009";// 支付宝扫码
		String transId = getTransId(productId);// "10";// 交易类型
		String orderDate = new SimpleDateFormat("yyyyMMdd").format(new Date());// 订单日期
		String orderNo = payEntity.getOrderNo();// new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());// 商户订单号

		Map<String, String> params = new TreeMap<>();
		params.put("version", this.version);
		params.put("merNo", this.merNo);
		params.put("notifyUrl", this.notifyUrl);
		params.put("commodityName", this.commodityName);
		params.put("returnUrl", returnUrl);
		params.put("transAmt", String.valueOf((int) amount * 100));
		params.put("requestNo", requestNo);
		params.put("productId", productId);
		params.put("transId", transId);
		params.put("orderDate", orderDate);
		params.put("orderNo", orderNo);
		params.put("signature", generateSign(params));

		String resultStr = HttpUtil.RequestForm(this.payUrl, params);
		JSONObject responseJson = JSONObject.fromObject(JSONObject.fromObject(resultStr));
		logger.info(responseJson.toString());
		if (responseJson.containsKey("respCode") && "P000".equals(responseJson.getString("respCode"))) {
			boolean signFlag = SignUtils.verifySign(resultStr, publicKey);
			if (signFlag) {
				logger.info("验签成功");
				if (StringUtils.isEmpty(mobile)) {
					return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, orderNo,
							responseJson.getString("codeUrl"));
				}
				return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, orderNo,
						responseJson.getString("payUrl"));
			} else {
				return PayUtil.returnPayJson("error", "2", "验签失败", userName, amount, orderNo, "");
			}
		} else {
			return PayUtil.returnPayJson("error", "2", responseJson.getString("respDesc"), userName, amount, orderNo,
					"");
		}
	}

	// 获取交易类型
	private String getTransId(String productId) {
		if ("1001".equals(productId) || "1006".equals(productId)|| "1009".equals(productId)) {
			return "10";
		}
		return "";
	}

	@Override
	public String callback(Map<String, String> infoMap) {
		JSONObject json = JSONObject.fromObject(infoMap);
		try {
			boolean signFlag = SignUtils.verifySign(json.toString(), publicKey);
			if (signFlag) {
				return "success";
			}
		} catch (Exception e) {
			return "false";
		}
		return "";
	}

	private String generateSign(Map<String, String> params) {
		StringBuilder buf = new StringBuilder();

		for (String key : params.keySet()) {
			String value = params.get(key);
			if (value == null || value.trim().length() == 0) {
				continue;
			}
			buf.append(key).append("=").append(value).append("&");
		}
		String signatureStr = buf.substring(0, buf.length() - 1);
		String signature = null;
		try {
			signature = RSAUtil.signByPrivate(SignUtils.SHA1(signatureStr), privateKey, "UTF-8");
			logger.info("生成签名串：" + signature);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("签名生成失败");
		}
		return signature;
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

}
