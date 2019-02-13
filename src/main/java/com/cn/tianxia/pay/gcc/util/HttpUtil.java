package com.cn.tianxia.pay.gcc.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class HttpUtil {

	private static final Logger logger = LoggerFactory
			.getLogger(HttpUtil.class);
	// 连接超时时间
	private static final int CONNECTION_TIMEOUT = 30000;// 30秒
	// 读数据超时时间
	private static final int READ_DATA_TIMEOUT = 30000;// 30秒
	
	/***编码格式*/
	private static volatile String ENCODING="utf-8";
	
	private static PoolingHttpClientConnectionManager connManager = null;
	private static CloseableHttpClient httpclient = null;
	static {
		connManager = new PoolingHttpClientConnectionManager();
		httpclient = HttpClients.custom().setConnectionManager(connManager)
				.build();
	}

	/**
	 * sslClient
	 * 
	 * @return
	 */
	private static CloseableHttpClient createSSLClient() {
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(
					null, new TrustStrategy() {
						// 信任所有
						@Override
						public boolean isTrusted(X509Certificate[] chain,
								String authType) throws CertificateException {
							return true;
						}
					}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					sslContext,
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return HttpClients.createDefault();
	}
	
	/**
	 * post请求，如果失败尝试3次
	 * @param url
	 * @param data
	 * @param encoding
	 * @return
	 */
	public static String tryPost(String url, Map<String, String> data, String encoding) {
		String resultStr = "";
		for (int i = 0; i < 3; i++) {
			try {
				resultStr = post(StringUtils.trim(url), data, encoding);
				break;
			} catch (Exception e) {
				logger.error("请求异常count:{连接地址："+url+"，次数} " + i, e);
			}
		}
		return resultStr;
	}

	/**
	 * Post请求（默认超时时间）
	 * 
	 * @param url
	 * @param data
	 * @param encoding
	 * @return
	 */
	public static String post(String url, Map<String, String> data,
			String encoding) throws IOException {
		return post(url, CONNECTION_TIMEOUT, READ_DATA_TIMEOUT, data, encoding);
	}
	
	
	/**
	 * Post请求（默认超时时间,默认编码格式）
	 * 
	 * @param url
	 * @param data
	 * @param encoding
	 * @return
	 */
	public static String post(String url, Map<String, String> data) throws IOException {
		
		logger.info("请求URL:"+url);
		logger.info("请求内容:"+JSON.toJSONString(data));
		
		return post(url, CONNECTION_TIMEOUT, READ_DATA_TIMEOUT, data, ENCODING);
	}
	
	public static String post(String url, String data,
			String encoding) throws IOException {
		return post(url, CONNECTION_TIMEOUT, READ_DATA_TIMEOUT, data, encoding);
	}

	public static String post(String url, int timeout,
			Map<String, String> data, String encoding) throws IOException {
		return post(url, timeout, timeout, data, encoding);
	}

	/**
	 * Post请求
	 * 
	 * @param url
	 * @param connectTimeout
	 * @param readTimeout
	 * @param data
	 * @param encoding
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	private static String post(String url, int connectTimeout, int readTimeout,
			Map<String, String> data, String encoding) throws IOException {
		HttpPost post = new HttpPost(url);
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(readTimeout)
				.setConnectTimeout(connectTimeout)
				.setConnectionRequestTimeout(connectTimeout)
				.setExpectContinueEnabled(false).build();
		post.setConfig(requestConfig);

		if (null != data && !data.isEmpty()) {
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			for (String key : data.keySet()) {
				String val =data.get(key);
				if(StringUtils.isNotBlank(val)){
					formparams.add(new BasicNameValuePair(key, val));
				}
			}
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(
					formparams, encoding);
			post.setEntity(formEntity);
		}
		CloseableHttpResponse response = null;
		if (url.startsWith("https")) {// https
			response = createSSLClient().execute(post);
		} else {
			response = httpclient.execute(post);
		}

		HttpEntity entity = response.getEntity();
		try {
			if (entity != null) {
				String str = EntityUtils.toString(entity, encoding);
				return str;
			}
		} finally {
			if (entity != null) {
				entity.getContent().close();
			}
			if (response != null) {
				response.close();
			}
		}
		return null;
	}
	
	/**
	 * Post请求
	 * 
	 * @param url
	 * @param connectTimeout
	 * @param readTimeout
	 * @param data
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	private static String post(String url, int connectTimeout, int readTimeout,
			String data, String encoding) throws IOException{
		HttpPost post = new HttpPost(url);
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(readTimeout)
				.setConnectTimeout(connectTimeout)
				.setConnectionRequestTimeout(connectTimeout)
				.setExpectContinueEnabled(false).build();
		post.setConfig(requestConfig);
		post.setEntity(new StringEntity(data,encoding));
		CloseableHttpResponse response = null;
		if (url.startsWith("https")) {// https
			response = createSSLClient().execute(post);
		} else {
			response = httpclient.execute(post);
		}

		HttpEntity entity = response.getEntity();
		try {
			if (entity != null) {
				String str = EntityUtils.toString(entity, encoding);
				return str;
			}
		} finally {
			if (entity != null) {
				entity.getContent().close();
			}
			if (response != null) {
				response.close();
			}
		}
		return null;
	}

	/**
	 * get请求，如果失败尝试3次
	 * 
	 * @param url
	 * @param encoding
	 * @return
	 */
	public static String tryGet(String url, String encoding) {
		String resultStr = "";
		for (int i = 0; i < 3; i++) {
			try {
				resultStr = get(url, encoding);
				break;
			} catch (Exception e) {
				logger.error("请求异常count:{连接地址："+url+"} ", i, e);
			}
		}
		return resultStr;
	}
	
	/**
	 * get请求（默认超时时间）
	 * 
	 * @param url
	 * @param data
	 * @param encoding
	 * @return
	 */
	public static String get(String url, String encoding) throws IOException {
		return get(url, null, CONNECTION_TIMEOUT, READ_DATA_TIMEOUT, encoding);
	}

	public static String get(String url, Map<String, String> cookies,
			String encoding) throws IOException {
		return get(url, cookies, CONNECTION_TIMEOUT, READ_DATA_TIMEOUT,
				encoding);
	}

	public static String get(String url, Map<String, String> cookies,
			int timeout, String encoding) throws IOException {
		return get(url, cookies, timeout, timeout, encoding);
	}

	private static String get(String url, Map<String, String> cookies,
			int connectTimeout, int readTimeout, String encoding)
			throws IOException {
		HttpGet get = new HttpGet(url);
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(readTimeout)
				.setConnectTimeout(connectTimeout)
				.setConnectionRequestTimeout(connectTimeout)
				.setExpectContinueEnabled(false).build();
		get.setConfig(requestConfig);
		if (cookies != null && !cookies.isEmpty()) {
			StringBuilder buffer = new StringBuilder(128);
			for (String cookieKey : cookies.keySet()) {
				buffer.append(cookieKey).append("=")
						.append(cookies.get(cookieKey)).append("; ");
			}
			// 设置cookie内容
			get.setHeader(new BasicHeader("Cookie", buffer.toString()));
		}
		CloseableHttpResponse response = null;
		if (url.startsWith("https")) {// https
			response = createSSLClient().execute(get);
		} else {
			response = httpclient.execute(get);
		}
		HttpEntity entity = response.getEntity();
		try {
			if (entity != null) {
				String str = EntityUtils.toString(entity, encoding);
				return str;
			}
		} finally {
			if (entity != null) {
				entity.getContent().close();
			}
			if (response != null) {
				response.close();
			}
		}

		return null;
	}

	/**
	 * map转成queryStr
	 * 
	 * @param paramMap
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String mapToQueryStr(Map<String, String> paramMap) {
		StringBuffer strBuff = new StringBuffer();
		for (String key : paramMap.keySet()) {
			strBuff.append(key).append("=").append(paramMap.get(key))
					.append("&");
		}
		return strBuff.substring(0, strBuff.length() - 1);
	}
	

    /**
     * @param params
     * @return
     * @throws Exception
     */
    public static Map<String, String> flattenParams(Map<String, Object> params)
            throws Exception {
        if (params == null) {
            return new HashMap<String, String>();
        }
        Map<String, String> flatParams = new HashMap<String, String>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?>) {
                Map<String, Object> flatNestedMap = new HashMap<String, Object>();
                Map<?, ?> nestedMap = (Map<?, ?>) value;
                for (Map.Entry<?, ?> nestedEntry : nestedMap.entrySet()) {
                    flatNestedMap.put(
                            String.format("%s[%s]", key, nestedEntry.getKey()),
                            nestedEntry.getValue());
                }
                flatParams.putAll(flattenParams(flatNestedMap));
            } else if (value instanceof ArrayList<?>) {
                ArrayList<?> ar = (ArrayList<?>) value;
                Map<String, Object> flatNestedMap = new HashMap<String, Object>();
                int size = ar.size();
                for (int i = 0; i < size; i++) {
                    flatNestedMap.put(String.format("%s[%d]", key, i), ar.get(i));
                }
                flatParams.putAll(flattenParams(flatNestedMap));
            } else if ("".equals(value)) {
               
            } else if (value == null) {
                flatParams.put(key, "");
            } else {
                flatParams.put(key, value.toString());
            }
        }
        return flatParams;
    }

}
