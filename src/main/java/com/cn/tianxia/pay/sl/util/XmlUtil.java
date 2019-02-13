package com.cn.tianxia.pay.sl.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alibaba.fastjson.JSONArray;

/**
 * 
 * @ClassName:  XmlUtil   
 * @Description:生成xml工具类
 * @author: Hardy
 * @date:   2018年8月22日 下午6:30:36   
 *     
 * @Copyright: 天下科技 
 *
 */
public class XmlUtil {

	private static DocumentBuilderFactory factory = null;
	private static DocumentBuilder builder = null;
	private static Document document;
	static {
		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/***
	 * 
	 * @Description 创建请求xml文件
	 * @param data
	 * @return
	 */
	public static String createXml(Map<String,String> data){
		/**
		 * 下面的参数必须传,可以为空,但必须参与签名
		 */
		data.put("version","1.0.1");
		data.put("application","SubmitOrder");
		data.put("bizType", "");
		data.put("credentialNo", "");
		data.put("credentialType", "");
		data.put("guaranteeAmt", "0");
		data.put("msgExt", "");
		data.put("payerId", "");
		data.put("salerId", "");
		data.put("userMobileNo", "");
		data.put("userName", "");
		data.put("userType", "1");
		data.put("accountType", "1");
		data.put("rptType", "1");
		data.put("payMode", "0");
		//排序
		Map<String,String> treemap = new TreeMap<String,String>(new Comparator<String>() {

			@Override
			public int compare(String key1, String key2) {
				return key1.compareTo(key2);
			}
		});
		
		treemap.putAll(data);
		
		document = builder.newDocument();
		Element root = document.createElement("message");
		//遍历key
		Iterator<String> keys = data.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			String value = data.get(key);
			root.setAttribute(key, value);
		}
		document.appendChild(root);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			DOMSource source = new DOMSource(document);
			transformer.setOutputProperty(OutputKeys.ENCODING,"utf-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			bos = new ByteArrayOutputStream();
			dos = new DataOutputStream(bos);
			StreamResult result = new StreamResult(dos);
			transformer.transform(source, result);
			String xml = new String(bos.toByteArray(),"utf-8");
			System.err.println(xml);
			return new String(bos.toByteArray(),"utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(dos != null){
				try {
					dos.close();
				} catch (Exception e2) {}
			}
			if(bos != null){
				try {
					bos.close();
				} catch (Exception e2) {}
			}
		}
		return "";
	}
	
	
	/**
	 * xml转换成支付通知响应
	 * @param xmlStr 响应报文
	 * @return 响应对象
	 */
	public static Map<String,String> formatXmlStr(String xmlStr) {
		ByteArrayInputStream bais = null;
		DataInputStream dis = null;
		try {
			bais = new ByteArrayInputStream(xmlStr.getBytes("utf-8"));
			dis = new DataInputStream(bais);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
			document = builder.parse(dis);
			document.getDocumentElement().normalize();
			NamedNodeMap nnm = document.getFirstChild().getAttributes();
			Map<String,String> response = new HashMap<String,String>();
			for(int j = 0; j<nnm.getLength(); j++){
				Node nodeitm= nnm.item(j);
				String key = nodeitm.getNodeName();
				String value = nodeitm.getNodeValue();
				response.put(key, value);
			}
			List<Map<String,String>> list = new ArrayList<Map<String,String>>();
			NodeList messageChildList = document.getFirstChild().getChildNodes();
			for(int i = 0; i<messageChildList.getLength(); i++){
				Node node = messageChildList.item(i);
				NodeList nodeCList = node.getChildNodes();
				if(node.getNodeName().equals("deductList")){
					for(int j = 0; j<nodeCList.getLength();j++){
						nnm = nodeCList.item(j).getAttributes();
						if(nnm == null)continue;
						Map<String,String> deductMap = new HashMap<String,String>();
						for(int k = 0; k<nnm.getLength(); k++){
							Node nodeitm= nnm.item(k);
							String key = nodeitm.getNodeName();
							String value = nodeitm.getNodeValue();
							deductMap.put(key, value);
						}
						list.add(deductMap);
					}
					response.put("deductList", JSONArray.toJSONString(list));
				} else if(node.getNodeName().equals("refundList")){
					for(int j = 0; j<nodeCList.getLength();j++){
						nnm = nodeCList.item(j).getAttributes();
						if(nnm == null)continue;
						Map<String,String> refundMap = new HashMap<String,String>();
						for(int k = 0; k<nnm.getLength(); k++){
							Node nodeitm= nnm.item(k);
							String key = nodeitm.getNodeName();
							String value = nodeitm.getNodeValue();
							refundMap.put(key, value);
						}
						list.add(refundMap);
					}
					response.put("refundList", JSONArray.toJSONString(list));
				}
			}
			return response;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(dis != null)try {dis.close();} catch (Exception e2) {}
			if(bais != null)try {bais.close();} catch (Exception e2) {}
		}
		return null;
	}
	
	public static String createNotifyXml(Map<String,String> data){
	    
	    if(data != null && !data.isEmpty()){
	      //排序
	        Map<String,String> treemap = new TreeMap<String,String>(new Comparator<String>() {

	            @Override
	            public int compare(String key1, String key2) {
	                return key1.compareTo(key2);
	            }
	        });
	        
	        treemap.putAll(data);
	        
	        //订单列表
	        List deductList = null;
	        if(data.containsKey("deductList")){
	            deductList = JSONArray.parseArray(data.get("deductList"), Map.class);
	            data.remove("deductList");
	        }
	        
	        //退款列表
	        List refundList = null;
	        if(data.containsKey("refundList")){
	            refundList = JSONArray.parseArray(data.get("deductList"), Map.class);
	            data.remove("refundList");
	        }
	        
	        
	        document = builder.newDocument();
	        Element root = document.createElement("message");
	        //遍历key
	        Iterator<String> keys = data.keySet().iterator();
	        while (keys.hasNext()) {
	            String key = keys.next();
	            String value = data.get(key);
	            root.setAttribute(key, value);
	        }
	        
	        if(deductList != null && deductList.size() > 0){
	            Element createElement = document.createElement("deductList");
	            for (Object deduct:deductList) {
                    Map<String,String> deductMap = (Map<String, String>) deduct;
                    if(deductMap != null && !deductMap.isEmpty()){
                        Element item = document.createElement("item");
                        item.setAttribute("payOrderId", deductMap.get("payOrderId"));
                        item.setAttribute("payAmt", deductMap.get("payAmt"));
                        item.setAttribute("payStatus", deductMap.get("payStatus"));
                        item.setAttribute("payDesc", deductMap.get("payDesc"));
                        item.setAttribute("payTime", deductMap.get("payTime"));
                        createElement.appendChild(item);
                    }
                }
	            root.appendChild(createElement);
	        }
	        
//	        if(refundList != null && refundList.size() > 0){
//	            Element createElement = document.createElement("refundList");
//                for (Object refund:refundList) {
//                    Map<String,String> refundMap = (Map<String, String>) refund;
//                    if(refundMap != null && !refundMap.isEmpty()){
//                        Element item = document.createElement("item");
//                        createElement.appendChild(item);
//                    }
//                }
//                root.appendChild(createElement);
//	        }
	        
	        document.appendChild(root);
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        DataOutputStream dos = new DataOutputStream(bos);
	        try {
	            TransformerFactory tf = TransformerFactory.newInstance();
	            Transformer transformer = tf.newTransformer();
	            DOMSource source = new DOMSource(document);
	            transformer.setOutputProperty(OutputKeys.ENCODING,"utf-8");
	            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	            bos = new ByteArrayOutputStream();
	            dos = new DataOutputStream(bos);
	            StreamResult result = new StreamResult(dos);
	            transformer.transform(source, result);
	            String xml = new String(bos.toByteArray(),"utf-8");
	            System.err.println(xml);
	            return new String(bos.toByteArray(),"utf-8");
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            if(dos != null){
	                try {
	                    dos.close();
	                } catch (Exception e2) {}
	            }
	            if(bos != null){
	                try {
	                    bos.close();
	                } catch (Exception e2) {}
	            }
	        }
	    }
	    return "";
	}
}
