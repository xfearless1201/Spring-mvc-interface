package com.cn.tianxia.po.v2;

import com.cn.tianxia.po.BaseResponse;
import net.sf.json.JSONObject;

/**
 * @Auther: zed
 * @Date: 2019/2/6 21:05
 * @Description: 用户注册返回po
 */
public class RegisterResponse extends BaseResponse {

    public static JSONObject success(JSONObject msg){
        JSONObject data = new JSONObject();
        data.put("code",SUCCESS_CODE);
        data.put("status",SUCCESS_STATUS);
        data.put("msg",msg);
        return data;
    }

}
