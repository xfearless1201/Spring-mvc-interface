package payTest;

import com.cn.tianxia.pay.impl.TT2PayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: TT2PayTest
 * @Description: TT2踢踢支付2测试类
 * @Author: Zed
 * @Date: 2018-12-19 14:20
 * @Version:1.0.0
 **/

public class TT2PayTest {
    @Test
    public void testCallbak() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("money","0.03");
        data.put("tradeNo","TT2bl120181219144221");
        data.put("dt",String.valueOf(System.currentTimeMillis()));
        data.put("type","alipay");
        data.put("clientId","vbkh1");
        TT2PayServiceImpl service = new TT2PayServiceImpl(null);
        String sign = service.callbackSign(data);
        data.put("sign",sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/TT2Notify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);

    }
}
