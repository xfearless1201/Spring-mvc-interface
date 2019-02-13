package com.cn.tianxia.pay.impl;

import java.math.BigDecimal;
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

/***
 * 
 * @ClassName NOMQPayServiceImpl
 * @Description 91免签支付
 * @author Hardy
 * @Date 2018年10月5日 上午10:02:29
 * @version 1.0.0
 */
public class NOMQPayServiceImpl implements PayService{
    
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(NOMQPayServiceImpl.class);
    
    private String payUrl ;//= "https://www.uspays.com/Pay_Index.html";//支付地址
    private String memberId ;//= "10005";// 商户号
    private String md5Key ;//= "fp9kzfhchbxidy3bxbvptx8ylpd6xiv6"; // 密钥
    private String notifyUrl ;//= "http://www.baidu.com/";// 异步回调地址
    
    //构造器，初始化数据
    public NOMQPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("memberId")){
                this.memberId = data.get("memberId");
            }
            if(data.containsKey("md5Key")){
                this.md5Key = data.get("md5Key");
            }
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
            }
        }
    }

    /**
     * 网银支付
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[NOMQ]91免签支付=================网银支付开始=====================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 1);
            //生成签名串
            String sign = generatorSign(data);
            data.put("pay_md5sign",sign);
            logger.info("[NOMQ]91免签支付最终请求参数:"+JSONObject.fromObject(data).toString());
            //发起支付请求
            String response = HttpUtils.toPostForm(data, payUrl);
            if(StringUtils.isBlank(response)){
                logger.error("[NOMQ]91免签支付发起HTTP请求无响应结果!");
                return PayUtil.returnWYPayJson("error", "form", "发起HTTP请求无响应结果,请稍后再试!","", "");
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[NOMQ]91免签支付====================扫码支付异常:"+e.getMessage());
            return PayUtil.returnWYPayJson("error", "form","网银支付异常!", "", "");
        }
    }

    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[NOMQ]91免签支付=================扫码支付开始=====================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 0);
            //生成签名串
            String sign = generatorSign(data);
            data.put("pay_md5sign",sign);
            logger.info("[NOMQ]91免签支付最终请求参数:"+JSONObject.fromObject(data).toString());
            //发起支付请求
            String response = HttpUtils.toPostForm(data, payUrl);
            if(StringUtils.isBlank(response)){
                logger.error("[NOMQ]91免签支付发起HTTP请求无响应结果!");
                return PayUtil.returnPayJson("error", "2", "下单失败", "", 0, "", "发起HTTP请求无响应结果,请稍后再试!");
            }
            
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(jsonObject.containsKey("status")){
                String status = jsonObject.getString("status");
                if(status.equalsIgnoreCase("success")){
                    //交易状态   成功  success 失败  error
                    String resultData = jsonObject.getString("data");
                    if(StringUtils.isNotBlank(resultData)){
                        JSONObject jsonData = JSONObject.fromObject(resultData);
                        if(jsonData.containsKey("qrcode")){
                            String qrcode = jsonData.getString("qrcode");//二维码地址,
                            
                            //手机直接跳转，PC扫码
                            if(StringUtils.isBlank(payEntity.getMobile())){
                                //PC端
                                return PayUtil.returnPayJson("success", "2", "展示二维码图片", payEntity.getUsername(), payEntity.getAmount(), payEntity.getOrderNo(), qrcode);
                            }
                            return PayUtil.returnPayJson("success", "4", "跳转移动连接", payEntity.getUsername(), payEntity.getAmount(), payEntity.getOrderNo(), qrcode); 
                        }
                    }
                }
            }
            
            String message = "下单失败原因:";
            if(jsonObject.containsKey("msg")){
                message = message +  jsonObject.getString("msg");
            }
            return PayUtil.returnPayJson("error", "2", message, "", 0, "", response);
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[NOMQ]91免签支付====================扫码支付异常:"+e.getMessage());
            return PayUtil.returnPayJson("error", "2", "下单异常!", "", 0, "", e.getMessage());
        }
    }
    
    /**
     * 回调
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[NOMQ]91免签支付验签开始================START=====================");
        try {
            //获取回调请求原签名
            String sourceSign = data.get("sign");
            logger.info("[NOMQ]91免签支付回调验签原签名串:"+sourceSign);
            //生成回调签名
            String sign = generatorSign(data);
            logger.info("[NOMQ]91免签支付回调验签生成签名串:"+sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[NOMQ]91免签支付验签异常:"+e.getMessage());
        }
        return "";
    }
    
    /**
     * 
     * @Description 组装支付请求参数
     * @param entity
     * @param type 支付方式  1 网银支付   0 扫码支付
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,Integer type) throws Exception{
        logger.info("[NOMQ]91免签支付组装请求参数开始===================START==================");
        try {
            //创建存储请求参数对象
            Map<String,String> data = new HashMap<>();
            String amount = new BigDecimal(entity.getAmount()).setScale(2).toString();
            data.put("pay_memberid", memberId);//平台分配商户号
            data.put("pay_orderid", entity.getOrderNo());//上送订单号唯一, 字符长度20
            data.put("pay_applydate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));//时间格式：2016-12-26 18:18:18
            if(type == 1){
                //网银支付
                data.put("pay_bankcode","907");//银行编码
            }else{
                data.put("pay_bankcode",entity.getPayCode());//银行编码
            }
            data.put("pay_notifyurl", notifyUrl);//服务端通知
            data.put("pay_callbackurl", entity.getRefererUrl());//页面跳转通知
            data.put("pay_amount",amount);//订单金额
            data.put("pay_attach",entity.getuId());//此字段在返回时按原样返回 (中文需要url编码) 不参与签名
            data.put("pay_productname", "TOP-UP");//商品名称 不参与签名
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[NOMQ]91免签支付组装请求参数异常:"+e.getMessage());
            throw new Exception("组装支付请求参数异常!");
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
        logger.info("[NOMQ]91免签支付生成加密签名串开始==================START=================");
        try {
            //签名算法：签名生成的通用步骤如下：
            //第一步，设所有发送或者接收到的数据为集合M，将集合M内非空参数值的参数按照参数名ASCII码从小到大排序（字典序），
            //使用URL键值对的格式（即key1=value1&key2=value2…）拼接成字符串。
            //第二步，在stringA最后拼接上 商户APIKEY得到stringSignTemp字符串，并对stringSignTemp进行MD5运算，
            //再将得到的字符串所有字符转换为大写，得到sign值signValue。
            
            if(data.isEmpty()){
                logger.error("[NOMQ]91免签支付生成签名参数不能为空!");
                throw new Exception("生成签名串参数不能为空!");
            }
            
            Map<String,String> treemap = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            
            treemap.putAll(data);
            
            //拼接待签名串
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = treemap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = treemap.get(key);
                
                //去除不参与签名的参数
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("pay_md5sign") 
                        || key.equalsIgnoreCase("pay_attach") || key.equalsIgnoreCase("pay_productname") 
                        || key.equalsIgnoreCase("attach") || key.equalsIgnoreCase("sign")) 
                    continue;
                sb.append(key).append("=").append(val).append("&");
            }
            sb.append("key=").append(md5Key);
            //待签名串
            String signStr = sb.toString();
            logger.info("[NOMQ]91支付生成待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[NOMQ]91支付生成加密签名串:"+sign);
            return sign;
        } catch (Exception e) {
            logger.error("[NOMQ]91免签支付生成加密签名串异常:"+e.getMessage());
            throw new Exception("生成签名串异常!");
        }
    }
}
