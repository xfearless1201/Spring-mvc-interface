package payTest;

import com.cn.tianxia.pay.impl.SLONPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: zed
 * @Date: 2019/1/29 11:21
 * @Description: SLONPayTest
 */
public class SLONPayTest {

    @Test
    public void testCallbak() throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("memberid", "190191995");
        data.put("orderid", "tasslon201901281735331735339220");
        data.put("amount", "99.00");
        data.put("transaction_id", "woshishabi1234");
        data.put("datetime", "2019-1-29 13:39:00");
        data.put("returncode", "00");
        data.put("attach", "top_Up");
        String sign = callbackSign(data);
        data.put("sign", sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/SLONNotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);
    }

    private String callbackSign(Map<String, String> data) throws Exception {
        String key = "yc2jf3c2nexbwestq0763qg4yhuczk3f";
        Map<String, String> config = new HashMap<>();
        config.put("md5Key", key);
        SLONPayServiceImpl slonPayService = new SLONPayServiceImpl(config);
        String sign = slonPayService.generatorSign(data);
        return sign;
    }
}
