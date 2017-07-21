<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<head>

	

	
	<c:url value="/css/upload.css" var="jstlCss" />
	<link href="${jstlCss}" rel="stylesheet" />

</head>
<html>
<body>
	<h2>Status of file Uploads</h2>

	<c:if test="${not empty finalListForUploadStatus}">
<div id="imglist">
		<ul>
			<c:forEach var="listValue" items="${finalListForUploadStatus}">
				<li>${listValue}</li>
			</c:forEach>
		</ul>
</div>
	</c:if>
	<!--
	<img src="<c:url value='/images/arrow.png'/>" />
	-->
</body>
</html>