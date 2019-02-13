package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;
import com.cn.tianxia.pay.utils.RandomUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName DEDPayServiceImpl
 * @Description 得到支付
 * @author Hardy
 * @Date 2018年10月17日 下午7:16:27
 * @version 1.0.0
 */
public class DEDPayServiceImpl implements PayService{
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(DEDPayServiceImpl.class);
    
    private String mchId;//商户号
    
    private String payUrl;//支付Url
    
    private String notifyUrl;//通知地址
    
    private String secret;//秘钥
    
    //构造器,初始化参数
    public DEDPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("mchId")){
                this.mchId = data.get("mchId");
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

    /**
     * 网银支付
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[DED]得到支付网银支付开始======================START======================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 1);
            //生成签名
            String sign = generatorSign(data);
            data.put("sign", sign);
            String response = HttpUtils.generatorForm(data, payUrl);
            logger.info("[DED]网银支付生成表单结果:"+response);
            return PayUtil.returnWYPayJson("success", "form", response, payEntity.getPayUrl(), "pay");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[DED]得到支付网银支付异常:"+e.getMessage());
            return PayUtil.returnWYPayJson("error","form","","","");
        }
    }
    
    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[DED]得到支付扫码支付开始===================START==================");
        try {
            //获取扫码支付请求参数
            Map<String,String> data = sealRequest(payEntity, 0);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign", sign);
            logger.info("得到支付扫码支付参数:{},请求路径:{}",data, payUrl);
            String response = HttpUtils.toPostForm(data, payUrl);
            logger.info("得到支付扫码支付请求返回结果:{}", response);
            if(StringUtils.isBlank(response)){
                logger.error("[DED]得到支付发起HTTP请求无响应结果!");
                return PayUtil.returnPayJson("error", "2", "[DED]得到支付发起HTTP请求无响应结果!", "", 0, "", "请求地址:"+payUrl);
            }
            
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            logger.info("得到支付扫码支付返回值:{}",jsonObject);
            if(jsonObject.containsKey("result_code") && jsonObject.getString("result_code").equals("SUCCESS")){
                //下单成功
                if(StringUtils.isBlank(payEntity.getMobile())){
                	logger.info("得到支付扫码支付路径:{}",jsonObject.getString("qrcode"));
                    String qrcode = jsonObject.getString("qrcode");
                    return PayResponse.sm_qrcode(payEntity, qrcode, "下单成功");
                }
                String pay_url = jsonObject.getString("pay_url");
                logger.info("得到支付手机H5支付:{}",pay_url);
                return PayResponse.sm_link(payEntity, pay_url, "下单成功!");
            }
            
            return PayResponse.error("下单失败:"+response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[DED]得到支付扫码支付异常:"+e.getMessage());
            return PayResponse.error("下单异常"+e.getMessage());
        }
    }

    /**
     * 回调
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[DED]得到支付回调验签开始==================START======================");
        try {
            //获取原签名串
            String sourceSign = data.get("sign");
            logger.info("[DED]得到支付获取原签名串:"+sourceSign);
            //进行rsa验签
            String sign = generatorSign(data);
            logger.info("[DED]得到支付生成验签签名串:"+sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[DED]得到支付回调验签异常:"+e.getMessage());
        }
        return "faild";
    }

    
    /**
     * 
     * @Description 组装支付请求参数
     * @param entity
     * @param type 1 网易 0 扫码
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity  entity,Integer type) throws Exception{
        logger.info("[DED]得到支付组装支付请求参数开始=======================START========================");
        try {
            //创建支付请求参数存储对象
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("##").format(entity.getAmount()*100);//订单总金额，单位为分
            data.put("mch_id",mchId);//商户号
           
            if(type == 1){
                //网易支付
                data.put("trade_type","GATEWAY");//交易类型
                data.put("issuer_id",entity.getPayCode());//银行机构代码,如果trade_type是GATEWAY， 这个项必填，机构代码详情见附录
            }else{
            	logger.info("===============[DED]得到支付扫码支付:{}",entity.getMobile());
                data.put("trade_type",entity.getPayCode());//交易类型
            }
            data.put("nonce",RandomUtils.generateString(16));//随机字符串
//            data.put("user_id","");//用户 ID,商户端的用户 ID
            data.put("timestamp",System.currentTimeMillis()+"");//时间戳
            data.put("subject","TOP-UP");//订单名称
//            data.put("detail","");//商品详情
            data.put("out_trade_no",entity.getOrderNo());//商户订单号,商户系统内部的订单号，32 个字符内
            data.put("total_fee",amount);//总金额,订单总金额，单位为分
            data.put("spbill_create_ip",entity.getIp());//终端 IP
//            data.put("timeout","");//过期时长
            data.put("notify_url",notifyUrl);//异步地址
            //data.put("return_url",entity.getRefererUrl());//返回地址
//            data.put("Platform_trade_no","");//平台流水号,平台流水号，如果trade_type 是QUICK，这个项必填
//            data.put("Sms_code","");//短信验证码,如果trade_type 是QUICK，这个项必填
            data.put("sign_type","MD5");//签名类型,响应签名类型，RSA,MD5，默认为RSA。MD5和下单的签名算法相同。
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[DED]得到支付组装支付请求参数异常:"+e.getMessage());
            throw new Exception("组装支付请求参数异常!");
        }
    }
    
    /**
     * 
     * @Description 生成签名
     * @param data
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data) throws Exception{
        logger.info("[DED]得到支付生成签名串开始=====================START==========================");
        try {
            //第一步，设所有发送或者接收到的数据为集合 M，将集合 M 内非空参数值的参数按照参数名 ASCII 码从小到大排序（字典序），一定要转换为大写
            //使用 URL 键值对的格式（即key1=value1&key2=value2…）拼接成字符串 stringA。特别注意以下重要规则：
            //1.参数名 ASCII 码从小到大排序（字典序）;2.如果参数的值为空不参与签名;3.参数名区分大小写;
            //4.验证调用返回或主动通知签名时，传送的 sign 参数不参与签名，将生成的签名与该sign 值作校验。
            //第二步，在 stringA 最后拼接上 key 得到 stringSignTemp 字符串，并对 stringSignTemp 进行 MD5 运算，
            //再将得到的字符串所有字符转换为大写，得到 sign 值 signValue。
            Map<String,String> treemap = MapUtils.sortByKeys(data);
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = treemap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = treemap.get(key);
                
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign")) continue;
                
                sb.append(key).append("=").append(val).append("&");
            }
            sb.append("key=").append(secret);
            //获取待签名串
            String signStr = sb.toString();
            logger.info("[DED]得到支付生成待签名串:"+signStr);
            //进行MD签名,大写
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[DED]得到支付生成签名串:"+sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[DED]得到支付生成签名串异常:"+e.getMessage());
            throw new Exception("生成支付签名串异常!");
        }
    }
}
