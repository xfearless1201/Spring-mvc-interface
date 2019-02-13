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

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName WKPayServiceImpl
 * @Description 悟空支付
 * @author Hardy
 * @Date 2018年10月2日 上午9:57:54
 * @version 1.0.0
 */
public class WKPayServiceImpl implements PayService{
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(WKPayServiceImpl.class);
    
    private String platSource;//平台编号
    
    private String notifyUrl;//回调地址
    
    private String pcPayUrl;//PC端支付地址
    
    private String mbPayUrl;//移动端支付地址
    
    private String secret;//秘钥
    
    //构造器,初始化基本参数信息
    public WKPayServiceImpl(Map<String,String> data,String type) {
        if(data != null && !data.isEmpty()){
            JSONObject jsonObject = JSONObject.fromObject(data.get(type));
            if(jsonObject != null && !jsonObject.isEmpty()){
                if(jsonObject.containsKey("platSource")){
                    this.platSource = jsonObject.getString("platSource");
                }
                if(jsonObject.containsKey("notifyUrl")){
                    this.notifyUrl = jsonObject.getString("notifyUrl");
                }
                if(jsonObject.containsKey("pcPayUrl")){
                    this.pcPayUrl = jsonObject.getString("pcPayUrl");
                }
                if(jsonObject.containsKey("mbPayUrl")){
                    this.mbPayUrl = jsonObject.getString("mbPayUrl");
                }
                if(jsonObject.containsKey("secret")){
                    this.secret = jsonObject.getString("secret");
                }
            }
        }
    }

    /**
     * 网银支付
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[WK]悟空支付网银支付开始====================START========================");
        try {
            String mobile = payEntity.getMobile();
            String payUrl = mbPayUrl;//支付地址
            //组装支付请求参数
            Map<String,String> data = sealRequest(payEntity,1);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign", sign);
            logger.info("[WK]悟空支付生成最终发起请求参数:"+JSONObject.fromObject(data).toString());
            if(StringUtils.isBlank(mobile)){
                //pc端
                payUrl = pcPayUrl;
            }
            //根据不同支付类型发起不同的支付方式
            String response = HttpUtils.generatorForm(data, payUrl);
            return PayUtil.returnWYPayJson("success", "form", response, payEntity.getPayUrl(), "");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[WK]悟空支付网银支付异常:"+e.getMessage());
            return PayUtil.returnWYPayJson("error", "form", "", "", "");
        }
    }

    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[WK]悟空支付扫码支付开始====================START========================");
        try {
            String mobile = payEntity.getMobile();
            String username = payEntity.getUsername();
            double amount = payEntity.getAmount();
            String orderNo = payEntity.getOrderNo();
            String payCode = payEntity.getPayCode();//从配置文件中获取的第三方需求的支付方式
            String payUrl = mbPayUrl;//支付地址
            logger.info("悟空支付扫码支付路径 :{}",payUrl);
            //组装支付请求参数
            Map<String,String> data = sealRequest(payEntity,0);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign", sign);
           
            logger.info("[WK]悟空支付生成最终发起请求参数:{},是否手机端:{}",data,mobile);
            if(StringUtils.isBlank(mobile)){
                //pc端
                payUrl = pcPayUrl;
            }
            logger.info("悟空支付请求路径,mbPayUrl={},payUrl={}",payUrl);
            logger.info("悟空支付扫码请求路径:pcPayUrl = {}",pcPayUrl);
            //根据不同支付类型发起不同的支付方式
            
            logger.info("悟空支付支付类型:{}",payCode);
            String response = null;
            
            /*if(payCode.equals("quick") || payCode.equals("alipay") || payCode.equals("wechat")){
            	logger.info("悟空支付开始生产FORM表单结果.........");
                //快捷支付,生成一个form表单进行提交
                response = HttpUtils.generatorForm(data, payUrl);
                logger.info("[WK]悟空支付生成FORM表单结果:"+response);
                return PayUtil.returnPayJson("success", "1", "生成一个form表单", username, amount, orderNo, response);
            }*/
            response = HttpUtils.toPostForm(data, payUrl);
            if(StringUtils.isBlank(response)){
                logger.info("[WK]悟空支付发起下单请求响应结果为空");
                return PayUtil.returnPayJson("error", "2", "发起HTTP请求无响应结果!", username, amount, orderNo, response);
            }
            logger.info("[WK]悟空支付发起HTTP请求响应结果:"+response);
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(StringUtils.isBlank(mobile)){
                //下单成功    
                if(jsonObject.containsKey("success") && jsonObject.getBoolean("success")){
                    JSONObject info = JSONObject.fromObject(jsonObject.getString("info"));
                    String qrCode = info.getString("qrCode");
                    return PayUtil.returnPayJson("success", "2", "生成二维码扫码图片", username, amount, orderNo, qrCode);
                }
            }else {
                if(jsonObject.containsKey("code") && jsonObject.getString("code").equals("1")){
                    String codeUrl = jsonObject.getString("codeUrl");//跳转链接，使用与H5
                    return PayUtil.returnPayJson("success", "4", "跳转链接", username, amount, orderNo, codeUrl);
                }
            }
            //请求失败
            String message="下单支付,失败原因:";
            if(jsonObject.containsKey("message")){
                message = message+jsonObject.getString("message");
            }
            return PayUtil.returnPayJson("error", "2", "下单失败", username, amount, orderNo, message);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[WK]悟空支付扫码支付异常:"+e.getMessage());
            return PayUtil.returnPayJson("error", "2", "下单失败", "", 0, "", "发起HTTP请求异常");
        }
    }

    /**
     * 支付回调
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[WK]悟空支付回调验签开始==================START=================");
        try {
            //获取回调参数原签名串
            String sourceSign = data.get("sign");
            logger.info("[WK]悟空支付回调原签名串:"+sourceSign);
            String sign = generatorSign(data);
            logger.info("[WK]悟空支付回调签名串:"+sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[WK]悟空支付回调验签异常:"+e.getMessage());
        }
        return "";
    }

    
    /**
     * 
     * @Description 组装支付请求 参数
     * @param payEntity
     * @param type 1 网银支付  0 扫码支付
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity payEntity,Integer type) throws Exception{
        logger.info("[WK]悟空支付组装支付请求参数开始===================START===================");
        try {
            //创建存储支付请求参数对象
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(payEntity.getAmount());
            data.put("platSource", platSource);//商户编号/平台来源
            data.put("payAmt", amount);//交易金额（单位：元）
            data.put("orderNo", payEntity.getOrderNo());//订单编号
            data.put("notifyUrl",notifyUrl);//异步通知回调URL地址
            if(type == 1){ //网银支付
                //支付类型(当前仅支持alipay/wechat/unionpay三种类型值)  其中银联扫码类型unionpay支持京东，美团，美团外卖，云闪付等多种常用扫码支付软件
                data.put("payType", "gateway");
                data.put("payChannelType", "1");//支付通道类型：1为储蓄卡（借记卡）
                data.put("payChannelCode", payEntity.getPayCode());//是   支付通道类型：1为储蓄卡（借记卡）
                if(StringUtils.isBlank(payEntity.getMobile())){
                    data.put("orderSource", "1");//是   订单来源：1-PC端，2-手机端
                }else{
                    data.put("orderSource", "2");//是   订单来源：1-PC端，2-手机端
                }
            }else{
                data.put("payType",payEntity.getPayCode());
                if(!payEntity.getPayCode().equals("quick")){//非快捷支付
                    data.put("ip", payEntity.getIp());//终端IP地址 payEntity.getIp()
                    if(StringUtils.isNotBlank(payEntity.getMobile()) && payEntity.getPayCode().equalsIgnoreCase("wechatH5")){
                        data.put("returnUrl", payEntity.getRefererUrl());//同步回调地址
                    }
                }
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[WK]悟空支付组装支付请求参数异常:"+e.getMessage());
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
        logger.info("[WK]悟空支付生成签名开始================START====================");
        try {
            //生成签名规则:第一步，设所有发送或者接收到的数据为集合 M，将集合 M 内非空参数值的参数按照参数名 ASCII 码从小到大排序（字典序），
            //使用 URL键的值的格式,值与值之间以“|”分隔（即 value1|value2…）拼接成字符串 stringA。
            //特别注意以下重要规则：1.参数名 ASCII 码从小到大排序（字典序）；2.如果参数的值为空不参与签名；也就是说参数是空的不需要参与签名；
            //3.参数名区分大小写；4.生成的签名需转成大写；
            Map<String,String> treemap = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            treemap.putAll(data);
            
            //拼接字符创
            StringBuffer sb = new StringBuffer();
            //遍历集合M
            Iterator<String> iterator = treemap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = treemap.get(key);
                if(StringUtils.isBlank(val) || key.equals("sign")) continue;
                sb.append(val).append("|");
            }
            sb.append(secret);
            //生成待签名串
            String signStr = sb.toString();
            logger.info("[WK]悟空支付生成待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);//MD5加密签名（大写）
            logger.info("[WK]悟空支付生成加密签名串:"+sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[WK]悟空支付生成签名异常:"+e.getMessage());
            throw new Exception("生成签名异常!");
        }
    }
    
    public static void main(String[] args) {
        JSONObject bank = new JSONObject();
        bank.put("platSource", "hg222m1397969206");
        bank.put("secret","e3d62293535c45f8801e24688a72fc43");
        bank.put("notifyUrl", "http://www.baidu.com");
        bank.put("pcPayUrl", "http://sun.wukong6.com:9000/pay/gateway");
        bank.put("mbPayUrl", "http://sun.wukong6.com:9000/pay/gateway");
        JSONObject ali = new JSONObject();
        ali.put("platSource", "hg222m1397969206");
        ali.put("secret","e3d62293535c45f8801e24688a72fc43");
        ali.put("notifyUrl", "http://www.baidu.com");
        ali.put("pcPayUrl", "http://sun.wukong6.com:9000/scan/getQrCode");
        ali.put("mbPayUrl", "http://sun.wukong6.com:9000/scan/getH5");
        JSONObject kj = new JSONObject();
        kj.put("platSource", "hg222m1397969206");
        kj.put("secret","e3d62293535c45f8801e24688a72fc43");
        kj.put("notifyUrl", "http://www.baidu.com");
        kj.put("pcPayUrl", "http://sun.wukong6.com:9000/pay/quick");
        kj.put("mbPayUrl", "http://sun.wukong6.com:9000/pay/quick");
        JSONObject wx = new JSONObject();
        wx.put("platSource", "hg222m1397969206");
        wx.put("secret","e3d62293535c45f8801e24688a72fc43");
        wx.put("notifyUrl", "http://www.baidu.com");
        wx.put("pcPayUrl", "http://sun.wukong6.com:9000/scan/getQrCode");
        wx.put("mbPayUrl", "http://sun.wukong6.com:9000/scan/getH5");
        JSONObject yl = new JSONObject();
        yl.put("platSource", "hg222m1397969206");
        yl.put("secret","e3d62293535c45f8801e24688a72fc43");
        yl.put("notifyUrl", "http://www.baidu.com");
        yl.put("pcPayUrl", "http://sun.wukong6.com:9000/scan/getQrCode");
        yl.put("mbPayUrl", "http://sun.wukong6.com:9000/scan/getH5");
        JSONObject jd = new JSONObject();
        jd.put("platSource", "hg222m1397969206");
        jd.put("secret","e3d62293535c45f8801e24688a72fc43");
        jd.put("notifyUrl", "http://www.baidu.com");
        jd.put("pcPayUrl", "http://sun.wukong6.com:9000/scan/getQrCode");
        jd.put("mbPayUrl", "http://sun.wukong6.com:9000/scan/getH5");
        JSONObject cft = new JSONObject();
        cft.put("platSource", "hg222m1397969206");
        cft.put("secret","e3d62293535c45f8801e24688a72fc43");
        cft.put("notifyUrl", "http://www.baidu.com");
        cft.put("pcPayUrl", "http://sun.wukong6.com:9000/scan/getQrCode");
        cft.put("mbPayUrl", "http://sun.wukong6.com:9000/scan/getH5");
        JSONObject data = new JSONObject();
        data.put("bank", bank);
        data.put("ali", ali);
        data.put("wx", wx);
        data.put("kj", kj);
        data.put("yl", yl);
        data.put("jd", jd);
        data.put("cft", cft);
        System.err.println(data.toString());
    }
}
