package com.onedrive;

import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.hpsf.HPSFPropertiesOnlyDocument;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.xml.xmp.XmpWriter;
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

	private String LAST_USED_FOLDER = home;

	// changed to public to run on server

	@Value("${download.directory.complete}")
	private String saveDir;// = "C://Users//Public";

	private RandomAccessFile randFile;

	@Override
	public String authorizeAndGetUserToken() throws URISyntaxException {

		String url = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id=c00a4c26-e64b-459b-91f6-31571b802ae4&scope=files.read.all&response_type=token&redirect_uri=http://localhost:8080/onedrive/redirect";
		String os = System.getProperty("os.name").toLowerCase();

		try {

			DefaultHttpClient httpClient = new DefaultHttpClient();
			final HttpGet httpRequest = new HttpGet(url);

			logger.info(httpRequest);

			HttpResponse response = httpClient.execute(httpRequest);

			Runtime rt = Runtime.getRuntime();
			if (os.indexOf("win") >= 0) {

				rt.exec("rundll32 url.dll,FileProtocolHandler " + url);

			} else if (os.indexOf("mac") >= 0) {

				rt.exec("open" + url);
			}

			else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
				String[] browsers = { "epiphany", "firefox", "mozilla", "konqueror", "netscape", "opera", "links",
						"lynx" };

				StringBuffer cmd = new StringBuffer();
				for (int i = 0; i < browsers.length; i++)
					cmd.append((i == 0 ? "" : " || ") + browsers[i] + " \"" + url + "\" ");

				rt.exec(new String[] { "sh", "-c", cmd.toString() });

			}
		} catch (Exception ex) {
			logger.error(" error occured" + ex.getMessage());
			ex.printStackTrace();
		}

		return null;
	}

	@Override
	public ModelAndView personalItemsDownloadAndConvert(TokenAndPath tokenAndPath) throws IOException,
			IllegalStateException, JsonSyntaxException, InterruptedException, NumberFormatException {

		ModelAndView enterLinkView = new ModelAndView();

		try {

			SuccessMessageObject messageObject = new SuccessMessageObject();

			String access_token = tokenAndPath.getToken();

			String tokenheader = "Bearer" + " " + access_token;

			String fileUrl = tokenAndPath.getPath();

			if (fileUrl.contains("?")) {
				int indexOfQueryParam = fileUrl.indexOf("?");

				fileUrl = fileUrl.substring(0, indexOfQueryParam);
			}

			// path for the single file
			if ((fileUrl.endsWith(".pdf")) || (fileUrl.endsWith(".PDF")) || (fileUrl.endsWith(".DOCS"))
					|| (fileUrl.endsWith(".docs")) || (fileUrl.endsWith(".DOCX")) || (fileUrl.endsWith(".docx"))
					|| (fileUrl.endsWith(".pptx")) || (fileUrl.endsWith(".PPTX")) || (fileUrl.endsWith(".PPT"))
					|| (fileUrl.endsWith(".ppt")) || (fileUrl.endsWith(".pdf")) || (fileUrl.endsWith(".xlsx"))
					|| (fileUrl.endsWith(".XLSX")) || (fileUrl.endsWith(".xls")) || (fileUrl.endsWith(".XLS"))) {

				String base_path = tokenAndPath.getPath();// replaceAll("%20", "
															// ");

				// gets the start index after the documents path
				int indexAfterDocuments = base_path.lastIndexOf("Documents") + 10;

				String file = base_path.substring(indexAfterDocuments);

				String oneDriveFileUrl = "https://graph.microsoft.com/beta/me/drive/root:/" + file;

				int local_directory = file.lastIndexOf("/") + 1;

				String local_folder = file.substring(local_directory);

				String MakeLocalDirectory = local_folder.replace("%20", " ");

				int indexToRemoveExntension = MakeLocalDirectory.lastIndexOf(".");

				String extensionLessDirectory = MakeLocalDirectory.substring(0, indexToRemoveExntension);

				File dir = new File(saveDir + "/" + extensionLessDirectory);

				dir.mkdirs();

				SuccessMessageObject responseAndMessage = UserServiceImpl.doGet(oneDriveFileUrl, tokenheader);

				String responseFromAdaptor = responseAndMessage.getResponse();

				System.out.println("responseFromAdaptor   " + responseFromAdaptor);

				Gson gson = new Gson();

				if (responseAndMessage.getMessage() != null
						&& responseAndMessage.getMessage().equalsIgnoreCase("error")) {

					ModelAndView errorView = new ModelAndView();

					Error error = gson.fromJson(responseFromAdaptor, Error.class);

					messageObject.setMessage(error.getError().getCode());

					errorView.addObject("message", messageObject);

					errorView.setViewName("display");

					return errorView;
				}

				MetaDataForFolder outerMetaData = gson.fromJson(responseFromAdaptor, MetaDataForFolder.class);

				String Url = outerMetaData.getMicrosoft_graph_downloadUrl();

				String driveIdMetaData = outerMetaData.getParentReference().getDriveId();

				String pathMetaData = outerMetaData.getParentReference().getPath();

				UserServiceImpl.downloadFile(Url, dir.getPath(), driveIdMetaData, pathMetaData);

				// fileReaderAndConverter(file, dir);

				messageObject.setMessage(" Your files are downloaded to " + dir.getPath().toString());
				enterLinkView.addObject("message", messageObject);
				enterLinkView.setViewName("display");

				return enterLinkView;

			}

			// folder execution starts from here
			enterLinkView.setViewName("display");

			String commonUrl = "https://graph.microsoft.com/beta/me/";

			String base_path = tokenAndPath.getPath();// replaceAll("%20", " ");

			// gets the start index after the documents path, Assuming every
			// shared url will be having documents in it.
			int indexAfterDocuments = base_path.lastIndexOf("Documents") + 10;

			String file = base_path.substring(indexAfterDocuments);

			int local_directory = file.lastIndexOf("/") + 1;
			String local_folder = file.substring(local_directory);

			String child = ":/children";

			String MakeLocalDirectory = local_folder.replace("%20", " ");

			String completeurl = commonUrl + "drive/root:/" + file + child;

			// making a directory
			File dir = new File(saveDir + "/" + MakeLocalDirectory);
			dir.mkdirs();

			// make a get call to one drive api

			SuccessMessageObject responseAndMessage = UserServiceImpl.doGet(completeurl, tokenheader);

			String responseFromAdaptor = responseAndMessage.getResponse();

			Gson gson = new Gson();

			if (responseAndMessage.getMessage() != null && responseAndMessage.getMessage().equalsIgnoreCase("error")) {
				Error error = gson.fromJson(responseFromAdaptor, Error.class);

				messageObject.setMessage(error.getError().getCode());

				ModelAndView errorView = new ModelAndView();
				errorView.addObject("message", messageObject);
				errorView.setViewName("display");
				return errorView;
			}

			OuterMetaData outerMetaData = gson.fromJson(responseFromAdaptor, OuterMetaData.class);

			List<ParentReference> listwithMetaData = new ArrayList<ParentReference>();

			List<String> downloadUrls = new ArrayList<String>();
			for (MetaDataForFolder metaDataForFolder : outerMetaData.getValue()) {

				if (metaDataForFolder.getFolder() != null
						&& (Integer.parseInt(metaDataForFolder.getFolder().getChildCount()) >= 1)) {
					readingInnerFolders(tokenheader, commonUrl, base_path, child, file, dir, gson, metaDataForFolder);
				} else {

					ParentReference parentReference = new ParentReference();

					String Url = metaDataForFolder.getMicrosoft_graph_downloadUrl();
					String driveID = metaDataForFolder.getParentReference().getDriveId();

					String path = metaDataForFolder.getParentReference().getPath();
					parentReference.setId(Url);
					parentReference.setDriveId(driveID);
					parentReference.setPath(path);
					listwithMetaData.add(parentReference);
					downloadUrls.add(Url);
				}
			}

			System.out.println(downloadUrls);

			// create the size of the thread pool dynamically
			if (!listwithMetaData.isEmpty()) {
				ExecutorService executor = Executors.newFixedThreadPool(listwithMetaData.size());
				final long startTime = System.currentTimeMillis();
				for (ParentReference parentReference : listwithMetaData) {

					System.out.println("saveDir------>" + saveDir);
					// multithreading framework for downloading files
					Runnable download = new MultiDownLoadExecutor(parentReference, dir.getPath());
					executor.execute(download);
				}
				executor.shutdown();

				final long endTime = System.currentTimeMillis();
				System.out.println("Time taken to get Response in millis:" + (endTime - startTime));

				// concurrentConverter(file, dir, executor);
			} else {

				// for empty urls just convert the files
				// fileReaderAndConverter(file, dir);
			}
			messageObject.setMessage(" Your files are downloaded to " + dir.getPath().toString());
			enterLinkView.addObject("message", messageObject);

			logger.info(enterLinkView);

		} catch (Exception universalException) {
			SuccessMessageObject messageObject = new SuccessMessageObject();
			messageObject.setMessage(universalException.getMessage());
			enterLinkView.addObject("message", messageObject);
			logger.info("error occured" + universalException.getMessage());
			return enterLinkView;

		}
		return enterLinkView;

	}

	private void concurrentConverter(String file, File dir, ExecutorService executor)
			throws InterruptedException, IOException {
		if ((executor.awaitTermination(900, TimeUnit.SECONDS))) {
			fileReaderAndConverter(file, dir);
		}
	}

	private void fileReaderAndConverter(String file, File dir) throws IOException {
		List<File> filesInFolder = Files.walk(Paths.get(dir.getPath())).filter(Files::isRegularFile).map(Path::toFile)
				.collect(Collectors.toList());
		logger.info("no of files read to convert into text format   " + filesInFolder.size());

		logger.info("files read from the directory " + filesInFolder);
		ExecutorService converterExecutor = Executors.newFixedThreadPool(1);

		for (File officefile : filesInFolder) {

			// //parallel conversion of all files
			// converterExecutor.execute(converter);
			Runnable converter = new ParallelConverter(officefile, file);
			converterExecutor.execute(converter);

		}
		converterExecutor.shutdown();
		try {
			converterExecutor.awaitTermination(180, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void readingInnerFolders(String tokenheader, String commonUrl, String base_path, String child, String file,
			File dir, final Gson gson, MetaDataForFolder metaDataForFolder)
			throws ClientProtocolException, IOException, InterruptedException {

		int indexAfterDocuments = base_path.lastIndexOf("Documents") + 10;

		String file1 = base_path.substring(indexAfterDocuments);

		String path = metaDataForFolder.getParentReference().getPath();

		// get the name of inside folder
		String insideFoldername = metaDataForFolder.getName();

		// form the url to get the children files for inside folder

		String OneDriveinsideFolderUrl = commonUrl + path + "/" + insideFoldername + child;

		// make a local directory with the folder structure

		String localinsideFolderName = insideFoldername.replaceAll("%20", " ");

		File innerdir1 = new File(dir.getPath() + "\\" + localinsideFolderName);

		innerdir1.mkdirs();

		// make a call
		SuccessMessageObject messageObject = UserServiceImpl.doGet(OneDriveinsideFolderUrl, tokenheader);

		String responseFromAdaptor1 = messageObject.getResponse();

		if (messageObject.getMessage() != null && messageObject.getMessage().equalsIgnoreCase("error")) {
			Error error = gson.fromJson(responseFromAdaptor1, Error.class);

			messageObject.setMessage(error.getError().getCode());

			ModelAndView errorView = new ModelAndView();
			errorView.addObject("message", messageObject);
		}

		OuterMetaData outerMetaData1 = gson.fromJson(responseFromAdaptor1, OuterMetaData.class);

		List<String> downloadUrls1 = new ArrayList<String>();

		List<ParentReference> listwithMetaData1 = new ArrayList<ParentReference>();

		for (MetaDataForFolder metaDataForFolder1 : outerMetaData1.getValue()) {
			if (metaDataForFolder1.getFolder() != null
					&& (Integer.parseInt(metaDataForFolder1.getFolder().getChildCount()) >= 1)) {
				InnerFoldersReaderUtility.processAndDownloadSubFolders(tokenheader, commonUrl, base_path, child, file1,
						innerdir1, gson, metaDataForFolder1);
			} else {
				String Url1 = metaDataForFolder1.getMicrosoft_graph_downloadUrl();

				ParentReference parentReference1 = new ParentReference();

				String driveID = metaDataForFolder1.getParentReference().getDriveId();

				String path1 = metaDataForFolder1.getParentReference().getPath();
				parentReference1.setId(Url1);
				parentReference1.setDriveId(driveID);
				parentReference1.setPath(path1);
				listwithMetaData1.add(parentReference1);

				downloadUrls1.add(Url1);
			}
		}
		if (!listwithMetaData1.isEmpty()) {
			ExecutorService executor1 = Executors.newFixedThreadPool(listwithMetaData1.size());
			for (ParentReference downloadUrl1 : listwithMetaData1) {

				// multithreading framework for downloading files
				Runnable download1 = new MultiDownLoadExecutor(downloadUrl1, innerdir1.getPath());
				executor1.execute(download1);
			}
			executor1.shutdown();

			try {
				executor1.awaitTermination(900, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public static void downloadFile(String fileURL, String saveDir, String driveId, String path)
			throws IOException, DocumentException {
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
					fileName = disposition.substring(index + 10, disposition.length() - 1);
				}
			} else {
				// extracts file name from URL
				fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
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

			System.out.println("now adding hidden metadata");

			// adding the path to meta data, to retrieve it while uploading

			File officefile = new File(saveFilePath);

			System.out.println("Working on file " + officefile.getName());
			String name = officefile.getName();

			String labeledFilePath = officefile.getAbsolutePath();

			if (officefile.getName().endsWith(".pdf") || officefile.getName().endsWith(".PDF")) {

				System.out.println("inside pdf");

				FileInputStream fileInputStream = new FileInputStream(officefile);
				PdfReader reader = new PdfReader(fileInputStream);

				PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(labeledFilePath));

				// get and edit meta-data
				HashMap<String, String> info = reader.getInfo();

				info.put("driveId", driveId);
				info.put("path", path);

				// add updated meta-data to pdf
				stamper.setMoreInfo(info);

				// update xmp meta-data
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				XmpWriter xmp = new XmpWriter(baos, info);
				xmp.close();
				stamper.setXmpMetadata(baos.toByteArray());
				stamper.close();
				baos.close();
				fileInputStream.close();
				System.out.println("added label for " + name);
			} else if (officefile.getName().endsWith(".docx") || officefile.getName().endsWith(".DOCX")) {

				System.out.println("inside docx");
				FileInputStream fileInputStream = new FileInputStream(officefile);

				XWPFDocument xWPFDocument = new XWPFDocument(fileInputStream);

				POIXMLProperties propsForDoc = xWPFDocument.getProperties();

				propsForDoc.getCoreProperties().setDescription(driveId);

				propsForDoc.getCustomProperties().addProperty("driveId", driveId);

				propsForDoc.getCustomProperties().addProperty("path", path);

				propsForDoc.commit();

				FileOutputStream fileOutputStreamForLabeledfile = new FileOutputStream(labeledFilePath);
				xWPFDocument.write(fileOutputStreamForLabeledfile);

				fileOutputStreamForLabeledfile.close();
				fileInputStream.close();

				System.out.println("added label for " + name);

			}

			else if (officefile.getName().endsWith(".xlsx") || officefile.getName().endsWith(".XLSX")) {

				System.out.println("inside xlsx");

				FileInputStream fileInputStream = new FileInputStream(officefile);

				XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);

				POIXMLProperties poixmlPropertiesForXlsx = workbook.getProperties();

				poixmlPropertiesForXlsx.getCoreProperties().setDescription(driveId);

				poixmlPropertiesForXlsx.getCustomProperties().addProperty("driveId", driveId);

				poixmlPropertiesForXlsx.getCustomProperties().addProperty("path", path);

				poixmlPropertiesForXlsx.commit();
				FileOutputStream fileOutputStreamForLabeledfile = new FileOutputStream(labeledFilePath);
				workbook.write(fileOutputStreamForLabeledfile);
				fileOutputStreamForLabeledfile.close();
				fileInputStream.close();
				workbook.close();
				System.out.println("added label for " + name);
			}

			else if (officefile.getName().endsWith(".PPTX") || officefile.getName().endsWith(".pptx")) {

				System.out.println("inside PPTX");

				FileInputStream fileInputStream = new FileInputStream(officefile);

				XMLSlideShow ppt = new XMLSlideShow(fileInputStream);

				POIXMLProperties pptxFileProps = ppt.getProperties();
				pptxFileProps.getCoreProperties().setDescription(driveId);

				pptxFileProps.getCustomProperties().addProperty("driveId", driveId);

				pptxFileProps.getCustomProperties().addProperty("path", path);

				pptxFileProps.commit();

				FileOutputStream fileOutputStreamForLabeledfile = new FileOutputStream(labeledFilePath);
				ppt.write(fileOutputStreamForLabeledfile);

				fileOutputStreamForLabeledfile.close();
				fileInputStream.close();

				System.out.println("added label for " + name);
			}

			else if (officefile.getName().endsWith(".ppt") || officefile.getName().endsWith(".PPT")
					|| (officefile.getName().endsWith(".xls") || officefile.getName().endsWith(".XLS"))
					|| (officefile.getName().endsWith(".doc") || officefile.getName().endsWith(".DOC"))) {

				System.out.println("inside doc,xls,ppt");

				FileInputStream fileInputStream = new FileInputStream(officefile);

				NPOIFSFileSystem fs = new NPOIFSFileSystem(fileInputStream);

				HPSFPropertiesOnlyDocument doc = new HPSFPropertiesOnlyDocument(fs);

				SummaryInformation si = doc.getSummaryInformation();
				if (si == null)
					doc.createInformationProperties();

				si.setComments(driveId);

				doc.getDocumentSummaryInformation().getCustomProperties().put("driveId", driveId);

				doc.getDocumentSummaryInformation().getCustomProperties().put("path", path);

				FileOutputStream fileOutputStreamForLabeledfile = new FileOutputStream(labeledFilePath);
				doc.write(fileOutputStreamForLabeledfile);
				fileOutputStreamForLabeledfile.close();
				fileInputStream.close();
				fs.close();
				System.out.println("added label for " + name);
			} else {
				System.err.println("Not a office file hence skipping");
			}

		} else {
			System.out.println("not a office or this app supported file, hence skipping");
		}
		httpConn.disconnect();
	}

	public static SuccessMessageObject doGet(final String url, String tokenheader)
			throws ClientProtocolException, IOException {

		try {
			SuccessMessageObject messageObject = new SuccessMessageObject();
			DefaultHttpClient httpClient = new DefaultHttpClient();
			final HttpGet httpRequest = new HttpGet(url);

			httpRequest.addHeader("Content-Type", "text/plain");
			httpRequest.addHeader("Authorization", tokenheader);

			logger.info(httpRequest.getMethod());

			HttpResponse response = httpClient.execute(httpRequest);

			logger.info(response);

			if (null == response) {
				logger.error("Http Request failed, httpResponse is null.");
				messageObject.setMessage("error");
				logger.error("HTTP response is null");
				releaseHttpConnection(httpRequest);

			}

			if (null == response.getStatusLine()) {
				logger.error("Http Request failed, httpResponse is null.");
				messageObject.setMessage("error");
				logger.error("HTTP getStatusLine response is null");
				releaseHttpConnection(httpRequest);

			}

			final Integer httpStatusCode = response.getStatusLine().getStatusCode();

			final org.apache.http.HttpEntity entity = (org.apache.http.HttpEntity) response.getEntity();
			final String responseString = EntityUtils.toString((org.apache.http.HttpEntity) entity, "UTF-8");
			EntityUtils.consume(entity);
			logger.info(httpRequest.toString());
			System.out.println(responseString);
			httpClient.getConnectionManager().shutdown();

			messageObject.setResponse(responseString);
			messageObject.setMessage("success");

			if (httpStatusCode != null && !httpStatusCode.equals(HttpStatus.SC_OK)) {
				messageObject.setMessage("error");
			}

			return messageObject;
		} catch (Exception ex) {

			SuccessMessageObject messageObject = new SuccessMessageObject();
			messageObject.setMessage("error");

			logger.info("error occured" + ex.getMessage());
			return messageObject;
		}

	}

	private static void releaseHttpConnection(final HttpRequestBase httpRequest) {
		if (null != httpRequest) {
			httpRequest.abort();
			;
			if (!httpRequest.isAborted()) {
				httpRequest.abort();
			}
		}
	}

	@Override
	public ModelAndView listSharedUsers(TokenAndPath tokenAndPath) throws IOException, IllegalStateException,
			JsonSyntaxException, InterruptedException, NumberFormatException, OpenXML4JException, XmlException {

		ModelAndView enterLinkView = new ModelAndView();

		try {

			SuccessMessageObject messageObject = new SuccessMessageObject();

			String access_token = tokenAndPath.getToken();

			String tokenheader = "Bearer" + " " + access_token;

			String urlForSharedWithMeItems = "https://graph.microsoft.com/beta/me/drive/sharedWithMe";

			// make a get call to one drive api

			SuccessMessageObject responseAndMessage = UserServiceImpl.doGet(urlForSharedWithMeItems, tokenheader);

			String responseFromAdaptor = responseAndMessage.getResponse();

			Gson gson = new Gson();

			if (responseAndMessage.getMessage() != null && responseAndMessage.getMessage().equalsIgnoreCase("error")) {

				ModelAndView errorView = new ModelAndView();

				Error error = gson.fromJson(responseFromAdaptor, Error.class);

				messageObject.setMessage(error.getError().getCode());

				errorView.addObject("message", messageObject);
				errorView.setViewName("display");
				return errorView;
			}

			OuterMetaData outerMetaData = gson.fromJson(responseFromAdaptor, OuterMetaData.class);

			HashMap<String, User> namesOfAllSharingUsers = new HashMap<String, User>();

			Set<String> displayNames = new HashSet<String>();

			HashMap<String, String> namesAndDriveId = new HashMap<String, String>();

			for (MetaDataForFolder metaDataForFolder : outerMetaData.getValue()) {

				String driveId = metaDataForFolder.getRemoteItem().getParentReference().getDriveId();

				namesOfAllSharingUsers = metaDataForFolder.getCreatedBy();
				if (namesOfAllSharingUsers.isEmpty()) {
					namesOfAllSharingUsers = metaDataForFolder.getLastModifiedBy();
				}
				// String driveId=
				// "b!xTDMGJt6IEiuUTWPKWl2DIgyJcgGyIxOnPrOum8TeyfKUQRBWwV8TofsOMwgqCI2";
				Collection<User> users = namesOfAllSharingUsers.values();
				Iterator<User> itr = users.iterator();
				while (itr.hasNext()) {
					User currentDriveUser = (User) itr.next();
					displayNames.add(currentDriveUser.getDisplayName());
					namesAndDriveId.put(currentDriveUser.getDisplayName(), driveId);
				}
			}

			enterLinkView.setViewName("shareduserslist");
			enterLinkView.addObject("sharedusers", namesAndDriveId);
			System.out.println("json form ");
			System.out.println(outerMetaData);

		} catch (Exception universalException) {
			logger.error(universalException.getStackTrace());
			SuccessMessageObject messageObject = new SuccessMessageObject();
			messageObject.setMessage(universalException.getMessage());
			enterLinkView.addObject("message", messageObject);
			logger.error("error occured" + universalException.getCause());
			return enterLinkView;
		}
		return enterLinkView;
	}

	@Override
	public ModelAndView sharedItemsDownloadAndConvert(TokenAndPath tokenAndPath)
			throws IOException, IllegalStateException, JsonSyntaxException, InterruptedException, NumberFormatException,
			OpenXML4JException, XmlException {

		ModelAndView enterLinkView = new ModelAndView();

		try {

			SuccessMessageObject messageObject = new SuccessMessageObject();

			enterLinkView.setViewName("display");

			String access_token = tokenAndPath.getToken();

			String tokenheader = "Bearer" + " " + access_token;

			String driveId = tokenAndPath.getDriveId();

			String fileUrl = tokenAndPath.getPath();

			String commonUrl = "https://graph.microsoft.com/beta";

			if (fileUrl.contains("?")) {
				int indexOfQueryParam = fileUrl.indexOf("?");

				fileUrl = fileUrl.substring(0, indexOfQueryParam);
			}

			if ((fileUrl.endsWith(".pdf")) || (fileUrl.endsWith(".PDF")) || (fileUrl.endsWith(".DOCS"))
					|| (fileUrl.endsWith(".docs")) || (fileUrl.endsWith(".DOCX")) || (fileUrl.endsWith(".docx"))
					|| (fileUrl.endsWith(".pptx")) || (fileUrl.endsWith(".PPTX")) || (fileUrl.endsWith(".PPT"))
					|| (fileUrl.endsWith(".ppt")) || (fileUrl.endsWith(".pdf")) || (fileUrl.endsWith(".xlsx"))
					|| (fileUrl.endsWith(".XLSX")) || (fileUrl.endsWith(".xls")) || (fileUrl.endsWith(".XLS"))) {

				String base_path = tokenAndPath.getPath();// replaceAll("%20", "
															// ");

				// gets the start index after the documents path
				int indexAfterDocuments = base_path.lastIndexOf("Documents") + 10;

				String file = base_path.substring(indexAfterDocuments);

				String oneDriveFileUrl = commonUrl + "/drives/" + driveId + "/root:/" + file;

				int local_directory = file.lastIndexOf("/") + 1;

				String local_folder = file.substring(local_directory);

				String MakeLocalDirectory = local_folder.replace("%20", " ");

				int indexToRemoveExntension = MakeLocalDirectory.lastIndexOf(".");

				String extensionLessDirectory = MakeLocalDirectory.substring(0, indexToRemoveExntension);

				File dir = new File(saveDir + "/" + extensionLessDirectory);

				dir.mkdirs();

				SuccessMessageObject responseAndMessage = UserServiceImpl.doGet(oneDriveFileUrl, tokenheader);

				String responseFromAdaptor = responseAndMessage.getResponse();

				System.out.println("responseFromAdaptor   " + responseFromAdaptor);

				Gson gson = new Gson();

				if (responseAndMessage.getMessage() != null
						&& responseAndMessage.getMessage().equalsIgnoreCase("error")) {

					ModelAndView errorView = new ModelAndView();

					Error error = gson.fromJson(responseFromAdaptor, Error.class);

					messageObject.setMessage(error.getError().getCode());

					errorView.addObject("message", messageObject);

					errorView.setViewName("display");

					return errorView;
				}

				MetaDataForFolder outerMetaData = gson.fromJson(responseFromAdaptor, MetaDataForFolder.class);

				String Url = outerMetaData.getMicrosoft_graph_downloadUrl();

				String driveIdMetaData = outerMetaData.getRemoteItem().getParentReference().getDriveId();

				String pathMetaData = outerMetaData.getRemoteItem().getParentReference().getPath();

				UserServiceImpl.downloadFile(Url, dir.getPath(), driveIdMetaData, pathMetaData);

				// fileReaderAndConverter(file, dir);

				messageObject.setMessage(" Your files are downloaded to " + dir.getPath().toString());
				enterLinkView.addObject("message", messageObject);
				enterLinkView.setViewName("display");

				return enterLinkView;

			}

			// String base_path =
			// "https://myoffice.accenture.com/personal/sai_kiran_akkireddy_accenture_com/Documents/testDownload";
			String base_path = tokenAndPath.getPath();// replaceAll("%20", " ");

			// gets the start index after the documents path
			int indexAfterDocuments = base_path.lastIndexOf("Documents") + 10;

			String folderPathAfterdocuments = base_path.substring(indexAfterDocuments);

			int local_directory = folderPathAfterdocuments.lastIndexOf("/") + 1;

			String local_folder = folderPathAfterdocuments.substring(local_directory);

			String childAppender = ":/children";

			String MakeLocalDirectory = local_folder.replace("%20", " ");

			String completeurl = commonUrl + "/drives/" + driveId + "/root:/" + folderPathAfterdocuments
					+ childAppender;

			// making a directory
			File dir = new File(saveDir + "/" + MakeLocalDirectory);
			dir.mkdirs();

			System.out.println(completeurl);

			// make a get call to one drive api

			SuccessMessageObject responseAndMessage = UserServiceImpl.doGet(completeurl, tokenheader);

			String responseFromAdaptor = responseAndMessage.getResponse();

			logger.info(responseFromAdaptor);

			final Gson gson = new Gson();

			if (responseAndMessage.getMessage() != null && responseAndMessage.getMessage().equalsIgnoreCase("error")) {

				ModelAndView errorView = new ModelAndView();

				Error error = gson.fromJson(responseFromAdaptor, Error.class);

				messageObject.setMessage(error.getError().getCode());

				errorView.addObject("message", messageObject);
				errorView.setViewName("display");
				return errorView;
			}

			OuterMetaData outerMetaData = gson.fromJson(responseFromAdaptor, OuterMetaData.class);

			System.out.println("json form ");
			System.out.println(outerMetaData);
			List<String> downloadUrls = new ArrayList<String>();

			List<ParentReference> listwithMetaData = new ArrayList<ParentReference>();
			for (MetaDataForFolder metaDataForFolder : outerMetaData.getValue()) {

				if (metaDataForFolder.getFolder() != null
						&& (Integer.parseInt(metaDataForFolder.getFolder().getChildCount()) >= 0)) {
					readingInnerFolders(tokenheader, commonUrl, base_path, childAppender, folderPathAfterdocuments, dir,
							gson, metaDataForFolder);
				} else {
					ParentReference parentReference = new ParentReference();

					String Url = metaDataForFolder.getMicrosoft_graph_downloadUrl();
					String driveID = metaDataForFolder.getParentReference().getDriveId();

					String path = metaDataForFolder.getParentReference().getPath();
					parentReference.setId(Url);
					parentReference.setDriveId(driveID);
					parentReference.setPath(path);
					listwithMetaData.add(parentReference);
					downloadUrls.add(Url);
				}
			}

			// create the size of the thread pool dynamically
			if (!listwithMetaData.isEmpty()) {
				ExecutorService executor = Executors.newFixedThreadPool(listwithMetaData.size());
				final long startTime = System.currentTimeMillis();
				for (ParentReference parentReference : listwithMetaData) {

					System.out.println("saveDir------>" + saveDir);
					// multithreading framework for downloading files
					Runnable download = new MultiDownLoadExecutor(parentReference, dir.getPath());
					executor.execute(download);
				}
				executor.shutdown();

				final long endTime = System.currentTimeMillis();
				System.out.println("Time taken to get Response in millis:" + (endTime - startTime));

				// concurrentConverter(folderPathAfterdocuments, dir, executor);
			} else {

				// if there are no urls in the current folder
				// fileReaderAndConverter(folderPathAfterdocuments, dir);
			}

			messageObject.setMessage(" Your files are downloaded to " + dir.getPath().toString());
			enterLinkView.addObject("message", messageObject);

			logger.info(enterLinkView);

		} catch (Exception universalException) {
			SuccessMessageObject messageObject = new SuccessMessageObject();
			messageObject.setMessage(universalException.getMessage());
			enterLinkView.addObject("message", messageObject);
			logger.info("error occured" + universalException.getMessage());
			return enterLinkView;

		}
		return enterLinkView;
	}

	/*
	 * upload documents upto 4mb (non-Javadoc)
	 * 
	 * @see com.onedrive.UserService#uploadDocumentsToOneDrive(com.onedrive.
	 * TokenAndPath, java.io.InputStream, java.lang.String)
	 */

	@Override
	public ModelAndView uploadDocumentsToOneDrive(TokenAndPath tokenAndPath, InputStream fileInputStream,
			FileInputStream fileContentForUpload, String nameOfFile)
			throws ClientProtocolException, IOException, MessagingException, DocumentException {

		ModelAndView uploadFileView = new ModelAndView();

		String access_token = tokenAndPath.getToken();

		// method to read the meta data to get the path to be uploaded.

		ReadDriveIdAndPath readDriveIdAndPath = new ReadDriveIdAndPath(fileContentForUpload, nameOfFile);

		String path = readDriveIdAndPath.getDriveIdAndPath();

		String commonUrl = "https://graph.microsoft.com/v1.0";

		String contentStringAppender = ":/content";

		String nameOfFileFormatted = nameOfFile.replace(" ", "%20");
		String completeurl = commonUrl + path + "/" + nameOfFileFormatted + contentStringAppender;

		try

		{

			byte[] bytearray = new byte[tokenAndPath.getFileSize()];

			BufferedInputStream bin = new BufferedInputStream(fileInputStream);
			bin.read(bytearray, 0, bytearray.length);

			URL uploadURL = new URL(completeurl.toString());

			HttpURLConnection uploadConn = (HttpURLConnection) uploadURL.openConnection();

			uploadConn.setRequestMethod("PUT");
			uploadConn.setUseCaches(false);
			uploadConn.setDoOutput(true);

			uploadConn.setRequestProperty("Authorization", "Bearer " + access_token); // this
																						// is
																						// right
			uploadConn.setRequestProperty("Content-Type", "application/octet-stream");

			uploadConn.setChunkedStreamingMode(0);

			try (OutputStream os = uploadConn.getOutputStream()) {
				os.write(bytearray, 0, bytearray.length);
				os.flush();

			}

			uploadConn.disconnect();
			bin.close();
			fileInputStream.close();
		}

		catch (Exception ex) {
			SuccessMessageObject messageObject = new SuccessMessageObject();
			messageObject.setMessage("Error occured  Reason: " + ex.getMessage());
			uploadFileView.addObject("message", messageObject);
			uploadFileView.setViewName("display");
		}

		SuccessMessageObject messageObject = new SuccessMessageObject();
		messageObject.setMessage("successfully uploaded " + nameOfFile + " to users shared drive");
		uploadFileView.addObject("message", messageObject);
		uploadFileView.setViewName("display");

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
			FileInputStream fileInputStream, FileInputStream fileContentForUpload, String nameOfFile)
			throws ClientProtocolException, IOException, MessagingException, DocumentException {

		ModelAndView uploadFileView = new ModelAndView();

		String access_token = tokenAndPath.getToken();

		byte[] bytes;

		ReadDriveIdAndPath readDriveIdAndPath = new ReadDriveIdAndPath(fileInputStream, nameOfFile);

		String path = readDriveIdAndPath.getDriveIdAndPath();

		String commonUrl = "https://graph.microsoft.com/v1.0";

		String nameOfFileFormatted = nameOfFile.replace(" ", "%20");
		try

		{

			String sessionCreateUrl = commonUrl + path + "/" + nameOfFileFormatted + ":/createUploadSession"
					+ "?@name.conflictBehavior=replace";

			DefaultHttpClient httpClient = new DefaultHttpClient();
			final HttpPost httpRequest = new HttpPost(sessionCreateUrl);

			Item item = new Item();
			item.setMicrosoft_graph_conflictBehavior("replace");
			UploadSessionBody uploadBody = new UploadSessionBody();

			uploadBody.setItem(item);

			Gson gson = new Gson();

			String jsonBody = gson.toJson(uploadBody);

			httpRequest.addHeader("Content-Type", "application/json");
			httpRequest.addHeader("Authorization", "Bearer " + access_token);

			logger.info(httpRequest.getMethod());

			final StringEntity input = new StringEntity(jsonBody);

			httpRequest.setEntity(input);

			HttpResponse concreteOneResponse = httpClient.execute(httpRequest);

			final Integer httpStatusCode = concreteOneResponse.getStatusLine().getStatusCode();

			if ((concreteOneResponse != null) && (concreteOneResponse.getStatusLine() != null)
					&& (httpStatusCode.equals(HttpStatus.SC_OK))) {

				final org.apache.http.HttpEntity entity = (org.apache.http.HttpEntity) concreteOneResponse.getEntity();

				final String responseString = EntityUtils.toString((org.apache.http.HttpEntity) entity, "UTF-8");

				EntityUtils.consume(entity);

				logger.info(httpRequest.toString());

				System.out.println(responseString);

				logger.info(responseString);

				httpClient.getConnectionManager().shutdown();

				UploadSessionCreateResponse uploadSessionCreateResponse = gson.fromJson(responseString,
						UploadSessionCreateResponse.class);

				String uploadUrl = uploadSessionCreateResponse.getUploadUrl();

				int numToCalculateEndrange = 1;
				int endRange = 0;
				int content_length = 0;
				long nextRanges = 0L;
				fileInputStream = fileContentForUpload;
				long fileInputStreamIntilialSize = fileInputStream.available();
				finished = false;
				canceled = false;
				while (!canceled && !finished) {

					long currFirstByteStream = 0L;

					PreparedRequest uploadChunk = new PreparedRequest(uploadUrl, PreparedRequestMethod.PUT);

					if (nextRanges + chunkSize < fileInputStream.available()) {
						bytes = new byte[chunkSize];
						content_length = chunkSize;
						endRange = numToCalculateEndrange * chunkSize - 1;
						numToCalculateEndrange++;
					}

					else {
						// optimistic cast, assuming the last bit of the file is
						// never bigger than MAXINT
						bytes = new byte[(int) (fileInputStream.available())];
						content_length = (int) fileInputStream.available();
						endRange = (numToCalculateEndrange - 1) * chunkSize + content_length - 1;
					}

					fileInputStream.read(bytes);
					uploadChunk.setBody(bytes);

					String Content_length = Integer.toString(content_length);
					uploadChunk.addHeader("Content-Length", (content_length) + "");
					logger.info("Content-Length      " + content_length);
					uploadChunk.addHeader("Content-Range",
							String.format("bytes %s-%s/%s", nextRanges, endRange, fileInputStreamIntilialSize));

					logger.info("Uploading chunk {} - {}" + nextRanges + "-" + endRange);
					String url;
					RequestBody body = null;

					if (uploadChunk.getBody() != null) {
						body = RequestBody.create(null, uploadChunk.getBody());
					}

					if (isCompleteURL(uploadChunk.getPath())) {
						url = uploadChunk.getPath();
					} else {
						url = String.format("%s%s?access_token=%s", this.baseUrl, uploadChunk.getPath(), access_token);
					}

					logger.debug(String.format("making request to %s", url));

					Request.Builder builder = new Request.Builder().method(uploadChunk.getMethod(), body).url(url);

					for (String key : uploadChunk.getHeader().keySet()) {
						builder.addHeader(key, uploadChunk.getHeader().get(key));
					}

					// Add auth permanently to header with redirection
					builder.header("Authorization", "bearer " + access_token);
					Request request = builder.build();
					OkHttpClient client = new OkHttpClient();
					client.setConnectTimeout(60, TimeUnit.SECONDS); // connect
																	// timeout
					client.setReadTimeout(60, TimeUnit.SECONDS);
					Response response = client.newCall(request).execute();

					ConcreteOneResponse concreteOneResponse1 = new ConcreteOneResponse(response);
					if (concreteOneResponse1.wasSuccess()) {
						// if request is successful a 202 accepted message is
						// received
						if (concreteOneResponse1.getStatusCode() == HttpStatus.SC_CREATED
								|| concreteOneResponse1.getStatusCode() == HttpStatus.SC_OK) {
							// if last chunk upload was successful end the
							finished = true;
							String resp = concreteOneResponse1.getBodyAsString();
							SuccessMessageObject messageObject = new SuccessMessageObject();
							messageObject.setMessage("successfully uploaded " + nameOfFile + " to users shared drive");
							uploadFileView.addObject("message", messageObject);
							uploadFileView.setViewName("display");
						} else {
							// just continue until we get created status
							uploadSession = gson.fromJson(concreteOneResponse1.getBodyAsString(), UploadSession.class);

							nextRanges = uploadSession.getNextRange();

						}
					}
				}
				return uploadFileView;
			} else {

				SuccessMessageObject messageObject = new SuccessMessageObject();
				// write the proper message
				messageObject.setMessage("Error occured  while uploading file " + nameOfFile + "<br>"
						+ concreteOneResponse.getStatusLine() + "</br>");
				uploadFileView.addObject("message", messageObject);
				uploadFileView.setViewName("display");
				return uploadFileView;

			}

		} catch (Exception e) {

			SuccessMessageObject messageObject = new SuccessMessageObject();
			messageObject.setMessage("Error occured  Reason: " + e.getMessage());
			uploadFileView.addObject("message", messageObject);
			uploadFileView.setViewName("display");
			return uploadFileView;

			// TODO: handle exception
		}
		
		finally{
			fileInputStream.close(); 
		 fileContentForUpload.close();;
		}

		// TODO Auto-generated method stub

	}

	@Override
	public ModelAndView uploadFolderToOneDrive(TokenAndPath tokenAndPath)
			throws ClientProtocolException, IOException, MessagingException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		ModelAndView uploadFileView = new ModelAndView();

		StringBuffer statusOfAllThreads = new StringBuffer();

		List<Future<String>> listForUploadStatus = new ArrayList<Future<String>>();

		List<String> finalListForUploadStatus = new ArrayList<String>();

		try {

			String accessToken = tokenAndPath.getToken();

			// List to hold the status of process

			// creating a folder choser from the local drive

			Preferences prefs = Preferences.userRoot().node(getClass().getName());

			// JFileChooser chooser = new
			// JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
			
			Frame frame=new JFrame();
			
			frame.setVisible(true);
			
			frame.setExtendedState(JFrame.ICONIFIED);
            frame.setExtendedState(JFrame.NORMAL);

			JFileChooser chooser = new JFileChooser(prefs.get(LAST_USED_FOLDER, new File(".").getAbsolutePath()));
			
			chooser.setFocusable(true);
		
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			chooser.updateUI();
			chooser.setDialogTitle("Double Click to go inside ,click save to select folder: ");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int returnValue = chooser.showSaveDialog(null);

			// Confirm dialog box the option from the user

			int result = JOptionPane.showConfirmDialog(null, "confirm " + chooser.getSelectedFile());

			while (result == JOptionPane.NO_OPTION) {
				returnValue = chooser.showSaveDialog(null);

				result = JOptionPane.showConfirmDialog(null, "confirm " + chooser.getSelectedFile());
			}

			if ((returnValue == JFileChooser.APPROVE_OPTION) && (result == JOptionPane.YES_OPTION)) {

				
				 frame.setVisible(false);
				prefs.put(LAST_USED_FOLDER, chooser.getSelectedFile().getParent());

				String pathGiven = chooser.getSelectedFile().toString();

				List<File> filesInFolder = Files.walk(Paths.get(pathGiven)).filter(Files::isRegularFile)
						.map(Path::toFile).collect(Collectors.toList());

				String TotalNoOfFilesInFolder = Integer.toString(filesInFolder.size());
				statusOfAllThreads
						.append("Total files inside the selected folder=" + TotalNoOfFilesInFolder + "</br></br>");
				ExecutorService UploadExecutor = Executors.newFixedThreadPool(10);

				for (File file : filesInFolder) {
					Callable<String> callableThread = new FolderUploaderToOneDrive(file, accessToken);
					Future<String> future = UploadExecutor.submit(callableThread);
					listForUploadStatus.add(future);
				}

				UploadExecutor.shutdown();
				try {
					UploadExecutor.awaitTermination(1800, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				for (Future<String> uploadResultForEachFile : listForUploadStatus) {
					try {
						// print the return value of Future, notice the output
						// delay in console
						// because Future.get() waits for task to get completed
						statusOfAllThreads.append(uploadResultForEachFile.get() + "</br></br>");
						finalListForUploadStatus.add(uploadResultForEachFile.get());
						System.out.println(new Date() + "::" + uploadResultForEachFile.get());
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}

			}

			if (result == JOptionPane.CANCEL_OPTION ||returnValue==JOptionPane.CANCEL_OPTION ) {
				uploadFileView.setViewName("uploadfile");
				return uploadFileView;

			}
		} catch (Exception e) {
			logger.info("Error occured while uploading a foder" + e.getMessage());
		}

		SuccessMessageObject messageObject = new SuccessMessageObject();
		messageObject.setMessage(statusOfAllThreads.toString());
		uploadFileView.addObject("finalListForUploadStatus", finalListForUploadStatus);
		uploadFileView.setViewName("uploadStatus");
		return uploadFileView;
	}

}