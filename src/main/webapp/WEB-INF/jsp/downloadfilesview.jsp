
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<HTML>


<HEAD>
<head>

<c:url value="../css/upload.css" var="jstlCss" />
	<link href="${jstlCss}" rel="stylesheet" />
<script type="text/javascript"
	src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>



</head>
<TITLE>DownloadFiles</TITLE>


</HEAD>
<BODY
	style="text-align: center; font-style: italic; background-color: azure; background: url(../images/robo.jpg) no-repeat center center fixed; webkit-background-size: cover; moz-background-size: cover; o-background-size: cover; background-size: cover;">

	<article>
		<header>
			<div style="text-align: center">
				<h1 id="headingmessage"
					style="font-size: 26pt; font-family: serif; font-style: italic; font-feature-settings: initial; padding-top: 43px; color: #3c3634;">Get
					the files or folders from One Micorsoft One Drive Cloud from the
					shared link</h1>

			</div>
		</header>

	</article>
	<div>
		<p
			style="text-align: center font-size: x-large; font-style: italic; font-feature-settings: initial; color: rgba(24, 69, 243, 0.82); width: 1750px; margin-top: 60px; font-size: 18pt; font-weight: bold;">
			Personal Files and Folders</p>
	</div>
	<div style="text-align: center">
		<form name="myForm" action="personalfiles" method="POST">


			<p
				style="font-size: 18pt; font-style: italic; font-feature-settings: initial; color: #3c3634; width: 1545px; margin-top: 60px;">Enter
				the One drive Link for Your Personal Items :</p>

			<input id="text1" type="text">
			<!-- <input id="param1" type="hidden" name="param1" value="Test"> -->
			<input id="param2" type="hidden" name="param2" value="Test2">
			<input type="button" id="downloadbutton"
				style="font-size: 13pt; color: white; background-color: green; border: 13px solid #336600; padding: 3px; box-shadow: 0 0 20px black; text-shadow: 0 0 13px black; cursor: pointer;"
				Value="Download" onclick="submitform();">
		</form>
		</div>
		
<div id="cover" style="display: none">


  </div>
	
</div>
	<div style="text-align: center">

		<p
			style="text-align: center font-size: x-large; font-style: italic; font-feature-settings: initial; color: rgba(24, 69, 243, 0.82); width: 1750px; margin-top: 60px; font-size: 18pt; font-weight: bold;">
			Shared Items</p>
		<form name="myForm2" action="shareditems" method="POST">
			<p
				style="font-size: 18pt; font-style: italic; font-feature-settings: initial; color: #3c3634; width: 1545px; margin-top: 60px;">Enter
				the One drive Link for Shared Items :</p>

			<input id="textshared" type="text">
			<!-- <input id="param1" type="hidden" name="param1" value="Test"> -->
			<input id="param3" type="hidden" name="param3" value="Test3">
			<input type="button" id="downloadbutton2"
				style="font-size: 13pt; color: white; background-color: green; border: 13px solid #336600; box-shadow: 0 0 20px black; text-shadow: 0 0 13px black; cursor: pointer; padding: 3px;"
				Value="Download" onclick="submitform2();">
		</form>


	</div>
	<div id="loader"></div>
	<div id="searchingimageDiv" style="display: none">
		<center>
			<img id="searchingimage1" src="../Images/Loading_icon.gif"
				alt="downloading and extracting text" />
		</center>
	</div>

	<script type=text/javascript>
		function submitform() {
			$("#downloadbutton2").hide();
			$("#downloadbutton").hide();
			$("#textshared").hide();
			$("p").hide();
			$("#text1").hide();
			$("#cover").show();
			
			
			document.getElementById('headingmessage').innerHTML = 'Downloading your files , please wait ... !';

			
	

			document.getElementById("param2").value = document
					.getElementById("text1").value;
			/*get the token value from header*/
			window.location.parseHash = function() {
				var hash = (this.hash || '').replace(/^#/, '').split('&'), parsed = {};

				for (var i = 0, el; i < hash.length; i++) {
					el = hash[i].split('=')
					parsed[el[0]] = el[1];
				}
				return parsed;
			};

			document.myForm.submit();

		}

		function submitform2() {

			$("#downloadbutton").hide();
			$("#downloadbutton2").hide();
			$("p").hide();
			$("#textshared").hide();
			$("#text1").hide();
			$("#cover").show();
			document.getElementById('headingmessage').innerHTML = 'Downloading your files , please wait ...  !';

			

			document.getElementById("param3").value = document
					.getElementById("textshared").value;
			/*get the token value from header*/
			window.location.parseHash = function() {
				var hash = (this.hash || '').replace(/^#/, '').split('&'), parsed = {};

				for (var i = 0, el; i < hash.length; i++) {
					el = hash[i].split('=')
					parsed[el[0]] = el[1];
				}
				return parsed;
			};

			document.myForm2.submit();

		}
	</script>

</BODY>

</HTML>