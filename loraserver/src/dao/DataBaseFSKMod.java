package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import com.mysql.jdbc.PreparedStatement;

import jsonform.InfoFSKModEndForm;
import jsonform.InfoForm;

public class DataBaseFSKMod implements DataBase {
//create table fsk_mod( time varchar(50), tmst double, freq int, chan int, rfch int, stat int, modu varchar(20), datr_fsk varchar(20), rssi int, size int);

	private static String SAVE_SQL = "INSERT INTO fsk_mod(time, tmst, freq, chan, rfch, stat, modu, datr_fsk, rssi, size)VALUES(?,?,?,?,?,?,?,?,?,?)";
	
	@Override
	public void SaveData(InfoForm info) {
		// TODO Auto-generated method stub
		System.out.println("database fsk");
		InfoFSKModEndForm fsk = (InfoFSKModEndForm)info;
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		try {
			connection = DriverManager.getConnection(dbUrl,dbUser,dbPwd);
			stmt = (PreparedStatement) connection.prepareStatement(SAVE_SQL);
			stmt.setObject(1, fsk.getTime());
			stmt.setObject(2, fsk.getTmst());
			stmt.setObject(3, fsk.getFreq());
			stmt.setObject(4, fsk.getChan());
			stmt.setObject(5, fsk.getRfch());
			stmt.setObject(6, fsk.getStat());
			stmt.setObject(7, fsk.getModu());
			stmt.setObject(8, fsk.getDatr_fsk());
			stmt.setObject(9, fsk.getRssi());
			stmt.setObject(10, fsk.getSize());
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
