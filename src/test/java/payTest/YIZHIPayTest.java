package payTest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName YIZHIPayTest
 * @Description 易智支付
 * @author Hardy
 * @Date 2018年10月30日 下午2:41:33
 * @version 1.0.0
 */
public class YIZHIPayTest {
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(YIZHIPayTest.class);
    
    @Test
    public void callbackTest() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("partner", "2108");
        data.put("ordernumber", "YIZHIbl1201810301457031457034209");
        data.put("orderstatus", "1");
        data.put("paymoney", "100");
        data.put("sysnumber", System.currentTimeMillis()+"");
        data.put("attach", "TOP-UP");
        String sign = generatorSign(data, 0);
        data.put("sign", sign);
        String notifyUrl = "http://192.168.0.61:282/XPJ/Notify/YIZHINotify.do";
        
        String response = HttpUtils.toPostForm(data, notifyUrl);
        
        System.err.println(response);
    }
    
    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("partner", "2108");
        data.put("payUrl", "http://open.8wpay.com/online/gateway");
        data.put("notifyUrl", "http://txw.tx8899.com/XPJ/Notify/YIZHINotify.do");
        data.put("secret", "48e11765b01006ede43c446012a7346a");
        data.put("method", "yzfapp.online.interface");
        
        System.err.println(data.toString());
    }
    
    private String generatorSign(Map<String,String> data,int signType) throws Exception{
        logger.info("[YIZHI]易智支付生成支付签名开始=======================START======================");
        try {
            
            StringBuffer sb = new StringBuffer();
            if(signType == 1){
                //支付签名
                sb.append("version=").append(data.get("version")).append("&");
                sb.append("method=").append(data.get("method")).append("&");
                sb.append("partner=").append(data.get("partner")).append("&");
                sb.append("banktype=").append(data.get("banktype")).append("&");
                sb.append("paymoney=").append(data.get("paymoney")).append("&");
                sb.append("ordernumber=").append(data.get("ordernumber")).append("&");
                sb.append("callbackurl=").append(data.get("callbackurl"));
            }else{
                sb.append("partner=").append(data.get("partner")).append("&");
                sb.append("ordernumber=").append(data.get("ordernumber")).append("&");
                sb.append("orderstatus=").append(data.get("orderstatus")).append("&");
                sb.append("paymoney=").append(data.get("paymoney"));
            }
            sb.append("48e11765b01006ede43c446012a7346a");
            //生成待签名串
            String signStr = sb.toString();
            logger.info("[YIZHI]易智支付生成待签名串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[YIZHI]易智支付生成加密签名串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YIZHI]易智支付生成支付签名异常:"+e.getMessage());
            throw new Exception("[YIZHI]易智支付生成支付签名异常");
        }
    }
}
