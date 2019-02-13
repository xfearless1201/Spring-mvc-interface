package payTest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.cn.tianxia.pay.utils.HttpUtils;

/**
 * 
 * @ClassName SFPayTest
 * @Description 速付回调
 * @author Hardy
 * @Date 2018年10月2日 下午9:11:09
 * @version 1.0.0
 */
public class SFPayTest {
    
    @Test
    public void callbackTest() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("trade_no","838647775630181039");
        data.put("order_amount","500.00");
        data.put("order_time","2018-10-04 18:09:36");
        data.put("notify_type","back_notify");
        data.put("trade_time","2018-10-04 18:09:36");
        data.put("merchant_code","13749232");
        data.put("trade_status","success");
        data.put("order_no","SFwwc201810041809351809356311");
        data.put("sign","380e82b9ad9deeec809be80273aa7384");
        
        String notifyUrl = "http://localhost:85/JJF/Notify/SFNotify.do";
        
        String response = HttpUtils.toPostForm(data, notifyUrl);
        
        System.err.println(response);
    }

}
