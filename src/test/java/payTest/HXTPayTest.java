package payTest;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: zed
 * @Date: 2019/1/18 17:06
 * @Description: 华夏通支付回调测试
 */
public class HXTPayTest {
    @Test
    public void testCallbak() throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("orderno", "woshinimadashabi123456");
        data.put("user_order_no", "bl1hxt201901181704151704152402");
        data.put("tradeno", "alipay1234567890");
        data.put("price", "122.01");
        data.put("realprice", "122.01");
        data.put("cuid", "193278yHsZ");
        data.put("note", "");
        String sign = callbackSign(data);
        data.put("sign", sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/HXTNotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);
    }

    private String callbackSign(Map<String, String> data) {
        String key = "98beabcaeab248a3a5072642fb6fc8f2";
        //user_order_no + orderno + tradeno + price + realprice + token
        StringBuffer sb = new StringBuffer();
        sb.append(data.get("user_order_no"));
        sb.append(data.get("orderno"));
        sb.append(data.get("tradeno"));
        sb.append(data.get("price"));
        sb.append(data.get("realprice"));
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

