package payTest;

import com.cn.tianxia.pay.impl.TT2PayServiceImpl;
import com.cn.tianxia.pay.impl.YIFBPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: YIFBPayTest
 * @Description: 易付宝支付测试类
 * @Author: Zed
 * @Date: 2018-12-27 19:24
 * @Version:1.0.0
 **/

public class YIFBPayTest {

    @Test
    public void testCallbak() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("memberid","181292815");
        data.put("orderid","YIFBbl1201812272028492028491785");
        data.put("amount","100.0");
        data.put("transaction_id","ps123456");
        data.put("datetime","20181227");
        data.put("returncode","00");
        data.put("attach","none");

        YIFBPayServiceImpl service = new YIFBPayServiceImpl(null);
        String sign = service.generatorSign(data);
        data.put("sign",sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/YIFBNotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);

    }

}
