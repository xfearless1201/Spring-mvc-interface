package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.qyf.util.ToolKit;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.RandomUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * @ClassName: DFIFPayServiceImpl
 * @Description: D15支付（支付宝）
 * @Author: Zed
 * @Date: 2019-01-11 10:57
 * @Version:1.0.0
 **/

public class DFIFPayServiceImpl implements PayService {
    private final static Logger logger = LoggerFactory.getLogger(DFIFPayServiceImpl.class);

    private String account = "shop800082";
    private String key = "BED6A0FCD601D19C522F89AE4CDD52E4";
    private String payUrl = "http://firepay.yincheng12.com/trade/pay.do";
    private String notifyUrl = "http://txw.tx8899.com/TYC/Notify/YFZFNotify.do";
    private String PAY_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCRm4gdbrrw2CAnZQYjuh1KX51CexXo+Qk19MDtacKvR06me4liEgPMnLe4QO8IA0j7KWca1o9QgQWCuzZ8fD/sznbXqbsma7DZE+6fGs4p2tazh2LVK0GCzu1GI2VOTfPeh5Z0fymO0HEcoUj0VBsv2LhT3HxzDiobb2j2Dy4N7QIDAQAB";
    private String PAY_PRIVATE_KEY = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIpBe2MuAskAGmU2YHeBYQtqoD+hqh4OSwRUQMuzVZPG1R5nIGpXkS0EfXfRXC8bowQDzZflAx7DYBFCQM0oiqXm2/8/h6bJpLU/NiJ31EGA8VmQZFwcSuhedcKlWGA1YCK3Ck7QvcZS01hG89MfJF7Zo8VEV3dntYTedYOx+IhzAgMBAAECgYBGEvR4C30L5Yp7XDk+uQu33p5EQitYOoRZOF7zH/0y/mdMlpZ+b828VHdHgIzJr6dLRKYy47dWI06Q0NTJZ1AGEOTVr8Vt7y2I4nF6ZPE6u3Xz8Aemzvp1WOxqCc3d1bMQLLhaS5cmt9eVrSIr+6Xj1K1UiLcFX5krSPGGaXUUCQJBAM3rnL136N8wkULdMhrRQs/qSDcyaMkdcLsu6pxt2k/Q0aDHOTE644LX7s/AGwBP/aKGdSvPy7wkFETAik2aKY8CQQCr4STPRqfgi/RTkj+oBpynivfemsveUtXn1tEU3sfPN+QDToV5Rh9XqtkIaKoqJPGnUWb0QcyW2wLCx0h1DtjdAkBuI2BBYtpWThbT6ZV8DIMsy8V2aGrtbua154EqzALhf/IviX9ImpPHjxE3YyvN/frOLBaNqWXyKmYA4+7VVOh3AkBTD7eRL4z7V8cYB+oZUjCsSt0kR1xUMWXL5yUdV9fpjRH0gyK/i6Kj5B2EEciCG15oxE7jpVSwZmB8LoAWiblFAkAwVNAIEqcmDFulokv/E99KwOo4yUFKKDsgY1iTrFYGnMT2z6KnTQv60VhOXjvloCcYTAF7sR6YL2MtnmrLjt/L";
    private static String CHARSET = "UTF-8";

    public DFIFPayServiceImpl(Map<String,String> map) {
        if(map != null && !map.isEmpty()){
            if(map.containsKey("account")){
                this.account = map.get("account");
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
            if(map.containsKey("PAY_PUBLIC_KEY")){
                this.PAY_PUBLIC_KEY = map.get("PAY_PUBLIC_KEY");
            }
            if(map.containsKey("PAY_PRIVATE_KEY")){
                this.PAY_PRIVATE_KEY = map.get("PAY_PRIVATE_KEY");
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
            String param = sealRequest(payEntity);

            String resStr = HttpUtils.toPostForm(param,payUrl);

            if (StringUtils.isBlank(resStr)) {
                logger.error("[D15PAY]D15支付下单失败：请求返回结果为空");
                PayResponse.error("[D15PAY]D15支付下单失败：请求返回结果为空");
            }
            JSONObject jsonObject = JSONObject.fromObject(resStr);

            if (jsonObject.containsKey("flag") && jsonObject.getString("flag").equals("00")) {
                String payUrl = jsonObject.getString("payUri");
                return PayResponse.sm_link(payEntity,payUrl,"下单成功");
            }

            return PayResponse.error(jsonObject.getString("msg"));

        } catch (Exception e) {
            e.printStackTrace();
            return PayResponse.error("[D15PAY]D15支付扫码支付下单失败"+e.getMessage());
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        
        Map<String, String> metaSignMap = new TreeMap<>();
        metaSignMap.put("account", data.get("account"));
        metaSignMap.put("type", data.get("type"));
        metaSignMap.put("orderId", data.get("orderId"));
        metaSignMap.put("amount", data.get("amount"));
        metaSignMap.put("trade", data.get("trade"));
        metaSignMap.put("result", data.get("result"));// 支付状态
        metaSignMap.put("time", data.get("time"));// yyyyMMddHHmmss
        String jsonStr = ToolKit.mapToJson(metaSignMap);
        String sig = ToolKit.MD5(jsonStr + key, ToolKit.CHARSET);
        if (StringUtils.isBlank(sig)) {
            logger.error("[D15PAY]D15支付回调验签失败：回调生成签名为空");
            return "fail";
        }
        if (sig.equalsIgnoreCase(data.get("sig"))) {
            return "success";
        }
        return "fail";
    }

    /**
     *
     * @Description 封装支付请求参数
     * @param entity
     * @return
     * @throws Exception
     */
    private String sealRequest(PayEntity entity) throws Exception {
        logger.info("[D15PAY]D15支付封装请求参数开始=====================START==================");
        try {
            //创建支付请求参数存储对象
//            Map<String,String> data = new HashMap<>();
            //订单金额
            String amount = new DecimalFormat("0").format(entity.getAmount() * 100);

            Map<String, String> data = new TreeMap<>();
            data.put("orderId", entity.getOrderNo());
            data.put("version", "V1.1");
            data.put("charset", "UTF-8");
            data.put("random", RandomUtils.generateString(4));

            data.put("account", account);
            data.put("type", entity.getPayCode());
            data.put("amount", amount);
            data.put("trade", "top_up");
            data.put("backUri", notifyUrl);
            data.put("skipUri", entity.getRefererUrl());

            // 参数列表转json字符串加签名密钥，使用MD5加密UTF-8编码生成签名，并将签名加入参数列表
            String metaSignJsonStr = ToolKit.mapToJson(data);
            logger.info("[D15PAY]签名前请求参数：{}",metaSignJsonStr);
            String sig = ToolKit.MD5(metaSignJsonStr + key, ToolKit.CHARSET);
            logger.info("sig=" + sig);
            data.put("sig", sig);

            // 公钥加密、BASE64位加密、URL编码加密并拼接商户号和版本号
            byte[] dataStr = ToolKit.encryptByPublicKey(ToolKit.mapToJson(data).getBytes(ToolKit.CHARSET),
                    PAY_PUBLIC_KEY);
            String param = new BASE64Encoder().encode(dataStr);
            String reqParam = "data=" + URLEncoder.encode(param, ToolKit.CHARSET) + "&account=" + data.get("account")
                    + "&version=" + data.get("version");
            logger.info("[D15PAY]D15支付请求参数：{}",reqParam);

            return reqParam;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[D15PAY]D15支付封装请求参数异常:"+e.getMessage());
            throw new Exception("封装支付请求参数异常!");
        }
    }

    public static void main(String[] args) {
        PayEntity testPay = new PayEntity();
        testPay.setAmount(112);
        testPay.setOrderNo("smk0000000012345678");
        testPay.setPayCode("ZFB_WAP");
        testPay.setRefererUrl("http://localhost:8080/xxx");
        //testPay.setMobile("mobile");
        DFIFPayServiceImpl service = new DFIFPayServiceImpl(null);
        service.smPay(testPay);
    }
}
