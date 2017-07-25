package com.onedrive;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.xmlbeans.XmlException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.SAXException;

import com.google.gson.JsonSyntaxException;
import com.itextpdf.text.DocumentException;

@Controller
public class OneDriveController {

	final static Logger logger = Logger.getLogger(OneDriveController.class);

	private UserService service;

	public OneDriveController(UserService service) {
		this.service = service;
	}

	/* method to get to the welcome page , takes to hello.jsp*/
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView connect(ModelMap model) {

		ModelAndView model1 = new ModelAndView();
		model1.setViewName("hello");
		TokenAndPath tokenAndPath = new TokenAndPath();

		String home = System.getProperty("user.home");

		tokenAndPath.setPath(home);

		model1.addObject("token", tokenAndPath);
		return model1;
		// return "hello";

	}

	
	/* method to authorize the user and get token , response or token will be sent to redirect url*/
	@RequestMapping(value = "/token", method = RequestMethod.GET)
	public String authorizeAndGetUserToken() throws URISyntaxException {

		return service.authorizeAndGetUserToken();
	}

	/* method to handle the redirect token sent and parse token jsp is view  */
	@RequestMapping(value = "onedrive/redirect", method = RequestMethod.GET)
	public String readToken(@RequestParam(value = "code", required = false) String code, HttpServletRequest request)
			throws URISyntaxException {

		return "parsetoken";
	}
	
	
	// method to extra  redirect to get the token from hash url 
		@RequestMapping(method = RequestMethod.POST, value = "onedrive/downloadfiles")
		public String getTokenAndPath1(HttpServletRequest request)
				throws URISyntaxException, IOException, JsonSyntaxException, IllegalStateException, InterruptedException,
				NumberFormatException, OpenXML4JException, XmlException {
			HttpSession session = request.getSession();
			session.setAttribute("token", request.getParameter("param1"));
		
		
			return "downloadfilesview";
			
		}

	/*
	 *  Method to download the files from personal one drive
	 * 
	 * token from the session attribute
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "onedrive/personalfiles")
	public ModelAndView getPersonalFilesAndConvertToText(HttpServletRequest request)
			throws URISyntaxException, IOException, JsonSyntaxException, IllegalStateException, InterruptedException,
			NumberFormatException, OpenXML4JException, XmlException {
		

		HttpSession session = request.getSession();

		TokenAndPath tokenAndPath = new TokenAndPath();
		tokenAndPath.setToken((String) session.getAttribute("token"));
		tokenAndPath.setPath(request.getParameter("param2"));

		return service.personalItemsDownloadAndConvert(tokenAndPath);

	}
	// method to display the list of user names for the shared files

	@RequestMapping(method = RequestMethod.POST, value = "onedrive/shareditems")
	public ModelAndView getSharedUsers(HttpServletRequest request)
			throws URISyntaxException, IOException, JsonSyntaxException, IllegalStateException, InterruptedException,
			NumberFormatException, OpenXML4JException, XmlException {
		

		HttpSession session = request.getSession();

		session.setAttribute("sharedItemUrl", request.getParameter("param3"));

		
		TokenAndPath tokenAndPath = new TokenAndPath();
		tokenAndPath.setToken((String) session.getAttribute("token"));
		tokenAndPath.setPath(request.getParameter("param3"));

		return service.listSharedUsers(tokenAndPath);

	}

	/* method to download the files from the users shared drive */

	@RequestMapping(method = RequestMethod.POST, value = "onedrive/downloadsharedfiles")
	public ModelAndView getSharedFilesAndConvertToText(HttpServletRequest request)
			throws URISyntaxException, IOException, JsonSyntaxException, IllegalStateException, InterruptedException,
			NumberFormatException, OpenXML4JException, XmlException {
	
		
		HttpSession session = request.getSession();

		
		String driveId = request.getParameter("driveId");
		String sharedItemUrl = (String) session.getAttribute("sharedItemUrl");
		logger.info("Getting the files for the drive id  " + driveId);
		
		TokenAndPath tokenAndPath = new TokenAndPath();
		tokenAndPath.setToken((String) session.getAttribute("token"));
		tokenAndPath.setDriveId(driveId);

		// take the path from the seesion stored in the previous call
		tokenAndPath.setPath(sharedItemUrl);

	
		return service.sharedItemsDownloadAndConvert(tokenAndPath);

	}

	

	
	//method to go to the upload jsp page
	@RequestMapping(value = "onedrive/upload", method = RequestMethod.GET)
	public ModelAndView goToUploadJsp(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();

		modelAndView.setViewName("uploadfile");
		return modelAndView;

	}

	// method to upload a single file to shared drive, consists of large file and small file implementation
	@RequestMapping(method = RequestMethod.POST, value = "onedrive/uploadfiles")
	public ModelAndView uploadDocumentsToOneDrive(HttpServletRequest request) throws URISyntaxException, IOException,
			JsonSyntaxException, IllegalStateException, InterruptedException, NumberFormatException, OpenXML4JException,
			XmlException, ServletException, FileUploadException, MessagingException, DocumentException {
		HttpSession session = request.getSession();
		// String driveId = request.getParameter("driveId");

		

		

		

		TokenAndPath tokenAndPath = new TokenAndPath();

		tokenAndPath.setToken((String) session.getAttribute("token"));

		

		final Part filePart = request.getPart("file");
		int fileSize = (int) filePart.getSize();

		tokenAndPath.setFileSize(fileSize);

		

		
		FileInputStream fileContent = (FileInputStream) filePart.getInputStream();

		FileInputStream fileContentForUpload = (FileInputStream) filePart.getInputStream();

		String nameOfFile = filePart.getSubmittedFileName();

		
		
		long sizeOfInputStream = (long) fileContent.available();

		long fourMBbsize = 4194304;
	

		if (sizeOfInputStream > fourMBbsize) {
		

			return service.uploadLargeDocumentsToOneDriveSDKByInputStream(tokenAndPath, fileContent,
					fileContentForUpload, nameOfFile);
		}

		return service.uploadDocumentsToOneDrive(tokenAndPath, fileContent, fileContentForUpload, nameOfFile);
		
	}

	/*
	 * Method to upload a folder to one drive
	 * 
	 * Takes the input as the path of the folder
	 * 
	 * Uploads to one drive
	 * 
	 */

	@RequestMapping(method = RequestMethod.POST, value = "onedrive/uploadfolder")
	public ModelAndView uploadFolderToOneDrive(HttpServletRequest request) throws URISyntaxException, IOException,
			JsonSyntaxException, IllegalStateException, InterruptedException, NumberFormatException, OpenXML4JException,
			XmlException, ServletException, FileUploadException, TransformerFactoryConfigurationError,
			ParserConfigurationException, SAXException, TransformerException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, MessagingException, UnsupportedLookAndFeelException {

		HttpSession session = request.getSession();

		System.out.println(session.getAttribute("token"));

		TokenAndPath tokenAndPath = new TokenAndPath();

		tokenAndPath.setToken((String) session.getAttribute("token"));

		return service.uploadFolderToOneDrive(tokenAndPath);

		
	}

}