package com.cn.tianxia.pay.tjf.utils;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * 类名：RefundResponseEntity 功能：响应信息实体类
 * 
 * 以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 * 该代码仅供学习和研究支付接口使用，只是提供一个参考。
 * 
 */

public class RefundResponseEntity {

	protected String respCode;
	protected String respDesc;
	protected String respAmt;                
	protected String signMsg;
	protected String qrCode;

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

	public String getQrCode() {
		return qrCode;
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}

    public String getRespAmt() {
		return respAmt;
	}

	public void setRespAmt(String respAmt) {
		this.respAmt = respAmt;
	}

	public void parse(String respStr) throws Exception {
		Map<String, String> resultMap = new HashMap<String, String>();
		XMLParserUtil.parse(respStr, resultMap);
		Document doc = DocumentHelper.parseText(respStr);
		Element root = doc.getRootElement();
		Element respData = root.element("detail");
		String srcData = respData.asXML();
		respCode = resultMap.get("/message/detail/code");
		if (StringUtils.isBlank(respCode)) {
			throw new Exception("响应信息格式错误：不存在'code'节点。");
		}
		respDesc = resultMap.get("/message/detail/desc");
		if (StringUtils.isBlank(respDesc)) {
			throw new Exception("响应信息格式错误：不存在'desc'节点");
		}
		respAmt = resultMap.get("/message/detail/Amt");
		/*if (StringUtils.isBlank(respAmt)) {
			throw new Exception("响应信息格式错误：不存在'Amt'节点");
		}*/
		
		signMsg = resultMap.get("/message/sign");
		if (StringUtils.isBlank(signMsg)) {
			throw new Exception("响应信息格式错误：不存在'sign'节点");
		}
		if (!SignUtil.verifyData(getSignMsg(), srcData)) {
			throw new Exception("签名验证不通过");
		}
		qrCode = resultMap.get("/message/detail/qrCode");
	}

}
