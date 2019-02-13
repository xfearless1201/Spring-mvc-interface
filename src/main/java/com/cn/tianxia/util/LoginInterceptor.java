package com.cn.tianxia.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.cn.tianxia.controller.BaseController;
import com.cn.tianxia.service.UserService;

/**
 * 登录认证的拦截器
 */
@Service
public class LoginInterceptor extends BaseController implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    private List<String> excludedUrls;

    public List<String> getExcludedUrls() {
        return excludedUrls;
    }

    public void setExcludedUrls(List<String> excludedUrls) {
        this.excludedUrls = excludedUrls;
    }

    /**
     * Handler执行完成之后调用这个方法
     */
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exc)
            throws Exception {

    }

    /**
     * Handler执行之后，ModelAndView返回之前调用这个方法
     */
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    /**
     * Handler执行之前调用这个方法
     */

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        logger.info("拦截器开始工作==============start==============");
        try {
            response.setContentType("application/json;charset=UTF-8");
            // 获取请求域名
            String url = request.getRequestURI();
            // 检查放行url,满足则直接放行
            if (!CollectionUtils.isEmpty(excludedUrls)) {
                for (String excludedUrl : excludedUrls) {
                    if (url.indexOf(excludedUrl) > 0) {
                        return true;
                    }
                }
            }

            // 判断是否需要填写验证码
            String isImgCode = request.getParameter("isImgCode");
            if (StringUtils.isNotBlank(isImgCode) && "0".equals(isImgCode)) {
                // 不需要输入验证码,输入默认的验证码
                Map requestParams = RequestUtil.getRequestMap(request);
                request.getSession().setAttribute("imgcode", "1234");
                List<String> list = new ArrayList<String>();
                list.add("1234");
                requestParams.put("imgcode", list);
            }

            String refurl = request.getHeader("referer");
            if (StringUtils.isBlank(refurl)) {
                printMessage(response,"非法域名,无权访问");
                return false;
            }
            String[] urls = refurl.split("/");
            String cagent = request.getParameter("cagent");
            String domainConfig = ObjectUtils.allNotNull(request.getSession().getAttribute("refurls"))
                    ? request.getSession().getAttribute("refurls").toString() : null;

            if (StringUtils.isBlank(domainConfig) || "[]".equals(domainConfig)) {
                List<Map<String, String>> list = userService.selectRefererUrl(urls[2], cagent);
                request.getSession().setAttribute("refurls", list.toString());
                domainConfig = list.toString();
            }

            if (domainConfig.indexOf(urls[2]) < 0) {
                logger.info("来源被拦截..地址:" + refurl + "---" + domainConfig);
                printMessage(response,"非法域名,无权访问");
                return false;
            }
            
            //判断用户是否
            String sessionid = request.getSession().getId();
            Object obj = request.getSession().getAttribute("uid");
            if(ObjectUtils.allNotNull(obj)){
                String uid = String.valueOf(obj);
                if(loginmaps.containsKey(uid)){
                    //从缓存中获取用户信息
                    Map<String, String> loginmap = loginmaps.get(uid);
                    if(loginmap.containsKey("sessionid") && sessionid.equals(loginmap.get("sessionid"))){
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            logger.info("拦截异常:{}", e.getMessage());
        }
        request.getSession().invalidate();
        printMessage(response,"无权操作,请先登录");
        return false;
    }

    public void printMessage(HttpServletResponse response,String message) throws IOException {
        PrintWriter pw =    response.getWriter();
        pw.print(message);
        pw.flush();
        pw.close();
    }
}
