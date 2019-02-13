package com.cn.tianxia.service.v2.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import com.cn.tianxia.dao.TransferDao;
import com.cn.tianxia.dao.v2.CagentDao;
import com.cn.tianxia.dao.v2.NewUserDao;
import com.cn.tianxia.dao.v2.PlatformConfigDao;
import com.cn.tianxia.dao.v2.PlatformStatusDao;
import com.cn.tianxia.dao.v2.UserGamestatusDao;
import com.cn.tianxia.entity.Transfer;
import com.cn.tianxia.entity.v2.CagentEntity;
import com.cn.tianxia.entity.v2.PlatformConfigEntity;
import com.cn.tianxia.entity.v2.UserEntity;
import com.cn.tianxia.entity.v2.UserGamestatusEntity;
import com.cn.tianxia.service.v2.UserGameTransferService;

@Service
public class UserGameTransferServiceImpl implements UserGameTransferService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserGameTransferServiceImpl.class);
    
    @Autowired
    private TransferDao transferDao;
    
    @Autowired
    private CagentDao cagentDao;
    
    @Autowired
    private NewUserDao newUserDao;
    
    @Autowired
    private UserGamestatusDao userGamestatusDao;
    
    @Autowired
    private PlatformConfigDao platformConfigDao;
    
    @Autowired
    private PlatformStatusDao platformStatusDao;
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public int insertUserTransferIn(Map<String, Object> data) {
        try {
            
            String uid = data.get("uid").toString();
            Double money = Double.parseDouble(data.get("t_money").toString());//订单 金额
            
            Transfer transfer = new Transfer();
            transfer.setUid(Integer.parseInt(data.get("uid").toString()));
            transfer.setBillno(data.get("billno").toString());
            transfer.setIp(data.get("ip").toString());
            transfer.setNewMoney(Float.valueOf(data.get("new_money").toString()));
            transfer.settMoney(Float.valueOf(data.get("t_money").toString()));
            transfer.setOldMoney(Float.valueOf(data.get("old_money").toString()));
            transfer.setResult(data.get("result").toString());
            transfer.settTime(new Date());
            transfer.settType(data.get("t_type").toString());
            transfer.setType(data.get("type").toString());
            transfer.setResult(data.get("result").toString());
            transfer.setUsername(data.get("ag_username").toString());
            transfer.setStatus(1);
            // 首先插入转账转入成功订单
            transferDao.insertSelective(transfer);
            // 扣除用户金额
            transferDao.updateWelletTransferIn(uid, money);
            return 1;
        } catch (Exception e) {
            logger.info("写入转入数据异常:{}",e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 0;
        }

    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public int insertUserTransferOut(Map<String, Object> data) {
        try {
            String uid = data.get("uid").toString();
            Double money = Double.parseDouble(data.get("t_money").toString());//订单 金额
            Transfer transfer = new Transfer();
            transfer.setUid(Integer.parseInt(data.get("uid").toString()));
            transfer.setBillno(data.get("billno").toString());
            transfer.setIp(data.get("ip").toString());
            transfer.setNewMoney(Float.valueOf(data.get("new_money").toString()));
            transfer.settMoney(Float.valueOf(data.get("t_money").toString()));
            transfer.setOldMoney(Float.valueOf(data.get("old_money").toString()));
            transfer.setResult(data.get("result").toString());
            transfer.settTime(new Date());
            transfer.settType(data.get("t_type").toString());
            transfer.setType(data.get("type").toString());
            transfer.setResult(data.get("result").toString());
            transfer.setUsername(data.get("ag_username").toString());
            transfer.setStatus(1);
            // 首先插入转账转入成功订单
            transferDao.insertSelective(transfer);
            // 扣除用户金额
            transferDao.updateWelletTransferOut(uid, money);
            return 1;
        } catch (Exception e) {
            logger.info("写入转入数据异常:{}",e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 0;
        }
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public int insertUserTransferFaild(Map<String, Object> data) {
        Transfer transfer = new Transfer();
        transfer.setUid(Integer.parseInt(data.get("uid").toString()));
        transfer.setBillno(data.get("billno").toString());
        transfer.setIp(data.get("ip").toString());
        transfer.setNewMoney(Float.valueOf(data.get("new_money").toString()));
        transfer.settMoney(Float.valueOf(data.get("t_money").toString()));
        transfer.setOldMoney(Float.valueOf(data.get("old_money").toString()));
        transfer.setResult(data.get("result").toString());
        transfer.settTime(new Date());
        transfer.settType(data.get("t_type").toString());
        transfer.setType(data.get("type").toString());
        transfer.setResult(data.get("result").toString());
        transfer.setUsername(data.get("ag_username").toString());
        transfer.setStatus(1);
        return transferDao.insertTransferFaild(transfer);
    }

    
    @Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public int insertUserTransferOutFaild(Map<String, Object> data) {
        try {
            String uid = data.get("uid").toString();
            Double money = Double.parseDouble(data.get("t_money").toString());//订单 金额
            Transfer transfer = new Transfer();
            transfer.setUid(Integer.parseInt(data.get("uid").toString()));
            transfer.setBillno(data.get("billno").toString());
            transfer.setIp(data.get("ip").toString());
            transfer.setNewMoney(Float.valueOf(data.get("new_money").toString()));
            transfer.settMoney(Float.valueOf(data.get("t_money").toString()));
            transfer.setOldMoney(Float.valueOf(data.get("old_money").toString()));
            transfer.setResult(data.get("result").toString());
            transfer.settTime(new Date());
            transfer.settType(data.get("t_type").toString());
            transfer.setType(data.get("type").toString());
            transfer.setResult(data.get("result").toString());
            transfer.setUsername(data.get("ag_username").toString());
            transfer.setStatus(1);
            transferDao.insertTransferFaild(transfer);
            transferDao.updateWelletTransferOut(uid, money);
            return 1;
        } catch (Exception e) {
            logger.info("转出(游戏上分)失败");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 0;
        }
        
    }

    @Override
    public Map<String, String> selectPlatformGameStatusByCagent(String cagent) {
        //通过用户平台编码查询平台游戏状态
        CagentEntity cagentEntity = cagentDao.selectByCagent(cagent);
        if(cagent != null){
            Map<String, String> map = 
                    transferDao.selectPlatformGameStatusByCagent(cagentEntity.getId());
            if(!CollectionUtils.isEmpty(map)){
                return map;
            }
        }
        return null;
    }

    /**
     * 获取用户余额
     */
    @Override
    public double getUserBalance(String uid) {
        Double balance = transferDao.getBalance(uid);
        if(balance == null){
            balance = 0.00D;
        }
        return balance;
    }

    @Override
    public int selectUserGameStatusBy(String uid, String gametype) {
        Integer status = transferDao.selectUserGameStatus(uid, gametype);
        if(status == null){
            return 0;
        }
        return status;
    }

    @Override
    public int insertUserGameStatus(String uid, String gametype) {
        return transferDao.insertUserGameStatus(uid, gametype);
    }

    @Override
    public String selectUserTypeHandicap(String game, String typeId) {
        //通过用户ID查询用户的分层信息
        return transferDao.selectUserTypeHandicap(game, typeId);
    }

    @Override
    public UserEntity selectUserInfoByUid(Integer uid) {
        return newUserDao.selectByPrimaryKey(uid);
    }

    @Override
    public UserGamestatusEntity getUserGamestatusByGameType(String uid, String gametype) {
        return userGamestatusDao.selectByGameType(uid, gametype);
    }

    @Override
    public Map<String, String> getPlatformConfig() {
        Map<String,String> data = new HashMap<String, String>();
        //查询所有游戏配置
        List<PlatformConfigEntity> platformConfigs = platformConfigDao.findAll();
        if(!CollectionUtils.isEmpty(platformConfigs)){
            for (PlatformConfigEntity platformConfig : platformConfigs) {
                data.put(platformConfig.getPlatformKey(), platformConfig.getPlatformConfig());
            }
        }
        return data;
    }

    @Override
    public Map<String, String> getPlatformStatusByCid(String cid) {
        return platformStatusDao.selectByCid(cid);
    }
    
}
