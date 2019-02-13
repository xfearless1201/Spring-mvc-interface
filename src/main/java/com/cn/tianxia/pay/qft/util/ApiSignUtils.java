package com.cn.tianxia.pay.qft.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @author liuxingmi
 * @datetime 2017年8月2日 下午8:33:09
 * @desc 签名验证
 */
public class ApiSignUtils {

    private static Logger logger = LoggerFactory.getLogger(ApiSignUtils.class);

    /**
     * @param jo     参数对象
     * @param secret 密钥
     *
     * @return Sign
     */
    public static String toSign(JSONObject jo, String fiterKey, String secret) {
        if (jo == null || StringUtils.isEmpty(secret))
            return "";
        String sb = toSignStrMap(jo, fiterKey, true) + secret;
        logger.debug("cmd=SignUtils:toSign msg=original Sign:" + sb.toString() + "  req param:" + JSON.toJSONString(jo));

        return Md5.utf8(sb);
    }

    public static String toSignStrMap(JSONObject jo, final String filterKey, boolean filterNull) {
        if (jo == null)
            return "";
        List<String> keysList = new ArrayList<>();
        Set<String> keys = jo.keySet();
        StringBuilder sb = new StringBuilder();
        Iterator iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(filterKey)) {
                if (key.toLowerCase().contains(filterKey.toLowerCase()) || org.apache.commons.lang3.StringUtils.isEmpty(jo.getString(key))) {
                    continue;
                }
            }
            keysList.add(key);
        }
        Collections.sort(keysList);
        for (String key : keysList) {
            sb.append(key + "=" + jo.getString(key)).append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        logger.info(
                "cmd=SignUtils:toSign msg=original Sign:" + sb.toString() + "  req param:" + JSON.toJSONString(jo));

        return sb.toString();
    }
    
    
    public static void main(String[] args) {
		
    	String str = "{\"amount\":\"110\",\"orderNo\":\"d423ac26-c027-4ce5-ad51-267510a10f4b\","
    			+ "\"transTime\":\"20171227222232\",\"sign\":\"a615e13f6b7dac85f7131783fc70e055\","
    			+ "\"merchantNo\":\"ME0000000065\",\"status\":\"1\"}";
		
    	JSONObject jo = JSON.parseObject(str);
    	
    	System.err.println(toSign(jo, "sign", "9efed2a1a4b5fc80486121830eb4a64d"));
	}
    
//
//	/**
//	 *
//	 * @param jo 参数对象
//	 * @param secret 密钥
//	 * @return Sign
//	 */
//	public static String toSignStr(JSONObject jo) {
//		if (jo == null)
//			return "";
//		Set<String> keys = jo.keySet();
//		// List<String> orderKeys = new ArrayList<String>();
//		StringBuilder sb = new StringBuilder();
//		// 除Sign外其他参数都按参数名排序进行参数值拼接后
//		keys.stream().sorted()
//				.filter((s -> !s.equals("sign") && !s.equals("ip")
//						&& (!(jo.get(s) instanceof JSONObject || jo.get(s) instanceof JSONArray)
//					    && StrUtil.isNotNull(jo.getString(s)))))
//				.forEach(s -> sb.append(s).append("=").append(jo.getString(s)).append("&"));
//
//		logger.debug("cmd=SignUtils:toSign msg=original Sign:" + sb.toString() + "  req param:" + JSON.toJSONString(jo));
//
//		int l = sb.length();
//
//		return sb.delete(l - 1, l).toString();
//	}
//
//	public static String toSignStr1(JSONObject jo) {
//		if (jo == null)
//			return "";
//		Set<String> keys = jo.keySet();
//		// List<String> orderKeys = new ArrayList<String>();
//		StringBuilder sb = new StringBuilder();
//		// 除Sign外其他参数都按参数名排序进行参数值拼接后
//		keys.stream().sorted()
//				.filter((s -> !s.equals("ip")
//						&& (!(jo.get(s) instanceof JSONObject || jo.get(s) instanceof JSONArray)
//						&& StrUtil.isNotNull(jo.getString(s)))))
//				.forEach(s -> sb.append(s).append("=").append(jo.getString(s)).append("&"));
//
//		logger.debug("11cmd=SignUtils:toSign msg=original Sign:" + sb.toString() + "  req param:" + JSON.toJSONString(jo));
//
//		int l = sb.length();
//
//		return sb.delete(l - 1, l).toString();
//	}

}
