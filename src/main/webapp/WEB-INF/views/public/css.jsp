<%@ page pageEncoding="UTF-8"%>
<link href="${pageContext.request.contextPath}/media/css/public/bootstrap.min.css" rel="stylesheet" />
<link href="${pageContext.request.contextPath}/media/css/public/sb-admin.css" rel="stylesheet"/>
<link href="${pageContext.request.contextPath}/media/css/public/morris.css" rel="stylesheet"/>
<link href="${pageContext.request.contextPath}/media/css/public/font-awesome.min.css" rel="stylesheet"/>
<link href="${pageContext.request.contextPath}/media/css/public/awesome-bootstrap-checkbox.css" rel="stylesheet"/>
<link href="${pageContext.request.contextPath}/media/css/public/magicsuggest.css" rel="stylesheet"/>
<link href="${pageContext.request.contextPath}/media/css/public/codemirror.css" rel="stylesheet"/>
<link href="${pageContext.request.contextPath}/media/css/public/show-hint.css" rel="stylesheet"/>
<link href="${pageContext.request.contextPath}/media/css/public/jquery.terminal.min.css" rel="stylesheet"/>
<link href="${pageContext.request.contextPath}/media/css/public/bootstrap-treeview.min.css" rel="stylesheet"/>
<link rel="shortcut icon" href="${pageContext.request.contextPath}/media/img/favicon.ico" />
<%
	String[] loader = request.getParameterValues("css");
	if (loader == null) {
		return;
	}
	for (String s : loader) {
%>
<link href="${pageContext.request.contextPath}/media/css/<%=s%>" rel="stylesheet"/>
<%
	}
%>