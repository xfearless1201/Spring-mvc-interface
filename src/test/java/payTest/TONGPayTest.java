package payTest;

import com.cn.tianxia.pay.impl.TONGPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @ClassName: TONGPayTest
 * @Description: 通支付测试类
 * @Author: Zed
 * @Date: 2018-12-23 11:26
 * @Version:1.0.0
 **/

public class TONGPayTest {

    private String secret = "Hewp1EZu71d0ZZ1DDe9Xv1x798DW30w9";//秘钥

    @Test
    public void testCallbak() throws Exception{

        Map<String,String> callbackMap = new HashMap<>();
//        {"sign":"b36708dd2e609392b2ff06f6d11d3d54","endtime":"2018-12-23 08:24:26","trade_no":"8961356","status":"1","attach":"qucdb345349845","sign_type":"MD5","money":"50.00",
//                "pid":"2714","code":"1","out_trade_no":"TONGquc201812230824080824081151","type":"alipay2","version":"1"}

        callbackMap.put("endtime","2018-12-23 14:14:26");
        callbackMap.put("trade_no","8961356");
        callbackMap.put("status","1");
        callbackMap.put("attach","qucdb345349845");
        callbackMap.put("sign_type","MD5");
        callbackMap.put("money","50.00");
        callbackMap.put("pid","2714");
        callbackMap.put("code","1");
        callbackMap.put("out_trade_no","TONGbl1201812231413061413067867");
        callbackMap.put("type","alipay2");
        callbackMap.put("version","1");
        String sign = generatorSign(callbackMap);
        callbackMap.put("sign",sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/TONGNotify.do";

        String response = HttpUtils.toPostForm(callbackMap, notifyUrl);

        System.err.println(response);

    }

    /**
     *
     * @Description 签名
     * @param data
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data) throws Exception{
        try {
            StringBuffer sb = new StringBuffer();
            Map<String,String> sortmap = MapUtils.sortByKeys(data);
            Iterator<String> iterator = sortmap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = sortmap.get(key);
                if(StringUtils.isBlank(val) || "sign".equalsIgnoreCase(key) || "sign_type".equalsIgnoreCase(key)) continue;
                sb.append("&").append(key).append("=").append(val);
            }
            String signStr = sb.append(secret).toString().replaceFirst("&", "");
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("[TONG]通支付生成签名异常");
        }
    }
}
