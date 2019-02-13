package com.cn.tianxia.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

/**
 * JDBC 的工具类 其中包含: 获取数据库连接, 关闭数据库资源等方法.
 */
public class IPTools {

    public static String getIpAddress(HttpServletRequest request) {
        /*
                                          * String ip = request.getHeader("x-forwarded-for"); if(ip == null ||
                                          * ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { ip =
                                          * request.getHeader("Proxy-Client-IP"); } if(ip == null || ip.length() == 0 ||
                                          * "unknown".equalsIgnoreCase(ip)) { ip =
                                          * request.getHeader("WL-Proxy-Client-IP"); } if(ip == null || ip.length() == 0
                                          * || "unknown".equalsIgnoreCase(ip)) { ip = request.getRemoteAddr(); }
                                          * if(ip.indexOf(",") !=-1){ //多级反向代理，截取有效IP ip =ip.split(",")[0]; }
                                          * if("0:0:0:0:0:0:0:1".equals(ip)){ ip="0.0.0.0"; } return ip;
                                          */

        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    public static String getIp(HttpServletRequest request) {
        String ipAddress = getIpAddress(request);
        if (StringUtils.isBlank(ipAddress)){
            return "127.0.0.1";
        }
        return ipAddress;
    }

}
