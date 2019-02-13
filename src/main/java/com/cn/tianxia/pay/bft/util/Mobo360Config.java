package com.cn.tianxia.pay.bft.util;

/**
 * 
 * 类名：Mobo360Config
 * 功能：基础配置类
 * 详细：设置商户相关信息及证书文件和通知地址等
 * 以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 * 该代码仅供学习和研究接口使用，只是提供一个参考。
 *
 */

public class Mobo360Config {
	// 请选择签名类型， MD5、CER(证书文件)、RSA
    public static final String SIGN_TYPE = "MD5";
	//public static final String SIGN_TYPE = "CER";
    //public static final String SIGN_TYPE = "RSA";

	//******************配置商户基本信息
	// 商户ID
	public static String PLATFORM_ID = "210001110012875";
	// 商户帐号
	public static String MERCHANT_ACC = "210001110012875";
	//******************配置商户基本信息***结束
	
	//******************使用MD5签名方式需配置下列参数
	// MD5签名的key值
	public static String MD5_KEY = "a6956840facb30af80603fd76314d084";
	//******************MD5签名方式需配置***结束
	
	
	
	//******************使用证书签名方式需配置下列参数
		// 商户私钥文件--用于商户数据签名
		public static String PFX_FILE = "c:/temp/test/210001510010040.pfx";
		// 支付系统公钥文件--用于支付系统返回数据验签
		public static String CERT_FILE = "c:/temp/test/epay.cer";
		// 私钥文件密码
		public static String PASSWD = "210001510010040";
		//******************证书签名方式需配置***结束

		//******************使用RSA签名方式需配置下列参数
		// 用于签名的商户RSA私钥
		public static String RSA_MERCH_PRIVATE_KEY = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAKnLEkZ8AZJWjMYTJwXarrAXFPkEdBvDo0rMDlOGXCRYyqG1azUBiksTnrbMiwseJ8qvLp6daISdTulj3JA5Tt0ENh9T2atzFDFsJVMVWJ69XLJ9ziMOg4cDvu9oksaUOv+dTx1Ek/HXGkPCWbVJFUAjk8Va7dkL8IAkmaSRwpcdAgMBAAECgYEAhYGqVgetiK5LNHfcyCqiDs2nbQIGdcpHvElkvmI4U1AJzEsFCAG1BsFfm6aLcet9KE7Enm1wwE2cvcEKrdCR48g7lTci2KK/wPZdFVIR1hzVEretcTUBFH544R/DR/hImIlNyak+5KY1DkPHDYSdLUxOikJ1SSGyIn1TEKRNYLUCQQDiAFMUdGmjlFRBVgnhR/ksdp1vcc3tWxkRY4TeL4uVMGlrDD0L2bA1w5njseNUm05pB1qJS8RVTD18Q7TO3wlLAkEAwFTCq8O/UjeqyElS1dksqMiOLeamH6lCVfxzvi0Amx1STP8seTU7XDoPpIPL6mr7PqkE/0U2nUd+giKHcZ/INwJBAI7TF64Al9Y54jlcL2hAvPbdi0cny7Up8iCsHQbxUywYaTauiFHZ4+NGVxWvkPQiJh53+D52NICXavACNdza1RsCQQCvFvq5/9PWd1METqwYfkMHzLyS0Nz/CcmYGeEMik945rKb4dmM5ocJqNnAvTMQTyA2pQwlj87uU725ntGLARj1AkBUFuI8cVqnW+7zhR45Njzmg4MsaeyDikdFyKPlm5s+S3O4PSI3X0d/KeKc9YEtWZbOgSXVfkbzO2QLOnql+QrQ";
		// 用于验签的支付系统RSA公钥
		public static String RSA_MBP_PUBLIC_KEY = "MIIC8zCCAlwCCQDCJFzeAEikJDANBgkqhkiG9w0BAQUFADCBvzELMAkGA1UEBhMCQ04xEDAOBgNVBAgTB1NJQ0hVQU4xEDAOBgNVBAcTB0NIRU5HRFUxKTAnBgNVBAoTIE1PQk8gTmV0d29yayBUZWNobm9sb2d5IENvLiwgTHRkMSkwJwYDVQQLEyBNT0JPIE5ldHdvcmsgVGVjaG5vbG9neSBDby4sIEx0ZDERMA8GA1UEAxMITW9iYW9QYXkxIzAhBgkqhkiG9w0BCQEWFG1vYmFvcGF5QG1vYm8zNjAuY29tMB4XDTExMDgxODA2MTMzOFoXDTEyMDgxNzA2MTMzOFowgbsxCzAJBgNVBAYTAkNOMRAwDgYDVQQIEwdTSUNIVUFOMRAwDgYDVQQHEwdDSEVOR0RVMSkwJwYDVQQKEyBNT0JPIE5ldHdvcmsgVGVjaG5vbG9neSBDby4sIEx0ZDEpMCcGA1UECxMgTU9CTyBOZXR3b3JrIFRlY2hub2xvZ3kgQ28uLCBMdGQxDTALBgNVBAMTBGVwYXkxIzAhBgkqhkiG9w0BCQEWFG1vYmFvcGF5QG1vYm8zNjAuY29tMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC7G3VZLvxYEiUU1GJAhOaHaCX28jYSLxIOMviUT7IJ9FLo51cYMzdWB2dlzHtItuNwFUyg8bdY2ptFQASW6af+56JDVV0Q/d/3C292TMI2e3ME9il2GcwNRXGSbvseH5AXjJPSvSxJWHSxeDXL7K7dzhod241Y8M0gYHpyOLvcnwIDAQABMA0GCSqGSIb3DQEBBQUAA4GBAGKcTB5X09rAi9jzZJneAQkAa5Yie64MUaj1jGcCQCLpGjI5PZad+1vLTQmtXXyRvMax1S8soKXPPIFiplSF6t91ZZm9/Bn4DrnLaxB53qjZtcY4L/L15uDBjt9hI3tP4jQZE/JSIB9iaS81Bcc+CJ8R4ZuEL/RLYNI3nh5ogIPX";
		//******************RSA签名方式需配置***结束

	
	// 通知地址
	public static String MERCHANT_NOTIFY_URL = "http://localhost/Pay/MFT_notify_url";
	//public static String MERCHANT_NOTIFY_URL = "http://127.0.0.1:8080/MobaoPayExample/callBack.jsp";
	// 支付系统网关
	
	public static String MOBAOPAY_GETWAY = "http://epay.zapwka.top/cgi-bin/netpayment/pay_gate.cgi";
	
	public static final String MOBAOPAY_API_VERSION = "1.0.0.0";
	public static final String MOBAOPAY_API_VERSIONAP = "1.0.0.1";

	// 接口名称
	public static final String MOBAOPAY_APINAME_PAY = "WEB_PAY_B2C";
	public static final String MOBAOPAY_APINAME_QUERY = "MOBO_TRAN_QUERY";
	public static final String MOBAOPAY_APINAME_REFUND = "MOBO_TRAN_RETURN";
	public static final String MOBAOPAY_APINAME_REAL_PAY = "CUST_REAL_PAY";
	public static final String MOBAOPAY_APINAME_DS = "SINGLE_ENTRUST_SETT";
	public static final String MOBAOPAY_MERCH_AMT_QUERY = "MERCH_AMT_QUERY";
	public static final String MOBAOPAY_WECHAT_PAY = "WECHAT_PAY";
	public static final String MOBAOPAY_AL_SCAN_PAY = "AL_SCAN_PAY";
	public static final String MOBAOPAY_QQ_WLT_PAY = "QQWLT_PAY";
	public static final String MOBAOPAY_JD_WLT_PAY = "JDWLT_PAY";
}
