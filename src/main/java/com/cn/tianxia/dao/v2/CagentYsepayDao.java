package com.cn.tianxia.dao.v2;


import com.cn.tianxia.entity.v2.CagentYsepayEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CagentYsepayDao {
    int deleteByPrimaryKey(Integer id);

    int insert(CagentYsepayEntity record);

    int insertSelective(CagentYsepayEntity record);

    CagentYsepayEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CagentYsepayEntity record);

    int updateByPrimaryKey(CagentYsepayEntity record);

    List<CagentYsepayEntity> selectPaymentListById(@Param("uid") String userId, @Param("payId") String payId);

    CagentYsepayEntity selectPaymentConfigByUidAndPayId(@Param("uid") String userId,@Param("pid") String payId);
}