package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.entity.QueryOrderVO;
import com.cn.tianxia.pay.po.OrderResponse;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName STZFPayServiceImpl
 * @Description 速通支付
 * @author Hardy
 * @Date 2018年11月18日 下午2:51:12
 * @version 1.0.0
 */
public class STZFPayServiceImpl implements PayService{
    
    //日志 
    private static final Logger logger = LoggerFactory.getLogger(STZFPayServiceImpl.class);
    
    private String MerchantId;//商户号
    
    private String payUrl;//支付请求地址
    
    private String queryUrl;//查询地址
    
    private String notifyUrl;//回调请求地址
    
    private String secret;//秘钥
    
    //构造器,初始化参数
    public STZFPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("MerchantId")){
                this.MerchantId = data.get("MerchantId");
            }
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
            }
            if(data.containsKey("secret")){
                this.secret = data.get("secret");
            }
            if(data.containsKey("queryUrl")){
                this.queryUrl = data.get("queryUrl");
            }
        }
    }
    
    /**
     * 网银支付
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[STZF]速通支付网银支付开始=====================START======================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity,1);
            //生成支付签名串
            String sign = generatorSign(data);
            data.put("sign", sign);
            logger.info("[STZF]速通支付网银支付请求报文:{}",JSONObject.fromObject(data).toString());
            String response = HttpUtils.toPostForm(data, payUrl);
            if(StringUtils.isBlank(response)){
                logger.info("[STZF]速通支付网银支付发起HTTP请求无响应结果!");
                return PayResponse.error("[STZF]速通支付网银支付发起HTTP请求无响应结果,请联系第三方...");
            }
            logger.info("[STZF]速通支付网银支付响应结果报文:{}",response);
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            
            if(jsonObject.containsKey("code") && "0".equals(jsonObject.getString("code"))){
                String payUrl = JSONObject.fromObject(jsonObject.getString("data")).getString("payUrl");
                return PayResponse.wy_link(payUrl);
            }
            //支付失败
            String msg = jsonObject.getString("msg");
            return PayResponse.error("支付失败原因:["+msg+"]");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[STZF]速通支付网银支付异常:{}",e.getMessage());
            return PayResponse.error("[STZF]速通支付网银支付异常:"+e.getMessage());
        }
    }
    
    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[STZF]速通支付扫码支付开始=====================START======================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity,0);
            //生成支付签名串
            String sign = generatorSign(data);
            data.put("sign", sign);
            logger.info("[STZF]速通支付扫码支付请求报文:{}",JSONObject.fromObject(data).toString());
            String response = HttpUtils.toPostForm(data, payUrl);
            if(StringUtils.isBlank(response)){
                logger.info("[STZF]速通支付扫码支付发起HTTP请求无响应结果!");
                return PayResponse.error("[STZF]速通支付扫码支付发起HTTP请求无响应结果,请联系第三方...");
            }
            logger.info("[STZF]速通支付扫码支付响应结果报文:{}",response);
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            
            if(jsonObject.containsKey("code") && "0".equals(jsonObject.getString("code"))){
                String payUrl = JSONObject.fromObject(jsonObject.getString("data")).getString("payUrl");
                return PayResponse.sm_link(payEntity, payUrl, "下单成功");
            }
            //支付失败
            String msg = jsonObject.getString("msg");
            return PayResponse.error("支付失败原因:["+msg+"]");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[STZF]速通支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[STZF]速通支付扫码支付异常:"+e.getMessage());
        }
    }
    
    /**
     * 回调
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[STZF]速通支付回调验签开始=====================START======================");
        try {
            //获取回调通知原签名串
            String sourceSign = data.get("sign");
            logger.info("[STZF]速通支付回调验签原签名串:{}",sourceSign);
            String sign = generatorSign(data);
            logger.info("[STZF]速通支付回调加密签名串:{}",sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[STZF]速通支付回调验签异常:{}",e.getMessage());
        }
        return "faild";
    }
    
    /**
     * 
     * @Description 查询订单支付状态
     * @return
     * @throws Exception
     */
    public JSONObject queryOrderStatus(QueryOrderVO order) throws Exception {
        logger.info("[STZF]速通支付查询订单状态开始==================START==================");
        try {
            //创建订单查询参数存储对象
            Map<String,String> data = new HashMap<>();
            data.put("mch_id", MerchantId);
            data.put("out_trade_no", order.getOrderNo());
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign", sign);
            
            logger.info("[STZF]速通支付查询订单请求报文:{}",JSONObject.fromObject(data).toString());
            
            //发起查询请求
            String response = HttpUtils.toPostJson(data, queryUrl);
            if(StringUtils.isBlank(response)){
                logger.info("[STZF]速通支付发起订单查询HTTP请求无响应结果!");
                return OrderResponse.error("[STZF]速通支付发起订单查询HTTP请求无响应结果");
            }
            logger.info("[STZF]速通支付发起订单查询响应报文:{}",response);
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(jsonObject.containsKey("status") && jsonObject.getBoolean("status")){
                //订单支付成功
                logger.info("[STZF]速通支付订单号:{},支付成功!",order.getOrderNo());
                return OrderResponse.success("订单支付成功", response);
            }
            
            //查询失败
            return OrderResponse.error("[STZF]速通支付订单号:{"+order.getOrderNo()+"},支付失败");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[STZF]速通支付查询订单状态异常:{}",e.getMessage());
            return OrderResponse.error("[STZF]速通支付查询订单状态异常!");
        }
    }
    
    /**
     * 
     * @Description 组装支付请求参数
     * @param entity
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,int type) throws Exception{
        logger.info("[STZF]速通支付组装支付请求参数开始====================START===============");
        try {
            //创建支付请求参数存储对象
            Map<String,String> data = new HashMap<>();
            
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            
            data.put("version","V1.0");//版本号，固定值：V1.0
            data.put("mch_id",MerchantId);//商户号
            if(type == 1){
                data.put("trade_type","wg");//支付方式（参考附录1）
            }else{
                data.put("trade_type",entity.getPayCode());//支付方式（参考附录1）
            }
            data.put("out_trade_no",entity.getOrderNo());//订单号（保证商户内统一）长度 20 位
            data.put("amount",amount);//金额，保留2位小数（单位：元）
            data.put("attach","TOP-UP");//商品名称
            data.put("body","TOP-UP");//商品内容
            data.put("mch_create_ip",entity.getIp());//用户端IP
            data.put("notify_url",notifyUrl);//异步通知地址
            data.put("return_url",entity.getRefererUrl());//支付完成，或者取消后跳转地址
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[STZF]速通支付组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[STZF]速通支付组装支付请求参数异常!");
        }
    }

    
    /**
     * 
     * @Description 生成加密签名串
     * @param data
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data) throws Exception{
        logger.info("[STZT]速通支付生成加密签名串开始======================START====================");
        try {
            //签名规则:32位字母大写,MD5加密
            //1 、除sign 字段外， 所有参数按照字段名的ascii 码从小到大排序后使用QueryString 的格式（即key1=value1&key2=value2…）拼接而成，
            //然后将key值拼在最后（参考示例），空值不传递，不参与签名组串。
            //2、签名原始串中，字段名和字段值都采用原始值，不进行URL Encode。
            //3、将待签名字符串进行标准的md5加密后即为sign的值（32位大写）。
            Map<String,String> sortmap = MapUtils.sortByKeys(data);
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = sortmap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = sortmap.get(key);
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign")) continue;
                sb.append("&").append(key).append("=").append(val);
            }
            sb.append(secret);
            
            //生成待签名串
            String signStr = sb.toString().replaceFirst("&", "");
            logger.info("[STZF]速通支付生成待签名串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[STZF]速通支付生成加密签名串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[STZF]速通支付生成加密签名串异常:{}",e.getMessage());
            throw new Exception("生成加密签名串异常!");
        }
    }
}
