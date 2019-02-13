package payTest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;

import net.sf.json.JSONObject;

public class HLFPayTest {

    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("payWapMark","pay001");
        data.put("merchantId","9a1a6c8acfcaafdeaed44d9339b427f0");
        data.put("payUrl","http://ccb.lanmeibank.com/v1/order/add");
        data.put("notifyUrl","http://ccb.lanmeibank.com");
        data.put("secret", "0dd554f1c71f05aecdb17a062f5dadc5");
        System.err.println(data.toString());
    }
    
    @Test
    public void callbackTest() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("out_trade_no","1805021506989678");
        data.put("cope_pay_amount", "100.00");
        data.put("pay_type","1");
        data.put("state","0");
        data.put("merchant_order_number","HLFbl1201810241644031644038491");
        data.put("timestamp","1805021506989678");
        
        String sign = generatorSign(data);
        
        data.put("sign", sign);
        
        String notifyUrl = "http://localhost:85/JJF/Notify/HLFNotify.do";
        
        String response = HttpUtils.toPostJson(JSONObject.fromObject(data), notifyUrl);
        
        System.err.println(response);
    }
    
    private String generatorSign(Map<String,String> data) throws Exception{
        try {
            //签名规则:（将sign以外所有参数按照第一个字符的键值ASCII码递增排序,组合成“参数=参数值”的格式，
            //并且把这些参数用&字符连接起来,此时生成的字符串为待签名字符串。MD5签名的商户需要将key的值拼接在字符串后面，调用MD5算法生成sign）
            Map<String,String> treemap = MapUtils.sortByKeys(data);
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = treemap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = treemap.get(key)+"";
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign")) continue;
                sb.append("&").append(key).append("=").append(val);
            }
            sb.append("0dd554f1c71f05aecdb17a062f5dadc5");
            String signStr = sb.toString().replaceFirst("&", "");
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("生成支付签名串异常!");
        }
    }
}
