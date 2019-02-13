package com.cn.tianxia.service.v2;

import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.cn.tianxia.entity.v2.UserEntity;
import com.cn.tianxia.entity.v2.UserGamestatusEntity;

/**
 * 
 * @ClassName UserGameTransferService
 * @Description 用户游戏转账接口
 * @author Hardy
 * @Date 2019年1月27日 上午7:09:06
 * @version 1.0.0
 */
public interface UserGameTransferService {

    int insertUserTransferIn(Map<String,Object> data);
    
    int insertUserTransferOut(Map<String,Object> data);
    
    int insertUserTransferFaild(Map<String,Object> data);
    
    int insertUserTransferOutFaild(Map<String, Object> data);
    
    Map<String, String> selectPlatformGameStatusByCagent(String cagent);
    
    /**
     * 
     * @Description 查询用户游戏状态
     * @param uid
     * @param gametype
     * @return
     */
    int selectUserGameStatusBy(String uid,String gametype);
    
    /**
     * 
     * @Description 写入用户游戏状态
     * @param uid
     * @param gametype
     * @return
     */
    int insertUserGameStatus(String uid,String gametype);
    
    /**
     * 
     * @Description 获取用户余额
     * @param uid
     * @return
     */
    double getUserBalance(String uid);
    
    
    /**
     * 
     * @Description 查询会员分层游戏盘口
     * @return
     */
    String selectUserTypeHandicap(String game,String typeId);
    
    /**
     * 
     * @Description 查询用户信息
     * @param uid
     * @return
     */
    UserEntity selectUserInfoByUid(Integer uid);
    
    /**
     * 
     * @Description 查询会员游戏状态
     * @param uid
     * @param gametype
     * @return
     */
    UserGamestatusEntity getUserGamestatusByGameType(String uid,String gametype);
    
    /**
     * 
     * @Description 获取所有游戏配置信息
     * @return
     */
    Map<String,String> getPlatformConfig();
    
    /**
     * 
     * @Description 查询平台游戏开关状态
     * @param cid
     * @return
     */
    Map<String,String> getPlatformStatusByCid(@Param("cid") String cid);
}
