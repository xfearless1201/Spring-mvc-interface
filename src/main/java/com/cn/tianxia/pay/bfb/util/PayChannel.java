package com.cn.tianxia.pay.bfb.util;

/**
 * 
 * @ClassName:  PayType   
 * @Description:支付通道枚举类
 * @author: Hardy
 * @date:   2018年8月18日 下午9:45:07   
 *     
 * @Copyright: 天下科技 
 *
 */
public enum PayChannel {
	BANK("BANK","网银"),WX("WX","微信"),ALI("ALI","支付宝"),CFT("CFT","财付通,QQ钱包"),YL("YL","银联"),JD("JD","京东"),KJ("KJ","快捷");
	private String code;
	
	private String name;

	private PayChannel(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
