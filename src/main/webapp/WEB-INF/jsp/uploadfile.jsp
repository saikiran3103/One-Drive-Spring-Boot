<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>One Drive file upload</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">






</head>
<body
	style="text-align: center; font-style: italic; background-color: azure; color: red; font-size: larger; padding-top: 50px; background: url(../images/blackBackground.jpg) no-repeat center center fixed; webkit-background-size: cover; moz-background-size: cover; o-background-size: cover; background-size: cover;">


	<div class="form-style-2">
		<div class="form-style-2-heading"
			style="font-size: 22pt; font-family: monospace; font-style: inherit; text-align: center; font-feature-settings: initial; color: #f44336; width: 1550px; margin-top: 85px;">Provide
			upload details</div>

		<div
			style="font-size: 22pt; font-family: monospace; font-style: inherit; font-feature-settings: initial; color: #f44336; width: 1550px; margin-top: 85px;">
			<form method="POST" action="uploadfiles"
				enctype="multipart/form-data">
				File: <input type="file" name="file" id="file"
					style="margin-top: 20px;"> <br />

				<p style="margin-top: 20px;">
					Destination: <input type="text" name="path" id="path" />
				</p>
				<input type="submit"
					style="margin-top: 15px; color: rgb(255, 193, 7); background: rgba(76, 175, 80, 0.97); box-shadow: 0 0 20px black; text-shadow: 0 0 13px black; font-size: 16px; cursor: pointer;"
					value="Upload" name="upload" id="upload"
					onclick="execute(document.getElementById('path').value);" />
			</form>




		</div>

	</div>

	<div
		style="font-size: 22pt; font-family: monospace; font-style: inherit; font-feature-settings: initial; color: #f44336; width: 1550px; margin-top: 85px;">

		<form method="POST" action="uploadfolder"
			enctype="multipart/form-data">
			Upload A local folder: <input type="submit"
				style="margin-top: 15px; color: rgb(255, 193, 7); background: rgba(76, 175, 80, 0.97); box-shadow: 0 0 20px black; text-shadow: 0 0 13px black; font-size: 16px; cursor: pointer;"
				value="Upload Local Folder" name="upload" id="upload"
				onclick="submitform();" />

		</form>
	</div>
</body>




</html>