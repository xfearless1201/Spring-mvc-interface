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
import com.cn.tianxia.pay.bfb.util.PayChannel;
import com.cn.tianxia.pay.qianying.util.HttpUtil;
import com.cn.tianxia.pay.qianying.util.MD5Util;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName AWPayServiceImpl
 * @Description 安稳付支付
 * @author Hardy
 * @Date 2018年9月1日 下午9:52:30
 * @version 1.0.0
 */
public class AWPayServiceImpl implements PayService{
    
    //日志
    private final static Logger logger = LoggerFactory.getLogger(AWPayServiceImpl.class);

    private String payMemberid;//商户号
    
    private String payNotifyurl;//回调地址
    
    private String sercet;//签名key
    
    private String payUrl;//支付地址
    
    //初始化配置文件
    public AWPayServiceImpl(Map<String,String> map) {
        if(map != null && !map.isEmpty()){
            
            if(map.containsKey("payMemberid")){
                this.payMemberid = map.get("payMemberid");
            }
            
            if(map.containsKey("payNotifyurl")){
                this.payNotifyurl = map.get("payNotifyurl");
            }
            
            if(map.containsKey("sercet")){
                this.sercet = map.get("sercet");
            }
            
            if(map.containsKey("payUrl")){
                this.payUrl = map.get("payUrl");
            }
        }
    }

    /**
     * 网银支付
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("{AWPay}安稳网银支付开始======================START============================");
        try {
            //封装支付请求参数
            Map<String,String> map = sealRequest(payEntity,1);
            //签名
            map = generatorSign(map);
            logger.info("{AWPay}安稳付支付请求参数:"+map.toString());
            //发起支付请求
            String username = payEntity.getUsername();
            String mobiel = payEntity.getMobile();
            double amount = payEntity.getAmount();
            String orderNo = payEntity.getOrderNo();
            String pay_url = payEntity.getPayUrl();
            return sealResponse(map, username, mobiel, amount, orderNo, pay_url,1);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("{AWPay}安稳支付网银支付异常:"+e.getMessage());
            return PayUtil.returnWYPayJson("error", "form", "", "", "");
        }
        
    }

    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("{AWPay}安稳付扫码支付开始======================START=========================");
        try {
            //封装支付请求参数
            Map<String,String> data = sealRequest(payEntity,0);
            //签名
            data = generatorSign(data);
            logger.info("{AWPay}安稳付支付参数:"+ data.toString());
            String username = payEntity.getUsername();
            String mobiel = payEntity.getMobile();
//            String mobiel = "mobiel";
            double amount = payEntity.getAmount();
            String orderNo = payEntity.getOrderNo();
            String pay_url = payEntity.getPayUrl();
            //获取支付返回参数
            return sealResponse(data, username, mobiel, amount, orderNo,pay_url,0);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("{AWPay}安稳付扫码支付异常:"+e.getMessage());
            return PayUtil.returnPayJson("error", "", e.getMessage(), "", 0, "", "");
        }
    }
    
    
    /**
     * 
     * @Description (TODO这里用一句话描述这个方法的作用)
     * @param entity
     * @param type 1 网银支付  0 扫码支付
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,Integer type)throws Exception{
        logger.info("{AWPay}安稳付支付封装支付请求参数开始======================START=========================");
        try {
            //组装参数
            Map<String,String> map = new HashMap<>();
            map.put("pay_memberid", payMemberid);//商户编号
            map.put("pay_orderid", entity.getOrderNo());//订单号
            map.put("pay_amount", new DecimalFormat("#.##").format(entity.getAmount()));//订单金额
            if(type == 1){
                //网银支付
                map.put("pay_service", "907");//固定值
                map.put("pay_bankcode", entity.getPayCode());
            }else{
                map.put("pay_service", entity.getPayCode());//固定值
            }
            map.put("pay_notifyurl", payNotifyurl);//回调地址
            map.put("pay_callbackurl", entity.getRefererUrl());//前端回调地址
            map.put("pay_applydate", new SimpleDateFormat("Y-m-d H:m:s").format(new Date()));
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("{AWPay}安稳付支付封装请求参数异常:"+e.getMessage());
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
    private Map<String,String> generatorSign(Map<String,String> data) throws Exception{
        logger.info("{AWPay}安稳付生成签名串开始=====================START=====================");
        try {
            //对map进行排序
            Map<String,String> map = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            //排序data
            map.putAll(data);
            logger.info("{AWPay}安稳付请求参数排序结果:"+map.toString());
            //生成待签名串
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = data.get(key);
                if(StringUtils.isBlank(value) || key.equals("sign")){
                    continue;
                }
                sb.append(key).append("=").append(value).append("&");
            }
            String signStr = sb.toString();
            if(StringUtils.isNotBlank(signStr)){
                signStr += "key="+sercet;
            }
            logger.info("{AWPay}安稳付待签名串:"+signStr);
            //进行签名
            String sign = MD5Util.convert(signStr).toUpperCase();
            logger.info("{AWPay}安稳付生成的签名串:"+sign);
            map.put("pay_md5sign", sign);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("{AWPay}安稳付支付生成签名串异常:"+e.getMessage());
            throw new Exception("生成签名串异常!");
        }
    }
    
    /**
     * 
     * @Description 组装支付返回结果
     * @param data
     * @return
     * @throws Exception
     */
    private JSONObject sealResponse(Map<String,String> data,String username,String mobiel,double amount,String orderNo,String pay_url,Integer type) throws Exception{
        logger.info("{AWPay}安慰付发起支付请求开始========================START=============================");
        try {
            
            String response = HttpUtil.toPostForm(data, payUrl);
            logger.info("{AWPay}安稳付支付返回结果:"+response);
            if(StringUtils.isNotBlank(response)){
                if(type == 1){
                    //网银支付
                    return PayUtil.returnWYPayJson("success", "form", response, pay_url, "");
                }
                //移动端支付
                JSONObject result = JSONObject.fromObject(response);
                String status = result.getString("status");//成功 ‘success’失败 ‘error’
                if(status.equals("success")){
                    //支付成功
                    String link= result.getString("data");//成功返回支付链接重定向到此链接即可
                    if(StringUtils.isBlank(mobiel)){
                        return PayUtil.returnPayJson("success", "2", "支付成功", username, amount, orderNo, link);
                    }
                    return PayUtil.returnPayJson("success", "4", "支付成功", username, amount, orderNo, link);
                }
                //支付失败
                String message = result.getString("msg");//失败时返回错误信息
                return PayUtil.returnPayJson("error", "", "支付失败!", username, amount, orderNo, message);
            }
            return PayUtil.returnPayJson("error", "", "支付请求失败!", username, amount, orderNo, "");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("{AWPay}安慰付支付调第三方接口异常:"+e.getMessage());
            throw new Exception("调用第三方接口异常!");
        }
    }
    
    /**
     * 
     * @Description 验签
     * @param map
     * @return
     */
    @Override
    public String callback(Map<String,String> map) {
        logger.info("{AWPay}安稳付回调验签开始=========================START=======================");
        try {
            if(map == null || map.isEmpty()) return "";
            //获取旧的签名
            String sign = map.get("sign");//获取签名
            map.remove("sign");
            //排序并签名
            Map<String,String> data = generatorSign(map);
            //获取新的签名
            String signStr = data.get("pay_md5sign");
            if(sign.equals(signStr)){
                return "success";
            }
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("{AWPay}安稳回调验签异常:"+e.getMessage());
            return "";
        }
    }
    
    
    public static void main(String[] args) throws Exception {
        Map<String,String> data = new HashMap<>();
        data.put("payMemberid", "10037");//商户编码
        data.put("payNotifyurl", "http://www.baidu.com");
        data.put("payUrl", "https://www.awf8.com/Pay_Index.html");
        data.put("sercet", "mb0y8dof4qz9saogbocr3hgr4n3rg7x9");
//        System.err.println(data.toString());
        Map<String,String> map = new HashMap<>();
        map.put("memberid", "10037");
        map.put("orderid", "AWbl1201809021535241535241186");
        map.put("amount", "10");
        map.put("returncode", "00");
        map.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        map.put("transaction_id", "AW"+System.currentTimeMillis());
        System.err.println(map.get("pay_md5sign"));
    }
}
