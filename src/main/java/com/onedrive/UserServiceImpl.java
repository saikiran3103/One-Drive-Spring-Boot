package com.onedrive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.xmlbeans.XmlException;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


@Controller
public class UserServiceImpl implements UserService {

	private static final int BUFFER_SIZE = 4096;

	
	final static Logger logger = Logger.getLogger(UserServiceImpl.class);

	private String home = System.getProperty("user.home");

	private String saveDir = home;

	private WordExtractor wd;

	@Override
	public String authorizeAndGetUserToken() throws URISyntaxException  {

		String url="https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id=c00a4c26-e64b-459b-91f6-31571b802ae4&scope=files.read.all&response_type=token&redirect_uri=http://localhost:8080/onedrive/redirect";
		String os = System.getProperty("os.name").toLowerCase();
	try {
		
		
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		final HttpGet httpRequest = new HttpGet( url );


		logger.info(httpRequest);
		
		HttpResponse response = httpClient.execute(httpRequest);

			Runtime rt = Runtime.getRuntime();
			if(os.indexOf( "win" ) >= 0){


				rt.exec( "rundll32 url.dll,FileProtocolHandler " + url);

			}
			else if(os.indexOf( "mac" ) >= 0){


				rt.exec( "open" + url);
			}


			else if(os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0){
				String[] browsers = {"epiphany", "firefox", "mozilla", "konqueror",
						"netscape","opera","links","lynx"};

				StringBuffer cmd = new StringBuffer();
				for (int i=0; i<browsers.length; i++)
					cmd.append( (i==0  ? "" : " || " ) + browsers[i] +" \"" + url + "\" ");

				rt.exec(new String[] { "sh", "-c", cmd.toString() });
		
	}
	}catch(Exception ex){
			logger.error(" error occured"  + ex.getMessage());
			ex.printStackTrace();
		}




		return null;
	}

	@Override
	public ModelAndView personalItemsDownloadAndConvert(TokenAndPath tokenAndPath) throws IOException, IllegalStateException, JsonSyntaxException, InterruptedException, NumberFormatException {

		ModelAndView enterLinkView = new ModelAndView();
	try{
		
		SuccessMessageObject messageObject= new SuccessMessageObject();
		
		String access_token= tokenAndPath.getToken();
		
		String tokenheader = "Bearer"+" "+access_token;
		
		
		
		String fileUrl=tokenAndPath.getPath();
		
		if(fileUrl.contains("?")){
			int indexOfQueryParam = fileUrl.indexOf("?");
			
			 fileUrl= fileUrl.substring(0, indexOfQueryParam);
		}
		
		
		// path for the single file
		if((fileUrl.endsWith(".pdf")) || (fileUrl.endsWith(".PDF")) || (fileUrl.endsWith(".DOCS"))|| (fileUrl.endsWith(".docs")) 
				|| (fileUrl.endsWith(".DOCX"))|| (fileUrl.endsWith(".docx")) || (fileUrl.endsWith(".pptx")) || (fileUrl.endsWith(".PPTX")) || 
				(fileUrl.endsWith(".PPT")) || (fileUrl.endsWith(".ppt"))
				|| (fileUrl.endsWith(".pdf")) || (fileUrl.endsWith(".xlsx")) || (fileUrl.endsWith(".XLSX")) || (fileUrl.endsWith(".xls")) ||
				(fileUrl.endsWith(".XLS"))){
			
			
		
			
			String base_path = tokenAndPath.getPath();//replaceAll("%20", " ");

			// gets the start index after the documents path
			int indexAfterDocuments =base_path.lastIndexOf("Documents")+10;


			String file = base_path.substring(indexAfterDocuments);
			
		String oneDriveFileUrl ="https://graph.microsoft.com/beta/me/drive/root:/"+file;
		
		int local_directory =file.lastIndexOf("/")+1;
		
		String local_folder = file.substring(local_directory);
		
		String MakeLocalDirectory =local_folder.replace("%20", " ");
		
		
		int indexToRemoveExntension = MakeLocalDirectory.lastIndexOf(".");
		
		String extensionLessDirectory = MakeLocalDirectory.substring(0, indexToRemoveExntension);

		
		File dir = new File(saveDir+"\\Downloads\\"+extensionLessDirectory);
	
	    dir.mkdirs();
		
		SuccessMessageObject responseAndMessage= UserServiceImpl.doGet(oneDriveFileUrl, tokenheader);
		
		String responseFromAdaptor= responseAndMessage.getResponse();
		
          final Gson gson = new Gson();
		
		if(responseAndMessage.getMessage()!=null && responseAndMessage.getMessage().equalsIgnoreCase("error")){
			Error errorMessage = gson.fromJson(responseFromAdaptor, Error.class);
			ModelAndView errorView = new ModelAndView();
			errorView.addObject("error", errorMessage);
			errorView.setViewName("display");
			return errorView;
		}
		
		MetaDataForFolder outerMetaData =gson.fromJson(responseFromAdaptor, MetaDataForFolder.class);
		
		
		
	
		
				String Url = outerMetaData.getMicrosoft_graph_downloadUrl();
				
		
		

		
		UserServiceImpl.downloadFile(Url,dir.getPath());
		
		
		
		fileReaderAndConverter(file, dir);
		
		messageObject.setMessage(" Your files are downloaded to "+dir.getPath().toString());
		enterLinkView.addObject("message",messageObject );
		enterLinkView.setViewName("display");
		
		return enterLinkView;
		
			
		}
		
		enterLinkView.setViewName("display");
		

		



		String commonUrl ="https://graph.microsoft.com/beta/me/";

		//	String base_path = "https://myoffice.accenture.com/personal/sai_kiran_akkireddy_accenture_com/Documents/testDownload";
		String base_path = tokenAndPath.getPath();//replaceAll("%20", " ");

		// gets the start index after the documents path
		int indexAfterDocuments =base_path.lastIndexOf("Documents")+10;


		String file = base_path.substring(indexAfterDocuments);

		int local_directory =file.lastIndexOf("/")+1;
		String local_folder = file.substring(local_directory);

		String child =":/children";

		String MakeLocalDirectory =local_folder.replace("%20", " ");


		String completeurl= commonUrl+"drive/root:/"+file+child;

		System.out.println("saiiii"+""+file);


		//making a directory
		File dir = new File(saveDir+"\\Downloads\\"+MakeLocalDirectory);
		dir.mkdirs();


		System.out.println(completeurl);





		//make a get call to one drive api
		
		
		


		SuccessMessageObject responseAndMessage= UserServiceImpl.doGet(completeurl, tokenheader);
		
		
		String responseFromAdaptor= responseAndMessage.getResponse();
		
		final Gson gson = new Gson();
		
		if(responseAndMessage.getMessage()!=null && responseAndMessage.getMessage().equalsIgnoreCase("error")){
			Error errorMessage = gson.fromJson(responseFromAdaptor, Error.class);
			ModelAndView errorView = new ModelAndView();
			errorView.addObject("error", errorMessage);
			errorView.setViewName("display");
			return errorView;
		}
		
		OuterMetaData outerMetaData =gson.fromJson(responseFromAdaptor, OuterMetaData.class);

		System.out.println("json form ");
		System.out.println(outerMetaData);
		List<String> downloadUrls = new ArrayList<String>();
		for (MetaDataForFolder metaDataForFolder:outerMetaData.getValue()){

			if(metaDataForFolder.getFolder()!=null &&
					(Integer.parseInt(metaDataForFolder.getFolder().getChildCount())>=1)){
				readingInnerFolders(tokenheader, commonUrl,base_path, child, file, dir, gson, metaDataForFolder);
			}else{
				String Url = metaDataForFolder.getMicrosoft_graph_downloadUrl();
				downloadUrls.add(Url);
			}
		}


		System.out.println(downloadUrls);

	


		// create the size of the thread pool dynamically
		if(!downloadUrls.isEmpty()){
		ExecutorService executor = Executors.newFixedThreadPool(downloadUrls.size());
		final long startTime = System.currentTimeMillis();
		for (String downloadUrl:downloadUrls){

			System.out.println("saveDir------>"+saveDir);			
			// multithreading framework for downloading files
			Runnable download= new MultiDownLoadExecutor(downloadUrl, dir.getPath());
			executor.execute(download);
		}
		executor.shutdown();
		
		final long endTime = System.currentTimeMillis();
		System.out.println("Time taken to get Response in millis:" + ( endTime - startTime ));

		concurrentConverter(file, dir, executor);
		}
		else{
			
			// for empty urls just convert the files
			fileReaderAndConverter(file, dir);
		}
		messageObject.setMessage(" Your files are downloaded to "+dir.getPath().toString());
		enterLinkView.addObject("message",messageObject );
		
		logger.info(enterLinkView);
		
	}
	catch(Exception universalException){
		SuccessMessageObject messageObject= new SuccessMessageObject();
		messageObject.setMessage(universalException.getMessage());
		enterLinkView.addObject("message",messageObject );
        logger.info("error occured" +universalException.getMessage());
        return enterLinkView;

	}
	return enterLinkView;	

	}

	private void concurrentConverter(String file, File dir, ExecutorService executor)
			throws InterruptedException, IOException {
		if(  (executor.awaitTermination(900, TimeUnit.SECONDS) )){
			fileReaderAndConverter(file, dir);
		}
	}

	private void fileReaderAndConverter(String file, File dir) throws IOException {
		List<File> filesInFolder = Files.walk(Paths.get(dir.getPath()))
				.filter(Files::isRegularFile)
				.map(Path::toFile)
				.collect(Collectors.toList());
		ExecutorService converterExecutor = Executors.newFixedThreadPool(filesInFolder.size());
		for(File officefile:filesInFolder){

			//parallel conversion of all files 
			Runnable converter= new ParallelConverter(officefile, file);
			converterExecutor.execute(converter);

		}
		converterExecutor.shutdown();
	}

	private void readingInnerFolders(String tokenheader, String commonUrl, String base_path,String child, String file, File dir,
			final Gson gson, MetaDataForFolder metaDataForFolder) throws ClientProtocolException, IOException, InterruptedException {




		int indexAfterDocuments =base_path.lastIndexOf("Documents")+10;


		String file1 = base_path.substring(indexAfterDocuments);

		String path= metaDataForFolder.getParentReference().getPath();




		// get the name of inside folder
		String insideFoldername=	metaDataForFolder.getName();

		// form the url to get the children files for inside folder

		String OneDriveinsideFolderUrl =commonUrl+path+"/"+insideFoldername+child;

		// make a local directory with the folder structure

		String localinsideFolderName=insideFoldername.replaceAll("%20", " ");  

		File innerdir1 = new File(dir.getPath()+"\\"+localinsideFolderName);

		innerdir1.mkdirs();

		// make a call 
		SuccessMessageObject messageObject= UserServiceImpl.doGet(OneDriveinsideFolderUrl, tokenheader);

		String responseFromAdaptor1=messageObject.getResponse();
		
		if(messageObject.getMessage()!=null && messageObject.getMessage().equalsIgnoreCase("error")){
			Error errorMessage = gson.fromJson(responseFromAdaptor1, Error.class);
			ModelAndView errorView = new ModelAndView();
			errorView.addObject("error", errorMessage);
			
		}
		
		OuterMetaData outerMetaData1 =gson.fromJson(responseFromAdaptor1, OuterMetaData.class);

		List<String> downloadUrls1 = new ArrayList<String>();

		for (MetaDataForFolder metaDataForFolder1:outerMetaData1.getValue()){
			if(metaDataForFolder1.getFolder()!=null &&
					(Integer.parseInt(metaDataForFolder1.getFolder().getChildCount())>=1)){
				InnerFoldersReaderUtility.processAndDownloadSubFolders(tokenheader, commonUrl,base_path, child, file1, innerdir1, gson, metaDataForFolder1);
			}
			else{
				String Url1 = metaDataForFolder1.getMicrosoft_graph_downloadUrl();
				downloadUrls1.add(Url1);
			}
		}
		if (!downloadUrls1.isEmpty()){
		ExecutorService executor1 = Executors.newFixedThreadPool(downloadUrls1.size());
		for (String downloadUrl1:downloadUrls1){


			// multithreading framework for downloading files
			Runnable download1= new MultiDownLoadExecutor(downloadUrl1, innerdir1.getPath());
			executor1.execute(download1);
		}
		executor1.shutdown();
		//		if(  (executor1.awaitTermination(20, TimeUnit.SECONDS) )){
		//			List<File> filesInFolder = Files.walk(Paths.get(innerdir1.getPath()))
		//					.filter(Files::isRegularFile)
		//					.map(Path::toFile)
		//					.collect(Collectors.toList());
		//			ExecutorService converterExecutor = Executors.newFixedThreadPool(filesInFolder.size());
		//			for(File officefile:filesInFolder){
		//
		//				//parallel conversion of all files 
		//				Runnable converter= new ParallelConverter(officefile, insideFoldername);
		//				converterExecutor.execute(converter);
		//
		//			}
		//			converterExecutor.shutdown();
		//		}
	}
	}

	public static void downloadFile(String fileURL, String saveDir)
			throws IOException {
		URL url = new URL(fileURL);
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		int responseCode = httpConn.getResponseCode();

		// always check HTTP response code first
		if (responseCode == HttpURLConnection.HTTP_OK) {
			String fileName = "";
			String disposition = httpConn.getHeaderField("Content-Disposition");
			String contentType = httpConn.getContentType();
			int contentLength = httpConn.getContentLength();

			if (disposition != null) {
				// extracts file name from header field
				int index = disposition.indexOf("filename=");
				if (index > 0) {
					fileName = disposition.substring(index + 10,
							disposition.length() - 1);
				}
			} else {
				// extracts file name from URL
				fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
						fileURL.length());
			}

			System.out.println("Content-Type = " + contentType);
			System.out.println("Content-Disposition = " + disposition);
			System.out.println("Content-Length = " + contentLength);
			System.out.println("fileName = " + fileName);

			// opens input stream from the HTTP connection
			InputStream inputStream = httpConn.getInputStream();
			String saveFilePath = saveDir + File.separator + fileName;

			// opens an output stream to save into file
			FileOutputStream outputStream = new FileOutputStream(saveFilePath);

			int bytesRead = -1;
			byte[] buffer = new byte[BUFFER_SIZE];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			outputStream.close();
			inputStream.close();

			System.out.println("File downloaded");
		} else {
			System.out.println("No file to download. Server replied HTTP code: " + responseCode);
		}
		httpConn.disconnect();
	}

	public  static SuccessMessageObject doGet( final String url,String tokenheader ) throws ClientProtocolException, IOException{
		
		
		
		try{
		SuccessMessageObject messageObject = new SuccessMessageObject();
		DefaultHttpClient httpClient = new DefaultHttpClient();
		final HttpGet httpRequest = new HttpGet( url );

		httpRequest.addHeader("Content-Type", "text/plain");
		httpRequest.addHeader("Authorization", tokenheader);

		logger.info(httpRequest.getMethod());
		
		HttpResponse response = httpClient.execute(httpRequest);
		
		logger.info(response);

		if ( null == response )	{
			logger.error( "Http Request failed, httpResponse is null." );
			messageObject.setMessage("error");
			logger.error("HTTP response is null");
			releaseHttpConnection( httpRequest );
			
		}

		if ( null == response.getStatusLine() )	{
			logger.error( "Http Request failed, httpResponse is null." );
			messageObject.setMessage("error");
			logger.error("HTTP getStatusLine response is null");
			releaseHttpConnection( httpRequest );
			
		}
		
		final Integer httpStatusCode = response.getStatusLine().getStatusCode();
		
		final org.apache.http.HttpEntity entity = (org.apache.http.HttpEntity) response.getEntity();
		final String responseString = EntityUtils.toString( (org.apache.http.HttpEntity) entity, "UTF-8" );
		EntityUtils.consume( entity );
		logger.info(httpRequest.toString());
		System.out.println(responseString);
		httpClient.getConnectionManager().shutdown();
		
		
		messageObject.setResponse(responseString);
		messageObject.setMessage("success");	
		
		if ( httpStatusCode != null && !httpStatusCode.equals( HttpStatus.SC_OK ) ){
			messageObject.setMessage("error");	
		}
		
		
		return messageObject;
		}
		catch(Exception ex){
			
			SuccessMessageObject messageObject= new SuccessMessageObject();
			messageObject.setMessage("error");
			
	        logger.info("error occured" +ex.getMessage());
	        return messageObject;
		}
	    
		
		
		
		
	}
	
	
	private static void releaseHttpConnection( final HttpRequestBase httpRequest ) {
		if ( null != httpRequest ) {
			httpRequest.abort();;
			if ( !httpRequest.isAborted() ) {
				httpRequest.abort();
			}
		}
	}


	@Override
	public ModelAndView listSharedUsers(TokenAndPath tokenAndPath)
			throws IOException, IllegalStateException, JsonSyntaxException, InterruptedException, NumberFormatException,
			OpenXML4JException, XmlException {
		
		
		
		ModelAndView enterLinkView = new ModelAndView();
		try{
			
			SuccessMessageObject messageObject= new SuccessMessageObject();
			
			
			
			enterLinkView.setViewName("test1");
			String access_token= tokenAndPath.getToken();

			String tokenheader = "Bearer"+" "+access_token;



			String urlForSharedWithMeItems ="https://graph.microsoft.com/beta/me/drive/sharedWithMe";

		




			//make a get call to one drive api
			
			
			
            SuccessMessageObject responseAndMessage= UserServiceImpl.doGet(urlForSharedWithMeItems, tokenheader);
			
			
			String responseFromAdaptor= responseAndMessage.getResponse();
			
			final Gson gson = new Gson();
			
			
			
			if(responseAndMessage.getMessage()!=null && responseAndMessage.getMessage().equalsIgnoreCase("error")){
				Error errorMessage = gson.fromJson(responseFromAdaptor, Error.class);
				ModelAndView errorView = new ModelAndView();
				errorView.addObject("error", errorMessage);
				errorView.setViewName("display");
				return errorView;
			}
			
			OuterMetaDataForSharedItems outerMetaData =gson.fromJson(responseFromAdaptor, OuterMetaDataForSharedItems.class);
			
			HashMap<String, User> namesOfAllSharingUsers = new HashMap<String, User>();
			
			Set<String> displayNames = new HashSet<String>();
			
			HashMap<String, String> namesAndDriveId = new HashMap<String, String>();
			
			for (MetaDataForSharedItem metaDataForFolder:outerMetaData.getValue()){
				
			//	String driveId =metaDataForFolder.getParentReference().getDriveId();
				namesOfAllSharingUsers=	metaDataForFolder.getCreatedBy();
			String driveId=	"b!xTDMGJt6IEiuUTWPKWl2DIgyJcgGyIxOnPrOum8TeyfKUQRBWwV8TofsOMwgqCI2";
				Collection<User> users=namesOfAllSharingUsers.values();
				Iterator<User> itr = 	users.iterator();
				while(itr.hasNext()) {
					User currentDriveUser = (User) itr.next();
					displayNames.add(currentDriveUser.getDisplayName());
					namesAndDriveId.put(currentDriveUser.getDisplayName(), driveId);
		}
						}

			enterLinkView.setViewName("shareduserslist");
			enterLinkView.addObject("sharedusers", namesAndDriveId);
			System.out.println("json form ");
			System.out.println(outerMetaData);

		
		
	}catch(Exception universalException){
		logger.error(universalException.getStackTrace());
		SuccessMessageObject messageObject= new SuccessMessageObject();
		messageObject.setMessage(universalException.getMessage());
		enterLinkView.addObject("message",messageObject );
        logger.error("error occured" +universalException.getCause());
        return enterLinkView;
	}
		return enterLinkView;
	}

	@Override
	public ModelAndView sharedItemsDownloadAndConvert(TokenAndPath tokenAndPath)
			throws IOException, IllegalStateException, JsonSyntaxException, InterruptedException, NumberFormatException,
			OpenXML4JException, XmlException {
		
		ModelAndView enterLinkView = new ModelAndView();
		try{
			
			SuccessMessageObject messageObject= new SuccessMessageObject();
			
		
			
			enterLinkView.setViewName("display");
			
			
			String access_token= tokenAndPath.getToken();

			String tokenheader = "Bearer"+" "+access_token;


			
			String driveId = tokenAndPath.getDriveId();

			String commonUrl ="https://graph.microsoft.com/beta";

			//	String base_path = "https://myoffice.accenture.com/personal/sai_kiran_akkireddy_accenture_com/Documents/testDownload";
			String base_path = tokenAndPath.getPath();//replaceAll("%20", " ");

			// gets the start index after the documents path
			int indexAfterDocuments =base_path.lastIndexOf("Documents")+10;


			String folderPathAfterdocuments = base_path.substring(indexAfterDocuments);

			int local_directory =folderPathAfterdocuments.lastIndexOf("/")+1;
			
			
			String local_folder = folderPathAfterdocuments.substring(local_directory);
			

			String childAppender =":/children";

			String MakeLocalDirectory =local_folder.replace("%20", " ");


			String completeurl= commonUrl+"/drives/"+driveId+"/root:/"+folderPathAfterdocuments+childAppender;

			System.out.println("saiiii"+""+folderPathAfterdocuments);


			//making a directory
			File dir = new File(saveDir+"\\Downloads\\"+MakeLocalDirectory);
			dir.mkdirs();


			System.out.println(completeurl);





			//make a get call to one drive api
			
			
			SuccessMessageObject responseAndMessage= UserServiceImpl.doGet(completeurl, tokenheader);
			
			
			String responseFromAdaptor= responseAndMessage.getResponse();
			
			logger.info(responseFromAdaptor);
			
			final Gson gson = new Gson();
			
			if(responseAndMessage.getMessage()!=null && responseAndMessage.getMessage().equalsIgnoreCase("error")){
				Error errorMessage = gson.fromJson(responseFromAdaptor, Error.class);
				ModelAndView errorView = new ModelAndView();
				errorView.addObject("error", errorMessage);
				errorView.setViewName("display");
				return errorView;
			}
			
			OuterMetaData outerMetaData =gson.fromJson(responseFromAdaptor, OuterMetaData.class);

			System.out.println("json form ");
			System.out.println(outerMetaData);
			List<String> downloadUrls = new ArrayList<String>();
			for (MetaDataForFolder metaDataForFolder:outerMetaData.getValue()){

				if(metaDataForFolder.getFolder()!=null &&
						(Integer.parseInt(metaDataForFolder.getFolder().getChildCount())>=0)){
					readingInnerFolders(tokenheader, commonUrl,base_path, childAppender, folderPathAfterdocuments, dir, gson, metaDataForFolder);
				}else{
					String Url = metaDataForFolder.getMicrosoft_graph_downloadUrl();
					downloadUrls.add(Url);
				}
			}
		
			// create the size of the thread pool dynamically
			if(!downloadUrls.isEmpty()){
			ExecutorService executor = Executors.newFixedThreadPool(downloadUrls.size());
			final long startTime = System.currentTimeMillis();
			for (String downloadUrl:downloadUrls){

				System.out.println("saveDir------>"+saveDir);			
				// multithreading framework for downloading files
				Runnable download= new MultiDownLoadExecutor(downloadUrl, dir.getPath());
				executor.execute(download);
			}
			executor.shutdown();
			
			final long endTime = System.currentTimeMillis();
			System.out.println("Time taken to get Response in millis:" + ( endTime - startTime ));

			concurrentConverter(folderPathAfterdocuments, dir, executor);
			}
			else{
				
				// if there are no urls in the current folder
				fileReaderAndConverter(folderPathAfterdocuments, dir);
			}
		
			messageObject.setMessage(" Your files are downloaded to "+dir.getPath().toString());
			enterLinkView.addObject("message",messageObject );
			
			logger.info(enterLinkView);
			
		}
		catch(Exception universalException){
			SuccessMessageObject messageObject= new SuccessMessageObject();
			messageObject.setMessage(universalException.getMessage());
			enterLinkView.addObject("message",messageObject );
	        logger.info("error occured" +universalException.getMessage());
	        return enterLinkView;

		}
		return enterLinkView;
}}