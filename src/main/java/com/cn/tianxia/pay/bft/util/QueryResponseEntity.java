package com.cn.tianxia.pay.bft.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class QueryResponseEntity {

	protected String respCode;
	protected String respDesc;
	protected String accDate;
	protected String accNo;
	protected String orderNo;
	protected String status;
	protected String signMsg;

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

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSignMsg() {
		return signMsg;
	}

	public void setSignMsg(String signMsg) {
		this.signMsg = signMsg;
	}

	private static Map<String, String> ORDER_STATUS = new HashMap<String, String>();
	static {
		//0-未支付 1-成功 2-失败 4-部分退款 5-全额退款 9-退款处理中 10-未支付 11-订单过期
		ORDER_STATUS.put("0", "未支付");
		ORDER_STATUS.put("1", "成功");
		ORDER_STATUS.put("2", "失败");
		ORDER_STATUS.put("4", "部分退款");
		ORDER_STATUS.put("5", "全额退款");
		ORDER_STATUS.put("9", "退款处理中");
		ORDER_STATUS.put("10", "未支付");
		ORDER_STATUS.put("11", "订单过期");
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
		if (("00").equalsIgnoreCase(respCode)) {
			
			accDate = resultMap.get("/moboAccount/respData/accDate");
			if (StringUtils.isBlank(accDate)) {
				throw new Exception("响应信息格式错误：不存在'accDate'节点。");
			}
			accNo = resultMap.get("/moboAccount/respData/accNo");
			if (StringUtils.isBlank(accNo)) {
				throw new Exception("响应信息格式错误：不存在'accNo'节点。");
			}
			orderNo = resultMap.get("/moboAccount/respData/orderNo");
			if (StringUtils.isBlank(orderNo)) {
				throw new Exception("响应信息格式错误：不存在'orderNo'节点。");
			}
			status = resultMap.get("/moboAccount/respData/Status");
			if (StringUtils.isBlank(status)) {
				throw new Exception("响应信息格式错误：不存在'status'节点。");
			} else {
				status = ORDER_STATUS.get(status);
			}
		}
		signMsg = resultMap.get("/moboAccount/signMsg");
		if (StringUtils.isBlank(signMsg)) {
			throw new Exception("响应信息格式错误：不存在'signMsg'节点");
		}
		if(!Mobo360SignUtil.verifyData(getSignMsg(), srcData))
		{
			throw new Exception("签名验证不通过");
		}
	}

}
