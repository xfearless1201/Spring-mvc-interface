package com.cn.tianxia.pay.ht.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

/***
 * 工具类
 * 
 * @author bob
 *
 */
public class Utils {

	/**
	 * 使用 Map按key进行排序
	 * 
	 * @param map
	 * @return
	 */
	public static Map<String, String> sortMapByKey(Map<String, String> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		//Map<String, String> sortMap = new TreeMap<String, String>((String str1, String str2) -> str1.compareTo(str2));
		Map<String,String> sortMap = new TreeMap<String,String>(new Comparator<String>() {

	            @Override
	            public int compare(String o1, String o2) {
	                int temp = o1.compareTo(o2);
	                return temp==0?o1.compareTo(o2):temp;
	            }
	        });
		
		
		
//		Map<String, String> sortMap = new TreeMap<String, String>(new Comparator<String>(){  
//            /* 
//             * int compare(Object o1, Object o2) 返回一个基本类型的整型， 
//             * 返回负数表示：o1 小于o2， 
//             * 返回0 表示：o1和o2相等， 
//             * 返回正数表示：o1大于o2。 
//             */  
//            public int compare(String o1, String o2) {  
//                //指定排序器按照降序排列  
//                return o2.compareTo(o1);  
//            }     
//        });  
		
		sortMap.putAll(map);
		return sortMap;
	}

	public static Map<String, String> getParameterMap(HttpServletRequest request) throws Exception {
		Map<String, String> resultMap = new HashMap<String, String>();
		Map<String, String[]> tempMap = request.getParameterMap();
		Set<String> keys = tempMap.keySet();
		for (String key : keys) {
			byte source[] = request.getParameter(key).getBytes("iso8859-1");
			String modelname = new String(source, "UTF-8");
			resultMap.put(key, modelname);
		}
		return resultMap;
	}

	public static String getSign(Map<String, String> map, String key) {
		map.remove("sign");
		map = sortMapByKey(map);
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (entry.getValue() != null) {
				sb.append(entry.getKey() + "=" + entry.getValue() + "&");
			}
		}
		String signStr = sb.append("key=" + key).toString();
		System.out.println(signStr);
		return MD5Encoder.encode(signStr, "UTF-8");
	}
}
