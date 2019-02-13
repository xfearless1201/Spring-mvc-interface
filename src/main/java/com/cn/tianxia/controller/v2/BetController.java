package com.cn.tianxia.controller.v2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cn.tianxia.controller.BaseController;
import com.cn.tianxia.po.v2.JSONArrayResponse;
import com.cn.tianxia.service.v2.GameBetService;
import com.cn.tianxia.vo.v2.BetInfoVO;

import net.sf.json.JSONArray;

/**
 * 
 * @ClassName BetController
 * @Description 投注接口
 * @author Hardy
 * @Date 2019年1月30日 下午8:37:06
 * @version 1.0.0
 */
@Controller
@RequestMapping("/User")
public class BetController extends BaseController{
    @Autowired
    private GameBetService gameBetService;
    
    /**
     * 
     * @Description 获取用户注单信息
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/getBetInfo")
    @ResponseBody
    public JSONArray getBetInfo(HttpServletRequest request,HttpServletResponse response,@RequestParam(required=true) String type,
                                    @RequestParam(defaultValue="10",required =false)Integer pageSize,
                                    @RequestParam(defaultValue="1",required =false)Integer pageNo,
                                    @RequestParam(required=true)String bdate,
                                    @RequestParam(required=true)String edate){
        logger.info("查询用户注单信息列表开始=================START====================");
        try {
            //获取用户信息
            Object obj = request.getSession().getAttribute("uid");
            if(!ObjectUtils.anyNotNull(obj)){
                logger.info("获取用户ID失败,登录超时请重新登录");
                return JSONArrayResponse.faild("登录超时,请重新登录");
            }
            //转义用户ID
            String uid = String.valueOf(obj);
            BetInfoVO betInfoVO = new BetInfoVO();
            betInfoVO.setUid(uid);
            betInfoVO.setBdate(bdate);
            betInfoVO.setEdate(edate);
            betInfoVO.setPageNo(pageNo);
            betInfoVO.setPageSize(pageSize);
            betInfoVO.setType(type);
            return gameBetService.getGameBetInfo(betInfoVO);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("查询用户注单信息列表异常:{}",e.getMessage());
            return JSONArrayResponse.faild("查询用户注单信息列表异常");
        }
    }
}
