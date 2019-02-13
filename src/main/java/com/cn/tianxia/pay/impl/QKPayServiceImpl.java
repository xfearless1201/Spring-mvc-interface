package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;
import com.cn.tianxia.pay.utils.XTUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName QKPayServiceImpl
 * @Description 钱快支付
 * @author Hardy
 * @Date 2018年11月11日 上午10:27:59
 * @version 1.0.0
 */
public class QKPayServiceImpl implements PayService {
    //日志 
    private static final Logger logger = LoggerFactory.getLogger(QKPayServiceImpl.class);
    
    private String merchantNo;//商户号
    
    private String payUrl;//支付请求地址
    
    private String notifyUrl;//回调请求地址
    
    private String secret;//秘钥
    
    //构造器,初始化参数
    public QKPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("merchantNo")){
                this.merchantNo = data.get("merchantNo");
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
        }
    }
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[QK]钱快支付网银支付开始=======================START======================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity);
            //获取签名串
            String sign = generatorSign(data, 1);
            data.put("hmac", sign);
            logger.info("[QK]钱快网银支付请求报文:{}",JSONObject.fromObject(data).toString());
            //发起支付请求
            String response = HttpUtils.toPostForm(data, payUrl);
            
            if(StringUtils.isBlank(response)){
                logger.info("[QK]钱快支付发起HTTP请求无响应结果:{}",response);
                return PayResponse.error("[QK]钱快支付发起HTTP请求无响应结果!");
            }
            
            //解析响应结果 
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(jsonObject.containsKey("status") && jsonObject.getString("status").equals("0")){
                //成功
                String payImg = jsonObject.getString("payImg");//把此连接生成二维码,然后进行扫描即可支付
                return PayResponse.sm_link(payEntity, payImg, "下单成功!");
            }
            
            String msg = jsonObject.getString("Msg");
            return PayResponse.error("下单失败原因:{"+msg+"}");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[QK]钱快支付网银支付异常:{}",e.getMessage());
            return PayResponse.error("[QK]钱快支付网银支付异常");
        }
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[QK]钱快支付扫码支付开始=======================START======================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity);
            //获取签名串
            String sign = generatorSign(data, 1);
            data.put("hmac", sign);
            logger.info("[QK]钱快扫码支付请求报文:{}",JSONObject.fromObject(data).toString());
            //发起支付请求
            String response = HttpUtils.toPostForm(data, payUrl);
            
            if(StringUtils.isBlank(response)){
                logger.info("[QK]钱快支付发起HTTP请求无响应结果:{}",response);
                return PayResponse.error("[QK]钱快支付发起HTTP请求无响应结果!");
            }
            
            //解析响应结果 
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(jsonObject.containsKey("status") && jsonObject.getString("status").equals("0")){
                //成功
                String payImg = jsonObject.getString("payImg");//把此连接生成二维码,然后进行扫描即可支付
                if(StringUtils.isBlank(payEntity.getMobile())){
                    return PayResponse.sm_qrcode(payEntity, payImg, "下单成功!");
                }
                return PayResponse.sm_link(payEntity, payImg, "下单成功!");
            }
            
            String msg = jsonObject.getString("Msg");
            return PayResponse.error("下单失败原因:{"+msg+"}");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[QK]钱快支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[QK]钱快支付扫码支付异常");
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[QK]钱快支付回调验签开始=========================START=====================");
        try {
            //获取回调原串
            String sourceSign = data.get("hmac");
            logger.info("[QK]钱快支付回调原签名串:{}",sourceSign);
            //生成新的签名串
            String sign = generatorSign(data, 2);
            logger.info("[QK]钱快支付回调生产加密签名串:{}",sign);
            
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[QK]钱快支付回调验签异常:{}",e.getMessage());
        }
        return "faild";
    }

    
    private Map<String,String> sealRequest(PayEntity entity) throws Exception{
        logger.info("[QK]钱快支付组装支付请求参数开始=======================START====================");
        try {
            //创建支付请求参数存储对象
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            data.put("p0_Cmd","Buy");//业务类型,固定值“Buy”.  1
            data.put("p1_MerId",merchantNo);//商户编号,商户在系统的唯一身份标识.获取方式请联系客服   2
            data.put("p2_Order",entity.getOrderNo());//商户订单号,提交的订单号必须在自身账户交易中唯一。 3
            data.put("p3_Amt",amount);//支付金额,单位:元，精确到分.此参数为空则无法直连(如直连会报错：抱歉，交易金额太小。)    4
            data.put("p4_Cur","CNY");//交易币种,固定值“CNY”.  5
            data.put("p5_Pid","TOP-UP");//商品名称,用于支付时显示在网关左侧的订单产品信息.此参数如用到中文，请注意转码.    6
            data.put("p6_Pcat","VIRTUAL");//商品种类,商品种类.
            data.put("p7_Pdesc","VIRTUAL-TOP-UP");//商品描述,商品描述.
            data.put("p8_Url",notifyUrl);//商户接收支付成功数据的地址,支付成功后本系统会向该地址发送两次成功通知，该地址可以带参数，
            data.put("pa_MP","TOP-UP");//商户扩展信息,返回时原样返回，此参数如用到中文，请注意转码.   11
            data.put("pd_FrpId",entity.getPayCode());//支付通道编码,该字段可依照附录:支付通道编码列表设置参数值.    12
            data.put("pr_NeedResponse","1");//应答机制,固定值为“1”:需要应答机制;收到服务器点对点支付成功通知，必须回写以“success”（无关大小写）开头的字符串，即使您收到成功通知时发现该订单已经处理过，也要正确回写“success”，否则将认为您的系统没有收到通知，启动重发机制，直到收到“success”为止。
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[QK]钱快支付装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[QK]钱快支付组装支付请求参数异常!");
        }
    }
    
    
    /**
     * 
     * @Description 生成签名串
     * @param data
     * @param type 1 支付  2 回调
     * @return
     * @throws Exception
     */
    private  String generatorSign(Map<String,String> data,Integer type) throws Exception{
        logger.info("[QK]钱快支付生成签名串开始========================START==========================");
        try {
            //签名规则:将参数值经过MD5加密成为结果串字符，然后以POST的方式进行提交，提交数据格式为键值对的方式传输（key1=value1&key2=value2 .....）
            StringBuffer sb = new StringBuffer();
            Map<String,String> sortmap = MapUtils.sortByKeys(data);
            Iterator<String> iterator = sortmap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = sortmap.get(key);
                
                //正则表达式检验是否包含数据
                String regex = ".*\\d+.*";
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("hmac") || !key.matches(regex)) continue;
                sb.append("&").append(key).append("=").append(val);
            }
            
            if(type == 1){
                sb.append("&").append("pa_MP=").append(sortmap.get("pa_MP"));
                sb.append("&").append("pd_FrpId=").append(sortmap.get("pd_FrpId"));
                sb.append("&").append("pr_NeedResponse=").append(sortmap.get("pr_NeedResponse"));
            }
            String signStr = sb.toString().replaceFirst("&", "");
            logger.info("[QK]钱快支付生成待签名串:{}",signStr);
            String sign = XTUtils.hmacSign(signStr, secret);
            logger.info("[QK]钱快支付生成加密签名串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[QK]钱快支付生成签名串异常:{}",e.getMessage());
            throw new Exception("[QK]钱快支付生成签名串异常!");
        }
    }
 
}
