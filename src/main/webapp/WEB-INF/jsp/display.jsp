<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<title>downloaded</title>
</head>
<body
	style="text-align: center; font-style: italic; background-color: azure; background: url(../images/welcomeGif.gif) no-repeat center center fixed; webkit-background-size: cover; moz-background-size: cover; o-background-size: cover; background-size: cover;">


	<div style="text-align: center">
		<h1 id="mymessage"
			style="font-size: 26pt; font-family: serif; font-style: italic; color: rgba(169, 66, 66, 0.98);">${message.message}</h1>
	</div>


	<script>
function myFunction() {

	

   if(typeof ${message} != undefined){
	   return ${message.message};
 }
  
   else{
	   return "check the logs for the error returned ";
   }
}
 


document.getElementById("mymessage").innerHTML = myFunction(); 
</script>
</body>
</html>