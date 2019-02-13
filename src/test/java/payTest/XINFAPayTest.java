package payTest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * 
 * @ClassName XINFAPayTest
 * @Description 鑫发支付测试类
 * @author Hardy
 * @Date 2018年9月30日 上午10:50:37
 * @version 1.0.0
 */
public class XINFAPayTest {
    
    @Test
    public void callbackTest(){
        String secret = "4124AD6CB509EBC0346B1C353F5F90B2";
        Map<String,String> data = new HashMap<>();
        data.put("merchNo","XF201808250248");//商户号
        data.put("payType","JD");//支付方式，参考附录8.1
        data.put("orderNo","XINFA20180930104925111pHbu");//订单号
        data.put("amount","10000");//金额（单位：分）
        data.put("goodsName","TXZF");//商品名称
        data.put("payStateCode","00");//支付状态，00表示成功
        data.put("payDate","20180930114925");//支付时间，格式：yyyyMMddHHmmss
    }

}
