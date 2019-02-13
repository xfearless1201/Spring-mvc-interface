package com.cn.tianxia.enums;

/**
 * 
 * @ClassName RocketMQEnum
 * @Description rocketMq消息枚举类
 * @author Hardy
 * @Date 2018年11月19日 下午8:18:26
 * @version 1.0.0
 */
public enum RocketMQEnum {
    NOTIFY_GROUP("NOTIFY_TOPIC","NOTIF_TAG"),
    ;
    
    // 消息群组名称
    private String topic;
    // 信息标签
    private String tag;
    
    private RocketMQEnum(String topic, String tag) {
        this.topic = topic;
        this.tag = tag;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

}
