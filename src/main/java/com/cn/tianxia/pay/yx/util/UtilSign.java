/**
 * 
 */
/**
 * @author Administrator
 *
 */
package com.cn.tianxia.pay.yx.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UtilSign{
	
	public static String GetSHAstr(Map<String,String> Parm,String Key){		
		if(Parm.containsKey("sign")){
			Parm.remove("sign");//������sign
		}
		if(Parm.containsKey("signType")){
			Parm.remove("signType");//������sign
		}
		List<String> SortStr = Ksort(Parm); //����
		String Md5Str = CreateLinkstring(Parm,SortStr);
		System.out.println("***********"+Md5Str);
		return SHAUtils.sign(Md5Str+Key, "utf-8");
	}
	
	/**
	 * ����  (����)
	 * @param Parm
	 * @return
	 */
	public static List<String> Ksort(Map<String,String> Parm){ 
		List<String> SMapKeyList = new ArrayList<String>(Parm.keySet()); 
		Collections.sort(SMapKeyList);
		return SMapKeyList;
	} 
	
	/**
	 * �ж�ֵ�Ƿ�Ϊ�� FALSE Ϊ����  TRUE Ϊ��
	 * @param Temp
	 * @return
	 */
	public static boolean StrEmpty(String Temp){
		if(null == Temp || Temp.isEmpty()){
			return true;
		}
		return false;
	}
	
	/**
	 * ƴ�ӱ���
	 * @param Parm
	 * @param SortStr
	 * @return
	 */
	public static String CreateLinkstring(Map<String,String> Parm,List<String> SortStr){
		String LinkStr = "";
		for(int i=0;i<SortStr.size();i++){
			if(!StrEmpty(Parm.get(SortStr.get(i).toString()))){
				LinkStr += SortStr.get(i) +"="+Parm.get(SortStr.get(i).toString());
				if((i+1)<SortStr.size()){
					LinkStr +="&";
				}
			}
		}
		return LinkStr;
	}
}