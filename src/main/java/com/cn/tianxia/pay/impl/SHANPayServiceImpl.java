package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.MapUtils;
import com.cn.tianxia.pay.utils.SHANUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName SHANPayServiceImpl
 * @Description 闪付
 * @author Hardy
 * @Date 2018年12月22日 上午10:16:37
 * @version 1.0.0
 */
public class SHANPayServiceImpl implements PayService {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(SHANPayServiceImpl.class);
    
    private String merno;//商户号
    
    private String payUrl;//支付地址
    
    private String notifyUrl;//回调地址
    
    private String reqSecret;//请求秘钥
    
    private String resSecret;//响应秘钥
    
    public SHANPayServiceImpl(Map<String,String> data) {
        if(MapUtils.isNotEntity(data)){
            if(data.containsKey("merno")){
                this.merno = data.get("merno");
            }
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
            }
            if(data.containsKey("reqSecret")){
                this.reqSecret = data.get("reqSecret");
            }
            if(data.containsKey("resSecret")){
            	this.resSecret = data.get("resSecret");
            }
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[SHAN]闪付网银支付开始==================START==================");
        try {
            
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 1);
            //生成签名串
            String sign = generatorSign(data,1,reqSecret);
            data.put("sign", sign);
            logger.info("[SHAN]闪付网银支付请求参数报文:{}",JSONObject.fromObject(data).toString());
            //发起HTTP请求
            String response = SHANUtils.sendHttpReq(payUrl, JSONObject.fromObject(data).toString(), "UTF-8");
            if(StringUtils.isBlank(response)){
                logger.info("[SHAN]闪付网银支付失败,发起HTTP请求无响应结果");
                return PayResponse.error("[SHAN]闪付网银支付失败,发起HTTP请求无响应结果");
            }
            logger.info("[SHAN]闪付网银支付失败,发起HTTP请求响应结果:{}",response);
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(jsonObject.containsKey("code") && "20000".equals(jsonObject.getString("code"))){
                //下单成功
                String htmlStr = jsonObject.getJSONObject("data").getString("pay_html");
                return PayResponse.wy_write(htmlStr);
            }
            return PayResponse.wy_write("下单失败:"+response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[SHAN]闪付网银支付异常:{}",e.getMessage());
            return PayResponse.error("[SHAN]闪付网银支付异常");
        }
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[SHAN]闪付支付回调验签开始=================START======================");
        try {
            String sourceSign = data.get("sign");
            logger.info("[SHAN]闪付获取上送签名:{}",sourceSign);
            String sign = generatorSign(data, 0,resSecret);
            logger.info("[SHAN]闪付获取服务器签名:{}",sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[SHAN]闪付支付回调验签异常:{}",e.getMessage());
        }
        return "faild";
    }

    
    /**
     * 
     * @Description 组装支付请求参数
     * @param entity
     * @param type 1 网银   其他 扫码
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,int type) throws Exception{
        logger.info("[SHAN]闪付支付组装支付请求参数开始================START=================");
        try {
            Map<String,String> data = new HashMap<>();
            
            String amount = new DecimalFormat("##").format(entity.getAmount()*100);
            
            data.put("out_trade_no",entity.getOrderNo());//商户订单号 最大长度30  Y   字符串，只允许使用字母、数字、- 、_,并以字母或数字开头，每商户提交的订单号，必须在自身账户交易中唯一
            data.put("amount",amount);//商户订单金额  数字  Y   单位（分）
            data.put("subject","TOP-UP");//订单标题   最大长度50  Y   
            data.put("merchant_id",merno);//商户id       Y   商户在平台的唯一标识
            if(type == 1){
                data.put("biz_code","3001");//业务代码      Y   3001：网关支付
                data.put("bank_code",entity.getPayCode());//银行代码     Y   支持部分银行（该字段不参与签名）
            }
//            data.put("body","");//订单详细信息    最大长度256 N   
//            data.put("extra","");//订单附加信息   最大长度1024    N   
            data.put("notify_url",notifyUrl);//异步回调地址  长度最大256 N   接收异步通知地址，合法URL
            data.put("sign","");//签名        Y   以上必填字段参与签名
            data.put("version", "2");
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[SHAN]闪付支付组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[SHAN]闪付支付组装支付请求参数异常");
        }
    }
    
    /**
     * 
     * @Description 生成签名串
     * @param data
     * @param type 1 支付 其他 回调
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data, int type, String secret) throws Exception{
        logger.info("[SHAN]闪付生成签名串开始=====================START=================");
        try {
            SortedMap<String, Object> orderedMap = new TreeMap<String, Object>();
            Set<String> pKeys = data.keySet();
            if(type == 1){
                for (String key : pKeys) {
                    if (data.get(key) != null && !"sign".equalsIgnoreCase(key) 
                            && !"notify_url".equalsIgnoreCase(key) && !"bank_code".equalsIgnoreCase(key) && 
                            !"version".equalsIgnoreCase(key)) {
                        orderedMap.put(key, data.get(key));
                    }
                }
            }else{
                for (String key : pKeys) {
                    if (data.get(key) != null && !"sign".equalsIgnoreCase(key)) {
                        orderedMap.put(key, data.get(key));
                    }
                }
            }
            pKeys = orderedMap.keySet();
            List<String> temp = new ArrayList<String>();
            for (String key : pKeys) {
                temp.add(key + "=" + orderedMap.get(key));
            }
            return SHANUtils.parse(StringUtils.join(temp.toArray(), "&") + secret,"UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[SHAN]闪付生成签名串异常:{}",e.getMessage());
            throw new Exception("[SHAN]闪付生成签名串异常");
        }
    }
    
}
