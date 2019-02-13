package payTest;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName ZYPayTest
 * @Description 中亿支付测试类
 * @author Hardy
 * @Date 2018年11月27日 上午10:24:32
 * @version 1.0.0
 */
public class ZYPayTest {

    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("appid", "10145");
        data.put("notifyUrl", "http://localhost:8087/JJF/Notify/ZYNotify.do");
        data.put("payUrl", "http://请查阅用户中心下的网关地址/Pay/jucai.php");
        data.put("sercet", "xj00k9KPPZKXWJ4GbGgx9ERpK4GF9bp0");
    }
    
    @Test
    public void callbackTest() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("out_trade_no", "2108");
        data.put("money", "100.00");
        data.put("ddh", "20160888888880010888888888");
        data.put("lb", "1");
        data.put("pay_time","2018/3/4 22:38:18");
        String signStr = "10145xj00k9KPPZKXWJ4GbGgx9ERpK4GF9bp0";
        String sign = MD5Utils.md5toUpCase_32Bit(signStr);
        data.put("sign", sign);
        
        String notifyUrl = "http://localhost:8087/JJF/Notify/ZYNotify.do";
        
        String response = HttpUtils.toPostForm(data, notifyUrl);
    }
}
