package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import com.mysql.jdbc.PreparedStatement;

import jsonform.InfoForm;
import jsonform.InfoLoraModEndForm;

public class DataBaseLoRaMod implements DataBase {
// create table lora_mod( time varchar(50), tmst double, freq int, chan int, rfch int, stat int, modu varchar(20), datr_lora varchar(20), codr varchar(20), rssi int, lsnr int, size int);
	
	private static String SAVE_SQL = "INSERT INTO lora_mod(time, tmst, freq, chan, rfch, stat, modu, datr_lora, codr, rssi, lsnr, size)VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
	
	@Override
	public void SaveData(InfoForm info) {
		// TODO Auto-generated method stub
		System.out.println("database lora");
		InfoLoraModEndForm lora = (InfoLoraModEndForm)info;
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		try {
			connection = DriverManager.getConnection(dbUrl,dbUser,dbPwd);
			stmt = (PreparedStatement) connection.prepareStatement(SAVE_SQL);
			stmt.setObject(1, lora.getTime());
			stmt.setObject(2, lora.getTmst());
			stmt.setObject(3, lora.getFreq());
			stmt.setObject(4, lora.getChan());
			stmt.setObject(5, lora.getRfch());
			stmt.setObject(6, lora.getStat());
			stmt.setObject(7, lora.getModu());
			stmt.setObject(8, lora.getDatr_lora());
			stmt.setObject(9, lora.getCodr());
			stmt.setObject(10, lora.getRssi());
			stmt.setObject(11, lora.getLsnr());
			stmt.setObject(12, lora.getSize());
			stmt.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(connection != null)
					connection.close();
				if(stmt != null)
					stmt.close();
				if(rst != null)
					rst.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void Query(String str) {
		// TODO Auto-generated method stub
		
	}

}
