package com.cn.tianxia.pay.dc.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wendy
 * @since 2017/4/18
 */
public class HttpClientUtils {

    public static String post(String uri, Map<String, String> paramMap) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(10000)
                .setConnectTimeout(10000).build();

        HttpPost httppost = new HttpPost(uri);
        httppost.setConfig(requestConfig);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        StringBuilder url = new StringBuilder();
        url.append(uri).append("?");
        String result = null;
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            params.add(new BasicNameValuePair(key, value));
            url.append(key).append("=").append(value).append("&");
        }
        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse httpresponse = httpClient.execute(httppost);
            HttpEntity entity = httpresponse.getEntity();
            result = EntityUtils.toString(entity);
        } catch (Exception e) {
            throw e;
        } finally {
            httpClient.close();
        }
        return result;
    }
}
