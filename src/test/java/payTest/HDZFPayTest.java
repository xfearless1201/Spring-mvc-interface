package payTest;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: HDZFPayTest
 * @Description: 宏达支付回调测试类
 * @Author: Zed
 * @Date: 2019-01-10 20:01
 * @Version:1.0.0
 **/

public class HDZFPayTest {
    @Test
    public void testCallbak() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("type","toCard");
        data.put("money","99");
        data.put("extend","woshishabi123");
        data.put("out_order_id","TAShdzf20190110181036181036195");
        data.put("no","woshishabi123");
        data.put("pid","10094");
        String sign = callbackSign(data);
        data.put("sign",sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/HDZFNotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);
    }

    private String callbackSign(Map<String,String> data) {
        String KEY = "1d8420d445e8441173009701a6e39f4a";
        StringBuffer sb = new StringBuffer();
        sb.append(data.get("pid"));
        sb.append(data.get("type"));
        sb.append(data.get("no"));
        sb.append(data.get("money"));
        sb.append(data.get("extend"));
        sb.append(data.get("out_order_id"));
        sb.append(KEY);
        String localSign = null;
        try {
            localSign = MD5Utils.md5toUpCase_32Bit(sb.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return localSign;
    }
}
