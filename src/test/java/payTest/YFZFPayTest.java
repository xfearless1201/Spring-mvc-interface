package payTest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.cn.tianxia.pay.impl.YFZFPayServiceImpl;

import net.sf.json.JSONObject;

public class YFZFPayTest {

    @Test
    public void callbacktest(){
        Map<String,String> data = new HashMap<>();
        data.put("trxNo", "77772018120410027887");
        data.put("respCode", "0000");
        data.put("orderDesc", "订单商品内容描述");
        data.put("status", "SUCCESS");
        data.put("respDesc", "交易成功");
        data.put("transInfo", "");
        data.put("transAmt", "1000.00");
        data.put("sign", "2BA34A37F7DFD77EA99DEA6BBD586002");
        data.put("serialNo", "1812041720228671591");
        data.put("transDate", "20181204");
        data.put("orderNo", "YFZFyhh201812041720221720224557");
        data.put("transId", "SMARTCLOUD_ALIPAY_TRANSFER_PAY");
        data.put("transTime", "20181204172023");
        data.put("merKey", "49dd33a1714a49ddb0d0d416c5333d09");
        JSONObject json = new JSONObject();
        json.put("realAmount", "1000.0");
        json.put("transAmt", "1000.0");
        data.put("transInfo", json.toString());
        
        Map<String,String> config = new HashMap<String,String>();
        config.put("payUrl","");
        config.put("merKey","49dd33a1714a49ddb0d0d416c5333d09");
        config.put("merNo","88882018112710001125");
        config.put("paySecret","b699634c86cf4324a8f0819b1f4f25e4");
        config.put("publicKey","MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDkLTj9wZDP4aKwJ1yePL4");
        config.put("notifyUrl","");
        YFZFPayServiceImpl payServiceImpl = new YFZFPayServiceImpl(config);
        
        payServiceImpl.callback(data);
    }
}
