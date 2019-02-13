package payTest;

import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.cn.tianxia.pay.utils.RsaUtils;


/**
 * 证书操作类
 *

 */
public class MerchantX509Cert {
    /**
     * Java密钥库(Java Key Store，JKS)KEY_STORE
     */
    public static final String KEY_STORE = "PKCS12";

    public static final String X509 = "X.509";


    /**
     * 签名
     * @param signSrc
     * @param keyStorePath
     * @param alias
     * @param keyStorePassword
     * @param aliasPassword
     * @return String
     * @throws Exception
     */
    public static String sign(String signSrc, String keyStorePath, String alias, String keyStorePassword, String aliasPassword) throws Exception {
        // 获得证书
        X509Certificate x509Certificate = (X509Certificate)getCertificate(keyStorePath, keyStorePassword, alias);
        // 取得私钥
        PrivateKey privateKey = getPrivateKey(keyStorePath, keyStorePassword, alias, aliasPassword);
        // 构建签名
        Signature signature = Signature.getInstance(x509Certificate.getSigAlgName());
        signature.initSign(privateKey);
        signature.update(signSrc.getBytes("UTF-8"));
        return new Hex().encodeHexString(signature.sign());
    }

    /**
     * 验证签名
     *
     * @param signSrc
     * @param sign
     * @param certificatePath
     * @return boolean
     * @throws Exception
     */
    public static boolean verifySign(String signSrc, String sign, String certificatePath) throws Exception {
        // 获得证书
        X509Certificate x509Certificate = (X509Certificate)getCertificate(certificatePath);
        // 获得公钥
        PublicKey publicKey = x509Certificate.getPublicKey();
        
        System.err.println(new Hex().encodeHexString(publicKey.getEncoded()));
        
        // 构建签名
        Signature signature = Signature.getInstance(x509Certificate.getSigAlgName());
        signature.initVerify(publicKey);
        signature.update(signSrc.getBytes("UTF-8"));
        byte[] signByte = new Hex().decode(sign.getBytes());

        boolean verifyStatus = false;
        verifyStatus = signature.verify(signByte);
        return verifyStatus;

    }

    public static boolean verify(String signSrc, String sign, String pubKey) throws Exception {
        try {
            byte[] keyBytes = hexString2ByteArr(pubKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFac = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFac.generatePublic(keySpec);
            // 构建签名
            Signature signature = Signature.getInstance("RSA/ECB/PKCS1Padding");
            signature.initVerify(publicKey);
            signature.update(signSrc.getBytes("UTF-8"));
            byte[] signByte = new Hex().decode(sign.getBytes());
            boolean verifyStatus = false;
            verifyStatus = signature.verify(signByte);
            return verifyStatus;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    public static String byteArr2HexString(byte[] bytearr) {
        if (bytearr == null) {
            return "null";
        }
        StringBuffer sb = new StringBuffer();

        for (int k = 0; k < bytearr.length; k++) {
            if ((bytearr[k] & 0xFF) < 16) {
                sb.append("0");
            }
            sb.append(Integer.toString(bytearr[k] & 0xFF, 16));
        }
        return sb.toString();
    }

    public static byte[] hexString2ByteArr(String hexString) {
        if ((hexString == null) || (hexString.length() % 2 != 0)) {
            return new byte[0];
        }

        byte[] dest = new byte[hexString.length() / 2];

        for (int i = 0; i < dest.length; i++) {
            String val = hexString.substring(2 * i, 2 * i + 2);
            dest[i] = ((byte) Integer.parseInt(val, 16));
        }
        return dest;
    }
    
    
    /**
     * 由 KeyStore获得私钥
     *
     * @param keyStorePath
     * @param keyStorePassword
     * @param alias
     * @param aliasPassword
     * @return
     * @throws Exception
     */
    private static PrivateKey getPrivateKey(String keyStorePath, String keyStorePassword, String alias, String aliasPassword) throws Exception {
        KeyStore ks = getKeyStore(keyStorePath, keyStorePassword);
        PrivateKey key = (PrivateKey)ks.getKey(alias, aliasPassword.toCharArray());
        return key;
    }

    /**
     * 由 Certificate获得公钥
     *
     * @param certificatePath
     * @return
     * @throws Exception
     */
    private static PublicKey getPublicKey(String certificatePath) throws Exception {
        Certificate certificate = getCertificate(certificatePath);
        PublicKey key = certificate.getPublicKey();
        return key;
    }

    /**
     * 获得Certificate
     *
     * @param certificatePath
     * @return
     * @throws Exception
     */
    private static Certificate getCertificate(String certificatePath) throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance(X509);
        FileInputStream in = new FileInputStream(certificatePath);

        Certificate certificate = certificateFactory.generateCertificate(in);
        in.close();

        return certificate;
    }

    /**
     * 获得Certificate
     *
     * @param keyStorePath
     * @param keyStorePassword
     * @param alias
     * @return
     * @throws Exception
     */
    private static Certificate getCertificate(String keyStorePath, String keyStorePassword, String alias) throws Exception {
        KeyStore ks = getKeyStore(keyStorePath, keyStorePassword);
        Certificate certificate = ks.getCertificate(alias);

        return certificate;
    }

    /**
     * 获得KeyStore
     *
     * @param keyStorePath
     * @param password
     * @return
     * @throws Exception
     */
    private static KeyStore getKeyStore(String keyStorePath, String password) throws Exception {
        FileInputStream is = new FileInputStream(keyStorePath);
        KeyStore ks = KeyStore.getInstance(KEY_STORE);
        ks.load(is, password.toCharArray());
        is.close();
        return ks;
    }


    public static void main(String[] args) throws DecoderException, Exception {

       String sign = "9f2951eaf9332c2ad8ed93b91a43b447c3e04218a628e0f395888384728250086aa4f3796fa7050c50ca43963ebfa65bcfbb9435306f4a83b9f589098d8c2d1320d8ac372301eed8be93fc3f17b6dc3d8b66e7514f315eade9897ed587d07cee13b9c0e242febd3caa810cd29fe97705afc2fc66b2649ba8ec0931f7802fead678b2492df268b72d5d40d2b01351653f046ae1c943f0ee1d0a8dcb9061b1605ab410325471cbdcc1a2840a0bbb48dc09e2298e8d8a18da42d25bca8aaa85894370c9321f7c27ef1d768d4ef31d71eb24ffe19eb41e72eb1a83e3852083c9f0e5ec2f403e34e21eb7b9de37e4e119fc6fe2a385a5f11d21e4a37e879f40fdb9bc"; 
       
       String pubKey = "30820122300d06092a864886f70d01010105000382010f003082010a0282010100b7369bc8b90d8205720006a0e83aa8ca84f81ad690543f5f51dbc9c7bc4581cc7972ffc2df0871a03ee3985f1af08a989bca6e5b586ffe0129a4e520dc101efd6c1af62a73b27081e8f66eb86357169b546261e1783236fc035378c3a9128f3da336ce32f8d9a0cfa85c363c50290aed5ba5ad47862ba2139d18a45a124a0e3592bc425a78d4c492c5594d7b0707376c8063cc35a5c3a52ac04a087db1d9754b5f4fb6085b9de866a8a8abeb8a48a72b45bcd088f59faf9dbe883536aab088fbff0ed108e3c1127dec8285324b6b88e27f09b54d0ef0e15bad125607359e676a816fdeef5b417e4a5d8848676e03ce00e09ed4e7c8bc3a0ce6a37cf7edbb97f50203010001";
       
       StringBuffer sb = new StringBuffer();
       sb.append("20181009-19041001-1151131230").append("20").append("20.00").append("0");
       String signStr = sb.toString();    
       
       boolean flag = RsaUtils.verify(signStr, sign, pubKey);

       System.err.println("验签结果:"+flag);

    }


}
