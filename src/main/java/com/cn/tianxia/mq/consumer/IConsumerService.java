package com.cn.tianxia.mq.consumer;

import org.apache.rocketmq.common.message.MessageExt;

public interface IConsumerService {
    /**
     * 消费端解析消息
     * @param msg
     */
    void handlerMessage(MessageExt msg);
}
