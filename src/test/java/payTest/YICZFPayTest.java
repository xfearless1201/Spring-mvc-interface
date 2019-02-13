package payTest;

import com.cn.tianxia.pay.impl.TT2PayServiceImpl;
import com.cn.tianxia.pay.impl.YICZFPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: YICZFPayTest
 * @Description: 宜橙支付测试类
 * @Author: Zed
 * @Date: 2019-01-06 17:18
 * @Version:1.0.0
 **/

public class YICZFPayTest {
    @Test
    public void testCallbak() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("orderid","YICZFbl1201901061404311404316732");
        data.put("opstate","0");
        data.put("sysorderid","woshishabi123");
        data.put("ovalue","100");
        String sign = callbackSign(data);
        data.put("sign",sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/YICZFNotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);
    }

    private String callbackSign(Map<String,String> data) {
        String key = "7046c9fe48f049eea5c71715d4c7844f";
        StringBuffer sb = new StringBuffer();
        sb.append("orderid=").append(data.get("orderid"));
        sb.append("&opstate=").append(data.get("opstate"));
        sb.append("&ovalue=").append(data.get("ovalue"));
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
