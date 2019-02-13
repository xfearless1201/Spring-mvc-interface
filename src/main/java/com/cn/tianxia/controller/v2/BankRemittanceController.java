package com.cn.tianxia.controller.v2;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cn.tianxia.common.CacheKeyConstants;
import com.cn.tianxia.controller.BaseController;
import com.cn.tianxia.po.BankRemittanceResponse;
import com.cn.tianxia.po.v2.JSONArrayResponse;
import com.cn.tianxia.service.v2.BankRemittanceService;
import com.cn.tianxia.vo.BankRemittanceVO;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @ClassName BankRemittanceController
 * @Description 银行汇款接口
 * @author Hardy
 * @Date 2019年2月4日 下午2:07:53
 * @version 1.0.0
 */
@Controller
@RequestMapping("bk")
public class BankRemittanceController extends BaseController{
    
    @Autowired
    private BankRemittanceService bankRemittanceService;
    
    
    /**
     * 
     * @Description 获取线下银行汇款配置信息
     * @param request
     * @param session
     * @return
     */
    @RequestMapping("getBankList.do") 
    @ResponseBody
    public JSONArray getBankList(HttpServletRequest request,HttpSession session) {
        logger.info("调用获取线下银行汇款配置信息业务开始==============START==================");
        try {
            Object obj = request.getSession().getAttribute("uid");
            if (!ObjectUtils.allNotNull(obj)) {
                logger.info("获取用户ID失败,用户登录超时");
                return JSONArrayResponse.faild("获取用户ID失败,用户登录超时");
            }
            String uid = String.valueOf(obj);
            return bankRemittanceService.getRemittanceBankInfo(uid);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用获取线下银行汇款配置信息业务异常:{}",e.getMessage());
            return JSONArrayResponse.faild("调用获取线下银行汇款配置信息业务异常");
            
        }
    }
    /**
     *  用户在线存款接口
     * @param request
     * @param session
     * @param bid
     * @param name
     * @param account
     * @param amount
     * @param ctime
     * @param type
     * @param caijin
     * @return
     */
    @RequestMapping("BankPay.do") 
    @ResponseBody
    public JSONObject BankPay(HttpServletRequest request,HttpSession session,String bid,String name,String account,double amount,String ctime,String type,String caijin) {  

        logger.info("银行汇款开始=================start==================");
        Object obj = session.getAttribute("uid");
        if(!ObjectUtils.allNotNull(obj)){
            return BankRemittanceResponse.error("获取用户ID为空,请重新登录");
        }
        String uid = String.valueOf(obj);
        //防止重复提交
        String key = CacheKeyConstants.BANK_REMITTANCE_KEY_UID + uid;
        if(userPayMap.containsKey(key)){
            //银行汇款订单已存在
            logger.info("银行汇款订单正在处理中,请等待....");
            return BankRemittanceResponse.error("银行汇款订单正在处理中,请等待....");
        }
        userPayMap.put(key, uid);
        try {
            //判断请求参数
            if(StringUtils.isBlank(name)){
                return BankRemittanceResponse.error("转账账户不能为空");
            }
            if(StringUtils.isBlank(account)){
                return BankRemittanceResponse.error("转账账号不能为空");
            }
            if(StringUtils.isBlank(ctime)){
                return BankRemittanceResponse.error("转账时间不能为空");
            }
            
            if(StringUtils.isBlank(caijin)){
                return BankRemittanceResponse.error("是否申请彩金不能为空");
            }
            Date remittanceDate = new SimpleDateFormat("yyyy-MM-dd").parse(ctime);
            //转账时间
            BankRemittanceVO bankRemittanceVO = new BankRemittanceVO();
            bankRemittanceVO.setBid(bid);
            bankRemittanceVO.setUid(uid);
            bankRemittanceVO.setAccount(account);
            bankRemittanceVO.setName(name);
            bankRemittanceVO.setAmount(amount);
            bankRemittanceVO.setType(type);
            bankRemittanceVO.setRemittanceDate(remittanceDate);
            bankRemittanceVO.setType(type);
            bankRemittanceVO.setCaijin(caijin);
            return bankRemittanceService.createRemittanceOrder(bankRemittanceVO);
        } catch (Exception e) {
            e.printStackTrace();
            return BankRemittanceResponse.error("银行汇款异常");
        }finally {
            if (userPayMap.containsKey(key)) {
                userPayMap.remove(key);
            }
        }
    }
}
