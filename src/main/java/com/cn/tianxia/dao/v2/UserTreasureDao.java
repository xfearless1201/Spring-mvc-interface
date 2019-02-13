package com.cn.tianxia.dao.v2;

import com.cn.tianxia.entity.v2.UserTreasureEntity;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface UserTreasureDao {
    int deleteByPrimaryKey(Integer id);

    int insert(UserTreasureEntity record);

    int insertSelective(UserTreasureEntity record);

    UserTreasureEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserTreasureEntity record);

    int updateByPrimaryKey(UserTreasureEntity record);
    
    /**
     * 
     * @Description 分页查询用户资金流水
     * @param uid
     * @param type
     * @param bdate
     * @param edate
     * @param pageNo
     * @param pageSize
     * @return
     */
    List<UserTreasureEntity> findAllByPage(@Param("uid") String uid,@Param("type") String type,
                                            @Param("bdate") Date bdate,@Param("edate") Date edate,
                                            @Param("pageNo") Integer pageNo,@Param("pageSize") Integer pageSize);
    

    /**
     * 
     * @Description 统计通页数
     * @param uid
     * @param type
     * @param bdate
     * @param edate
     * @return
     */
    Map<String,String> countTotalPages(@Param("uid") String uid,@Param("type") String type,
                                                @Param("bdate") Date bdate,@Param("edate") Date edate);
}