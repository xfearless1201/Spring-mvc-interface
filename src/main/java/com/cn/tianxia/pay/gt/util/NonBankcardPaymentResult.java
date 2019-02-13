package com.cn.tianxia.pay.gt.util;
/**
 * 闈為摱琛屽崱鏀粯缁撴灉
 * @author lu.li
 *
 */
public class NonBankcardPaymentResult {
	private String cmd;			// 
	private String code;			// 
	private String order;		// 
	private String returnmsg;	// 
	private String sign;			// 
	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	public String getReturnmsg() {
		return returnmsg;
	}
	public void setReturnmsg(String returnmsg) {
		this.returnmsg = returnmsg;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	
	
}
