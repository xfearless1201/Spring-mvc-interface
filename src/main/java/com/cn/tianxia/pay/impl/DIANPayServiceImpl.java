package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.RandomUtils;
import com.cn.tianxia.pay.yj.util.XmlUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName: DIANPayServiceImpl
 * @Description: 点点支付
 * @Author: Zed
 * @Date: 2018-12-27 15:59
 * @Version:1.0.0
 **/

public class DIANPayServiceImpl implements PayService {
    private final static Logger logger = LoggerFactory.getLogger(DIANPayServiceImpl.class);

    /** 支付地址 **/
    private String api_url = "http://openapi.51zzhy.com/gateway/soa";

    /** 商户号 **/
    private String merchantId = "4055";

    /** md5key **/
    private String md5Key = "d05dcff704886db697a92be3a20ecce4";

    /** notifyUrl **/
    private String notifyUrl = "http://txw8899.com/TXY/Notify/DIANNotify.do";

    public DIANPayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("api_url")) {
                this.api_url = pmap.get("api_url");
            }
            if (pmap.containsKey("merchantId")) {
                this.merchantId = pmap.get("merchantId");
            }
            if (pmap.containsKey("md5Key")) {
                this.md5Key = pmap.get("md5Key");
            }
            if (pmap.containsKey("notifyUrl")) {
                this.notifyUrl = pmap.get("notifyUrl");
            }
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        return null;
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        try {
            Map<String, String> paramsMap = sealRequest(payEntity);
            String sign = generatorSign(paramsMap);
            paramsMap.put("sign", sign);
            logger.info("[DIAN]点点支付请求参数:" + JSONObject.fromObject(paramsMap).toString());
            String res = HttpUtils.toPostForm(paramsMap,api_url);

            if (StringUtils.isBlank(res)) {
                logger.error("[DIAN]点点支付扫码支付请求异常,返回结果为空!");
                return PayResponse.error("[DIAN]点点支付扫码支付请求异常,返回结果为空!");
            }
            logger.info("[DIAN]点点支付响应参数字符:" + res);
            Map<String,String> map = XmlUtils.xmlStr2Map(res);

            if (map.containsKey("msg") && "0".equals(map.get("msg"))) {
                String qrCodeUrl = map.get("url");
                if (StringUtils.isNotBlank(payEntity.getMobile())) {
                    return PayResponse.sm_link(payEntity,qrCodeUrl,"下单成功");
                }
                return PayResponse.sm_qrcode(payEntity,qrCodeUrl,"下单成功");
            }
            return PayResponse.error("[DIAN]点点支付扫码支付下单失败:"+ map.get("msg"));

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[DIAN]点点支付扫码支付下单失败:{}",e.getMessage());
            return PayResponse.error("[DIAN]点点支付扫码支付下单失败:"+e.getMessage());
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        String sourceSign = data.remove("sign");
        if (StringUtils.isBlank(sourceSign)) {
            logger.info("[DIAN]点点支付回调验签失败：回调签名为空！");
            return "fail";
        }
        String localSign;
        try {
            localSign = generatorSign(data);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[DIAN]点点支付签名异常："+ e.getMessage());
            return "fail";
        }

        logger.info("本地签名:" + localSign + "      服务器签名:" + sourceSign);
        if (sourceSign.equalsIgnoreCase(localSign)) {
            return "success";
        }
        return "fail";
    }

    /**
     *
     * @param payEntity
     * @return
     */
    private Map<String,String> sealRequest(PayEntity payEntity) throws Exception {
        logger.info("[DIAN]点点支付封装请求参数开始=====================START==================");
        try {
            //创建支付请求参数存储对象
            Map<String,String> data = new HashMap<>();
            //订单金额
            String amount = new DecimalFormat("0.00").format(payEntity.getAmount());//交易金额 分为单位

            data.put("version","1.0");//	版本号	是	String(8)	版本号，固定值是1.0
            data.put("charset","UTF-8");//	字符集	是	String(8)	字符集，固定值是UTF-8
            data.put(" merchant_id",merchantId);//	商户号	是	String(32)	商户号，由点点分配
            data.put("out_trade_no",payEntity.getOrderNo());//	商户订单号	是	String(32)	商户系统内部的订单号 ,32个字符内、 可包含字母,确保在商户系统唯一 " +
            data.put("trade_type",payEntity.getPayCode());//	支付类型	是	String(32)	支付类型, ，详细参照7
            data.put("user_ip",payEntity.getIp());//	IP地址	是	String(32)	提交的ip地址
            data.put("subject","top_up");//	商品名称	是	String(128)	商品的标题/交易标题/订单标题/订单关键字等。
            data.put("body","");//	商品描述	是	String(128)	对一笔交易的具体描述信息。如果是多种商品，请将商品描述字符串累加传给body。
            data.put("user_id",payEntity.getuId());//	账号	是	String(32)	充值账号
            data.put("total_fee",amount);//	总金额	是	String	该笔订单的资金总额，单位为RMB-Yuan。取值范围为[0.01，100000000.00]，可精确到小数点后两位。
            data.put("notify_url",notifyUrl);//	通知地址	是	String(255)	接收点点通知的URL，需给绝对路径，255字符内格式如:http://wap.tenpay.com/tenpay.asp
            data.put("return_url",payEntity.getRefererUrl());//	返回地址	否	String(255)	接收点点返回的URL，需给绝对路径，255字符内格式如:http://wap.tenpay.com/tenpay.asp默认返回到我方链接
            data.put("nonce_str",RandomUtils.generateString(10));//	随机字符串	是	String(32)	随机字符串，不长于 32 位
                  //  业务参数
           // data.put("biz_content","");//	业务参数	是	String(512)	格式JSON,处理时，先URLEncode,然后Base64加密


            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[DIAN]点点支付封装请求参数异常:",e.getMessage());
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
        logger.info("[DIAN]点点支付生成支付签名串开始==================START========================");
        try {

            StringBuilder sb = new StringBuilder();
            SortedMap<String,String> sortedMap = new TreeMap<>(data);
            for (String key:sortedMap.keySet()) {
                if (StringUtils.isBlank(sortedMap.get(key)) || "sign".equalsIgnoreCase(key) || "pay_md5sign".equalsIgnoreCase(key)) {
                    continue;
                }
                sb.append(key).append("=").append(sortedMap.get(key)).append("&");
            }
            sb.append("key=").append(md5Key);
            logger.info("[DIAN]点点支付待签名字符:" + sb.toString());
            String sign = MD5Utils.md5toUpCase_32Bit(sb.toString());
            if (StringUtils.isBlank(sign)) {
                logger.error("[DIAN]点点支付生成签名串为空！");
                return null;
            }
            logger.info("[DIAN]点点支付生成加密签名串:"+ sign.toLowerCase());
            return sign.toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[DIAN]点点支付生成支付签名串异常:"+ e.getMessage());
            throw new Exception("生成支付签名串异常!");
        }
    }

    public static void main(String[] args) {
        PayEntity entity = new PayEntity();
        entity.setPayCode("010002");
        entity.setAmount(100);
        entity.setOrderNo("yfbbl1123456");
        entity.setIp("127.0.0.1");
        entity.setRefererUrl("http://localhost:85/JJF/Noitfy.do");
        entity.setuId("123456");
        DIANPayServiceImpl service = new DIANPayServiceImpl(null);
        service.smPay(entity);
    }
}
