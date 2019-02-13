package payTest;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: zed
 * @Date: 2019/1/23 09:56
 * @Description: 万达支付回调测试类
 */
public class WDZFPayTest {
    @Test
    public void test() throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("CustomerId","bl1wdzf201901230925500925503966");
        data.put("OrderId","saiwengeniubi666");
        data.put("Money","1999");
        data.put("Status","1");
        data.put("Time","2019-1-23 10:28:33");
        data.put("Message","top_Up");
        data.put("Type","8");
        String sign = callbackSign(data);
        data.put("Sign", sign);

        String postJson = JSONObject.fromObject(data).toString();

        String notifyUrl = "http://localhost:85/JJF/Notify/WDZFNotify.do";

        String response = HttpUtils.toPostJson(postJson, notifyUrl);

        System.err.println(response);
    }

    private String callbackSign(Map<String, String> data) {
        String Key = "9d40e1329780d62e";
        StringBuffer sb = new StringBuffer();
        sb.append("CustomerId=").append(data.get("CustomerId"));
        sb.append("&OrderId=").append(data.get("OrderId"));
        sb.append("&Money=").append(data.get("Money"));
        sb.append("&Status=").append(data.get("Status"));
        sb.append("&Message=").append(data.get("Message"));
        sb.append("&Type=").append(data.get("Type"));
        sb.append("&Key=").append(Key);
        String localSign = null;
        try {
            localSign = MD5Utils.md5toUpCase_32Bit(sb.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return localSign;
    }
}
