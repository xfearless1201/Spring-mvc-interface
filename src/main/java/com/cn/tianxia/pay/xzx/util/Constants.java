package com.cn.tianxia.pay.xzx.util;

import java.util.Properties;

public class Constants {

	public static String MER_ID;
	
	public static String TER_ID;
	
	public static String keyPass;
	
	public static String aliasName;
	
	public static String DFkey;
	
	public static String RETURN_URL;
	
	public static String NOTICE_URL;
	
	static {
		Properties p = LoadParam.loadPropertyFromClasspath();
		MER_ID=p.getProperty("MER_ID");
		TER_ID=p.getProperty("TER_ID");
		keyPass=p.getProperty("keyPass");
		aliasName = p.getProperty("aliasName");
		DFkey = p.getProperty("DFkey");
		RETURN_URL = p.getProperty("RETURN_URL");
		NOTICE_URL = p.getProperty("NOTICE_URL");
	}
}
