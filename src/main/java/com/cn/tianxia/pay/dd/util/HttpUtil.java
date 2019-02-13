package com.cn.tianxia.pay.dd.util;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class HttpUtil {
    private static int SocketTimeout = 10000;//10绉�
    private static int ConnectTimeout = 10000;//10绉�
    private static Boolean SetTimeOut = true;

    private static CloseableHttpClient getHttpClient() {
        RegistryBuilder<ConnectionSocketFactory> registryBuilder =
            RegistryBuilder.<ConnectionSocketFactory>create();
        ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();
        registryBuilder.register("http", plainSF);
        //鎸囧畾淇′换瀵嗛挜瀛樺偍瀵硅薄鍜岃繛鎺ュ鎺ュ瓧宸ュ巶
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            //淇′换浠讳綍閾炬帴
            TrustStrategy anyTrustStrategy = new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            };
            SSLContext sslContext =
                SSLContexts.custom().useTLS().loadTrustMaterial(trustStore, anyTrustStrategy).build();
            LayeredConnectionSocketFactory sslSF =
                new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            registryBuilder.register("https", sslSF);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        Registry<ConnectionSocketFactory> registry = registryBuilder.build();
        //璁剧疆杩炴帴绠＄悊鍣�
        PoolingHttpClientConnectionManager connManager =
            new PoolingHttpClientConnectionManager(registry);
        //		connManager.setDefaultConnectionConfig(connConfig);
        //		connManager.setDefaultSocketConfig(socketConfig);
        //鏋勫缓瀹㈡埛绔�
        return HttpClientBuilder.create().setConnectionManager(connManager).build();
    }

    /**
     * get
     *
     * @param url     璇锋眰鐨剈rl
     * @param queries 璇锋眰鐨勫弬鏁帮紝鍦ㄦ祻瑙堝櫒锛熷悗闈㈢殑鏁版嵁锛屾病鏈夊彲浠ヤ紶null
     * @return
     * @throws IOException
     */
    public static String get(String url, Map<String, String> queries) throws IOException {
        String responseBody = "";
        //CloseableHttpClient httpClient=HttpClients.createDefault();
        //鏀寔https
        CloseableHttpClient httpClient = getHttpClient();

        StringBuilder sb = new StringBuilder(url);

        if (queries != null && queries.keySet().size() > 0) {
            boolean firstFlag = true;
            Iterator iterator = queries.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry<String, String>) iterator.next();
                if (firstFlag) {
                    sb.append("?" + (String) entry.getKey() + "=" + (String) entry.getValue());
                    firstFlag = false;
                } else {
                    sb.append("&" + (String) entry.getKey() + "=" + (String) entry.getValue());
                }
            }
        }

        HttpGet httpGet = new HttpGet(sb.toString());
        if (SetTimeOut) {
            RequestConfig requestConfig =
                RequestConfig.custom().setSocketTimeout(SocketTimeout).setConnectTimeout(ConnectTimeout).build();//璁剧疆璇锋眰鍜屼紶杈撹秴鏃舵椂闂�
            httpGet.setConfig(requestConfig);
        }
        try {
            System.out.println("Executing request " + httpGet.getRequestLine());
            //璇锋眰鏁版嵁
            CloseableHttpResponse response = httpClient.execute(httpGet);
            System.out.println(response.getStatusLine());
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                // do something useful with the response body
                // and ensure it is fully consumed
                responseBody = EntityUtils.toString(entity);
                //EntityUtils.consume(entity);
            } else {
                System.out.println("http return status error:" + status);
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            httpClient.close();
        }
        return responseBody;
    }

    /**
     * post
     *
     * @param url     璇锋眰鐨剈rl
     * @param queries 璇锋眰鐨勫弬鏁帮紝鍦ㄦ祻瑙堝櫒锛熷悗闈㈢殑鏁版嵁锛屾病鏈夊彲浠ヤ紶null
     * @param params  post form 鎻愪氦鐨勫弬鏁�
     * @return
     * @throws IOException
     */
    public static String post(String url, Map<String, String> queries, Map<String, String> params) throws IOException {
        String responseBody = "";
        //CloseableHttpClient httpClient = HttpClients.createDefault();
        //鏀寔https
        CloseableHttpClient httpClient = getHttpClient();

        StringBuilder sb = new StringBuilder(url);

        if (queries != null && queries.keySet().size() > 0) {
            boolean firstFlag = true;
            Iterator iterator = queries.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry<String, String>) iterator.next();
                if (firstFlag) {
                    sb.append("?" + (String) entry.getKey() + "=" + (String) entry.getValue());
                    firstFlag = false;
                } else {
                    sb.append("&" + (String) entry.getKey() + "=" + (String) entry.getValue());
                }
            }
        }

        //鎸囧畾url,鍜宧ttp鏂瑰紡
        HttpPost httpPost = new HttpPost(sb.toString());
        if (SetTimeOut) {
            RequestConfig requestConfig =
                RequestConfig.custom().setSocketTimeout(SocketTimeout).setConnectTimeout(ConnectTimeout).build();//璁剧疆璇锋眰鍜屼紶杈撹秴鏃舵椂闂�
            httpPost.setConfig(requestConfig);
        }
        //娣诲姞鍙傛暟
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        if (params != null && params.keySet().size() > 0) {
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
                nvps.add(new BasicNameValuePair((String) entry.getKey(), (String) entry.getValue()));
            }
        }
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
        //璇锋眰鏁版嵁
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            System.out.println(response.getStatusLine());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                // do something useful with the response body
                // and ensure it is fully consumed
                responseBody = EntityUtils.toString(entity);
                //EntityUtils.consume(entity);
            } else {
                System.out.println(
                    "http return status error:" + response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            response.close();
        }
        return responseBody;
    }

    public static String post(String url, List<NameValuePair> nvps) throws IOException {

        String responseBody = "";
        //CloseableHttpClient httpClient = HttpClients.createDefault();
        //鏀寔https
        CloseableHttpClient httpClient = getHttpClient();

        StringBuilder sb = new StringBuilder(url);

        //鎸囧畾url,鍜宧ttp鏂瑰紡
        HttpPost httpPost = new HttpPost(sb.toString());
        if (SetTimeOut) {
            RequestConfig requestConfig =
                RequestConfig.custom().setSocketTimeout(SocketTimeout).setConnectTimeout(ConnectTimeout).build();//璁剧疆璇锋眰鍜屼紶杈撹秴鏃舵椂闂�
            httpPost.setConfig(requestConfig);
        }

        httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
        //璇锋眰鏁版嵁
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            System.out.println(response.getStatusLine());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                // do something useful with the response body
                // and ensure it is fully consumed
                responseBody = EntityUtils.toString(entity);
                //EntityUtils.consume(entity);
            } else {
                System.out.println(
                    "http return status error:" + response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            response.close();
        }
        return responseBody;
    }
}
