package com.cn.tianxia.controller.v2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.cn.tianxia.po.BaseResponse;
import com.cn.tianxia.service.v2.DepositRecordService;
import com.cn.tianxia.vo.v2.DepositRecordVO;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cn.tianxia.controller.BaseController;

import net.sf.json.JSONArray;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Hardy
 * @version 1.0.0
 * @ClassName DepositRecordController
 * @Description 存款记录接口
 * @Date 2019年1月31日 下午9:25:02
 */
@Controller
@RequestMapping("/User")
public class DepositRecordController extends BaseController {

    @Autowired
    private DepositRecordService depositRecordService;

    @RequestMapping("/getReChargeInfo")
    @ResponseBody
    public JSONArray getReChargeInfo(HttpServletRequest request, HttpServletResponse response,
                                     String status, String Type,
                                     @RequestParam() String bdate,
                                     @RequestParam() String edate,
                                     @RequestParam(defaultValue = "10", required = false) int pageSize,
                                     @RequestParam(defaultValue = "1", required = false) int pageNo) {

        logger.info("查询用户存款记录开始==================START=======================");

        try {
            HttpSession session = request.getSession();
            Object sessionUid = session.getAttribute("uid");
            if ( !ObjectUtils.allNotNull(sessionUid)) {
                logger.info("查询用户存款记录异常:用户未登录");
                return JSONArray.fromObject(BaseResponse.error(BaseResponse.ERROR_CODE,"查询用户存款记录异常:用户未登录"));
            }
            String uid = sessionUid.toString();
            DepositRecordVO depositRecordVO = new DepositRecordVO();
            depositRecordVO.setUid(uid);
            depositRecordVO.setType(Type);
            depositRecordVO.setBdate(bdate);
            depositRecordVO.setEdate(edate);
            depositRecordVO.setStatus(status);
            depositRecordVO.setPageNo(pageNo);
            depositRecordVO.setPageSize(pageSize);

            return depositRecordService.findAllByPage(depositRecordVO);

        } catch (Exception e) {
            logger.info("查询用户存款记录异常:{}", e.getMessage());
            return JSONArray.fromObject(BaseResponse.error(BaseResponse.ERROR_CODE,"查询用户存款记录异常:" + e.getMessage()));
        }
    }
}
