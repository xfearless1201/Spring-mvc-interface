package com.cn.tianxia.pay.gc.util;

/***
 * 业务异常
 * @author lihejia
 */
public class TfcpayBussinessException extends TfcpayException {

	private static final long serialVersionUID = 1L;

	private final String param;
	
	public TfcpayBussinessException(String message,String param) {
		super(message);
		this.param = param;
	}
	public String getParam() {
		return param;
	}
}
