<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<title>downloaded </title>
</head>
<body>

    
   <div style="text-align:center">  <h1 id= "mymessage" style="
    font-size: 26pt;
    font-family: serif;
    font-style: italic;
    font-feature-settings: initial;
    color: rgba(169, 66, 66, 0.98);">Your files are down-loaded in to ${message.message}</h1>
     </div>


<script>
function myFunction(message,error) {


   if(message.message){
	   return "Your files are down-loaded in to"+ ${message.message};
 }
   else if(error){
	   var textmessage = ${error.code}+" " +${error.message};
	   return textmessage;
   }
   else{
	   return "Please check the link and the folder name and retry ";
   }
}
var messageobtained =${message};
var errorObtained= ${error};
document.getElementById("mymessage").innerHTML = myFunction(messageobtained, errorObtained); 
</script>
</body>
</html>