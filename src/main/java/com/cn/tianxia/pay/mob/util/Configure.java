package com.cn.tianxia.pay.mob.util;

public class Configure {
	//��ʽ
	private final static String PAY_URL = "http://115.182.202.23:8880/ks_smpay/netsm/pay.sm";
//	private final static String PAY_URL = "http://onlinepay.kspay.net:8080/ks_smpay/netsm/pay.sm";
//	private final static String PAY_URL = "http://localhost:8080/ks_smpay/netsm/pay.sm";


	public static String getPAY_URL() {
		return PAY_URL;
	}
}
