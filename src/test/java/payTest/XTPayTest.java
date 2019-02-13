package payTest;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName XTPayTest
 * @Description 信通支付测试类
 * @author Hardy
 * @Date 2018年10月5日 下午2:02:47
 * @version 1.0.0
 */
public class XTPayTest {

private static String encodingCharset = "UTF-8";
    
    /**
     * @param aValue
     * @param aKey
     * @return
     */
    public static String hmacSign(String aValue, String aKey) {
        byte k_ipad[] = new byte[64];
        byte k_opad[] = new byte[64];
        byte keyb[];
        byte value[];
        try {
            keyb = aKey.getBytes(encodingCharset);
            value = aValue.getBytes(encodingCharset);
        } catch (UnsupportedEncodingException e) {
            keyb = aKey.getBytes();
            value = aValue.getBytes();
        }

        Arrays.fill(k_ipad, keyb.length, 64, (byte) 54);
        Arrays.fill(k_opad, keyb.length, 64, (byte) 92);
        for (int i = 0; i < keyb.length; i++) {
            k_ipad[i] = (byte) (keyb[i] ^ 0x36);
            k_opad[i] = (byte) (keyb[i] ^ 0x5c);
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {

            return null;
        }
        md.update(k_ipad);
        md.update(value);
        byte dg[] = md.digest();
        md.reset();
        md.update(k_opad);
        md.update(dg, 0, 16);
        dg = md.digest();
        return toHex(dg);
    }

    public static String toHex(byte input[]) {
        if (input == null)
            return null;
        StringBuffer output = new StringBuffer(input.length * 2);
        for (int i = 0; i < input.length; i++) {
            int current = input[i] & 0xff;
            if (current < 16)
                output.append("0");
            output.append(Integer.toString(current, 16));
        }

        return output.toString();
    }

    /**
     * 
     * @param args
     * @param key
     * @return
     */
    public static String getHmac(String[] args, String key) {
        if (args == null || args.length == 0) {
            return (null);
        }
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            str.append(args[i]);
        }
        return (hmacSign(str.toString(), key));
    }

    /**
     * @param aValue
     * @return
     */
    public static String digest(String aValue) {
        aValue = aValue.trim();
        byte value[];
        try {
            value = aValue.getBytes(encodingCharset);
        } catch (UnsupportedEncodingException e) {
            value = aValue.getBytes();
        }
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        return toHex(md.digest(value));

    }
    
  public static void main(String[] args) throws NoSuchAlgorithmException {
      JSONObject data = new JSONObject();
      data.put("merId", "22");
      data.put("secret", "35de170fc7836ea645e1a7d7b307ff6e");
      data.put("notifyUrl", "http://www.baidu.com");
      data.put("payUrl", "https://www.xintonpay.com/hspay/api_node");
      System.err.println(data.toString());
  }
  
  @Test
  public void callbackTest() throws Exception{
      Map<String,String> data = new HashMap<String,String>();
      data.put("r3_Amt","100.00");
      data.put("r6_Order","XTbl1201810021925391925391047");
      data.put("r0_Cmd","Buy");
      data.put("ru_Trxtime","2018-10-05");
      data.put("r2_TrxId","O2018100514065898178722");
      data.put("rb_BankId","alipay");
      data.put("rp_PayDate","2018-10-05");
      data.put("rq_CardNo","");
      data.put("r7_Uid","");
      data.put("ro_BankOrderId","");
      data.put("r9_BType","2");
      data.put("r5_Pid","TOP-UP");
      data.put("hmac","b0f8d399bf74fe3813c35e77f74764cb");
      data.put("r1_Code","1");
      data.put("r8_MP","XR");
      data.put("r4_Cur","CNY");
      data.put("p1_MerId","22");
      
//      String regex = ".*\\d+.*";
//      
//      Map<String,String> bakmap = new HashMap<String,String>();
//      
//      Iterator<String> iterator = data.keySet().iterator();
//      while(iterator.hasNext()){
//          String key = iterator.next();
//          if(key.matches(regex)){
//              bakmap.put(key, data.get(key));
//          }
//      }
//      
//      System.err.println(bakmap.toString());
      
//      String notifyUrl = "http://localhost:85/JJF/Notify/XTNotify.do";
//      
//      String response = HttpUtils.toPostForm(data, notifyUrl);
//      
//      System.err.println("回调结果:"+response);
      
      System.err.println(JSONObject.fromObject(data).toString());
  }
}
