package com.cn.tianxia.pay.yczf.util;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class SignUtil {

	public static Map<String, String> parseParam(HttpServletRequest request, String charset) throws UnsupportedEncodingException {
		if (request == null) {
			return null;
		}
		Map<String, String> reqMap = new LinkedHashMap<String, String>();
		Enumeration<String> enu = request.getParameterNames();
		while (enu.hasMoreElements()) {
			String paramName = (String) enu.nextElement();
			String paramValue = request.getParameter(paramName);
			reqMap.put(paramName, URLDecoder.decode(paramValue, charset));
		}
		return reqMap;
	}
	/**
	 * 接收数据后，对返回参数值进行处理
	 * 
	 * @param respData
	 * @param base64Keys
	 * @param urlParamConnectFlag
	 * @param charset
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static Map<String, String> parseResponse(String respData, String[] base64Keys, String urlParamConnectFlag, String charset) throws UnsupportedEncodingException {
		Map<String, String> map = new LinkedHashMap<String, String>();
		String[] str = respData.split(urlParamConnectFlag);
		// 4.1 对所有参数值调用URLDecoder.decode进行utf-8解码
		for (int i = 0; i < str.length; i++) {
			int z = str[i].indexOf("=");
			map.put(str[i].substring(0, z), URLDecoder.decode(str[i].substring(z + 1, str[i].length()), charset));
		}
		//4.2 对特殊参数值进行处理 先把其中的”%2b”替换为“+”然后Base64解码
		SignUtil.convertRespData(map, base64Keys);
		return map;
	}
	/**
	 * 1.获取签名数据
	 * @param map
	 * @param urlParamConnectFlag
	 * @param removeKey
	 * @return
	 */
	public static String getSignMsg(Map<String, String> map, String urlParamConnectFlag, Set<String> removeKey) {
		return getURLParam(map, urlParamConnectFlag, true, removeKey);
	}
	/**
	 * 计算签名
	 * @param signMethod
	 * @param signedMsg
	 * @param key
	 * @param charSet
	 * @return
	 */
	public static String sign(String signMethod, String signedMsg, String key, String charSet) {
		try {
			String[] algArray = { "MD5", "SHA1", "SHA256", "SHA512" };
			String algorithm = null;
			for (int i = 0; i < algArray.length; i++) {
				if (algArray[i].equalsIgnoreCase(signMethod)) {
					algorithm = algArray[i];
					break;
				}
			}

			if (algorithm == null) {
				return null;
			}
			return Md5Util.md5(signedMsg + key, charSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 验证签名
	 * @param signMethod
	 * @param signedMsg
	 * @param hmac
	 * @param key
	 * @param charSet
	 * @return
	 */
	public static boolean verifySign(String signMethod, String signedMsg, String hmac, String key, String charSet) {
		try {
			if (null == hmac || null == signedMsg) {
				return false;
			}
			return hmac.equalsIgnoreCase(sign(signMethod, signedMsg, key, charSet));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String getURLParam(Map<String, String> map, String urlParamConnectFlag, boolean isSort, Set<String> removeKey) {
		StringBuffer param = new StringBuffer();
		List<String> msgList = new ArrayList<String>();
		for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			String value = (String) map.get(key);
			if ((removeKey == null) || (!removeKey.contains(key))) {
				msgList.add(key + "=" + toEmpty(value));
			}
		}

		if (isSort) {
			Collections.sort(msgList);
		}

		for (int i = 0; i < msgList.size(); i++) {
			String msg = (String) msgList.get(i);
			if (i > 0) {
				param.append(urlParamConnectFlag);
			}
			param.append(msg);
		}

		return param.toString();
	}

	public static String getAsynNotifySignMsg(Map<String, String> map, String urlParamConnectFlag, Set<String> removeKey) {
		String msg = null;
		try {
			msg = getAsynNotifyURLParam(map, urlParamConnectFlag, true, removeKey);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return msg;
	}

	public static String getAsynNotifyURLParam(Map<String, String> map, String urlParamConnectFlag, boolean isSort, Set<String> removeKey) throws UnsupportedEncodingException {
		StringBuffer param = new StringBuffer();
		List<String> msgList = new ArrayList<String>();
		for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			String value = (String) map.get(key);
			if ((removeKey == null) || (!removeKey.contains(key))) {
				if (key.equals("PlatTxMsg")) {
					msgList.add(key + "=" + URLDecoder.decode(toEmpty(value), "utf-8"));
				} else {
					msgList.add(key + "=" + toEmpty(value));
				}
			}
		}

		if (isSort) {
			Collections.sort(msgList);
		}

		for (int i = 0; i < msgList.size(); i++) {
			String msg = (String) msgList.get(i);
			if (i > 0) {
				param.append(urlParamConnectFlag);
			}
			param.append(msg);
		}

		return param.toString();
	}

	public static String toEmpty(String aStr) {
		return aStr == null ? "" : aStr;
	}

	/**
	 * 对请求参数值进行处理
	 * 3.1 对特殊参数先进行Base64编码然后把其中的”+”替换为“%2b”
	 * 3.2 对所有参数值进行url的utf-8 编码。
	 * @param map
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getWebForm(Map<String, String> map, String[] base64Keys, String charset, String urlParamConnectFlag) throws UnsupportedEncodingException {
		if (null == map || map.keySet().size() == 0) {
			return null;
		}
		if (charset == null || urlParamConnectFlag == null)
			return null;
		//3.1 对特殊参数先进行Base64编码然后把其中的”+”替换为“%2b”
		SignUtil.convertReqData(map, base64Keys, charset);
		//3.2 对所有参数值调用URLEncoder.encode进行utf-8编码
		StringBuffer url = new StringBuffer();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String value = entry.getValue();
			String str = (value != null ? value : "");
			str = URLEncoder.encode(str, "UTF-8");
			url.append(entry.getKey()).append("=").append(str).append(urlParamConnectFlag);
		}

		String strURL = null;
		strURL = url.toString();
		if (strURL != null && urlParamConnectFlag.equals(String.valueOf(strURL.charAt(strURL.length() - 1)))) {
			strURL = strURL.substring(0, strURL.length() - 1);
		}
		return strURL;
	}

	/**
	 * 转换特殊请求字段
	 * 3.1  对特殊参数的值先进行Base64编码然后把其中的”+”替换为“%2b”
	 * 
	 * @param paramMap
	 */
	public static void convertReqData(Map<String, String> paramMap, String[] base64Keys, String charset) {
		if (base64Keys == null)
			return;
		for (int i = 0; i < base64Keys.length; i++) {
			String key = base64Keys[i];
			String value = (String) paramMap.get(key);
			if (value != null) {
				try {
					String text = Base64.encode(value.getBytes(charset)).replace("+", "%2b");
					// 更新请求参数
					paramMap.put(key, text);
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * 4.2 对返回的特殊参数值进行处理 先把其中的”%2b”替换为“+”然后Base64解码
	 * 转换特殊返回字段
	 * 
	 * @param paramMap
	 */
	public static void convertRespData(Map<String, String> paramMap, String[] base64Keys) {
		if (base64Keys == null)
			return;
		for (int i = 0; i < base64Keys.length; i++) {
			String key = base64Keys[i];
			String value = (String) paramMap.get(key);
			if (value != null) {
				try {
					String text = new String(Base64.decode(value.replace("%2b", "+")));
					// 更新请求参数
					paramMap.put(key, text);
				} catch (Exception e) {
				}
			}
		}
	}

	/*
	 * 获取请求地址
	 */
	public static String parseUrlFromGetReqData(String reqdata) {
		if (reqdata == null)
			return null;
		int s = reqdata.indexOf("?");
		if (s == -1) {
			return null;
		}
		return reqdata.substring(0, s);
	}

	/*
	 * 获取请求参数
	 */
	public static Map<String, String> parseReqMapFromGetReqData(String reqdata) {
		if (reqdata == null)
			return null;
		int s = reqdata.indexOf("?");
		if (s == -1) {
			return null;
		}
		String data = reqdata.substring(s + 1, reqdata.length());
		if (data == null)
			return null;
		String[] dataMap = data.split("&");
		if (dataMap == null || dataMap.length < 1)
			return null;
		int index = -1;
		String key = null;
		String value = null;

		Map<String, String> map = new LinkedHashMap<String, String>();
		for (int i = 0; i < dataMap.length; i++) {
			index = dataMap[i].indexOf("=");
			key = dataMap[i].substring(0, index);
			value = dataMap[i].substring(index + 1, dataMap[i].length());
			map.put(key, value);
		}
		return map;
	}
}