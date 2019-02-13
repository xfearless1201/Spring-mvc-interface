package com.cn.tianxia.pay.gc.util;

public abstract class TfcpayException extends Exception {
	private static final long serialVersionUID = 1L;

	public TfcpayException(String message) {
		super(message, null);
	}

	public TfcpayException(String message, Throwable e) {
		super(message, e);
	}

	

}
