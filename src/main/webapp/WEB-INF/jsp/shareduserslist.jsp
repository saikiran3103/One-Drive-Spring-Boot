<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="true" %>
<title>shared users</title>
</head>
<body  style="
    text-align: center;
    font-style: italic;
    background-color: azure;
    color: red;
    font-size: larger;
     padding-top: 119px;">
     
    <p>
This is the URL you provided, check the user name in the url and confirm the user<br>
</p>
"<c:out value="${sessionScope.sharedItemUrl}"/>"

<form name="downloadSharedfilesForm" action="downloadsharedfiles" method="POST">

<table>

 <c:forEach items="${sharedusers}" var="user">
 <br><br>
<input type="radio" name="myRadio" value="${user.value}" id="myRadio"  >



      
       shared user-->"${user.key}"
        </c:forEach>
    </table>



 <input id="driveId" type="hidden" name="driveId" value="Test3">
  
 <input type="button" id="downloadbutton2" Value="Confirm the User shared"  onclick="submitUser();"
 
	 style="
 
    
    
 margin-top: 22px;
 color: rgb(255, 193, 7);
 background: rgba(76, 175, 80, 0.97);
 box-shadow: 0 0 20px black;
 text-shadow: 0 0 13px black;
 font-size: 16px;
  cursor: pointer;
"

 >
</form>
<br>






<script>
function submitUser() {
   
	document.getElementById("driveId").value =document.querySelector('input[name="myRadio"]:checked').value;
	
    document.downloadSharedfilesForm.submit();
    
}




</script>

</body>
</html>