package com.cn.tianxia.pay.xq.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import net.sf.json.JSONObject;

public class StringUtils {
	/*
	 * ���������� �̻���+ʱ��+6��������(��20λ)
	 */
	public static String produceOrderNo(String MerNo) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
		Date d = new Date();
		String str1 = df.format(d).toString();
		String str2 = "";
		for (int i = 0; i < 6; i++) {
			str2 += (int) (Math.random() * 10);
		}
		String str3 = MerNo + str1;
		return str3;
	}

	/**
	 * �ж��ַ����Ƿ�Ϊ��
	 * <ul>
	 * <li>isEmpty(null) = true</li>
	 * <li>isEmpty("") = true</li>
	 * <li>isEmpty(" ") = true</li>
	 * <li>isEmpty("abc") = false</li>
	 * </ul>
	 * 
	 * @param value
	 *            Ŀ���ַ���
	 * @return true/false
	 */
	public static boolean isEmpty(String value) {
		int strLen;
		if (value == null || (strLen = value.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(value.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * ������mapתStr(key=val&key=val......)
	 * 
	 * @param packageParams
	 * @return
	 */
	public static String createRetStr(Map<String, String> packageParams) {
		StringBuffer content = new StringBuffer();
		List<String> keys = new ArrayList<String>(packageParams.keySet());
		Collections.sort(keys);
		StringBuffer sb = new StringBuffer();
		Set<?> es = packageParams.entrySet();
		Iterator<?> it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = String.valueOf(entry.getValue());
			if (null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
			}
		}
		String sss = sb.toString().substring(sb.toString().length() - 1, sb.toString().length());
		if (sss.equals("&")) {
			sss = sb.toString().substring(0, sb.toString().length() - 1);
		}
		return sss;
	}
	
	public static String createRetStr(Map<String, String> packageParams,String serverSign) {
		StringBuffer content = new StringBuffer();
		List<String> keys = new ArrayList<String>(packageParams.keySet());
		Collections.sort(keys);
		StringBuffer sb = new StringBuffer();
		Set<?> es = packageParams.entrySet();
		Iterator<?> it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = String.valueOf(entry.getValue());
//			if (null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
//			}
		}
		String sss = sb.toString().substring(sb.toString().length() - 1, sb.toString().length());
		if (sss.equals("&")) {
			sss = sb.toString().substring(0, sb.toString().length() - 1);
		}
		return sss;
	}


	/**
	 * ���˿ռ���ֵ��������
	 * 
	 * @param sArray
	 * @return
	 */
	public static Map<String, Object> paraFilter(Map<String, Object> sArray) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (sArray == null || sArray.size() <= 0) {
			return result;
		}
		for (String key : sArray.keySet()) {
			Object value = sArray.get(key);
			if (value == null || value.equals("")) {
				continue;
			}
			result.put(key, value);
		}
		return result;
	}

	/**
	 * ƴ���ַ�key=val&key=val......
	 * 
	 * @param params
	 * @return
	 */
	public static String createLinkString(Map<String, Object> params, String keyValue) {
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		String prestr = "";
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			Object value = params.get(key);
			if (i == keys.size() - 1) {// ƴ��ʱ�����������һ��&�ַ�
				prestr = prestr + key + "=" + value;
			} else {
				prestr = prestr + key + "=" + value + "&";
			}
		}

		return prestr + "&key=" + keyValue;
	}

	/**
	 * ��ȡǩ���ַ���
	 * 
	 * @return
	 */
	public static String getSignStr(Map<String, String> params, String keyValue) {
		List<String> keys = new ArrayList<String>(params.keySet());
		// Collections.sort(keys);
		String prestr = "";
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			Object value = params.get(key);
			if (i == keys.size() - 1) {// ƴ��ʱ�����������һ��&�ַ�
				prestr = prestr + key + "=" + value + "";
			} else {
				prestr = prestr + key + "=" + value + "&";
			}
		}
		return prestr + keyValue;
	}

	/**
	 * <һ�仰���ܼ���> <������ϸ����>requestת�ַ���
	 * 
	 * @param request
	 * @return
	 * @see [�ࡢ��#��������#��Ա]
	 */
	public static String parseRequst(HttpServletRequest request) {
		String body = "";
		try {
			ServletInputStream inputStream = request.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
			while (true) {
				String info = br.readLine();
				if (info == null) {
					break;
				}
				if (body == null || "".equals(body)) {
					body = info;
				} else {
					body += info;
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return body;
	}

	/**
	 * ���RequestתMap
	 * 
	 * @param paraMap
	 * @return
	 */
	public static Map<String, String> getResultMap(Map paraMap) {
		// ����ֵMap
		Map returnMap = new HashMap();
		Iterator entries = paraMap.entrySet().iterator();
		Map.Entry entry;
		String name = "";
		String value = "";
		while (entries.hasNext()) {
			entry = (Map.Entry) entries.next();
			name = (String) entry.getKey();
			Object valueObj = entry.getValue();
			if (null == valueObj) {
				value = "";
			} else if (valueObj instanceof String[]) {
				String[] values = (String[]) valueObj;
				for (int i = 0; i < values.length; i++) {
					value = values[i] + ",";
				}
				value = value.substring(0, value.length() - 1);
			} else {
				value = valueObj.toString();
			}
			returnMap.put(name, value);
		}
		return returnMap;
	}

	/**
	 * ����XML�ַ���
	 * 
	 * @param xml
	 * @return
	 * @throws DocumentException
	 */
	public static Map<String, Object> parseXmlStr(String xml) throws DocumentException {
		Document document = DocumentHelper.parseText(xml);
		Element root = document.getRootElement();
		return parseElement(root);
	}

	/**
	 * ����Element
	 * 
	 * @param root
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> parseElement(Element root) {
		Iterator<Element> rootItor = root.elementIterator();
		Map<String, Object> rMap = new HashMap<>();
		while (rootItor.hasNext()) {
			Element tmpElement = rootItor.next();
			String name = tmpElement.getName();
			String content = tmpElement.getText();
			rMap.put(name, content);
		}
		return rMap;
	}

	/**
	 * Stringת��Map
	 * 
	 * @param rasStr
	 */
	public static Map<String, String> StrToMap(String rasStr) {
		TreeMap<String, String> map = null;
		if (rasStr != null && rasStr != "") {
			map = new TreeMap<String, String>();
			String[] s1 = rasStr.split("&");
			String[] s2 = new String[2];
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < s1.length; i++) {
				s2 = s1[i].split("=", 2);
				map.put(s2[0], s2[1]);
				if (!s2[0].equals("sign")) {
					sb.append(s2[0] + "=" + s2[1] + "&");
				}
			}
		}
		return map;
	}

	/**
	 * JSonObjectת
	 * 
	 * @param object
	 * @return
	 */
	public static HashMap<String, String> JsonToMap(Object object) {
		HashMap<String, String> data = new HashMap<String, String>();
		// ��json�ַ���ת����jsonObject
		JSONObject jsonObject = JSONObject.fromObject(object);
		Iterator it = jsonObject.keys();
		// ����jsonObject���ݣ���ӵ�Map����
		while (it.hasNext()) {
			String key = String.valueOf(it.next());
			String value = (String) jsonObject.get(key);
			data.put(key, value);
		}
		return data;
	}

	/**
	 * ����ʱ���ַ��� yyyy��MM��dd�� ����
	 * 
	 * @param dateTime
	 * @return
	 */
	public String getDateymd(Date dateTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(dateTime).toString();
	}

	/**
	 * ����ʱ���ַ���HHmmss����
	 * 
	 * @param dateTime
	 * @return
	 */
	public String getDatehms(Date dateTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
		return sdf.format(dateTime).toString();
	}

	/**
	 * ����ʱ���ַ��� yyyy��MM��dd�� hhʱmm��ss������
	 * 
	 * @param dateTime
	 * @return
	 */
	public static String getDateymdhms(Date dateTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		return sdf.format(dateTime).toString();
	}

	public static String parseXML(Map<String, String> parameters) {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version='1.0' encoding='utf-8'?>");
		sb.append("<ScanPayRequest>");
		Set es = parameters.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (null != v && !"".equals(v) && !"appkey".equals(k)) {
				sb.append("<" + k + ">" + parameters.get(k) + "</" + k + ">\n");
			}
		}
		sb.append("</ScanPayRequest>");
		// sb.append("</xml>");
		return sb.toString();
	}

	public static void main(String[] args) {
		System.out.println(produceOrderNo("HJKM"));
	}
}
