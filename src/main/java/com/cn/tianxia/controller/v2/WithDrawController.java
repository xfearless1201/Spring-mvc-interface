package com.cn.tianxia.controller.v2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.cn.tianxia.po.BaseResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cn.tianxia.controller.BaseController;
import com.cn.tianxia.po.WithDrawResponse;
import com.cn.tianxia.service.v2.WithDrawService;
import com.cn.tianxia.vo.CreateWithDrawOrderVO;

import net.sf.json.JSONObject;

/**
 * @author Hardy
 * @version 1.0.0
 * @ClassName WithDrawController
 * @Description 会员提现
 * @Date 2019年1月29日 下午2:44:47
 */
@RequestMapping("/User")
@Controller
public class WithDrawController extends BaseController {

    @Autowired
    private WithDrawService withDrawService;

    /**
     * @param session
     * @param request
     * @param response
     * @param credit
     * @param cardid
     * @param password
     * @param poundage
     * @param administrative_fee
     * @param withdrawConfig
     * @return
     * @throws Exception
     * @Description 提现接口
     */
    @RequestMapping("/WithDraw")
    @ResponseBody
    public JSONObject WithDraw(HttpSession session, HttpServletRequest request,
                               HttpServletResponse response, int credit, String cardid, String password, Double poundage,
                               Double administrative_fee, Double withdrawConfig) {
        logger.info("会员提现接口开始===============START======================");
        Object obj = session.getAttribute("uid");
        if (!ObjectUtils.anyNotNull(obj)) {
            logger.info("从缓存中获取用户ID失败,登录过期,请重新登录");
            return WithDrawResponse.faild("获取用户ID失败,请重新登录");
        }
        String uid = String.valueOf(obj);

        //key缓存
        String key = "USER:WITHDRAW:" + uid;
        if (gameMap.containsKey(key)) {
            String oldCardid = gameMap.get(key).split(",")[0];
            long times = Long.parseLong(gameMap.get(key).split(",")[1]);
            long nowTimes = System.currentTimeMillis();
            if (cardid.equals(oldCardid) && nowTimes - times < 3000) {
                return WithDrawResponse.faild("提现订单正在处理中,请稍等....");
            }
        }
        gameMap.put(key, cardid + "," + String.valueOf(System.currentTimeMillis()));
        try {
            //校验请求参数
            JSONObject params = checkRequestParams(credit, cardid, password, withdrawConfig);
            if (params.containsKey("code") && "0".equals(params.getString("code"))) {
                return params;
            }
            synchronized (this) {
                CreateWithDrawOrderVO createWithDrawOrderVO = new CreateWithDrawOrderVO();
                createWithDrawOrderVO.setCardid(cardid);
                createWithDrawOrderVO.setCredit(credit);
                createWithDrawOrderVO.setPassword(password);
                createWithDrawOrderVO.setPoundage(poundage);
                createWithDrawOrderVO.setUid(uid);
                createWithDrawOrderVO.setWithdrawConfig(withdrawConfig);
                createWithDrawOrderVO.setAdministrative_fee(administrative_fee);
                //发起提现请求
                return withDrawService.createWithDrawOrder(createWithDrawOrderVO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("会员创建提现订单异常:{}", e.getMessage());
            return WithDrawResponse.faild("会员创建提现订单异常");
        } finally {
            if (withDrawMap.containsKey(key)) {
                logger.info("删除缓存key:{}", key);
                withDrawMap.remove(key);
            }
        }
    }

    /**
     * @param request
     * @param response
     * @Description 获取用户打码量，游戏流水，强制提款手续费
     */
    @RequestMapping("/selectWithdrawConfig")
    @ResponseBody
    public JSONObject selectWithdrawConfig(HttpServletRequest request, HttpServletResponse response) {
        logger.info("获取用户打码量、游戏流水、强制提款金额---------------------开始-----------------------");
        try {
            HttpSession session = request.getSession();
            Object uidAttr = session.getAttribute("uid");
            if (!ObjectUtils.allNotNull(uidAttr)) {
                return BaseResponse.error( BaseResponse.ERROR_CODE , "用户未登录");
            }
            String uid = uidAttr.toString();

            return withDrawService.selectWithdrawConfig(uid);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("获取用户打码量、游戏流水、强制提款手续费接口异常:{}" + e.getMessage());
            return BaseResponse.error(BaseResponse.ERROR_CODE, "获取用户打码量、游戏流水、强制提款手续费接口异常:" + e.getMessage());
        }
    }


    /**
     * @param credit
     * @param cardid
     * @param password
     * @param poundage
     * @param administrative_fee
     * @param withdrawConfig
     * @return
     * @Description 校验请求参数
     */
    private JSONObject checkRequestParams(int credit, String cardid, String password, Double withdrawConfig) {

        if (credit < 100 || credit > 500000) {
            return WithDrawResponse.faild("提款金额请输入大于100小于500000之间的金额");
        }

        if (StringUtils.isBlank(cardid)) {
            return WithDrawResponse.faild("提现银行卡号不能为空");
        }

        if (StringUtils.isBlank(password)) {
            return WithDrawResponse.faild("提现密码不能为空");
        }

        return WithDrawResponse.success("参数校验成功");
    }
}
