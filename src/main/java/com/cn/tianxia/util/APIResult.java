package com.cn.tianxia.util;

import java.util.HashMap;
import java.util.Map;

public class APIResult {
	/**
	 * 
	 * @Description:TODO
	 * 
	 * @author:zouwei
	 * 
	 * @time:2017年6月13日 上午9:57:50
	 * 
	 */
	private String code;
	private String message;
	private String url;
	private Map<String, Object> params = new HashMap<String, Object>();

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
}
