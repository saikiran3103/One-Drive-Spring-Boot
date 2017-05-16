package com.onedrive;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.xmlbeans.XmlException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.JsonSyntaxException;

@Controller
public class HelloController {
	
	
	final static Logger logger = Logger.getLogger(HelloController.class);
	
	private UserService service;

	public HelloController (UserService service) {
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
		//return "hello";

	}
		@RequestMapping(value = "/token", method = RequestMethod.GET)
		public String  authorizeAndGetUserToken() throws URISyntaxException {
			
		
			return service.authorizeAndGetUserToken();
		}

		@RequestMapping(value="onedrive/redirect",method = RequestMethod.GET )
		public String  readToken( @RequestParam(value = "code", required = false) String code, HttpServletRequest request) throws URISyntaxException {
//			System.out.println(request.get;
//			String path =request.getPathInfo();
			System.out.println(request.getParameter("param1"));
//			HttpSession session = request.getSession();
//			session.setAttribute("token", request.getParameter("param1"));
//			System.out.println("saiiiiii"+"   "+path);
			
//			request.getParameterMap();
			logger.info("Request"+request.toString());
			
			return "welcome";
		}

//		@RequestMapping(method = RequestMethod.POST, value="download")
//	    public ModelAndView   finaldownload(TokenAndPath tokenAndPath ) throws URISyntaxException, IOException, JsonSyntaxException, IllegalStateException, InterruptedException, NumberFormatException, OpenXML4JException, XmlException {
//			return service.finaldownload(tokenAndPath);
//		}
		
		
		/* final Method to download the files
		 * 
		 * 
		 * 
		 */
		@RequestMapping(method = RequestMethod.POST, value="onedrive/path1")
	    public ModelAndView getPersonalFilesAndConvertToText(HttpServletRequest request ) throws URISyntaxException, IOException, JsonSyntaxException, IllegalStateException, InterruptedException, NumberFormatException, OpenXML4JException, XmlException {
//			System.out.println(request.getParameter("param1"));
			System.out.println(request.getParameter("param2"));
			
			
			logger.info("HttpServletRequest"+request);
			HttpSession session = request.getSession();
			logger.info("Request"+request.toString());
			System.out.println(session.getAttribute("token"));
			TokenAndPath tokenAndPath=new TokenAndPath();
			tokenAndPath.setToken((String)session.getAttribute("token"));
			tokenAndPath.setPath(request.getParameter("param2"));
			logger.info("accesstoken: "+session.getAttribute("token"));
			return service.personalItemsDownloadAndConvert(tokenAndPath);
			
		}
		// method to display the list of user names for the shared files
		
		@RequestMapping(method = RequestMethod.POST, value="onedrive/shareditems")
	    public ModelAndView getSharedUsers(HttpServletRequest request ) throws URISyntaxException, IOException, JsonSyntaxException, IllegalStateException, InterruptedException, NumberFormatException, OpenXML4JException, XmlException {
//			System.out.println(request.getParameter("param1"));
			System.out.println(request.getParameter("param2"));
			HttpSession session = request.getSession();
			
			
			session.setAttribute("sharedItemUrl", request.getParameter("param3"));
			logger.info("Request"+request.toString());
			logger.info("In onedrive/shareditems" );

			System.out.println(session.getAttribute("token"));
			TokenAndPath tokenAndPath=new TokenAndPath();
			tokenAndPath.setToken((String)session.getAttribute("token"));
			tokenAndPath.setPath(request.getParameter("param3"));
			logger.info("accesstoken: "+session.getAttribute("token"));
			return service.listSharedUsers(tokenAndPath);
			
		}
		
		// method to download and convert the shared files 
		
		@RequestMapping(method = RequestMethod.POST, value="onedrive/downloadsharedfiles")
	    public ModelAndView getSharedFilesAndConvertToText(HttpServletRequest request ) throws URISyntaxException, IOException, JsonSyntaxException, IllegalStateException, InterruptedException, NumberFormatException, OpenXML4JException, XmlException {
//			System.out.println(request.getParameter("param1"));
			System.out.println(request.getParameter("param2"));
			HttpSession session = request.getSession();
		
			logger.info("Request"+request.toString());
			logger.info("In onedrive/shareditems" );
			 String driveId = request.getParameter("driveId");
			 String sharedItemUrl = 	(String)session.getAttribute("sharedItemUrl");
		    logger.info("Getting the files for the drive id  "+driveId );
			System.out.println(session.getAttribute("token"));
			TokenAndPath tokenAndPath=new TokenAndPath();
			tokenAndPath.setToken((String)session.getAttribute("token"));
			tokenAndPath.setDriveId(driveId);
			
			//take the path from the seesion stored in the previous call
			tokenAndPath.setPath(sharedItemUrl);
			
			
			
		
			logger.info("accesstoken: "+session.getAttribute("token"));
			return service.sharedItemsDownloadAndConvert(tokenAndPath);
			
		}
		
		
		//method to redirect to hide the token
		@RequestMapping(method = RequestMethod.POST, value="onedrive/path")
	    public String getTokenAndPath1(HttpServletRequest request ) throws URISyntaxException, IOException, JsonSyntaxException, IllegalStateException, InterruptedException, NumberFormatException, OpenXML4JException, XmlException {
			HttpSession session = request.getSession();
			session.setAttribute("token", request.getParameter("param1"));
			System.out.println(request.getParameter("param1"));
			System.out.println(session.getAttribute("token"));
			logger.info("sai is testing logs");
			return "test1";
			//return "displayPath";
		}
		
}