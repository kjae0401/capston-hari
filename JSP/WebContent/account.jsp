<%@ page import = "ex.db.MemberManage"%>
<%@ page import = "java.util.ArrayList" %>
<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
    
<%
	MemberManage memberManage = MemberManage.getInstance();
	
	request.setCharacterEncoding("UTF-8");
	String sql = request.getParameter("sql");
	sql = request.getParameter("sql");
	ArrayList<String> returns = memberManage.account(sql);
	
	for(int i=0; i<returns.size(); i++) {
		out.println(returns.get(i));
	}
%>