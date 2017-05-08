package com.mydrive.web.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MultiDownLoadExecutor implements Runnable {
	  
    private String fileURL;
    
    private String saveDir;;
    public MultiDownLoadExecutor(String fileURL, String saveDir){
        this.fileURL=fileURL;
        this.saveDir=saveDir;
    }

    @Override
    public void run() {
    	try {
			MultiDownLoadExecutor.downloadFile(fileURL, saveDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
                byte[] buffer = new byte[4096];
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
	@Override
	public String toString() {
		return "MultiDownLoadExecutor [fileURL=" + fileURL + ", saveDir=" + saveDir + "]";
	}

   
}