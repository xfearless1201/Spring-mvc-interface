package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.sl.util.Base64;
import com.cn.tianxia.pay.sl.util.MD5;
import com.cn.tianxia.pay.sl.util.SecurityUtil;
import com.cn.tianxia.pay.sl.util.Tools;
import com.cn.tianxia.pay.sl.util.XmlUtil;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName: SLPayServiceImpl
 * @Description:随乐支付
 * @author: Hardy
 * @date: 2018年8月21日 下午9:49:14
 * 
 * @Copyright: 天下科技
 *
 */
public class SLPayServiceImpl implements PayService {

	/**
	 * 随乐支付日志
	 */
	private static final Logger logger = LoggerFactory.getLogger(SLPayServiceImpl.class);

	private String merchantId;// 商户代码

	private String merchantPayNotifyUrl;// 异步通知地址;

	private String payUrl;// 支付请求地址

	private String priKey;// 私钥

	private String pubKey;// 签名公钥
	
	public SLPayServiceImpl(Map<String, String> pmap) {
		if (pmap != null) {
			if (pmap.containsKey("merchantId")) {
				this.merchantId = pmap.get("merchantId");
			}

			if (pmap.containsKey("merchantPayNotifyUrl")) {
				this.merchantPayNotifyUrl = pmap.get("merchantPayNotifyUrl");
			}

			if (pmap.containsKey("priKey")) {
				this.priKey = pmap.get("priKey");
			}

			if (pmap.containsKey("pubKey")) {
				this.pubKey = pmap.get("pubKey");
			}

			if (pmap.containsKey("payUrl")) {
				this.payUrl = pmap.get("payUrl");
			}
		}
	}

	/**
	 * 网银支付
	 */
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		logger.info("SL随乐网银支付========================start================================");
		String merchantOrderId = payEntity.getOrderNo();// 订单号
		// 订单金额,以分为单位
		double amount = payEntity.getAmount();
		String merchantOrderAmt = new DecimalFormat("#").format(amount * 100);
		String bankId = payEntity.getPayCode();
		if(StringUtils.isBlank(bankId)){
			bankId = "";
		}
		// 封装请求参数
		Map<String, String> data = new HashMap<>();
		data.put("merchantId", merchantId);
		data.put("merchantOrderId", merchantOrderId);
		data.put("merchantOrderAmt",merchantOrderAmt);
		data.put("merchantPayNotifyUrl", merchantPayNotifyUrl);
		data.put("orderTime",new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		data.put("bankId", bankId);
		logger.info("SL随乐支付请求参数:" + JSONObject.fromObject(data).toString());
		//请求参数生成xml字符串
		String xmlStr = XmlUtil.createXml(data);
		logger.info("SL随乐支付转换XML参数:{xmlStr="+xmlStr+"}");
		// 报文数据的 BASE64 编码（xmlStr）
		 String xmlBase64Str = null;
		 try {
		 xmlBase64Str = new String(Base64.encode(xmlStr.getBytes("utf-8")));
		 logger.info("SL随乐支付获取Base64编码报文数据:{xmlBase64Str="+xmlBase64Str+"}");
		 } catch (UnsupportedEncodingException e) {
		 e.printStackTrace();
		 logger.info("SL随乐支付获取Base64编码报文数据异常:"+e.getMessage());
		 }

		/**
		 * 签名数据的 BASE64 编码（signStr） 1.先对xmlStr进行MD5加密 2.在对MD5加密的后的结果进行RSA加密
		 * 3.最后在进行Base64编码
		 */
		 String signBase64Str = null;
		 try {
			 //MD5加密的xml字符串
			 byte[] md5XmlStr = MD5.getDigest(xmlStr.getBytes("UTF-8"));
			 logger.info("SL随乐支付MD5加密后的xmlStr字符串:{md5XmlStr="+md5XmlStr+"}");
			 //生成RSA加密串
			 byte[] signXmlStr = SecurityUtil.sign(md5XmlStr, priKey);
			 //生成Base64加密串
			 signBase64Str = new String(Base64.encode(signXmlStr));
			 logger.info("SL随乐支付获取BASE64编码签名数据:{signBase64Str="+signBase64Str+"}");
		} catch (Exception e) {
			e.printStackTrace();
			 logger.info("SL随乐支付签名异常:"+e.getMessage());
		}
		 // 组装请求参数
		 StringBuffer sb = new StringBuffer();
		 sb.append(xmlBase64Str).append("|").append(signBase64Str);
		 String requestData = sb.toString();
		logger.info("SL随乐支付最终请求参数:" + requestData);
		// 发起form表单请求
		StringBuffer formsb = new StringBuffer();
		formsb.append("<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\"");
		formsb.append(payUrl);
		formsb.append("\"><input type=\"hidden\" name=\"msg\" value='");
		formsb.append(requestData).append("'>\r\n</form></body>");
		String formStr = formsb.toString();
		logger.info("SL随乐支付网关支付结果返回值:" + formStr);
		logger.info("==============================SL随乐网页支付end=====================================");
		return PayUtil.returnWYPayJson("success", "form", formStr, payEntity.getPayUrl(), "");
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
	 * 支付通知结果解析
	 * @param notifyResultStr 通知字符串
	 * @return 通知结果
	 * @throws Exception
	 */
	public String callback(String notifyResultStr){
		logger.info("SL支付回调验签开始========================start======================");
		try {
			String [] e = Tools.split(notifyResultStr, "|");
			String responseSrc = e[0];
			String signSrc = e[1];
			//验签
			if(e.length == 2){
				responseSrc = new String(Base64.decode(e[0]),"UTF-8");
				if (SecurityUtil.verify(responseSrc,signSrc,pubKey)){
					logger.info("SL随乐支付签证签名成功!");
					return "success";
				} 
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("SL随乐支付签证签名异常!"+e.getMessage());
		}
		return "";
	}

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }

}
