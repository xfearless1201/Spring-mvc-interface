package com.cn.tianxia.pay.jh.util;

import javax.net.ssl.TrustManagerFactory;

/**
 * <b>功能说明:</b>
 * @author 
 */
public class TrustKeyStore {
	private TrustManagerFactory trustManagerFactory;
	
	TrustKeyStore(TrustManagerFactory trustManagerFactory){
		this.trustManagerFactory = trustManagerFactory;
	}
	
	TrustManagerFactory getTrustManagerFactory(){
		return trustManagerFactory;
	}
}
