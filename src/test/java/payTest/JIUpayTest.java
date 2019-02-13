package payTest;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: zed
 * @Date: 2019/1/17 11:28
 * @Description: 九久支付回调测试类
 */
public class JIUpayTest {
    @Test
    public void testCallbak() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("resultcode","1");
        data.put("transactionid","woshinimadedasb1234");
        data.put("mchid","90060");
        data.put("mchno","bl1jiu201901171104021104022540");
        data.put("tradetype","alipay");
        data.put("totalfee","12200");
        data.put("attach","");
        String sign = callbackSign(data);
        data.put("sign",sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/JIUNotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);
    }

    private String callbackSign(Map<String,String> data) {
        String key = "1742863a95604211af386b325d35403c";
        StringBuffer sb = new StringBuffer();
        sb.append("resultcode").append("=").append(data.get("resultcode"));
        sb.append("&").append("transactionid").append("=").append(data.get("transactionid"));
        sb.append("&").append("mchid").append("=").append(data.get("mchid"));
        sb.append("&").append("mchno").append("=").append(data.get("mchno"));
        sb.append("&").append("tradetype").append("=").append(data.get("tradetype"));
        sb.append("&").append("totalfee").append("=").append(data.get("totalfee"));
        sb.append("&").append("attach").append("=").append(data.get("attach"));
        sb.append("&").append("key").append("=").append(key);
        String localSign = null;
        try {
            localSign = MD5Utils.md5toUpCase_32Bit(sb.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return localSign;
    }
}
