package payTest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.RandomUtils;

import net.sf.json.JSONObject;

/**
 * @ClassName YBTPayTest
 * @Description 中富通支付测试类
 * @author Hardy
 * @Date 2018年10月27日 上午10:27:17
 * @version 1.0.0
 */
public class YBTPayTest {
    
    private static final Logger logger = LoggerFactory.getLogger(YBTPayTest.class);

    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("payUrl", "http://47.52.44.224:8080/YBT/YBTPAY");// 当前接口版本 V1.0
        data.put("merchantNum", "3369220181022");// 分配给商家的商户号
        data.put("notifyUrl", "http://47.52.44.224:8080/YBT/YBTPAY");// 随机字符串
        data.put("merMark", "Dw9Rc");// 分配给商家的商户标识
        data.put("secret", "vS6mQLXh80jvBT0");// 客户端ip，如127.0.0.1
        System.err.println(data.toString());
    }

    /**
     * @throws Exception 
     * @Description 回调测试类
     */
    @Test
    public void callbackTest() throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("merchantNum", "3369220181022");
        data.put("orderNum", "1542005146879YFFXtWN2");
        data.put("amount", "10000");
        data.put("nonce_str", RandomUtils.generateString(8));
        data.put("orderStatus", "SUCCESS");
        data.put("remark", "TOP-UP");
        String sign = generatorSign(data, 0);
        data.put("sign", sign);
        String notifyUrl = "http://localhost:85/JJF/Notify/YBTNotify.do";
        String response = HttpUtils.toPostForm(data, notifyUrl);
        
        System.err.println(response);
    }
 
    
    
    private String generatorSign(Map<String,String> data,Integer type) throws Exception{
        logger.info("[YBT]中富通支付生成支付签名串开始=========================START=========================");
        try {
            StringBuffer sb = new StringBuffer();
            if(type == 1){
                //生成支付签名串
                //支付签名规则:参数签名顺序（必须按照此顺序组织签名）说明及示例
                //sign=md5（version=版本号&merchantNum=商户号&nonce_str=随机字符串&merMark=商户标识&client_ip=客户端IP
                //&payType=支付类型&orderNum=交易订单号&amount=交易金额&body=订单描述&key=商户密钥)
                sb.append("version=").append(data.get("version")).append("&");
                sb.append("merchantNum=").append("3369220181022").append("&");
                sb.append("nonce_str=").append(data.get("nonce_str")).append("&");
                sb.append("merMark=").append("Dw9Rc").append("&");
                sb.append("client_ip=").append(data.get("client_ip")).append("&");
                sb.append("payType=").append(data.get("payType")).append("&");
                sb.append("orderNum=").append(data.get("orderNum")).append("&");
                sb.append("amount=").append(data.get("amount")).append("&");
                sb.append("body=").append(data.get("body")).append("&");
            }else{
                //生成回调签名串
                //sign=md5（merchantNum=商户号&orderNum=商户订单号&amount=交易金额&nonce_str=随机字符串&orderStatus=订单状态&key=商户密钥)
                sb.append("merchantNum=").append("3369220181022").append("&");
                sb.append("orderNum=").append(data.get("orderNum")).append("&");
                sb.append("amount=").append(data.get("amount")).append("&");
                sb.append("nonce_str=").append(data.get("nonce_str")).append("&");
                sb.append("orderStatus=").append(data.get("orderStatus")).append("&");
            }
            sb.append("key=").append("vS6mQLXh80jvBT0");
            //生成待签名串
            String signStr = sb.toString();
            //签名
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[YBT]中富通支付生成待签名串:{},[YBT]中富通支付生成加密签名串:{}",signStr,sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YBT]中富通支付生成支付签名串异常:"+e.getMessage());
            throw new Exception("[YBT]中富通支付生成支付签名串异常!");
        }
    }
    
    /**
     * 
     * @throws Exception 
     * @Description 支付测试类
     */
    @Test
    public void payTest() throws Exception{
        String url = "http://47.52.44.224:8080/YBT/YBTPAY";
//        String key = "vS6mQLXh80jvBT0"; // 商户密钥
        Map<String, String> map = new HashMap<String, String>();
        map.put("version", "V1.0");
        map.put("nonce_str", "XXXXX");
        map.put("merMark", "Dw9Rc");
        map.put("client_ip", "127.0.0.1");
        map.put("amount", "10000"); //分
        map.put("orderTime", "2018-08-07 11:01:01");
        map.put("signType", "MD5");
        map.put("merchantNum", "3369220181022"); // 商户号
        map.put("payType", "B2C"); // 支付方式
        map.put("bank_code", "ABC");  // 银行编码
        map.put("body", "XXXX");
        map.put("orderNum", "Dw9RcYBTbl12018102710475751");
        map.put("notifyUrl", "127.0.0.1");
//        String [] arr = {"version", "merchantNum", "nonce_str", "merMark", "client_ip", "payType", "orderNum", "amount", "body"};
//        String str = ZHUtils.getStr(map, arr);
//        String sign = MD5Utils.MD5Encode(str + "&key=" + key, "UTF-8", true);
        String sign = generatorSign(map, 1);
        map.put("sign", sign);
        
//        String result = ZHUtils.sendPostJson(url, new JSONObject(map));
        String result = HttpUtils.toPostJsonStr(JSONObject.fromObject(map), url);
        System.out.println(result);
            
    }
}
