package com.cn.tianxia.pay.jhz.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONObject;
public class RequestUtil {
	

	private static ImageOutputStream output;
	
    /**
     * 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
     */
    public static String getParamSrc(Map<String, String> paramsMap) {
        StringBuffer paramstr = new StringBuffer();
        TreeMap<String, Object> sortMap = new TreeMap();
        sortMap.putAll(paramsMap);
        for (String pkey : sortMap.keySet()) {
       	 Object pvalue = sortMap.get(pkey);
       
       	if (null!=pvalue  && "" != pvalue && !pkey.equals("sign")) {// 空值不传递，不签名
               	 
       			paramstr.append(pkey + "=" + pvalue + "&"); // 签名原串，不url编码
			}
        }
        // 去掉最后一个&
        String result = paramstr.substring(0, paramstr.length() - 1);
        // 原串转码
        try {
            result = new String(result.getBytes("utf-8"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
     */
    public static String getParamSr(Map<String, String> paramsMap) {
        StringBuffer paramstr = new StringBuffer();
        TreeMap<String, Object> sortMap = new TreeMap();
        sortMap.putAll(paramsMap);
        for (String pkey : sortMap.keySet()) {
       	 Object pvalue = sortMap.get(pkey);
       
       	if (!pkey.equals("sign")) {
               	 
       			paramstr.append(pkey + "=" + pvalue + "&"); // 签名原串，不url编码
			}
        }
        // 去掉最后一个&
        String result = paramstr.substring(0, paramstr.length() - 1);
        // 原串转码
        try {
            result = new String(result.getBytes("utf-8"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
    
	public static String doPostStr(String url, String param){
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(),  "utf-8"));
            out.print(param);
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(),  "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                } 
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
	 
	/**
	 * json数据转map
	 */
	public static TreeMap<String, String> toMap(String jsonstr)  
	{  
		TreeMap<String, String> data = new TreeMap<String, String>();  
	      JSONObject jsonObject = JSONObject.fromObject(jsonstr);  
	      Iterator it = jsonObject.keys();  
	      while (it.hasNext())  
	      {  
	          String key = String.valueOf(it.next());  
	          String value = jsonObject.get(key).toString();  
	          data.put(key, value);  
	      }  
	      return data;  
	}  
	 public static String toString(HttpServletRequest request){  
	        String valueStr = "";  
	        try {  
	            StringBuffer sb = new StringBuffer();  
	            InputStream is = request.getInputStream();  
	            InputStreamReader isr = new InputStreamReader(is,"utf-8");  
	            BufferedReader br = new BufferedReader(isr);  
	            String s = "";  
	            while ((s = br.readLine()) != null) {  
	                sb.append(s);  
	            }  
	            valueStr = sb.toString();  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	            valueStr = "";  
	        }  
	        return valueStr;  
		}
}
