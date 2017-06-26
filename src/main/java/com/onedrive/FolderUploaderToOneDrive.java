package com.onedrive;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
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

	public FolderUploaderToOneDrive(File file, String token) {
		this.token = token;
		this.file = file;
	}

	@Override
	public String call() {

		byte[] bytes;

		try {
			FileInputStream fileInputStream = new FileInputStream(file);

			long fourMBbsize = 4194304;

			long sizeOfFile = file.length();

			if (sizeOfFile < fourMBbsize) {

				String driveId = "b!xTDMGJt6IEiuUTWPKWl2DIgyJcgGyIxOnPrOum8TeyfKUQRBWwV8TofsOMwgqCI2";

				String commonUrl = "https://graph.microsoft.com/v1.0/drives/";

				// "
				String nameOfFile = file.getName(); // ");

				// gets the start index after the documents path
				int indexAfterDocuments = base_path.lastIndexOf("Documents") + 10;

				String folderPathAfterdocuments = base_path.substring(indexAfterDocuments);

				String contentStringAppender = ":/content";

				String nameOfFileFormatted = nameOfFile.replace(" ", "%20");
				String uploadUrl = commonUrl + driveId + "/root:/" + folderPathAfterdocuments + "/"
						+ nameOfFileFormatted + contentStringAppender;

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

				// using the lei drive Id ,change it later
				String driveId = "b!xTDMGJt6IEiuUTWPKWl2DIgyJcgGyIxOnPrOum8TeyfKUQRBWwV8TofsOMwgqCI2";

				String commonUrl = "https://graph.microsoft.com/v1.0/drives/";

				// String base_path = file.getPath();// replaceAll("%20", " ");

				// gets the start index after the documents path
				int indexAfterDocuments = base_path.lastIndexOf("Documents") + 10;

				String folderPathAfterdocuments = base_path.substring(indexAfterDocuments);

				String nameOfFileFormatted = nameOfFile.replace(" ", "%20");
				String sessionCreateUrl = commonUrl + driveId + "/root:/" + folderPathAfterdocuments + "/"
						+ nameOfFileFormatted + ":/createUploadSession" + "?@name.conflictBehavior=replace";

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
		return statusOfFileUpload;
	}

}
