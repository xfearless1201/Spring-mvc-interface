package com.cn.tianxia.service;

import org.apache.ibatis.annotations.Param;

import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * 功能概要：UserService接口类
 */
public interface LuckyDrawService {

    //根据来源域名查询平台活动
    List<Map<String, Object>> selectLuckyDrawStatus(String domain);

    //根据活动ID查询活动明细
    List<Map<String, Object>> selectLuckyDrawDetail(int lid);

    //查询会员可抽奖次数
    List<Map<String, Object>> selectUserTimes(Map<String, Object> map);

    List<Map<String, Object>> selectUserLuckDrawTodayTimes(@Param("lid") String lid, @Param("uid") String uid, @Param("begintime") String begintime, @Param("endtime") String endtime);

    List<Map<String, Object>> selectUserLuckDrawTotalTimes(@Param("lid") String lid, @Param("uid") String uid);

    void insertUserLuckrdrawLog(Map<String, Object> luckrdrawLogMap);

    Map<String, Object> selectByPrimaryKey(@Param("uid") String uid);

    Map<String, Object> selectByCidCagentStoredvalue(@Param("cagent") String cagent);

    void insertUserTreasure(Map<String, String> userWalletLog);

    void updateByPrimaryKeySelective(Map<String, Object> updateUserWallet);

    Map<String, Object> getUserWalletId(@Param("uid") String uid, @Param("number") String number);

    void insertStoredvalueLog(Map<String, String> hashMap);

    void updateStoredvalue(Map<String, String> storedvalueMap);

    int updateLuckydraw(Map<String, String> storedvalueMap);

    String selectUserDetail(Map<String, Object> userTimesMap);

    List<Map<String, Object>> selectUserValidBetTimes(Map<String, Object> userTimesMap);
    
    public JSONObject luckyDraw(String userName,String refurl)throws Exception;
}
