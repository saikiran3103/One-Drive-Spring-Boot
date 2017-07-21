package com.onedrive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.log4j.Logger;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

	@RequestMapping(value = "/token", method = RequestMethod.GET)
	public String authorizeAndGetUserToken() throws URISyntaxException {

		return service.authorizeAndGetUserToken();
	}

	@RequestMapping(value = "onedrive/redirect", method = RequestMethod.GET)
	public String readToken(@RequestParam(value = "code", required = false) String code, HttpServletRequest request)
			throws URISyntaxException {
		
		System.out.println(request.getParameter("param1"));
		

		logger.info("Request" + request.toString());

		return "welcome";
	}

	

	/*
	 * final Method to download the files
	 * 
	 * 
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "onedrive/path1")
	public ModelAndView getPersonalFilesAndConvertToText(HttpServletRequest request)
			throws URISyntaxException, IOException, JsonSyntaxException, IllegalStateException, InterruptedException,
			NumberFormatException, OpenXML4JException, XmlException {
		// System.out.println(request.getParameter("param1"));
		System.out.println(request.getParameter("param2"));

		logger.info("HttpServletRequest" + request);
		HttpSession session = request.getSession();
		logger.info("Request" + request.toString());
		System.out.println(session.getAttribute("token"));
		TokenAndPath tokenAndPath = new TokenAndPath();
		tokenAndPath.setToken((String) session.getAttribute("token"));
		tokenAndPath.setPath(request.getParameter("param2"));
		logger.info("accesstoken: " + session.getAttribute("token"));
		return service.personalItemsDownloadAndConvert(tokenAndPath);

	}
	// method to display the list of user names for the shared files

	@RequestMapping(method = RequestMethod.POST, value = "onedrive/shareditems")
	public ModelAndView getSharedUsers(HttpServletRequest request)
			throws URISyntaxException, IOException, JsonSyntaxException, IllegalStateException, InterruptedException,
			NumberFormatException, OpenXML4JException, XmlException {
		// System.out.println(request.getParameter("param1"));
		System.out.println(request.getParameter("param2"));
		HttpSession session = request.getSession();

		session.setAttribute("sharedItemUrl", request.getParameter("param3"));
		logger.info("Request" + request.toString());
		logger.info("In onedrive/shareditems");

		System.out.println(session.getAttribute("token"));
		TokenAndPath tokenAndPath = new TokenAndPath();
		tokenAndPath.setToken((String) session.getAttribute("token"));
		tokenAndPath.setPath(request.getParameter("param3"));
		logger.info("accesstoken: " + session.getAttribute("token"));
		return service.listSharedUsers(tokenAndPath);

	}

	// method to download and convert the shared files

	@RequestMapping(method = RequestMethod.POST, value = "onedrive/downloadsharedfiles")
	public ModelAndView getSharedFilesAndConvertToText(HttpServletRequest request)
			throws URISyntaxException, IOException, JsonSyntaxException, IllegalStateException, InterruptedException,
			NumberFormatException, OpenXML4JException, XmlException {
		// System.out.println(request.getParameter("param1"));
		System.out.println(request.getParameter("param2"));
		HttpSession session = request.getSession();

		logger.info("Request" + request.toString());
		logger.info("In onedrive/shareditems");
		String driveId = request.getParameter("driveId");
		String sharedItemUrl = (String) session.getAttribute("sharedItemUrl");
		logger.info("Getting the files for the drive id  " + driveId);
		System.out.println(session.getAttribute("token"));
		TokenAndPath tokenAndPath = new TokenAndPath();
		tokenAndPath.setToken((String) session.getAttribute("token"));
		tokenAndPath.setDriveId(driveId);

		// take the path from the seesion stored in the previous call
		tokenAndPath.setPath(sharedItemUrl);

		logger.info("accesstoken: " + session.getAttribute("token"));
		return service.sharedItemsDownloadAndConvert(tokenAndPath);

	}

	// method to redirect to hide the token
	@RequestMapping(method = RequestMethod.POST, value = "onedrive/path")
	public String getTokenAndPath1(HttpServletRequest request)
			throws URISyntaxException, IOException, JsonSyntaxException, IllegalStateException, InterruptedException,
			NumberFormatException, OpenXML4JException, XmlException {
		HttpSession session = request.getSession();
		session.setAttribute("token", request.getParameter("param1"));
		System.out.println(request.getParameter("param1"));
		System.out.println(session.getAttribute("token"));
		logger.info("sai is testing logs");
		return "test1";
		// return "displayPath";
	}

	@RequestMapping(value = "onedrive/upload", method = RequestMethod.GET)
	public ModelAndView goToUploadJsp(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();

		modelAndView.setViewName("uploadfile");
		return modelAndView;

	}

	// method to upload folder  to one drive
	@RequestMapping(method = RequestMethod.POST, value = "onedrive/uploadfiles")
	public ModelAndView uploadDocumentsToOneDrive(HttpServletRequest request) throws URISyntaxException, IOException,
			JsonSyntaxException, IllegalStateException, InterruptedException, NumberFormatException, OpenXML4JException,
			XmlException, ServletException, FileUploadException, MessagingException, DocumentException {
		HttpSession session = request.getSession();
		// String driveId = request.getParameter("driveId");

		String driveId = "b!xTDMGJt6IEiuUTWPKWl2DIgyJcgGyIxOnPrOum8TeyfKUQRBWwV8TofsOMwgqCI2";

		logger.info("Getting the files for the drive id  " + driveId);

		System.out.println(session.getAttribute("token"));

		TokenAndPath tokenAndPath = new TokenAndPath();

		tokenAndPath.setToken((String) session.getAttribute("token"));

		tokenAndPath.setDriveId(driveId);

		final Part filePart = request.getPart("file");
		int fileSize = (int) filePart.getSize();

		tokenAndPath.setFileSize(fileSize);

		String path = request.getParameter("path");

		tokenAndPath.setPath(path);

		logger.info(path + "path");

		logger.info(filePart.getSubmittedFileName().getBytes() + "filePart.getName()");
		FileInputStream fileContent = (FileInputStream) filePart.getInputStream();
		
		FileInputStream fileContentForUpload = (FileInputStream) filePart.getInputStream();
		
		

		String nameOfFile = filePart.getSubmittedFileName();

		byte[] fileArray = filePart.getSubmittedFileName().getBytes();

		System.out.println("fileContent2-->" +fileContentForUpload.available());

		long sizeOfInputStream = (long) fileContent.available();

		long fourMBbsize = 4194304;
		int count = 0;

		if (sizeOfInputStream > 4194304) {
			// return
			// service.uploadLargeDocumentsToOneDriveSDK(tokenAndPath,fileContent,nameOfFile);

			return service.uploadLargeDocumentsToOneDriveSDKByInputStream(tokenAndPath, fileContent,fileContentForUpload, nameOfFile);
		}

		return service.uploadDocumentsToOneDrive(tokenAndPath, fileContent,fileContentForUpload, nameOfFile);
		// return "displayPath";
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
	public ModelAndView uploadFolderToOneDrive(HttpServletRequest request)
			throws URISyntaxException, IOException, JsonSyntaxException, IllegalStateException, InterruptedException,
			NumberFormatException, OpenXML4JException, XmlException, ServletException, FileUploadException,
			TransformerFactoryConfigurationError, ParserConfigurationException, SAXException, TransformerException, ClassNotFoundException, InstantiationException, IllegalAccessException, MessagingException, UnsupportedLookAndFeelException {

		HttpSession session = request.getSession();
		
		

		
		System.out.println(session.getAttribute("token"));

		TokenAndPath tokenAndPath = new TokenAndPath();

		tokenAndPath.setToken((String) session.getAttribute("token"));

		
		
		return service.uploadFolderToOneDrive(tokenAndPath);
		
	//	return null;
	}
	
	
	

	

}