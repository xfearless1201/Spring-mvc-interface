package com.cn.tianxia.util;

public class main{
	
	public static void main(String[] args){
		
		IpLocation1 location=IpLocation1.getInstance();
		String address=location.findLocation("2001:DB8:0:23:8:800:200C:417A");
		System.out.println(address);

	}
	
}
