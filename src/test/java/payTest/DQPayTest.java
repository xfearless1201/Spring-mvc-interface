package payTest;

import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.cn.tianxia.pay.daqiang.util.SignUtils;
import com.cn.tianxia.pay.impl.DQPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;

/**
 * 
 * @ClassName DQPayTest
 * @Description 大强支付回调
 * @author Hardy
 * @Date 2018年9月29日 下午9:41:40
 * @version 1.0.0
 */
public class DQPayTest {
    
    @Test
    public void callbackTest() throws Exception{
//        Map<String,String> map = new HashMap<>();
//        map.put("goodsInfo","TOP-UP");//商户uid
//        map.put("md5Key","2FiXw2IFzQLgNZygCAUHTyhQNieUM2W7");//通知回调网址
//        map.put("merchantNo","2018050005");//商品名称
//        DQPayServiceImpl payService = new DQPayServiceImpl(map);
        Map<String,String> data = new HashMap<>();
        data.put("memberOrderId", "DQBL1201809151901361901368797");
        data.put("orderId", "1180515144735e57c538166780000");
        data.put("orderAmount", "100");
        data.put("createTime", "20180515144735");
        data.put("merchantNo", "2018050005");
        data.put("stateCode", "SUCCESS");
//        data.put("completeTime", "");
        data.put("key", "2FiXw2IFzQLgNZygCAUHTyhQNieUM2W7");
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
            if(StringUtils.isBlank(val) || key.equals("sign")) continue;
            
            sb.append("&").append(key).append("=").append(val);
        }
        String signStr = sb.toString().replaceFirst("&", "");
        String sign = SignUtils.md5(signStr);
        data.put("sign", sign);
        data.remove("key");
        String tagUrl = "http://localhost:85/JJF/Notify/DQNotify.do";
        String response = HttpUtils.toPostForm(data, tagUrl);
        System.err.println("回调返回结果:"+response);
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String src = "createTime=2018-05-15 14:47:35&key=wTvz84AuexEh68yhLCpMGYwF6Zk79RJu&memberOrderId=DQyhh201809292125512125519940&merchantNo=2018080162&orderAmount=100&orderId=1180515144735e57c538166780000&stateCode=SUCCESS";
    
        String sign = SignUtils.md5(src);
        
        String srcc = "createTime=2018-05-15 14:47:35&key=wTvz84AuexEh68yhLCpMGYwF6Zk79RJu&memberOrderId=DQyhh201809292125512125519940&merchantNo=2018080162&orderAmount=100&orderId=1180515144735e57c538166780000&stateCode=SUCCESS";
        
        String sigg = SignUtils.md5(srcc);
        
        if(sign.equals(sigg)){
            System.err.println("一样");
        }
        System.err.println("不一样");
    }
}
