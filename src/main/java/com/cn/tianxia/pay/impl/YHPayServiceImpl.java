package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.XTUtils;

import net.sf.json.JSONObject;

public class YHPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(YHPayServiceImpl.class);
	/**支付地址*/
	private String payUrl;
	/**商户编号*/
	private String merchantNo;
	/**商户接收支付成功数据的地址*/
	private String notifyUrl;
	/**商户密钥*/
	private String md5Key;
	public YHPayServiceImpl(Map<String,String> data) {
		if(data!=null){
			if(data.containsKey("payUrl")){
				this.payUrl = data.get("payUrl");
			}
			if(data.containsKey("merchantNo")){
				this.merchantNo = data.get("merchantNo");
			}
			if(data.containsKey("notifyUrl")){
				this.notifyUrl = data.get("notifyUrl");
			}
			if(data.containsKey("md5Key")){
				this.md5Key = data.get("md5Key");
			}
		}
	}
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("以合支付扫码支付开始======================START==================");
		try {
			//封装请求参数
			Map<String, String> data = sealRequest(payEntity);
			//生成签名串
			String sign = generatorSign(data);
			data.put("hmac", sign);
			logger.info("以合支付请求参数:"+JSONObject.fromObject(data).toString());
			//生成请求表单
			String resStr = HttpUtils.generatorForm(data, payUrl);
			logger.info("以合支付响应信息:"+resStr);
			if(StringUtils.isBlank(resStr)){
				logger.info("[YH]以合支付扫码支付发起HTTP请求无响应结果");
				return PayResponse.error("[YH]以合支付扫码支付发起HTTP请求无响应结果");
			}
			return PayResponse.sm_form(payEntity, resStr, "下单成功");
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("以合支付生成异常:"+e.getMessage());
			return PayUtil.returnWYPayJson("error", "form", "", "", "");
		}
	}

	@Override
	public String callback(Map<String, String> data) {
		 String sourceSign = data.remove("hmac");
	        if (StringUtils.isBlank(sourceSign)) {
	            logger.info("[YHZF]以合支付回调验签失败：回调签名为空！");
	            return "fail";
	        }
	        if(verifyCallback(sourceSign,data))
	            return "success";
	        return "fail";
	}
	/**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @param type 支付类型  1 网银支付   2 扫码支付
     * @return
     * @throws Exception
     */
	public Map<String, String> sealRequest(PayEntity payEntity) throws Exception{
		 logger.info("[YH]以合支付封装请求参数开始=====================START==================");
		try {
			DecimalFormat    df   = new DecimalFormat("0.00");
			Map<String,String> data = new HashMap<>();
			data.put("p0_Cmd", "Buy");
			data.put("p1_MerId", merchantNo);
			data.put("p2_Order", payEntity.getOrderNo());
			data.put("p3_Amt", df.format(payEntity.getAmount()));
			data.put("p4_Cur", "CNY");
			data.put("p5_Pid", "");
			data.put("p6_Pcat", "");
			data.put("p7_Pdesc", "");
			data.put("p8_Url", notifyUrl);
			data.put("p9_SAF", "0");
			data.put("pa_MP", "");
			data.put("pd_FrpId", payEntity.getPayCode());
			data.put("pr_NeedResponse", "1");
			return data;
		} catch (Exception e) {
			 e.printStackTrace();
	         logger.error("[YH]支付封装请求参数异常:",e.getMessage());
	         throw new Exception("封装支付请求参数异常!");
		}
	}
	/**
     * 
     * @Description 生成签名串
     * @param data
     * @return
	 * @throws Exception
     */
    private String generatorSign(Map<String,String> data) throws Exception{
    	logger.info("[YHZF]以合支付生成支付签名串开始==================START========================");
    	try {
			StringBuilder sb = new StringBuilder();
			sb.append(data.get("p0_Cmd"));
			sb.append(data.get("p1_MerId"));
			sb.append(data.get("p2_Order"));
			sb.append(data.get("p3_Amt"));
			sb.append(data.get("p4_Cur"));
			sb.append(data.get("p5_Pid"));
			sb.append(data.get("p6_Pcat"));
			sb.append(data.get("p7_Pdesc"));
			sb.append(data.get("p8_Url"));
			sb.append(data.get("p9_SAF"));
			sb.append(data.get("pa_MP"));
			sb.append(data.get("pd_FrpId"));
			sb.append(data.get("pr_NeedResponse"));
			//生成待签名串
			String signStr = sb.toString();
			logger.info("[YHZF]以合支付生成待签名串:{}",signStr);
			String sign = XTUtils.hmacSign(sb.toString(),md5Key);
			if (StringUtils.isBlank(sign)) {
			    logger.error("[YHZF]以合支付生成签名串为空！");
			    return null;
			}
			logger.info("[YHZF]以合支付生成加密签名串:{}",sign);
			return sign;
		} catch (Exception e) {
			 e.printStackTrace();
             logger.error("[YHZF]以合支付生成支付签名串异常:"+ e.getMessage());
             throw new Exception("生成支付签名串异常!");
		}
    }
    private boolean verifyCallback(String hmac,Map<String,String> data) {
        StringBuffer sValue = new StringBuffer();
        // 商户编号
        sValue.append(data.get("p1_MerId")==null?"":data.get("p1_MerId"));
        // 业务类型
        sValue.append(data.get("r0_Cmd")==null?"":data.get("r0_Cmd"));
        // 支付结果
        sValue.append(data.get("r1_Code")==null?"":data.get("r1_Code"));
        // 易宝支付交易流水号
        sValue.append(data.get("r2_TrxId")==null?"":data.get("r2_TrxId"));
        // 支付金额
        sValue.append(data.get("r3_Amt")==null?"":data.get("r3_Amt"));
        // 交易币种
        sValue.append(data.get("r4_Cur")==null?"":data.get("r4_Cur"));
        // 商品名称
        sValue.append(data.get("r5_Pid")==null?"":data.get("r5_Pid"));
        // 商户订单号
        sValue.append(data.get("r6_Order")==null?"":data.get("r6_Order"));
        // 易宝支付会员ID
        sValue.append(data.get("r7_Uid")==null?"":data.get("r7_Uid"));
        // 商户扩展信息
        sValue.append(data.get("r8_MP")==null?"":data.get("r8_MP"));
        // 交易结果返回类型
        sValue.append(data.get("r9_BType")==null?"":data.get("r9_BType"));
        String sNewString;
        sNewString = XTUtils.hmacSign(sValue.toString(),md5Key);

        if (hmac.equals(sNewString)) {
            return true;
        }
        return false;
    }
}
