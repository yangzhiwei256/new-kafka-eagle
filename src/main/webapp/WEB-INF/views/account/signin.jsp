<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<!DOCTYPE html>
<html lang="zh">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<title>Login - KafkaEagle</title>
<meta name="description" content="">
<link rel="shortcut icon" href="${pageContext.request.contextPath}/media/img/favicon.ico" />
<style type="text/css">
input:-webkit-autofill, textarea:-webkit-autofill, select:-webkit-autofill
	{
	-webkit-text-fill-color: #ededed !important;
	-webkit-box-shadow: 0 0 0px 1000px transparent inset !important;
	background-color: transparent;
	background-image: none;
	transition: background-color 50000s ease-in-out 0s;
}

input {
	background-color: transparent;
}
</style>
<link rel="stylesheet" href="${pageContext.request.contextPath}/media/css/public/account/main.css">
</head>

<body>
	<div class="login-page">
		<div class="row">
			<div class="col-lg-4 col-lg-offset-4">
				<img class="user-avatar" src="${pageContext.request.contextPath}/media/img/ke_login.png" width="150px" alt="avatar">
				<h1>Kafka Eagle</h1>
				<form role="form" action="${pageContext.request.contextPath}/account/signin/action" method="post"
					onsubmit="return contextFormValid();">
					<div class="form-content">
						<div class="form-group">
							<input class="form-control input-underline input-lg" id="usr" name="username" placeholder="Account"
								autocomplete="off" type="text">
						</div>
						<div class="form-group">
							<input class="form-control input-underline input-lg" id="pwd"
								name="password" placeholder="Password" autocomplete="off" type="password">
                            <input type="hidden" id="ref_url" name="ref_url">
						</div>
						<div class="form-group">
							<div id="alert_mssage" style="display: none" class="alert alert-danger"></div>
							${error_msg}
						</div>
					</div>
					<button id="submit" class="btn rounded-btn">Signin</button>
				</form>
			</div>
		</div>
	</div>
</body>
<jsp:include page="../public/script.jsp">
	<jsp:param value="main/account/signin.js" name="loader" />
</jsp:include>
<script type="text/javascript">
	function contextFormValid() {
		var url = window.location.href;
		var ref_url = "";
		var username = $("#usr").val();
		var password = $("#pwd").val();
		if (url.indexOf("/signin?") > -1) {
			ref_url = url.split("/signin?")[1];
		}
		if (ref_url.length === 0) {
			ref_url = "/";
		}
		$("#ref_url").val(ref_url);
		if (username.length === 0 || password.length === 0) {
			$("#alert_mssage").text("Account or password is not null.").show();
			setTimeout(function() {
				$("#alert_mssage").hide()
			}, 3000);
			return false;
		}
		return true;
	}
</script>
</html>