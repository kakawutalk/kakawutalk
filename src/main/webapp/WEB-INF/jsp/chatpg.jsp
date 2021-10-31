<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
    
<!DOCTYPE html>
<html>
<head>
<!-- css file -->
<%@ include file="./common/title.jsp"%>
<script type="text/javascript">
	
</script>

</head>
<body>
	<!-- Header -->
	<%@ include file="./common/header.jsp"%>
	
	<div id="main_container1">
		<div class="form_container1">
			<div class="form1">
			<%
				request.setCharacterEncoding("UTF-8");				
				int cnumPK = (int)request.getAttribute("cnumPK");
			%>
				<form method="POST" action="/chatpg">
					<div class="input-box">
					<input type="hidden" id="cnumPK" name="cnumPK" value="<%=cnumPK %>">
						<div class="inputlabel1">OOO chatting room</div>
					</div>
					
					<div class="chat_box">
					<div>sdf</div>
					<div>sdff</div>
					</div>	
									
					<div class="sendtext_box">
						<textarea id="log" name="log" placeholder="text_input"></textarea>
						<input type="submit" id="sendtext_btn" value="send" class="sendtext_btn">
					</div>
				</form>
			</div>
		</div>
	</div>
</body>
</html>