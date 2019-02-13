package payTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cn.tianxia.pay.impl.TT2PayServiceImpl;
import com.cn.tianxia.pay.impl.XFTPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.google.gson.JsonObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: XFTPayTest
 * @Description: 信付通支付
 * @Author: Zed
 * @Date: 2018-12-24 15:30
 * @Version:1.0.0
 **/

public class XFTPayTest {
    @Test
    public void testCallback() throws Exception{
        String notifyString = "{\"gmt_create\"=\"2018-01-18 16:50:27\", \"order_no\"=\"XFTbl1201812241505481505484144\", \"gmt_payment\"=\"2018-01-18 16:50:27\", \"seller_email\"=\"3xxxxxx@qq.com\", \"notify_time\"=\"2018-01-18 16:50:27\", \"quantity\"=\"1\", \"sign\"=\"D1AB9C9A70474228EB797B6F23CFE5F4CBAC437F\", \"discount\"=\"0.00\", \"body\"=\"bonus\", \"is_success\"=\"T\", \"title\"=\"11111\", \"gmt_logistics_modify\"=\"2018-01-18 16:50:27\", \"notify_id\"=\"99c7f42c41e7426481a57f7b65d60887\", \"notify_type\"=\"WAIT_TRIGGER\", \"payment_type\"=\"1\", \"ext_param2\"=\"BANKPAY\", \"price\"=\"2.00\", \"total_fee\"=\"2.00\", \"trade_status\"=\"TRADE_FINISHED\", \"trade_no\"=\"101801181602312\",\"signType\"=\"SHA\", \"seller_actions\"=\"SEND_GOODS\", \"seller_id\"=\"100000000008888\", \"is_total_fee_adjust\"=\"0\"}";
        String notifyJson = notifyString.replaceAll("=",":");
        //JSONObject jsonObject = JSONObject.parseObject(notifyString);
        Map<String,String> data =  JSON.parseObject(notifyJson,Map.class);
        XFTPayServiceImpl service = new XFTPayServiceImpl(null);
        String sign = service.generatorSign(data);
        data.put("sign",sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/XFTNotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);
    }
}
