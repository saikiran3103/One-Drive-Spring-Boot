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

@Controller
public class OneDriveController {

	final static Logger logger = Logger.getLogger(OneDriveController.class);

	private UserService service;

	public OneDriveController(UserService service) {
		this.service = service;
	}

	@RequestMapping(value = "/model", method = RequestMethod.GET)
	public String printWelcome(ModelMap model) {

		model.addAttribute("message", "Spring 3 MVC Hello World");
		return "model";

	}

	@RequestMapping(value = "/hello/{name:.+}", method = RequestMethod.GET)
	public ModelAndView hello(@PathVariable("name") String name) {

		ModelAndView model = new ModelAndView();
		model.setViewName("model");
		model.addObject("msg", name);
		TokenAndPath tokenAndPath = new TokenAndPath();
		tokenAndPath.setPath("/sai/path");
		tokenAndPath.setToken("12345token");
		model.addObject("token", tokenAndPath);

		return model;

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
		// System.out.println(request.get;
		// String path =request.getPathInfo();
		System.out.println(request.getParameter("param1"));
		// HttpSession session = request.getSession();
		// session.setAttribute("token", request.getParameter("param1"));
		// System.out.println("saiiiiii"+" "+path);

		// request.getParameterMap();
		logger.info("Request" + request.toString());

		return "welcome";
	}

	// @RequestMapping(method = RequestMethod.POST, value="download")
	// public ModelAndView finaldownload(TokenAndPath tokenAndPath ) throws
	// URISyntaxException, IOException, JsonSyntaxException,
	// IllegalStateException, InterruptedException, NumberFormatException,
	// OpenXML4JException, XmlException {
	// return service.finaldownload(tokenAndPath);
	// }

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

	// method to upload files to one drive
	@RequestMapping(method = RequestMethod.POST, value = "onedrive/uploadfiles")
	public ModelAndView uploadDocumentsToOneDrive(HttpServletRequest request) throws URISyntaxException, IOException,
			JsonSyntaxException, IllegalStateException, InterruptedException, NumberFormatException, OpenXML4JException,
			XmlException, ServletException, FileUploadException, MessagingException {
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
		InputStream fileContent = (FileInputStream) filePart.getInputStream();

		String nameOfFile = filePart.getSubmittedFileName();

		byte[] fileArray = filePart.getSubmittedFileName().getBytes();

		logger.info("inside upload files");

		long sizeOfInputStream = (long) fileContent.available();

		long fourMBbsize = 4194304;
		int count = 0;

		if (sizeOfInputStream > 4194304) {
			// return
			// service.uploadLargeDocumentsToOneDriveSDK(tokenAndPath,fileContent,nameOfFile);

			return service.uploadLargeDocumentsToOneDriveSDKByInputStream(tokenAndPath, fileContent, nameOfFile);
		}

		return service.uploadDocumentsToOneDrive(tokenAndPath, fileContent, nameOfFile);
		// return "displayPath";
	}

	// method to add comment to a file
	@RequestMapping(method = RequestMethod.GET, value = "onedrive/comment")
	public ModelAndView addComment()
			throws URISyntaxException, IOException, JsonSyntaxException, IllegalStateException, InterruptedException,
			NumberFormatException, OpenXML4JException, XmlException, ServletException, FileUploadException,
			TransformerFactoryConfigurationError, ParserConfigurationException, SAXException, TransformerException {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		dbf.setValidating(false);

		DocumentBuilder db = dbf.newDocumentBuilder();

		String path = "C:/Users/sai.kiran.akkireddy/Downloads/testDownload/pdf.pdf";

		Document doc = db
				.parse(new FileInputStream(new File("C:/Users/sai.kiran.akkireddy/Downloads/testDownload/pdf.pdf")));

		Element element = doc.getDocumentElement();

		Comment comment = doc.createComment("This is a comment");
		element.getParentNode().insertBefore(comment, element);

		Path path1;

		File myFile = new File("C:/Users/sai.kiran.akkireddy/Downloads/testDownload/roles.docx");
		OPCPackage pkg = OPCPackage.open(myFile);
		POIXMLProperties poixmlProperties = new POIXMLProperties(pkg);

		PackagePropertiesPart ppropsPart = poixmlProperties.getCoreProperties().getUnderlyingProperties();
		ppropsPart.setTitleProperty("changed by admin ");

		ppropsPart.getContentTypeDetails();

		URL resource = getClass().getResource("testfile.txt");

		path1 = Paths.get(resource.toURI());

		Files.setAttribute(path1, "user:xdg.comment", ByteBuffer.wrap("Halllo".getBytes(StandardCharsets.UTF_8)),
				LinkOption.NOFOLLOW_LINKS);

		Transformer tf = TransformerFactory.newInstance().newTransformer();

		tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		tf.setOutputProperty(OutputKeys.INDENT, "yes");

		Writer out = new StringWriter();

		tf.transform(new DOMSource(doc), new StreamResult(out));

		System.out.println(out.toString());

		return null;

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
		
		String driveId = "b!xTDMGJt6IEiuUTWPKWl2DIgyJcgGyIxOnPrOum8TeyfKUQRBWwV8TofsOMwgqCI2";

		logger.info("Getting the files for the drive id  " + driveId);

		System.out.println(session.getAttribute("token"));

		TokenAndPath tokenAndPath = new TokenAndPath();

		tokenAndPath.setToken((String) session.getAttribute("token"));

		tokenAndPath.setDriveId(driveId);
		
		return service.uploadFolderToOneDrive(tokenAndPath);
		
	//	return null;
	}

}