package com.cn.tianxia.pay.yjh.util;

import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

/**
 * Http工具类
 * @author devin
 * 2016年6月24日
 */
public class HttpUtil {

	/** 超时时间 **/
	private static final int SOCKE_TTIMEOUT = 15 * 1000;
	private static final int CONNECT_TIMEOUT = 15 * 1000;
	
	/** http连接管理器和客户端 */
	private static PoolingHttpClientConnectionManager connManager;
	private static CloseableHttpClient httpClient;
	
	/** 连接回收任务 */
	private static ScheduledExecutorService scheduler;
	
	// 初始化http-client
	private static CloseableHttpClient getHttpClient() {
	    if (httpClient == null) synchronized (HttpUtil.class) {
	        if (httpClient == null) {
	            RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create();
	            ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();
	            registryBuilder.register("http", plainSF);
	            try {
	                // 指定信任的密钥存储对象（信任任何链接）
	                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	                TrustStrategy anyTrustStrategy = new TrustStrategy() {
	                    // 信任所有
	                    @Override
	                    public boolean isTrusted(X509Certificate[] x509Certificates, String s)
	                            throws CertificateException {
	                        return true;
	                    }
	                };
	                SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(trustStore, anyTrustStrategy).build();
	                registryBuilder.register("https", new SSLConnectionSocketFactory(sslContext, 
                            new String[] {"TLSv1", "TLSv1.1", "TLSv1.2"},   // 支持的加密协议
                            null,
                            SSLConnectionSocketFactory.getDefaultHostnameVerifier()));
	            } catch (Exception e) {
	                throw new RuntimeException(e);
	            }
	            
	            Registry<ConnectionSocketFactory> registry = registryBuilder.build();
	            connManager = new PoolingHttpClientConnectionManager(registry);
	            httpClient = HttpClientBuilder.create().setConnectionManager(connManager).build();
	            
	            // 启动连接回收任务，每2s执行一次，延迟2s开始执行
	            scheduler = Executors.newScheduledThreadPool(1);
	            scheduler.scheduleWithFixedDelay(new Runnable() {
                    public void run() {
                        if (connManager != null) {
                            try {
                                connManager.closeExpiredConnections();
                                connManager.closeIdleConnections(30, TimeUnit.SECONDS); // 空闲时间30s
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, 2, 2, TimeUnit.SECONDS);
	        }
        }
		
	    return httpClient;
	}
	
	// 执行http request
	public static HttpResult doRequest(HttpRequestBase httpRequest) {
		StringBuffer traceBuffer = new StringBuffer("url:<").append(httpRequest.getURI()).append(">");

		// 设置超时时间
		RequestConfig requestConfig = RequestConfig.custom().
				setSocketTimeout(SOCKE_TTIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).build(); 
		httpRequest.setConfig(requestConfig);
		
		// 执行
		CloseableHttpClient httpClient = getHttpClient();
		CloseableHttpResponse response = null;
		HttpResult httpResult = new HttpResult();
		
		try {
			response = httpClient.execute(httpRequest);
			
			// 状态码
			int statusCode = response.getStatusLine().getStatusCode();
			httpResult.setStatusCode(statusCode);
			traceBuffer.append(", statusCode:<").append(statusCode).append(">");
			
			// 提取内容
			HttpEntity entity = response.getEntity();
			String responseBody = EntityUtils.toString(entity, Config.CHARSET);
			httpResult.setContent(responseBody);
			traceBuffer.append(", ret:<").append(responseBody).append(">");
		} catch (Exception e) {
			httpResult.setContent(e.toString());
			e.printStackTrace();
		} finally {
		    System.out.println(traceBuffer.toString());
			if (null != response) {
                try {
                    response.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
		}
		
		return httpResult;
	}

	// 发送get请求
	public static HttpResult doGet(String url, Map<String, String> params) {
		// 拼接uri
		StringBuilder uriBuilder = new StringBuilder(url);
		if (!Util.isEmpty(params)) {
			// ?
			uriBuilder.append("?");
			
			// x=y&
			Map.Entry<String, String> entry = null;
			for(Iterator<Map.Entry<String, String>> itr = params.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				uriBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
			}
			
			// 移除末尾的'&'
			uriBuilder.setLength(uriBuilder.length() - 1);
		}

		HttpGet httpGet = new HttpGet(uriBuilder.toString());
		return doRequest(httpGet);
	}
	
	public static HttpResult doGet(String url) throws IOException {
		return doGet(url, null);
	}

	// 发送post请求
	public static HttpResult doPost(String url, Map<String, String> params)  {
		HttpPost httpPost = new HttpPost(url);
		
		// 添加参数
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (!Util.isEmpty(params)) {
		    Map.Entry<String, String> entry = null;
			for(Iterator<Map.Entry<String, String>> itr = params.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		}
		
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
		return doRequest(httpPost);
	}
	
	public static HttpResult doPost(String url)  {
		return doPost(url, null);
	}
	
	// http返回结果
	public static class HttpResult{
		private int statusCode = -1;  // 异常的情况
		private String content = "";
		
		public boolean isSuccess() {
			return HttpStatus.SC_OK == this.statusCode;
		}
		
		public int getStatusCode() {
			return statusCode;
		}
		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		
		@Override
		public String toString() {
			return "statusCode:" + statusCode + ", content:" + content;
		}
	}
}