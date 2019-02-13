
/**
 * @author Administrator
 *
 */
package com.cn.tianxia.pay.mqzf.util;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.pay.ly.util.HttpFormParameter;
import com.cn.tianxia.pay.ly.util.HttpMethod;
import com.cn.tianxia.pay.ly.util.HttpSendModel;
import com.cn.tianxia.pay.ly.util.SimpleHttpClient;
import com.cn.tianxia.pay.ly.util.SimpleHttpResponse;

public class HttpUtil{
	private final static Logger logger = LoggerFactory.getLogger(HttpUtil.class);
	
	public static String HtmlFrom(String Url,Map<String,String> Parms){
		if(Parms.isEmpty()){
			return  "参数不能为空！";
		}
		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\"" + Url + "\">";
		for (String key : Parms.keySet()){
			FormString +="<input name=\"" + key + "\" type=\"hidden\" value='" + Parms.get(key) + "'>\r\n";
		}
		
	    FormString +="</form></body>";
	    return FormString; 
	}
	
	public static String RequestForm(String Url,Map<String,String> Parms){		
		if(Parms.isEmpty()){
			return  "参数不能为空！";
		}
		String PostParms = "";
		
		int PostItemTotal = Parms.keySet().size();
		int Itemp=0;
		for (String key : Parms.keySet()){
			PostParms += key + "="+Parms.get(key);
			Itemp++;
			if(Itemp<PostItemTotal){
				PostParms +="&";
			}
		}
		
		logger.info("【请求参数】："+PostParms);
		HttpSendModel httpSendModel = new HttpSendModel(Url + "?" + PostParms);
		logger.info("【后端请求】：" + Url + "?" + PostParms);
		httpSendModel.setMethod(HttpMethod.POST);
		SimpleHttpResponse response = null;
		try {
			response = doRequest(httpSendModel, "UTF-8");
		} catch (Exception e) {
			return e.getMessage();
		}
		return response.getEntityString();
	}
	
	public static SimpleHttpResponse doRequest(HttpSendModel httpSendModel,
			String getCharSet) throws Exception {

		// 创建默认的httpClient客户端端
		SimpleHttpClient simpleHttpclient = new SimpleHttpClient();

		try {
			return doRequest(simpleHttpclient, httpSendModel, getCharSet);
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}finally {
			simpleHttpclient.getHttpclient().getConnectionManager().shutdown();
		}

	}

	/**
	 * @param httpclient
	 * @param httpSendModel
	 * @param getCharSet
	 * @return
	 * @throws Exception 
	 */

	public static SimpleHttpResponse doRequest(SimpleHttpClient simpleHttpclient, HttpSendModel httpSendModel,String getCharSet) throws Exception {
		HttpRequestBase httpRequest = buildHttpRequest(httpSendModel);

		if (httpSendModel.getUrl().startsWith("https://")) {
			simpleHttpclient.enableSSL();
		}

		try {
			HttpResponse response = simpleHttpclient.getHttpclient().execute(httpRequest);
			int statusCode = response.getStatusLine().getStatusCode();

			if (isRequestSuccess(statusCode)) {
				return new SimpleHttpResponse(statusCode, EntityUtils.toString(response.getEntity(), getCharSet), null);
			} else {
				return new SimpleHttpResponse(statusCode, null, response.getStatusLine().getReasonPhrase());
			}
		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * @param httpSendModel
	 * @return
	 * @throws Exception 
	 */
	protected static HttpRequestBase buildHttpRequest(
			HttpSendModel httpSendModel) throws Exception {
		HttpRequestBase httpRequest;
		if (httpSendModel.getMethod() == null) {
			throw new Exception("请求方式未设定");
		} else if (httpSendModel.getMethod() == HttpMethod.POST) {

			String url = httpSendModel.getUrl();
			String sendCharSet = httpSendModel.getCharSet();
			List<HttpFormParameter> params = httpSendModel.getParams();

			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			if (params != null && params.size() != 0) {

				for (HttpFormParameter param : params) {
					qparams.add(new BasicNameValuePair(param.getName(), param
							.getValue()));
				}

			}

			HttpPost httppost = new HttpPost(url);
			try {
				httppost.setEntity(new UrlEncodedFormEntity(qparams,
						sendCharSet));
			} catch (UnsupportedEncodingException e) {
				throw new Exception("构建post请求参数失败", e);
			}

			httpRequest = httppost;
		} else if (httpSendModel.getMethod() == HttpMethod.GET) {
			HttpGet httpget = new HttpGet(httpSendModel.buildGetRequestUrl());

			httpRequest = httpget;
		} else {
			throw new Exception("请求方式不支持：" + httpSendModel.getMethod());
		}

		return httpRequest;
	}

	/**
	 * 请求是否成功
	 * 
	 * @param statusCode
	 * @return
	 */
	public static boolean isRequestSuccess(int statusCode) {
		return statusCode == 200;
	}
	
	/**
	 * 
	 * @Title: post   
	 * @Description: http-post请求   
	 * @param: @param url
	 * @param: @param data
	 * @param: @return      
	 * @return: String      
	 * @throws
	 */
	public static String post(String url,Map<String,String> data){
		try {
			HttpClient httpClient = wrapClient(url);
			HttpPost httpPost = new HttpPost(url);
			if(data != null && data.size() > 0){
	        	List<NameValuePair> nvps = new ArrayList<NameValuePair>();
	        	for (Map.Entry<String, String> d : data.entrySet()) {
	        		NameValuePair v = new BasicNameValuePair(d.getKey(),d.getValue());
	        		nvps.add(v);
				}
	        	UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nvps, Consts.UTF_8);
	            httpPost.setEntity(formEntity);
	        }
	        
	        HttpResponse response = httpClient.execute(httpPost);
	        if(response.getStatusLine().getStatusCode() == 200){
	        	HttpEntity entity = response.getEntity();
	        	
	        	BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(),Consts.UTF_8));
	            StringBuffer sb = new StringBuffer();
	            
	            String content = null;
	            while((content = reader.readLine()) != null){
	            	sb.append(content);
	            }
	            return sb.toString();
	        }else{
	        	return null;
	        }
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
     * 获取 HttpClient
     * @param host
     * @param path
     * @return
     */
    private static HttpClient wrapClient(String url) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        if (StringUtils.isNotBlank(url) && url.startsWith("https://")) {
            return sslClient();
        }
        return httpClient;
    }
	
	public static HttpClient sslClient(){
		try {
            // 在调用SSL之前需要重写验证方法，取消检测SSL
            X509TrustManager trustManager = new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
				public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
            };
            
            SSLContext ctx = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
            ctx.init(null, new TrustManager[] { trustManager }, null);
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);
            // 创建Registry
            RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT)
                    .setExpectContinueEnabled(Boolean.TRUE).setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM,AuthSchemes.DIGEST))
                    .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https",socketFactory).build();
            // 创建ConnectionManager，添加Connection配置信息
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            CloseableHttpClient closeableHttpClient = HttpClients.custom().setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(requestConfig).build();
            return closeableHttpClient;
        } catch (KeyManagementException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
	}
}
