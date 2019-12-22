<%@ page import = "ex.db.MemberManage"%>
<%@ page import = "java.util.ArrayList" %>
<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
    
<%
	MemberManage memberManage = MemberManage.getInstance();
	
	request.setCharacterEncoding("UTF-8");
	String sql = request.getParameter("sql");
	String returns = memberManage.returnValue(sql);
	
	System.out.println(returns);
	out.println(returns);
%>