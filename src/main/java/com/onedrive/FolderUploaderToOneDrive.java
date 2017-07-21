package com.onedrive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.hpsf.HPSFPropertiesOnlyDocument;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import com.google.gson.Gson;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class FolderUploaderToOneDrive implements Callable<String> {

	final static Logger logger = Logger.getLogger(FolderUploaderToOneDrive.class);

	private String statusOfFileUpload = null;
	private boolean canceled = false;
	private boolean finished = false;
	private String token;
	private File file;
	private int successCounter = 0;
	private int failureCounter = 0;

	private static final String base_path = "https://myoffice.accenture.com/personal/lei_a_ding_accenture_com/Documents/test/UploadFolderTest";
	private static final int chunkSize = 320 * 1024 * 30;

	private String driveId;

	private String path;

	public FolderUploaderToOneDrive(File file, String token) {
		this.token = token;
		this.file = file;
	}

	@Override
	public String call() throws IOException {

		FileInputStream fileInputStream =null;
		
		byte[] bytes;

		try {
	//		FileInputStream fileInputStream = new FileInputStream(file);

			long fourMBbsize = 4194304;

			long sizeOfFile = file.length();

			if (sizeOfFile < fourMBbsize) {

				String name = file.getName();

				String labeledFilePath = file.getAbsolutePath();

				getDriveIdAndPath(labeledFilePath);
				
				
				 fileInputStream = new FileInputStream(file);

				// String driveId =
				// "b!xTDMGJt6IEiuUTWPKWl2DIgyJcgGyIxOnPrOum8TeyfKUQRBWwV8TofsOMwgqCI2";

				String commonUrl = "https://graph.microsoft.com/v1.0";

				// "
				String nameOfFile = file.getName(); // ");

				// gets the start index after the documents path
				

				String contentStringAppender = ":/content";

				String nameOfFileFormatted = nameOfFile.replace(" ", "%20");
				String uploadUrl = commonUrl + path + "/" + nameOfFileFormatted + contentStringAppender;
				
				System.out.println("uploadUrl-->"+uploadUrl);

				bytes = new byte[(int) file.length()];

				PreparedRequest uploadChunk = new PreparedRequest(uploadUrl, PreparedRequestMethod.PUT);

				fileInputStream.read(bytes);
				uploadChunk.setBody(bytes);

				RequestBody body = null;

				if (uploadChunk.getBody() != null) {
					body = RequestBody.create(null, uploadChunk.getBody());
				}

				String url = uploadChunk.getPath();

				logger.debug(String.format("making request to %s", url));

				Request.Builder builder = new Request.Builder().method(uploadChunk.getMethod(), body).url(url);

				builder.header("Authorization", "bearer " + token);

				Request request = builder.build();
				OkHttpClient client = new OkHttpClient();
				client.setConnectTimeout(60, TimeUnit.SECONDS); // connect
																// timeout
				client.setReadTimeout(60, TimeUnit.SECONDS);
				Response response = client.newCall(request).execute();

				ConcreteOneResponse concreteOneResponse1 = new ConcreteOneResponse(response);

				if (concreteOneResponse1.wasSuccess()) {
					if (concreteOneResponse1.getStatusCode() == 200 || concreteOneResponse1.getStatusCode() == 201) {
						// if last chunk upload was successful end the

						logger.info("Uploaded  successfully " + file.getName() + " to one drive");
						statusOfFileUpload = "Succesfully Uploaded " + file.getAbsolutePath();
					}

				} else {
					Gson gson = new Gson();
					Error error = gson.fromJson(response.body().toString(), Error.class);

					logger.info("File " + file + "is not uploaded because of " + error.getError().getMessage());

					System.err.println("File " + file + "is not uploaded because of " + error.getError().getMessage());

				}
			}
			// For files greater than 4mb , create a session and upload by
			// fragments
			else {

				String nameOfFile = file.getName();
				
				

				String labeledFilePath = file.getAbsolutePath();

				
				//method to get the path to upload to one drive from the file
				getDriveIdAndPath(labeledFilePath);
				
				 fileInputStream = new FileInputStream(file);

				// using the lei drive Id ,change it later
//				String driveId = "b!xTDMGJt6IEiuUTWPKWl2DIgyJcgGyIxOnPrOum8TeyfKUQRBWwV8TofsOMwgqCI2";
//
				String commonUrl = "https://graph.microsoft.com/v1.0";
				
				

				
			

				String nameOfFileFormatted = nameOfFile.replace(" ", "%20");
				String sessionCreateUrl = commonUrl + path + "/" + nameOfFileFormatted  + ":/createUploadSession" + "?@name.conflictBehavior=replace";

				DefaultHttpClient httpClient = new DefaultHttpClient();
				final HttpPost httpRequest = new HttpPost(sessionCreateUrl);

				Item item = new Item();
				item.setMicrosoft_graph_conflictBehavior("replace");
				UploadSessionBody uploadBody = new UploadSessionBody();

				uploadBody.setItem(item);

				Gson gson = new Gson();

				String jsonBody = gson.toJson(uploadBody);

				httpRequest.addHeader("Content-Type", "application/json");
				httpRequest.addHeader("Authorization", "Bearer " + token);

				logger.info(httpRequest.getMethod());

				final StringEntity input = new StringEntity(jsonBody);

				httpRequest.setEntity(input);

				HttpResponse concreteOneResponse = httpClient.execute(httpRequest);

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

				URL uploadURL = new URL(uploadUrl.toString());

				int numToCalculateEndrange = 1;
				int endRange = 0;
				int content_length = 0;
				long nextRanges = 0L;

				long fileInputStreamIntilialSize = fileInputStream.available();
				finished = false;
				canceled = false;

				while (!canceled && !finished) {

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

					uploadChunk.addHeader("Content-Range",
							String.format("bytes %s-%s/%s", nextRanges, endRange, fileInputStreamIntilialSize));

					logger.info("Uploading chunk {} - {}" + nextRanges + "-" + endRange);
					String url;
					RequestBody body = null;

					if (uploadChunk.getBody() != null) {
						body = RequestBody.create(null, uploadChunk.getBody());
					}

					url = uploadChunk.getPath();

					logger.debug(String.format("making request to %s", url));

					Request.Builder builder = new Request.Builder().method(uploadChunk.getMethod(), body).url(url);

					for (String key : uploadChunk.getHeader().keySet()) {
						builder.addHeader(key, uploadChunk.getHeader().get(key));
					}

					// Add auth permanently to header with redirection
					builder.header("Authorization", "bearer " + token);
					Request request = builder.build();
					OkHttpClient client = new OkHttpClient();
					client.setConnectTimeout(60, TimeUnit.SECONDS); // connect
																	// timeout
					client.setReadTimeout(60, TimeUnit.SECONDS);
					Response response = client.newCall(request).execute();

					ConcreteOneResponse uploadOneDriveResponse = new ConcreteOneResponse(response);
					if (uploadOneDriveResponse.wasSuccess()) {
						if (uploadOneDriveResponse.getStatusCode() == 200
								|| uploadOneDriveResponse.getStatusCode() == 201) {
							// if last chunk upload was successful end the
							finished = true;
							logger.info("successfully uploaded all the fragments of " + file.getAbsolutePath());
							statusOfFileUpload = "Succesfully Uploaded " + file.getAbsolutePath();

						} else {
							// just continue
							UploadSession uploadSession = gson.fromJson(uploadOneDriveResponse.getBodyAsString(),
									UploadSession.class);

							nextRanges = uploadSession.getNextRange();

						}
					}

				}
			}
		} catch (Exception ex) {
			logger.error("ex.printStackTrace()", ex);
			logger.error("Error occured while uploading file " + file.getAbsolutePath());

			statusOfFileUpload = "Upload Error for " + file.getAbsolutePath();
			System.err.println(ex.getMessage());
			return statusOfFileUpload;
		}
		finally{
			fileInputStream.close();
		}
		return statusOfFileUpload;
	}

	/**
	 * @param labeledFilePath
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws DocumentException
	 */
	private void getDriveIdAndPath(String labeledFilePath)
			throws FileNotFoundException, IOException, DocumentException {
		if (file.getName().endsWith(".pdf") || file.getName().endsWith(".PDF")) {

			System.out.println("inside pdf");

			System.out.println("Reading the file to get metaData " + file.getName());

			FileInputStream fileInputStreamMetaData = new FileInputStream(file);
			PdfReader reader = new PdfReader(fileInputStreamMetaData);

			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(labeledFilePath));

			// get and edit meta-data
			HashMap<String, String> info = reader.getInfo();

			driveId = info.get("driveId");

			System.out.println("driveId--->" + driveId);

			path = info.get("path");

			System.out.println("path--->" + path);
			
			stamper.close();

			fileInputStreamMetaData.close();

		} else if (file.getName().endsWith(".docx") || file.getName().endsWith(".DOCX")) {

			System.out.println("inside docx");

			System.out.println("Reading the file to get metaData " + file.getName());
			FileInputStream fileInputStream1 = new FileInputStream(file);

			XWPFDocument xWPFDocument = new XWPFDocument(fileInputStream1);

			POIXMLProperties propsForDoc = xWPFDocument.getProperties();

			String Category = propsForDoc.getCoreProperties().getCategory();
			
			System.out.println("Category-->"+Category);

			String[] metaDataArray = Category.split("-->");

			driveId = metaDataArray[0];
			path = metaDataArray[1];

			String[] extractDriveId = driveId.split("-");
			String[] extractpath = path.split("-");
			driveId = extractDriveId[1];
			path = extractpath[1];

			System.out.println("driveId--->" + driveId);

			System.out.println("path--->" + path);

			fileInputStream1.close();
			

		}

		else if (file.getName().endsWith(".xlsx") || file.getName().endsWith(".XLSX")) {

			System.out.println("inside xlsx");

			System.out.println("Reading the file to get metaData " + file.getName());

			FileInputStream fileInputStream1 = new FileInputStream(file);

			XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream1);

			POIXMLProperties poixmlPropertiesForXlsx = workbook.getProperties();

			String Category = poixmlPropertiesForXlsx.getCoreProperties().getCategory();

			String[] metaDataArray = Category.split("-->");

			driveId = metaDataArray[0];
			 path = metaDataArray[1];

			System.out.println("driveId--->" + driveId);

			System.out.println("path--->" + path);

			String[] extractDriveId = driveId.split("-");
			String[] extractpath = path.split("-");
			driveId = extractDriveId[1];
			path = extractpath[1];

			fileInputStream1.close();
			
		}

		else if (file.getName().endsWith(".PPTX") || file.getName().endsWith(".pptx")) {

			System.out.println("inside PPTX");

			System.out.println("Reading the file to get metaData " + file.getName());

			FileInputStream fileInputStream1 = new FileInputStream(file);

			XMLSlideShow ppt = new XMLSlideShow(fileInputStream1);

			POIXMLProperties pptxFileProps = ppt.getProperties();

			String Category = pptxFileProps.getCoreProperties().getCategory();

			String[] metaDataArray = Category.split("-->");

			driveId = metaDataArray[0];
			path = metaDataArray[1];

			String[] extractDriveId = driveId.split("-");
			String[] extractpath = path.split("-");
			driveId = extractDriveId[1];
			path = extractpath[1];

			System.out.println("driveId--->" + driveId);

			System.out.println("path--->" + path);
			fileInputStream1.close();
			
		}

		else if (file.getName().endsWith(".ppt") || file.getName().endsWith(".PPT")
				|| (file.getName().endsWith(".xls") || file.getName().endsWith(".XLS"))
				|| (file.getName().endsWith(".doc") || file.getName().endsWith(".DOC"))) {

			System.out.println("inside doc,xls,ppt");

			System.out.println("Reading the file to get metaData " + file.getName());
			FileInputStream fileInputStream1 = new FileInputStream(file);

			NPOIFSFileSystem fs = new NPOIFSFileSystem(fileInputStream1);

			HPSFPropertiesOnlyDocument doc = new HPSFPropertiesOnlyDocument(fs);

			SummaryInformation si = doc.getSummaryInformation();
			if (si == null)
				doc.createInformationProperties();
			String Category = si.getKeywords();

			String[] metaDataArray = Category.split("-->");

			driveId = metaDataArray[0];
			path = metaDataArray[1];

			String[] extractDriveId = driveId.split("-");
			String[] extractpath = path.split("-");
			driveId = extractDriveId[1];
			path = extractpath[1];

			System.out.println("driveId--->" + driveId);

			System.out.println("path--->" + path);

			fileInputStream1.close();
			
		} else {
			System.err.println("Not a office file hence skipping");
		}
	}

}
