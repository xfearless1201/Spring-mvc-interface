package com.cn.tianxia.pay.gcc.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.cn.tianxia.pay.gc.util.TfcpayBussinessException;
import com.cn.tianxia.pay.gc.util.TfcpayException;
import com.cn.tianxia.pay.gc.util.TfcpaySignException;
import com.cn.tianxia.pay.impl.GCPayServiceImpl;

public class TfcpayUtil {
	
	private final static Logger logger = LoggerFactory.getLogger(TfcpayUtil.class);
	/**
	 * 横线
	 */
	public static final String HORIZONTAL = "-";

	/***
	 * 生成sign,  并转Map<String,Object> 为Map<String,String>
	 * @param params  参数集合
	 * @param signKey 密钥
	 * @return
	 */
	
	public static Map<String,String> flattenParamsAndSign(Map<String,Object> params, String signKey){
		Map<String, String> data=new TreeMap<>();
		try {
			data = flattenParams(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String sign=generateMD5(data, signKey);
		data.put("sign", sign);
		return data;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String,String> parseResult(String content){
		
		if(StringUtils.isBlank(content)){
			return null;
		}
		
		Map<String,String> result=(Map<String, String>) JSONObject.parse(content);
		return result;
		
	}
	
	/***
	 * 对返回数据进行验签
	 * @param params 返回数据map集合
	 * @param signKey
	 * @return
	 * @throws SignException 
	 */
	public static boolean VerifySign(Map<String,String> params,String signKey) throws TfcpaySignException,TfcpayBussinessException{
		
		if(params==null||params.isEmpty()||StringUtils.isBlank(signKey)){
			throw new TfcpaySignException("验签参数为空",params.toString());
		}
		String resultSign=params.get("sign");
		String code=params.get("code");
		
		//如果sign为空并且code不为空的时候判断是code失败,不需做验签
		if(StringUtils.isBlank(resultSign)&&!StringUtils.isBlank(code)){
			throw new TfcpayBussinessException("业务状态:"+code,params.toString());
		}
		
		String sign=generateMD5(params,signKey);
		
		if(resultSign.equals(sign)){
			return true;
		}else{
			throw new TfcpaySignException("验签失败,签名不一致",params.toString());
		}
		
	}
	
	
	/***
	 * 对返回数据进行验签
	 * @param content 返回数据content
	 * @param signKey
	 * @return
	 */
	public static boolean VerifySign(String content,String signKey){
		
		//转换结果为map
		Map<String,String> params=parseResult(content);
		
		String resultSign=params.get("sign");
		if(StringUtils.isBlank(resultSign)){
			return false;
		}
		String sign=generateMD5(params,signKey);
		
		if(resultSign.equals(sign)){
			return true;
		}
		return false;
	}
	
	
	/***
	 * 
	 * 生成sign
	 * @author lihejia
	 * @param params  参数集合
	 * @param signKey 密钥
	 * @return
	 */
	
	public static String generateMD5(Map<String,String> params, String  signKey){
		
//		if(StringUtils.isBlank(signKey) || params==null || params.isEmpty()){
//			return null;
//		}
//		List<String> sortList = new ArrayList<>();
//		for(Map.Entry<String,String> entry : params.entrySet()){
//			String key = entry.getKey();
//			String val = String.valueOf(entry.getValue());
//			//去除空值和sign值
//			if(StringUtils.isBlank(val)||"sign".equals(key)){
//				continue;
//			}
//			sortList.add(key + "=" + val);
//		}
//		Collections.sort(sortList);
//		sortList.add(signKey);
//		
//		String preSign = String.join("&", sortList);
//		System.out.println("默认："+preSign);
////		System.out.println("默认MD5:"+Md5.utf8(preSign));
//		
//		String mrMd5=Md5.utf8(preSign);
		
		String buff = "";
		try {
			List infoIds = new ArrayList(params.entrySet());

			Collections.sort(infoIds, new Comparator<Map.Entry<String, String>>() {
				public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
					return ((String) o1.getKey()).toString().compareTo((String) o2.getKey());
				}
			});
			for (int i = 0; i < infoIds.size(); i++) {
				Map.Entry item = (Map.Entry) infoIds.get(i);

				if (item.getKey() != "") {
					String key = String.valueOf(item.getKey());
					String val = String.valueOf(item.getValue());
					if ("".equals(val) ||"sign".equals(key)) {
						continue;
					}
					buff = buff + key + "=" + val + "&";
				}
			}
			if (!buff.isEmpty())
				buff = buff.substring(0, buff.length() - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		logger.info("字符原串:"+buff+"&"+signKey);
		String md5Str=buff+"&"+signKey;
//		String gzMd5=Md5.utf8(buff+"&"+signKey);
//		System.out.println("跟新MD5:"+gzMd5);
//		
//		System.out.println("MD5对比结果"+mrMd5.equals(gzMd5));
		
		return Md5.utf8(md5Str);
	}


	/**
	 * 生成下一个UUID
	 * @return
	 */
	public static String nextUUID(){
		return UUID.randomUUID().toString().replace(HORIZONTAL, StringUtils.EMPTY);
	}
	


	/**
	 * 从request中获得参数Map，并返回可读的Map
	 * @param properties
	 * @return
	 */
	public static Map<String,String> requestMaptoMap(Map<String,String[]> properties){
	    // 返回值Map
	    Map<String,String> returnMap = new HashMap<String,String>();
	    Iterator<Entry<String, String[]>> entries = properties.entrySet().iterator();
	    Map.Entry<String, String[]> entry;
	   
	    while (entries.hasNext()) {
	        entry = (Map.Entry<String, String[]>) entries.next();
	        String name = (String) entry.getKey();
	        Object valueObj = entry.getValue();
	        String value=null;
	        if(null == valueObj){
	            value = "";
	        }else if(valueObj instanceof String[]){
	            String[] values = (String[])valueObj;
	            for(int i=0;i<values.length;i++){
	                value = values[i] + ",";
	            }
	            value = value.substring(0, value.length()-1);
	        }else{
	            value = valueObj.toString();
	        }
	        returnMap.put(name, value);
	    }
	    
	    return returnMap;
	}
	
	
	  /**
     * @param params
     * @return
     * @throws Exception
     */
    public static Map<String, String> flattenParams(Map<String, Object> params)
            throws Exception {
        if (params == null) {
            return new HashMap<String, String>();
        }
        Map<String, String> flatParams = new HashMap<String, String>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?>) {
                Map<String, Object> flatNestedMap = new HashMap<String, Object>();
                Map<?, ?> nestedMap = (Map<?, ?>) value;
                for (Map.Entry<?, ?> nestedEntry : nestedMap.entrySet()) {
                    flatNestedMap.put(
                            String.format("%s[%s]", key, nestedEntry.getKey()),
                            nestedEntry.getValue());
                }
                flatParams.putAll(flattenParams(flatNestedMap));
            } else if (value instanceof ArrayList<?>) {
                ArrayList<?> ar = (ArrayList<?>) value;
                Map<String, Object> flatNestedMap = new HashMap<String, Object>();
                int size = ar.size();
                for (int i = 0; i < size; i++) {
                    flatNestedMap.put(String.format("%s[%d]", key, i), ar.get(i));
                }
                flatParams.putAll(flattenParams(flatNestedMap));
            } 
            else if ("".equals(value)) {
               
            } else if (value == null) {
                flatParams.put(key, "");
            } else {
                flatParams.put(key, value.toString());
            }
        }
        return flatParams;
    }
    
	/**判断请求后的业务状态,此判断仅供参考*/
	private static void judgeStatus(Map<String, String> resultMap,String key){
		//先判断通信状态
		if(!"SUCCESS".equals(resultMap.get("code"))){
			System.out.println("通信状态失败,");
		   return ;
		}
		//校验签名
		try {
			TfcpayUtil.VerifySign(resultMap,key);
		} catch (TfcpaySignException e) {
			System.out.println(e.getMessage());
			return ;	
		} catch (TfcpayBussinessException e) {
			System.out.println("业务异常"+e.getMessage());
			e.printStackTrace();
		}
		//判断业务状态
		if("SUCCESS".equals(resultMap.get("resultCode"))){
			////TODO 请处理下单成功的业务逻辑
			System.out.println("操作成功!!");
		}else{
			System.out.println("操作失败!!!\n失败原因:"+resultMap.get("errCodeDes"));
			//TODO 请处理下单失败的业务逻辑
		}
	}
	
	
	/**发送http请求*/
	public static void sendTo(Map<String, Object> param,String url,String key){
		Map<String, String> data = null;
		try {
			data = TfcpayUtil.flattenParamsAndSign(param, key);
			String result = HttpUtil.post(url, data);
			System.out.println("请求返回内容\n" + result);
			Map<String, String> resultMap = TfcpayUtil.parseResult(result);
			judgeStatus(resultMap, key);
		}catch (IOException e) {
			System.out.println("请求异常");
			e.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
}
