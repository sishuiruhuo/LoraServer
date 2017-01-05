package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import com.mysql.jdbc.PreparedStatement;

import jsonform.InfoForm;
import jsonform.InfoPktErrorForm;

public class DataBasePktError implements DataBase {
	//create table pkt_error(error int);
	private static String SAVE_SQL = "INSERT INTO pkt_error(error)VALUES(?)";
	
	@Override
	public void SaveData(InfoForm info) {
		// TODO Auto-generated method stub
		System.out.println("database pkterror");
		InfoPktErrorForm pkterror = (InfoPktErrorForm)info;
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		try {
			connection = DriverManager.getConnection(dbUrl,dbUser,dbPwd);
			stmt = (PreparedStatement) connection.prepareStatement(SAVE_SQL);
			stmt.setObject(1, pkterror.getError());
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
