<HTML>
<HEAD>

<head>
   <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
    
  <style>
.loader {
  border: 16px solid #f3f3f3;
  border-radius: 50%;
  border-top: 16px solid #3498db;
  width: 120px;
  height: 120px;
  -webkit-animation: spin 2s linear infinite;
  animation: spin 2s linear infinite;
}

@-webkit-keyframes spin {
  0% { -webkit-transform: rotate(0deg); }
  100% { -webkit-transform: rotate(360deg); }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
   
</head>
<TITLE>Your Title Here</TITLE>


</HEAD>
<BODY  style="
    text-align: center;
    font-style: italic;
    background-color: azure;
    background: url(../images/blackBackground.jpg) no-repeat center center fixed; 
  webkit-background-size: cover;
  moz-background-size: cover;
  o-background-size: cover;
  
  background-size: cover;">

<article>
  <header>
   <div  style="text-align:center">  <h1 id= "headingmessage" style="
    font-size: 26pt;
    font-family: serif;
    font-style: italic;
    font-feature-settings: initial;
    padding-top: 43px;
    color: #FF5722;">Get the files or folders from One Micorsoft One Drive Cloud from the shared link</h1>
    
    </div>
  </header>
  
</article>
<div>
<p style="text-align:center
    font-size: x-large;
    font-style: italic;
    font-feature-settings: initial;
    color: rgba(24, 69, 243, 0.82);
    width: 1750px;
    margin-top: 60px;
     font-size: 18pt;
    font-weight: bold;"

> Personal Items </p></div>
<div style="text-align:center"> 
<form name="myForm" action="path1" method="POST">


<p style="
 
 
    font-size: 18pt;
    font-style: italic;
    font-feature-settings: initial;
    color: #ffeb3b;
    width: 1545px;
    margin-top: 60px;
    ">Enter the One drive Link for Your Personal Items :</p>
    
     <input id="text1" type="text" >
<!-- <input id="param1" type="hidden" name="param1" value="Test"> -->
<input id="param2" type="hidden" name="param2" value="Test2">
<input type="button" id="downloadbutton" 
style="font-size:13pt;color:white;background-color:green;border: 13px solid #336600;padding:3px;
box-shadow: 0 0 20px black;
 text-shadow: 0 0 13px black;
 
  cursor: pointer;

"
 Value="Download And Convert" onclick="submitform();">
</form>

</div>

<div style="text-align:center"> 

<p style="text-align:center
     font-size: x-large;
    font-style: italic;
    font-feature-settings: initial;
    color: rgba(24, 69, 243, 0.82);
    width: 1750px;
    margin-top: 60px;
     font-size: 18pt;
    font-weight: bold;"

> Shared Items </p>
<form name="myForm2" action="shareditems" method="POST">
<p style="
 
 
    font-size: 18pt;
    font-style: italic;
    font-feature-settings: initial;
    color: #ffeb3b;
    width: 1545px;
    margin-top: 60px;
    ">Enter the  One drive Link for Shared Items :</p>
    
     <input id="textshared" type="text" >
<!-- <input id="param1" type="hidden" name="param1" value="Test"> -->
<input id="param3" type="hidden" name="param3" value="Test3">
<input type="button" id="downloadbutton2" style="font-size:13pt;color:white;background-color:green;border: 13px solid #336600;
box-shadow: 0 0 20px black;
 text-shadow: 0 0 13px black;
 
  cursor: pointer;

padding:3px;" Value="Download And Convert" onclick="submitform2();">
</form>
 

</div>
	<div id="loader"></div>
<div id="searchingimageDiv" style="display:none">
<center><img id="searchingimage1" src="/Images/Loading_icon.gif" alt="downloading and extracting text" /> </center></div>

<script type= text/javascript>

function submitform(){
	$("#downloadbutton2").hide();
	$("#downloadbutton").hide();
	$("#textshared").hide();
	$("p").hide();
	$("#text1").hide();
	document.getElementById('headingmessage').innerHTML ='Your files are being downloaded and processed !';
	    
	var oimageDiv=document.getElementById('searchingimageDiv') ;
	//set display to inline if currently none, otherwise to none 
	oimageDiv.style.display=(oimageDiv.style.display=='none')?'inline':'none' 
			
		var oimageDiv=document.getElementById('loader') ;
	//set display to inline if currently none, otherwise to none 
	oimageDiv.style.display=(oimageDiv.style.display=='none')?'inline':'none' 
	
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
		
	
		 

		/* var obj= location.parseHash();
		    obj.hash;  //fdg 
		    document.getElementById("param1").value = obj.access_token;   //value2 */
		    document.myForm.submit();
		    $(window).load(function() {
		        $('#loader').show();
		     });
		  

}


function submitform2(){
	
	$("#downloadbutton").hide();
	$("#downloadbutton2").hide();
	$("p").hide();
	$("#textshared").hide();
	$("#text1").hide();
	document.getElementById('headingmessage').innerHTML ='Your files are being downloaded and processed !';
	    
	var oimageDiv=document.getElementById('searchingimageDiv') ;
	//set display to inline if currently none, otherwise to none 
	oimageDiv.style.display=(oimageDiv.style.display=='none')?'inline':'none' 
			
		var oimageDiv=document.getElementById('loader') ;
	//set display to inline if currently none, otherwise to none 
	oimageDiv.style.display=(oimageDiv.style.display=='none')?'inline':'none' 
	
	document.getElementById("param3").value = document.getElementById("textshared").value;
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
		
	
		 

		/* var obj= location.parseHash();
		    obj.hash;  //fdg 
		    document.getElementById("param1").value = obj.access_token;   //value2 */
		    document.myForm2.submit();
		    $(window).load(function() {
		        $('#loader').show();
		     });
		  

}



</script>

</BODY>









</HTML>