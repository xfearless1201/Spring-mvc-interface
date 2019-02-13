package com.cn.tianxia.pay.bft.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class ScanPayResponseEntity {
	
	protected String respCode;
	protected String respDesc;
	protected String signMsg;
	protected String codeUrl;

	
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
		codeUrl = resultMap.get("/moboAccount/respData/codeUrl");
		signMsg = resultMap.get("/moboAccount/signMsg");
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


	public String getCodeUrl() {
		return codeUrl;
	}


	public void setCodeUrl(String codeUrl) {
		this.codeUrl = codeUrl;
	}
	
	
	
	
}
