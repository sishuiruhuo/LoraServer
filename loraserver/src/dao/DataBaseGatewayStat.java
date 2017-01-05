package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import com.mysql.jdbc.PreparedStatement;

import jsonform.InfoForm;
import jsonform.InfoGateWayStatForm;

public class DataBaseGatewayStat implements DataBase {
//create table gateway_stat( time varchar(50), lati int, longe int, alti int, rxnb int, rxok int, rxfw int, ackr int, dwnb int, txnb int);

	private static String SAVE_SQL = "INSERT INTO gateway_stat(time, lati, longe, alti, rxnb, rxok, rxfw, ackr, dwnb, txnb)VALUES(?,?,?,?,?,?,?,?,?,?)";
		
	@Override
	public void SaveData(InfoForm info) {
		// TODO Auto-generated method stub
		System.out.println("database gateway stat");
		InfoGateWayStatForm gwstat = (InfoGateWayStatForm)info;
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		try {
			connection = DriverManager.getConnection(dbUrl,dbUser,dbPwd);
			stmt = (PreparedStatement) connection.prepareStatement(SAVE_SQL);
			stmt.setObject(1, gwstat.getTime());
			stmt.setObject(2, gwstat.getLati());
			stmt.setObject(3, gwstat.getLonge());
			stmt.setObject(4, gwstat.getAlti());
			stmt.setObject(5, gwstat.getRxnb());
			stmt.setObject(6, gwstat.getRxok());
			stmt.setObject(7, gwstat.getRxfw());
			stmt.setObject(8, gwstat.getAckr());
			stmt.setObject(9, gwstat.getDwnb());
			stmt.setObject(10, gwstat.getTxnb());
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
