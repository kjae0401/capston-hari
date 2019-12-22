<%@ page import = "ex.db.MemberManage"%>
<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
    
<%
	MemberManage memberManage = MemberManage.getInstance();
	
	request.setCharacterEncoding("UTF-8");
	String sql = request.getParameter("sql");
	String returns = memberManage.connectionDB(sql);
	
	if (returns.equals("½ÇÆÐ")) { returns = "true"; }
	else { returns = "false"; }
	
	System.out.println(returns);
	out.println(returns);
%>