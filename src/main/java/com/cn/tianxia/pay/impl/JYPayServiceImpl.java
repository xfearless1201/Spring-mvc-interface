package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;

public class JYPayServiceImpl implements PayService{
	private final static Logger logger = LoggerFactory.getLogger(JYPayServiceImpl.class);
	
    private String memberid;//商户号
    private String key;//结算类型
    private String payUrl;//支付地址
    private String notifyUrl;//异步通知地址
    
    public JYPayServiceImpl(Map<String,String> data){
    	if(data != null && !data.isEmpty()){
            if(data.containsKey("memberid")){
                this.memberid = data.get("memberid");
            }
            if(data.containsKey("key")){
                this.key = data.get("key");
            }
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
            }
        }
    }

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		 logger.info("[JY]九域支付网银支付开始===================START=================");
	        try {
	            //获取支付请求参数
	            Map<String,String> data = sealRequest(payEntity,1);
	            //生成签名串
	            String sign = generatorSign(data);
	            data.put("pay_md5sign",sign);//验签字段 是   MD5加密
	            String html = createForm(data);
	            logger.info("[JY]九域支付请求报文:"+html);
	            //发起HTTP-POST请求
//	            String response = HttpUtils.toPostForm(html, payUrl);
//	            if(StringUtils.isBlank(response)){
//	                logger.error("[JY]九域支付支付失败,请求无响应结果!");
//	                return PayUtil.returnWYPayJson("error", "","", "", "");
//	            }
	            return PayUtil.returnWYPayJson("success", "form", html, payEntity.getPayUrl(), "");
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	            logger.error("[JY]九域支付网银支付异常:"+e.getMessage());
	            return PayUtil.returnWYPayJson("error", "[JY]九域网银支付异常!", "", "", "");
	        }
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String callback(Map<String, String> data) {
		logger.info("[JY]九域支付回调验签开始===================START===================");
        try {
            //获取验签原签名串
            String sourceSign = data.remove("sign");
            
            logger.info("[JY]九域支付验签原签名串:{}", sourceSign);
            
            //生成验签签名
            String sign = generatorSign(data);
            logger.info("[JY]九域支付验签生成签名串:{}", sign);
            if(sourceSign.equalsIgnoreCase(sign))
            	return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[JY]九域支付回调验签异常:{}", e.getMessage());
        }
        return "fail";
	}
	/**
	 * 构建请求表单
	 * 
	 * 
	 */
	private String createForm(Map<String,String> data){
		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ payUrl + "\">";
		for (String key : data.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + data.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";

		System.out.println("九域支付表单:" + FormString);
		return FormString;
	}
	
    /**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @param type 1 网银 
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,Integer type) throws Exception{
        logger.info("[JY]九域支付封装请求参数开始=====================START==================");
        try {
            //创建支付请求参数存储对象
            Map<String,String> data = new HashMap<>();
            //订单时间
            Date orderTime = new Date();
            //订单金额
            String amount = new DecimalFormat("0.00").format(entity.getAmount());//交易1金额 是   分为单位
            data.put("pay_memberid",memberid);//商户号    是   171107105912001
            if(type == 1){
                data.put("pay_bankcode","907");//支付方式编码   907 网银支付 
                data.put("pay_bankid", entity.getPayCode());
                data.put("pay_productname", "TOP-UP");//商品名称
            }
            data.put("pay_orderid",entity.getOrderNo());//订单号    是   20位长度唯一订单标识
            data.put("pay_applydate",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(orderTime));
            data.put("pay_amount",amount);//交易1金额 是   元为单位
            data.put("pay_notifyurl",notifyUrl);//后台回调通知地址 是   后台回调通知地址
            data.put("pay_callbackurl",entity.getPayUrl());//页面通知地址   否   页面通知地址 h5支付必传
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[JY]九域支付封装请求参数异常:"+e.getMessage());
            throw new Exception("封装支付请求参数异常!");
        }
    }
    
    /**
     * 
     * @Description 生成支付签名串
     * @param data
     * @return
     * @throws Exception
     */
    public String generatorSign(Map<String,String> data) throws Exception{
        logger.info("[JY]九域支付生成支付签名串开始==================START========================");
        try {
             //签名规则:
            //把除签名字段和集合字段以外的所有字段（不包括值为null的）内容按照报文字段字典顺序，
            //依次按照“字段名=字段值”的方式用“&”符号连接，最后加上机构工作密钥，使用MD5算法计算数字签名，填入签名字段。接受方应按响应步骤验证签名。
            Map<String,String> treemap = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            treemap.putAll(data);
            
            //生成待签名串
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = treemap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = treemap.get(key);
                
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign") || key.equalsIgnoreCase("signature") || key.equalsIgnoreCase("pay_bankid") 
                		|| key.equalsIgnoreCase("pay_productname") || key.equalsIgnoreCase("attach")) 
                	continue;
                sb.append(key).append("=").append(val).append("&");
            }
            
            //生成待签名串
            String signStr = sb.toString()+"key="+key;
            logger.info("[JY]九域支付生成待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[JY]九域支付生成加密签名串:"+sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[JY]支付生成支付签名串异常:"+e.getMessage());
            throw new Exception("生成支付签名串异常!");
        }
    }
    
    
    /**
     * 
     * @Description 格式化map参数
     * @param data
     * @return
     * @throws Exception
     */
    public String formatMapToString(Map<String,String> data) throws Exception{
        logger.info("[JY]九域支付请求参数类型转换开始====================START====================");
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("{");
            Iterator<String> iterator = data.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String val = data.get(key);
                sb.append('"').append(key).append('"').append(":");
                if(StringUtils.isNoneBlank(val)){
                    sb.append('"').append(val).append('"');
                }else{
                    sb.append('"').append('"');
                }
                if(iterator.hasNext()){
                    sb.append(",");
                }
            }
            sb.append("}");
            String reqParams = sb.toString();
            return reqParams;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[JY]九域支付请求参数类型转换异常:"+e.getMessage());
            throw new Exception("请求参数转换类型异常!");
        }
    }
    
	public static void main(String[] args) throws Exception {
		Map<String,String> initParam = new HashMap<>();
		initParam.put("memberid", "180891370");
		initParam.put("key", "nwd8pwfhunf58x9jxyr3q5rnfcryfytc");
		initParam.put("payUrl", "https://www.9-epay.com/Pay_Index.html");
		initParam.put("notifyUrl", "www.baidu.com");
		JYPayServiceImpl jypay = new JYPayServiceImpl(initParam);
//		PayEntity payEntity = new PayEntity();
//		payEntity.setAmount(12.56);
//		payEntity.setPayCode("ICBC");
//		payEntity.setOrderNo("0000000111");
//		payEntity.setRefererUrl("http://10.0.12.68/6666");
//		payEntity.setPayUrl("http://10.1.1.1/ssssss");
//		JSONObject result = jypay.wyPay(payEntity);
//		System.out.print(result);
		Map<String,String> resulteMap = new HashMap<>();
        resulteMap.put("amount", "100");
        resulteMap.put("datetime", "20181124145953");
        resulteMap.put("memberid", "180891370");
        resulteMap.put("orderid", "JYbl1201811241021431021431183");
        resulteMap.put("returncode", "00");
        resulteMap.put("transaction_id", "JYbl1201811241021431021431183");
        resulteMap.put("sign", "xxxx");
        resulteMap.put("attach", "hhhhh");
		String sign = jypay.generatorSign(resulteMap);
		System.out.print(sign);
	
        
	}

}
