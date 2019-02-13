package com.cn.tianxia.pay.utils;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName HttpServletUtils
 * @Description selvet工具类
 * @author Hardy
 * @Date 2018年9月29日 下午3:16:13
 * @version 1.0.0
 */
public class HttpServletUtils {

    public static String getV4IP() {
        String ip = "";
        String url = "http://pv.sohu.com/cityjson?ie=utf-8";
        String result = null;
        try {
            result = HttpUtils.toPostJson("",url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String subResult = result.substring(19, result.length() - 1);
        // 解析返回内容
        Map<String, Object> strMap = JSONObject.fromObject(subResult);
        if (StringUtils.isNotBlank(subResult)) {
            ip = strMap.get("cip").toString();
        } else {
            ip = "127.0.0.1";
        }
        return ip;
    }
    
    /**
     * 功能描述:
     * 获取请求IP
     * @Author: Hardy
     * @Date: 2018年08月28日 17:36:18
     * @param request
     * @return: java.lang.String
     **/
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if(StringUtils.isNotBlank(ip) && ip.equals("unKnown")){
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }else{
            ip = request.getHeader("X-Real-IP");
            if(StringUtils.isNotBlank(ip) && ip.equals("unKnown")){
                return ip;
            }
        }
        return String.valueOf(request.getRemoteAddr());
    }
}
