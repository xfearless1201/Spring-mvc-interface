package com.cn.tianxia.pay.impl;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.lxzf.util.RsaUtil;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName: LXZFPayServiceImpl
 * @Description:联行支付
 * @author: Hardy
 * @date: 2018年7月27日 下午8:48:40
 * 
 * @Copyright: 天下科技
 *
 */
public class LXZFPayServiceImpl implements PayService {
	
	private static final Logger logger = LoggerFactory.getLogger(LXZFPayServiceImpl.class);

	private String payUrl;// 支付url
	
	private String priKey;//私钥
	
	private String pubKey;//联行公钥

	private String merId;// 商户号

	private String dealNotify;// 异步回调url

	public LXZFPayServiceImpl(Map<String, String> pmap) {
		if (pmap != null) {
			if (pmap.containsKey("merId")) {
				this.merId = pmap.get("merId");
			}
			if (pmap.containsKey("dealNotify")) {
				this.dealNotify = pmap.get("dealNotify");
			}
			if (pmap.containsKey("payUrl")) {
				this.payUrl = pmap.get("payUrl");
			}
			if(pmap.containsKey("priKey")){
				this.priKey = pmap.get("priKey");
			}
			if(pmap.containsKey("pubKey")){
				this.pubKey = pmap.get("pubKey");
			}
		}
	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		// 保留两位小数点,单位为元
		BigDecimal dealFee = new BigDecimal(payEntity.getAmount()).setScale(2);// 订单金额
		String dealOrder = payEntity.getOrderNo();// 订单号
		String dealReturn = payEntity.getRefererUrl();// 同步回调url

		/**
		 * *注意:下面参数的顺序不可变
		 */
		JSONObject pjson = new JSONObject();
		pjson.put("merId", merId);
		pjson.put("dealOrder", dealOrder);// 订单号
		pjson.put("dealFee", dealFee);// 订单金额
		pjson.put("dealReturn", dealReturn);// 同步回调地址
		pjson.put("dealNotify", dealNotify);// 异步回调地址

		// 组装签名串
		StringBuffer sb = new StringBuffer();
		sb.append(merId).append(dealOrder).append(dealFee);
		sb.append(dealReturn).append(dealNotify);
		String data = sb.toString();
		// 通过RSA签名
		data = RsaUtil.sign(data, priKey);
		pjson.put("dealSignure", data);

		System.err.println("请求参数串:" + pjson.toString());
		String payForm = createPayForm(pjson);

		return PayUtil.returnWYPayJson("success", "form", payForm, payEntity.getPayUrl(), "");
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	
	/**
	 * 
	 * @Title: callback   
	 * @Description:支付异步回调
	 * @param: @param map
	 * @param: @return      
	 * @return: String      
	 * @throws
	 */
	@Override
	public String callback(Map<String,String> map){
		
		logger.info("验证签名开始==============================");
		logger.info("验证签名参数:"+map);
		
		//异步回调请求参数签名
		String dealSignure = map.get("dealSignure");
		
		//剩下的参数参与签名
		StringBuffer sb = new StringBuffer();
		sb.append(map.get("dealOrder")).append(map.get("dealState"));
		String data = sb.toString();
		
		logger.info("验签开始执行===========,验签参数:"+data);
		
		boolean isVerify = RsaUtil.verify(data, dealSignure, pubKey);
		
		logger.info("验证签名结果:"+isVerify);
		
		if(isVerify){
			//验签成功
			return "success";
		}
		return "";
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

	public static void main(String[] args) {
		JSONObject json = new JSONObject();
		json.put("payUrl", "http://user.sdecpay.com/dpaygate.html");
		json.put("priKey", "30820276020100300d06092a864886f70d0101010500048202603082025c02010002818100d600ca5673dfde7157faa14e3498ac168ede9e8a7394195bcb3bb294c65ac43fd54b805f6b71e16ad7da1e5931f81c7472ac763790e9201158907e177a800d7280c14db07dc2034e0b5032c8cb36a10a10b4cfdfbe2e07d50c76a874692d9adfe113f06c9112e4dce116f573db69cb5b1f5344bf97aced33832aa21aeb2bee29020301000102818049da61f9ade98f61bc440150f4f3cf47c5ac6c3849a4cd8f91369bed9dfa8b09a0797ae6f3348cdf8b7879cabc64eca4ff4227560bcb7846a432d16116e4843ee31171a888cfab3ff47418f7cddb2a7179890129c5b84acf798dd78feb3acb7ea6ad4b8ff73c33df27e9db4998ca1d7c46ecc7d7ed43a177ba815456b75d3a25024100f603b40daf14d583ef988264bf8f321ec0a5e1ad2f67f09ac8aaa4fe65139081d217dcd0002676a2f8540d481ec95875d9a0457e06806897112a731e8da28ca3024100deb07643bc02602c9bdea7c16db8b3db009de168de01683d5f97b51d38b1522b6a6e4b0991d0db41de6d5281c849db2088921b6029312686314ffbd2e288dac302410097ac5ed4d4ed9e59f25cef7f57cedc12b3a951c2a96886a1973805f345b3d33306a9699cdf8ad737d0ac5967ee7b2a4bb639a6be7519b3e1f75659952bb35ccb02405d6c1e51504096e46007cacce52105a32ce10f3d955575cbb99cc5889ace1930c997b7b72e0b36d0ad65a3a57ac7313cb6cec4d954aea000d013440c36a93f4d02407ae65632c546ce8b0fb822dd6a57146777ca0b1ab10408c21686e35fbe3ea6d345db5267b155ba68cad83511f4754d06da8a7d9d1d103eb2f690b6dd1f4d9536");
		json.put("pubKey", "30819f300d06092a864886f70d010101050003818d0030818902818100d600ca5673dfde7157faa14e3498ac168ede9e8a7394195bcb3bb294c65ac43fd54b805f6b71e16ad7da1e5931f81c7472ac763790e9201158907e177a800d7280c14db07dc2034e0b5032c8cb36a10a10b4cfdfbe2e07d50c76a874692d9adfe113f06c9112e4dce116f573db69cb5b1f5344bf97aced33832aa21aeb2bee290203010001");
		json.put("merId", "905294");
		json.put("dealNotify", "http://user.sdecpay.com/dpaygate.html");
		System.err.println(json.toString());
	}
}
