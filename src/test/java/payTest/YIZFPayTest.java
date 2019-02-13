package payTest;

import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import com.cn.tianxia.pay.utils.MD5Utils;

/**
 * 
 * @ClassName YIZFPayTest
 * @Description 易支付回调
 * @author Hardy
 * @Date 2018年10月10日 下午4:27:50
 * @version 1.0.0
 */
public class YIZFPayTest {
    
    @Test
    public void callbackTest() throws NoSuchAlgorithmException{
        
        String sourceSign = "0d1175e6a6a3ba0a8889f2e59892a413";

        StringBuffer sb = new StringBuffer();
        sb.append("YIZFtyc201810101634191634197159").append("50.00").append("b8109dd06a12278ed54c3e877167aec761b96b6b");
        String signStr = sb.toString();
        String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
        System.err.println(sign);
        if(sourceSign.equals(sign)){
            System.err.println("验签通过");
        }else{
            System.err.println("验签失败");
        }
    }

}
