package com.cn.tianxia.pay.tx.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

//import com.sk.core.exception.SkBaseException;
//
//import net.sf.json.JSONObject;


public abstract class AbstractRequestDTO implements IRequestDTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5189050649731297682L;

	/** serialVersionUID */

	@NotNull(message="版本号[version]必输")
	@Size(min=1,max=10,message="版本号[version]长度有误")
	private String version;
	
	@NotNull(message="商户编号[merId]必输")
	@Size(min=1,max=15,message="商户编号[merId]长度有误")
	private String merId;
	
	@NotNull(message="签名值[sign]必输")
	@Size(min=1,max=64,message="签名值[sign]长度有误")
	private String sign;
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getMerId() {
		return merId;
	}

	public void setMerId(String merId) {
		this.merId = merId;
	}

//	// --
//	public IRequestDTO fromJSONObject(JSONObject data) throws SkBaseException {
//		try {
//			JSONObject body = (JSONObject) data.get("body");
//			return (IRequestDTO) JSONObject.toBean(body, this.getClass());
//		} catch (Exception e) {
//			throw new SkBaseException("100000");
//		}
//	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public TreeMap<String,String>  toTreeMap(){
		List<Field> fieldList = new ArrayList<Field>() ;
		Field [] fields = this.getClass().getDeclaredFields();
		if(fields != null&&fields.length>0) {
			 fieldList.addAll(Arrays.asList(fields));
		}
		Field [] fields2 = this.getClass().getSuperclass().getDeclaredFields();
		if(fields2 != null&&fields2.length>0) {
			 fieldList.addAll(Arrays.asList(fields2));
		}
		TreeMap<String,String> map  =new TreeMap<String,String>();
		for(Field field : fieldList) {
			if("serialVersionUID".equals(field.getName())) {
				continue;
			}
			String fldVal = getFieldValue(this,  field.getName()).toString();
			map.put(field.getName(), fldVal);
		}
		return map;
	}
	
	public String toString(){
		List<Field> fieldList = new ArrayList<Field>() ;
		Field [] fields = this.getClass().getDeclaredFields();
		if(fields != null&&fields.length>0) {
			 fieldList.addAll(Arrays.asList(fields));
		}
		Field [] fields2 = this.getClass().getSuperclass().getDeclaredFields();
		if(fields2 != null&&fields2.length>0) {
			 fieldList.addAll(Arrays.asList(fields2));
		}
		StringBuffer buf = new StringBuffer();
		for(Field field : fieldList) {
			String fldVal = getFieldValue(this,  field.getName()).toString();
			buf.append("["+field.getName()+":"+fldVal+"]");
		}
		return buf.toString();
	}

	private String getFieldValue(Object owner, String fieldName) {
		Object obj = invokeMethod(owner, fieldName, null);
		
		if(obj != null) {
			return obj.toString();
		}
		else {
			return "";
		}
	}

	/**
	 * 
	 * 执行某个Field的getField方法
	 * 
	 * @param owner
	 *            类
	 * @param fieldName
	 *            类的属性名称
	 * @param args
	 *            参数，默认为null
	 * @return
	 */
	private Object invokeMethod(Object owner, String fieldName, Object[] args) {
		Class<? extends Object> ownerClass = owner.getClass();

		// fieldName -> FieldName
		String methodName = fieldName.substring(0, 1).toUpperCase()
				+ fieldName.substring(1);

		Method method = null;
		try {
			method = ownerClass.getMethod("get" + methodName);
		} catch (SecurityException e) {

			// e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// e.printStackTrace();
			return "";
		}

		// invoke getMethod
		try {
			return method.invoke(owner);
		} catch (Exception e) {
			return "";
		}
	}
}
