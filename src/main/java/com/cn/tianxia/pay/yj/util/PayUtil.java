package com.cn.tianxia.pay.yj.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;


public class PayUtil {
	
	/**
	 * post 请求
	 * @param url 请求地址
	 * @param xml 请求参数
	 * @return
	 * @throws Exception
	 */
	public static String httpPostWithXML(String url, String xml) throws Exception {
    	//创建httpclient工具对象   
    	org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
        //创建post请求方法   
        PostMethod myPost = new PostMethod(url);    
        String responseString = null;    
        try{   
          //设置请求头部类型   
          myPost.setRequestHeader("Content-Type","text/xml");  
          myPost.setRequestHeader("charset","utf-8");  
          myPost.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.87 Safari/537.36");
              
          myPost.setRequestEntity(new StringRequestEntity(xml,"text/xml","utf-8"));     
          int statusCode = client.executeMethod(myPost);    
	      if(statusCode == HttpStatus.SC_OK){    
              BufferedInputStream bis = new BufferedInputStream(myPost.getResponseBodyAsStream());    
              byte[] bytes = new byte[1024];    
              ByteArrayOutputStream bos = new ByteArrayOutputStream();    
              int count = 0;    
              while((count = bis.read(bytes))!= -1){    
                  bos.write(bytes, 0, count);    
              }    
              byte[] strByte = bos.toByteArray();    
              responseString = new String(strByte,0,strByte.length,"utf-8");    
              bos.close();    
              bis.close();  
          }  
        }catch (Exception e) {    
            e.printStackTrace();    
        } 
	    myPost.releaseConnection();    
	    return responseString;
    }
	
	/**
	 * 加签
	 * @param signKey 签名key
	 * @param params  参数Map
	 * @return
	 */
	public static String sign(String signKey, Map<String, String> params) {
        try {
        	String charset = params.get("charset");
    		if(StringUtils.isBlank(charset)){
    			charset = "utf-8";
    		}
            params = paraFilter(params); // 过滤掉一些不需要签名的字段
            StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
            buildPayParams(buf, params, false);
            buf.append("&key=").append(signKey);
            String preStr = buf.toString();
            System.err.println("MD5签名原串===>"+preStr);
            String sign = DigestUtils.md5Hex(preStr.getBytes(charset)).toUpperCase();
            System.err.println("MD5签名===>"+sign);
            return sign;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
	/**
	 * 过滤参数
	 * 
	 * @author
	 * @param sArray
	 * @return
	 */
	public static Map<String, String> paraFilter(Map<String, String> sArray) {
		Map<String, String> result = null;
		if (sArray == null || sArray.size() <= 0) {
			return result;
		}
		result = new HashMap<String, String>(sArray.size());
		for (String key : sArray.keySet()) {
			String value = sArray.get(key);
			if (StringUtils.isBlank(value) || key.equalsIgnoreCase("sign")) {
				continue;
			}
			result.put(key, value);
		}
		return result;
	}
	
	/**
	 * 拼接签名参数
	 * @author
	 * @param payParams
	 * @return
	 */
	public static void buildPayParams(StringBuilder sb, Map<String, String> payParams, boolean encoding) {
		List<String> keys = new ArrayList<String>(payParams.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			sb.append(key).append("=");
			if (encoding) {
				sb.append(urlEncode(payParams.get(key)));
			} else {
				sb.append(payParams.get(key));
			}
			sb.append("&");
		}
		sb.setLength(sb.length() - 1);
	}
	
	/**
	 * encode
	 * @param str
	 * @return
	 */
	public static String urlEncode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (Throwable e) {
			return str;
		}
	}
}
