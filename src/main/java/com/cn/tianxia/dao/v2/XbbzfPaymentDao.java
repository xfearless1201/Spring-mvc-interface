package com.cn.tianxia.dao.v2;

import com.cn.tianxia.entity.v2.XbbzfPaymentEntity;
import org.apache.ibatis.annotations.Param;

/**
 * 新币宝支付 查询会员信息
 * @author TX
 *
 */
public interface XbbzfPaymentDao {

	XbbzfPaymentEntity selectUserName(@Param(value = "uid") int uid);

	int insertXbbzfPaymentEntity(XbbzfPaymentEntity entity);
}
