package payTest;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: BFZFPayTest
 * @Description: 宝付支付测试类
 * @Author: Zed
 * @Date: 2019-01-07 17:38
 * @Version:1.0.0
 **/

public class BFZFPayTest {
    @Test
    public void testCallbak() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("memberid","180110048");//商户编号
        data.put("orderid","BFZFbl1201901071416231416232500");//订单号
        data.put("amount","500.00");//订单金额
        data.put("transaction_id","woshidashabi1111");//	交易流水号
        data.put("datetime","2019-01-07 11:11:11");//交易时间
        data.put("returncode","00");//交易状态
        data.put("attach","");//扩展返回

        String sign = generatorSign(data);
        data.put("sign",sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/BFZFNotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);
    }
    private String generatorSign(Map<String, String> params) throws NoSuchAlgorithmException {
        String md5key = "ynclaw7zuqxn7y8z4b086l3f75u0j5ie";
        StringBuffer sb = new StringBuffer();

        if (params == null || params.size() <= 0) {
            return "";
        }
        for (String key : params.keySet()) {
            String value = params.get(key);
            if (StringUtils.isEmpty(value) || "sign".equals(key)) {
                continue;
            }
            sb.append(key).append("=").append(value).append("&");
        }

        sb.append("key=").append(md5key);
        String md5 = MD5Utils.md5toUpCase_32Bit(sb.toString());
        return md5;
    }

}
