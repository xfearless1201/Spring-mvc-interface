package com.cn.tianxia.pay.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName XmlUtils
 * @Description xml解析工具类
 * @author Hardy
 * @Date 2018年12月29日 下午2:45:25
 * @version 1.0.0
 */
public class XmlUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(XmlUtils.class);
    
    /**
     * 
     * @Description 解析xml文件
     * @param xmlStr
     * @return
     */
    public static JSONObject parseXml(String xmlStr) {
        logger.info("解析xml开始===========START================");
        try {
            JSONObject jsonObject = new JSONObject();
            SAXReader reader = new SAXReader();
            InputStream in = new ByteArrayInputStream(xmlStr.getBytes("utf-8"));
            Document dom = reader.read(in);
            Element root=dom.getRootElement();
            Iterator<Element> eles = root.elementIterator();
            while(eles.hasNext()){
                Element node = eles.next();
                String key = node.getName();
                String val = node.getStringValue();
                jsonObject.put(key, val);  
            }
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("解析xml异常:{}",e.getMessage());
        }
        return null;
    }
}
