package ex.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MemberManage {
private static MemberManage instance = new MemberManage();
	
	public static MemberManage getInstance() {
		return instance;
	}
	public MemberManage() { }
	
	String jdbcUrl = "jdbc:oracle:thin:@113.198.237.95:1521:xe";
	String userId = "dbdb";
	String userPw = "dbdb";
	
	Connection conn = null;
	PreparedStatement pstmt = null;
	PreparedStatement pstmt2 = null;
	ResultSet rs = null;
	
	String returns = "a";
	
	public String connectionDB(String sql) {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(jdbcUrl, userId, userPw);
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			System.out.println(sql.toString());
			if (rs.next()) {
				returns = "성공";
			} else {
				returns = "실패";
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt2 != null)try {pstmt2.close();} catch (SQLException ex) {}
			if (pstmt != null)try {pstmt.close();} catch (SQLException ex) {}
			if (conn != null)try {conn.close(); }catch (SQLException ex) {}
		}
		return returns;
	}
}