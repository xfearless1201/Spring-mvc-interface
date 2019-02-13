package com.cn.tianxia.pay.sf.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import com.cn.tianxia.pay.ht.util.MD5Encoder;

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
		Map<String, String> sortMap = new TreeMap<String, String>(new Comparator<String>() {
			@Override
			public int compare(String str1, String str2) {
				return str1.compareTo(str2);
			}
		});
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
