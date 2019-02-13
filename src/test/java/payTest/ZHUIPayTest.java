package payTest;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: zed
 * @Date: 2019/1/15 11:55
 * @Description: 众惠支付回调测试类
 */
public class ZHUIPayTest {
    @Test
    public void testCallbak() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("status","00");
        data.put("out_trade_no","bl1zhui20190115103434103434917");
        data.put("total_fee","12201");
        data.put("statusMessage","success");
        data.put("payment_method","100");
        data.put("orderTime","2019-01-15 11:11:11");
        data.put("cas_time_stamp",String.valueOf(System.currentTimeMillis()));
        String sign = callbackSign(data);
        data.put("sign",sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/ZHUINotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);
    }

    private String callbackSign(Map<String,String> data) {
        String key = "b1b59ac212db43f59ffd05f814ec8595";
        StringBuffer sb = new StringBuffer();
        sb.append(data.get("out_trade_no"));
        sb.append(data.get("total_fee"));
        sb.append(data.get("orderTime"));
        sb.append(data.get("cas_time_stamp"));
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
