package com.cn.tianxia.pay.shzf.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpGetClient {

	/**
	 * 模拟请求
	 * 
	 * @param url		资源地址
	 * @param map	参数列表
	 * @param encoding	编码
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static String send(String url, Map<String,String> map) {
		String body = "";
		try {
			//创建httpclient对象
			CloseableHttpClient client = HttpClients.createDefault();
			//创建post方式请求对象
			HttpPost httpPost = new HttpPost(url);
			
			//装填参数
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			if(map!=null){
				for (Entry<String, String> entry : map.entrySet()) {
					nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
			}
			//设置参数到请求对象中
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
	 
			System.out.println("请求地址："+url);
			System.out.println("请求参数："+nvps.toString());
			
			//设置header信息
			//指定报文头【Content-type】、【User-Agent】
			httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
			httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
			
			//执行请求操作，并拿到结果（同步阻塞）
			CloseableHttpResponse response = client.execute(httpPost);
			//获取结果实体
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				//按指定编码转换结果实体为String类型
				body = EntityUtils.toString(entity, "UTF-8");
			}
			EntityUtils.consume(entity);
			//释放链接
			response.close();
		}catch (Exception e) {
		}
		
        return body;
	}



}
