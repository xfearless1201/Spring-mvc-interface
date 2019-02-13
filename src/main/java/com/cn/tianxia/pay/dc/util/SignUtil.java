package com.cn.tianxia.pay.dc.util;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64EncoderStream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;

/**
 * @author wendy
 * @since 2017/4/18
 */
public class SignUtil {

    private static final String HMAC_SHA1 = "HmacSHA1";

    private static final String UTF8 = "UTF8";

    /**
     * HMAC-SHA1加密字节数组生成BASE64字符串
     *
     * @author sam.xie
     * @date 2014年12月25日 下午2:36:33
     * @param hmac
     * @return
     */
    public static String getSign(byte[] hmac) {
        String sign = new String(BASE64EncoderStream.encode(hmac));
        System.out.println("sign:" + sign);
        return sign;
    }

    /**
     * 通过源串和openKey进行HmacSHA1
     *
     * @author sam.xie
     * @date 2014年12月25日 上午11:51:35
     * @param source
     *            源串
     * @param openKey
     *            openKey
     * @return
     * @throws UnsupportedEncodingException
     */
    public static byte[] getHmacSHA1(String source, String openKey) {
        try {
            byte[] data = source.getBytes(UTF8);
            byte[] key = (openKey + "&").getBytes(UTF8);
            SecretKeySpec sigKey = new SecretKeySpec(key, HMAC_SHA1);
            Mac mac;
            mac = Mac.getInstance(HMAC_SHA1);
            mac.init(sigKey);
            return mac.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
