package com.cn.tianxia.pay.dc.util;

public class DCCallbackRequest {
	String userId; // 用户编号
	String requestId; //
	String payNo;// 得成平台返回交易流水号

	String returnCode;// 000000-成功。其他信息提示码，提示各类相关失败信息
	String message;// 返回码信息提示
	String characterSet; // 字符集
	String ipAddress; // IP地址
	String signType; // 签名方式
	// WX,WX,ZFB,QQ
	String type;// 请求接口类型 扫码
	String version; // 版本号
	String hmac;// 签名数据
	/* 业务参数 */
	String amount;// 订单金额单位为元
	String ordersts;// 成功：S 失败：F 处理中：W
	String totalFee;// 商户合计手续费
	String payAmount;// 应支付金额
	

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getPayNo() {
		return payNo;
	}

	public void setPayNo(String payNo) {
		this.payNo = payNo;
	}

	public String getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCharacterSet() {
		return characterSet;
	}

	public void setCharacterSet(String characterSet) {
		this.characterSet = characterSet;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getSignType() {
		return signType;
	}

	public void setSignType(String signType) {
		this.signType = signType;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getHmac() {
		return hmac;
	}

	public void setHmac(String hmac) {
		this.hmac = hmac;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getOrdersts() {
		return ordersts;
	}

	public void setOrdersts(String ordersts) {
		this.ordersts = ordersts;
	}

	public String getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(String totalFee) {
		this.totalFee = totalFee;
	}

	public String getPayAmount() {
		return payAmount;
	}

	public void setPayAmount(String payAmount) {
		this.payAmount = payAmount;
	}

}
