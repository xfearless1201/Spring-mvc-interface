package com.cn.tianxia.service.v2;

import com.cn.tianxia.vo.v2.TreasureRecordVO;
import net.sf.json.JSONObject;

/**
 * @Auther: zed
 * @Date: 2019/2/1 10:57
 * @Description: 资金流水记录查询Service
 */
public interface TreasureRecordService {
    JSONObject findAllByPage(TreasureRecordVO treasureRecordVO);
}
