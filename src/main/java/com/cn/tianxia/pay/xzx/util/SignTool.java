package com.cn.tianxia.pay.xzx.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

public class SignTool {

	/**
	 * the location where you put your private key
	 */
	private String keyFilePath;
	/**
	 * the password
	 */
	private String password = "";

	/**
	 * 商户号，不带01
	 */
	private String merchantAcct = "";

	/**
	 * cert alias
	 */
	private String alias = "";


	public static final String SIGNATUREPARAMS = "merchantAcctId,terminalId,payType,"
			+ "orderId,orderTime,orderAmount,dealId,dealTime,"
			+ "bankDealId,payResult,errCode,errMsg";

	public SignTool() {
	}

	public SignTool(String merchantAcct, String pfxFilePath, String password,
			String alias) {
		this.merchantAcct = merchantAcct;
		this.keyFilePath = pfxFilePath;
		this.password = password;
		this.alias = alias;
	}
	

	public String signMsg(String sourceMsg) {

		System.out.println("sourceMsg:" + sourceMsg);

		String signMsg = "";
		try {
//			keyFilePath = SignTool.class.getResource(merchantAcct + ".pfx")
//					.toURI().getPath();
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream ksfis = new FileInputStream(keyFilePath);
//			BufferedInputStream ksbufin = new BufferedInputStream(ksfis);
//			char[] keyPwd = password.toCharArray();
//			ks.load(ksbufin, password.toCharArray());
			PrivateKey priK = GetPvkformPfx(ksfis, password);//(PrivateKey) ks.getKey(alias, keyPwd);
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initSign(priK);
			signature.update(sourceMsg.getBytes("UTF-8"));
			sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
			signMsg = encoder.encode(signature.sign());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("signMsg:" + signMsg);
		return signMsg;
	}
	
	
	
	
	public static PrivateKey GetPvkformPfx(InputStream pfx, String strPassword) {
		try {
			KeyStore ks = KeyStore.getInstance("PKCS12");
			
			char[] nPassword = null;
			if ((strPassword == null) || strPassword.trim().equals("")) {
				nPassword = null;
			} else {
				nPassword = strPassword.toCharArray();
			}
			ks.load(pfx, nPassword);
			pfx.close();
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
			}
			// Now once we know the alias, we could get the keys.
			PrivateKey prikey = (PrivateKey) ks.getKey(keyAlias, nPassword);
			return prikey;
		} catch (Exception e) {
		}
		
		return null;
	}


//	public String verfiy(HttpServletRequest request) throws IOException {
//		String message = null;
//		Map<String, Object> map = ServletUtils.getRequestParameters(request);
//		message = message + "notify info:" + map.toString();
//		System.out.println("messag===" + message);
//		String dataReceived = "";
//		List<String> keyList = new ArrayList<String>();
//		Set<String> set = map.keySet();
//		for (String key : set) {
//			if (SIGNATUREPARAMS.indexOf(key) >= 0 && map.get(key) != null
//					&& !StringUtils.isEmpty((String) map.get(key))) {
//				keyList.add(key);
//			}
//		}
//		Collections.sort(keyList);
//
//		for (String key : keyList) {
//			dataReceived = appendParam(dataReceived, key, (String) map.get(key));
//		}
//
//		String signMsg = (String) map.get("signMsg");
//		System.err.println("signMsg===" + signMsg);
//
//		boolean veryfyResult = false;
//		try {
//			veryfyResult = XMLSecurityProcess.veryfySignature(dataReceived,
//					signMsg);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		message = message + "\r\n " + ":" + veryfyResult;
//		message += dataReceived;
//		return message;
//	}

	public String appendParam(String returns, String paramId, String paramValue) {
		if (returns != "") {
			if (paramValue != "") {

				returns += "&" + paramId + "=" + paramValue;
			}

		} else {

			if (paramValue != "") {
				returns = paramId + "=" + paramValue;
			}
		}

		return returns;
	}

	public static void main(String[] args) {
		SignTool pair = new SignTool();
		String sourceMsg = "bankDealId=140000046106&bankId=ICBC&dealId=1128&errCode=000000&exchangeRate=null&language=1&merchantAcctId=1002148002101&orderAmount=600&orderCurrency=EUR&orderId=20150420150332&orderTime=20150420150332&payAmount=null&payResult=10&payType=10&signType=null&version=v2.0";
		String msg = "VaDSZuu8RywzhD2ge8N4TroD2LqlUMQdG+YcD+MNdm3C6oTp1CPfrwM5GNyvHUFBXvrexfmlYd5txEfs0rB3pbDlrf9ZlwUu5UBciWUm+OZHoaomc7z8HPD+2q6JTzIH9pZ7fLBJq3s7juzSW6qtTjf6Cz6qsQvCwzwnRU05Xqs";
		String flag = pair.signMsg(sourceMsg);
		System.err.println("==" + flag);
		// pair.enCodeByCerA(val, msg);
	}

}
