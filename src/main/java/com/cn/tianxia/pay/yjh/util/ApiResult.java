package com.cn.tianxia.pay.yjh.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * api调用结果
 * @author devin
 * <br/>2017年8月26日
 */
public class ApiResult {

    /** 成功的返回码 */
    private static final String SUC_CODE = "00";
    
    /** 服务器返回的json字符串 */
    private String jsonstr;
    
    /** 解析json字符串得到的json对象 */
    private JSONObject json;
    
    /**
     * 初始化
     * @param jsonstr json字符串
     */
    public ApiResult(String jsonstr) {
        this.jsonstr = jsonstr;
        json = JSON.parseObject(jsonstr);
    }
    
    /**
     * 是否返回成功
     * @return
     */
    public boolean isSuccess() {
        return SUC_CODE.equals(getRetCode());
    }
    
    public String get(String key) {
        return json.getString(key);
    }

    public String getRetCode() {
        return get("retCode");
    }
    
    public String getRetMsg() {
        return get("retMsg");
    }
    
    @Override
    public String toString() {
        return jsonstr;
    }
}
