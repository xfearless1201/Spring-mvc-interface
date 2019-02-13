package com.cn.tianxia.pay.mob.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class HttpUrlConnection {

    public String send(String uri,String params) throws IOException{
        URL url = new URL(uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStream inStream = null;
        try {
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(3000);//���ӳ�ʱ ��λ����
//	        conn.setReadTimeout(3000);//��ȡ��ʱ ��λ����
	        conn.setDoOutput(true);
	        
	        byte[] bypes = params.toString().getBytes();
	        conn.getOutputStream().write(bypes);
	        System.out.println();
	        inStream=conn.getInputStream();
	        return new String(StreamTool.readInputStream(inStream), "gbk");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			inStream.close();
		}
		return null;
    }

}
