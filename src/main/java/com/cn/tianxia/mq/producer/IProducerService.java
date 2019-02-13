package com.cn.tianxia.mq.producer;

import org.apache.rocketmq.client.producer.SendCallback;

import com.cn.tianxia.mq.vo.MQBaseVO;

/**
 * 
 * @ClassName IProducerService
 * @Description 生产者接口
 * @author Hardy
 * @Date 2019年2月7日 下午6:44:39
 * @version 1.0.0
 */
public interface IProducerService {
    /**
     * 同步发送MQ
     * @param topic
     * @param entity
     */
    public void send(String topic, MQBaseVO entity);

    /**
     * 发送MQ,提供回调函数，超时时间默认3s
     * @param topic 
     * @param entity
     * @param sendCallback
     */
    public void send( String topic, MQBaseVO entity, SendCallback sendCallback );

    /**
     * 单向发送MQ，不等待服务器回应且没有回调函数触发，适用于某些耗时非常短，但对可靠性要求并不高的场景，例如日志收集。
     * @param topic
     * @param entity
     */
    public void sendOneway(String topic, MQBaseVO entity);
}
