package com.cn.tianxia.pay.tx.util;


public class QueryOrderReqDto extends AbstractRequestDTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8916854292214088134L;

	
	private String merOrderNo;   //商户订单号
	
	public String getMerOrderNo() {
		return merOrderNo;
	}

	public void setMerOrderNo(String merOrderNo) {
		this.merOrderNo = merOrderNo;
	}
	
}
