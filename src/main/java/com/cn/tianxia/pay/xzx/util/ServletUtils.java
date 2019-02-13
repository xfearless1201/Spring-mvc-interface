package com.cn.tianxia.pay.xzx.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


public class ServletUtils {

	/**
	 * 
	 * @Title: writerJson
	 * @param response
	 * @param str
	 *            void
	 * @throws
	 */
	public static void writerJson(HttpServletResponse response, String str) {
		writerText(response,"utf-8", "application/x-json", str);
	}

	public static void writerJson(HttpServletResponse response,
			String characterEncoding, String str) {
		writerText(response, characterEncoding, "application/x-json", str);
	}

	/**
	 * 
	 * @Title: writerXml
	 * @param response
	 * @param str
	 *            void
	 * @throws
	 */

	public static void writerXml(HttpServletResponse response, String str) {
		writerText(response, "utf-8", "text/xml", str);
	}

	public static void writerXml(HttpServletResponse response,
			String characterEncoding, String str) {
		writerText(response, characterEncoding, "text/xml", str);
	}

	/**
	 * 
	 * @Title: writerText
	 * @param response
	 * @param str
	 *            void
	 * @throws
	 */

	public static void writerText(HttpServletResponse response, String str) {
		writerText(response, "utf-8", "text/plain", str);
	}

	public static void writerText(HttpServletResponse response,
			String characterEncoding, String str) {
		writerText(response, characterEncoding, "text/plain", str);
	}

	/**
	 * 
	 * @Title: writerText
	 * @param response
	 * @param characterEncoding
	 * @param contentType
	 * @param str
	 *            void
	 * @throws
	 */
	public static void writerText(HttpServletResponse response,
			String characterEncoding, String contentType, String str) {
		PrintWriter writer = null;
		try {
			response.setCharacterEncoding(characterEncoding);
			response.setContentType(contentType);
			writer = response.getWriter();
			writer.print(str);
			writer.flush();
		} catch (IOException e) {
			// throw e;
			System.err.println(e.getLocalizedMessage());
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * 
	 * @Title: getRequestAttributes
	 * @param request
	 * @return Map<String,Object>
	 * @throws
	 */
	public static Map<String, Object> getRequestAttributes(
			ServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		loadRequestAttributes(map, request);
		return map;
	}

	/**
	 * 
	 * @Title: loadRequestAttributes
	 * @param map
	 * @param request
	 *            void
	 * @throws
	 */

	@SuppressWarnings("rawtypes")
	public static void loadRequestAttributes(Map<String, Object> map,
			ServletRequest request) {
		Enumeration names = request.getAttributeNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			map.put(name, request.getAttribute(name));
		}
	}

	/**
	 * 
	 * @Title: getRequestParameters
	 * @param request
	 * @return Map<String,Object>
	 * @throws
	 */
	public static Map<String, Object> getRequestParameters(
			ServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		loadRequestParameters(map, request);
		return map;
	}

	/**
	 * 
	 * @Title: loadRequestParameters
	 * @param map
	 * @param request
	 *            void
	 * @throws
	 */
	@SuppressWarnings("rawtypes")
	public static void loadRequestParameters(Map<String, Object> map,
			ServletRequest request) {
		Enumeration names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			map.put(name, getRequestParameter(request, name));
		}
	}

	/**
	 * 
	 * @Title: getRequestParameter
	 * @param request
	 * @param name
	 * @return Object
	 * @throws
	 */
	public static Object getRequestParameter(ServletRequest request, String name) {
		String[] values = request.getParameterValues(name);
		if (values == null) {
			return null;
		}
		if (values.length == 1) {
			return values[0];
		}
		return values;
	}

	/**
	 * 
	 * @Title: getUrlSuffix
	 * @param url
	 * @return String
	 * @throws
	 */
	public static String getUrlSuffix(String url) {
		if (StringUtils.isBlank(url)) {
			return null;
		}
		if (StringUtils.indexOf(url, "?") != -1) {
			if (StringUtils.endsWith(url, "?")
					|| StringUtils.endsWith(url, "&")) {
				return "";
			} else {
				return "&";
			}
		} else {
			return "?";
		}
	}

	/**
	 * 
	 * @Title: buildParameterString
	 * @param params
	 * @param paramNames
	 * @return String
	 * @throws
	 */
	public static String buildParamString(Map<String, Object> params,
			String[] paramNames) {
		if (ArrayUtils.isEmpty(paramNames)) {
			return "";
		}
		ToStringBuilder builder = new ToStringBuilder(null,
				new MapToStringStyle());
		for (String paramName : paramNames) {
			if (params.containsKey(paramName)) {
				builder.append(paramName, params.get(paramName));
			}
		}
		builder.getStringBuffer().setLength(
				builder.getStringBuffer().length() - 1);
		return builder.toString();
	}

	/**
	 * 
	 * @Title: buildParamString
	 * @param params
	 * @return String
	 * @throws
	 */
	public static String buildParamString(Map<String, Object> params) {
		ToStringBuilder builder = new ToStringBuilder(null,
				new MapToStringStyle());
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			builder.append(entry.getKey(), entry.getValue());
		}
		if (builder.getStringBuffer().length() > 0) {
			builder.getStringBuffer().setLength(
					builder.getStringBuffer().length() - 1);
		}
		return builder.toString();
	}
}

/**
 * 
 * @project fx-message-transformation
 * @modify comment
 * @version
 */
class MapToStringStyle extends ToStringStyle {

	private static final long serialVersionUID = 1L;

	MapToStringStyle() {
		setUseClassName(false);
		setContentEnd("");
		setContentStart("");
		setUseIdentityHashCode(false);
		setNullText("");
		setFieldSeparator("&");
		setArrayStart("");
		setArrayEnd("");
	}
}
