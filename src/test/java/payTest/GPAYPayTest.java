package payTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cn.tianxia.pay.impl.GPAYPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import org.junit.Test;

import java.util.Map;

/**
 * @ClassName: GPAYPayTest
 * @Description: GPAY支付测试类
 * @Author: Zed
 * @Date: 2018-12-31 16:01
 * @Version:1.0.0
 **/

public class GPAYPayTest {
    @Test
    public void testCallbak() throws Exception{
        String data = " {\n" +
                "                    \"code\": 1,\n" +
                "                    \"data\": {\n" +
                "                        \"actualMoney\": 0,\n" +
                "                        \"aliName\": \"\",\n" +
                "                        \"aliUserId\": \"\",\n" +
                "                        \"bizCode\": \"12121\",\n" +
                "                        \"channel\": \"alipay\",\n" +
                "                        \"createdTime\": \"2018-12-05 14:10:25\",\n" +
                "                        \"deviceId\": \"\",\n" +
                "                        \"endtime\": null,\n" +
                "                        \"id\": 951,\n" +
                "                        \"matchId\": 1,\n" +
                "                        \"money\": 1,\n" +
                "                        \"notifyUrl\": \"http://localhost:8082/notify/order\",\n" +
                "                        \"orderId\": \"1X2X951XhgRuF\",\n" +
                "                        \"orderNo\": \"\",\n" +
                "                        \"payName\": \"\",\n" +
                "                        \"payType\": \"QR2\",\n" +
                "                        \"payUrl\": \"http://47.100.56.123:8082/pay.html?_s=web-otrher&orderId=1X2X951XhgRuF\",\n" +
                "                        \"status\": \"SUCCESS\",\n" +
                "                        \"terminalId\": 2,\n" +
                "                        \"updatedTime\": \"2018-12-05 14:10:25\"\n" +
                "                    },\n" +
                "                    \"message\": \"下单成功\",\n" +
                "                    \"sign\": \"Wn53ksrURL6hv+QAiB6s/W2nN8PdUjR19NcsDXunS+b+kAQZono4TzAVerHun6SyIjdiSK2j08arasEpe76u3h/czaTY7P6DvqUMqBuiS6lL8IODQUrU9Jk8OC5P7HlDJZ/vAv7n0K6wKvKiovS166YR2sKLwSWLE2Hbx6wVJ5w=\"\n" +
                "                }";
//        JSONObject jsonObject =  JSON.parseObject(data);
//        String datajson =   jsonObject.getString("data");
//        Map map =  JSON.parseObject(datajson);
//        String sign = jsonObject.getString("sign");
//        map.put("sign",sign);
//
//        GPAYPayServiceImpl gpayPayService = new GPAYPayServiceImpl(null);
//        String hhehe = gpayPayService.callback(map);
//        System.out.println(hhehe);
        String notifyUrl = "http://localhost:85/JJF/Notify/GPAYNotify.do";

        String response = HttpUtils.toPostJson(data, notifyUrl);



        System.err.println(response);

    }
}
