package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.*;

/**
 * @ClassName: FJZFPayServiceImpl
 * @Description: 发家支付
 * @Author: Zed
 * @Date: 2018-12-14 15:41
 * @Version:1.0.0
 **/

public class FJZFPayServiceImpl implements PayService {

    private final static Logger logger = LoggerFactory.getLogger(FJZFPayServiceImpl.class);
    private String api_code = "61383361";//商户号
    private String notifyUrl = "http://txw.tx8899.com/AMH/Notify/FJZFNotify.do";
    private String key = "020db6244da3ae613f2805127b3c089f";
    private String payUrl = "http://g1.fjpay.vip/channel/Common/mail_interface";    //支付地址

    public FJZFPayServiceImpl(Map<String,String> map){
        if(map != null && !map.isEmpty()){
            if(map.containsKey("api_code")){
                this.api_code = map.get("api_code");
            }
            if(map.containsKey("notifyUrl")){
                this.notifyUrl = map.get("notifyUrl");
            }
            if(map.containsKey("key")){
                this.key = map.get("key");
            }
            if(map.containsKey("payUrl")){
                this.payUrl = map.get("payUrl");
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
            Map<String,String> params = sealRequest(payEntity);
            String sign = generatorSign(params);
            params.put("sign",sign);
            logger.info("[FJZF]发家支付扫码支付请求参数:{}",params.toString());

            String response = HttpUtils.post(params,payUrl);

            if (StringUtils.isBlank(response)) {
                logger.info("[FJZF]发家支付扫码支付请求异常:请求结果为空！");
                return PayResponse.error("[FJZF]发家支付扫码支付请求异常:请求结果为空");
            }

            JSONObject jsonObject = JSONObject.fromObject(response);
            JSONObject returnMessage = jsonObject.getJSONObject("messages");

            if (null != returnMessage && returnMessage.containsKey("returncode") && "SUCCESS".equals(returnMessage.getString("returncode"))) {
                logger.info("[JRZF]金睿支付扫码支付成功状态值:{}",returnMessage.getString("returncode"));
                //支付充值成功
                String payurl = jsonObject.getString("payurl");
                if(StringUtils.isBlank(payEntity.getMobile())){
                    //PC端
                    return PayResponse.sm_qrcode(payEntity, payurl, "扫码下单成功");
                }
                return PayResponse.sm_link(payEntity, payurl, "H5下单成功");
            }

            return PayResponse.error("下单失败:"+response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[FJZF]发家支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[FJZF]发家支付扫码支付异常:"+e.getMessage());
        }

    }

    @Override
    public String callback(Map<String, String> data) {

        try {
            String sourceSign = data.remove("sign");
            logger.info("[FJZF]发家支付回调验签原签名串;",data.toString());
            logger.info("[FJZF]发家支付回调验签原签名字段：{}",sourceSign);
            data.remove("messages");
            data.put("api_code",api_code);
            String validSign = generatorSign(data);
            logger.info("[FJZF]发家支付回调验签生成签名：{}",validSign);
            if (sourceSign.equalsIgnoreCase(validSign)) {
                logger.info("[FJZF]发家支付回调验签成功");
                return "success";
            }
            return "fail";
        } catch (Exception e) {
            logger.error("[JFZF]发家支付回调验签异常:" + e.getMessage());
            return "fail";
        }
    }

    /**
     *
     * @Description 封装支付请求参数
     * @param entity
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity) throws Exception{
        logger.info("[FJZF]发家支付封装请求参数开始=====================START==================");
        try {
            //创建支付请求参数存储对象
            Map<String,String> data = new HashMap<>();
            //订单金额
            String amount = new DecimalFormat("##.##").format(entity.getAmount());//交易金额
            data.put("return_type","json");//	返回数据类型	是	字符串  必填参数json， html（详情请看，返回说明）
            data.put("api_code",api_code);//	商户号	是	字符串
            data.put("is_type",entity.getPayCode());//	支付类型 支付渠道：alipay支付宝，wechat微信，alipay_wap支付宝h5..........等等
            data.put("price",amount);//	订单定价	是	float，保留2位小数
            data.put("order_id",entity.getOrderNo());//	您的自定义单号	是	字符串，最长50位
            data.put("time",String.valueOf(System.currentTimeMillis()));//	发起时间	是	时间戳，最长10位
            data.put("mark","top_up");//	描述	是	字符串，最长100位
            data.put("return_url",entity.getRefererUrl());//	成功后网页跳转地址	是	字符串，最长255位
            data.put("notify_url",notifyUrl);//	通知状态异步回调接收地址	是	字符串，最长255位

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[FJZF]发家支付封装请求参数异常:"+e.getMessage());
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
        logger.info("[FJZF]发家支付生成支付签名串开始==================START========================");
        try {
            //签名规则:
           // 参数按照ASCII 码的顺序进行排序，按照key=value进行组合，多个参数“&”号相连接，如abc=123&bbb=aaaa&……这种形式，注意最后连接的时候，将排序好的参数最后加上商户秘钥 &key=”api_key   将排序后的参数与其对应值，组合成“参数=参数值”的格式，并且把这些参数用&字符连接起来，此时生成的字符串为待签名字符串。将待签名字符串和商户私钥带入 SHA1 算法中得出 sign。

            Map<String,String> treemap = new TreeMap<>();
            treemap.putAll(data);

            //生成待签名串
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = treemap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = treemap.get(key);
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign"))
                    continue;
                sb.append(key).append("=").append(val).append("&");

            }

            //生成待签名串
            String signStr = sb.toString()+"key="+key;
            logger.info("[FJZF]发家支付生成待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[FJZF]发家支付生成加密签名串:"+sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[FJZF]发家支付生成支付签名串异常:"+e.getMessage());
            throw new Exception("生成支付签名串异常!");
        }
    }

    public static void main(String[] args) {
        PayEntity entity = new PayEntity();
        entity.setPayCode("alipay_wap");
        entity.setAmount(100);
        entity.setOrderNo("fjzf00000066");
        entity.setRefererUrl("http://www.baidu.com");
        FJZFPayServiceImpl fjzfPayService = new FJZFPayServiceImpl(null);
        fjzfPayService.smPay(entity);

    }
    
}
