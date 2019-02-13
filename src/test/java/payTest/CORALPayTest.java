package payTest;

import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.cn.tianxia.pay.impl.CORALPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;

/**
 * 
 * @ClassName CORALPayTest
 * @Description 珊瑚支付回调测试类
 * @author Hardy
 * @Date 2018年9月29日 下午9:05:40
 * @version 1.0.0
 */
public class CORALPayTest {
    
    @Test
    public void callbackTest() throws Exception{
        Map<String,String> map = new HashMap<String,String>();
        map.put("pay_url","https://pay.hnshunqi.com/Pay_Index.html");//商户uid
        map.put("pay_memberid","10768");//通知回调网址
        map.put("pay_notifyurl","http://txw.tx8899.com/YHH/Notify/CORALNotify.do");//商品名称
        map.put("key", "o5hm6wnx17kgu1jtyvsbsor4p1b3nq29");
        CORALPayServiceImpl payService = new CORALPayServiceImpl(map);
        
        Map<String,String> data = new HashMap<String,String>();
        data.put("orderid", "20180905200240897303");
        data.put("transaction_id", "2018092910523110456321784");
        data.put("amount", "100");
        data.put("datetime", "2018-09-05 21:02:52");
        data.put("memberid", "10768");
        data.put("returncode", "00");
        data.put("attach", "");
        
        Map<String,String> treemap = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                // TODO Auto-generated method stub
                return o1.compareTo(o2);
            }
        });
        
        treemap.putAll(data);
        StringBuffer sb = new StringBuffer();
        Iterator<String> iterator = treemap.keySet().iterator();
        while(iterator.hasNext()){
            String key = iterator.next();
            String val = treemap.get(key);
            if(StringUtils.isBlank(val) || key.equals("key")) continue;
            
            sb.append(key).append("=").append(val).append("&");
        }
        
        sb.append("key").append("=").append("o5hm6wnx17kgu1jtyvsbsor4p1b3nq29");
        
        String signStr = sb.toString();
        
        String sign = MD5Utils.md5toUpCase_32Bit(signStr);
        data.put("sign", sign);
        String tagUrl = "http://localhost:85/JJF/Notify/CORALNotify.do";
        String response = HttpUtils.post(data,tagUrl);
        System.err.println("回调返回结果:"+response);
    }

}
