package com.cn.tianxia.pay.impl;

import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;

import net.sf.json.JSONObject;

public class HANYPayServiceImpl implements PayService {
    private final static Logger logger = LoggerFactory.getLogger(HANYPayServiceImpl.class);
    /**支付地址*/
	private String payUrl;
	/**机构商户号*/
	private String orgMemberid;
	/**瀚银商户号*/
	private String hyMemberid;
	/**异步回调地址*/
	private String notifyUrl;
	/**机构号*/
	private String orgNo;
	/**商户密钥*/
	private String md5Key;
	/**构造器*/
	public HANYPayServiceImpl(Map<String,String> data,String type) {
		if(MapUtils.isNotEntity(data)){
            if(data.containsKey(type)){
                JSONObject jsonObject = JSONObject.fromObject(data.get(type));
                if(jsonObject.containsKey("payUrl")){
                    this.payUrl = jsonObject.getString("payUrl");
                }
                if(jsonObject.containsKey("orgMemberid")){
                    this.orgMemberid = jsonObject.getString("orgMemberid");
                }
                if(jsonObject.containsKey("hyMemberid")){
                    this.hyMemberid = jsonObject.getString("hyMemberid");
                }
                if(jsonObject.containsKey("notifyUrl")){
                    this.notifyUrl = jsonObject.getString("notifyUrl");
                }
                if(jsonObject.containsKey("orgNo")){
                	this.orgNo = jsonObject.getString("orgNo");
                }
                if(jsonObject.containsKey("md5Key")){
                	this.md5Key = jsonObject.getString("md5Key");
                }
            }
        }
	}
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		logger.info("[HANY]瀚银支付网银支付开始------------------------------------");
		try {
			Map<String, String> data = sealRequest(payEntity, 1);
			data = generatorSign(data,payEntity,1);
			logger.info("[HANY]瀚银支付网银支付请求参数："+JSONObject.fromObject(data).toString());
			String resStr = HttpUtils.generatorForm(data, payUrl);
			logger.info("[HANY]瀚银支付网银支付响应信息："+resStr);
			if(StringUtils.isBlank(resStr)){
				logger.info("[HANY]瀚银支付网银支付发起HTTP请求无响应结果");
				return PayResponse.error("[HANY]瀚银支付网银支付发起HTTP请求无响应结果");
			}
			return PayResponse.wy_form(payEntity.getPayUrl(), resStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("[HANY]瀚银支付扫码支付开始------------------------------------");
		try {
			Map<String, String> data = sealRequest(payEntity, 0);
			data = generatorSign(data,payEntity,0);
			logger.info("[HANY]瀚银支付扫码支付请求参数："+JSONObject.fromObject(data).toString());
			String resStr = HttpUtils.generatorForm(data, payUrl);
			logger.info("[HANY]瀚银支付扫码支付响应信息："+resStr);
			if(StringUtils.isBlank(resStr)){
				logger.info("[HANY]瀚银支付扫码支付发起HTTP请求无响应结果");
				return PayResponse.error("[HANY]瀚银支付扫码支付发起HTTP请求无响应结果");
			}
			return PayResponse.wy_form(payEntity.getPayUrl(), resStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String callback(Map<String, String> data) {
		try {
			String sourceSign = data.get("signature");
			StringBuilder sb = new StringBuilder();
			List<String> commonAttr = commonAttr();
			for (String attr : commonAttr) {
				sb.append(data.get(attr)).append("|");
			}
			sb.append(data.get("transSeq")).append("|");
			sb.append(data.get("transCharge")).append("|");
			sb.append(data.get("settleDate")).append("|");
			sb.append(data.get("merReserve")).append("|");
			sb.append(data.get("statusCode")).append("|");
			sb.append(data.get("statusMsg")).append("|");
			sb.append(md5Key);//密钥
			//生成待签名串
			String signStr = sb.toString();
			logger.info("[HANY]瀚银验签生成待签名串:{}",signStr);
			String sign = MD5Utils.md5toUpCase_32Bit(signStr);
			logger.info("[HANY]瀚银验签生成加密签名串:{}",sign);
			if(sign.equals(sourceSign)) return "success";
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			logger.info("[HANY]瀚银支付验签失败："+e.getMessage());
		}
		return null;
	}
	/**
     * 
     * @Description (TODO这里用一句话描述这个方法的作用)
     * @param entity
     * @param type 1 网银支付  0 扫码支付
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity payEntity,Integer type)throws Exception{
		try {
			DecimalFormat df = new DecimalFormat("0");
			String uid = UUID.randomUUID().toString();
			SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDDhhmmss");
			String toDate = sdf.format(new Date());
			Map<String, String> data = new HashMap<>();
			if(type==1){
				data.put("signType", "MD5");//签名方法
				data.put("encoding", "UTF-8");//编码方式
				data.put("version", "2.0.0");//版本号
				data.put("insCode", orgNo);//机构号
				data.put("insMerchantCode", orgMemberid);//机构商户号
				data.put("hpMerCode", hyMemberid);//瀚银商户号
				data.put("nonceStr",uid.replace("-", ""));//随机参数
				data.put("orderNo", payEntity.getOrderNo());//商户订单号
				data.put("orderDate", toDate.substring(0, 8));//商户订单日期YYYYMMDD
				data.put("orderTime", toDate);//商户订单发送时间YYYYMMDDhhmmss
				data.put("orderAmount", df.format(payEntity.getAmount()*100));//订单金额
				data.put("currencyCode", "156");//币种
				data.put("paymentChannel", "ABC");//银行代码
				data.put("frontUrl", notifyUrl);//前台通知地址
				data.put("backUrl", notifyUrl);//后台异步通知地址
				data.put("merReserve", "");//商户自定义域
				data.put("ledger", "");//分账域
				data.put("riskArea", "");//风控域
				data.put("gatewayProductType", "B2C");//网关支付产品类型
				data.put("accNoType", "DEBIT");//卡类型 CREDIT:贷记卡 DEBIT:借记卡
				data.put("clientType", "");//客户端类型01:PC浏览器  02:手机浏览器  03:手机APP 99:其他
			}else{
				data.put("insCode", orgNo);//机构号
				data.put("insMerchantCode", orgMemberid);//机构商户号
				data.put("hpMerCode", hyMemberid);//瀚银商户号
				data.put("orderNo", payEntity.getOrderNo());//商户订单号
				data.put("orderTime", toDate);//商户订单发送时间YYYYMMDDhhmmss
				data.put("orderAmount", df.format(payEntity.getAmount()*100));//订单金额 单位：分
				data.put("currencyCode", "156");//币种
				data.put("name", "");//姓名
				data.put("idNumber", "");//身份证号
				data.put("accNo", "");//卡号
				data.put("telNo", "");//手机号
				data.put("productType","100000");//产品类型
				data.put("paymentType", "2008");//支付类型
				data.put("merGroup", "");//商户类型
				data.put("nonceStr",uid.replace("-", ""));//随机参数
				data.put("frontUrl", notifyUrl);//前台通知地址
				data.put("backUrl", notifyUrl);//后台异步通知地址
			}
			
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("[HANY]瀚银支付组装请求参数异常："+e.getMessage());
			return null;
		}
    }
    
    /**
     * 
     * @Description 生成签名串
     * @param data
     * @return
     * @throws Exception
     */
    private Map<String,String> generatorSign(Map<String,String> data,PayEntity payEntity,Integer type) throws Exception{
    	try {
    		StringBuilder sb = new StringBuilder();
    		if(type==1){
    			List<String> commonAttr = commonAttr();
        		for (String attr : commonAttr) {
        			sb.append(attr+"=").append(data.get(attr)).append("&");
    			}
        		sb.append("paymentChannel=").append(data.get("paymentChannel")).append("&");
        		sb.append("frontUrl=").append(data.get("frontUrl")).append("&");
        		sb.append("backUrl=").append(data.get("backUrl")).append("&");
        		sb.append("merReserve=").append(data.get("merReserve")).append("&");
        		sb.append("ledger=").append(data.get("ledger")).append("&");
        		sb.append("riskArea=").append(data.get("riskArea")).append("&");
        		sb.append("gatewayProductType=").append(data.get("gatewayProductType")).append("&");
        		sb.append("accNoType=").append(data.get("accNoType")).append("&");
        		sb.append("clientType=").append(data.get("clientType")).append("&");
    			sb.append("signKey=").append(md5Key);//密钥
    		}else{
    			sb.append(data.get("insCode")).append("|");
    			sb.append(data.get("insMerchantCode")).append("|");
    			sb.append(data.get("hpMerCode")).append("|");
    			sb.append(data.get("orderNo")).append("|");
    			sb.append(data.get("orderTime")).append("|");
    			sb.append(data.get("currencyCode")).append("|");
    			sb.append(data.get("orderAmount")).append("|");
    			sb.append(data.get("name")).append("|");
    			sb.append(data.get("idNumber")).append("|");
    			sb.append(data.get("accNo")).append("|");
    			sb.append(data.get("telNo")).append("|");
    			sb.append(data.get("productType")).append("|");
    			sb.append(data.get("paymentType")).append("|");
    			sb.append(data.get("merGroup")).append("|");
    			sb.append(data.get("nonceStr")).append("|");
    			sb.append(data.get("frontUrl")).append("|");
    			sb.append(data.get("backUrl")).append("|");
    			sb.append(md5Key);//密钥
    		}
			//生成待签名串
			String signStr = sb.toString();
			logger.info("[HANY]瀚银支付生成待签名串:{}",signStr);
			String sign = MD5Utils.md5toUpCase_32Bit(signStr);
			logger.info("[HANY]瀚银支付生成加密签名串:{}",sign);
			data.put("signature", sign);//签名
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("[HANY]瀚银支付生成签名异常："+e.getMessage());
			return null;
		}
    }
    private List<String> commonAttr(){
    	String[] attrArray={"signType","encoding","version","insCode","insMerchantCode","hpMerCode","nonceStr",
    			"orderNo","orderDate","orderTime","orderAmount","currencyCode"};
    	List<String> list = new ArrayList<>();
    	for (String attrStr : attrArray) {
			list.add(attrStr);
		}
    	return list;
    }
   /* public Map<String, String> bankCodeChoose(){
    	String[] realBankCode={"ICBC","ABC","BOCSH","CCB","CMB","SPDB","GDB","BOCOM","CNCB","CMBC","CIB","CEB","HXB","BOS","PSBC","BCCB","PAB"};
    	String[] bankCode={"ICBC","ABC","BOC","CCB","CMB","SPDB","CGB","BOCM","CITIC","CMBC","CIB","CEB","HXB","SHBANK","PSBC","BCCB","PAYH"};
    	Map<String, String> map = new HashMap<>();
    	for (int i=0;i<realBankCode.length;i++) {
    		map.put(bankCode[i], realBankCode[i]);
		}
    	return map;
    }*/
}
