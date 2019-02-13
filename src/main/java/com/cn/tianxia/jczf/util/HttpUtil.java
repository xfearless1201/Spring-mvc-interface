package com.cn.tianxia.jczf.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * Created on 2018/1/26.
 */
public class HttpUtil {

  public static Map<String, Object> doPost(String url, Map<String, Object> param) {
    // 创建Httpclient对象
    CloseableHttpClient httpClient = HttpClientBuilder.create()
        .setConnectionTimeToLive(30, TimeUnit.MINUTES).build();
    CloseableHttpResponse response = null;
    try {
      // 创建Http Post请求
      HttpPost httpPost = new HttpPost(url);
      // 创建参数列表
      if (param != null) {
        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        for (Entry<String, Object> entry : param.entrySet()) {
          if (entry.getValue() != null && !"".equals(entry.getValue())) {
            paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
          }
        }
        System.out.println("paramList: " + JSON.toJSONString(paramList));
        // 模拟表单
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, "UTF-8");
        httpPost.setEntity(entity);
      }
      // 执行http请求
      response = httpClient.execute(httpPost);
      if (response != null) {
        String resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        return JSON.parseObject(resultString, new TypeReference<Map<String, Object>>() {
        });
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (response != null) {
          response.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

}
