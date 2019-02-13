package payTest;

import com.cn.tianxia.pay.impl.LAOMPayServiceImpl;
import com.cn.tianxia.pay.impl.TT2PayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: LAOMPayTest
 * @Description: 老马支付测试类
 * @Author: Zed
 * @Date: 2018-12-21 11:04
 * @Version:1.0.0
 **/

public class LAOMPayTest {
    @Test
    public void testCallbak() throws Exception{
        HashMap<String, String> params = new HashMap<>();
        params.put("account_id", "10000");// 商户ID
        params.put("content_type", "json");// 网页类型
        params.put("thoroughfare", "service_auto");// 支付通道
        params.put("out_trade_no", "LAOMbl1201812211054401054403248");// 订单信息
        params.put("trade_no", "sbzflaomama123");// 订单信息
        params.put("robin", "2");// 轮训状态 //2开启1关闭
        params.put("amount", "100.00");// 支付金额
        params.put("type", "2");// 支付类型 //1为微信，2为支付宝
        params.put("keyId", "");// 设备KEY 轮询无需填写
        params.put("status", "success");// 设备KEY 轮询无需填写
        params.put("account_key", "B96A3751178FA7");// 设备KEY 轮询无需填写
        LAOMPayServiceImpl service = new LAOMPayServiceImpl(null);
        String sign = service.generatorSign(params);
        params.put("sign", sign);// 签名算法


        String notifyUrl = "http://localhost:85/JJF/Notify/LAOMNotify.do";

        String response = HttpUtils.toPostForm(params, notifyUrl);

        System.err.println(response);

    }
}
