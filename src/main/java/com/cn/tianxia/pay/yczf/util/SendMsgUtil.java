package com.cn.tianxia.pay.yczf.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SendMsgUtil {

	public static String post(String reqUrl, String reqMsg) {
		StringBuffer buffer = new StringBuffer();
		HttpURLConnection connection = null;
		try {

			URL url = new URL(reqUrl);
			connection = (HttpURLConnection) url.openConnection();

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Cache-Control", "no-cache");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			connection.setRequestProperty("Accept-Language", "zh-CN");
			connection.setRequestProperty("Accept-Charset", "UTF-8");

			if (reqMsg != null && !reqMsg.trim().equals("")) {
				connection.getOutputStream().write(reqMsg.getBytes());
				connection.getOutputStream().flush();
				connection.getOutputStream().close();
			}
			connection.connect();

			int statusCode = connection.getResponseCode();
			if (statusCode == 200) {
				InputStreamReader in = new InputStreamReader(connection.getInputStream());
				BufferedReader br = new BufferedReader(in);
				String line;
				while (null != (line = br.readLine())) {
					buffer.append(line);
				}
				in.close();
				br.close();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return buffer.toString();
	}
}
