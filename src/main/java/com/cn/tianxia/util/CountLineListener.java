package com.cn.tianxia.util;   
   
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.servlet.http.HttpSessionEvent;   
import javax.servlet.http.HttpSessionListener;

import com.cn.tianxia.controller.BaseController;

import net.sf.json.JSONObject;
  
public class CountLineListener  extends BaseController  implements HttpSessionListener{     
     /*********** 
     * 创建session时调用 
     */   
     public void sessionCreated(HttpSessionEvent event) {    
    	 ////System.out.println("创建ession......" +event.getSession().getId());   
     }   
   
     /************ 
     * 销毁session时调用 
     */   
     public void sessionDestroyed(HttpSessionEvent event) {    
         String uid ="";
         try{
        	 uid=event.getSession().getAttribute("uid").toString(); 
         }catch(Exception e){
        	 
         }
         if(loginmaps.containsKey(uid)){
        	 String sessionid=event.getSession().getId();
        	 Map<String, String> loginmap=loginmaps.get(uid);  
        	 if(loginmap.containsKey("sessionid")){
            	 if(sessionid.equals(loginmap.get("sessionid").toString())){
            		 loginmaps.remove(uid);      
                 }  
            }
        	   	 
         }
    }   
   
}  