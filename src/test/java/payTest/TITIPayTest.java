package payTest;

import com.cn.tianxia.pay.impl.TITIPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: TITIPayTest
 * @Description:
 * @Author: Zed
 * @Date: 2018-12-18 10:40
 * @Version:1.0.0
 **/

public class TITIPayTest {
    @Test
    public void testCallbak() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("money","0.01");
        data.put("tradeNo","TITIbl12018121811311");
        data.put("dt",String.valueOf(System.currentTimeMillis()));
        data.put("type","alipay");
        data.put("clientId","vbkh1");
        TITIPayServiceImpl service = new TITIPayServiceImpl(null);
        String sign = service.callbackSign(data);
        data.put("sign",sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/TITINotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);

    }
}
