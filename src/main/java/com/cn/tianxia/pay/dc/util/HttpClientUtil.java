package com.cn.tianxia.pay.dc.util;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
/*
 * 利用HttpClient进行post请求的工具类
 */

import com.alibaba.fastjson.JSONObject;

/**
 *
 * ClassName: HttpClientUtil <br/>
 * Function: TODO 类的作用：. <br/>
 * @author lishaofeng  396191970@qq.com
 * @date: 2017年3月1日 上午11:36:12 <br/>
 * @version
 * @since JDK 1.8
 */
public class HttpClientUtil {

	/**
	 *
	 * doPostMap:(方法的作用：). <br/>
	 * TODO(注意事项 ：).<br/>
	 * @param url
	 * @param map
	 * @param charset UTF-8
	 * @param contentType  "application/x-www-form-urlencoded"  "text/plain" application/json
	 * @return
	 *
	 * @author lishaofeng  396191970@qq.com
	 * @date: 2017年3月1日 上午11:36:44 <br/>
	 */
	public static  String doPostMap(String url,Map<String,String> map,String charset,String contentType){
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		try{
			httpClient = new SSLClient();
			httpPost = new HttpPost(url);
			//设置参数
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			Iterator iterator = map.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<String,String> elem = (Entry<String, String>) iterator.next();
				list.add(new BasicNameValuePair(elem.getKey(),elem.getValue()));
			}
			if(list.size() > 0){
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,charset);
				httpPost.setEntity(entity);
				httpPost.setHeader("Content-Type", contentType);

			}
			HttpResponse response = httpClient.execute(httpPost);
			if(response != null){
				System.out.println("response.getStatusLine().getStatusCode()"+response.getStatusLine().getStatusCode());
				HttpEntity resEntity = response.getEntity();
				if(resEntity != null){
					result = EntityUtils.toString(resEntity,charset);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return result;
	}

	/**
	 *
	 * doPost:(方法的作用：). <br/>
	 * TODO(注意事项 ：).<br/>
	 * @param url
	 * @param str
	 * @param charset UTF-8
	 * @param contentType  "application/x-www-form-urlencoded"  "text/plain" application/json
	 * @return
	 *
	 * @author lishaofeng  396191970@qq.com
	 * @date: 2017年3月1日 上午11:38:09 <br/>
	 */
	public static  String doPost(String url,String str,String charset,String contentType){
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		try{
			httpClient = new SSLClient();
			httpPost = new HttpPost(url);
			//设置参数
            StringEntity entityParams = new StringEntity(str, charset);
			httpPost.setEntity(entityParams );
			httpPost.setHeader("Content-Type", contentType);


			HttpResponse response = httpClient.execute(httpPost);
			if(response != null){
			    System.out.println("网络状态："+response.getStatusLine().getStatusCode());
				HttpEntity resEntity = response.getEntity();
				if(resEntity != null){
					result = EntityUtils.toString(resEntity,charset);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return result;
	}

	public static  HttpEntity doPostEntity(String url,String str,String charset,String contentType){
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		HttpEntity resEntity= null;
		try{
			httpClient = new SSLClient();
			httpPost = new HttpPost(url);
			//设置参数
            StringEntity entityParams = new StringEntity(str, charset);
			httpPost.setEntity(entityParams );
			httpPost.setHeader("Content-Type", contentType);


			HttpResponse response = httpClient.execute(httpPost);
			if(response != null){
				 System.out.println("网络状态："+response.getStatusLine().getStatusCode());
				 resEntity = response.getEntity();

			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return resEntity;
	}
	  /**
     * 发送get请求
     *
     * @param url
     *            路径
     * @return
     */
    public static JSONObject httpGet1(String url) {
        // get请求返回结果
        JSONObject jsonResult = null;
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            // 发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);

            /** 请求发送成功，并得到响应 **/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /** 读取服务器返回过来的json字符串数据 **/
                String strResult = EntityUtils.toString(response.getEntity());
                /** 把json字符串转换成json对象 **/
                jsonResult = JSONObject.parseObject(strResult);
                url = URLDecoder.decode(url, "UTF-8");
            } else {
            }
        } catch (IOException e) {
        }
        return jsonResult;
    }

    public static String httpGet(String url) throws IOException {
        CloseableHttpResponse response = null;
        CloseableHttpClient client = null;
        String res = null;

        try {
            HttpGet httpGet = new HttpGet(url);

            httpGet.setHeader("Content-Type", "text/plain;charset=UTF-8");
            client = HttpClients.createDefault();
            response = client.execute(httpGet);
            HttpEntity httpEntity = response.getEntity();

            if (response == null || httpEntity == null) {
                res = "操作失败";
                return "";
            }
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                res = "操作失败" + statusCode;
                return "";
            }

            res = new String(EntityUtils.toByteArray(httpEntity), "UTF-8");
        } catch (Exception e) {
            return "";
        } finally {
            if (response != null) {
                response.close();
            }
            if (client != null) {
                client.close();
            }
        }
        return res;

    }
}