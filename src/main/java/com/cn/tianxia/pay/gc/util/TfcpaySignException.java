package com.cn.tianxia.pay.gc.util;

/***
 * 验签异常
 * @author lihejia
 */
public class TfcpaySignException extends TfcpayException {

	private static final long serialVersionUID = 1L;

	private final String param;
	
	public TfcpaySignException(String message,String param) {
		super(message);
		this.param = param;
	}
	public String getParam() {
		return param;
	}
}
