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

	<article>
		<header>
			<div style="text-align: center">
				<h1>Get the files or folders from One Micorsoft One Drive Cloud
					from the shared link</h1>

			</div>
		</header>



	</article>
	<div style="text-align: center">
		<form name="myForm" action="path" method="POST"
			style="color: #0012ff; font-style: italic; font-size: x-large;">
			Enter the shared One drive URL for <b>personal</b> items :<input
				id="text1" type="text"> <input id="param1" type="hidden"
				name="param1" value="Test"> <input id="param2" type="hidden"
				name="param2" value="Test2"> <input type="button"
				id="downloadbutton"
				style="font-size: 13pt; color: white; background-color: green; border: 13px solid #336600; padding: 3px;"
				value="Download And Convert" onclick="submitform();">

		</form>
	</div>
	<br />
	<div style="text-align: center">
		<form name="myForm2" action="path" method="POST"
			style="color: #0012ff; font-style: italic; font-size: x-large;">
			Enter the shared One drive URL for <b>shared</b> items :<input
				id="text1" type="text"> <input id="param1" type="hidden"
				name="param1" value="Test"> <input id="param2" type="hidden"
				name="param2" value="Test2"> <input type="button"
				id="downloadbutton"
				style="font-size: 13pt; color: white; background-color: green; border: 13px solid #336600; padding: 3px;"
				value="Download And Convert" onclick="submitform();">

		</form>
	</div>




	<div style="text-align: center">
		<h1
			style="font-size: 26pt; font-family: serif; font-style: italic; font-feature-settings: initial; color: rgba(169, 66, 66, 0.98);">${message.message}</h1>
	</div>
</BODY>

<script type= text/javascript>
function submitform(){
	
	document.getElementById("param2").value = document.getElementById("text1").value;
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
