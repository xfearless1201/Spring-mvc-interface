package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.Comparator;
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
import com.cn.tianxia.pay.utils.RandomUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName SZPayServiceImpl
 * @Description 山竹支付
 * @author Hardy
 * @Date 2018年10月8日 下午8:57:47
 * @version 1.0.0
 */
public class SZPayServiceImpl implements PayService{
    
    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(SZPayServiceImpl.class);
    
    private String payUrl;//支付请求地址
    private String notifyUrl;//回调请求地址
    private String secret;//签名秘钥
    private String mchId;//商户号
    
    public SZPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
            }
            if(data.containsKey("secret")){
                this.secret = data.get("secret");
            }
            if(data.containsKey("mchId")){
                this.mchId = data.get("mchId");
            }
        }
    }

    /**
     * 网银支付
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[SZ]山竹支付网银支付开始===================START=======================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 1);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign",sign);//签名，详见 签名生成算法
            logger.info("[SZ]山竹支付请求报文:"+JSONObject.fromObject(data).toString());
            String formStr = HttpUtils.generatorForm(data, payUrl);
            return PayUtil.returnWYPayJson("success", "form", formStr, payEntity.getPayUrl(), "pay");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[SZ]山竹支付网银支付异常:"+e.getMessage());
            return PayUtil.returnWYPayJson("error", "form", e.getMessage(), "", "");
        }
    }

    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[SZ]山竹支付扫码支付开始===================START=======================");
        try {
            
            String username = payEntity.getUsername();
            String order_no = payEntity.getOrderNo();
            double amount = payEntity.getAmount();
            String mobile = payEntity.getMobile();
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 0);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign",sign);//签名，详见 签名生成算法
            logger.info("[SZ]山竹支付请求报文:"+JSONObject.fromObject(data).toString());
            String response = HttpUtils.toPostForm(data, payUrl);
            if(StringUtils.isBlank(response)){
                logger.error("[SZ]山竹支付发起HTTP请求无响应结果!");
                return PayUtil.returnPayJson("error", "2", "发起HTTP请求无响应结果!",username,amount,order_no,"请求响应结果,[response:"+response+"]");
            }
         
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(jsonObject.containsKey("return_code")){
                //SUCCESS/FAIL 此字段是通信标识，非交易标识，交易是否成功需要查看 result_code 来判断
                String return_code = jsonObject.getString("return_code");
                if(return_code.equalsIgnoreCase("SUCCESS")){
                    
                    if(payEntity.getPayCode().equalsIgnoreCase("trade.unionpay") || StringUtils.isNotBlank(mobile)){
                        //快捷支付
                        String prepay_url = jsonObject.getString("prepay_url");//交易类型为h5，公众号返回
                        return PayUtil.returnPayJson("success", "4", "下单成功", username, amount, order_no, prepay_url);
                    }
                    //PC端 显示二维码
                    String code_img_url = jsonObject.getString("code_img_url");//交易类型为扫码是返回
                    return PayUtil.returnPayJson("success", "2", "下单成功", username, amount, order_no, code_img_url);
                }
            }
            return PayUtil.returnPayJson("error", "2", "下单失败:"+jsonObject.getString("return_msg"), username, amount, order_no, response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[SZ]山竹支付扫码支付异常:"+e.getMessage());
            return PayUtil.returnPayJson("error","2","下单失败!","",0,"",e.getMessage());
        }
    }

    /**
     * 回调
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[SZ]山竹支付回调验签开始=================START====================");
        try {
            //获取原回调签名
            String sourceSign = data.get("sign");
            logger.info("[SZ]山竹支付回调原签名串:"+sourceSign);
            //生成签名串
            String sign = generatorSign(data);
            logger.info("[SZ]山竹支付回调验签生成签名串:"+sign);
            if(sign.equalsIgnoreCase(sourceSign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[SZ]山竹支付回调验签异常:"+e.getMessage());
        }
        return "faild";
    }

    /**
     * 
     * @Description 组装支付请求参数
     * @param entity
     * @param type 1 网银 0 扫码
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,Integer type) throws Exception{
        logger.info("[SZ]山竹支付组装支付请求参数开始==================START====================");
        try {
            Map<String,String> data = new HashMap<>();
            //格式化金额
            String amount = new DecimalFormat("##").format(entity.getAmount()*100);
            data.put("mch_id",mchId);//商户号，唯一标识        
            data.put("body","TOP-UP");//商品或支付单简要描述
            data.put("detail","TOP-UP");//商品详情
            data.put("attach","SZPAY");//附加数据
            data.put("out_trade_no",entity.getOrderNo());//商户系统内部的订单号,32 个字符内、可包含字母
            data.put("amount",amount);//总金额
            data.put("fee_type","CNY");//货币类型
            data.put("spbill_create_ip",entity.getIp());//终端IP
//            data.put("goods_tag","");//商品标记
            data.put("notify_url",notifyUrl);//通知地址
            data.put("return_url",entity.getRefererUrl());//页面回调地址
//            data.put("limit_pay","");//no_credit：指定不能使用信用卡支付
            if(type == 1){
                data.put("payment_type","trade.gateway");//支付类型
                data.put("bank_type",entity.getPayCode());//当payment_type为trade.gateway 时必填 详细说明见附件-银行编码
            }else{
                data.put("payment_type",entity.getPayCode());//支付类型
            }
            data.put("nonce_str",RandomUtils.generateString(16));//随机字符串，不长于32位
            data.put("sign_type","MD5");//签名类型，目前支持HMAC-SHA256和MD5，默认为MD5
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[SZ]山竹支付组装支付请求参数异常:"+e.getMessage());
            throw new Exception("组装支付请求参数异常!");
        }
    }
    
    /**
     * 
     * @Description 生成签名串
     * @param data
     * @return
     */
    private String generatorSign(Map<String,String> data) throws Exception{
        logger.info("[SZ]山竹支付生成签名串开始=================START===================");
        try {
            //特别注意以下重要规则： 
            //1. 参数名 ASCII 码从小到大排序（字典序）； 
            //2. 如果参数的值为空不参与签名；也就是说参数是空的不需要参与签名； 
            //3. 参数名区分大小写； 
            //4. 验证调用返回或主动通知签名时，传送的 sign 和 sign_type 参数不参与签名，将生成的签名与该 sign 值作校验。 
            //5. 支付接口可能增加字段，验证签名时必须支持增加的扩展字段
            Map<String,String> treemap = new TreeMap<>(new Comparator<String>() {
                //参数名 ASCII 码从小到大排序（字典序）；    
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            }); 
            
            treemap.putAll(data);
            
            //签名步骤:
            //第一步，设所有发送或者接收到的数据为集合 M，将集合 M 内非空参数值的参数按照参数名 ASCII 码从 小到大排序（字典序），
            //使用URL 键值对的格式（即 key1=value1&key2=value2…）拼接成字符串stringA。
            //第二步，在stringA最后拼接上&key=支付密钥得到stringSignTemp字符串，并对stringSignTemp进行MD5运算，
            //再将得到的字符串所有字符转换为小写，得到sign值signValue。
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = treemap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = treemap.get(key);
                
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign") 
                        || key.equalsIgnoreCase("sign_type")) continue;
                sb.append(key).append("=").append(val).append("&");
            }
            sb.append("key=").append(secret);
            String signStr = sb.toString();
            logger.info("[SZ]山竹支付生成待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[SZ]山竹支付生成加密签名串:"+sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[SZ]山竹支付生成签名串异常:"+e.getMessage());
            throw new Exception("生成签名串异常!");
        }
    }
}
