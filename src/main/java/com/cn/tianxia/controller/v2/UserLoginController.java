package com.cn.tianxia.controller.v2;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.cn.tianxia.common.v2.SystemConfigLoader;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cn.tianxia.common.v2.KeyConstant;
import com.cn.tianxia.controller.BaseController;
import com.cn.tianxia.po.BaseResponse;
import com.cn.tianxia.po.v2.LoginResponse;
import com.cn.tianxia.service.v2.UserLoginService;
import com.cn.tianxia.util.IPTools;
import com.cn.tianxia.vo.v2.UserLoginVO;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName UserLoginController
 * @Description 用户登录接口
 * @author Hardy
 * @Date 2019年2月6日 下午2:52:56
 * @version 1.0.0
 */
@Controller
public class UserLoginController extends BaseController{
    
    @Autowired
    private UserLoginService userLoginService;
    @Autowired
    private SystemConfigLoader systemConfigLoader;

    /**
     * 
     * @Description 用户登录接口
     * @param request
     * @param response
     * @param tname
     * @param tpwd
     * @param savelogin
     * @param imgcode
     * @param isMobile
     * @param isImgCode
     * @return
     */
    @RequestMapping("login.do")
    @ResponseBody
    public JSONObject pcLogin(HttpServletRequest request, HttpServletResponse response,String tname,String tpwd,
                                                    String savelogin,String imgcode,String isMobile,String isImgCode){
        logger.info("调用用户登录接口开始=================START==================");
        try {
            //获取当前sessionID
            String sessionId = request.getSession().getId();
            //从缓存中获取用户ID
            Object uidObj = request.getSession().getAttribute("uid");
            if(ObjectUtils.allNotNull(uidObj)){
                String uid = String.valueOf(uidObj);
                if(loginmaps.containsKey(uid)){
                    Map<String,String> cacheMap = loginmaps.get(uid);
                    if(cacheMap.containsKey("sessionid") && sessionId.equalsIgnoreCase(cacheMap.get("sessionid"))){
                        JSONObject data = new JSONObject();
                        data.put("status", "ok");
                        data.put("userKey", cacheMap.get("userkey"));
                        data.put("userName",cacheMap.get("userName"));
                        data.put("balance", cacheMap.get("balance"));
                        data.put("integral",cacheMap.get("integral"));
                        return data;
                    }
                }
            }
            
            //判断请求参数
            if(StringUtils.isBlank(tname)){
                logger.info("请求数据异常:用户登录账号不能为空--->>>tname:{}",tname);
                return LoginResponse.faild("0", "请求数据异常:登录用户名不能为空");
            }
            
            if(StringUtils.isBlank(tpwd)){
                logger.info("请求数据异常:用户登录密码不能为空--->>>tpwd:{}",tpwd);
                return LoginResponse.faild("0", "请求数据异常:用户登录密码不能为空");
            }
            
            if(StringUtils.isBlank(isMobile)){
                isMobile = "0";//PC
            }
            
            //判断是否需要验证验证码,从配置文件中读取权限
            boolean isValidCode = true;//默认为真
            String controlUser = systemConfigLoader.getProperty(KeyConstant.CONTROL_USER_KEY);
            if(controlUser.equals(tname)){
                isValidCode = false;
            }
            
            if(isValidCode && !"0".equals(isImgCode)){
                //需要验证验证码
                Object simgcodeObj = request.getSession().getAttribute("imgcode");//从缓存中获取验证码
                if(ObjectUtils.allNotNull(simgcodeObj)){
                    String simgcode = String.valueOf(simgcodeObj);
                    if(StringUtils.isBlank(imgcode)){
                        logger.info("请求参数异常:验证码不能为空--->>>imgcode:{}",imgcode);
                        return LoginResponse.faild("0", "请求参数异常:验证码不能为空");
                    }
                    
                    if(!simgcode.equalsIgnoreCase(imgcode)){
                        logger.info("请求参数异常:验证码输入不正确--->>>imgcode:{}",imgcode);
                        return LoginResponse.faild("0", "请求参数异常:验证码输入不正确");
                    }
                }else{
                    logger.info("从缓存中获取验证码失败");
                    return LoginResponse.faild("0", "获取验证码失败,请重新刷新图片验证码");
                }
            }
            
            //获取请求IP 和 请求域名
            String refurl = request.getHeader("referer");
            if(StringUtils.isBlank(refurl)){
                logger.info("获取请求域名失败");
                return LoginResponse.faild("0", "域名不匹配,获取请求域名失败");
            }
            String ip = IPTools.getIp(request);
            if(StringUtils.isBlank(ip)){
                ip = "127.0.0.1";
            }
            String address = IPTools.getIpAddress(request);
            UserLoginVO userLoginVO = new UserLoginVO();
            userLoginVO.setUsername(tname);
            userLoginVO.setPassword(tpwd);
            userLoginVO.setIsMobile(isMobile);
            userLoginVO.setAddress(address);
            userLoginVO.setRefurl(refurl);
            JSONObject result = userLoginService.login(userLoginVO);
            if(result.containsKey("status") && "ok".equalsIgnoreCase(result.getString("status"))){
                //登录成功,防止重复登录
                Map<String, String> loginmap = new HashMap<>();
                loginmap.put("sessionid", sessionId);
                loginmap.put("ip", ip);
                loginmap.put("refurl",refurl);
                loginmap.put("address", address);
                loginmap.put("isMobile", isMobile);
                //从返回结果中删除缓存json
                JSONObject cacheJson = (JSONObject) result.remove("cacheJson");
                Iterator<String> iterator = cacheJson.keys();
                while(iterator.hasNext()){
                    String key = iterator.next();
                    Object obj = cacheJson.get(key);
                    loginmap.put(key, String.valueOf(obj));
                    request.getSession().setAttribute(key, obj);
                }
                //登陆用户session
                loginmaps.put(loginmap.get("uid"), loginmap);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用用户登录接口异常:{}",e.getMessage());
            return LoginResponse.faild("0", "调用用户登录接口异常");
        }finally {
            request.getSession().setAttribute("isreg", "");
            request.getSession().setAttribute("imgcode", "");
        }
    }
    
    
    /**
     * 
     * @Description 检查是否登录
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping("checklogin.do")
    @ResponseBody
    public  JSONObject CheckLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("调用检查是否登录接口开始=================START======================");
        try {
            JSONObject data = new JSONObject();
            //获取sessionId
            String sessionId = request.getSession().getId();
            //从缓存中获取用户ID
            Object obj = request.getSession().getAttribute("uid");
            if(ObjectUtils.allNotNull(obj)){
                String uid = String.valueOf(obj);
                
                //通过用户ID获取缓存信息
                Map<String,String> cacheMap = loginmaps.get(uid);
                //判断sessionId是否正确
                if(cacheMap.containsKey("sessionid") && sessionId.equalsIgnoreCase(cacheMap.get("sessionid"))){
                    //用户已登录
                    data.put("userkey", cacheMap.get("userkey"));
                    data.put("userName",cacheMap.get("userName"));
                    data.put("balance", cacheMap.get("balance"));
                    data.put("integral",cacheMap.get("integral"));
                    data.put("msg", "success");
                    return data;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用检查是否登录接口异常:{}",e.getMessage());
        }
        //非登录
        request.getSession().invalidate();
        return BaseResponse.faild("0", "faild");
    }

    /**
     * 退出系统
     * @param session
     * @return
     * @throws Exception
     */
    @RequestMapping("logout.do")
    @ResponseBody
    public JSONObject logout(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws Exception{

        Cookie[] cookies = request.getCookies();
        if (null!=cookies) {
            for(Cookie cookie : cookies){
                cookie.setValue(null);
                cookie.setMaxAge(0);// 立即销毁cookie
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                response.addCookie(cookie);
            }
        }
        //从缓存中获取用户ID
        Object obj = request.getSession().getAttribute("uid");
        if(ObjectUtils.allNotNull(obj)){
            String uid = String.valueOf(obj);
            //删除loginMap中uid信息
            loginmaps.remove(uid);
        }
        //清除Session
        session.invalidate();
        JSONObject jo=new JSONObject();
        return jo;
    }
}
