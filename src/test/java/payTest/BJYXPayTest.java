package payTest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName BJYXPayTest
 * @Description 北京易迅支付
 * @author Hardy
 * @Date 2018年10月6日 下午2:28:31
 * @version 1.0.0
 */
public class BJYXPayTest {

    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("payUrl", "http://pay.weiguodu.cn/apisubmit");
        data.put("customerId", "12152");
        data.put("notifyUrl", "http://localhost:8087/JJF/Notify/BJYXNotify.do");
        data.put("apiKey", "b098a41a2e9a38f571767dd9883bf829873cc2de");
        System.err.println(data.toString());
    }
    
    /**
     * 
     * @throws Exception 
     * @Description 回调测试类
     */
    @Test
    public void callbackTest() throws Exception{
        String apiKey = "baa4b9af2a2ac74c49104866c421b463346993ac";
        Map<String,String> data = new HashMap<>();
        data.put("status", "1");
        data.put("customerid", "12214");
        data.put("sdpayno", "777201810061519254322");//流水号
        data.put("sdorderno", "BJYXbl120181006145954");//订单号
        data.put("total_fee", "100.00");
        data.put("paytype", "alipay");
        data.put("remark", "TOP-UP");
        StringBuffer sb = new StringBuffer();
        sb.append("customerid=").append(data.get("customerid")).append("&");
        sb.append("status=").append(data.get("status")).append("&");
        sb.append("sdpayno=").append(data.get("sdpayno")).append("&");
        sb.append("sdorderno=").append(data.get("sdorderno")).append("&");
        sb.append("total_fee=").append(data.get("total_fee")).append("&");
        sb.append("paytype=").append(data.get("paytype")).append("&");
        sb.append(apiKey);
        String sign = MD5Utils.md5toUpCase_32Bit(sb.toString()).toLowerCase();
        data.put("sign", sign);
        String notifyUrl = "http://localhost:85/JJF/Notify/BJYXNotify.do";
        String response = HttpUtils.toPostForm(data, notifyUrl);
        System.err.println("回调结果:"+response);
    }
}
