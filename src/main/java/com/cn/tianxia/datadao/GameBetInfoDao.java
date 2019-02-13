package com.cn.tianxia.datadao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.cn.tianxia.entity.v2.GameBetInfoEntity;

/**
 * 
 * @ClassName GameBetInfoDao
 * @Description 游戏注单dao
 * @author Hardy
 * @Date 2019年1月31日 上午10:12:28
 * @version 1.0.0
 */
public interface GameBetInfoDao {

    /**
     * 
     * @Description 查询用户游戏注单记录列表
     * @param username 游戏登录名称
     * @param cagent 平台编码
     * @param bdate 起始时间
     * @param sdate 结束时间
     * @return
     */
    List<GameBetInfoEntity> findAllByPage(@Param("username") String username,@Param("cagent") String cagent,
                                            @Param("type") String type,
                                            @Param("startime") Date startime,@Param("endtime") Date endtime,
                                            @Param("pageNo") int pageNo,@Param("pageSize") int pageSize);
    
    /**
     * 
     * @Description 查询用户游戏注单记录总条数
     * @param username
     * @param cagent
     * @param type
     * @param startime
     * @param endtime
     * @return
     */
    Integer selectBetCount(@Param("username") String username,@Param("cagent") String cagent,
                           @Param("type") String type,
                           @Param("startime") Date startime,@Param("endtime") Date endtime);
}
