package payTest;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: JIANPayTest
 * @Description: 简付支付测试类
 * @Author: Zed
 * @Date: 2019-01-08 19:22
 * @Version:1.0.0
 **/

public class JIANPayTest {
    @Test
    public void testCallbak() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("user_id","4566");
        data.put("user_order","bl1jian201901081953471953476362");
        data.put("user_money","200.00");
        data.put("user_status","1");
        data.put("user_ext","top_up");
        String sign = callbackSign(data);
        data.put("user_sign",sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/JIANNotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);
    }

    private String callbackSign(Map<String,String> data) {
        String key = "654E5775E731CB9A2582328F120CB0AE";
        StringBuffer sb = new StringBuffer();
        sb.append("user_id=").append(data.get("user_id"));
        sb.append("&user_order=").append(data.get("user_order"));
        sb.append("&user_money=").append(data.get("user_money"));
        sb.append("&user_status=").append(data.get("user_status"));
        sb.append("&user_ext=").append(data.get("user_ext"));
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
