package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayConstant;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName JHZFPayServiceImpl
 * @Description 聚合支付
 * @author Hardy
 * @Date 2018年11月27日 上午10:38:30
 * @version 1.0.0
 */
public class JHZFPayServiceImpl implements PayService {
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(JHZFPayServiceImpl.class);
    
    private String wyMerId;//网银支付商户号
    
    private String smMerId;//扫码支付商户号
    
    private String notifyUrl;
    
    private String smPayUrl;
    
    private String sercet;//秘钥 
    
    private String receivableType;//到账类型
    
    private String wyPayUrl;

    public JHZFPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
           
            if(data.containsKey("wyMerId")){
                this.wyMerId = data.get("wyMerId");
            }
            
            if(data.containsKey("smMerId")){
                this.smMerId = data.get("smMerId");
            }
            
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
            }
            
            if(data.containsKey("smPayUrl")){
                this.smPayUrl = data.get("smPayUrl");
            }
            
            if(data.containsKey("wyPayUrl")){
                this.wyPayUrl = data.get("wyPayUrl");
            }
            
            if(data.containsKey("sercet")){
                this.sercet = data.get("sercet");
            }
            
            if(data.containsKey("receivableType")){
                this.receivableType = data.get("receivableType");
            }
        }
    }

    /**
     * 
     * @Description 网银支付
     * @param payEntity
     * @return
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[JHZF]聚合支付网银支付开始===================START=====================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 1);
            //生成签名串
            String sign = generatorSign(data);
            data.put("signData", sign);
            logger.info("[JHZF]聚合支付网银支付请求报文:{}",JSONObject.fromObject(data).toString());
            //发起HTTP请求,使用form表单格式提交数据
            String response = HttpUtils.generatorForm(data, wyPayUrl);
            logger.info("[JHZF]聚合支付网银支付生成form表单:{}",response);
            return PayResponse.wy_form(payEntity.getPayUrl(), response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[JHZF]聚合支付网银支付异常:{}",e.getMessage());
            return PayResponse.error("聚合支付网银支付异常");
        }
    }

    /**
     * 
     * @Description 扫码支付
     * @param payEntity
     * @return
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[JHZF]聚合支付扫码支付开始===================START=====================");
        try {
            //获取支付请求参数
            String mobile = payEntity.getMobile();
            Map<String,String> data = sealRequest(payEntity,0);
            //生成签名串
            String sign = generatorSign(data);
            data.put("signData", sign);
            logger.info("[JHZF]聚合支付扫码支付请求报文:{}",JSONObject.fromObject(data).toString());
            if(StringUtils.isNoneBlank(mobile)){
                //移动端
                smPayUrl = wyPayUrl;
            }
            String response = HttpUtils.toPostForm(data,smPayUrl);
            if(StringUtils.isBlank(response)){
                logger.info("[JHZF]聚合支付扫码支付发起HTTP请求无响应结果");
                return PayResponse.error("聚合支付扫码支付发起HTTP请求无响应结果");
            }
            logger.info("[JHZF]聚合支付扫码支付发起HTTP请求响应报文:{}",response);
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(jsonObject.containsKey("retCode") && "1".equals(jsonObject.getString("retCode"))){
                //支付请求成功
                if(StringUtils.isNoneBlank(mobile)){
                    //手机端
                    String htmlText = jsonObject.getString("htmlText");
                    logger.info("[JHZF]聚合支付响应结果:{}",htmlText);
                }else{
                    String qrcode = jsonObject.getString("qrcode");
                    logger.info("[JHZF]聚合支付响应结果:{}",qrcode);
                    return PayResponse.sm_qrcode(payEntity, qrcode, "下单成功");
                }
            }
            //下单失败
            String retMsg = jsonObject.getString("retMsg");
            return PayResponse.error("下单失败:"+retMsg);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[JHZF]聚合支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("聚合支付扫码支付异常");
        }
    }

    /**
     * 
     * @Description 回调
     * @param data
     * @return
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[JHZF]聚合支付回调通知开始====================START=================");
        try {
            //获取回调原签名串
            String sourceSign = data.get("signData");
            logger.info("[JHZF]聚合支付回调原签名串:{}",sourceSign);
            String sign = generatorSign(data);
            logger.info("[JHZF]聚合支付回调生产签名串:{}",sign);
            
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
            
        } catch (Exception e) {
            logger.info("[JHZF]聚合支付回调通知异常:{}",e.getMessage());
        }
        return "faild";
    }

    
    /**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @param payType 支付类型  1 网银 0 扫码
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,int payType) throws Exception{
        logger.info("[JHZF]聚合支付封装支付请求参数开始===================START===================");
        try {
            //创建存储对象
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("##").format(entity.getAmount()*100);//订单金额
            data.put("versionId","1.0");//服务版本号,必输,1.0 当前
            data.put("orderAmount",amount);//订单金额,必输,以分为单位
            data.put("orderDate",new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));//订单日期,必输,yyyyMMddHHmmss
            data.put("currency","RMB");//货币类型,必输,RMB：人民币 其他币种代号另行提供
            data.put("accountType","0");//银行卡种类,必输,0-借记卡 1-贷记卡 (部分通道必输，一事一议)
            data.put("transType","008");//交易类别,必输,默认填写 008
            data.put("asynNotifyUrl",notifyUrl);//异步通知 URL,必输  结果返回 URL，V2.1.7 接口用到。支付系统处理完请求后，将处理结果返回给这个 URL      
            data.put("synNotifyUrl",entity.getRefererUrl());//同步返回 URL,必输   针对该交易的交易状态同步通知接收 URL
//            data.put("bankCardNo","");//银行卡号,可输,(部分通道必输，一事一议,快捷支付必输)
            data.put("signType","MD5");//加密方式,必输,MD5
            data.put("prdOrdNo",entity.getOrderNo());//商户订单号,必输  
            if(payType== 1){
                data.put("merId",wyMerId);//商户编号,必输  
                //网银支付
                data.put("payMode","00020");//支付方式,必输,支付方式   00020-银行卡 00023-快捷 00024-支付宝Wap
                data.put("tranChannel",entity.getPayCode());//银行编码,必输,银行编码请参照银行统一编码列表 (支付宝 Wap 固定值103)
            }else{
                data.put("merId",smMerId);//商户编号,必输  
                if(StringUtils.isNoneBlank(entity.getMobile()) && PayConstant.CHANEL_ALI.equals(entity.getPayType())){
                    //手机端,支付宝固定值
                    data.put("payMode","00024");
                    data.put("tranChannel",entity.getPayCode());//银行编码,必输,银行编码请参照银行统一编码列表 (支付宝 Wap 固定值103)
                }else if(PayConstant.CHANEL_KJ.equals(entity.getPayType())){
                    data.put("payMode","00023");//支付方式,必输,支付方式   00020-银行卡 00023-快捷 00024-支付宝Wap
                    data.put("tranChannel",entity.getPayCode());//银行编码,必输,银行编码请参照银行统一编码列表 (支付宝 Wap 固定值103)
                }else{
                    data.put("payMode",entity.getPayCode());//支付方式,必输,支付方式   00020-银行卡 00023-快捷 00024-支付宝Wap
                }
            }
            data.put("receivableType",receivableType);//到账类型,必输,D00,T01,D01: D00 为 D+0,T01 为 T+1,D01 为 D+1  10
            data.put("prdAmt",amount);//商品价格,可输,以分为单位
//            data.put("prdDisUrl","");//商品展示网址,可输
            data.put("prdName","TOP-UP");//商品名称,必输
//            data.put("prdShortName","");//商品简称,可输
            data.put("prdDesc","pay");//商品描述,必输
            data.put("pnum","1");//商品数量,必输       
//            data.put("merParam","");//扩展参数,可输
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[JHZF]聚合支付封装支付请求参数异常:{}",e.getMessage());
            throw new Exception("组装支付请求参数异常");
        }
    }
    
    /**
     * 
     * @Description 生成签名串
     * @param data
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String, String> data) throws Exception{
        logger.info("[JHZF]聚合支付生成签名串开始========================START========================");
        try {
            //参数排序
            //签名规则:将把所有参数按名称 a-z 排序,并且按 key=value 格式用“&”符号拼接起来,遇到 key 为空值的参数不参加签名，在字符串的最后还需拼接上 MD5 加密key，
            //如果字符串中有中文在 MD5 加密时还需用 UTF-8 编码 。 如 asynNotifyUrl=http://localhost:8090/demo/decryptVe rifyResultServlet&
            //merId=0000000051867 3&orderAmount=1&orderStatus=01&payId=8888542891850&payTime=2017011915402
            //&prdOrdNo=86861772861018112&signData=9D75A9609BF8F4D8475444996616B592&signType=MD5
            //&synNotifyUrl=http://m.test.mall.com&transType=008&versionId=1.0&key=osjHY2n9bYDj 500
            //MD5 转码后需要转换大写, 回调的时候 signData 不参与签名
            Map<String,String> sortmap = MapUtils.sortByKeys(data);
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = sortmap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = sortmap.get(key);
                
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("signData")) continue;
                
                sb.append(key).append("=").append(val).append("&");
            }
            sb.append("key=").append(sercet);
            String signStr = sb.toString();
            logger.info("[JHZF]聚合支付生成待签名串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);//大写
            logger.info("[JHZF]聚合支付生成加密签名串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[JHZF]聚合支付生成签名串异常:{}",e.getMessage());
            throw new Exception("聚合支付生成签名串异常");
        }
    }
}
