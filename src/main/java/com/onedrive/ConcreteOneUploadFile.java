package com.onedrive;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

/**
 * Implementation of OneUploadFile, blocking operation
 */
public class ConcreteOneUploadFile implements OneUploadFile {

    private static final int chunkSize = 320 * 1024 * 30; // (use a multiple value of 320KB, best practice of dev.onedrive)
    private static final Logger logger = Logger.getLogger(ConcreteOneUploadFile.class);
    private static final Gson gson = new Gson();
    private final ReentrantLock shouldRun = new ReentrantLock(true);
    private File fileToUpload;
    private ConcreteOneDriveSDK api;
    private boolean canceled = false;
	private boolean finished = false;
	private UploadSession uploadSession;
	private RandomAccessFile randFile;
	private String uploadUrl ="";

	public ConcreteOneUploadFile(String url ,
			File fileToUpload, ConcreteOneDriveSDK api) throws IOException, OneDriveAuthenticationException {
		
		this.api = checkNotNull(api);

		if (fileToUpload != null) {
			if (fileToUpload.isFile()) {
				if (fileToUpload.canRead()) {
					this.fileToUpload = fileToUpload;
						randFile = new RandomAccessFile(fileToUpload, "r");
				} else {
					throw new IOException(String.format("File %s is not readable!", fileToUpload.getName()));
				}
			} else {
				throw new IOException(String.format("%s is not a File",
						fileToUpload.getAbsolutePath()));
			}
		} else {
			throw new NullPointerException("FileToUpload was null");
		}
		//this.uploadSession = api.createUploadSession(parentFolder, fileToUpload.getName());
		
		String urlbusiness="https://myoffice.accenture.com/personal/lei_a_ding_accenture_com/_api/v2.0/drives/b!xTDMGJt6IEiuUTWPKWl2DIgyJcgGyIxOnPrOum8TeyfKUQRBWwV8TofsOMwgqCI2/items/01QI6G2QNJ7MDVSWYTNZHLGA5Y4A3F6RBM/uploadSession?guid='1f1e19d6-f0e6-4a87-971c-c0588803ec1b'&path='~tmpC0_wordtestpostman.pdf'&overwrite=False&rename=True&access_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIwMDAwMDAwMy0wMDAwLTBmZjEtY2UwMC0wMDAwMDAwMDAwMDAvbXlvZmZpY2UuYWNjZW50dXJlLmNvbUBlMDc5M2QzOS0wOTM5LTQ5NmQtYjEyOS0xOThlZGQ5MTZmZWIiLCJpc3MiOiIwMDAwMDAwMy0wMDAwLTBmZjEtY2UwMC0wMDAwMDAwMDAwMDAiLCJuYmYiOjE0OTczOTMyNTgsImV4cCI6MTQ5NzQ3OTY1OCwiZW5kcG9pbnR1cmwiOiJHTS84aTlDQ0piTEJSVlYwYys2Nk5pcnNOOTRVbVgrL3dIeGZJSkVXZzJjPSIsImVuZHBvaW50dXJsTGVuZ3RoIjoiMzA5IiwiaXNsb29wYmFjayI6IlRydWUiLCJuYW1laWQiOiIwIy5mfG1lbWJlcnNoaXB8c2FpLmtpcmFuLmFra2lyZWRkeUBhY2NlbnR1cmUuY29tIiwibmlpIjoibWljcm9zb2Z0LnNoYXJlcG9pbnQiLCJpc3VzZXIiOiJ0cnVlIiwiY2FjaGVrZXkiOiIwaC5mfG1lbWJlcnNoaXB8MTAwMzAwMDBhMDk1MTc5MUBsaXZlLmNvbSIsInVzZVBlcnNpc3RlbnRDb29raWUiOm51bGx9.QH_w5u10JSBUxOGcn-y_sgpw1mU66_Pkk_que2JHWDQ&prooftoken=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImpOX1RsZ1otUUk0UHZpc2pTVnpKMW9ySnRnOCJ9.eyJhdWQiOiIwMDAwMDAwMy0wMDAwLTBmZjEtY2UwMC0wMDAwMDAwMDAwMDBAKiIsImlzcyI6IjAwMDAwMDAzLTAwMDAtMGZmMS1jZTAwLTAwMDAwMDAwMDAwMEAqIiwibmJmIjoiMTQ5NzMyMDY4NyIsImV4cCI6IjE0OTc5MjU0ODciLCJwcmYiOiJJdDRkb1VVOWloaEFBN3NhTkZkYW9oeFVSZSt3akRMdStJVE4yNm9kblhZMlRSQkNnMGVQVC8rUnA4ODN1czNqTHl6UWlKTSsxSVRyRkhMNHg1bW9CckZJc3JsNytFMUxIOTcrMExEZW5JUTcyOGt5NVVYRTBhNStpOGFFdmZQT0crOCtySVBWTlJuT2ZRQVN2S0dkbGpjWHdSc1VoWUtxcTVmUDdFanB6aWl6dytZM3piaDNxMjNsa0VVZXJjNTVCY2lYeG5UdFVuV25jQjQzdHd4U0RpcGU2Z3BBVHBJK1BqdElmVjFDUnFJZ1ZQckZ0NzNLcThvNFQrNTlHTEZQck9GN0xXRWxZam9CVHFrQ3dmODZrcy80MVFmU2lNOVVGbW9SWC9TaENQOXVNTWJpYmtCcE5YQmdCejZSOVQrUzJlRTZMRzcyV3FIdmRKK3pEMllVcVE9PSIsImlzdXNlciI6InRydWUifQ.jwGCvZ9gAKHah8X8yOiFFAEE0yX4fnZSwYVrRkscNROuzLqutPaueocGpYBJnly977DcYAYrO2VIZI4h7AxNO7jsVGR3JnYOLV0apnTN7Tg3EjggrHOIKbVhfEl9lbAE5XmDhWGTUnX0NpU3tNzxJtoPpjw4c8CtTwjTzvPwkL9M82qwEn8qI2CqeJXPdZv0wFip5A-qflVszVAYQeFlJtNAyGtMOGzBwBk22HlBDl7jvlrgLBUZ8TMD1vj-Q3vGqjokscsVUw1Q5y7yfu4NFu437GVXd13WGMGT9ONxn8BmyJnytyQvnGAoZNrorpqM-2MKmg-dFyD8Mc91j3Z71g";
		this.uploadUrl = url;
    }

    @Override
	public long fileSize() {
		return fileToUpload.length();
	}

    @Override
	public long uploadStatus() throws IOException, OneDriveException {
		if (uploadSession != null) {
            PreparedRequest request = new PreparedRequest(this.uploadUrl, PreparedRequestMethod.GET);
			OneResponse response = api.makeRequest(request);
            if (response.wasSuccess()) {
                return gson.fromJson(response.getBodyAsString(), UploadSession.class).getNextRange();
            } else {
				throw new OneDriveException(response.getBodyAsString());
			}
		}
		return 0;
	}

    @Override
	public OneFile startUpload() throws IOException, OneDriveException {
		byte[] bytes;
		ConcreteOneFile finishedFile = null;

		OneResponse response;

		while (!canceled && !finished) {
			shouldRun.lock();

			long currFirstByte = randFile.getFilePointer();
			PreparedRequest uploadChunk = new PreparedRequest(this.uploadUrl, PreparedRequestMethod.PUT);

			if (currFirstByte + chunkSize < randFile.length()) {
				bytes = new byte[chunkSize];
			} else {
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

      //     logger.info("Uploading chunk {} - {}", start, randFile.getFilePointer() - 1);
            response = api.makeRequest(uploadChunk);
			if (response.wasSuccess()) {
				if (response.getStatusCode()==200 || response.getStatusCode()==201) { 
					// if last chunk upload was successful end the
					finished = true;
                    finishedFile = gson.fromJson(response.getBodyAsString(), ConcreteOneFile.class);

                }else {
					//just continue
                    uploadSession = gson.fromJson(response.getBodyAsString(),
							UploadSession.class);
                    randFile.seek(uploadSession.getNextRange());
				}
			} else {
				logger.info("Something went wrong while uploading last chunk. Trying to fetch upload status from server to retry");
				logger.trace(response.getBodyAsString());
                response = api.makeRequest(this.uploadUrl, PreparedRequestMethod.GET, null);

                if (response.wasSuccess()) {
                    uploadSession = gson.fromJson(
							response.getBodyAsString(), UploadSession.class);
                    randFile.seek(uploadSession.getNextRange());
            //        logger.debug("Fetched updated uploadSession. Server requests {} as next chunk",uploadSession.getNextRange());

				} else {
					canceled=true;
					logger.info("Something went wrong while uploading. Was unable to fetch the currentUpload session from the Server");
					throw new OneDriveException(
                            String.format("Could not get current upload status from Server, aborting. Message was: %s", response.getBodyAsString()));
                }
			}
			shouldRun.unlock();
		}

        logger.info("finished upload");

		finishedFile.setApi(api);
		return finishedFile;

	}

    @Override
	public OneUploadFile pauseUpload() {
		logger.info("Pausing upload");
		shouldRun.lock();
		logger.info("Upload paused");
		return this;
	}

    @Override
	public OneUploadFile resumeUpload() {
		logger.info("Resuming upload");
		try{
			shouldRun.unlock();
			logger.info("Upload resumed");
		}catch (IllegalMonitorStateException e) {
			logger.info("Trying to resume an already running download");
		}
		return this;
	}

    @Override
	public OneUploadFile cancelUpload() throws IOException, OneDriveAuthenticationException {
		logger.info("Canceling upload");
		this.canceled = true;
		if (uploadSession != null) {
			api.makeRequest(this.uploadUrl,
                    PreparedRequestMethod.DELETE, "");
            logger.info("Upload was canceled");
		}
		return this;
	}

    @Override
	public File getUploadFile() {
		return this.fileToUpload;
	}

	@Override
	public OneFile call() throws IOException, OneDriveException {
		logger.info("Starting upload");
		return startUpload();
	}

}
