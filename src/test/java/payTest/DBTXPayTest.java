package payTest;

import com.cn.tianxia.pay.impl.DBTXPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: zed
 * @Date: 2019/1/29 11:19
 * @Description: 大宝天下支付回调测试类
 */
public class DBTXPayTest {
    @Test
    public void testCallbak() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("memberid","190191995");
        data.put("orderid","tasdbtx734161734169038");
        data.put("amount","199.0");
        data.put("transaction_id","woshishabi1234");
        data.put("datetime","2019-1-29 13:39:00");
        data.put("returncode","00");
        data.put("attach","top_Up");
        String sign = callbackSign(data);
        data.put("sign",sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/DBTXNotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);
    }

    private String callbackSign(Map<String,String> data) throws Exception{
        String key = "fcvq3tt8az8pzle0llkcj4v6p38iage5";
        Map<String,String> config = new HashMap<>();
        config.put("key",key);
        DBTXPayServiceImpl dbtxPayService = new DBTXPayServiceImpl(config);
        String sign = dbtxPayService.generatorSign(data);
        return sign;
    }
}
