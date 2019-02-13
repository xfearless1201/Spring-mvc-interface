package com.cn.tianxia.pay.tx.util;


public class QueryOrderRespDto extends AbstractResponseDTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8242420971973932211L;

    private String payNo;          //平台订单号
	
	private String merOrderNo;     //商户订单号
	
	private String payStatus;      //支付状态 N:订单登记 I支付中 S:支付成功 F:支付失败
	
	private String payDate;        //支付日期
	
	private String payTime;        //支付时间
	
	private String orderTitle;     //订单标题
	
	private String orderDesc;      //订单描述

	private String orderAmt;      //订单金额
	
	private String realAmt;       //实际支付金额
	public String getPayNo() {
		return payNo;
	}

	public void setPayNo(String payNo) {
		this.payNo = payNo;
	}

	public String getMerOrderNo() {
		return merOrderNo;
	}

	public void setMerOrderNo(String merOrderNo) {
		this.merOrderNo = merOrderNo;
	}

	public String getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(String payStatus) {
		this.payStatus = payStatus;
	}

	public String getPayDate() {
		return payDate;
	}

	public void setPayDate(String payDate) {
		this.payDate = payDate;
	}

	public String getPayTime() {
		return payTime;
	}

	public void setPayTime(String payTime) {
		this.payTime = payTime;
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

	public String getOrderAmt() {
		return orderAmt;
	}

	public void setOrderAmt(String orderAmt) {
		this.orderAmt = orderAmt;
	}

	public String getRealAmt() {
		return realAmt;
	}

	public void setRealAmt(String realAmt) {
		this.realAmt = realAmt;
	}
	
	
}
