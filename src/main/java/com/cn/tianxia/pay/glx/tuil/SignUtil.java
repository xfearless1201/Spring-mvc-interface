package com.cn.tianxia.pay.glx.tuil;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

public class SignUtil {
	public static final String appKey = "2f5549cc22a77ace382afe175c353c93e6c060bb6db90f8a2a97dde3f9d2e1e5";
	public static final String appId = "a620fedbc44649b9aa9ae614ac874213";
	
	public static String sign(Map<String,Object> map,String appKey){
		Map<String,String> mapResult = clearNull(map);
		return getSign(mapResult, appKey);
	}
	//此处的map已经经过 排序  并且 清除了null null字符串  空值  有值的参数同时去空格
	private static String getSign(Map<String,String> map,String appKey){
		map.remove("sign");
		String sb = "";
		Set<String> set = map.keySet();
		for(String s:set){
			sb += s+"="+map.get(s)+"&";
		}
		sb = sb.substring(0,sb.length()-1);
		sb += appKey;
		System.out.println("签名前字符串:"+sb.toString());
		System.out.println("签名后:"+Md5Utils.signature(sb.toString()));
		return Md5Utils.signature(sb.toString());
	}
	/**
	 * 过滤掉 null 空值  null 字符串
	 * 并 根据 ASCII 升序
	 * @return
	 */
	private static Map<String,String> clearNull(Map<String,Object> map){
		Map<String,String> mapTree = new TreeMap<String, String>();
		Set<String> set = map.keySet();
		for(String s:set){
			String value = mapGetString(map, s);
			if(value != null){
				mapTree.put(s, value.trim());
			}else{
				System.out.println("被过滤掉的空值Key:" + s);
			}
		}
		return mapTree;
	}
	public static String mapGetString(Map<String,Object> map,String key){
		String value = (map.get(key)+"").trim();
		return (StringUtils.isEmpty(value)||"null".equals(value))?null:value;
	}
}
