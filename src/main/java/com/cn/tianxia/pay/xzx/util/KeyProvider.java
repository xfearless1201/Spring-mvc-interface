package com.cn.tianxia.pay.xzx.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class KeyProvider {
	
	/**
	 * @return
	 * @throws Exception
	 */
    public static SecretKey getkeyEncryedKey() throws Exception 
    {
    	 String jceAlgorithmName = "AES";
         KeyGenerator keyGenerator = KeyGenerator.getInstance(jceAlgorithmName);
         keyGenerator.init(128);
         return keyGenerator.generateKey();
    }

    
    /**
     * @return  
     * @throws Exception
     */
	public static PrivateKey getPrivateKey(String envType,String memberCode, String sPass, String kPass,
			String alias,String instanceName) throws CertificateException, IOException,
			UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException {
		KeyStore ks = loadKeyStore(envType,memberCode, sPass, instanceName);
		Key key = null;
		PublicKey publicKey = null;
		PrivateKey privateKey = null;
		if (ks.containsAlias(alias)) {
			key = ks.getKey(alias, kPass.toCharArray());
			if (key instanceof PrivateKey) {
				Certificate cert = ks.getCertificate(alias);
				publicKey = cert.getPublicKey();
				privateKey = (PrivateKey) key;
				return new KeyPair(publicKey, privateKey).getPrivate();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static KeyStore loadKeyStore(String envType, String memberCode,String sPass,String instanceName)throws KeyStoreException, NoSuchAlgorithmException,CertificateException, IOException {
		KeyStore myKS = KeyStore.getInstance(instanceName);
		InputStream is = KeyProvider.class.getResourceAsStream("/licence/"+ envType+ "/"+memberCode+".pfx");
		if(is == null){
			is = KeyProvider.class.getResourceAsStream("/licence/default.pfx");
		}
		myKS.load(is, sPass.toCharArray());
		is.close();
		return myKS;
	}
    
    
    /**
     * @return
     * @throws Exception
     */
    public static Key getServerPublicKey(String envType) throws Exception
    {
    	InputStream is = KeyProvider.class.getResourceAsStream("/licence/"+ envType+ "/99bill_public/atimes_public.cer");
    	
    	try{
    		CertificateFactory certFactory=CertificateFactory.getInstance("X.509");
        	
    		Certificate cert = certFactory.generateCertificate(is);
    		return cert.getPublicKey();
    	}catch(Exception e){
    		throw new Exception("read public key error.");
    	}finally{
    		is.close();
    	}
    }
}
