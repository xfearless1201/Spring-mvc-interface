package com.cn.tianxia.pay.xzx.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LoadParam {

	public static Properties loadPropertyFromClasspath() {
		Properties p = new Properties();
		InputStream is = null;
		
		try {
			is = LoadParam.class.getClassLoader().getResourceAsStream("config.properties");
			p.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return p;
	}
}
