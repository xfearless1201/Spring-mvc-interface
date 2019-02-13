package com.cn.tianxia.pay.tx.util;

import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;

public class SignUtil {
	
	/**
	 * 计算签名值
	 * @param reqStr
	 * @param signKey
	 * @return
	 */
    public static String signData(TreeMap<String, String> reqStr,String signKey) {
       	StringBuffer buf = new StringBuffer();
        for (String key : reqStr.keySet()) {
        	    if("sign".equalsIgnoreCase(key)) {
        	    	    continue;
        	    }
        	    String val = reqStr.get(key);
          	if(val!=null&&!"".equals(val.trim())){
        		   buf.append(key).append("=").append(val).append("&");
        	    }
        }
        buf.append("key").append("=").append(signKey);
        String signD = MD5Utils.md5(buf.toString()); 
        return signD;
    }
}
