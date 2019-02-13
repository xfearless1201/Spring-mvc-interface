package payTest;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName XDPayTest
 * @Description 兄弟支付测试类
 * @author Hardy
 * @Date 2018年10月13日 下午2:53:12
 * @version 1.0.0
 */
public class XDPayTest {

    
    public static void main(String[] args) {
//        JSONObject data = new JSONObject();
//        data.put("merchantNo", "2000015");
//        data.put("secret", "4AEm9bQPaaj0TJ2ML5yQT0L4HZqQqy6t43L6jdMMMkrtvtbbH7");
//        data.put("notifyUrl", "http://www.baidu.com");
//        data.put("payUrl", "https://www.brotherpay.net/payapi/api");
//        System.err.println(data.toString());
        
//        String str = "amount=100.00&body=top-up&fronturl=http://localhost/&merchantno=2000015&notifyurl=http://www.baidu.com&orderno=xdbl12018101910264710264712324AEm9bQPaaj0TJ2ML5yQT0L4HZqQqy6t43L6jdMMMkrtvtbbH7";
//        
//        String sign = stringToMD5(str);
//        
//        String sourceSign = "387d7ce322259dff9719b1819c32ee17";
//        
//        if(sign.equalsIgnoreCase(sourceSign)){
//            System.err.println("签名成功");
//        }else{
//            System.err.println("签名失败!");
//        }
        
    }
    
    
    public static String stringToMD5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有这个md5算法！");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }
}
