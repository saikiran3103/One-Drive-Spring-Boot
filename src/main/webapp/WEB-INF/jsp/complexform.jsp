<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Form Page</title>
<script
		src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
		  <script type="text/javascript" src="../js/validator.js"></script>
		  <link rel="stylesheet" type="text/css" href="css/validator.css" />
</head>
<body>
 
  
  
  
  

<form method="post" action="mailto:Frank@cohowinery.com" class="ContactForm">
    <p>Name: <input type="text" size="65" name="Name" id="name"></p>
    <p>E-mail Address:  <input type="text" size="65" name="Email" id="email"></p>
    <p>Telephone: <input type="text" size="65" name="Telephone" id="phone"><br>
        <input type="checkbox" name="DoNotCall"> Please do not call me.</p>
    <p>What can we help you with?
        <select type="text" value="" name="Subject">
            <option>  </option>
            <option>Customer Service</option>
            <option>Question</option>
            <option>Comment</option>
            <option>Consultation</option>
            <option>Other</option>
        </select></p>
    <p>Comments:  <textarea cols="55" name="Comment">  </textarea></p>
    <p><input type="submit" value="Send" name="submit"  onclick="ValidateContactForm();"><input type="reset" value="Reset" name="reset"></p>
</form>

  
  
 
  
  
  <script>
function submitComplexForm() {
	 document.complexformsubmit.submit();
	 

}







</script>
  
      
</body>
</html>