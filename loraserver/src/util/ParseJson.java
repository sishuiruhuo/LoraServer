package util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import base64.base64__;
import dao.DataBaseAction;
import jsonform.InfoFSKModEndForm;
import jsonform.InfoForm;
import jsonform.InfoGateWayStatForm;
import jsonform.InfoLoraModEndForm;

public class ParseJson {
	private String json = "{\"rxpk\":[{\"time\":\"2013-03-31T16:21:17.528002Z\",\"tmst\":3512348611,\"chan\":2,\"rfch\":0,\"freq\":866.349812,\"stat\":1,\"modu\":\"LORA\",\"datr\":\"SF7BW125\",\"codr\":\"4/6\",\"rssi\":-35,\"lsnr\":5.1,\"size\":32,\"data\":\"-DS4CGaDCdG+48eJNM3Vai-zDpsR71Pn9CPA9uCON84\"},{\"time\":\"2013-03-31T16:21:17.530974Z\",\"tmst\":3512348514,\"chan\":9,\"rfch\":1,\"freq\":869.1,\"stat\":1,\"modu\":\"FSK\",\"datr\":50000,\"rssi\":-75,\"size\":16,\"data\":\"VEVTVF9QQUNLRVRfMTIzNA==\"},{\"time\":\"2013-03-31T16:21:17.532038Z\",\"tmst\":3316387610,\"chan\":0,\"rfch\":0,\"freq\":863.00981,\"stat\":1,\"modu\":\"LORA\",\"datr\":\"SF10BW125\",\"codr\":\"4/7\",\"rssi\":-38,\"lsnr\":5.5,\"size\":32,\"data\":\"ysgRl452xNLep9S1NTIg2lomKDxUgn3DJ7DE+b00Ass\"}],\"stat\":{\"time\":\"2014-01-12 08:59:28 GMT\",\"lati\":46.24000,\"long\":3.25230,\"alti\":145,\"rxnb\":2,\"rxok\":2,\"rxfw\":2,\"ackr\":100.0,\"dwnb\":2,\"txnb\":2}}";
	
	//private HashMap<String,Info> Map = new HashMap();
	public String getJson() {
		return json;
	}

	/**
	 * 这里实现 data 部分的 base64 解码
	 * @param jsonstr
	 * @return
	 */
	public static HashMap<String, InfoForm> parseOfJson(String jsonstr) {
		HashMap<String, InfoForm> InfoMap = new HashMap();
		try {
			JSONObject json = new JSONObject(jsonstr);
	/*		@SuppressWarnings("unchecked")
			Iterator<Object> iterator = json.keys();
			
				while(iterator.hasNext())
				{
				String key = (String) iterator.next();
				@SuppressWarnings("unused")
				String value = (String)json.getString(key);
				System.out.println("key=*******************************************"+key);
				}*/
			
			InfoLoraModEndForm loraendpkt = new InfoLoraModEndForm();
			InfoFSKModEndForm fskendpkt = new InfoFSKModEndForm();
			InfoGateWayStatForm gwstat = new InfoGateWayStatForm();
			
			if(!json.isNull("rxpk")){
				JSONArray rxpk_arry = json.getJSONArray("rxpk");
				//rxpt
				for(int i = 0; i < rxpk_arry.length(); i ++){
					if(rxpk_arry.getJSONObject(i).getString("modu").equals("LORA")){
					//	loraendpkt.time = rxpk_arry.getJSONObject(i).getString("time");
						loraendpkt.tmst = rxpk_arry.getJSONObject(i).getDouble("tmst");
						loraendpkt.chan = rxpk_arry.getJSONObject(i).getInt("chan");
						loraendpkt.rfch = rxpk_arry.getJSONObject(i).getInt("rfch");
						loraendpkt.freq = rxpk_arry.getJSONObject(i).getInt("freq");
						loraendpkt.stat = rxpk_arry.getJSONObject(i).getInt("stat");
						loraendpkt.modu = rxpk_arry.getJSONObject(i).getString("modu");
						loraendpkt.datr_lora = rxpk_arry.getJSONObject(i).getString("datr");
						loraendpkt.codr = rxpk_arry.getJSONObject(i).getString("codr");
						loraendpkt.rssi = rxpk_arry.getJSONObject(i).getInt("rssi");
						loraendpkt.lsnr = rxpk_arry.getJSONObject(i).getInt("lsnr");
						loraendpkt.size = rxpk_arry.getJSONObject(i).getInt("size");
						loraendpkt.data = base64__.decode(rxpk_arry.getJSONObject(i).getString("data"));
						InfoMap.put("LoRaMod" + i, loraendpkt);
						System.out.println("--------------" + i + "---------------");
						System.out.println("time:" + loraendpkt.time);
						System.out.println("tmst:" + loraendpkt.tmst);
						System.out.println("chan:" + loraendpkt.chan);
						System.out.println("rfch:" + loraendpkt.rfch);
						System.out.println("freq:" + loraendpkt.freq);
						System.out.println("stat:" + loraendpkt.stat);
						System.out.println("modu:" + loraendpkt.modu);
						System.out.println("datr_lora:" + loraendpkt.datr_lora);
						System.out.println("codr:" + loraendpkt.codr);
						System.out.println("rssi:" + loraendpkt.rssi);
						System.out.println("lsnr:" + loraendpkt.lsnr);
						System.out.println("size:" + loraendpkt.size);
						base64__.myprintHex((loraendpkt.data));
					} else {
					//	fskendpkt.time = rxpk_arry.getJSONObject(i).getString("time");
						fskendpkt.tmst = rxpk_arry.getJSONObject(i).getDouble("tmst");
						fskendpkt.chan = rxpk_arry.getJSONObject(i).getInt("chan");
						fskendpkt.rfch = rxpk_arry.getJSONObject(i).getInt("rfch");
						fskendpkt.freq = rxpk_arry.getJSONObject(i).getInt("freq");
						fskendpkt.stat = rxpk_arry.getJSONObject(i).getInt("stat");
						fskendpkt.modu = rxpk_arry.getJSONObject(i).getString("modu");
						fskendpkt.datr_fsk = rxpk_arry.getJSONObject(i).getInt("datr");
						fskendpkt.rssi = rxpk_arry.getJSONObject(i).getInt("rssi");
						fskendpkt.size = rxpk_arry.getJSONObject(i).getInt("size");
						fskendpkt.data = base64__.decode(rxpk_arry.getJSONObject(i).getString("data"));
						InfoMap.put("FSKMod" + i, fskendpkt);
						System.out.println("--------------" + i + "---------------");
						System.out.println("time:" + fskendpkt.time);
						System.out.println("tmst:" + fskendpkt.tmst);
						System.out.println("chan:" + fskendpkt.chan);
						System.out.println("rfch:" + fskendpkt.rfch);
						System.out.println("freq:" + fskendpkt.freq);
						System.out.println("stat:" + fskendpkt.stat);
						System.out.println("modu:" + fskendpkt.modu);
						System.out.println("datr_lora:" + fskendpkt.datr_fsk);
						System.out.println("rssi:" + fskendpkt.rssi);
						System.out.println("size:" + fskendpkt.size);
						base64__.myprintHex((fskendpkt.data));
					}
				}
			} if(!json.isNull("stat") ){					
			//stat_________________________________________________________
				System.out.println("=====================stat==========================");
				JSONObject stat = json.getJSONObject("stat");
				gwstat.time = stat.getString("time");
				gwstat.lati = stat.getInt("lati");
				gwstat.longe = stat.getInt("long");
				gwstat.alti = stat.getInt("alti");
				gwstat.rxnb = stat.getInt("rxnb");
				gwstat.rxok = stat.getInt("rxok");
				gwstat.rxfw = stat.getInt("rxfw");
				gwstat.ackr = stat.getInt("ackr");
				gwstat.dwnb = stat.getInt("dwnb");
				gwstat.txnb = stat.getInt("txnb");
				InfoMap.put("GatewayStat" + "1", gwstat);
				System.out.println("time:" + gwstat.time);
				System.out.println("lati:" + gwstat.lati);
				System.out.println("longe:" + gwstat.longe);
				System.out.println("alti:" + gwstat.alti);
				System.out.println("rxnb:" + gwstat.rxnb);
				System.out.println("rxok:" + gwstat.rxok);
				System.out.println("rxfw:" + gwstat.rxfw);
				System.out.println("ackr:" + gwstat.ackr);
				System.out.println("dwnb:" + gwstat.dwnb);
				System.out.println("txnb:" + gwstat.txnb);
				if(!stat.isNull("error") )	{
					gwstat.ackr = stat.getInt("ackr");
					gwstat.alti = stat.getInt("alti");
					gwstat.dwnb = stat.getInt("dwnb");
					gwstat.lati = stat.getInt("lati");
					gwstat.longe = stat.getInt("long");
					gwstat.rxfw = stat.getInt("rxfw");
					gwstat.rxnb = stat.getInt("rxnb");
					gwstat.rxok = stat.getInt("rxok");
					gwstat.time = stat.getString("time");
					gwstat.txnb = stat.getInt("txnb");
					InfoMap.put("PktError"+ "1", gwstat);
				}
			}
			return InfoMap;
		}catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null; 
	}
	
	
	// 用于本地测试 json 解析及存到数据库
	public static void main(String[] args){
		ParseJson pjs = new ParseJson();
		ParseJson.parseOfJson(pjs.getJson());
		HashMap<String, InfoForm> InfoMap = ParseJson.parseOfJson(pjs.json);
		InfoForm info; 
		Set set = InfoMap.keySet(); 
	    for(Iterator itr=set.iterator();itr.hasNext();){ 
	    	String value =(String) itr.next(); 
	    	info = InfoMap.get(value); 
//	      	info.saveData();
	    	if(info instanceof InfoFSKModEndForm){
	    	  System.out.println("fsk");
	    	}
	    	if(info instanceof InfoLoraModEndForm){
		    	  System.out.println("lora");
		    }
	     } 
	    DataBaseAction.SaveData(InfoMap);
	}

}