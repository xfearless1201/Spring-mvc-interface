package com.cn.tianxia.pay.mob.util;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;



/** 
 * ʹ�� Map��key�������� 
 * @param map 
 * @return 
 */  
public class MapKeyComparator implements Comparator<String> {
    public int compare(String str1, String str2) {  
        return str1.compareTo(str2);  
    }  
    
    
    /** 
     * ʹ�� Map��key�������� Map<String,Object>
     * @param map 
     * @return 
     */  
    public static Map<String, Object> sortMapByKey(Map<String, Object> map) {  
        if (map == null || map.isEmpty()) {  
            return null;  
        }  
        Map<String, Object> sortMap = new TreeMap<String, Object>(new MapKeyComparator());  
        sortMap.putAll(map);  
        return sortMap;  
    }
    /** 
     * ʹ�� Map��key�������� Map<String,String>
     * @param map 
     * @return 
     */ 
    public static Map<String, String> sortMapByKey1(Map<String, String> map) {  
        if (map == null || map.isEmpty()) {  
            return null;  
        }  
        Map<String, String> sortMap = new TreeMap<String, String>(new MapKeyComparator());  
        sortMap.putAll(map);  
        return sortMap;  
    }
    
    
    public static void main(String[] args) {  
//        Map map = new TreeMap<String,String>();  
//        map.put("KFC", "kfc");  
//        map.put("WNBA", "wnba");  
//        map.put("NBA", "nba");  
//        map.put("CBA", "cba");  
//        Map<String, String> resultMap = sortMapByKey(map);    //��Key��������  
//        for (Map.Entry<String, String> entry : resultMap.entrySet()) {  
//            System.out.println(entry.getKey() + " " + entry.getValue());  
//        }  
    } 
    

}
