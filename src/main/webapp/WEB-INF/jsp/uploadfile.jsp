<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>JavaScript file upload</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">

</head>
<body
 style="
    text-align: center;
    font-style: italic;
    background-color: azure;
    color: red;
    font-size: larger;
     padding-top: 119px;
     
    
  webkit-background-size: cover;
  moz-background-size: cover;
  o-background-size: cover;
  background-size: cover;">
 
        <form method="POST" action="uploadfiles" enctype="multipart/form-data" >
            File:
            <input type="file" name="file" id="file" /> <br/>
            Destination:
            <input type="text"  name="path"  id="path"/>
            <br>
            <input type="submit" value="Upload" name="upload" id="upload" onclick="execute(document.getElementById('path').value);" />
        </form>
        
        
        
        
        <script type= text/javascript>

function submitform(){
	
		    
	
	
	document.getElementById("path").value = document.getElementById("text1").value;
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
		
		
		 </script>
    </body>
    
    
    
    
</html>