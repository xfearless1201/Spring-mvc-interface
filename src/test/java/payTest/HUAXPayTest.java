package payTest;

import com.cn.tianxia.pay.impl.HUAXPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: HUAXPayTest
 * @Description: 华信支付回调测试
 * @Author: Zed
 * @Date: 2018-12-28 21:15
 * @Version:1.0.0
 **/

public class HUAXPayTest {
    @Test
    public void testCallbak() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("orderNo","imhuaxin12345");
        data.put("outOrderNo","HUAXbl1201812282107322107322353");
        data.put("merchantNo","eWGnGGXaQFEwnnZ");
        data.put("amount","100.0");
        data.put("payTime",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        HUAXPayServiceImpl service = new HUAXPayServiceImpl(null);
        String sign = service.generatorSign(data);
        data.put("sign",sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/HUAXNotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);

    }
}

