package payTest;

import org.junit.Test;
import sun.misc.BASE64Encoder;

import java.io.FileInputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * 
 * @ClassName ReadPFX
 * @Description 获取RSA公钥私钥字符串
 * @author Hardy
 * @Date 2018年8月25日 上午10:27:57
 * @version 1.0.0
 */


public class ReadPFX {
    
	@Test
	public  void testPayCallBack(){
		//验证公钥私钥是否为一对儿
		String priKey="308204bd020100300d06092a864886f70d0101010500048204a7308204a30201000282010100c5c74f574c8c23a46aca8278bf0ce05369fbec7de376304c4fdc59cd2a658433c79192de40e52c392ff084d7cffe27730fdfc9ebd4e829c160a1c953555b04998fa685e9c553d6fb19645d993fe469cb1b7bb2b6bd7b052ca10447eb33801af4c3195e2afaf3e4fdfc87ec0bc4206b7b28c148e783a9192f73114bbb88d998e32f00e6350c966451c7cbbfb51923da9bf2ba8d3114a1329c9cf440b381fa6048e8caf40172eec7ae1887313497d9d4b867ac251d08027977ba0eb5987ce5c0107490cfe032798581a2ee1a24b1e6f9068080bd76b7023c14a10dd6b79d504c5377c066a14f766ceeda8b4244ec250dbfee0f062019651800af804e3a5672ac650203010001028201001c7c334a30853290f8ac881419f821cb01c5bdeef7b6cb0802ac8ae688c7ac3dc4ed8b88a5321ca773e7214aeff2fbfd8f78bcdd5181ca99c67026cdf0ca362463d329105d5250f1435bf83b1d3fa9990e3147b1182342c085c45713620c7e5568501c06f6b3eff440cdd6ca6f09f850f9b2b4ec5b431c947fef2ac82cd2a2393d1fd3831c71506c3ac140abef6f5681ba90a73ab076b7f8633b9213ee83acc342d9556dd1d62099ad44a0fcfdf2c51b5d05951a5f5c7206ec6209e171d92cea3f94bc705b7a781347dbde0a0a56eb91d59684b7304570f288fe01e77efdea8983515e02aa1296cea993f7b576b2f6d352b38275d8e9847cff9027b923975a1502818100f968c3bdc11358b36ae5ddb9440f572573090f9af70c314cb426f408247fd494e725015346f029e47f847514bf9c6b50afa9e06d45c8076352983b365df9652bb1703fda38084a77a866f6325f95025e4ee68f26cf69af2fed7393893d5d20390a3c26be3de4bb227e00fdf70c8d5260c7f443e2880720ffa6d7e89e3f4dbb5702818100cb01447a635715c640fa13a35bebc06041d88d1731f763e55edcf0afcc0963218f8f1aa73fa8bcdfc271ae99ddb992926e82d227532bae556e30a20c885adac2935ddb4f6602cdcce02969458c170edb269bc5d915edf33936bf5e127144f4d8562b10aae5d0b3791685bad04339bddbebb07713bc81eac90db9b15334d43ca302818000ae63927a1b866b7f083fb1c8d8cde1214e4e079b532233566ea0ce00e3847e1f9f7f406dfa2bf31ed20acbd2b627a8e1c1f205fa61d07b59f7667a0089b4d808b508c79ccd90fe80ac3f51e5e6ef49b8a7728ea59461511d31ea9e3e57a9c5de1c49f2cd1279dc5f41bb6fd84dee82ab2924a252c2ca394888606f4636fd23028181009c57e08e457da736658a739f6746bc8525910ab06779f90488b7ade22648c080f3582a15ceb03cae33c96bc7f27c5e06ca7794345718980e116c011197de29069b8a384200f57ddc2073a430bcc0715f946f79c343e15b659fe56824d9f95a7c56789a4093f7d49118286c04c3117048344f5695eba33ed812eb054768b35ff302818062794e51803cf0a4bf91fb6c5f4306d8a34c451946024065082fa5f8bc16508c770dde6c1009972dcddbd18f14b66c3f0da8b19834a626c318d443afc16fe06059f06f980ee2a521e634bd52258202b296d2cfa3733fa5b2dbd95a91f59417ad94def379092ef6dfeb68fbc23aafb1055eea8067c48e7bd133f94a12432f27b3";
		String pubKey="30820122300d06092a864886f70d01010105000382010f003082010a0282010100c5c74f574c8c23a46aca8278bf0ce05369fbec7de376304c4fdc59cd2a658433c79192de40e52c392ff084d7cffe27730fdfc9ebd4e829c160a1c953555b04998fa685e9c553d6fb19645d993fe469cb1b7bb2b6bd7b052ca10447eb33801af4c3195e2afaf3e4fdfc87ec0bc4206b7b28c148e783a9192f73114bbb88d998e32f00e6350c966451c7cbbfb51923da9bf2ba8d3114a1329c9cf440b381fa6048e8caf40172eec7ae1887313497d9d4b867ac251d08027977ba0eb5987ce5c0107490cfe032798581a2ee1a24b1e6f9068080bd76b7023c14a10dd6b79d504c5377c066a14f766ceeda8b4244ec250dbfee0f062019651800af804e3a5672ac650203010001";
		String result = checkPriKeyAndPubKey(priKey,pubKey);
		System.err.println(result);


		String v_oid = "20180824-19041001-1533089011";
		String v_pstatus = "20";
		String v_amount = "100";
		String v_moneytype = "1";
		String v_count = "1";
		;		HashMap<String,String> m =new HashMap<String, String>();
		m.put("v_count",v_count);
		m.put("v_oid",v_oid);
		m.put("v_pmode","cmb");   // 1 - 支付完成；
		m.put("v_pstatus",v_pstatus);   // 支付成功（当 v_pstatus=1 时）
		m.put("v_pstring","1");
		m.put("v_amount",v_amount);
		m.put("v_moneytype",v_moneytype);
		m.put("v_mac","");
		m.put("v_md5money","");


		String strorigine = v_oid+v_pstatus+v_amount+v_moneytype;
		String sign = sign(strorigine, priKey);
		m.put("v_sign",sign);
		HttpUtils.doPost("http://localhost:8087/JJF/Notify/SXYNotify.do",m);
	}

	@Test
	public void readPublicKeyAndPrivateKey(){
		//		readPrivateKey("D:\\key\\联行私钥.pfx", "12345678");
		//		readPrivateKey("D:\\key\\商户私钥_读取密码12345678.pfx", "12345678");
		//获取商户公钥
		//		readPubliKey("F:\\随乐-金士\\对接demo和文档\\server_cert.cer");
	}


	public static void readPrivateKey(String strPfx, String strPassword) throws Exception {
		System.out.println(strPfx);
		KeyStore ks = KeyStore.getInstance("PKCS12");
		FileInputStream fis = new FileInputStream(strPfx);
		// If the keystore password is empty(""), then we have to set
		// to null, otherwise it won't work!!!
		char[] nPassword = null;
		if ((strPassword == null) || strPassword.trim().equals("")) {
			nPassword = null;
		} else {
			nPassword = strPassword.toCharArray();
		}
		ks.load(fis, nPassword);
		fis.close();
		// System.out.println("keystore type=" + ks.getType());
		// Now we loop all the aliases, we need the alias to get keys.
		// It seems that this value is the "Friendly name" field in the
		// detals tab <-- Certificate window <-- view <-- Certificate
		// Button <-- Content tab <-- Internet Options <-- Tools menu
		// In MS IE 6.
		Enumeration enumas = ks.aliases();
		String keyAlias = null;
		if (enumas.hasMoreElements())// we are readin just one certificate.
		{
			keyAlias = (String) enumas.nextElement();
			// System.out.println("alias=[" + keyAlias + "]");
		}
		// Now once we know the alias, we could get the keys.
		// System.out.println("is key entry=" + ks.isKeyEntry(keyAlias));
		PrivateKey prikey = (PrivateKey) ks.getKey(keyAlias, nPassword);
		Certificate cert = ks.getCertificate(keyAlias);
		PublicKey pubkey = cert.getPublicKey();
		// System.out.println("cert class = " + cert.getClass().getName());
		// System.out.println("cert = " + cert);
		// System.out.println("public key = " + pubkey);
		// System.out.println("private key = " + prikey);

		System.out.println("public key = " + byteArr2HexString(pubkey.getEncoded()));
		System.out.println("private key = " + byteArr2HexString(prikey.getEncoded()));
	}

	public static void readPubliKey(String path) throws Exception {
		System.out.println(path);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate cert = (X509Certificate) cf.generateCertificate(new FileInputStream(path));
		PublicKey publicKey = cert.getPublicKey();
		BASE64Encoder base64Encoder = new BASE64Encoder();
		// String publicKeyString = base64Encoder.encode(publicKey.getEncoded());
		String publicKeyString = byteArr2HexString(publicKey.getEncoded());
		System.out.println(publicKeyString);
	}
	
	// 转换成十六进制字符串
	public static String Byte2String(byte[] b) {
		String hs = "";
		String stmp = "";

		for (int n = 0; n < b.length; n++) {
			stmp = (Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
			// if (n<b.length-1) hs=hs+":";
		}
		return hs.toUpperCase();
	}

	public static byte[] StringToByte(int number) {
		int temp = number;
		byte[] b = new byte[4];
		for (int i = b.length - 1; i > -1; i--) {
			b[i] = new Integer(temp & 0xff).byteValue();// 将最高位保存在最低位
			temp = temp >> 8; // 向右移8位
		}
		return b;
	}

	public PrivateKey GetPvkformPfx(String strPfx, String strPassword) {
		try {
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(strPfx);
			// If the keystore password is empty(""), then we have to set
			// to null, otherwise it won't work!!!
			char[] nPassword = null;
			if ((strPassword == null) || strPassword.trim().equals("")) {
				nPassword = null;
			} else {
				nPassword = strPassword.toCharArray();
			}
			ks.load(fis, nPassword);
			fis.close();
			System.out.println("keystore type=" + ks.getType());
			// Now we loop all the aliases, we need the alias to get keys.
			// It seems that this value is the "Friendly name" field in the
			// detals tab <-- Certificate window <-- view <-- Certificate
			// Button <-- Content tab <-- Internet Options <-- Tools menu
			// In MS IE 6.
			Enumeration enumas = ks.aliases();
			String keyAlias = null;
			if (enumas.hasMoreElements())// we are readin just one certificate.
			{
				keyAlias = (String) enumas.nextElement();
				System.out.println("alias=[" + keyAlias + "]");
			}
			// Now once we know the alias, we could get the keys.
			System.out.println("is key entry=" + ks.isKeyEntry(keyAlias));
			PrivateKey prikey = (PrivateKey) ks.getKey(keyAlias, nPassword);
			Certificate cert = ks.getCertificate(keyAlias);
			PublicKey pubkey = cert.getPublicKey();
			System.out.println("cert class = " + cert.getClass().getName());
			System.out.println("cert = " + cert);
			System.out.println("public key = " + pubkey);
			System.out.println("private key = " + prikey);
			return prikey;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String byteArr2HexString(byte[] bytearr)
    {
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

    public static byte[] hexString2ByteArr(String hexString)
    {
        if ((hexString == null) || (hexString.length() % 2 != 0)) {
            return new byte[0];
        }

        byte[] dest = new byte[hexString.length() / 2];

        for (int i = 0; i < dest.length; i++) {
            String val = hexString.substring(2 * i, 2 * i + 2);
            dest[i] = ((byte)Integer.parseInt(val, 16));
        }
        return dest;
    }
    
    public static String sign(String data, String privateKey)
    {
        String sign = null;
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(hexString2ByteArr(privateKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyFactory.generatePrivate(keySpec);
            Signature si = Signature.getInstance("SHA1WithRSA");
            si.initSign(priKey);
            si.update(data.getBytes("UTF-8"));
            byte[] dataSign = si.sign();
            sign = byteArr2HexString(dataSign);
        } catch (Exception e) {
            e.printStackTrace();
            sign = null;
        }
        return sign;
    }

    public static boolean verify(String data, String sign, String publicKey)
    {
        boolean succ = false;
        try {
            Signature verf = Signature.getInstance("SHA1WithRSA");
            KeyFactory keyFac = KeyFactory.getInstance("RSA");
            PublicKey puk = keyFac.generatePublic(new X509EncodedKeySpec(hexString2ByteArr(publicKey)));
            verf.initVerify(puk);
            verf.update(data.getBytes("UTF-8"));
            succ = verf.verify(hexString2ByteArr(sign));
        } catch (Exception e) {
            e.printStackTrace();
            succ = false;
        }
        return succ;
    }
    
    
    public static String checkPriKeyAndPubKey(String priKey,String pubKey){
        
        String str = "key1=value1&key2=value2";
        
        //签名
        String sign = sign(str, priKey);
        
        //验签
        boolean key = verify(str, sign, pubKey);
        
        if(key){
            return "success";
        }
        
        return "faild";
    }
}