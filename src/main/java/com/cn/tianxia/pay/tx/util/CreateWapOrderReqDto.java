package com.cn.tianxia.pay.tx.util;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


public class CreateWapOrderReqDto extends AbstractRequestDTO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7679702185376526964L;

	@NotNull(message="商户订单号[merOrderNo]必输")
	@Size(min=1,max=64,message="商户订单号[merOrderNo]长度有误")
	private String merOrderNo;   //商户订单号
	
	@NotNull(message="交易金额[orderAmt]必输")
	private String orderAmt;     //交易金额  元为单位
	
	@NotNull(message="支付平台[payPlat]必输")
	@Size(min=1,max=20,message="支付平台[payPlat]长度有误")
	private String payPlat;      //支付平台 alipay:支付宝  wxpay:微信支付
	
	@NotNull(message="订单标题[orderTitle]必输")
	@Size(min=1,max=64,message="订单标题[orderTitle]长度有误")
	private String orderTitle;   //订单标题
	
	@NotNull(message="订单描述[orderDesc]必输")
	@Size(min=1,max=128,message="订单描述[orderDesc]长度有误")
	private String orderDesc;    //订单描述
	
	@NotNull(message="后台通知url[notifyUrl]必输")
	@Size(min=1,max=128,message="后台通知url[notifyUrl]长度有误")
	private String notifyUrl;     //后台通知url

	@Size(min=1,max=128,message="页面跳转url[callbackUrl]长度有误")
	private String callbackUrl;     //后台通知url
	
	public String getMerOrderNo() {
		return merOrderNo;
	}

	public void setMerOrderNo(String merOrderNo) {
		this.merOrderNo = merOrderNo;
	}

	public String getOrderAmt() {
		return orderAmt;
	}

	public void setOrderAmt(String orderAmt) {
		this.orderAmt = orderAmt;
	}

	public String getPayPlat() {
		return payPlat;
	}

	public void setPayPlat(String payPlat) {
		this.payPlat = payPlat;
	}

	public String getOrderTitle() {
		return orderTitle;
	}

	public void setOrderTitle(String orderTitle) {
		this.orderTitle = orderTitle;
	}

	public String getOrderDesc() {
		return orderDesc;
	}

	public void setOrderDesc(String orderDesc) {
		this.orderDesc = orderDesc;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}
	
	public String toString(){
		return super.toString();
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}
	
	
}
