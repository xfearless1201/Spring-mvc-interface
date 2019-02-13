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

/**
 * 
 * @ClassName HYFZFPayTest
 * @Description 汇银付支付
 * @author Hardy
 * @Date 2018年9月29日 下午5:38:05
 * @version 1.0.0
 */
public class HYFZFPayTest {
    
    /**
     * 
     * @throws Exception 
     * @Description 回调测试类
     */
    @Test
    public void callbackTest() throws Exception{
        
        String secret = "4D103FE862189A77DEE701159202ECEDE14A68686ED9AEF3";
        
        Map<String,String> map = new HashMap<>();
        map.put("merchant_code","M0007");//商户号
        map.put("key","4D103FE862189A77DEE701159202ECEDE14A68686ED9AEF3");//商户唯一订单号
        map.put("payUrl","http://pay.zuowawa.com/Main/Order");//支付金额
        map.put("notify_url","http://txw.tx8899.com/AMJ/Notify/HYFZFNotify.do");//回传参数        
        
        Map<String,String> data = new HashMap<>();
        data.put("merchant_code","M0007");//商户号
        data.put("order_no","HYZFbl120180727145604145604158");//商户唯一订单号
        data.put("pay_amount","100");//支付金额
        data.put("return_params","TOP-UP");//回传参数
        data.put("trade_no","20180929124552");//支付平台订单号
        data.put("trade_time","2018-09-29 12:45:52");//支付平台订单 时间   
        data.put("trade_status","1");//交易状态
        data.put("notify_type","back_notify");//通知类型
        String signStr = getReqParams(data)+"key="+secret;
        String sign = MD5Utils.md5toUpCase_32Bit(signStr);
        data.put("sign", sign);
        String reqParams = getReqParams(data);
        String tagUrl = "http://localhost:85/JJF/Notify/HYFZFNotify.do";
        String response = HttpUtils.toPostForm(reqParams, tagUrl);
        System.err.println(response);
    }
    
    private String getReqParams(Map<String,String> data){
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
            if(StringUtils.isBlank(val)) continue;
            sb.append(key).append("=").append(val).append("&");
        }
        return sb.toString();
    }

}
