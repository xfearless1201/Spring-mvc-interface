package com.cn.tianxia.pay.tjf.utils;

import org.dom4j.Attribute;
import org.dom4j.Element;

import java.util.List;
import java.util.Map;

/**
 * 
 * 解析xml数据，将xml数据解析好后放入map中，目前只支持元素名不重复的xml数据 取数据时直接中map中根据key值取值。
 * key采用元素路径的方式.元素用类似/root/element/data的路径形式，属性值用root/element/data/@id的形式，id是属性名称。
 * @author luochong
 *
 */
@SuppressWarnings("unchecked")
public class XMLParserUtil {
	public static void parse(String xmlData, Map<String, String> resultMap) throws Exception {
		org.dom4j.Document doc = org.dom4j.DocumentHelper.parseText(xmlData);
		Element root = doc.getRootElement();
		parseNode(root, resultMap);
	}
	
	private static void parseNode(Element node, Map<String, String> resultMap) {
		List attList = node.attributes();
		List eleList = node.elements();
		
		for (int i=0; i<attList.size(); i++) {
			Attribute att = (Attribute) attList.get(i);
			resultMap.put(att.getPath(), att.getText().trim());
		}
		
		resultMap.put(node.getPath(), node.getTextTrim());
		for (int i=0; i<eleList.size(); i++) {
			parseNode((Element) eleList.get(i), resultMap);
		}
	}
}
