package payTest;

import com.cn.tianxia.pay.impl.HYZFPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: HYZFPayTest
 * @Description: 虎云支付测试
 * @Author: Zed
 * @Date: 2018-12-18 10:04
 * @Version:1.0.0
 **/

public class HYZFPayTest {

    @Test
    public void testCallback() throws Exception{

        String key = "9f8fc1268e02c58aaef50cf4d1e9944fe05074b5";

        Map<String,String> data = new HashMap<>();
        data.put("status","1");//        订单状态	status
        data.put("customerid","110040");   //        商户编号	customerid
        data.put("sdpayno","pornhub001");//        平台订单号	sdpayno
        data.put("sdorderno","HYZFbl1201812171452521452522217");//        商户订单号	sdorderno
        data.put("total_fee","100.00");//        交易金额	total_fee
        data.put("paytype","alipayscan");//        支付类型	paytype
        data.put("remark","");//        订单备注说明	remark

        StringBuffer sb = new StringBuffer();
        sb.append("customerid=").append(data.get("customerid"))
                .append("&status=").append(data.get("status"))
                .append("&sdpayno=").append(data.get("sdpayno"))
                .append("&sdorderno=").append(data.get("sdorderno"))
                .append("&total_fee=").append(data.get("total_fee"))
                .append("&paytype=").append(data.get("paytype"))
                .append("&").append(key);
        String sign = MD5Utils.md5toUpCase_32Bit(sb.toString());
        data.put("sign",sign);//        md5验证签名串	sign

        String notifyUrl = "http://localhost:85/JJF/Notify/HYZFNotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);
    }
}
