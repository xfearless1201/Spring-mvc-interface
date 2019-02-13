package payTest;

import com.cn.tianxia.pay.impl.ZIHAIPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: zed
 * @Date: 2019/1/29 14:19
 * @Description: 资海支付回调测试类
 */
public class ZIHAIPayTest {
    @Test
    public void testCallbak() throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("memberid", "190190551");
        data.put("orderid", "bl1zihai201901290934500934506218");
        data.put("amount", "333.00");
        data.put("transaction_id", "woshishabi1234");
        data.put("datetime", "2019-1-29 13:39:00");
        data.put("returncode", "00");
        data.put("attach", "top_Up");
        String sign = callbackSign(data);
        data.put("sign", sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/ZIHAINotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);
    }

    private String callbackSign(Map<String, String> data) throws Exception {
        String key = "lxnugno9lun38d29qjyu6n9yce69ylh9";
        Map<String, String> config = new HashMap<>();
        config.put("md5Key", key);
        ZIHAIPayServiceImpl zihaiPayService = new ZIHAIPayServiceImpl(config);
        String sign = zihaiPayService.generatorSign(data);
        return sign;
    }
}
