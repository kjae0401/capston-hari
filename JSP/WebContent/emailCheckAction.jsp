
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="ex.db.SHA256" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="ex.db.MemberManage" %>
<%
	request.setCharacterEncoding("UTF-8");
	String code = null;
	
	String userEmail = request.getParameter("email");
	String compare = new SHA256().getSHA256(userEmail);
	
	if(request.getParameter("code") != null) {
		code = request.getParameter("code");
	}
	if(compare.equals(code)) {
		MemberManage memberManage = MemberManage.getInstance();
		
		request.setCharacterEncoding("UTF-8");
		String sql = "update USERTBL set AUTH='TRUE' where email='" + userEmail + "'";
		String returns = memberManage.connectionDB(sql);
		
		System.out.println(returns);
		out.println("이메일 인증에 성공하였습니다.\n이 페이지는 3초 뒤 자동 종료됩니다.");
	} else {
		System.out.println("실패");
		out.print("이메일 인증에 실패하였습니다.\n이 페이지는 3초 뒤 자동 종료됩니다.");
	}
%>
<script>
	setTimeout("window.close()",3000);
</script>