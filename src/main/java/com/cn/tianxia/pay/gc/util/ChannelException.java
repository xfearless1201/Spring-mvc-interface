package com.cn.tianxia.pay.gc.util;

public class ChannelException extends TfcpayException {

	private static final long serialVersionUID = 1L;

	private final String param;

	public ChannelException(String message, String param, Throwable e) {
		super(message, e);
		this.param = param;
	}

	public String getParam() {
		return param;
	}

}
