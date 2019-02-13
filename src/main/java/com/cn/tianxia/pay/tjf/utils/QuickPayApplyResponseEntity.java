package com.cn.tianxia.pay.tjf.utils;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruby on 2017/10/19.
 */
public class QuickPayApplyResponseEntity {

    private String code;
    private String desc;
    private String opeNo;
    private String opeDate;
    private String sessionID;
    private String sign;

    public QuickPayApplyResponseEntity() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getOpeNo() {
        return opeNo;
    }

    public void setOpeNo(String opeNo) {
        this.opeNo = opeNo;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getOpeDate() {
        return opeDate;
    }

    public void setOpeDate(String opeDate) {
        this.opeDate = opeDate;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public void parse(String respStr) throws Exception {
        Map<String, String> resultMap = new HashMap<String, String>();
        XMLParserUtil.parse(respStr, resultMap);
        Document doc = DocumentHelper.parseText(respStr);
        Element root = doc.getRootElement();
        Element respData = root.element("detail");
        String srcData = respData.asXML();
        code = resultMap.get("/message/detail/code");
        if (StringUtils.isBlank(code)) {
            throw new Exception("响应信息格式错误：不存在'code'节点。");
        }
        desc = resultMap.get("/message/detail/desc");
        if (StringUtils.isBlank(desc)) {
            throw new Exception("响应信息格式错误：不存在'desc'节点。");
        }
        opeNo = resultMap.get("/message/detail/opeNo");
        opeDate = resultMap.get("/message/detail/opeDate");
        sessionID = resultMap.get("/message/detail/sessionID");

        sign = resultMap.get("/message/sign");
        if (StringUtils.isBlank(sign)) {
            throw new Exception("响应信息格式错误：不存在'sign'节点");
        }

        if (!SignUtil.verifyData(getSign(), srcData)) {
            throw new Exception("签名验证不通过");
        }
    }
}
