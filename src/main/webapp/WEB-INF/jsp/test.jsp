<HTML>
<HEAD>
<TITLE>Get Files</TITLE>
</HEAD>
<BODY onload="javascript:submitform();" BGCOLOR="FFFFFF">


<p>test</p>
<article>
  <header>
   <div style="text-align:center">  <h1>Get the files or folders from One Micorsoft One Drive Cloud from the shared link</h1>
    
    </div>
  </header>
  
</article>
<div style="text-align:center"> 
<form name="myForm" action="path" method="POST">
Enter the shared  One drive URL : <input id="text1" type="text" >
<input id="param1" type="hidden" name="param1" value="Test">
<input id="param2" type="hidden" name="param2" value="Test2">
<input type="button" Value="Download And Convert" onclick="submitform();">
</form>
</div>


<script type= text/javascript>
function submitform(){
	
	document.getElementById("param2").value = document.getElementById("text1").value;
	/*get the token value from header*/
	location.parseHash = function(){
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
<style>
#loading {
   width: 100%;
   height: 100%;
   top: 0;
   left: 0;
   position: fixed;
   display: block;
   opacity: 0.7;
   background-color: #fff;
   z-index: 99;
   text-align: center;
}

#loading-image {
  position: absolute;
  top: 100px;
  left: 240px;
  z-index: 100;
  
  
  
  
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

<div id="loader"></div>
<div class="loader"></div>

<script type="text/javascript">
     $(window).load(function() {
     $('#loader').hide();
  });
</script>
</HTML>