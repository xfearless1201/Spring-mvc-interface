package com.cn.tianxia.pay.bft.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class CustRealPayResponseEntity {
	protected String respCode;
	protected String respDesc;
	protected String signMsg;
	protected String accDate;
	protected String accNo;
	protected String batchNo;

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

	public String getAccDate() {
		return accDate;
	}

	public void setAccDate(String accDate) {
		this.accDate = accDate;
	}

	public String getAccNo() {
		return accNo;
	}

	public void setAccNo(String accNo) {
		this.accNo = accNo;
	}
	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
	public void parse(String respStr) throws Exception {
		Map<String, String> resultMap = new HashMap<String, String>();
		UtilXMLParser.parse(respStr, resultMap);
		Document doc = DocumentHelper.parseText(respStr);
		Element root = doc.getRootElement();
		Element respData = root.element("respData");
		String srcData = respData.asXML();
		respCode = resultMap.get("/moboAccount/respData/respCode");
		if (StringUtils.isBlank(respCode)) {
			throw new Exception("响应信息格式错误：不存在'respCode'节点。");
		}
		respDesc = resultMap.get("/moboAccount/respData/respDesc");
		if (StringUtils.isBlank(respDesc)) {
			throw new Exception("响应信息格式错误：不存在'respDesc'节点");
		}
//		signMsg = resultMap.get("/moboAccount/signMsg");
//		if (StringUtils.isBlank(signMsg)) {
//			throw new Exception("响应信息格式错误：不存在'signMsg'节点");
//		}
//		if (!Mobo360SignUtil.verifyData(getSignMsg(), srcData)) {
//			throw new Exception("签名验证不通过");
//		}
		accDate = resultMap.get("/moboAccount/respData/accDate");
		accNo = resultMap.get("/moboAccount/respData/accNo");
	}
	
	
	public void parseDS(String respStr) throws Exception {
		Map<String, String> resultMap = new HashMap<String, String>();
		UtilXMLParser.parse(respStr, resultMap);
		Document doc = DocumentHelper.parseText(respStr);
		Element root = doc.getRootElement();
		Element respData = root.element("respData");
		String srcData = respData.asXML();
		respCode = resultMap.get("/moboAccount/respData/respCode");
		if (StringUtils.isBlank(respCode)) {
			throw new Exception("响应信息格式错误：不存在'respCode'节点。");
		}
		respDesc = resultMap.get("/moboAccount/respData/respDesc");
		if (StringUtils.isBlank(respDesc)) {
			throw new Exception("响应信息格式错误：不存在'respDesc'节点");
		}
//		signMsg = resultMap.get("/moboAccount/signMsg");
//		if (StringUtils.isBlank(signMsg)) {
//			throw new Exception("响应信息格式错误：不存在'signMsg'节点");
//		}
//		if (!Mobo360SignUtil.verifyData(getSignMsg(), srcData)) {
//			throw new Exception("签名验证不通过");
//		}
		accDate = resultMap.get("/moboAccount/respData/accDate");
		batchNo = resultMap.get("/moboAccount/respData/batchNo");
	}
	
	
	
	
}
