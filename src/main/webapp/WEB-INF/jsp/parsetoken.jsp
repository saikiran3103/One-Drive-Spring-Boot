<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html lang="en">
<head>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<link rel="stylesheet" type="text/css"
	href="webjars/bootstrap/3.3.7/css/bootstrap.min.css" />

<!-- 
	<spring:url value="/css/main.css" var="springCss" />
	<link href="${springCss}" rel="stylesheet" />
	 -->
<c:url value="/css/main.css" var="jstlCss" />
<link href="${jstlCss}" rel="stylesheet" />

</head>


<BODY onload="javascript:submitform();" BGCOLOR="FFFFFF">

	



	
	<div style="text-align: center">
		<form name="myForm" action="downloadfiles" method="POST"
			style="color: #0012ff; font-style: italic; font-size: x-large;">
		<input
				id="text1" type="text"> <input id="param1" type="hidden"
				name="param1" value="Test">>

		</form>
	</div>
	<br />
	




	
</BODY>

<script type= text/javascript>
function submitform(){
	
	
	/*get the token value from header*/
	window.location.parseHash = function(){
		   var hash = (this.hash ||'').replace(/^#/,'').split('&'),
		       parsed = {};
		   for(var i =0,el;i<hash.length; i++ ){
		        el=hash[i].split('=')
		        parsed[el[0]] = el[1];
		   }
		   return parsed;
		};
		var obj= location.parseHash();
		    obj.hash;  //fdg 
		    document.getElementById("param1").value = obj.access_token;   //value2x
		    document.myForm.submit();
}
</script>

</BODY>

</html>
