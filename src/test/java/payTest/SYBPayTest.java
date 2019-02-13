package payTest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;

public class SYBPayTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SYBPayTest.class);
    
    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("fxid","2018108");//商户号
        data.put("payUrl","http://qzf.yuanma360.com/Pay");//商户订单号
        data.put("notifyUrl","http://qzf.yuanma360.com/Pay");//商品名
        data.put("secret","fBHRAYVnimySFGhbnrZHzMUlXeXwGdDV");//支付金额 单位元
        System.err.println(data.toString());
    }

    @Test
    public void callbackTest() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("fxid", "2018102");
        data.put("fxddh", "SYBbl1201810271546351");
        data.put("fxorder", "1531392180374");
        data.put("fxdesc", "TOP-UP");
        data.put("fxfee", "100.00");
        data.put("fxattch", "");
        data.put("fxstatus", "1");
        data.put("fxtime", System.currentTimeMillis()+"");
        String sign = generatorSign(data, 0);
        data.put("fxsign",sign);
        
        String notifyUrl = "http://localhost:85/JJF/Notify/SYBNotify.do";
        
        String response = HttpUtils.toPostForm(data, notifyUrl);
        
        System.err.println(response);
        
    }
    
    private String generatorSign(Map<String,String> data,Integer type) throws Exception{
        logger.info("[SYB]收盈宝支付生成支付签名串开始======================START========================");
        try {
            StringBuffer sb = new StringBuffer();
            if(type == 1){
                //生成支付待签名串,签名【md5(商务号+商户订单号+支付金额+异步通知地址+商户秘钥)】
                sb.append("2018102").append(data.get("fxddh")).append(data.get("fxfee")).append("");
            }else{
                //签名【md5(订单状态+商务号+商户订单号+支付金额+商户秘钥)】
                sb.append(data.get("fxstatus")).append("2018102").append(data.get("fxddh")).append(data.get("fxfee"));
            }
            sb.append("ZWknlKlofOldVvMZQwJxUXcTSqzNjykb");
            String signStr = sb.toString();
            logger.info("[SYB]收盈宝支付生成待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[SYB]收盈宝支付生成加密签名串:"+sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[SYB]收盈宝支付生成支付签名串异常:"+e.getMessage());
            throw new Exception("[SYB]收盈宝支付生成支付签名串异常");
        }
    }
}
