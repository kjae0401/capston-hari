package ex.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MemberManage {
private static MemberManage instance = new MemberManage();
private final char spliter = 0x11;

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
	
	public String returnValue(String sql) {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(jdbcUrl, userId, userPw);
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			System.out.println(sql.toString());
			
			if (rs.next()) {
				returns = rs.getString(1).trim();
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
	
	public ArrayList<String> friend(String sql) {
		ArrayList<String> values = new ArrayList<String>();
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(jdbcUrl, userId, userPw);
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			System.out.println(sql.toString());
			while (rs.next()) {
				values.add(rs.getString(1).trim());
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt2 != null)try {pstmt2.close();} catch (SQLException ex) {}
			if (pstmt != null)try {pstmt.close();} catch (SQLException ex) {}
			if (conn != null)try {conn.close(); }catch (SQLException ex) {}
		}
		return values;
	}
	
	public ArrayList<String> chatting(String sql) {
		ArrayList<String> values = new ArrayList<String>();
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(jdbcUrl, userId, userPw);
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			System.out.println(sql.toString());
			while (rs.next()) {
				String s = null;
				String plus = rs.getString(2).trim();
				plus = plus.replaceAll("(\r\n|\r|\n|\n\r)", String.valueOf(spliter));
				s = rs.getString(1).trim() + spliter + plus + spliter + rs.getString(3).trim() + spliter + rs.getString(4).trim();
				values.add(s);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt2 != null)try {pstmt2.close();} catch (SQLException ex) {}
			if (pstmt != null)try {pstmt.close();} catch (SQLException ex) {}
			if (conn != null)try {conn.close(); }catch (SQLException ex) {}
		}
		return values;
	}
	
	public ArrayList<String> loadchat(String sql) {
		ArrayList<String> values = new ArrayList<String>();
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(jdbcUrl, userId, userPw);
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			System.out.println(sql.toString());
			while (rs.next()) {
				String plus = rs.getString(3).trim();
				plus = plus.replaceAll("(\r\n|\r|\n|\n\r)", String.valueOf(spliter));
				String s = rs.getString(1).trim() + spliter + rs.getString(2).trim()  + spliter + plus + spliter + rs.getString(4).trim();
				values.add(s);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt2 != null)try {pstmt2.close();} catch (SQLException ex) {}
			if (pstmt != null)try {pstmt.close();} catch (SQLException ex) {}
			if (conn != null)try {conn.close(); }catch (SQLException ex) {}
		}
		return values;
	}
	
	public ArrayList<String> account(String sql) {
		ArrayList<String> values = new ArrayList<String>();
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(jdbcUrl, userId, userPw);
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			System.out.println(sql.toString());
			while (rs.next()) {
				String s = null;
				try {
					s = rs.getString(1).trim() + spliter + rs.getString(2).trim() + spliter + rs.getString(3).trim();
				} catch (NullPointerException e) {
					s = rs.getString(1).trim() + spliter + rs.getString(2).trim() + spliter + "null";
	            }
				values.add(s);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt2 != null)try {pstmt2.close();} catch (SQLException ex) {}
			if (pstmt != null)try {pstmt.close();} catch (SQLException ex) {}
			if (conn != null)try {conn.close(); }catch (SQLException ex) {}
		}
		return values;
	}
}