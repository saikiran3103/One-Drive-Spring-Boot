<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<style> img.animated-gif{
  width: 180px;
  height: auto;
}</style>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="true" %>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
<title>shared users</title>
</head>
<body  style="
    text-align: center;
    font-style: italic;
    background-color: azure;
    color: red;
    font-size: larger;
     padding-top: 119px;
     
      background: url(http://brandonfryedesign.com/projects/weather-api/icons/cloudy1.gif) no-repeat center center fixed; 
  webkit-background-size: cover;
  moz-background-size: cover;
  o-background-size: cover;
  background-size: cover;">
     
     
     
   
     
  <p id = "message"  style= "
     color:#0029ff;
     font-size:  x-large;"></p>   
    <p id ="urlmessage" style= "
     color:#0029ff;
     font-size:  x-large;">
This is the URL you provided, check the user name in the url and confirm the user<br>
</p>
<p   id = "sharedItemUrl" style= "
     color:#0029ff;
     font-size:  x-large;"><c:out value="${sessionScope.sharedItemUrl}"/>"</p>
     
     <div id="searchingimageDiv" style="display:none">
<center><img  class="animated-gif" id="searchingimage1" src="/Images/loading1.gif" alt="downloading and extracting text" /> </center></div>

<form name="downloadSharedfilesForm" action="downloadsharedfiles" method="POST">



<table id ="mytable">

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
	
	var oimageDiv=document.getElementById('searchingimageDiv') ;
	//set display to inline if currently none, otherwise to none 
	oimageDiv.style.display=(oimageDiv.style.display=='none')?'inline':'none' 
	
	$("#c").hide();
	
	
	
	document.getElementById("urlmessage").innerHTML ="";
	
	document.getElementById("sharedItemUrl").innerHTML ="";
	   
	
	document.getElementById("message").innerHTML ="Please wait while we are downloading and Converting your shared files ...";
   
	document.getElementById("driveId").value =document.querySelector('input[name="myRadio"]:checked').value;
	
    document.downloadSharedfilesForm.submit();
    
}




</script>

</body>
</html>