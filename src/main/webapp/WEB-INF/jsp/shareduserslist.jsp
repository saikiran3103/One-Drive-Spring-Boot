<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<title>shared users</title>
</head>
<body>




<input type="radio" name="colors" value="red" id="myRadio">


<table>
       <c:forEach items="${sharedusers}" var="user">
       shared user="${user.key}"
        </c:forEach>
    </table>


<form action="" method="POST">
  <input type="hidden" name="q" value="a">
</form>

<button onclick="submitUser()">Confirm the User shared</button>



<script>
function submitUser() {
    var x = document.getElementById("myRadio").value;
    document.getElementById("demo").innerHTML = x;
}

<script>
function myFunction(message,error) {


   if(message.message){
	   return ${message.message};
 }
   else if(error){
	   var textmessage = ${error.code}+" " +${error.message};
	   return textmessage;
   }
   else{
	   return "check the logs for the error returned ";
   }
}
var messageobtained =${sharedusers};
var errorObtained= ${error};
document.getElementById("mymessage").innerHTML = myFunction(messageobtained, errorObtained); 

function logMapElements(value, key, sharedusers) {
    console.log(`m[${key}] = ${value}`);
}





</script>


</script>

</body>
</html>