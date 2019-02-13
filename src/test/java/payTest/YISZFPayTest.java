package payTest;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: YISZFPayTest
 * @Description: 易收支付测试类
 * @Author: Zed
 * @Date: 2019-01-07 15:05
 * @Version:1.0.0
 **/

public class YISZFPayTest {
    @Test
    public void testCallbak() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("status","1");
        data.put("customerid","10930");
        data.put("sdpayno","woshishabi123");
        data.put("sdorderno","YISZFbl1201901071406351406354501");
        data.put("total_fee","100.00");
        data.put("paytype","alipay");
        data.put("remark","100");
        String sign = callbackSign(data);
        data.put("sign",sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/YISZFNotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);
    }

    private String callbackSign(Map<String,String> data) {
        String key = "6cbbf06e3cc1e3ac0240194de7060ab2d0fbd4d0";
        StringBuffer sb = new StringBuffer();
        sb.append("customerid=").append(data.get("customerid"));
        sb.append("&status=").append(data.get("status"));
        sb.append("&sdpayno=").append(data.get("sdpayno"));
        sb.append("&sdorderno=").append(data.get("sdorderno"));
        sb.append("&total_fee=").append(data.get("total_fee"));
        sb.append("&paytype=").append(data.get("paytype")).append("&");
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
