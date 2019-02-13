package com.cn.tianxia.pay.bft.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class MerchantBalanceQueryResponseEntity {
	
	protected String respCode;
	protected String respDesc;
	protected String signMsg;
	protected String amt;
	protected String freezeAmt;
	
	public void parse(String respStr) throws Exception {
		Map<String, String> resultMap = new HashMap<String, String>();
		UtilXMLParser.parse(respStr, resultMap);
		respCode = resultMap.get("/moboAccount/respData/respCode");
		if (StringUtils.isBlank(respCode)) {
			throw new Exception("响应信息格式错误：不存在'respCode'节点。");
		}
		respDesc = resultMap.get("/moboAccount/respData/respDesc");
		if (StringUtils.isBlank(respDesc)) {
			throw new Exception("响应信息格式错误：不存在'respDesc'节点");
		}
		amt = resultMap.get("/moboAccount/respData/Amt");
		if (StringUtils.isBlank(amt)) {
			throw new Exception("响应信息格式错误：不存在'amt'节点");
		}
		freezeAmt = resultMap.get("/moboAccount/respData/freezeAmt");
		if (StringUtils.isBlank(freezeAmt)) {
			throw new Exception("响应信息格式错误：不存在'freezeAmt'节点");
		}
		signMsg = resultMap.get("/moboAccount/signMsg");
		if (StringUtils.isBlank(signMsg)) {
			throw new Exception("响应信息格式错误：不存在'signMsg'节点");
		}
	}



	public String getRespCode() {
		return respCode;
	}



	public void setRespCode(String respCode) {
		this.respCode = respCode;
	}



	public String getRespDesc() {
		return respDesc;
	}



	public void setRespDesc(String respDesc) {
		this.respDesc = respDesc;
	}



	public String getSignMsg() {
		return signMsg;
	}



	public void setSignMsg(String signMsg) {
		this.signMsg = signMsg;
	}



	public String getAmt() {
		return amt;
	}



	public void setAmt(String amt) {
		this.amt = amt;
	}



	public String getFreezeAmt() {
		return freezeAmt;
	}



	public void setFreezeAmt(String freezeAmt) {
		this.freezeAmt = freezeAmt;
	}

	

	
	
	
	
}
