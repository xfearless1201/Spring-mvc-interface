package payTest;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.cn.tianxia.pay.impl.ABHPayServiceImpl;
import com.cn.tianxia.pay.utils.DESUtils;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.ParamsUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName ABHPayTest
 * @Description 阿里宝盒支付
 * @author Hardy
 * @Date 2018年10月14日 下午2:43:35
 * @version 1.0.0
 */
public class ABHPayTest {
    
    public static final String secret="8E8BA7AEFF20DFEDDD20FB178552F0D0";
    
    public static final String desKey="PR3tmPJqcH5RE0iGsW4qiN5VYtjX7sVU";
    
    public static final String notifyUrl="http://localhost:85/JJF/Notify/ABHNotify.do";

    public static void main(String[] args) {
//        JSONObject data = new JSONObject();
//        data.put("branchId","1008");//机构编号  是   1001
//        data.put("merCode","181009140141533");//商户号    是   171107105912001
//        data.put("settType","T0");//结算类型  是   T1：T+1结算 T0：T+0结算
//        data.put("notifyUrl","");//页面通知地址   否   页面通知地址 h5支付必传
//        data.put("payUrl", "http://47.98.42.128:9001/psgAgent/scanPay");//支付地址
//        data.put("secret", "8E8BA7AEFF20DFEDDD20FB178552F0D0");
//        data.put("desKey", "PR3tmPJqcH5RE0iGsW4qiN5VYtjX7sVU");//加密key
//        System.err.println(data.toString());
//        double amt = 100.00;
//        String amount = new DecimalFormat("##").format(amt*100-1);//交易1金额 是   分为单位
//        System.err.println(amount);
        String refurl = "http://192.168.0.175:9000/user/getUserInfo";
        
        String referer = refurl.split("/")[2];
        System.err.println(referer);
        String damain = "192.168.0.175:9000";
        if(referer.indexOf(damain) > 0){
            System.err.println(true);
        }else{
            System.err.println(false);
        }
    }
    
    @Test
    public void callbackTest() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("branchId","1008");//机构编号  是   1001
        data.put("commodityName","TOP-UP");//商户名称 否   商户
        data.put("returnCount","");//回调次数   否   2
        data.put("returnUrl","");//回调地址 否   http://127.0.0.1:8080/test
        data.put("orderId","ABHbl1201810170910240");//订单号    是   20位长度唯一订单标识
        data.put("flowId","1201810170910240");//交易流水号   是   
        data.put("transDate","20181017");//交易日期 是   yyyyMMdd
        data.put("transTime","101027");//交易时间 是   hhmmss
        data.put("transAmt","10000");//交易金额  是   分为单位
        data.put("transCode","00");//交易状态 是   00表示成功FF失败其他处理中
//        data.put("transMsg",URLEncoder.encode("成功", "UTF-8"));//交易描述  是   成功
        ABHPayServiceImpl payServiceImpl = new ABHPayServiceImpl(null);
        String sign = payServiceImpl.generatorSign(data);
        data.put("signature",sign);//验签字段 是   MD5加密
        String reqParams = JSONObject.fromObject(data).toString();
//        String reqParams = ParamsUtils.formatMapToJson(data).trim();
        reqParams = URLEncoder.encode(DESUtils.encrypt(reqParams.trim(), desKey),"UTF-8");
        Map<String,String> pmap = new HashMap<>();
        pmap.put("data", reqParams);
        String response = HttpUtils.toPostForm(pmap, notifyUrl);
        
        System.err.println(response);
    }
}

