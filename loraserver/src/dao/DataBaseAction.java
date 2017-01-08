package dao;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map.Entry;

import jsonform.InfoForm;
public class DataBaseAction {
	public static void SaveData(HashMap<String, InfoForm> infomap){
		InfoForm info; 
		DataBase db;
	    for(Entry<String, InfoForm> entry : infomap.entrySet()){ 
			String value = entry.getKey();
			info = entry.getValue();
			try {
				System.out.println("dao.DataBase" + value.substring(0, value.length() - 1));
				Class<?> cls = Class.forName("dao.DataBase" + value.substring(0, value.length() - 1));
				Constructor<?> ctr = cls.getConstructor();
				db = (DataBase)ctr.newInstance();
				db.SaveData(info);
			} catch (Exception e) {
				e.printStackTrace();
			}
	    } 
	}
	
	public static void Query(String q_str){
		
	}
	
}
