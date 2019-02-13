package com.cn.tianxia.controller.v2;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cn.tianxia.controller.BaseController;
import com.cn.tianxia.po.BaseResponse;
import com.cn.tianxia.po.v2.JSONArrayResponse;
import com.cn.tianxia.service.v2.BankCardService;
import com.cn.tianxia.vo.v2.AddBankCardVO;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @ClassName BankController
 * @Description 银行卡接口
 * @author Hardy
 * @Date 2019年1月30日 下午8:30:57
 * @version 1.0.0
 */
@Controller
@RequestMapping("/User")
public class BankCardController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @Autowired
    private BankCardService bankCardService;

    /**
     * 添加银行卡
     *
     * @param request
     * @return
     */
    @RequestMapping("/addUserCard")
    @ResponseBody
    public JSONObject addUserCard(HttpServletRequest request, AddBankCardVO addBankCardVO) {
        logger.info("用户添加银行卡开始================start=================");
        try {
            //从缓存中获取用户ID
            Object obj = request.getSession().getAttribute("uid");
            if (!ObjectUtils.allNotNull(obj)) {
                logger.info("获取用户ID失败,用户登录超时");
                return BaseResponse.error("0","获取用户ID失败,用户登录超时");
            }
            String uid = String.valueOf(obj);
            addBankCardVO.setUid(uid);
            return bankCardService.addUserCard(addBankCardVO);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("用户添加银行卡异常:{}",e.getMessage());
            return BaseResponse.error("0", "用户添加银行卡异常");
        }
        
    }

    /**
     * 删除银行卡
     *
     * @param request
     * @param cardId   银行卡ID
     * @param password 取款密码
     * @return
     */
    @RequestMapping("/delUserCard")
    @ResponseBody
    public JSONObject delUserCard(HttpServletRequest request, String cardId, String password) {
        logger.info("用户删除银行卡开始================start=================");
        try {
            //从缓存中获取用户ID
            Object obj = request.getSession().getAttribute("uid");
            if (!ObjectUtils.allNotNull(obj)) {
                logger.info("获取用户ID失败,用户登录超时");
                return BaseResponse.error("0","获取用户ID失败,用户登录超时");
            }
            String uid = String.valueOf(obj);
            return bankCardService.delUserCard(uid,cardId,password);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("用户删除银行卡异常:{}",e.getMessage());
            return BaseResponse.error("0", "用户删除银行卡异常");
        }
        
        
    }

    /**
     * 获取银行卡信息
     *
     * @param request
     * @return
     */
    @RequestMapping("/getUserCard")
    @ResponseBody
    public JSONArray getUserCard(HttpServletRequest request) {
        logger.info("用户获取银行卡列表开始================start=================");
        try {
            //从缓存中获取用户ID
            Object obj = request.getSession().getAttribute("uid");
            if (!ObjectUtils.allNotNull(obj)) {
                logger.info("获取用户ID失败,用户登录超时");
                return JSONArrayResponse.faild("获取用户ID失败,用户登录超时");
            }
            String uid = String.valueOf(obj);
            return bankCardService.getUserCard(uid);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("用户获取银行卡列表异常:{}",e.getMessage());
            return JSONArrayResponse.faild("用户获取银行卡列表异常");
        }
    }
}
