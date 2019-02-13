package com.cn.tianxia.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cn.tianxia.common.PayConstant;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayProperties;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.common.VersionConstant;
import com.cn.tianxia.json.JSONUtils;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.impl.BFTPayServiceImpl;
import com.cn.tianxia.pay.impl.GSTPayServiceImpl;
import com.cn.tianxia.pay.impl.HTPayServiceImpl;
import com.cn.tianxia.pay.impl.SHBPayServiceImpl;
import com.cn.tianxia.pay.impl.TCPPayServiceImpl;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.ys.util.DateUtil;
import com.cn.tianxia.po.BaseResponse;
import com.cn.tianxia.service.UserService;
import com.cn.tianxia.service.v2.PlatPaymentService;
import com.cn.tianxia.service.v2.PlatformPayService;
import com.cn.tianxia.util.FileLog;
import com.cn.tianxia.util.IPTools;
import com.cn.tianxia.util.JDBCTools;
import com.cn.tianxia.vo.BankPayVO;
import com.cn.tianxia.vo.ScanPayVO;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @Description:TODO
 * @author:zouwei
 * @time:2017年7月9日 下午3:24:04
 */
@RequestMapping("PlatformPay")
@Controller
@Scope("prototype")
public class PlatformPayController extends BaseController {

    @Resource
    private UserService userService;

    @Autowired
    private PlatPaymentService platPaymentService;

    @Autowired
    private PlatformPayService platformPayService;

    private String ret_str_success = "success";
    private String ret_str_failed = "fail";
    private String t_trade_status; // 商户交易状态

    /**
     * 获取支付渠道
     * @param request
     * @param session
     * @param response
     * @return
     */
//    @RequestMapping("paymentChannel")
//    @ResponseBody
    public JSONObject getPaymentChannel(HttpServletRequest request, HttpSession session, HttpServletResponse response) {

        if ("2".equals(VersionConstant.VERSION_CODE)) {
            return newGetPaymentChannel(session);
        }

        JSONObject retJson = new JSONObject();
        String uid = session.getAttribute("uid").toString();

        if (StringUtils.isNullOrEmpty(uid)) {
            retJson.put("status", "error");
            retJson.put("code", "1001");
            retJson.put("channel", "");
            retJson.put("msg", "用户尚未登录系统！");
            return retJson;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        Map<String, String> userChannel = userService.selectUserPaychannel(map);

        if (null == userChannel || userChannel.size() <= 0) {
            retJson.put("status", "error");
            retJson.put("code", "1002");
            retJson.put("channel", "");
           retJson.put("msg", "渠道数据为空！");
            return retJson;
        }

        String[] channel = userChannel.get("payment_channel").toString().split(",");
        if (null == channel || channel.length == 0 || StringUtils.isNullOrEmpty(channel[0])) {
            retJson.put("status", "error");
            retJson.put("code", "1003");
            retJson.put("channel", "");
            retJson.put("msg", "未设置渠道数据！");
            return retJson;
        }
        //排序
/*        List<Integer> oldchannel = new ArrayList<>();
        List<Integer> newchannel = new ArrayList<>();
        List<Integer> newchannel2 = new ArrayList<>();*/
        /*for (int i = 0; i < channel.length; i++) {
            if(!channel[i].equals("21")  && !channel[i].equals("29") && !channel[i].equals("4") && !channel[i].equals("3")&& !channel[i].equals("7")&& !channel[i].equals("1")&& !channel[i].equals("6")) {
                oldchannel.add(Integer.parseInt(channel[i]));
            }else if(channel[i].equals("21")){
                newchannel2.add(Integer.parseInt(channel[i]));
            }else {
                newchannel.add(Integer.parseInt(channel[i]));
            }
        }
        List<Integer> listnew = new ArrayList<>();
        for(int i = 0; i < newchannel.size(); i++) {
            listnew.add(newchannel.get(i));
        }
        for(int i = 0; i < oldchannel.size(); i++) {
            listnew.add(oldchannel.get(i));
        }
        for(int i = 0; i < newchannel2.size(); i++) {
            listnew.add(newchannel2.get(i));
        }*/
       /* for (int i = 0; i < oldchannel.size(); i++) {
            if(oldchannel.get(i)==8) {
                newchannel.add(0, oldchannel.get(i));
                oldchannel.remove(i);
            }
            if(oldchannel.get(i)==9) {
                newchannel.add(1, oldchannel.get(i));
                oldchannel.remove(i);
            }
            if(oldchannel.get(i)==10) {
                newchannel.add(2, oldchannel.get(i));
                oldchannel.remove(i);
            }
            if(oldchannel.get(i)==11) {
                newchannel.add(3, oldchannel.get(i));
                oldchannel.remove(i);
            }
            if(oldchannel.get(i)==6) {
                newchannel.add(4, oldchannel.get(i));
                oldchannel.remove(i);
            }
        }*/
       
        List mbchannel = new ArrayList<>();
        List pcchannel = new ArrayList<>();
        for (int i = 0; i < channel.length; i++) {
            if (Integer.parseInt(channel[i]) < 20) {
                pcchannel.add(Integer.parseInt(channel[i]));
            } else {
                mbchannel.add(Integer.parseInt(channel[i]));
            }
        }
       /* for (int i = 0; i < channel.length; i++) {
            if (Integer.parseInt(channel[i]) < 20) {
                pcchannel.add(Integer.parseInt(channel[i]));
            } else {
                mbchannel.add(Integer.parseInt(channel[i]));
            }
        }*/

        retJson.put("code", "1000");
        retJson.put("status", "success");
        retJson.put("PCchannel", JSONUtils.toJSONArray(pcchannel));
        retJson.put("MBchannel", JSONUtils.toJSONArray(mbchannel));
        retJson.put("msg", "支付渠道数据获取成功！");
        return retJson;
    }

    /**
     * 改造获取可用支付列表
     * @param session 从session中获取uid
     * @return
     */
    private JSONObject newGetPaymentChannel(HttpSession session) {
        Object uid = session.getAttribute("uid");
        if (!ObjectUtils.allNotNull(uid)) {
            return BaseResponse.error("1001","用户未登陆！");
        }
        return platPaymentService.getPaymentChannel(uid.toString());
    }

//    @RequestMapping("getPaymentList")
//    @ResponseBody
    public JSONObject getPaymentList(HttpServletRequest request, HttpSession session, HttpServletResponse response,
            String type) {
        if ("2".equals(VersionConstant.VERSION_CODE)) {
            return newGetPaymentList(session,type);
        }
        JSONObject retJson = new JSONObject();
        retJson.put("status", "error");
        retJson.put("code", "1000");
        retJson.put("msg", "接口异常！");
        if(!ObjectUtils.allNotNull(session.getAttribute("uid"))){
            retJson.put("status", "error");
            retJson.put("code", "1001");
            retJson.put("msg", "参数异常type:" + type + "  uid" + "");
            return retJson;
        }
        String uid = session.getAttribute("uid").toString();

        Map<String, Object> typeMap = new HashMap<String, Object>(2);
        typeMap.put("uid", uid);
        typeMap.put("typeid", type);
        
        List<Map<String, String>> paymentIds = userService.selectUserTypeById(typeMap);
        if (paymentIds.size() <= 0) {
            retJson.put("status", "error");
            retJson.put("code", "1002");
            retJson.put("msg", "该分层没有可用支付商！");
            return retJson;
        }
        
        // TODO 数据库现在数据类型为Int
        
      //  String onlinepay_ids = String.valueOf(paymentIds.get(0).get("onlinepay_id"));
        List ids = new ArrayList();
        for(Map idmap : paymentIds) {
        	ids.add(idmap.get("payment_id"));
        }
        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("ids", ids);

        List<Map<String, String>> payList = userService.selectUserPaymentList(params);
        if (payList.size() <= 0) {
            retJson.put("status", "error");
            retJson.put("code", "1003");
            retJson.put("msg", "没有可用支付商列表！");
            return retJson;
        }
        // ArrayList list = new ArrayList<>();

        JSONArray jsonArr = new JSONArray();
        // logger.info("支付商总数:" + payList.size());
        boolean result = false;
        for (int i = 0; i < payList.size(); i++) {
            Map<String, String> map = payList.get(i);
            String[] types = map.get("type").split(",");
            for (int j = 0; j < types.length; j++) {
                if (type.equals(types[j])) {
                    result = true;
                }
            }
            if (!result)
                continue;
            result = false;
            JSONObject json = JSONObject.fromObject(map);

            JSONObject jo = new JSONObject();
            jo.put("paymentName", json.get("payment_name").toString());
            jo.put("id", json.get("id").toString());
            // jo.put("dayquota", json.getInt("dayquota"));
            if ("1".equals(type) || "5".equals(type)) {
                jo.put("minquota", json.getInt("minquota"));
                jo.put("maxquota", json.getInt("maxquota"));
            } else if ("2".equals(type) || "6".equals(type)) {
                jo.put("minquota", json.getInt("ali_minquota"));
                jo.put("maxquota", json.getInt("ali_maxquota"));
            } else if ("3".equals(type) || "7".equals(type)) {
                jo.put("minquota", json.getInt("wx_minquota"));
                jo.put("maxquota", json.getInt("wx_maxquota"));
            } else if ("4".equals(type) || "8".equals(type)) {
                jo.put("maxquota", json.getInt("qrmaxquota"));
                jo.put("minquota", json.getInt("qrminquota"));
            } else if ("9".equals(type) || "10".equals(type)) {
                jo.put("maxquota", json.getInt("yl_maxquota"));
                jo.put("minquota", json.getInt("yl_minquota"));
            } else if ("11".equals(type) || "12".equals(type)) {// 11pc京东扫码，12手机端京东扫码
                jo.put("maxquota", json.getInt("jd_maxquota"));
                jo.put("minquota", json.getInt("jd_minquota"));
            } else if ("13".equals(type) || "14".equals(type)) {// 13pc端快捷，14手机端快捷
                jo.put("maxquota", json.getInt("kj_maxquota"));
                jo.put("minquota", json.getInt("kj_minquota"));
            } else if ("15".equals(type) || "16".equals(type)) {// 15 PC微信条码 16
                                                                // 手机微信条码
                jo.put("maxquota", json.getInt("wxtm_maxquota"));
                jo.put("minquota", json.getInt("wxtm_minquota"));
            } else if ("17".equals(type) || "18".equals(type)) {// 17 PC支付宝条码 18
                                                                // 手机支付宝条码
                jo.put("maxquota", json.getInt("alitm_maxquota"));
                jo.put("minquota", json.getInt("alitm_minquota"));
            }
            // 验证当日金额是否超过限制
//            if (!maxTodayAmount(uid, json.getInt("dayquota"),json.get("payment_name").toString())) {
//                // logger.info("验证通过当日金额:" + json.getInt("dayquota"));
//
//            }
            jsonArr.add(jo);
        }
        if (jsonArr.size() > 0) {
            retJson.put("status", "success");
            retJson.put("typeList", jsonArr);
            retJson.put("code", "1005");
            retJson.put("msg", "接口获取成功！");
        } else {
            retJson.put("status", "error");
            retJson.put("typeList", jsonArr);
            retJson.put("code", "1003");
            retJson.put("msg", "没有可用支付商列表！");
            return retJson;
        }
        return retJson;
    }

    /**
     * 改造获取支付商列表
     * @param session 从session中获取uid
     * @param type 支付类型
     * @return
     */
    private JSONObject newGetPaymentList(HttpSession session, String type) {
        Object uid = session.getAttribute("uid");
        if (!ObjectUtils.allNotNull(uid)) {
            return BaseResponse.error("1001","用户未登陆！");
        }
        return platPaymentService.getPaymentList(uid.toString(),type);
    }

    /**
     * // 网银支付验证 // 参数topay=SHB支付商代码 &acounmt=10支付金额 &bankcode=ICBC 支付银行代码
     * 
     * @param request
     * @param response
     * @return
     */
    public Map<String, Object> check(HttpServletRequest request, HttpSession session, HttpServletResponse response){
        logger.info("获取支付请求参数开始===========================START================================");
        // 初始化返回数据
        Map<String,Object> returnMap = new HashMap<String, Object>();
        returnMap.put("msg", "error");
        try {
            StringBuffer url = request.getRequestURL();
            String tempContextUrl = url.delete(url.length() - request.getRequestURI().length(), url.length())
                    .append("/").toString();
            String uid = session.getAttribute("uid").toString();//用户ID
            double amount = Double.parseDouble(request.getParameter("acounmt").toString());// 支付金额
            //支付入口方式,网银支付、扫码支付....
            String[] ParmList = new String[] {"bankcode", "acounmt", "topay", "scancode", "isApp" };
            // 扫描支付添加scancode
            if (request.getParameterMap().containsKey(ParmList[3])) {
                String scancode = request.getParameter("scancode").toString();
                // 扫描支付
                if (!"".equals(scancode) && null != scancode
                        && (scancode.equals("wx") || scancode.equals("ali") || scancode.equals("cft")
                                || scancode.equals("yl") || scancode.equals("jd") || scancode.equals("kj")
                                || scancode.equals("wxtm") || scancode.equals("alitm"))) {
                    returnMap.put("scancode", scancode);
                } else {
                    logger.info("扫码支付渠道不能为空!");
                    returnMap.put("errorMsg", "扫码支付渠道不能为空!");
                    return returnMap;
                }
            }
            // 网银支付添加Bankcode
            if (request.getParameterMap().containsKey(ParmList[0])) {
                String bankcode = request.getParameter("bankcode").toString();
                // 扫描支付
                if (!"".equals(bankcode) && null != bankcode) {
                    returnMap.put("bankcode", bankcode);
                } else {
                    logger.info("网银支付银行编码不能为空!");
                    returnMap.put("errorMsg", "网银支付银行编码不能为空!");
                    return returnMap;
                }
            }
            //查询用户信息
            Map<String,Object> userMap = userService.findUserInfoByUid(Integer.parseInt(uid));
            if(userMap == null || userMap.isEmpty()){
                logger.info("非法用户,查询用户信息失败,用户ID:{},用户不存在!",uid);
                returnMap.put("errorMsg", "用户ID:["+uid+"不存在,查询失败!]");
                return returnMap;
            }
            
            String cagent = userMap.get("cagent").toString();//平台编码
            String cid = userMap.get("cid").toString();//平台ID
            String username = userMap.get("username").toString();//用户名
            //查询支付商信息
            String payId = request.getParameter("payId");
            if (StringUtils.isNullOrEmpty(payId)) {
                logger.info("支付商ID不能为空!");
                returnMap.put("errorMsg", "支付商ID:["+payId+"]不存在,查询结果为空!");
                return returnMap;
            }
            List<Map<String, String>> hsConfiglist = userService.selectYsepaybyId(payId,uid);
            if (null == hsConfiglist || hsConfiglist.size() == 0) {
                logger.info("查询支付配置信息失败,支付商ID:{}",payId);
                returnMap.put("errorMsg", "查询支付配置信息失败!");
                return returnMap;
            }
            // 获取配置信息
            Map<String, String> hsConfigMap = hsConfiglist.get(0);
            // 获取用户所属支付商
            String topay = hsConfigMap.get("payment_name").toString();
            //支付商配置信息
            String PaymentConfig = hsConfigMap.get("payment_config").toString();
            // 获取支付地址
            String pay_url = hsConfigMap.get("pay_url").toString();
            if ("".equals(pay_url) || null == pay_url) {
                logger.info("网银支付跳转URL不能为空!");
                returnMap.put("errorMsg", "网银支付跳转URL不能为空!");
                return returnMap;
            }
            // 生成订单
            String order_no = PayUtil.getOrderNo(userMap, topay);
            JSONObject ShbJson = JSONObject.fromObject(hsConfigMap);
            // 设置手机端模式 0h5模式 1二维码模式
            returnMap.put("mbish5", "0");
            if (returnMap.containsKey("scancode")) {
                String scancode = returnMap.get("scancode").toString();
                if ("wx".equals(scancode)) {
                    // 手机端是否二维码
                    if (1 == ShbJson.getInt("ish5_wx")) {
                        returnMap.put("mbish5", "1");
                    }
                    if (amount <= 0 || amount < ShbJson.getDouble("wx_minquota")
                            || amount > ShbJson.getDouble("wx_maxquota")) {
                        logger.info("微信支付金额验证失败,请输入大于{},且小于{}之间的金额",ShbJson.getDouble("wx_minquota"),ShbJson.getDouble("wx_maxquota"));
                        returnMap.put("errorMsg", "微信支付金额验证失败,请输入大于"+ShbJson.getDouble("wx_minquota")+",且小于"+ShbJson.getDouble("wx_maxquota")+"之间的金额");
                        return returnMap;
                    }
                } else if ("ali".equals(scancode)) {
                    if (1 == ShbJson.getInt("ish5_ali")) {
                        returnMap.put("mbish5", "1");
                    }
                    if (amount <= 0 || amount < ShbJson.getDouble("ali_minquota")
                            || amount > ShbJson.getDouble("ali_maxquota")) {
                        logger.info("支付宝支付金额验证失败,请输入大于{},且小于{}之间的金额",ShbJson.getDouble("ali_minquota"),ShbJson.getDouble("ali_maxquota"));
                        returnMap.put("errorMsg","支付宝支付金额验证失败,请输入大于"+ShbJson.getDouble("ali_minquota")+",且小于"+ShbJson.getDouble("ali_maxquota")+"之间的金额");
                        return returnMap;
                    }
                } else if ("cft".equals(scancode)) {
                    if (1 == ShbJson.getInt("ish5_cft")) {
                        returnMap.put("mbish5", "1");
                    }
                    if (amount <= 0 || amount < ShbJson.getDouble("qrminquota")
                            || amount > ShbJson.getDouble("qrmaxquota")) {
                        logger.info("财付通支付金额验证失败,请输入大于{},且小于{}之间的金额",ShbJson.getDouble("qrminquota"),ShbJson.getDouble("qrmaxquota"));
                        returnMap.put("errorMsg","财付通支付金额验证失败,请输入大于"+ShbJson.getDouble("qrminquota")+",且小于"+ShbJson.getDouble("qrmaxquota")+"之间的金额");
                        return returnMap;
                    }
                } else if ("yl".equals(scancode)) {
                    if (1 == ShbJson.getInt("ish5_yl")) {
                        returnMap.put("mbish5", "1");
                    }
                    if (amount <= 0 || amount < ShbJson.getDouble("yl_minquota")
                            || amount > ShbJson.getDouble("yl_maxquota")) {
                        logger.info("银联支付金额验证失败,请输入大于{},且小于{}之间的金额",ShbJson.getDouble("yl_minquota"),ShbJson.getDouble("yl_maxquota"));
                        returnMap.put("errorMsg","银联支付金额验证失败,请输入大于"+ShbJson.getDouble("yl_minquota")+",且小于"+ShbJson.getDouble("yl_maxquota")+"之间的金额");
                        return returnMap;
                    }
                } else if ("jd".equals(scancode)) {
                    if (1 == ShbJson.getInt("ish5_jd")) {
                        returnMap.put("mbish5", "1");
                    }
                    if (amount <= 0 || amount < ShbJson.getDouble("jd_minquota")
                            || amount > ShbJson.getDouble("jd_maxquota")) {
                        logger.info("京东支付金额验证失败,请输入大于{},且小于{}之间的金额",ShbJson.getDouble("jd_minquota"),ShbJson.getDouble("jd_maxquota"));
                        returnMap.put("errorMsg","京东支付金额验证失败,请输入大于"+ShbJson.getDouble("jd_minquota")+",且小于"+ShbJson.getDouble("jd_maxquota")+"之间的金额");
                        return returnMap;
                    }
                } else if ("kj".equals(scancode)) {
                    if (amount <= 0 || amount < ShbJson.getDouble("kj_minquota")
                            || amount > ShbJson.getDouble("kj_maxquota")) {
                        logger.info("快捷支付金额验证失败,请输入大于{},且小于{}之间的金额",ShbJson.getDouble("kj_minquota"),ShbJson.getDouble("kj_maxquota"));
                        returnMap.put("errorMsg","快捷支付金额验证失败,请输入大于"+ShbJson.getDouble("kj_minquota")+",且小于"+ShbJson.getDouble("kj_maxquota")+"之间的金额");
                        return returnMap;
                    }
                } else if ("wxtm".equals(scancode)) {
                    if (amount <= 0 || amount < ShbJson.getDouble("wxtm_minquota")
                            || amount > ShbJson.getDouble("wxtm_maxquota")) {
                        logger.info("微信条码金额验证失败,请输入大于{},且小于{}之间的金额",ShbJson.getDouble("wxtm_minquota"),ShbJson.getDouble("wxtm_maxquota"));
                        returnMap.put("errorMsg","微信条码金额验证失败,请输入大于"+ShbJson.getDouble("wxtm_minquota")+",且小于"+ShbJson.getDouble("wxtm_maxquota")+"之间的金额");
                        return returnMap;
                    }
                } else if ("alitm".equals(scancode)) {
                    if (amount <= 0 || amount < ShbJson.getDouble("alitm_minquota")
                            || amount > ShbJson.getDouble("alitm_maxquota")) {
                        logger.info("支付宝条码金额验证失败,请输入大于{},且小于{}之间的金额",ShbJson.getDouble("alitm_minquota"),ShbJson.getDouble("alitm_maxquota"));
                        returnMap.put("errorMsg","支付宝条码金额验证失败,请输入大于"+ShbJson.getDouble("alitm_minquota")+",且小于"+ShbJson.getDouble("alitm_maxquota")+"之间的金额");
                        return returnMap;
                    }
                }
            } else if (returnMap.containsKey("bankcode")) {
                if (amount <= 0 || amount < ShbJson.getDouble("minquota") || amount > ShbJson.getDouble("maxquota")) {
                    logger.info("网银支付金额验证失败,请输入大于{},且小于{}之间的金额",ShbJson.getDouble("minquota"),ShbJson.getDouble("maxquota"));
                    returnMap.put("errorMsg","网银支付金额验证失败,请输入大于"+ShbJson.getDouble("minquota")+",且小于"+ShbJson.getDouble("maxquota")+"之间的金额");
                    return returnMap;
                }
            }
            
            // YS支付商
            if (topay.equals(PayConstant.CONSTANT_YS)) {
                hsConfiglist.get(0).put("RETURN_URL", tempContextUrl);
                session.setAttribute("ysPay", hsConfiglist.get(0));
            }
            // 特殊生成轻易付的订单
            if (PayConstant.CONSTANT_QYF.equals(topay)) {
                order_no = QYFgetOrder(session, topay);
            }
            /***added by hb at 2018-06-22 全谷迪卿支付订单号长度减少到17位 start */
            // 特殊生成全谷迪卿订单号
            if(PayConstant.CONSTANT_QGDL.equals(topay)) {
                String payName = PayConstant.CONSTANT_QGDL.substring(0, 2);
                order_no = payName+new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date());
            }
            /*** added by hb at 2018-06-22 全谷迪卿支付订单号长度减少到17位 end */
            /***added by hb at 2018-06-25 免签支付订单号长度20位 start */
            // 特殊生成全谷迪卿订单号
            if(PayConstant.CONSTANT_MQZF.equals(topay)) {
                order_no = "MQ" + new SimpleDateFormat("yyMMddHHmmss").format(new Date())+getRandomString(6);
            }
            //汇银付支付订单号只要30位
            if (PayConstant.CONSTANT_HYFZF.equals(topay)){
                order_no="HYZF"+order_no.substring(5,31);
            }
            //万通支付订单号只要20位;大富支付订单号只要20位
            if(PayConstant.CONSTANT_WT.equals(topay) || PayConstant.CONSTANT_DAF.equals(topay) 
                    || PayConstant.CONSTANT_NWT.equals(topay) || PayConstant.CONSTANT_NOMQ.equalsIgnoreCase(topay)
                    || PayConstant.CONSTANT_BJYX.equals(topay) || PayConstant.CONSTANT_ABH.equals(topay) 
                    || PayConstant.CONSTANT_YIFA.equals(topay) || PayConstant.CONSTANT_YBT.equals(topay)
                    || PayConstant.CONSTANT_SYB.equals(topay) || PayConstant.CONSTANT_SLJH.equals(topay)
                    || PayConstant.CONSTANT_STZF.equals(topay) || PayConstant.CONSTANT_XINFA.equals(topay)
                    || PayConstant.CONSTANT_TDZF.equals(topay) || PayConstant.CONSTANT_XCFP.equals(topay)){
                order_no = order_no.substring(0,21);
            }

            /** 踢踢支付 和 踢踢支付2 订单号只能为20位 **/
            if (PayConstant.CONSTANT_TITI.equals(topay) || PayConstant.CONSTANT_TT2.equals(topay)) {
                String payName = topay.substring(0,2);
                String agent = cagent.substring(0,3);
                order_no = payName + agent + new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date());
            }
            
            if(PayConstant.CONSTANT_QIANYING.equals(topay)){
                order_no = "QIANY"+order_no.substring(8, order_no.length());
            }
            //信付宝支付订单号：仅允许字母或数字类型,不超过22个字符，不要有中文
            if(PayConstant.CONSTANT_XFB.equals(topay) || PayConstant.CONSTANT_YFTP.equals(topay)){
                order_no = order_no.substring(0,22);
            }
            if(PayConstant.CONSTANT_TXZF.equalsIgnoreCase(topay) || PayConstant.CONSTANT_SHAN.equals(topay)
                    || PayConstant.CONSTANT_YINF.equals(topay) || PayConstant.CONSTANT_JIDA.equals(topay)){
                order_no = order_no.substring(0,30);
            }
            
            /*** added by hb at 2018-06-22 全谷迪卿支付订单号长度20位 end */
            // 判断平台充值额度
            Double remainvalue = Double.parseDouble(userMap.get("remainvalue").toString());
            if (remainvalue - amount < 0) {
                logger.info("平台商:{},剩余额度不足:{}",cagent,remainvalue);
                returnMap.put("errorMsg","平台商:{"+cagent+"},剩余额度不足:{"+remainvalue+"},请提醒平台充值!");
                return returnMap;
            }
            //临时添加根据平台自定义订单金额,一定要注意
            if("TYC".equals(cagent) && PayConstant.CONSTANT_WK.equals(topay)){
                //太阳城平台的悟空支付
                String payCode = returnMap.get("scancode")+"";
                if(payCode.equalsIgnoreCase("ali")){
                    amount = new BigDecimal(Double.valueOf(amount)).subtract(new BigDecimal(Double.valueOf(0.01))).doubleValue();
                }
            }
            ///乐百支付 支付金额 减一
            if(PayConstant.CONSTANT_LBZF.equals(topay)){
            	double money = Double.valueOf(amount);
            	logger.info("乐百支付 money = {}", money);
            	if(money%100 == 0){
            		amount = new BigDecimal(Double.valueOf(amount)).subtract(new BigDecimal(Double.valueOf(1))).doubleValue();
            		logger.info("乐百支付 amount = {}", amount);
            	}
            }
            
            //阿里宝盒支付银联H5支付自动扣减一分钱
            if(PayConstant.CONSTANT_ABH.equals(topay)){
                String payCode = returnMap.get("scancode")+"";
                String mobile = request.getParameter("mobile");
                if(payCode.equalsIgnoreCase("yl") && ShbJson.getInt("ish5_yl")==0 && !StringUtils.isNullOrEmpty(mobile)){
                    amount = new BigDecimal(Double.valueOf(amount)).subtract(new BigDecimal(Double.valueOf(0.01))).doubleValue();
                }
            }
            
            //鼎盛支付 订单号不能22位
            if(PayConstant.CONSTANT_DSZF.equals(topay)){
                order_no = order_no.substring(0,22);
            }
            //iipays支付订单号不能操过 30 位
            if(PayConstant.CONSTANT_IIZF.equals(topay)){
            	order_no = order_no.substring(0,30);
            }
            //万通XX 支付不能超过 20位
            if(PayConstant.CONSTANT_WTXX.equals(topay)){
            	order_no = order_no.substring(0,20);
            }
            //宜橙支付订单号30位
            if(PayConstant.CONSTANT_YICZF.equals(topay)){
                order_no = order_no.substring(0,30);
            }
            //大宝天下支付订单号22位
            if(PayConstant.CONSTANT_DBTX.equals(topay)){
            	int dbtxLen = PayConstant.CONSTANT_DBTX.length();
            	String substr = order_no.substring(dbtxLen+3, dbtxLen+12);
                order_no = order_no.replace(substr, "");
            }
            //宏达支付订单号30位
            if (PayConstant.CONSTANT_HDZF.equals(topay)) {
                order_no = order_no.substring(0,30);
            }
            // 返回数据
            returnMap.put("order_no", order_no);
            returnMap.put("uid", uid);
            returnMap.put("amount", amount);
            returnMap.put("username", username);
            returnMap.put("msg","success");
            returnMap.put("pmapsconfig",PaymentConfig);
            returnMap.put("payId", payId);
            returnMap.put("cagent", cagent);
            returnMap.put("cid", cid);
            returnMap.put("topay", topay);
            returnMap.put("pay_url", pay_url);
            returnMap.put("returnUrl", tempContextUrl);//前段回调url
            return returnMap;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("获取支付请求参数异常:{}",e.getMessage());
            returnMap.put("msg", "error");
            returnMap.put("errorMsg", "获取支付请求参数异常!");
            return returnMap;
        }
    }

    /**
     * 轻易付订单生成
     * 
     * @param session
     * @param topay
     * @return
     */
    private String QYFgetOrder(HttpSession session, String topay) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", session.getAttribute("uid").toString());
        userMap = userService.selectUserById(userMap);
        String currTime = DateUtil.getCurrentDate("yyyyMMddHHmmss");
        String strTime = currTime.substring(8, currTime.length());
        String strRandom = DateUtil.getRandom(4) + "";
        String strReq = strTime + strRandom;
        String qyf_order_no = currTime + strReq + topay + userMap.get("cagent").toString().toLowerCase();
        return qyf_order_no;
    }

    /**
     * // 网银支付
     * 
     * @param request
     * @param response
     * @param model
     * @return
     * @throws IOException
     * @throws ServletException
     */
    @RequestMapping("/onlineBanking") // 定义参数 bankcode //acounmt //platformcode
    public String onlineBanking(HttpServletRequest request, HttpSession session, HttpServletResponse response,
            Model model, RedirectAttributes attr) throws ServletException, IOException {
        if ("2".equals(VersionConstant.VERSION_CODE)) {
            return BankPay(request,session,response,model,attr);
        }
        logger.info("扫码支付请求开始=======================START=================================");
        String uid = session.getAttribute("uid").toString();//用户ID
        // 获取来源域名
        StringBuffer url = request.getRequestURL();
        String tempContextUrl = url.delete(url.length() - request.getRequestURI().length(), url.length()).append("/")
                .toString();
        try {
            //判断缓存是否存在用户请求
            if (payMap.containsKey("PAY:"+uid)) {
                logger.info("支付请求正在处理,请不要重复提交....");
                return "redirect:" + tempContextUrl;
            }
            payMap.put("PAY:"+uid, "1");
            //获取支付请求参数
            Map<String,Object> reqParams = check(request, session, response);
            if(reqParams.containsKey("msg") && "error".equalsIgnoreCase(reqParams.get("msg").toString())){
                String errorMsg = reqParams.get("errorMsg")+"";
                logger.info("获取支付请求参数失败:{}",errorMsg);
                return "redirect:" + tempContextUrl;
            }
            logger.info("发起扫码支付请求参数:{}",JSONObject.fromObject(reqParams).toString());    
            // 获取请求参数
            String mobile = request.getParameter("mobile");//移动端
            String topay = reqParams.get("topay").toString();//支付商编码
            String username = reqParams.get("username").toString();
            Double amount = Double.parseDouble(reqParams.get("amount").toString());
            String order_no = reqParams.get("order_no").toString();//订单号
            String bankcode = reqParams.get("bankcode").toString();// 扫描支付code
            Map<String,String> pmapsconfig = JSONObject.fromObject(reqParams.get("pmapsconfig").toString());
            String ip = StringUtils.isNullOrEmpty(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
            String returnUrl = reqParams.get("returnUrl").toString();
            String pay_url = reqParams.get("pay_url").toString();
            String cid = reqParams.get("cid").toString();
            String cagent = reqParams.get("cagent").toString();
            String payType = "1";// 网银
            String payId = reqParams.get("payId").toString();//支付商ID
            // 填充实体
            PayEntity payEntity = new PayEntity();
            payEntity.setUsername(username);
            payEntity.setuId(uid);
            payEntity.setIp(ip);
            payEntity.setRefererUrl(returnUrl);
            payEntity.setAmount(amount);
            payEntity.setTopay(topay);
            payEntity.setOrderNo(order_no);
            payEntity.setPayUrl(pay_url);
            payEntity.setPayConfig(pmapsconfig);
            payEntity.setMobile(mobile);
            payEntity.setCid(cid);
            payEntity.setCagent(cagent);
            payEntity.setPayType(payType);
            payEntity.setPayCode(bankcode);
            payEntity.setPayId(payId);
            payEntity.setDescription("create wy top-up order is success");
            // 调用支付商接口-》发送订单请求
            String className = "com.cn.tianxia.pay.impl." + topay.toUpperCase() + "PayServiceImpl";
            logger.info("加载支付类路径:" + className);
            Constructor<?> constructor;
            PayService payService = null;
            // 是否默认构造方法
            String type = "";
            boolean methodFalg = false;
            // 通财支付 秒卡通 国盛通
            if ("TCP".equals(topay)) {
                type = "bank";
                methodFalg = true;
            } else if ("MKT".equals(topay) || "GST".equals(topay) || "SF".equals(topay) || PayConstant.CONSTANT_BFB.equals(topay)
                    || PayConstant.CONSTANT_WK.equals(topay)||PayConstant.CONSTANT_SKP.equals(topay)||PayConstant.CONSTANT_HANY.equals(topay)) {
                type = "bank";
                methodFalg = true;
            }
            //出事话构造方法
            if (!methodFalg) {
                // 默认构造方法
                constructor = Class.forName(className).getConstructor(Map.class);
                payService = (PayService) constructor.newInstance(pmapsconfig);
            } else {
                // 带字符的构成方法
                constructor = Class.forName(className).getConstructor(Map.class, String.class);
                payService = (PayService) constructor.newInstance(pmapsconfig, type);
            }
            //创建订单
            userService.createTopUpRecharge(payEntity);
            JSONObject prtMap = payService.wyPay(payEntity);
            // 失败直接返回--》》通知调用异常处理页面
            if (prtMap.containsKey("status") && "error".equals(prtMap.get("status").toString())) {
                logger.info("网银表单构造异常--->>");
                // 直接返回来源地址
                return "redirect:" + tempContextUrl;
            }
            // 网银支付三种跳转情况:一、from表单通过网关服务器 二、model通过jsp页面提交 三、redirect 重定向URL
            if (prtMap.containsKey("form")) {
                String form = "";
                try {
                    form = URLEncoder.encode(prtMap.getString("form"), "utf-8");
                } catch (Exception e) {
                    logger.info("URL编码异常！"+e);
                    e.printStackTrace();
                }
                attr.addAttribute("from", form);
                return prtMap.get("redirect").toString();
            } else if (prtMap.containsKey("jsp_content")) {
                model.addAttribute("html", prtMap.get("jsp_content").toString());
                return prtMap.get("jsp_name").toString();
            } else if (prtMap.containsKey("link")) {
                return "redirect:" + prtMap.get("link").toString();
            } else if (prtMap.containsKey("credential")) {
                //model.addAttribute("html", prtMap.get("jsp_content").toString());
                model.addAttribute("JWP_ATTR", prtMap.get("credential").toString());
                //request.setAttribute("JWP_ATTR", prtMap.get("credential").toString());
                String bankurl = "/page/middle.jsp";
                //request.getRequestDispatcher(bankurl).forward(request, response);
                return "forward:" + bankurl;
            }else if(prtMap.containsKey("file")){
                String htmlStr = prtMap.getString("file");
                response.setCharacterEncoding("utf-8");
                response.setContentType("text/html");
                PrintWriter printWriter = response.getWriter();
                printWriter.print(htmlStr);
                printWriter.flush();
                printWriter.close();
                return printWriter.toString();
            }else{
                logger.info("跳转类型错误！！！");
                return "redirect:" + tempContextUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("网银支付异常:{}",e.getMessage());
            return "redirect:" + tempContextUrl;
        }finally {
            if (payMap.containsKey("PAY:"+uid)) {
                payMap.remove("PAY:"+uid);
            }
        }
    }

    private String BankPay(HttpServletRequest request, HttpSession session, HttpServletResponse response, Model model, RedirectAttributes attr) {
        logger.info("网银支付请求开始--------------------START---------------------------");
        // 获取来源域名
        StringBuffer url = request.getRequestURL();
        String requestUrl = url.delete(url.length() - request.getRequestURI().length(), url.length()).append("/")
                .toString();
        String ip = StringUtils.isNullOrEmpty(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        if (!ObjectUtils.allNotNull(session.getAttribute("uid"))) {
            logger.error("网银请求异常：用户未登录");
            return "redirect:" + requestUrl;
        }
        String uid = session.getAttribute("uid").toString();


        //校验参数
        String payId = request.getParameter("payId");
        String amount = request.getParameter("acounmt");
        String bankcode = request.getParameter("bankcode");

        if (org.apache.commons.lang3.StringUtils.isAnyBlank(payId,amount,bankcode) ) {
            logger.error("网银支付请求参数错误，请检查参数，必传参数不能为空！");
            return "redirect:" + requestUrl;
        }
        //判断缓存是否存在用户请求
        if (payMap.containsKey("PAY:"+uid)) {
            logger.info("支付请求正在处理,请不要重复提交....");
            return "redirect:" + requestUrl;
        }
        payMap.put("PAY:"+uid, "1");
        //请求VO
        BankPayVO bankPayVO = new BankPayVO();
        bankPayVO.setUid(uid);
        bankPayVO.setPayId(payId);
        bankPayVO.setAmount(Double.valueOf(amount));
        bankPayVO.setBankcode(bankcode);
        bankPayVO.setReturn_url(requestUrl);
        bankPayVO.setIp(ip);

        JSONObject result = platformPayService.bankPay(bankPayVO);

        try {
            // 失败直接返回--》》通知调用异常处理页面
            if (result.containsKey("status") && "error".equals(result.get("status").toString())) {
                logger.info("网银表单构造异常--->>");
                // 直接返回来源地址
                return "redirect:" + requestUrl;
            }
            // 网银支付三种跳转情况:一、from表单通过网关服务器 二、model通过jsp页面提交 三、redirect 重定向URL
            if (result.containsKey("form")) {
                String form =  URLEncoder.encode(result.getString("form"), "utf-8");
                attr.addAttribute("from", form);
                return result.getString("redirect");
            } else if (result.containsKey("jsp_content")) {
                model.addAttribute("html", result.get("jsp_content").toString());
                return result.getString("jsp_name");
            } else if (result.containsKey("link")) {
                return "redirect:" + result.getString("link");
            } else if (result.containsKey("credential")) {
                model.addAttribute("JWP_ATTR", result.getString("credential"));
                String bankurl = "/page/middle.jsp";
                return "forward:" + bankurl;
            } else if (result.containsKey("file")) {
                String htmlStr = result.getString("file");
                response.setCharacterEncoding("utf-8");
                response.setContentType("text/html");
                PrintWriter printWriter = response.getWriter();
                printWriter.print(htmlStr);
                printWriter.flush();
                printWriter.close();
                return printWriter.toString();
            } else {
                logger.info("跳转类型错误！！！");
                return "redirect:" + requestUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("网银支付异常:{}",e.getMessage());
            return "redirect:" + requestUrl;
        }finally {
            payMap.remove("PAY:"+uid);
        }
    }

    /**
     * 扫描支付 // 参数:scancode 扫描支付code amount 支付金额 topay 支付商code
     * 
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping("/scanPay")

    @ResponseBody
    public JSONObject scanPay(HttpServletRequest request, HttpSession session, HttpServletResponse response,
            Model model) {

        if ("2".equals(VersionConstant.VERSION_CODE)) {
            return newScanPay(request,session,response);
        }
        logger.info("扫码支付请求开始=======================START=================================");
        String uid = session.getAttribute("uid").toString();//用户ID
        try {
            //判断缓存是否存在用户请求
            if (payMap.containsKey("PAY:"+uid)) {
                logger.info("支付请求正在处理,请不要重复提交....");
                return PayUtil.returnPayJson("error","1","支付请求正在处理,请不要重复提交....","",0,"", "支付请求正在处理,请不要重复提交....");
            }
            payMap.put("PAY:"+uid, "1");
            
            //获取支付请求参数
            Map<String,Object> reqParams = check(request, session, response);
            if(reqParams.containsKey("msg") && "error".equalsIgnoreCase(reqParams.get("msg").toString())){
                String errorMsg = reqParams.get("errorMsg")+"";
                logger.info("获取支付请求参数失败:{}",errorMsg);
                return PayResponse.error(errorMsg);
            }
            logger.info("发起扫码支付请求参数:{}",JSONObject.fromObject(reqParams).toString());    
            // 获取请求参数
            String mbIsH5 = reqParams.get("mbish5").toString();
            //String mbIsH5 = "0";
//            String mobile = request.getParameter("mobile");
            //测试手机端
            String mobile = "mobile";//request.getParameter("mobile");
            logger.info("扫码支付请求渠道:{}"+mobile);
            String topay = reqParams.get("topay").toString();//支付商编码
            String username = reqParams.get("username").toString();
            Double amount = Double.parseDouble(reqParams.get("amount").toString());
            String order_no = reqParams.get("order_no").toString();//订单号
            String scancode = reqParams.get("scancode").toString();// 扫描支付code
            Map<String,String> pmapsconfig = JSONObject.fromObject(reqParams.get("pmapsconfig").toString());
            String ip = StringUtils.isNullOrEmpty(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
            String returnUrl = reqParams.get("returnUrl").toString();
            String pay_url = reqParams.get("pay_url").toString();
            String cid = reqParams.get("cid").toString();
            String cagent = reqParams.get("cagent").toString();
            String payId = reqParams.get("payId").toString();//支付商ID
            // 填充实体
            PayEntity payEntity = new PayEntity();
            payEntity.setUsername(username);
            payEntity.setuId(uid);
            payEntity.setIp(ip);
            payEntity.setRefererUrl(returnUrl);
            payEntity.setAmount(amount);
            payEntity.setTopay(topay);
            payEntity.setOrderNo(order_no);
            payEntity.setPayUrl(pay_url);
            payEntity.setPayConfig(pmapsconfig);
            payEntity.setMobile("0".equals(mbIsH5)?mobile:null);
            payEntity.setCid(cid);
            payEntity.setCagent(cagent);
            payEntity.setPayId(payId);
            logger.info("从配置properties文件中读取支付渠道开始=========================START=====================");
            String payType = "";// 支付类型 目前分为4种：1 网银，2 微信 ， 3 支付宝，4qq支付(财付通)，5银联扫码
            String scanType = "";
            // 属性文件中定义的支付类型 {1,2,3,} 1微信，2，支付宝，3qq
            String[] sptNumber = null;
            if (!StringUtils.isNullOrEmpty(mobile) && "0".equals(mbIsH5)) {
                // 手机端
                try {
                    if (PayProperties.scanMobileTypeMap == null || PayProperties.scanMobileTypeMap.size() == 0) {
                        PayProperties tb = new PayProperties();
                        PayProperties.scanMobileTypeMap = tb.readMobileProperties();
                    }
                    if (PayProperties.scanMobileTypeMap.containsKey(topay)) {
                        String sptStr = PayProperties.scanMobileTypeMap.get(topay);
                        sptNumber = sptStr.split(",");
                    } else {
                        logger.info("扫码支付匹配类型异常！！！");
                        return PayUtil.returnPayJson("error", "1", "扫码支付匹配类型异常", username, amount, order_no, "");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("从配置properties文件中获取移动端支付渠道异常:{}",e.getMessage());
                    return PayUtil.returnPayJson("error", "1", "从配置properties文件中获取移动端支付渠道异常", username, amount, order_no, "");
                }
                
            } else {
                // 设置手机端返回二维码
                if ("1".equals(mbIsH5)) {
                    mobile = "";
                }
                try {
                    // pc端
                    if (PayProperties.scanTypeMap == null || PayProperties.scanTypeMap.size() == 0) {
                        PayProperties tb = new PayProperties();
                        PayProperties.scanTypeMap = tb.readProperties();
                    }
                    if (PayProperties.scanTypeMap.containsKey(topay)) {
                        String sptStr = PayProperties.scanTypeMap.get(topay);
                        sptNumber = sptStr.split(",");
                    } else {
                        logger.info("扫码支付匹配类型异常！！！");
                        return PayUtil.returnPayJson("error", "1", "扫码支付匹配类型异常", username, amount, order_no, "");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("从配置properties文件中获取PC端支付渠道异常:{}",e.getMessage());
                    return PayUtil.returnPayJson("error", "1", "从配置properties文件中获取PC端支付渠道异常", username, amount, order_no, "");
                }
            }
            logger.info("创建构造器函数开始==========================START============================");
            // 是否默认构造方法
            String type = "";
            boolean methodFalg = false;
            // 通财支付 国盛通 秒卡通
            if ("TCP".equals(topay)) {
                type = "scan";
                methodFalg = true;
            } else if (PayConstant.CONSTANT_MKT.equals(topay) || PayConstant.CONSTANT_GST.equals(topay)
                    || PayConstant.CONSTANT_SF.equals(topay) || PayConstant.CONSTANT_BFB.equals(topay)
                    ||PayConstant.CONSTANT_WK.equals(topay)) {
                if (PayConstant.CONSTANT_WX.equals(scancode)) {
                    type = PayConstant.CONSTANT_WX;
                } else if (PayConstant.CONSTANT_ALI.equals(scancode)) {
                    type = PayConstant.CONSTANT_ALI;
                } else if (PayConstant.CONSTANT_CFT.equals(scancode)) {
                    type = PayConstant.CONSTANT_CFT;
                } else if (PayConstant.CONSTANT_JD.equals(scancode)) {
                    type = PayConstant.CONSTANT_JD;
                } else if (PayConstant.CONSTANT_KJ.equals(scancode)) {
                    type = PayConstant.CONSTANT_KJ;
                } else if (PayConstant.CONSTANT_WXTM.equals(scancode)) {
                    type = PayConstant.CONSTANT_WXTM;
                } else if (PayConstant.CONSTANT_ALITM.equals(scancode)) {
                    type = PayConstant.CONSTANT_ALITM;
                }else if (PayConstant.CONSTANT_YL.equals(scancode)){
                    //银联扫码
                    type = PayConstant.CONSTANT_YL;
                }
                methodFalg = true;
            }

            // 判断支付类型 
            try {
                if (PayConstant.CONSTANT_WX.equals(scancode)) {
                    scanType = sptNumber[0];
                    payType = PayConstant.CHANEL_WX;
                } else if (PayConstant.CONSTANT_ALI.equals(scancode)) {
                    scanType = sptNumber[1];
                    payType = PayConstant.CHANEL_ALI;
                } else if (PayConstant.CONSTANT_CFT.equals(scancode)) {
                    scanType = sptNumber[2];
                    payType = PayConstant.CHANEL_CFT;
                } else if (PayConstant.CONSTANT_YL.equals(scancode)) {
                    scanType = sptNumber[3];
                    payType = PayConstant.CHANEL_YL;
                } else if (PayConstant.CONSTANT_JD.equals(scancode)) {
                    scanType = sptNumber[4];
                    payType = PayConstant.CHANEL_JD;
                } else if (PayConstant.CONSTANT_KJ.equals(scancode)) {
                    scanType = sptNumber[5];
                    payType = PayConstant.CHANEL_KJ;
                } else if (PayConstant.CONSTANT_WXTM.equals(scancode)) {
                    scanType = sptNumber[6];
                    payType = PayConstant.CHANEL_WXTM;
                } else if (PayConstant.CONSTANT_ALITM.equals(scancode)) {
                    scanType = sptNumber[7];
                    payType = PayConstant.CHANEL_ALITM;
                } else {
                    logger.info("不存在的支付类型");
                    return PayUtil.returnPayJson("error", "1", "渠道类型异常", username, amount, order_no, "");
                }
            } catch (Exception e) {
                logger.info("该支付类型配置文件未添加！！！" + e);
                return PayUtil.returnPayJson("error", "1", "该支付类型配置文件未添加！", username, amount, order_no, "");
            }
            payEntity.setPayCode(scanType);
            payEntity.setPayType(payType);
            /*** 连云港快捷支付通道传入银行卡号  added by hb at 2018-06-05 --start*/
            if("KJ".equals(scanType) && "LYG".equals(topay)) {
                Map<String, Object> extendMap = new HashMap<>();
                extendMap.put("bankCardNo", request.getParameter("bankCardNo"));
                payEntity.setExtendMap(extendMap);
            }
            /*** 连云港快捷支付传入银行卡号  added by hb at 2018-06-05 --end*/
            // 调用支付商接口-》发送订单请求
            String className = "com.cn.tianxia.pay.impl." + topay.toUpperCase() + "PayServiceImpl";
            logger.info("加载支付类路径:" + className);
            Constructor<?> constructor;
            PayService payService = null;
            try {
                if (!methodFalg) {
                    // 默认构造方法
                    constructor = Class.forName(className).getConstructor(Map.class);
                    payService = (PayService) constructor.newInstance(pmapsconfig);
                } else {
                    // 带字符的构成方法
                    constructor = Class.forName(className).getConstructor(Map.class, String.class);
                    payService = (PayService) constructor.newInstance(pmapsconfig, type);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("加载扫码支付类异常:{}",e.getMessage());
                return PayUtil.returnPayJson("error", "1", "加载扫码支付类异常!", username, amount, order_no, "");
            }

            //创建待支付订单
            logger.info("发起第三方订单支付请求开始========================START==================================");
            JSONObject jsonObject = payService.smPay(payEntity);
            if(jsonObject.containsKey("status") && jsonObject.getString("status").equalsIgnoreCase("success")){
                //创建订单成功
                logger.info("发起第三方支付请求成功:{}",jsonObject.toString());
                payEntity.setDescription("Create top-up order is success");
            }else{
                //支付失败订单
                logger.info("发起第三方支付请求失败:{}",jsonObject.toString());
                payEntity.setDescription("Create top-up order is faild,Please contact customer service.");
            }
            userService.createTopUpRecharge(payEntity);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("扫码支付异常:{}",e.getMessage());
            return PayUtil.returnPayJson("error", "1", "支付请求发起失败","",0,"",e.getMessage());
        }finally {
            payMap.remove("PAY:"+uid);
        }
    }

    private JSONObject newScanPay(HttpServletRequest request, HttpSession session, HttpServletResponse response) {
        logger.info("扫码支付请求开始=======================START=================================");
        // 获取来源域名
        StringBuffer url = request.getRequestURL();
        String requestUrl = url.delete(url.length() - request.getRequestURI().length(), url.length()).append("/")
                .toString();
        //请求ip
        String ip = StringUtils.isNullOrEmpty(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        if (!ObjectUtils.allNotNull(session.getAttribute("uid"))) {
            logger.error("扫码支付请求异常：用户未登录");
            return BaseResponse.error("1000","扫码支付请求异常：用户未登陆！");
        }
        String uid = session.getAttribute("uid").toString();

        //判断缓存是否存在用户请求
        if (payMap.containsKey("PAY:"+uid)) {
            logger.info("支付请求正在处理,请不要重复提交....");
            return BaseResponse.error("1000","支付请求正在处理,请不要重复提交....");
        }
        payMap.put("PAY:"+uid, "1");

        try {
            String payId = request.getParameter("payId");
            String amount = request.getParameter("acounmt");
            String scancode = request.getParameter("scancode");
            String mobile = request.getParameter("mobile");
//            String mobile = "mobile";

            //校验参数
            if (org.apache.commons.lang3.StringUtils.isAnyBlank(payId, amount, scancode)) {
                logger.error("扫码支付请求参数错误，请检查参数，必传参数不能为空！");
                return BaseResponse.error("1000", "扫码支付请求参数错误，请检查参数，必传参数不能为空！");
            }
            //请求VO
            ScanPayVO scanPayVO = new ScanPayVO();
            scanPayVO.setUid(uid);
            scanPayVO.setPayId(payId);
            scanPayVO.setAmount(Double.valueOf(amount));
            scanPayVO.setScancode(scancode);
            scanPayVO.setRefererUrl(requestUrl);
            scanPayVO.setIp(ip);
            scanPayVO.setMobile(mobile);

            return platformPayService.scanPay(scanPayVO);
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("扫码支付异常："+e.getMessage());
            return BaseResponse.error("1000","扫码支付异常："+e.getMessage());
        }finally {
            payMap.remove("PAY:" + uid);
        }
    }

    /**
     * 网银回调方法
     *
     * @param request
     * @param session
     * @return
     */
    @RequestMapping("/bankingNotify.do")
    @ResponseBody
    public String bankingNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String order_no = "";// (String) request.getParameter("order_no");
        String trade_no = (String) request.getParameter("trade_no");
        String trade_status = (String) request.getParameter("trade_status");
        // 不同支付商订单参数名称不一样需要匹配
        String[] reqParams = new String[] {"order_no", "orderNo", "requestId" };// 我方订单号
        String[] tradeNoParams = new String[] {"trade_no", "payNo", "accNo" };// 支付商订单号

        order_no = PayUtil.matching(reqParams, request);
        trade_no = PayUtil.matching(tradeNoParams, request);
        if (payMap.containsKey(order_no)) {
            logger.info("order_no:" + order_no + "重复调用!");
            return ret_str_success;
        }
        payMap.put(order_no, "1");
        try {
            Map<String, String> infoMap = new HashMap<String, String>();
            Enumeration enu = request.getParameterNames();
            while (enu.hasMoreElements()) {
                String paraName = (String) enu.nextElement();
                infoMap.put(paraName, request.getParameter(paraName).toString());
            }
            logger.info("bankingNotify:" + JSONUtils.toJSONString(infoMap));
            // 文件记录
            FileLog f = new FileLog();
            Map<String, String> fileMap = new HashMap<String, String>();
            fileMap.put("requestIp", IPTools.getIp(request));
            fileMap.put("requestParams", JSONUtils.toJSONString(infoMap));
            f.setLog("bankingNotify", fileMap);

            // 验证参数
            if ("".equals(order_no) || null == order_no || "".equals(trade_no) || null == trade_no) {
                return ret_str_success;
            }
            // 验证单据号
            Map<String, Object> map = new HashMap<>();
            map.put("orderno", order_no);
            List<Map<String, String>> lm = userService.selectChickReCharge(map);
            // 单据不存在,直接返回

            if (lm.size() <= 0) {
                return ret_str_success;
            }
            Map<String, String> order = lm.get(0);
            // 获取用户信息
            Object ouid = order.get("uid");
            String uid = ouid.toString();
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            map = userService.selectUserById(param);
            JSONObject jmap = new JSONObject().fromObject(map);

            String payId = String.valueOf(order.get("pay_id"));
            if (StringUtils.isNullOrEmpty(payId)) {
                logger.info("支付商id不存在！");
                return ret_str_success;
            }
            List<Map<String, String>> plist = userService.selectYsepaybyId(payId,uid);
            if (plist.size() > 0) {
            } else {
                return ret_str_success;
            }

            // 获取配置信息
            Map<String, String> hsConfigMap = plist.get(0);
            // 支付商
            String paymentName = hsConfigMap.get("payment_name").toString();
            if (null == paymentName || "".equals(paymentName)) {
                return ret_str_success;
            }
            String PaymentConfig = hsConfigMap.get("payment_config").toString();
            // 配置信息
            Map<String, String> pmapsconfig = JSONUtils.toHashMap(PaymentConfig);

            // 判断支付商签名是否正确
            if (paymentName.equals(PayConstant.CONSTANT_SHB) || paymentName.equals(PayConstant.CONSTANT_ZF)
                    || paymentName.equals(PayConstant.CONSTANT_DDB) || paymentName.equals(PayConstant.CONSTANT_WF)) {
                // 初始化数据
                t_trade_status = "SUCCESS";
                ret_str_success = "SUCCESS";
                ret_str_failed = "Signature Error";

                SHBPayServiceImpl shbPay = new SHBPayServiceImpl(pmapsconfig);
                String result = shbPay.offlineNotify(request);
                if (!"SUCCESS".equals(result)) {
                    return ret_str_success; // 验签失败，业务结束
                }
            } else if (paymentName.equals(PayConstant.CONSTANT_GST) || paymentName.equals(PayConstant.CONSTANT_MKT)) {
                ret_str_success = "success";
                ret_str_failed = "fail";
                t_trade_status = "success";// 订单为success表示成功
                // 订单状态为空返回
                if (StringUtils.isNullOrEmpty(trade_status)) {
                    return ret_str_success;
                }

                GSTPayServiceImpl gstPay = new GSTPayServiceImpl(pmapsconfig, "bank");
                boolean falg = gstPay.validPageNotify(request);
                if (!falg) {
                    logger.error("GST参数验证失败");
                    return ret_str_success;
                }
            } else if (paymentName.equals(PayConstant.CONSTANT_BFT)) {
                // 初始化数据
                ret_str_success = "SUCCESS";
                ret_str_failed = "fail";
                t_trade_status = "1";// 订单为1表示成功 orderStatus
                logger.info("佰付通回调参数验证开始..");
                String orderStatus = request.getParameter("orderStatus");// 订单状态

                if (StringUtils.isNullOrEmpty(orderStatus)) {
                    logger.info("佰付通业务参数为空！");
                    return ret_str_success;
                }

                BFTPayServiceImpl bft = new BFTPayServiceImpl(pmapsconfig);
                boolean falg = bft.BFTCallback(request);
                // 验证失败直接返回
                if (!falg) {
                    return ret_str_success;
                }
                trade_status = orderStatus;

            } else if (paymentName.equals(PayConstant.CONSTANT_HT)) {
                // 初始化数据
                ret_str_success = "success";
                ret_str_failed = "fail";
                t_trade_status = "success";// 订单为1表示成功 orderStatus
                logger.info("汇通回调参数验证开始..");

                // 验证单据号
                // Map<String, Object> map1 = new HashMap<>();
                // map1.put("orderno", order_no);
                // List<Map<String, String>> lm1 = userService.selectChickReCharge(map);
                // Map<String, String> order1 = lm1.get(0);
                String pay_type = JSONObject.fromObject(order).get("pay_type").toString();

                JSONObject htJson = JSONObject.fromObject(pmapsconfig);
                String key = "";
//                String merchant = "";
                if (pay_type.equals("微信")) {
                    key = htJson.get("wx_key").toString();
//                    merchant = htJson.get("wx_merchant").toString();
                } else if (pay_type.equals("银联")) {
                    key = htJson.get("yl_key").toString();
//                    merchant = htJson.get("yl_merchant").toString();
                } else if (pay_type.equals("京东")) {
                    key = htJson.get("jd_key").toString();
//                    merchant = htJson.get("jd_merchant").toString();
                } else if (pay_type.equals("网银")) {
                    key = htJson.get("key").toString();
//                    merchant = htJson.get("merchant_code").toString();
                } else if (pay_type.equals("快捷")) {
                    key = htJson.get("kj_key").toString();
//                    merchant = htJson.get("kj_merchant").toString();
                } else {
                    logger.error("HT类型匹配异常");
                    return ret_str_success;
                }

                HTPayServiceImpl ht = new HTPayServiceImpl(pmapsconfig);
                boolean falg = ht.HTNotify(request, key);
                // 验证失败直接返回
                if (!falg) {
                    logger.info("汇通验签失败直接返回!");
                    return ret_str_success;
                }
            } else {
                // 异常请求
                logger.error("异常请求");
                return ret_str_success;
            }
            // 回调日志
            Map<String, Object> cmap = new HashMap<>();
            Map<String, String[]> errorMap = new HashMap<String, String[]>();
            errorMap = request.getParameterMap();
            cmap.put("params", JSONObject.fromObject(errorMap).toString());
            cmap.put("ip", IPTools.getIp(request));
            cmap.put("status", ret_str_failed);
            userService.InsertCallbacklog(cmap);
            try {
                System.out.println("-------------------------网银支付--------------------------");

                JSONObject jo = new JSONObject().fromObject(order);
                String oamount = jo.getString("order_amount");

                String status = order.get("trade_status");
                // 如果单据状态不是交易中则跳过后续步骤
                if (!"处理中".equals(status)) {
                    return ret_str_success;
                }
                Map<String, Object> params = new HashMap<>();
                params.put("uid", uid);
                Map<String, Object> user = userService.selectUserById(params);

                double wallet = Double.parseDouble(jmap.getString("wallet"));
                double amt = Double.parseDouble(oamount);
                String sql2 = "update t_recharge set trade_status = ?,trade_no=? where order_no=?";
                List<String> list = new ArrayList<>();
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                Map<String, Object> paramsMap = new HashMap<String, Object>();

                if (t_trade_status.equals(trade_status)) {
                    JSONObject json = JSONObject.fromObject(plist.get(0));
                    Double cj = amt * json.getDouble("dividend_rate");
                    Double dml = (amt + cj) * json.getInt("coding_rate");
                    // System.out.println("单据成功");
                    DecimalFormat df = new DecimalFormat("######0.00");
                    cj = Double.parseDouble(df.format(cj));
                    try {
                        paramsMap.put("amt", amt);
                        paramsMap.put("uid", uid);
                        paramsMap.put("wallet", wallet);
                        paramsMap.put("cj", cj);
                        paramsMap.put("tradeNo", trade_no);
                        paramsMap.put("outTradeNo", order_no);
                        paramsMap.put("cagent", user.get("cagent"));
                        paramsMap.put("dml", dml);
                        paramsMap.put("number", "CJ" + System.currentTimeMillis());
                        paramsMap.put("number2", "CK" + System.currentTimeMillis());
                        paramsMap.put("_err", "0");
                        userService.ysePayCallBack(paramsMap);
                        if ("1".equals(paramsMap.get("_err"))) {
                            return ret_str_failed;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ret_str_failed;
                    }
                    System.out.println("UID : " + uid);
                    System.out.println("Username : " + jmap.getString("username"));
                    System.out.println("order : " + jo);
                    System.out.println("amt : " + amt);
                    System.out.println("dml : " + dml);
                    System.out.println("cj : " + cj);
                    System.out.println("------------------订单完成----------------------");
                } else {
                    // 更新转账信息
                    connection = JDBCTools.getConnection();
                    connection.setAutoCommit(false);

                    list.clear();
                    list.add("faild");
                    list.add(trade_no);
                    list.add(order_no);

                    preparedStatement = connection.prepareStatement(sql2);
                    for (int i = 0; i < list.size(); i++) {
                        preparedStatement.setObject(i + 1, list.get(i));
                    }
                    preparedStatement.executeUpdate();
                    connection.commit();
                }
            } catch (Exception e) {
            	logger.info("网银支付异常："+e);
                e.printStackTrace();
                return ret_str_failed;
            }
            return ret_str_success;
        } catch (Exception e) {
        	logger.info("网银支付异常："+e);
            return ret_str_failed;
        } finally {
            if (payMap.containsKey(order_no)) {
                payMap.remove(order_no);
            }
        }
    }

    /**
     * 扫描支付回调接口
     *
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/scanPayNotify.do")
    @ResponseBody
    public String scanPayNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String order_no = "";// (String) request.getParameter("order_no");
        String trade_no = (String) request.getParameter("trade_no");
        String trade_status = (String) request.getParameter("trade_status");
        // 不同支付商订单参数名称不一样需要匹配
        String[] reqParams = new String[] {"order_no", "orderNo" };
        String[] tradeNoParams = new String[] {"trade_no", "payNo" };

        order_no = PayUtil.matching(reqParams, request);
        trade_no = PayUtil.matching(tradeNoParams, request);

        Map<String, String> infoMap = new HashMap<String, String>();
        Enumeration enu = request.getParameterNames();
        while (enu.hasMoreElements()) {
            String paraName = (String) enu.nextElement();
            infoMap.put(paraName, request.getParameter(paraName).toString());
        }
        logger.info("scanPayNotify:" + JSONUtils.toJSONString(infoMap));
        // 文件记录
        FileLog f = new FileLog();
        Map<String, String> fileMap = new HashMap<String, String>();
        fileMap.put("requestIp", IPTools.getIp(request));
        fileMap.put("requestParams", JSONUtils.toJSONString(infoMap));
        f.setLog("scanPayNotify", fileMap);
        // 验证参数
        if ("".equals(order_no) || null == order_no || "".equals(trade_no) || null == trade_no) {
            return ret_str_success;
        }
        if (payMap.containsKey(order_no)) {
            logger.info("order_no:" + order_no + "重复调用!");
            return ret_str_success;
        }
        payMap.put(order_no, "1");
        try {
            // 验证单据号
            Map<String, Object> map = new HashMap<>();
            map.put("orderno", order_no);
            List<Map<String, String>> lm = userService.selectChickReCharge(map);
            // 单据不存在,直接返回
            if (lm.size() <= 0) {
                logger.info("lm不存在！");
                return ret_str_success;
            }
            Map<String, String> order = lm.get(0);
            // System.out.println("单据信息:"+new JSONObject().fromObject(order));
            // 获取用户信息
            Object ouid = order.get("uid");
            String uid = ouid.toString();
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            map = userService.selectUserById(param);
            JSONObject jmap = new JSONObject().fromObject(map);

            String payId = String.valueOf(order.get("pay_id"));
            if (StringUtils.isNullOrEmpty(payId)) {
                logger.info("支付商id不存在！");
                return ret_str_success;
            }
            List<Map<String, String>> plist = userService.selectYsepaybyId(payId,uid);
            if (plist.size() > 0) {
            } else {
                logger.info("plist不存在！");
                return ret_str_success;
            }
            // 获取配置信息
            Map<String, String> hsConfigMap = plist.get(0);
            // 支付商
            String paymentName = hsConfigMap.get("payment_name").toString();
            if (null == paymentName || "".equals(paymentName)) {
                logger.info("payment_name不存在！");
                return ret_str_success;
            }
            String PaymentConfig = hsConfigMap.get("payment_config").toString();
            // 配置信息
            Map<String, String> pmapsconfig = JSONUtils.toHashMap(PaymentConfig);

            // 判断支付商签名是否正确
            if (paymentName.equals(PayConstant.CONSTANT_SHB) || paymentName.equals(PayConstant.CONSTANT_ZF)
                    || paymentName.equals(PayConstant.CONSTANT_DDB) || paymentName.equals(PayConstant.CONSTANT_WF)) {
                t_trade_status = "SUCCESS";
                ret_str_success = "SUCCESS";
                ret_str_failed = "Signature Error";
                SHBPayServiceImpl shbPay = new SHBPayServiceImpl(pmapsconfig);
                String result = shbPay.scanPayNotity(request);
                if (!"SUCCESS".equals(result)) {
                    logger.info("速汇宝扫码验签失败");
                    return ret_str_success; // 验签失败，业务结束
                }
            } else if (paymentName.equals(PayConstant.CONSTANT_TCP)) {
                // 初始化数据
                ret_str_success = "ok";
                ret_str_failed = "fail";
                String cft_req_result = request.getParameter("result");
                if ("".equals(cft_req_result) || null == cft_req_result) {
                    return ret_str_success;
                }

                t_trade_status = "1";// 订单为1表示成功
                trade_status = cft_req_result;// 获取订单状态
                TCPPayServiceImpl tcp = new TCPPayServiceImpl(pmapsconfig, "scan");
                String tcp_result = tcp.tcpPaycallback(request, response);
                if (!"ok".equalsIgnoreCase(tcp_result)) {
                    return ret_str_success;
                }
                // 支付商订单号
                // trade_no = request.getParameter("payNo");
            } else if (paymentName.equals(PayConstant.CONSTANT_GST) || paymentName.equals(PayConstant.CONSTANT_MKT)) {
                ret_str_success = "success";
                ret_str_failed = "fail";
                t_trade_status = "success";// 订单为success表示成功
                // 订单状态为空返回
                if (StringUtils.isNullOrEmpty(trade_status)) {
                    return ret_str_success;
                }
                // 国盛通微信和支付宝为单独商户号,获取单独微信与支付宝商户号支付key做验证
                String pay_type = JSONObject.fromObject(order).get("pay_type").toString();
                String type = "";
                if (pay_type.equals("微信")) {
                    type = PayConstant.CONSTANT_WX;
                } else if (pay_type.equals("支付宝")) {
                    type = PayConstant.CONSTANT_ALI;
                } else if (pay_type.equals("财付通")) {
                    type = PayConstant.CONSTANT_CFT;
                } else if (pay_type.equals("银联")) {
                    type = PayConstant.CONSTANT_YL;
                } else if (pay_type.equals("京东")) {
                    type = PayConstant.CONSTANT_JD;
                } else if (pay_type.equals("快捷")) {
                    type = PayConstant.CONSTANT_KJ;
                } else {
                    logger.error("GST类型匹配异常");
                    return ret_str_success;
                }

                GSTPayServiceImpl gstPay = new GSTPayServiceImpl(pmapsconfig, type);
                boolean falg = gstPay.validPageNotify(request);
                if (!falg) {
                    logger.error("GST参数验证失败");
                    return ret_str_success;
                }
            } else {
                // 异常请求
                logger.error("异常请求");
                return ret_str_success;
            }

            // 回调日志
            Map<String, Object> cmap = new HashMap<>();
            Map<String, String[]> errorMap = new HashMap<String, String[]>();
            errorMap = request.getParameterMap();
            cmap.put("params", JSONObject.fromObject(errorMap).toString());
            cmap.put("ip", IPTools.getIp(request));
            cmap.put("status", ret_str_failed);
            userService.InsertCallbacklog(cmap);

            try {
                System.out.println("-------------------------网银支付--------------------------");

                JSONObject jo = new JSONObject().fromObject(order);
                String oamount = jo.getString("order_amount");

                String status = order.get("trade_status");
                // 如果单据状态不是交易中则跳过后续步骤
                if (!"处理中".equals(status)) {
                    return ret_str_success;
                }
                Map<String, Object> params = new HashMap<>();
                params.put("uid", uid);
                Map<String, Object> user = userService.selectUserById(params);

                double wallet = Double.parseDouble(jmap.getString("wallet"));
                double amt = Double.parseDouble(oamount);
                String sql2 = "update t_recharge set trade_status = ?,trade_no=? where order_no=?";
                List<String> list = new ArrayList<>();
                Connection connection = null;
                PreparedStatement preparedStatement = null;

                if (t_trade_status.equals(trade_status)) {
                    JSONObject json = JSONObject.fromObject(plist.get(0));
                    Double cj = amt * json.getDouble("dividend_rate");
                    Double dml = (amt + cj) * json.getInt("coding_rate");
                    // System.out.println("单据成功");
                    DecimalFormat df = new DecimalFormat("######0.00");
                    cj = Double.parseDouble(df.format(cj));
                    Map<String, Object> paramsMap = new HashMap<String, Object>();
                    try {
                        paramsMap.put("amt", amt);
                        paramsMap.put("uid", uid);
                        paramsMap.put("wallet", wallet);
                        paramsMap.put("cj", cj);
                        paramsMap.put("tradeNo", trade_no);
                        paramsMap.put("outTradeNo", order_no);
                        paramsMap.put("cagent", user.get("cagent"));
                        paramsMap.put("dml", dml);
                        paramsMap.put("number", "CJ" + System.currentTimeMillis());
                        paramsMap.put("number2", "CK" + System.currentTimeMillis());
                        paramsMap.put("_err", "0");
                        userService.ysePayCallBack(paramsMap);
                        if ("1".equals(paramsMap.get("_err"))) {
                            return ret_str_failed;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ret_str_failed;
                    }
                    System.out.println("UID : " + uid);
                    System.out.println("Username : " + jmap.getString("username"));
                    System.out.println("order : " + jo);
                    System.out.println("amt : " + amt);
                    System.out.println("dml : " + dml);
                    System.out.println("cj : " + cj);
                    System.out.println("------------------订单完成----------------------");
                } else {
                    // 更新转账信息
                    connection = JDBCTools.getConnection();
                    connection.setAutoCommit(false);

                    list.clear();
                    list.add("faild");
                    list.add(trade_no);
                    list.add(order_no);

                    preparedStatement = connection.prepareStatement(sql2);
                    for (int i = 0; i < list.size(); i++) {
                        preparedStatement.setObject(i + 1, list.get(i));
                    }
                    preparedStatement.executeUpdate();
                    connection.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return ret_str_failed;
            }

            return ret_str_success;
        } catch (Exception e) {
            return ret_str_failed;
        } finally {
            if (payMap.containsKey(order_no)) {
                payMap.remove(order_no);
            }
        }
    }

    public JSONObject getPublicKey(String merId) {
        // 201709181816002
        List<Map<String, String>> ysepay = userService.selectTcagentYsepay("JFK");
        String paymentConfig = "";
        JSONObject jsStr = new JSONObject();
        for (int i = 0; i < ysepay.size(); i++) {
            paymentConfig = ysepay.get(i).get("payment_config");
            jsStr = JSONObject.fromObject(paymentConfig);
            if (merId.equals(jsStr.get("merId"))) {
                return jsStr;
            }
        }
        return jsStr;
    }

    private String getRandomString(int length) {
		// 定义一个字符串（A-Z，a-z，0-9）即62位；
		String str = "zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
		// 由Random生成随机数
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		// 长度为几就循环几次
		for (int i = 0; i < length; ++i) {
			// 产生0-61的数字
			int number = random.nextInt(62);
			// 将产生的数字通过length次承载到sb中
			sb.append(str.charAt(number));
		}
		// 将承载的字符转换成字符串
		return sb.toString();
	}

}
