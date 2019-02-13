package payTest;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: BATPayTest
 * @Description: 蝙蝠侠支付回调测试
 * @Author: Zed
 * @Date: 2019-01-09 16:17
 * @Version:1.0.0
 **/

public class BATPayTest {
    @Test
    public void testCallbak() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("bb","1.0");
        data.put("status","success");
        data.put("shid","10223");
        data.put("ddh","dfcbat201901091719491719494578");
        data.put("je","20");
        data.put("zftd","zfg");
        data.put("ybtz","xxxxxxxxxx");
        data.put("tbdz","xxxxxxxx");
        data.put("ddmc","sssssss");
        data.put("ddbz","ssssssssss");
        String sign = callbackSign(data);
        data.put("sign",sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/BATNotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);
    }

    private String callbackSign(Map<String,String> data) {
        String key = "1ebo1ivs12rs5i7s9e3kxiv0lmzoty3g9m3c92r0";
        StringBuffer sb = new StringBuffer();
        sb.append("status=").append(data.get("status"));
        sb.append("&shid=").append(data.get("shid"));
        sb.append("&bb=").append(data.get("bb"));
        sb.append("&zftd=").append(data.get("zftd"));
        sb.append("&ddh=").append(data.get("ddh"));
        sb.append("&je=").append(data.get("je"));
        sb.append("&ddmc=").append(data.get("ddmc"));
        sb.append("&ddbz=").append(data.get("ddbz"));
        sb.append("&ybtz=").append(data.get("ybtz"));
        sb.append("&tbtz=").append(data.get("tbtz"));
        sb.append("&").append(key);
        String localSign = null;
        try {
            localSign = MD5Utils.md5toUpCase_32Bit(sb.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return localSign;
    }
}
