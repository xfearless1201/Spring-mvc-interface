package com.cn.tianxia.mq.producer.impl;

import java.util.UUID;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.cn.tianxia.common.v2.SerializableUtil;
import com.cn.tianxia.mq.producer.IProducerService;
import com.cn.tianxia.mq.vo.MQBaseVO;

/**
 * 
 * @ClassName GameTransferProducerImpl
 * @Description 游戏转账生成着实现类
 * @author Hardy
 * @Date 2019年2月7日 下午6:45:39
 * @version 1.0.0
 */
public class GameTransferProducerImpl implements IProducerService,InitializingBean {
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(GameTransferProducerImpl.class);

    private String namesrvAddr;

    private String producerGroup;
    
    private String producerTopic;

    private Boolean retryAnotherBrokerWhenNotStoreOK;

    private DefaultMQProducer producer;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        producer = new DefaultMQProducer();
        producer.setProducerGroup( this.producerGroup );
        producer.setNamesrvAddr( this.namesrvAddr );
        producer.setRetryAnotherBrokerWhenNotStoreOK(this.retryAnotherBrokerWhenNotStoreOK);
        producer.start();
        logger.info( "[{}:{}] start successd!",producerGroup,namesrvAddr );
    }
    
    /**
     * 销毁
     */
    public void destroy() throws Exception {
        if (producer != null) {
            logger.info("producer: [{}:{}] end ",producerGroup,namesrvAddr);
            producer.shutdown();
        }

    }
    
    @Override
    public void send(String topic, MQBaseVO entity) {
        String keys = UUID.randomUUID().toString();
        entity.setMqKey(keys);
        String tags = entity.getClass().getName();
        Message msg = new Message(topic, tags, keys,
                SerializableUtil.toByte(entity));
        try {
            producer.send(msg);
        } catch (Exception e) {
            logger.error(keys.concat(":发送消息失败"), e);
            throw new RuntimeException("发送消息失败",e);
        } 
    }

    @Override
    public void send(String topic, MQBaseVO entity, SendCallback sendCallback) {
        String keys = UUID.randomUUID().toString();
        entity.setMqKey(keys);
        String tags = entity.getClass().getName();
        Message msg = new Message(topic, tags, keys,
                SerializableUtil.toByte(entity));
        try {
            producer.send(msg, sendCallback);
        } catch (Exception e) {
            logger.error(keys.concat(":发送消息失败"), e);
            throw new RuntimeException("发送消息失败",e);
        } 
        
    }

    @Override
    public void sendOneway(String topic, MQBaseVO entity) {
        String keys = UUID.randomUUID().toString();
        entity.setMqKey(keys);
        String tags = entity.getClass().getName();
        Message msg = new Message(topic, tags, keys,
                SerializableUtil.toByte(entity));
        try {
            producer.sendOneway(msg);
        } catch (Exception e) {
            logger.error(keys.concat(":发送消息失败"), e);
            throw new RuntimeException("发送消息失败",e);
        }
    }

    
    public String getNamesrvAddr() {
        return namesrvAddr;
    }
    
    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }
    
    public String getProducerGroup() {
        return producerGroup;
    }
    
    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }
    
    public Boolean getRetryAnotherBrokerWhenNotStoreOK() {
        return retryAnotherBrokerWhenNotStoreOK;
    }
    
    public void setRetryAnotherBrokerWhenNotStoreOK(Boolean retryAnotherBrokerWhenNotStoreOK) {
        this.retryAnotherBrokerWhenNotStoreOK = retryAnotherBrokerWhenNotStoreOK;
    }
    
    public String getProducerTopic() {
        return producerTopic;
    }

    
    public void setProducerTopic(String producerTopic) {
        this.producerTopic = producerTopic;
    }

    public DefaultMQProducer getProducer() {
        return producer;
    }
    
    public void setProducer(DefaultMQProducer producer) {
        this.producer = producer;
    }
}
