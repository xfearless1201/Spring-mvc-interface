package payTest;

import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;

public class NOMQPayTest {

    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("payUrl", "http://www.hotsoo.com/Pay_Index.html");
        data.put("memberId", "10002");
        data.put("md5Key", "t4ig5acnpx4fet4zapshjacjd9o4bhbi");
        data.put("notifyUrl", "Http://www.baidu.com");
        System.err.println(data.toString());
    }
    
    @Test
    public void callbackTest() throws Exception{
        String secert = "hlpwdtt9dq9tz7uvliw7bfnq9rqj2ivt";
        
        Map<String,String> data = new HashMap<String,String>();
        data.put("memberid", "10064");
        data.put("orderid", "NOMQbl120181005203435");
        data.put("amount", "100.00");
        data.put("transaction_id", "201810051341201447");
        data.put("datetime", "2018-10-05 13:41:20");
        data.put("returncode", "00");
        data.put("attach", "attach");
        
        Map<String,String> treemap = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        treemap.putAll(data);
        
        StringBuffer sb = new StringBuffer();
        Iterator<String> iterator = treemap.keySet().iterator();
        while(iterator.hasNext()){
            String key = iterator.next();
            String val = treemap.get(key);
            if(StringUtils.isBlank(val) || key.equalsIgnoreCase("attach") || key.equalsIgnoreCase("sign")) continue;
            
            sb.append(key).append("=").append(val).append("&");
        }
        sb.append("key=").append(secert);
        String signStr = sb.toString();
        
        String sign = MD5Utils.md5toUpCase_32Bit(signStr);
        data.put("sign", sign);
        
        String notifyUrl = "http://localhost:8087/JJF/Notify/NOMQNotify.do";
        
        String response = HttpUtils.toPostForm(data, notifyUrl);
        
        System.err.println("回调结果:"+response);
        
    }
}
