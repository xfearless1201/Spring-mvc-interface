package com.cn.tianxia.pay.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;
import com.cn.tianxia.pay.wfzf.util.HttpUtil;
//import com.cn.tianxia.pay.wfzf.util.HttpUtil;
import com.cn.tianxia.pay.wfzf.util.MD5Util;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

/**
 * 五福支付接口
 * 
 * @author hb
 * @date 2018-05-26
 */
public class WFZFPayServiceImpl implements PayService{
	private final static Logger logger = LoggerFactory.getLogger(WFZFPayServiceImpl.class);

	/**支付地址*/
	private String payUrl ;
	/**回调地址*/
	private String notifyUrl ;
	/**商户号*/
	private String merId ;
	/**商户密钥*/
	private String merKey ;
	/**商品名称*/
	private String pName ;
	
	public WFZFPayServiceImpl() {
		
	}
	
	public WFZFPayServiceImpl(Map<String, String> pmap) {
		if(pmap != null) {
			if (pmap.containsKey("payUrl")) {//支付地址
				this.payUrl = pmap.get("payUrl");
			}
			if (pmap.containsKey("notifyUrl")) {//支付通知地址
				this.notifyUrl = pmap.get("notifyUrl");
			}
			if (pmap.containsKey("merId")) {//商户号
				this.merId = pmap.get("merId");
			}
			if (pmap.containsKey("merKey")) {//商户密钥
				this.merKey = pmap.get("merKey");
			}
			if (pmap.containsKey("pName")) {//商品名称
				this.pName = "pay";//pmap.get("pName");
			}
		}
	}

	private void qryOrder() throws Exception {
		String qryUrl = "https://pay.8331vip.com/payApi";
		String svcName = "UniThirdPay";
		String merId = "WF70060";
		String merchOrderId = "WFZFbl1201805271400491400493789";
		String key = "AL2AXCSZ20ZHGQA18XW2V6LE37R1GKHU";
		
		Map<String, String> md5Params = new TreeMap<>();
		md5Params.put("svcName", svcName);
		md5Params.put("merId", merId);
		md5Params.put("merchOrderId", merchOrderId);
		
		StringBuilder sb = new StringBuilder();
		for(String k : md5Params.keySet()) {
			sb.append(md5Params.get(k));
		}
		sb.append(key);
		
		String md5Value = MD5Util.encode(sb.toString()).toUpperCase();
		
		Map<String, String> qryParams = new HashMap<>();
		qryParams.put("svcName", svcName);
		qryParams.put("merId", merId);
		qryParams.put("merchOrderId", merchOrderId);
		qryParams.put("md5value", md5Value);
		
		String resultStr = doPost(qryUrl,qryParams,null); //HttpUtil.RequestForm(qryUrl, qryParams);
		System.out.println(resultStr);
	}
	
	
	public String doPost(String reqUrl, Map<String, String> parameters,
			String recvEncoding) throws Exception{
		HttpClient client=HttpClients.createDefault();
		
		HttpPost post=new HttpPost(reqUrl);
		List<NameValuePair> nvps=new ArrayList<NameValuePair>();
		Iterator<Entry<String, String>> iterator=parameters.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, String> entry=iterator.next();
			nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		post.setEntity(new UrlEncodedFormEntity(nvps,"gbk"));
		CloseableHttpResponse response=(CloseableHttpResponse)client.execute(post);
		HttpEntity clientEntity=response.getEntity();
		BufferedReader reader=new BufferedReader(new InputStreamReader(clientEntity.getContent()));
		String lines;
		StringBuilder sb=new StringBuilder();
		while ((lines = reader.readLine()) != null) {
			sb.append("\n" + lines);
		}
		HttpClientUtils.closeQuietly(response);
		HttpClientUtils.closeQuietly(client);
		return sb.toString();
	}
	
	/*public static void main(String[] args) {
		PayEntity entity = new PayEntity();
		entity.setAmount(43);
		entity.setOrderNo("WFZFbl1201805271619021619027057");
		entity.setPayCode("QQ_NATIVE");
		entity.setRefererUrl("http://182.16.110.186/");
		
		WFZFPayServiceImpl wfzf = new WFZFPayServiceImpl();
		wfzf.merId="WF70060";
		wfzf.merKey="AL2AXCSZ20ZHGQA18XW2V6LE37R1GKHU";
		wfzf.notifyUrl="http://182.16.110.186:8080/XPJ/Notify/WfzfNotify.do";
		wfzf.pName="pay";
		
		wfzf.smPay(entity);
	}*/
	
	
	@Override
	public JSONObject smPay(PayEntity payEntity) {
		
		Map<String , String> paramMap = new TreeMap<String , String>();
		paramMap.put("merId",this.merId);
		paramMap.put("pName",this.pName);
		paramMap.put("notifyUrl",this.notifyUrl);
		
		String merchOrderId = payEntity.getOrderNo();//订单号
		String tranType = payEntity.getPayCode();//支付通道
		String retUrl = payEntity.getRefererUrl();//页面地址
		String userName = payEntity.getUsername();
		
		String svcName = getSvcName(tranType);
		paramMap.put("svcName",svcName);
		paramMap.put("tranType",tranType);
		paramMap.put("merchOrderId",merchOrderId);
		paramMap.put("merData",svcName+"_"+tranType+"_"+retUrl);
		
		if("WEIXIN_NATIVE".equals(tranType)) {
			paramMap.put("showCashier", "1");
		}
		
		double amt = payEntity.getAmount();
		paramMap.put("amt",String.valueOf((int)amt*100));
		paramMap.put("retUrl",retUrl);
		
		logger.info("生成签名参数 paramMap = "+ paramMap);
		
		StringBuilder sb = new StringBuilder();
		for(String key : paramMap.keySet()) {
			sb.append(paramMap.get(key));
		}
		sb.append(this.merKey);
		logger.info("签名字符串 = "+ sb);
		
		//接口签名
		String md5Value = MD5Util.encode(sb.toString()).toUpperCase();
		paramMap.put("md5value", md5Value);
		logger.info("all paramMap =  "+ paramMap);
		
		String resultstr = null;
		//native方式处理 
		if(isNative(tranType) && !"WEIXIN_NATIVE".equals(tranType)) {
			resultstr = HttpUtil.RequestForm(this.payUrl, paramMap);
			if(isJson(resultstr)) {
				JSONObject responseJson = JSONObject.fromObject(resultstr);
				if (responseJson.containsKey("retCode") && "000000".equals(responseJson.getString("retCode"))) {
					logger.info("native支付请求成功："+responseJson);
					return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amt, merchOrderId, responseJson.getString("payUrl"));
				}else{
					logger.info("native支付请求失败："+responseJson);
					return getReturnJson("error", "", responseJson.toString());
				}
			}
			else {
				logger.info("支付失败 = "+ resultstr);
				return getReturnJson("error", "", resultstr); 
			}
		}
		//非native方式处理，或者
		else {
			String formStr = buildForm(paramMap, payUrl);
			logger.info("支付form表单："+formStr);
			return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, amt, merchOrderId, formStr);
		}
	}
	
	private boolean isNative(String tranType) {
		if(tranType.endsWith("NATIVE")) {
			return true;
		}
		return false;
	}
	
	private String buildWeixinTMForm(String html) {
	     //待请求参数数组
        String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................";
        FormString+= "<form  id=\"actform\" name=\"actform\" method=\"post\" action='' onsubmit='return false'>";
        FormString += html;
		FormString += "</form></body>";

        return FormString;
	}
	
	//pcQuickPay,wapQuickPay,UniThirdPay 
	private String getSvcName(String tranType) {
		if("2000047".equals(tranType)) {
			return "pcQuickPay";
		}
		if("2000048".equals(tranType)) {
			return "wapQuickPay";
		}
		return "UniThirdPay";
	}

	/**
	 * 
	 * @Title: buildRequest  
	 * @Description: 建立请求，以表单HTML形式构造（默认）
	 * @param @param sParaTemp 请求参数数组
	 * @param @param strMethod 提交方式。两个值可选：post、get
	 * @param @param strButtonName 确认按钮显示文字
	 * @param @param actionUrl	提交表单HTML文本
	 * @param @return    参数  
	 * @return String    返回类型  
	 * @throws
	 */
	public String buildForm(Map<String, String> paramMap, String payUrl) {
        //待请求参数数组
        String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""+ payUrl + "\">";
		for (String key : paramMap.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramMap.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";

        return FormString;
    }

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * 判断是否是json结构
	 */
	public static boolean isJson(String value) {
		try {
			JSONObject.fromObject(value);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public static void main(String[] args) throws Exception {

		JSONObject paramJson = JSONObject.fromObject("{tranTime='20180527 16:26:10', amt='4300',  merData='UniThirdPay_QQ_NATIVE_http://182.16.110.186/',  md5value='ACF86467DC10D53391173CAAC62C34B8',  status='0', merchOrderId='WFZFbl1201805271619021619027057', orderId='I201805270007821342', orderStatusMsg='交易成功'} ");
		Map<String, String> params = new TreeMap<>();
		for(Object key : paramJson.keySet()) {
			params.put((String)key, paramJson.getString((String)key));
		}
		System.out.println(params);
		
		String notifyUrl = "http://localhost:8080/JJF/Notify/WfzfNotify.do";
		
		String result = HttpUtil.RequestForm(notifyUrl, params);
		System.out.println(result);
	/*	JSONObject paramJson = JSONObject.fromObject("{tranTime='20180527 16:23:04', amt='4300', merData='UniThirdPay_QQ_NATIVE_http://182.16.110.186/', md5value='C1DD44823891755B8B8E4EF23FD7710D', status='0', merchOrderId='WFZFbl1201805271619021619027057', orderId='I201805270007821342', orderStatusMsg='交易成功'}");	
		Map<String, String> params = new TreeMap<>();
		for(Object key : paramJson.keySet()) {
			params.put((String)key, paramJson.getString((String)key));
		}
		System.out.println(params);
		StringBuilder sb = new StringBuilder(); 
		for(String key : params.keySet()) {
			if("md5value".equalsIgnoreCase(key)) {
				continue;
			}
			sb.append(params.get(key));
		}
		sb.append("AL2AXCSZ20ZHGQA18XW2V6LE37R1GKHU");
		String omd5 = params.get("md5value");
		String md5 = MD5Util.encode(sb.toString()).toUpperCase();
		System.out.println(omd5);
		System.out.println(md5);*/
	}

	/**
	 * 回调验签
	 * @param infoMap
	 * @param request
	 * @param response
	 * @return
	 */
	@Override
	public String callback(Map<String, String> infoMap) {
	    try {
	        String sourceSign = infoMap.get("md5value");
	        logger.info("[WFZF]五福支付原签名串:{}",sourceSign);
	        StringBuilder sb = new StringBuilder();
            //排序
	        Map<String,String> sortmap = MapUtils.sortByKeys(infoMap);
	        Iterator<String> iterator = sortmap.keySet().iterator();
	        while(iterator.hasNext()){
	            String key = iterator.next();
	            String val = sortmap.get(key);
	            if(StringUtils.isBlank(val) || "md5value".equalsIgnoreCase(key)) continue;
	            sb.append(val);
	        }
	        sb.append(merKey);
	        String signStr = sb.toString();
	        logger.info("[WFZF]五福支付生成待加密串:{}",signStr);
	        String sign = MD5Utils.md5toUpCase_32Bit(signStr);
	        logger.info("[WFZF]五福支付生成加密串:{}",sign);
	        if(sign.equalsIgnoreCase(sourceSign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[WFZF]五福支付回调验签异常:{}",e.getMessage());
        }
	    return "faild";
	}
	
	/**
	 * 结果返回
	 * 
	 * @param status
	 * @param qrCode
	 * @param msg
	 * @return
	 */
	private JSONObject getReturnJson(String status, String qrCode, String msg) {
		JSONObject json = new JSONObject();
		json.put("status", status);
		json.put("qrCode", qrCode);
		json.put("msg", msg);
		return json;
	}
	
}
