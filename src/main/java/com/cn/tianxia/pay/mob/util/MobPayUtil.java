package com.cn.tianxia.pay.mob.util;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class MobPayUtil {
	public static  String   MerKey="1FDD2547FA4FB61F";  //商户密钥
	public  static  String   MerCode="818310048160000";  //商户号
	
	/****
	 * 将字符串转换为加密的串
	 * @param transMap
	 * @return
	 */
	public  static String getUrlStr(Map<String,String> transMap){
		//组织需要加密的字符串
		String transStr="";
		int flag=0;
		for(String key:transMap.keySet()) 
		{
			if((transMap.size()-1)==flag){
				transStr=transStr+key+"="+transMap.get(key);
			}else{
				transStr=transStr+key+"="+transMap.get(key)+"&";
			}
			flag++;
		} 
		return 	transStr;
	}
	
	
	/****
	 * 将map参数转换为一般加密格式参数
	 * @param map
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
    public static  String   convertUrl(Map map){
    	StringBuffer  buf= new StringBuffer();
    	if(null!=map){
    		Iterator  it= map.keySet().iterator();
    		while (it.hasNext()) {
				String key = (String) it.next();
				buf.append(key);
				buf.append("=");
				if(null==map.get(key)){
					buf.append("");
				}else{
					buf.append(map.get(key).toString());
				}
				if(it.hasNext()){
				buf.append("&");
				}
			}
    		return  buf.toString();
    	}else{
    		return null;
    	}
    }
    public static Map<String,String>  getParameters(HttpServletRequest request){
        Map<String,String>  map = new HashMap<String,String>();
        try{
        	Enumeration enu=request.getParameterNames();  
        	while(enu.hasMoreElements()){  
                String paraName=(String)enu.nextElement();  
                map.put(paraName,request.getParameter(paraName));
        	}  
        	return  map;
        }catch (Exception e) {
        	return  null;
        	
		}  
    }
    

}
