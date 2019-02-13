package com.cn.tianxia.controller.v2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.cn.tianxia.controller.BaseController;
import com.cn.tianxia.util.IPTools;
import com.cn.tianxia.util.ValidateCode;

/**
 * 
 * @ClassName ValidateCodeController
 * @Description 验证码接口
 * @author Hardy
 * @Date 2019年2月8日 下午12:28:19
 * @version 1.0.0
 */
@Controller
public class ValidateCodeController extends BaseController{

    /**
     * 响应验证码页面
     * @return
     */
    @RequestMapping(value="validateCode")
    public void validateCode(HttpServletRequest request,HttpServletResponse response) throws Exception{
        // 设置响应的类型格式为图片格式
        response.setContentType("image/jpeg");
        //禁止图像缓存。
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        HttpSession session = request.getSession();
        logger.info(IPTools.getIpAddress(request) +"---validateCode获取session,id="+session.getId());
        ValidateCode vCode = new ValidateCode(100,30,4,30);
        String code=vCode.getCode();
        session.setAttribute("imgcode", code);
        logger.info(IPTools.getIpAddress(request) +"---生产验证码:"+session.getId()+"-----"+code);
        vCode.write(response.getOutputStream());
    }
}
