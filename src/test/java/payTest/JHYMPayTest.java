package payTest;

import com.cn.tianxia.pay.impl.JHYMPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: zed
 * @Date: 2019/1/29 11:17
 * @Description: 聚合银码支付回调测试类
 */
public class JHYMPayTest {
    @Test
    public void testCallbak() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("pid","1028");//
        data.put("trade_no","20160806151343349021");//	是	String	20160806151343349021	聚合支付订单号
        data.put("out_trade_no","bl1jhym201901281532091532099514");//	是	String	20160806151343349	商户系统内部的订单号
        data.put("type","alipay");//	是	String	alipay	alipay:支付宝,wxpay:微信支付
        data.put("name","sss");//	是	String	VIP会员
        data.put("money","329.00");//	是	String	1.00
        data.put("trade_status","TRADE_SUCCESS");//	是	String	TRADE_SUCCESS
        data.put("sign_type","MD5");//	是

        Map<String,String> config = new HashMap<>();
        config.put("key","3lL49lSTgpV4Az1RtL9RXLLa3lPp3Xkv");
        JHYMPayServiceImpl jhymPayService = new JHYMPayServiceImpl(config);
        String sign = jhymPayService.generatorSign(data);
        data.put("sign",sign);

        String notifyUrl = "http://localhost:85/JJF/Notify/JHYMNotify.do";

        String response = HttpUtils.toPostForm(data, notifyUrl);

        System.err.println(response);
    }

}
