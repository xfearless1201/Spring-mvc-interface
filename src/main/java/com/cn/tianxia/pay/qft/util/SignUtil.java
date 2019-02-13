package com.cn.tianxia.pay.qft.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

public class SignUtil {

    public static String generateMD5(Map<String, String> params, String fileterKey, String  req) {
        String signKey = req;
        if (signKey == null || "".equals(signKey) || "null".equals(signKey) || params == null || params.isEmpty()) {
            return "";
        }

        List<String> sortList = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String val = (String) entry.getValue();
            if (fileterKey.indexOf(key) > -1) {
                continue;
            }
            if (val == null || "".equals(val)) {
                continue;
            }
            sortList.add(key + "=" + val);
        }
        Collections.sort(sortList);
        StringBuffer preSign = new StringBuffer();
        for (String key : sortList) {
            preSign.append(key).append("&");
        }
        preSign.delete(preSign.length()-1,preSign.length()).append(signKey);
        System.out.println("preSign===>>" + preSign);
        return Md5.utf8(preSign.toString());
    }
}
