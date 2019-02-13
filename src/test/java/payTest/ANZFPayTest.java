package payTest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.pay.impl.ANZFPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName ANZFPayTest
 * @Description A9支付测试类
 * @author Hardy
 * @Date 2018年11月7日 下午9:56:30
 * @version 1.0.0
 */
public class ANZFPayTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ANZFPayTest.class);

    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("payUrl", "http://pay.eknvmp.cn/pay");
        data.put("MerchantId", "1479040");
        data.put("notifyUrl", "http://localhost:8087/JJF/Notify/ANZFNotify.do");
        data.put("secret", "21sOOqp30KLDldq");
        System.err.println(data.toString());
    }
    
    /**
     * 
     * @throws Exception 
     * @Description 回调
     */
    @Test
    public void callbackTest() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("MerchantId","1479040");//商户编号,聚合商户编号
        data.put("out_trade_no","ANZFbl1201811072200422200429175");//商户订单号,商户订单号
        data.put("total_amount","100.00");//订单金额,精确到分 0.00
        data.put("paytime",String.valueOf(System.currentTimeMillis()));//  
        data.put("trade_no",String.valueOf(System.currentTimeMillis()));//
        data.put("trade_status","SUCCESS");//同步跳转,同步跳转地址（部分通道无同步跳转）
        data.put("sign",generatorSign(data));//签名摘要 
        
        String notifyUrl = "http://192.168.0.61:282/XPJ/Notify/ANZFNotify.do";
        
        String response = HttpUtils.toPostForm(data, notifyUrl);
        
        System.err.println(response);
    }
    
    private String generatorSign(Map<String,String> data) throws Exception{
        logger.info("[ANZF]A9支付封装支付生成加密签名串开始======================START===================");
        try {
            //签名规则:
            //第一步，设所有发送或者接收到的数据为集合M，将集合M内非空参数值的参数按照参数名ASCII码从小到大排序（字典序），
            //使用URL键值对的格式（即key1=value1&key2=value2…）拼接成字符串stringA。
            //如果参数的值为空不参与签名；
            //第二步，在stringA最后拼接上key得到stringSignTemp字符串，并对stringSignTemp进行MD5运算，
            //再将得到的字符串所有字符转换为大写，得到sign值signValue。
            StringBuffer sb = new StringBuffer();
            Map<String,String> sortmap = MapUtils.sortByKeys(data);
            Iterator<String> iterator = sortmap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = sortmap.get(key);
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign")) continue;
                sb.append("&").append(key).append("=").append(val);
            }
            sb.append("21sOOqp30KLDldq");
            String signStr = sb.toString().replaceFirst("&", "");
            logger.info("[ANZF]A9支付生成待签名串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[ANZF]A9支付生成加密签名串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[ANZF]A9支付封装支付生成加密签名串异常:{}",e.getMessage());
            throw new Exception("[ANZF]A9支付封装支付生成加密签名串异常");
        }
    }
}
