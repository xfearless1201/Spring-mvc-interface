package com.cn.tianxia.mq.vo;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @ClassName MQBaseVO
 * @Description MQ基本消息VO类
 * @author Hardy
 * @Date 2019年2月7日 下午11:35:32
 * @version 1.0.0
 */
public class MQBaseVO implements Serializable {

    private static final long serialVersionUID = 8797094956526436467L;

    private Map<String, Object> extObj = new LinkedHashMap<String, Object>();

    private String mqId;

    private String mqKey;

    public Map<String, Object> getExtObj() {
        return extObj;
    }

    public void setExtObj(Map<String, Object> extObj) {
        this.extObj = extObj;
    }

    public String getMqId() {
        return mqId;
    }

    public void setMqId(String mqId) {
        this.mqId = mqId;
    }

    public String getMqKey() {
        return mqKey;
    }

    public void setMqKey(String mqKey) {
        this.mqKey = mqKey;
    }

    @Override
    public String toString() {
        return "MQBaseVO [extObj=" + extObj + ", mqId=" + mqId + ", mqKey=" + mqKey + "]";
    }
    
}
