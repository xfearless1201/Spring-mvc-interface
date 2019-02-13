package com.cn.tianxia.pay.ys.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

/**
 * 加载配置文件根据配置文件,赋值到常量中
 */
public class YspayConfig {   
	
	
	public YspayConfig(String cagent,HttpSession session){
		Map<String, String> map=(Map<String, String>) session.getAttribute("ysPay");
		if(map!=null){
			JSONObject jo=new JSONObject().fromObject(map.get("payment_config"));
			if(jo!=null){
				DEFAULT_CHARSET=jo.getString("default_charset");
				PASSWORD_PARTNER_PKCS12=jo.getString("password_parter_pkcs12");
				RSA_ALGORITHM=jo.getString("rsa_algorithm");
				SIGN_ALGORITHM=jo.getString("sign_algorithm");
				PATH_PARTER_PKCS12=jo.getString("path_parter_pkcs12");
				PATH_YSEPAY_PUBLIC_CERT=jo.getString("path_ysepay_public_cert");
				YSEPAY_GATEWAY_URL=jo.getString("ysepay_getway_url");
				YSEPAY_GATEWAY_URL_DF=jo.getString("ysepay_getway_url_df");
				VERSION=jo.getString("version");
				NOTIFY_URL=jo.getString("notify_url");
				RETURN_URL=jo.getString("retrun_url");
				PARTNER_ID=jo.getString("partner_id");
				SELLER_ID=jo.getString("seller_id");
				SELLER_NAME=jo.getString("seller_name");
				payment_name=jo.getString("payment_name");
			} 
		}
	}
	
	
	// 使用商户自己的私钥签名请求时，采用的字符编码
	private String DEFAULT_CHARSET ="" ;
	// 合作商家私钥pkcs12证书密码
	private String PASSWORD_PARTNER_PKCS12="" ;
	// rsa算法名
	private String RSA_ALGORITHM="" ;
	// 合作商家私钥pkcs12证书路径
	private String PATH_PARTER_PKCS12="" ;
	// 银盛公钥pkcs12证书路径
	private String PATH_YSEPAY_PUBLIC_CERT="" ;
	// 银盛支付接入网关url
	private String YSEPAY_GATEWAY_URL="" ;
	// 代付url
	private String YSEPAY_GATEWAY_URL_DF="" ;
	// 版本号
	private String VERSION="" ;
	// 签名算法
	private String SIGN_ALGORITHM="" ;
	// 银盛支付服务器主动通知商户网站里指定的页面http路径。
	private String NOTIFY_URL="" ;
	// 银盛支付处理完请求后，当前页面自动跳转到商户网站里指定页面的http路径。
	private String RETURN_URL="" ;
	//商户号
	private String PARTNER_ID="" ;
	//卖方帐号
	private String SELLER_ID="" ;
	//卖方名称
	private String SELLER_NAME="" ;
	
	private String payment_name="";

	public String getDEFAULT_CHARSET() {
		return DEFAULT_CHARSET;
	}

	public void setDEFAULT_CHARSET(String dEFAULT_CHARSET) {
		DEFAULT_CHARSET = dEFAULT_CHARSET;
	}

	public String getPASSWORD_PARTNER_PKCS12() {
		return PASSWORD_PARTNER_PKCS12;
	}

	public void setPASSWORD_PARTNER_PKCS12(String pASSWORD_PARTNER_PKCS12) {
		PASSWORD_PARTNER_PKCS12 = pASSWORD_PARTNER_PKCS12;
	}

	public String getRSA_ALGORITHM() {
		return RSA_ALGORITHM;
	}

	public void setRSA_ALGORITHM(String rSA_ALGORITHM) {
		RSA_ALGORITHM = rSA_ALGORITHM;
	}

	public String getPATH_PARTER_PKCS12() {
		return PATH_PARTER_PKCS12;
	}

	public void setPATH_PARTER_PKCS12(String pATH_PARTER_PKCS12) {
		PATH_PARTER_PKCS12 = pATH_PARTER_PKCS12;
	}

	public String getPATH_YSEPAY_PUBLIC_CERT() {
		return PATH_YSEPAY_PUBLIC_CERT;
	}

	public void setPATH_YSEPAY_PUBLIC_CERT(String pATH_YSEPAY_PUBLIC_CERT) {
		PATH_YSEPAY_PUBLIC_CERT = pATH_YSEPAY_PUBLIC_CERT;
	}

	public String getYSEPAY_GATEWAY_URL() {
		return YSEPAY_GATEWAY_URL;
	}

	public void setYSEPAY_GATEWAY_URL(String ySEPAY_GATEWAY_URL) {
		YSEPAY_GATEWAY_URL = ySEPAY_GATEWAY_URL;
	}

	public String getYSEPAY_GATEWAY_URL_DF() {
		return YSEPAY_GATEWAY_URL_DF;
	}

	public void setYSEPAY_GATEWAY_URL_DF(String ySEPAY_GATEWAY_URL_DF) {
		YSEPAY_GATEWAY_URL_DF = ySEPAY_GATEWAY_URL_DF;
	}

	public String getVERSION() {
		return VERSION;
	}

	public void setVERSION(String vERSION) {
		VERSION = vERSION;
	}

	public String getSIGN_ALGORITHM() {
		return SIGN_ALGORITHM;
	}

	public void setSIGN_ALGORITHM(String sIGN_ALGORITHM) {
		SIGN_ALGORITHM = sIGN_ALGORITHM;
	}

	public String getNOTIFY_URL() {
		return NOTIFY_URL;
	}

	public void setNOTIFY_URL(String nOTIFY_URL) {
		NOTIFY_URL = nOTIFY_URL;
	}

	public String getRETURN_URL() {
		return RETURN_URL;
	}

	public void setRETURN_URL(String rETURN_URL) {
		RETURN_URL = rETURN_URL;
	}

	public String getPARTNER_ID() {
		return PARTNER_ID;
	}

	public void setPARTNER_ID(String pARTNER_ID) {
		PARTNER_ID = pARTNER_ID;
	}

	public String getSELLER_ID() {
		return SELLER_ID;
	}

	public void setSELLER_ID(String sELLER_ID) {
		SELLER_ID = sELLER_ID;
	}

	public String getSELLER_NAME() {
		return SELLER_NAME;
	}

	public void setSELLER_NAME(String sELLER_NAME) {
		SELLER_NAME = sELLER_NAME;
	}

	public String getPayment_name() {
		return payment_name;
	}

	public void setPayment_name(String payment_name) {
		this.payment_name = payment_name;
	}
	 
}
