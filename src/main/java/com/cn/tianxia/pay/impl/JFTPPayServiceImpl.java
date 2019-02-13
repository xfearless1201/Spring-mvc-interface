package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

/**
 * 
 * @ClassName JFTPPayServiceImpl
 * @Description 俊付通支付
 * @author Hardy
 * @Date 2018年12月29日 下午5:30:04
 * @version 1.0.0
 */
public class JFTPPayServiceImpl implements PayService {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(JFTPPayServiceImpl.class);
    
    private String pid;//商户号
    
    private String payUrl;//支付地址
    
    private String notifyUrl;//回调地址
    
    private String secret;//秘钥
    
    private String productUrl;//产品编码url
    
    //构造器,初始化参数
    public JFTPPayServiceImpl(Map<String,String> data) {
        if(MapUtils.isNotEntity(data)){
            if(data.containsKey("pid")){
                this.pid = data.get("pid");
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
            if(data.containsKey("productUrl")){
                this.productUrl = data.get("productUrl");
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
        logger.info("[JFTP]竣付通支付扫码支付开始==============START===============");
        try {
            //获取请求参数
            Map<String,String> data = sealRequest(payEntity, 0);
            //生成签名串
            String sign = generatorSign(data, 1);
            data.put("p8_sign", sign);
            logger.info("[JFTP]竣付通支付扫码支付请求参数报文:{}",JSONObject.fromObject(data).toString());
            //发起支付请求
            String formStr = HttpUtils.generatorFormGet(data, payUrl);
            return PayResponse.sm_form(payEntity, formStr, "下单成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[JFTP]竣付通支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[JFTP]竣付通支付扫码支付异常");
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[JFTP]竣付通支付回调验签开始===========START==================");
        try {
            //获取原签名串
            String sourceSign = data.get("p10_sign");
            logger.info("[JFTP]竣付通支付回调验签原签名串:{}",sourceSign);
            String sign = generatorSign(data, 0);
            logger.info("[JFTP]竣付通支付回调验签生成加密签名串:{}",sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[JFTP]竣付通支付回调验签异常:{}",e.getMessage());
        }
        return "faild";
    }
    
    /**
     * 
     * @Description 组装支付请求参数
     * @param entity
     * @param type 1 网银 2 扫码
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,int type) throws Exception{
        logger.info("[JFTP]竣付通支付组装支付请求参数开始===================START==============");
        try {
            Map<String,String> data = new HashMap<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            data.put("p1_yingyongnum",pid);//商户在竣付通平台的应用 ID。 必填
            data.put("p2_ordernumber",entity.getOrderNo());//用户订单号。不可重复，
            data.put("p3_money",amount);//订单金额。支持两种格式，精确到元或精确到分，
            data.put("p6_ordertime",sdf.format(new Date()));//商户订单创建时间。格式 yyyymmddhhmmss。如 20170919105912。  
            data.put("p9_signtype","1");//签名方式。可选值 1。1 代表 MD5 方式。
            if(type == 1){
                data.put("p10_bank_card_code",entity.getPayCode());//网银或卡类编码，点卡支付填写卡类编码，网银支付则填写银行编码。银行编码如：CCB，卡类编码：
            }else{
                data.put("p7_productcode",entity.getPayCode());//终端支付方式，固定值“ZFB”。    必填
            }
//            data.put("p11_cardtype","");//卡类型。可选值 1、2。1代表借记卡 2代表贷记卡。    
//            data.put("p12_channel","");//网银类型。可选值 1、2。1代表个人网银 B2C2代表企业网银 B2B 默认个人网银。    
//            data.put("p13_orderfailertime","");//订单失效时间，格式为 yyyymmddhhmmss。
            data.put("p14_customname",entity.getuId());//付款人在商户系统中的帐号。请务必填写真实信息，否则将影响后续查单结果。 
//            data.put("p15_customcontact","");//付款人联系方式。 可空
            data.put("p16_customip",entity.getIp().replace(".", "_"));//付款人 ip 地址，规定以 192_168_0_253 格式，如果以“192.168.0.253”可能会发生签名错误。   
//            data.put("p17_product","");//商品名称。  可空
//            data.put("p18_productcat","");//商品种类。   可空
//            data.put("P19_productnum","");//商品数量，不传递默认 0。   可空
//            data.put("p20_pdesc","");//商品描述。此参数我们会在下行过程中原样返回。您可以在此参数中记录一些数据，方便在下行过程中直接读取。   
//            data.put("p21_version","");//版本号。   可空
//            data.put("p22_sdkversion","");//sdk 版本号。非 SDK 不需传此参数。   为空
//            data.put("p23_charset","");//固定值 UTF-8。目前仅支持 UTF-8 编码   可空
//            data.put("p24_remark","");//备注。此参数我们会在下行过程中原样返回。您可以在此参数中记录一些数据，方便在下行过程中直接读取。 
            if(StringUtils.isBlank(entity.getMobile())){
                data.put("p25_terminal","1");//终端设备类型，可选值 1、2、3 1代表 pc2代 表 ios3代表 android。   
            }else{
                data.put("p25_terminal","3");
            }
//            data.put("paytype","");//展现形式：ZZ    直转类必填
            data.put("p26_ext1","1.1");//商户标识：1.1  必填
//            data.put("p27_ext2","");//额外参数，暂无作用，不传此参数   空
//            data.put("p28_ext3","");//额外参数，暂无作用，不传此参数   空
//            data.put("p29_ext4","");//额外参数，暂无作用，不传此参数   空
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[JFTP]竣付通支付组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[JFTP]竣付通支付组装支付请求参数异常");
        }
    }
    
    /**
     * 
     * @Description 签名
     * @param data
     * @param type 1 支付 2 回调
     * @return
     * @throws Exception
     */
    public String generatorSign(Map<String,String> data,int type) throws Exception{
        logger.info("[JFTP]竣付通支付生成签名开始===================START==================");
        try {
            StringBuffer sb = new StringBuffer();
            if(type == 1){
                //支付
                sb.append(pid).append("&");
                sb.append(data.get("p2_ordernumber")).append("&");
                sb.append(data.get("p3_money")).append("&");
                sb.append(data.get("p6_ordertime")).append("&");
                sb.append(data.get("p7_productcode")).append("&");
            }else{
                //回调
                //p1_yingyongnum+"&"+p2_ordernumber+"&"+p3_money+"&"+p4_zfstate+"&"+p5_orderid+
                //"&"+p6_productcode+"&"+p7_bank_card_code+"&"+p8_charset+"&"+p9_signtype+
                //"&"+p11_pdesc+"&"+p13_zfmoney+"&"+key);
                sb.append(data.get("p1_yingyongnum")).append("&");
                sb.append(data.get("p2_ordernumber")).append("&");
                sb.append(data.get("p3_money")).append("&");
                sb.append(data.get("p4_zfstate")).append("&");
                sb.append(data.get("p5_orderid")).append("&");
                sb.append(data.get("p6_productcode")).append("&");
                sb.append(data.get("p7_bank_card_code")).append("&");
                sb.append(data.get("p8_charset")).append("&");
                sb.append(data.get("p9_signtype")).append("&");
                sb.append(data.get("p11_pdesc")).append("&");
                sb.append(data.get("p13_zfmoney")).append("&");
            }
            sb.append(secret);
            String signStr = sb.toString();
            logger.info("[JFTP]竣付通支付生成待签名串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[JFTP]竣付通支付生成加密签名串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[JFTP]竣付通支付生成签名异常:{}",e.getMessage());
            throw new Exception("[JFTP]竣付通支付生成签名异常");
        }
    }

    /**
     * 
     * @Description 获取产品类型
     * @param entity
     * @return
     * @throws Exception
     */
    private String getProducetCode(PayEntity entity)throws Exception{
        logger.info("[JFTP]竣付通支付获取支付渠道开始==============START============");
        try {
            //组装请求参数
            Map<String,String> data = new HashMap<>();
            data.put("p1_yingyongnum", pid);
            if(StringUtils.isBlank(entity.getMobile())){
                //PC端
                data.put("paymentScenario", "PC");
            }else {
                data.put("paymentScenario", "WAP");
            }
            
            StringBuffer sb = new StringBuffer();
            sb.append(pid).append("&").append(data.get("paymentScenario"));
            sb.append("&").append(secret);
            
            //签名
            String signStr = sb.toString();
            logger.info("[JFTP]竣付通支付获取支付渠道生成待签名串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[JFTP]竣付通支付获取支付渠道生成签名串:{}",sign);
            String response = HttpUtils.toPostForm(data, productUrl+sign);
            if(StringUtils.isNotBlank(response)){
                logger.info("[JFTP]竣付通支付获取支付渠道发起HTTP请求响应结果:{}",response);
                //解析响应结果
                System.err.println(response);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[JFTP]竣付通支付获取支付渠道异常:{}",e.getMessage());
            throw new Exception("[JFTP]竣付通支付获取支付渠道异常");
        }
    }
    
    
    public static void main(String[] args) throws Exception {
        Map<String,String> map = new HashMap<>();
        map.put("secret", "040109160314VxtfpLxf");
        map.put("pid", "01018123779701");
        map.put("productUrl", "http://pdl.jtpay.com/product/getProductType/");
        
        JFTPPayServiceImpl payServiceImpl = new JFTPPayServiceImpl(map);
        PayEntity entity = new PayEntity();
        String result = payServiceImpl.getProducetCode(entity);
        System.err.println(result);
    }
    
    

}
