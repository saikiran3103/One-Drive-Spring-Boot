package com.onedrive;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
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
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;


/**
 * @author sai.kiran.akkireddy
 *
 */
@Controller
public class UserServiceImpl implements UserService {

	private static final int BUFFER_SIZE = 4096;

	private UploadSession uploadSession;

	private String baseUrl = "https://api.onedrive.com/v1.0/";
	private boolean canceled = false;
	private boolean finished = false;

	private static final int chunkSize = 320 * 1024 * 30;
	final static Logger logger = Logger.getLogger(UserServiceImpl.class);

	private String home = System.getProperty("user.home");

	private String saveDir = home;

	private RandomAccessFile randFile;



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

				System.out.println("responseFromAdaptor   "+responseFromAdaptor);

				Gson gson = new Gson();



				if(responseAndMessage.getMessage()!=null && responseAndMessage.getMessage().equalsIgnoreCase("error")){

					ModelAndView errorView = new ModelAndView();

					Error error = gson.fromJson(responseFromAdaptor, Error.class);


					messageObject.setMessage(error.getError().getCode());

					errorView.addObject("message", messageObject);

					errorView.setViewName("display");

					return errorView;
				}

				MetaDataForFolder outerMetaData =gson.fromJson(responseFromAdaptor, MetaDataForFolder.class);





				String Url = outerMetaData.getMicrosoft_graph_downloadUrl();





				UserServiceImpl.downloadFile(Url,dir.getPath());



				fileReaderAndConverter(file, dir);

				messageObject.setMessage(" Your files are downloaded to "+dir.getPath().toString()+" <br> And Converted to text format");
				enterLinkView.addObject("message",messageObject );
				enterLinkView.setViewName("display");

				return enterLinkView;


			}



			// folder execution starts from here
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

			Gson gson = new Gson();


			if(responseAndMessage.getMessage()!=null && responseAndMessage.getMessage().equalsIgnoreCase("error")){
				Error error = gson.fromJson(responseFromAdaptor, Error.class);


				messageObject.setMessage(error.getError().getCode());




				ModelAndView errorView = new ModelAndView();
				errorView.addObject("message", messageObject);
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
			messageObject.setMessage(" Your files are downloaded to "+dir.getPath().toString()+" <br>  And Converted to text format");
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
		logger.info("no of files read to convert into text format   "+ filesInFolder.size());

		logger.info("files read from the directory "+filesInFolder);
		ExecutorService converterExecutor = Executors.newFixedThreadPool(1);

		for(File officefile:filesInFolder){


			//			//parallel conversion of all files 
			//	converterExecutor.execute(converter);
			Runnable converter = new ParallelConverter(officefile, file);
			converterExecutor.execute(converter);

		}
		converterExecutor.shutdown();
		try {
			converterExecutor.awaitTermination(180,TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			Error error = gson.fromJson(responseFromAdaptor1, Error.class);


			messageObject.setMessage(error.getError().getCode());



			ModelAndView errorView = new ModelAndView();
			errorView.addObject("message", messageObject);
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

			try {
				executor1.awaitTermination(900, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

			Gson gson = new Gson();



			if(responseAndMessage.getMessage()!=null && responseAndMessage.getMessage().equalsIgnoreCase("error")){

				ModelAndView errorView = new ModelAndView();

				Error error = gson.fromJson(responseFromAdaptor, Error.class);



				messageObject.setMessage(error.getError().getCode());

				errorView.addObject("message", messageObject);
				errorView.setViewName("display");
				return errorView;
			}

			OuterMetaData outerMetaData =gson.fromJson(responseFromAdaptor, OuterMetaData.class);

			HashMap<String, User> namesOfAllSharingUsers = new HashMap<String, User>();

			Set<String> displayNames = new HashSet<String>();

			HashMap<String, String> namesAndDriveId = new HashMap<String, String>();

			for (MetaDataForFolder metaDataForFolder:outerMetaData.getValue()){

				String driveId =metaDataForFolder.getRemoteItem().getParentReference().getDriveId();				



				namesOfAllSharingUsers=	metaDataForFolder.getCreatedBy();
				if(namesOfAllSharingUsers.isEmpty()){
					namesOfAllSharingUsers = metaDataForFolder.getLastModifiedBy();
				}
				//String driveId=	"b!xTDMGJt6IEiuUTWPKWl2DIgyJcgGyIxOnPrOum8TeyfKUQRBWwV8TofsOMwgqCI2";
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

			String fileUrl=tokenAndPath.getPath();

			String commonUrl ="https://graph.microsoft.com/beta";

			if(fileUrl.contains("?")){
				int indexOfQueryParam = fileUrl.indexOf("?");

				fileUrl= fileUrl.substring(0, indexOfQueryParam);
			}

			if((fileUrl.endsWith(".pdf")) || (fileUrl.endsWith(".PDF")) || (fileUrl.endsWith(".DOCS"))|| (fileUrl.endsWith(".docs")) 
					|| (fileUrl.endsWith(".DOCX"))|| (fileUrl.endsWith(".docx")) || (fileUrl.endsWith(".pptx")) || (fileUrl.endsWith(".PPTX")) || 
					(fileUrl.endsWith(".PPT")) || (fileUrl.endsWith(".ppt"))
					|| (fileUrl.endsWith(".pdf")) || (fileUrl.endsWith(".xlsx")) || (fileUrl.endsWith(".XLSX")) || (fileUrl.endsWith(".xls")) ||
					(fileUrl.endsWith(".XLS"))){





				String base_path = tokenAndPath.getPath();//replaceAll("%20", " ");

				// gets the start index after the documents path
				int indexAfterDocuments =base_path.lastIndexOf("Documents")+10;


				String file = base_path.substring(indexAfterDocuments);

				String oneDriveFileUrl =commonUrl+"/drives/"+driveId+"/root:/"+file;

				int local_directory =file.lastIndexOf("/")+1;

				String local_folder = file.substring(local_directory);

				String MakeLocalDirectory =local_folder.replace("%20", " ");


				int indexToRemoveExntension = MakeLocalDirectory.lastIndexOf(".");

				String extensionLessDirectory = MakeLocalDirectory.substring(0, indexToRemoveExntension);


				File dir = new File(saveDir+"\\Downloads\\"+extensionLessDirectory);

				dir.mkdirs();

				SuccessMessageObject responseAndMessage= UserServiceImpl.doGet(oneDriveFileUrl, tokenheader);

				String responseFromAdaptor= responseAndMessage.getResponse();

				System.out.println("responseFromAdaptor   "+responseFromAdaptor);

				Gson gson = new Gson();



				if(responseAndMessage.getMessage()!=null && responseAndMessage.getMessage().equalsIgnoreCase("error")){

					ModelAndView errorView = new ModelAndView();

					Error error = gson.fromJson(responseFromAdaptor, Error.class);


					messageObject.setMessage(error.getError().getCode());

					errorView.addObject("message", messageObject);

					errorView.setViewName("display");

					return errorView;
				}

				MetaDataForFolder outerMetaData =gson.fromJson(responseFromAdaptor, MetaDataForFolder.class);





				String Url = outerMetaData.getMicrosoft_graph_downloadUrl();





				UserServiceImpl.downloadFile(Url,dir.getPath());



				fileReaderAndConverter(file, dir);

				messageObject.setMessage(" Your files are downloaded to "+dir.getPath().toString()+" <br> And Converted to text format");
				enterLinkView.addObject("message",messageObject );
				enterLinkView.setViewName("display");

				return enterLinkView;

			}



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

				ModelAndView errorView = new ModelAndView();

				Error error = gson.fromJson(responseFromAdaptor, Error.class);

				messageObject.setMessage(error.getError().getCode());

				errorView.addObject("message", messageObject);
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

			messageObject.setMessage(" Your files are downloaded to "+dir.getPath().toString()+" <br> And Converted to text format");
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

	/* upload documents upto 4mb 
	 * (non-Javadoc)
	 * @see com.onedrive.UserService#uploadDocumentsToOneDrive(com.onedrive.TokenAndPath, java.io.InputStream, java.lang.String)
	 */


	@Override
	public ModelAndView uploadDocumentsToOneDrive(TokenAndPath tokenAndPath,InputStream fileInputStream, String nameOfFile) throws ClientProtocolException, IOException, MessagingException {




		ModelAndView uploadFileView = new ModelAndView();

		String access_token= tokenAndPath.getToken();





		String driveId = tokenAndPath.getDriveId();



		String commonUrl ="https://graph.microsoft.com/v1.0/drives/";

		String base_path = tokenAndPath.getPath();//replaceAll("%20", " ");

		// gets the start index after the documents path
		int indexAfterDocuments =base_path.lastIndexOf("Documents")+10;


		String folderPathAfterdocuments = base_path.substring(indexAfterDocuments);







		String contentStringAppender =":/content";

		String nameOfFileFormatted = nameOfFile.replace(" ", "%20");
		String completeurl= commonUrl+driveId+"/root:/"+folderPathAfterdocuments+"/"+nameOfFileFormatted+contentStringAppender;



		//	 String completeurl ="https://graph.microsoft.com/beta/me/drive/root:/"+file.getName()+":/content";
		try 

		{


			byte [] bytearray = new byte [tokenAndPath.getFileSize()];

			BufferedInputStream bin = new BufferedInputStream(fileInputStream); 
			bin.read(bytearray,0,bytearray.length);





			URL uploadURL = new URL( completeurl.toString() );

			HttpURLConnection uploadConn = (HttpURLConnection)uploadURL.openConnection();


			uploadConn.setRequestMethod( "PUT" );
			uploadConn.setUseCaches(false);
			uploadConn.setDoOutput( true );         

			uploadConn.setRequestProperty("Authorization", "Bearer " + access_token);  // this is right
			uploadConn.setRequestProperty("Content-Type", "application/octet-stream");

			uploadConn.setChunkedStreamingMode( 0 );

			try (OutputStream os = uploadConn.getOutputStream()) {
				os.write(bytearray,0,bytearray.length);
				os.flush();


			}


			uploadConn.disconnect();

		}

		catch(Exception ex){
			SuccessMessageObject messageObject= new SuccessMessageObject();
			messageObject.setMessage("Error occured  Reason: "+ex.getMessage());
			uploadFileView.addObject("message",messageObject );
			uploadFileView.setViewName("display");
		}

		SuccessMessageObject messageObject= new SuccessMessageObject();
		messageObject.setMessage("successfully uploaded "+nameOfFile+ " to users shared drive");
		uploadFileView.addObject("message",messageObject );
		uploadFileView.setViewName("display");



		return uploadFileView;
	}

	@Override
	public ModelAndView uploadLargeDocumentsToOneDrive(TokenAndPath tokenAndPath, InputStream fileInputStream,
			String nameOfFile) throws ClientProtocolException, IOException, MessagingException {



		ModelAndView uploadFileView = new ModelAndView();

		String access_token= tokenAndPath.getToken();





		String driveId = tokenAndPath.getDriveId();



		String commonUrl ="https://graph.microsoft.com/v1.0/drives/";

		String base_path = tokenAndPath.getPath();//replaceAll("%20", " ");

		// gets the start index after the documents path
		int indexAfterDocuments =base_path.lastIndexOf("Documents")+10;


		String folderPathAfterdocuments = base_path.substring(indexAfterDocuments);







		String contentStringAppender =":/content";

		String nameOfFileFormatted = nameOfFile.replace(" ", "%20");
		String completeurl= commonUrl+driveId+"/root:/"+folderPathAfterdocuments+"/"+nameOfFileFormatted+contentStringAppender;


		try 

		{

			String sessionCreateUrl = commonUrl+driveId+"/root:/"+folderPathAfterdocuments+"/"+nameOfFileFormatted+":/createUploadSession";




			DefaultHttpClient httpClient = new DefaultHttpClient();
			final HttpPost httpRequest = new HttpPost( sessionCreateUrl );


			Item item = new Item();
			item.setMicrosoft_graph_conflictBehavior("replace");
			UploadSessionBody body= new UploadSessionBody();

			body.setItem(item);

			Gson  gson= new Gson();

			String jsonBody= 	gson.toJson(body);

			httpRequest.addHeader("Content-Type", "application/json");
			httpRequest.addHeader("Authorization", "Bearer " + access_token);

			logger.info(httpRequest.getMethod());


			final StringEntity input = new StringEntity(jsonBody );

			HttpResponse response = httpClient.execute(httpRequest);


			final org.apache.http.HttpEntity entity = (org.apache.http.HttpEntity) response.getEntity();

			final String responseString = EntityUtils.toString( (org.apache.http.HttpEntity) entity, "UTF-8" );

			EntityUtils.consume( entity );

			logger.info(httpRequest.toString());

			System.out.println(responseString);

			httpClient.getConnectionManager().shutdown();



			UploadSessionCreateResponse uploadSessionCreateResponse =gson.fromJson(responseString, UploadSessionCreateResponse.class);

			String uploadUrl=uploadSessionCreateResponse.getUploadUrl();


			URL uploadURL = new URL( uploadUrl.toString() );



			int size = 2*1024*1024;
			byte buffer[] = new byte[size];

			int totalSizeOfStream= fileInputStream.available();
			int startRange=0;
			int count = 0;
			int endRange= size-1;
			HttpURLConnection uploadConn = (HttpURLConnection)uploadURL.openConnection();
			uploadConn.setDoOutput( true );
			uploadConn.setRequestMethod( "PUT" );
			uploadConn.setUseCaches(false);
			OutputStream os= null;
			while (true) {
				int i = fileInputStream.read(buffer, 0, size);
				if (i == -1)
					break;






				uploadConn.setRequestProperty("Authorization", "Bearer " + access_token);  // this is right
				uploadConn.setRequestProperty("Content-Type", "application/octet-stream");

				if(i<size){
					endRange=startRange+i-1;
				}
				String ContentRangeHeader= "bytes "+startRange+"-"+endRange+"/"+totalSizeOfStream;


				uploadConn.setRequestProperty("Content-Range", ContentRangeHeader);



				os = uploadConn.getOutputStream();

				os.write(buffer, 0, i);
				os.close();  
				startRange=endRange+1;
				endRange= endRange+size;
				++count;

			}
			fileInputStream.close();
			os.flush();
			os.close();

			uploadConn.disconnect();
		}

		catch(Exception ex){
			SuccessMessageObject messageObject= new SuccessMessageObject();
			messageObject.setMessage("Error occured  Reason: "+ex.getMessage());
			uploadFileView.addObject("message",messageObject );
			uploadFileView.setViewName("display");
			return uploadFileView;
		} 

		SuccessMessageObject messageObject= new SuccessMessageObject();
		messageObject.setMessage("successfully uploaded "+nameOfFile+ " to users shared drive");
		uploadFileView.addObject("message",messageObject );
		uploadFileView.setViewName("display");



		return uploadFileView;
	}

	@Override
	public ModelAndView uploadLargeDocumentsToOneDriveSDK(TokenAndPath tokenAndPath, InputStream fileInputStream,
			String nameOfFile) throws ClientProtocolException, IOException, MessagingException {

		ModelAndView uploadFileView = new ModelAndView();

		String access_token= tokenAndPath.getToken();



		byte[] bytes;

		String driveId = tokenAndPath.getDriveId();



		String commonUrl ="https://graph.microsoft.com/v1.0/drives/";

		String base_path = tokenAndPath.getPath();//replaceAll("%20", " ");

		// gets the start index after the documents path
		int indexAfterDocuments =base_path.lastIndexOf("Documents")+10;


		String folderPathAfterdocuments = base_path.substring(indexAfterDocuments);







		String contentStringAppender =":/content";

		String nameOfFileFormatted = nameOfFile.replace(" ", "%20");
		String completeurl= commonUrl+driveId+"/root:/"+folderPathAfterdocuments+"/"+nameOfFileFormatted+contentStringAppender;


		try 

		{

			String sessionCreateUrl =
					commonUrl+driveId+"/root:/"+folderPathAfterdocuments+"/"+nameOfFileFormatted+":/createUploadSession"+"?@name.conflictBehavior=replace";




			DefaultHttpClient httpClient = new DefaultHttpClient();
			final HttpPost httpRequest = new HttpPost( sessionCreateUrl );


			Item item = new Item();
			item.setMicrosoft_graph_conflictBehavior("replace");
			UploadSessionBody uploadBody= new UploadSessionBody();

			uploadBody.setItem(item);

			Gson  gson= new Gson();

			String jsonBody= 	gson.toJson(uploadBody);
			
			logger.info(jsonBody);

			httpRequest.addHeader("Content-Type", "application/json");
			httpRequest.addHeader("Authorization", "Bearer " + access_token);

			logger.info(httpRequest.getMethod());


			final StringEntity input = new StringEntity(jsonBody );
			
			
			httpRequest.setEntity(input);

			HttpResponse concreteOneResponse = httpClient.execute(httpRequest);




			final org.apache.http.HttpEntity entity = (org.apache.http.HttpEntity) concreteOneResponse.getEntity();

			final String responseString = EntityUtils.toString( (org.apache.http.HttpEntity) entity, "UTF-8" );

			EntityUtils.consume( entity );

			logger.info(httpRequest.toString());

			System.out.println(responseString);
			
			logger.info(responseString);

			httpClient.getConnectionManager().shutdown();



			UploadSessionCreateResponse uploadSessionCreateResponse =gson.fromJson(responseString, UploadSessionCreateResponse.class);

			String uploadUrl=uploadSessionCreateResponse.getUploadUrl();


			URL uploadURL = new URL( uploadUrl.toString() );

			//		("C:/Users/sai.kiran.akkireddy/Downloads/testDownload/largepdf.pdf");Sample large docx

			//C:/Users/sai.kiran.akkireddy/Downloads/testDownload/oversize_pdf_test_0.pdf

			File fileToUpload =new File("C:/Users/sai.kiran.akkireddy/Downloads/testDownload/oversize_pdf_test_0.pdf");

			



            randFile = new RandomAccessFile(fileToUpload, "r");

			while (!canceled && !finished){


				long currFirstByte = randFile.getFilePointer();
                
				logger.info("currFirstByte   "+currFirstByte);

				PreparedRequest uploadChunk = new PreparedRequest(uploadUrl, PreparedRequestMethod.PUT);
				if (currFirstByte + chunkSize < randFile.length()) {
					bytes = new byte[chunkSize];
				}
				else {
					// optimistic cast, assuming the last bit of the file is
					// never bigger than MAXINT
					bytes = new byte[(int) (randFile.length() - randFile.getFilePointer())];
				}

				long start = randFile.getFilePointer();
				randFile.readFully(bytes);
				uploadChunk.setBody(bytes);
				uploadChunk.addHeader("Content-Length", (randFile.getFilePointer() - start) + "");
				uploadChunk.addHeader(
						"Content-Range",
						String.format("bytes %s-%s/%s", start, randFile.getFilePointer() - 1, randFile.length()));
				long end_range=  randFile.getFilePointer()- 1;
				logger.info("Uploading chunk {} - {}"+start+"-"+end_range);
				String url ;
				RequestBody body = null;

				if (uploadChunk.getBody() != null) {
					body = RequestBody.create(null, uploadChunk.getBody());
				}

				if (isCompleteURL(uploadChunk.getPath())) {
					url = uploadChunk.getPath();
				} else {
					url = String.format("%s%s?access_token=%s", this.baseUrl, uploadChunk.getPath(),access_token);
				}

				logger.debug(String.format("making request to %s",url));

				Request.Builder builder = new Request.Builder().method(uploadChunk.getMethod(), body).url(url);

				for (String key : uploadChunk.getHeader().keySet()) {
					logger.info("key  "+key+"--"+uploadChunk.getHeader().get(key) );
					builder.addHeader(key, uploadChunk.getHeader().get(key));
				}

				//Add auth permanently to header with redirection
				
			
				builder.header("Authorization", "bearer " +access_token );
				Request request = builder.build();
				OkHttpClient client = new OkHttpClient();
				client.setConnectTimeout(60, TimeUnit.SECONDS); // connect timeout
				client.setReadTimeout(60, TimeUnit.SECONDS);
				Response response=    client.newCall(request).execute();

				ConcreteOneResponse concreteOneResponse1=   new ConcreteOneResponse(response);
				if (concreteOneResponse1.wasSuccess()) {
					if (concreteOneResponse1.getStatusCode()==200 || concreteOneResponse1.getStatusCode()==201) { 
						// if last chunk upload was successful end the
						finished = true;
						String resp =concreteOneResponse1.getBodyAsString();
						logger.info(resp);
						
						System.out.println(resp);
						SuccessMessageObject messageObject= new SuccessMessageObject();
						messageObject.setMessage("successfully uploaded "+nameOfFile+ " to users shared drive");
						uploadFileView.addObject("message",messageObject );
						uploadFileView.setViewName("display");
					}else {
						//just continue
						String resp =concreteOneResponse1.getBodyAsString();
						logger.info(resp);
						System.out.println(resp);
						uploadSession= gson.fromJson(concreteOneResponse1.getBodyAsString(),
								UploadSession.class);

						
						
						
						
					logger.info("uploadSession.getNextExpectedRanges()   "+Arrays.toString(uploadSession.getNextExpectedRanges()));
						
						long nextRanges= uploadSession.getNextRange();
						randFile.seek(((com.onedrive.UploadSession) uploadSession).getNextRange());
					}
				}}}
		catch (Exception e) {

			SuccessMessageObject messageObject= new SuccessMessageObject();
			messageObject.setMessage("Error occured  Reason: "+e.getMessage());
			uploadFileView.addObject("message",messageObject );
			uploadFileView.setViewName("display");
			System.out.println(e.getMessage());
			logger.error(e.getMessage());
			return uploadFileView;

			// TODO: handle exception
		}
		return uploadFileView;
	}

	private boolean isCompleteURL(String url) {
		try {
			URL u = new URL(url); // this would check for the protocol
			u.toURI();// does the extra checking required for validation of URI
		} catch (URISyntaxException | MalformedURLException e) {
			// if exception then no url
			return false;
		}
		return true;
	}

	@Override
	public ModelAndView uploadLargeDocumentsToOneDriveSDKByInputStream(TokenAndPath tokenAndPath,
			InputStream fileInputStream, String nameOfFile)
			throws ClientProtocolException, IOException, MessagingException {
		
		
		


		ModelAndView uploadFileView = new ModelAndView();

		String access_token= tokenAndPath.getToken();



		byte[] bytes;

		String driveId = tokenAndPath.getDriveId();



		String commonUrl ="https://graph.microsoft.com/v1.0/drives/";

		String base_path = tokenAndPath.getPath();//replaceAll("%20", " ");

		// gets the start index after the documents path
		int indexAfterDocuments =base_path.lastIndexOf("Documents")+10;


		String folderPathAfterdocuments = base_path.substring(indexAfterDocuments);







		String contentStringAppender =":/content";

		String nameOfFileFormatted = nameOfFile.replace(" ", "%20");
		String completeurl= commonUrl+driveId+"/root:/"+folderPathAfterdocuments+"/"+nameOfFileFormatted+contentStringAppender;


		try 

		{

			String sessionCreateUrl = commonUrl+driveId+"/root:/"+folderPathAfterdocuments+"/"+nameOfFileFormatted+":/createUploadSession"+"?@name.conflictBehavior=replace";



			DefaultHttpClient httpClient = new DefaultHttpClient();
			final HttpPost httpRequest = new HttpPost( sessionCreateUrl );


			Item item = new Item();
			item.setMicrosoft_graph_conflictBehavior("replace");
			UploadSessionBody uploadBody= new UploadSessionBody();

			uploadBody.setItem(item);

			Gson  gson= new Gson();

			String jsonBody= 	gson.toJson(uploadBody);

			httpRequest.addHeader("Content-Type", "application/json");
			httpRequest.addHeader("Authorization", "Bearer " + access_token);

			logger.info(httpRequest.getMethod());


			final StringEntity input = new StringEntity(jsonBody );

			httpRequest.setEntity(input);

			HttpResponse concreteOneResponse = httpClient.execute(httpRequest);




			final org.apache.http.HttpEntity entity = (org.apache.http.HttpEntity) concreteOneResponse.getEntity();

			final String responseString = EntityUtils.toString( (org.apache.http.HttpEntity) entity, "UTF-8" );

			EntityUtils.consume( entity );

			logger.info(httpRequest.toString());

			System.out.println(responseString);
			
			logger.info(responseString);

			httpClient.getConnectionManager().shutdown();



			UploadSessionCreateResponse uploadSessionCreateResponse =gson.fromJson(responseString, UploadSessionCreateResponse.class);

			String uploadUrl=uploadSessionCreateResponse.getUploadUrl();


			URL uploadURL = new URL( uploadUrl.toString() );

			//		("C:/Users/sai.kiran.akkireddy/Downloads/testDownload/largepdf.pdf");Sample large docx

			//C:/Users/sai.kiran.akkireddy/Downloads/testDownload/oversize_pdf_test_0.pdf

		//	File fileToUpload =new File("C:/Users/sai.kiran.akkireddy/Downloads/testDownload/large_docx.docx");

			



    //        randFile = new RandomAccessFile(fileToUpload, "r");
            
            int numToCalculateEndrange=1;
            int endRange=0;
            int content_length=0;
            long nextRanges=0L;
            
            long fileInputStreamIntilialSize= fileInputStream.available();
			while (!canceled && !finished){


								
				long currFirstByteStream = 0L;


				PreparedRequest uploadChunk = new PreparedRequest(uploadUrl, PreparedRequestMethod.PUT);
//				if (currFirstByte + chunkSize < randFile.length()) {
//					bytes = new byte[chunkSize];
//					
//				}
				if (nextRanges + chunkSize < fileInputStream.available()) {
					bytes = new byte[chunkSize];
					content_length=chunkSize;
					endRange= numToCalculateEndrange*chunkSize-1;
					numToCalculateEndrange++;
				}
//				else {
//					// optimistic cast, assuming the last bit of the file is
//					// never bigger than MAXINT
//					bytes = new byte[(int) (randFile.length() - randFile.getFilePointer())];
//				}
				
				else {
					// optimistic cast, assuming the last bit of the file is
					// never bigger than MAXINT
					bytes = new byte[(int) (fileInputStream.available())];
					content_length= (int)fileInputStream.available();
					endRange=(numToCalculateEndrange-1)*chunkSize+content_length;
				}

		//		long start = randFile.getFilePointer();
			//	randFile.readFully(bytes);
				fileInputStream.read(bytes);
				uploadChunk.setBody(bytes);
				
				String Content_length = Integer.toString(content_length);
				uploadChunk.addHeader("Content-Length", (content_length) + "");
				
		//		uploadChunk.addHeader("Content-Length", (randFile.getFilePointer() - start) + "");
				uploadChunk.addHeader(
						"Content-Range",
						String.format("bytes %s-%s/%s", nextRanges, endRange, fileInputStreamIntilialSize));
				
				logger.info("Uploading chunk {} - {}"+nextRanges+"-"+endRange);
				String url ;
				RequestBody body = null;

				if (uploadChunk.getBody() != null) {
					body = RequestBody.create(null, uploadChunk.getBody());
				}

				if (isCompleteURL(uploadChunk.getPath())) {
					url = uploadChunk.getPath();
				} else {
					url = String.format("%s%s?access_token=%s", this.baseUrl, uploadChunk.getPath(),access_token);
				}

				logger.debug(String.format("making request to %s",url));

				Request.Builder builder = new Request.Builder().method(uploadChunk.getMethod(), body).url(url);

				for (String key : uploadChunk.getHeader().keySet()) {
					builder.addHeader(key, uploadChunk.getHeader().get(key));
				}

				//Add auth permanently to header with redirection
				builder.header("Authorization", "bearer " +access_token );
				Request request = builder.build();
				OkHttpClient client = new OkHttpClient();
				Response response=    client.newCall(request).execute();

				ConcreteOneResponse concreteOneResponse1=   new ConcreteOneResponse(response);
				if (concreteOneResponse1.wasSuccess()) {
					if (concreteOneResponse1.getStatusCode()==200 || concreteOneResponse1.getStatusCode()==201) { 
						// if last chunk upload was successful end the
						finished = true;
						String resp =concreteOneResponse1.getBodyAsString();
						SuccessMessageObject messageObject= new SuccessMessageObject();
						messageObject.setMessage("successfully uploaded "+nameOfFile+ " to users shared drive");
						uploadFileView.addObject("message",messageObject );
						uploadFileView.setViewName("display");
					}else {
						//just continue
						uploadSession= gson.fromJson(concreteOneResponse1.getBodyAsString(),
								UploadSession.class);

						 nextRanges= uploadSession.getNextRange();
//						randFile.seek(((com.onedrive.UploadSession) uploadSession).getNextRange());
					}
				}}}
		catch (Exception e) {

			SuccessMessageObject messageObject= new SuccessMessageObject();
			messageObject.setMessage("Error occured  Reason: "+e.getMessage());
			uploadFileView.addObject("message",messageObject );
			uploadFileView.setViewName("display");
			return uploadFileView;

			// TODO: handle exception
		}
		return uploadFileView;
	
		// TODO Auto-generated method stub
		
	}


	/**
	 * Adds the label to a office document
	 *
	 * @param label and the file or file path
	 * @return the status success and model name
	 * @throws AppException  the app exception
	 * @throws TechException the tech exception
	 */


}