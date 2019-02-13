package com.cn.tianxia.pay.tx.util;


public class CreateWapOrderRespDto extends AbstractResponseDTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5694484335342927917L;

	private String payNo;          //平台订单号
	
	private String merOrderNo;     //商户订单号
	
	private String jumpUrl;        //跳转URL

	private String realAmt;     //实际支付金额
	
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

	public String getJumpUrl() {
		return jumpUrl;
	}

	public void setJumpUrl(String jumpUrl) {
		this.jumpUrl = jumpUrl;
	}

	public String getRealAmt() {
		return realAmt;
	}

	public void setRealAmt(String realAmt) {
		this.realAmt = realAmt;
	}
	
	
	
	
}
