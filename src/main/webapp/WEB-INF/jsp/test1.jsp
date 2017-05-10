<HTML>
<HEAD>

<head>
   <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
    
   
   
</head>
<TITLE>Your Title Here</TITLE>
</HEAD>
<BODY  BGCOLOR="white">

<article>
  <header>
   <div  style="text-align:center">  <h1 id= "headingmessage" style="
    font-size: 34pt;
    font-family: serif;
    font-style: italic;
    font-feature-settings: initial;
    color: rgba(169, 66, 66, 0.98);">Get the files or folders from One Micorsoft One Drive Cloud from the shared link</h1>
    
    </div>
  </header>
  
</article>
<div style="text-align:center"> 
<form name="myForm" action="path1" method="POST">
<p style="
 
 
    font-size: 22pt;
    font-style: italic;
    font-feature-settings: initial;
    color: #f3185d;
    width: 1545px;
    margin-top: 85px;
    ">Enter the shared  One drive URL :</p>
    
     <input id="text1" type="text" >
<!-- <input id="param1" type="hidden" name="param1" value="Test"> -->
<input id="param2" type="hidden" name="param2" value="Test2">
<input type="button" id="downloadbutton" style="font-size:13pt;color:white;background-color:green;border: 13px solid #336600;padding:3px;" Value="Download And Convert" onclick="submitform();">
</form>
</div>
	<div id="loader"></div>
<div id="searchingimageDiv" style="display:none">
<center><img id="searchingimage1" src="http://www.dokolica.rs/wp-content/uploads/2016/02/Cento-Lodigiani-animated-GIFs-12.gif" alt="downloading and extracting text" /> </center></div>

<script type= text/javascript>

function submitform(){
	
	$("#downloadbutton").hide();
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

</script>

</BODY>
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








</HTML>