package com.cn.tianxia.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import com.cn.tianxia.pay.utils.RandomUtils;
import com.cn.tianxia.pay.ys.util.DateUtil;

import net.sf.json.JSONObject;

/**
 * 
 * @author zw
 *
 */
public class PayUtil {

	/**
	 * 生成订单号
	 *
	 * @param provider 支付商编号
	 * @param cagent   平台商编号
	 * @return
	 */
	public static String generatorPayOrderNo(String provider, String cagent) {
		String order_no = createOrderNo(provider, cagent);

		// 特殊生成轻易付的订单
		if (PayConstant.CONSTANT_QYF.equals(provider)) {
			String currTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			String strTime = currTime.substring(8, currTime.length());
			String strRandom = RandomUtils.generateNumberStr(4);
			String strReq = strTime + strRandom;
			order_no = currTime + strReq + provider + cagent.toLowerCase();
		}
		/***added by hb at 2018-06-22 全谷迪卿支付订单号长度减少到17位 start */
		// 特殊生成全谷迪卿订单号
		if (PayConstant.CONSTANT_QGDL.equals(provider)) {
			String payName = PayConstant.CONSTANT_QGDL.substring(0, 2);
			order_no = payName + new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date());
		}
		/*** added by hb at 2018-06-22 全谷迪卿支付订单号长度减少到17位 end */
		/***added by hb at 2018-06-25 免签支付订单号长度20位 start */
		// 特殊生成全谷迪卿订单号
		if (PayConstant.CONSTANT_MQZF.equals(provider)) {
			order_no = "MQ" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + RandomUtils.generateString(6);
		}
		//汇银付支付订单号只要30位
		if (PayConstant.CONSTANT_HYFZF.equals(provider)) {
			order_no = "HYZF" + order_no.substring(5, 31);
		}
		//万通支付订单号只要20位;大富支付订单号只要20位
		if (PayConstant.CONSTANT_WT.equals(provider) || PayConstant.CONSTANT_DAF.equals(provider)
				|| PayConstant.CONSTANT_NWT.equals(provider) || PayConstant.CONSTANT_NOMQ.equalsIgnoreCase(provider)
				|| PayConstant.CONSTANT_BJYX.equals(provider) || PayConstant.CONSTANT_ABH.equals(provider)
				|| PayConstant.CONSTANT_YIFA.equals(provider) || PayConstant.CONSTANT_YBT.equals(provider)
				|| PayConstant.CONSTANT_SYB.equals(provider) || PayConstant.CONSTANT_SLJH.equals(provider)
				|| PayConstant.CONSTANT_STZF.equals(provider) || PayConstant.CONSTANT_XINFA.equals(provider)) {
			order_no = order_no.substring(0, 21);
		}

		/** 踢踢支付 和 踢踢支付2 订单号只能为20位 **/
		if (PayConstant.CONSTANT_TITI.equals(provider) || PayConstant.CONSTANT_TT2.equals(provider)
		        || PayConstant.CONSTANT_YTBP.equals(provider)) {
			String payName = provider.substring(0,2);
			String agent = cagent.substring(0,3);
			order_no = payName + agent + new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date());
		}

		//珊瑚支付订单号20位
		if(PayConstant.CONSTANT_CORAL.equals(provider)){
		    String keyup_prefix=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
	        String keyup_append=String.valueOf(new Random().nextInt(899999)+100000);
	        order_no=keyup_prefix+keyup_append;
		}
		
		if (PayConstant.CONSTANT_QIANYING.equals(provider)) {
			order_no = "QIANY" + order_no.substring(8, order_no.length());
		}
		//信付宝支付订单号：仅允许字母或数字类型,不超过22个字符，不要有中文
		if (PayConstant.CONSTANT_XFB.equals(provider) || PayConstant.CONSTANT_EASY.equals(provider)) {
			order_no = order_no.substring(0, 22);
		}
		if (PayConstant.CONSTANT_TXZF.equalsIgnoreCase(provider) || PayConstant.CONSTANT_ZHUI.equals(provider)
		        || PayConstant.CONSTANT_HPZF.equals(provider) || PayConstant.CONSTANT_FYZF.equals(provider)
		        || PayConstant.CONSTANT_TTZF.equals(provider) || PayConstant.CONSTANT_YXIN.equals(provider)) {
			order_no = order_no.substring(0, 30);
		}
		/*** added by hb at 2018-06-22 全谷迪卿支付订单号长度20位 end */
		//鼎盛支付 订单号不能22位
		if (PayConstant.CONSTANT_DSZF.equals(provider)) {
			order_no = order_no.substring(0, 22);
		}
		//iipays支付订单号不能操过 30 位
		if(PayConstant.CONSTANT_IIZF.equals(provider)){
			order_no = order_no.substring(0,30);
		}
		//万通XX 支付不能超过 20位
		if(PayConstant.CONSTANT_WTXX.equals(provider)){
			order_no = order_no.substring(0,20);
		}
		//宜橙支付订单号30位
		if(PayConstant.CONSTANT_YICZF.equals(provider) || PayConstant.CONSTANT_SHAN.equals(provider)){
			order_no = order_no.substring(0,30);
		}
		//大宝天下支付订单号22位
		if(PayConstant.CONSTANT_DBTX.equals(provider)){
			int dbtxLen = PayConstant.CONSTANT_DBTX.length();
			String substr = order_no.substring(dbtxLen+3, dbtxLen+12);
			order_no = order_no.replace(substr, "");
		}
		//宏达支付订单号30位
		if(PayConstant.CONSTANT_HDZF.equals(provider) || PayConstant.CONSTANT_JIDA.equals(provider)){
			order_no = order_no.substring(0,30);
		}
		return order_no;
	}

	public static String createOrderNo(String cagent, String pcode) {
		// ---------------生成支付订单号 开始------------------------
		// 14位 当前时间 yyyyMMddHHmmss
		String currTime = DateUtil.getCurrentDate("yyyyMMddHHmmss");
		// 8位日期
		String strTime = currTime.substring(8, currTime.length());
		// 四位随机数
		String strRandom = DateUtil.getRandom(4) + "";
		// 10位序列号,可以自行调整。
		String strReq = strTime + strRandom;
		// 订单号，此处用时间加随机数生成，商户根据自己情况调整，只要保持全局唯一就行
		String order_no = pcode + cagent.toLowerCase() + currTime + strReq;
		// ---------------生成支付订单号 结束------------------------
		return order_no;
	}

	/**
	 * 生成订单号
	 * 
	 * @param user
	 * @return
	 */
	public static String getOrderNo(Map<String, Object> user, String pcode) {
		// ---------------生成支付订单号 开始------------------------
		// 14位 当前时间 yyyyMMddHHmmss
		String currTime = DateUtil.getCurrentDate("yyyyMMddHHmmss");
		// 8位日期
		String strTime = currTime.substring(8, currTime.length());
		// 四位随机数
		String strRandom = DateUtil.getRandom(4) + "";
		// 10位序列号,可以自行调整。
		String strReq = strTime + strRandom;
		// 订单号，此处用时间加随机数生成，商户根据自己情况调整，只要保持全局唯一就行
		String order_no = pcode + user.get("cagent").toString().toLowerCase() + currTime + strReq;
		// ---------------生成支付订单号 结束------------------------
		return order_no;
	}

	/**
	 * 扫码接口返回
	 * 
	 * @param status
	 * @param type
	 * @param msg
	 * @param username
	 * @param amount
	 * @param order_no
	 * @param ret_str
	 * @return
	 */
	public static JSONObject returnPayJson(String status, String type, String msg, String username, double amount,
			String order_no, String ret_str) {
		JSONObject json = new JSONObject();
		json.put("res_type", type);
		json.put("status", status);
		json.put("msg", msg);
		json.put("acount", String.valueOf(amount));
		json.put("user_name", username);
		json.put("order_no", order_no);

		if (type.equals("1")) {
			// 表单提交方式
			json.put("html", ret_str);
		} else if (type.equals("2")) {
			// 二维码图片生成
			json.put("qrcode", ret_str);
		} else if (type.equals("3")) {
			// 二维码图片连接
			json.put("qrcode_url", ret_str);
		} else if (type.equals("4")) {
			// 跳转连接
			json.put("html", ret_str);
		}
		return json;
	}

	/**
	 * 网银返回 type分为三种 1form 表单提交 2jsp jsp页面提交表单 3 link 详见QYZF 跳转连接
	 * *********************XM GT JH JHZ 使用jsp类型
	 * 
	 * @param status
	 * @param type
	 * @param content
	 * @param pay_url
	 * @param jsp_name
	 * @return
	 */
	public static JSONObject returnWYPayJson(String status, String type, String content, String pay_url,
			String jsp_name) {
		JSONObject json = new JSONObject();
		json.put("status", status);
		json.put("type", type);
		if (type.equals("form")) {
			// 表单提交方式
			json.put("form", content);
			json.put("redirect", "redirect:http://" + pay_url + "/pay.action");
		} else if (type.equals("jsp")) {
			// jsp页面提交
			json.put("jsp_name", jsp_name);
			json.put("jsp_content", content);
		} else if (type.equals("link")) {
			// 跳转连接
			json.put("link", content);
		}else if(type.equals("jumpPay")){
		    json.put("jumpPay", content);
            json.put("redirect", "redirect:http://" + pay_url);
		}
		return json;
	}

	/**
	 * 适配参数
	 * 
	 * @param params
	 * @param request
	 * @return
	 */
	public static String matching(String[] params, HttpServletRequest request) {
		String mathingStr = "";
		Map<String, String[]> Map = request.getParameterMap();
		for (int i = 0; i < params.length; i++)
			if (Map.containsKey(params[i])) {
				return mathingStr = Map.get(params[i])[0];
			}
		return mathingStr;
	}

}
