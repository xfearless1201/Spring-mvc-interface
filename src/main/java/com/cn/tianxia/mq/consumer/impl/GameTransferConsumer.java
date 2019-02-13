package com.cn.tianxia.mq.consumer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cn.tianxia.dao.TransferDao;
import com.cn.tianxia.dao.v2.NewUserDao;
import com.cn.tianxia.entity.Transfer;
import com.cn.tianxia.entity.v2.UserEntity;
import com.cn.tianxia.mq.vo.GameTransferVO;
import com.cn.tianxia.mq.vo.MQBaseVO;

/**
 * 
 * @ClassName GameTransferConsumer
 * @Description 游戏转账消费
 * @author Hardy
 * @Date 2019年2月7日 下午11:45:42
 * @version 1.0.0
 */
public class GameTransferConsumer extends AbstractConsumerServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(GameTransferConsumer.class);
    
    @Autowired
    private TransferDao transferDao;
    
    @Autowired
    private NewUserDao newUserDao;
    
    @Override
    public void execute(MQBaseVO entity) {
        logger.info("消费游戏转账失败订单信息开始=============START==================");
        try {
            
            GameTransferVO gameTransferVO = (GameTransferVO) entity;
            
            Integer id = gameTransferVO.getId();
            String uid = gameTransferVO.getUid();
            Transfer transfer = transferDao.selectByPrimaryKey(id);
            
            UserEntity user = newUserDao.selectByPrimaryKey(Integer.parseInt(uid));
            System.err.println("转账信息:"+transfer.toString()+",用户信息:"+user.toString());
            
        } catch (Exception e) {
            // TODO: handle exception
        }   
    }
}
