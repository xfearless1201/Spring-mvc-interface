package payTest;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.util.v2.MapUtils;

import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Auther: zed
 * @Date: 2019/1/17 11:28
 * @Description: 新币宝支付回调测试类
 */
public class XBBPayTest {
    @Test
    public void testCallbak() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("UserName","56625");
        data.put("OrderId","woshinimadedasb1234");
        data.put("OrderNum","bl1xbbzf201901161659261659263627");
        data.put("Type","1");
        data.put("Coin","DC");
        data.put("CoinAmount","123");
        data.put("LegalAmount","124.00");
        data.put("State1","2");
        data.put("State2","2");
        data.put("CreateTime","2019-01-17 14:03:00");
        data.put("FinishTime","2019-01-17 14:03:00");
        data.put("Remark","top_up");
        data.put("Price","12.1");
        data.put("Token","1234");

        String sign = callbackSign(data);
        data.put("Sign",sign);

        String requestString = MapUtils.mapToString(data);

        String notifyUrl = "http://localhost:85/JJF/Notify/XBBZFNotify.do";

        String response = HttpUtils.toPostForm(requestString, notifyUrl);

        System.err.println(response);
    }


    private String callbackSign(Map<String,String> data) {
        String key = "4HfryatIJv";
        StringBuffer sb = new StringBuffer();
        TreeMap<String,String> sortMap = new TreeMap<>(data);
        for (Map.Entry entry:sortMap.entrySet()) {
            if ("FinishTime".equals(entry.getKey()) || "Sign".equals(entry.getKey()))
                continue;
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(key);
        String localSign = null;
        try {
            localSign = MD5Utils.md5toUpCase_32Bit(sb.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return localSign;
    }
}
