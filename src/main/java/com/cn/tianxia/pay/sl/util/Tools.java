package com.cn.tianxia.pay.sl.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Tools
{
    /**
     * 通过r分解src
     * @param src
     * @return
     */
	public static String [] split(String src,String r){
    	if(src == null || r == null || src.length() == 0 
    			|| r.length() == 0 || src.indexOf(r) == -1){
    		String [] strs = {src};
    		return strs;
    	}
    	List list = new ArrayList();
    	int site = src.indexOf(r); 
    	int rLen = r.length();
    	while(site != -1){
    		list.add(src.substring(0, site));
    		src = src.substring(site + rLen);
    		site = src.indexOf(r);
    	}
    	list.add(src);
    	String [] strs = new String [list.size()];
    	for(int i = 0; i<list.size(); i++){
    		strs[i] = (String)list.get(i);
    	}
    	return strs;
    }
	public static String SHA1String(String inStr) {
        MessageDigest md = null;
        String outStr = null;
        try {
            md = MessageDigest.getInstance("SHA-1");     //选择SHA-1，也可以选择MD5
            byte[] digest = md.digest(inStr.getBytes());       //返回的是byet[]，要转化为String存储比较方便
            outStr = bytetoString(digest);
        }
        catch (NoSuchAlgorithmException nsae) {
            nsae.printStackTrace();
        }
        return outStr;
    } 
	public static String bytetoString(byte[] digest) {
        String str = "";
        String tempStr = "";
        
        for (int i = 0; i < digest.length; i++) {
            tempStr = (Integer.toHexString(digest[i] & 0xff));
            if (tempStr.length() == 1) {
                str = str + "0" + tempStr;
            }
            else {
                str = str + tempStr;
            }
        }
        return str.toLowerCase();
    }
}

