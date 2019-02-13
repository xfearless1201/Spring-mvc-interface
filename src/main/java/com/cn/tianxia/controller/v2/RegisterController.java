package com.cn.tianxia.controller.v2;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cn.tianxia.controller.BaseController;
import com.cn.tianxia.po.BaseResponse;
import com.cn.tianxia.po.v2.RegisterResponse;
import com.cn.tianxia.service.v2.RegisterService;
import com.cn.tianxia.service.v2.ShortMessageService;
import com.cn.tianxia.util.IPTools;
import com.cn.tianxia.vo.v2.RegisterVO;

import net.sf.json.JSONObject;

/**
 * @Auther: zed
 * @Date: 2019/2/6 10:09
 * @Description: 用户注册controller
 */
@Controller
public class RegisterController extends BaseController {
    @Autowired
    private RegisterService registerService;
    @Autowired
    private ShortMessageService shortMessageService;

    private final static Logger logger = LoggerFactory.getLogger(RegisterController.class);


    /**
     * 账号异步验证
     *
     * @param request
     * @param userName
     * @param cagent
     * @param async
     * @return
     */
    @RequestMapping("User/asyncVerify")
    @ResponseBody
    public JSONObject asyncVerify(HttpServletRequest request, String userName, String cagent, String async) {
        if (StringUtils.isBlank(async) && !"1".equals(async)) {
            return BaseResponse.error(BaseResponse.ERROR_CODE, "error");
        }
        return registerService.verifyAccount(cagent, userName);
    }

    /**
     * 用户注册
     */
    @RequestMapping("User/register")
    @ResponseBody
    public JSONObject register(HttpSession session, HttpServletRequest request, HttpServletResponse response, RegisterVO registerVO) {
        try {
            String address = IPTools.getIpAddress(request);
            String loginIp = IPTools.getIp(request);
            String isImgCode = registerVO.getIsImgCode();
            String imgcode = registerVO.getImgcode();
            
            logger.info("调用注册接口请求参数报文:{}",registerVO.toString());
            if (!"0".equals(isImgCode)) {
                if (StringUtils.isBlank(imgcode)) {
                    return BaseResponse.error(BaseResponse.ERROR_CODE, "验证码不能为空！");
                }
            }

            Object simgcode = session.getAttribute("imgcode");// 验证码
            if (!ObjectUtils.allNotNull(simgcode)) {
                return BaseResponse.error(BaseResponse.ERROR_CODE, "请刷新图片验证码！");
            }

            // 获取验证来源域名是否属于该代理平台
            String refererUrl = request.getHeader("referer");

            if (StringUtils.isBlank(refererUrl)) {
                return BaseResponse.error(BaseResponse.ERROR_CODE, "来源域名不能为空！");
            }
            // 注册uuid
            String reguuid = registerVO.getReguuid();
            if (StringUtils.isBlank(reguuid)) {
                return RegisterResponse.error(RegisterResponse.ERROR_CODE,"010");
            }
            if (!regist.containsKey(reguuid)) {
                return RegisterResponse.error(RegisterResponse.ERROR_CODE,"010");
            }

            registerVO.setRefererUrl(refererUrl);
            registerVO.setSimgcode(String.valueOf(simgcode));
            registerVO.setLoginIp(loginIp);// 登录IP
            registerVO.setAddress(address);// 登录域名地址

            JSONObject result = registerService.register(registerVO);

            if (result.containsKey("status") && result.getString("status").equals("error")) {
                return result;
            }

            JSONObject returnToFront = registerSuccess(result,session,registerVO);

            return returnToFront;

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("用户注册异常：" + e.getMessage());
            return BaseResponse.error(BaseResponse.ERROR_CODE, "用户注册异常：" + e.getMessage());
        } finally {
            String reguuid = registerVO.getReguuid();
            if (StringUtils.isNotBlank(reguuid)) {
                regist.remove(reguuid);
            }
        }
    }

    /**
     * 用户手机注册
     */
    @RequestMapping("Mobile/register.do")
    @ResponseBody
    public JSONObject mobileRegister(HttpServletRequest request, HttpSession session, HttpServletResponse response,
                                     RegisterVO registerVO) {
        logger.info("用户手机注册Controller---------------------开始--------------------------");
        try {
            String address = IPTools.getIpAddress(request);
            String loginIp = IPTools.getIp(request);

            // 获取来源域名
            String refererUrl = request.getHeader("referer");

            if (StringUtils.isBlank(refererUrl)) {
                return BaseResponse.error(BaseResponse.ERROR_CODE, "来源域名不能为空！");
            }

            if (StringUtils.isBlank(registerVO.getMsgCode())) {
                registerVO.setMsgCode("1");
            }

            registerVO.setAddress(address);// 登录域名地址
            registerVO.setLoginIp(loginIp);// 登录IP
            registerVO.setRefererUrl(refererUrl);

            JSONObject result = registerService.register(registerVO);

            if (result.containsKey("status") && result.getString("status").equals("error")) {
                return result;
            }

            JSONObject returnToFront = registerSuccess(result,session,registerVO);

            //发送注册短信

            JSONObject sendResult = shortMessageService.sendRegisterSuccess(registerVO.getCagent(),registerVO.getMobileNo(),registerVO.getPassWord(),refererUrl);

            if (sendResult.containsKey("status") && "success".equals(sendResult.getString("status"))) {
                return returnToFront;
            } else {
                return RegisterResponse.error(RegisterResponse.ERROR_CODE,sendResult.getString("msg"));
            }


        } catch (Exception e) {
            e.printStackTrace();
            logger.error("用户手机注册异常：{}",e.getMessage());
            return RegisterResponse.error(RegisterResponse.ERROR_CODE,"用户手机注册异常" + e.getMessage());
        } finally {
            msgMap.remove(registerVO.getCagent() + registerVO.getMobileNo());
        }
    }

    private JSONObject registerSuccess(JSONObject result, HttpSession session,RegisterVO registerVO) {
        JSONObject dataJson = result.getJSONObject("msg");
        UUID uuid = UUID.randomUUID();
        String token = uuid.toString();
        Map<String,String> loginMap = new HashMap<>();
        loginMap.put("uid",dataJson.getString("uid"));
        loginMap.put("userkey", token);
        loginMap.put("userName",dataJson.getString("username"));
        loginMap.put("realname",dataJson.getString("realname"));
        loginMap.put("ag_username", dataJson.getString("ag_username"));
        loginMap.put("hg_username", dataJson.getString("hg_username"));
        loginMap.put("ag_password", dataJson.getString("ag_password"));
        loginMap.put("loginmobile", dataJson.getString("loginmobile"));
        loginMap.put("cagent",dataJson.getString("cagent"));
        loginMap.put("balance",dataJson.getString("balance"));
        loginMap.put("integral","0.00");
        loginMap.put("cid",dataJson.getString("cid"));
        loginMap.put("typeid",dataJson.getString("typeid"));
        loginMap.put("login_time",dataJson.getString("login_time"));
        loginMap.put("Transfer","0");
        loginMap.put("WithDraw","0");
        loginMap.put("sessionid",session.getId());
        loginMap.put("ip",registerVO.getLoginIp());
        loginMap.put("refurl",registerVO.getRefererUrl());
        loginMap.put("address",registerVO.getAddress());
        loginMap.put("isMobile",dataJson.getString("isMobile"));
        loginmaps.put(dataJson.getString("uid"),loginMap);
        session.setAttribute("uid", dataJson.getString("uid"));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userKey",token);
        jsonObject.put("userName",dataJson.getString("username"));
        jsonObject.put("balance",dataJson.getString("balance"));
        jsonObject.put("msg","success");
        return jsonObject;
    }

}
