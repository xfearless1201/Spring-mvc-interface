package payTest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.cn.tianxia.pay.utils.HttpUtils;

/**
 * 
 * @ClassName TTZFPayTest
 * @Description 天天支付
 * @author Hardy
 * @Date 2018年10月30日 下午4:56:44
 * @version 1.0.0
 */
public class TTZFPayTest {

    @Test
    public void payTest() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("payKey","a4a9777e1dd84257993c4e210cea96a9");
        data.put("orderPrice","100.00");
        data.put("outTradeNo","2018103015120042625805");
        data.put("productType","20000203");
        data.put("orderTime","20181030151201");
        data.put("productName","pay");
        data.put("orderIp","124.160.214.236");
        data.put("returnUrl","http%3A%2F%2F47.96.72.239%2Fcallback%2FP88882018092010001491");
        data.put("notifyUrl","http%3A%2F%2F47.96.72.239%2Fcallback%2FP88882018092010001491");
        data.put("remark","2018103015120042625805");
        data.put("sign","FE397DA157FDE7B0203B1FD328960CD7");

        String payUrl = "https://g32djwox6ldsk.dahuangf.cn/cnpPay/initPay";
        
        String response = HttpUtils.toPostForm(data, payUrl);
        
        System.err.println(response);
    }
}
