package com.cn.tianxia.mq.consumer.impl;

import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.v2.SerializableUtil;
import com.cn.tianxia.mq.consumer.IConsumerService;
import com.cn.tianxia.mq.vo.MQBaseVO;

/**
 * 
 * @ClassName AbstractConsumerServiceImpl
 * @Description 消费者抽象类
 * @author Hardy
 * @Date 2019年2月7日 下午10:26:09
 * @version 1.0.0
 */
public abstract class AbstractConsumerServiceImpl implements IConsumerService {

    protected Logger logger = LoggerFactory.getLogger(AbstractConsumerServiceImpl.class);

    private String classTypeName;
    
    @Override
    public void handlerMessage(MessageExt msg) {
        try {
            MQBaseVO entity = doStart(msg);
            execute(entity);
            doEnd(entity);
        } catch (Exception e) {
            logger.error("处理mq消息异常。",e);
        }
    }

    /**
     * 解析mq消息前置处理
     * @param msg
     * @param entity 
     * @throws ClassNotFoundException 
     */
    protected MQBaseVO doStart(MessageExt msg) throws ClassNotFoundException {
        Class<? extends MQBaseVO> clazz = (Class<? extends MQBaseVO>) Class.forName(classTypeName);
        return SerializableUtil.parse(msg.getBody(), clazz);
    }

    /**
     * 解析mq消息后置处理
     * @param entity
     */
    protected void doEnd(MQBaseVO entity) {
        
    }

    /**
     * 解析mq消息 MessageExt
     * @param entity
     */
    public abstract void execute(MQBaseVO entity);

    public void setClassTypeName(String classTypeName) {
        this.classTypeName = classTypeName;
    }
    
}
