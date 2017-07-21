package com.onedrive;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.hpsf.HPSFPropertiesOnlyDocument;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;


import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.xml.xmp.XmpWriter;

public class MultiDownLoadExecutor implements Runnable {

	private ParentReference parentReference;

	private String saveDir;;

	public MultiDownLoadExecutor(ParentReference parentReference, String saveDir) {
		this.parentReference = parentReference;
		this.saveDir = saveDir;
	}

	@Override
	public void run() {
		try {
			try {
				MultiDownLoadExecutor.downloadFile(parentReference.getId(), parentReference.getDriveId(),
						parentReference.getPath(), saveDir);
			} catch (OpenXML4JException | XmlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void downloadFile(String fileURL, String driveId, String path, String saveDir)
			throws IOException, OpenXML4JException, XmlException, DocumentException {
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
			byte[] buffer = new byte[4096];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			outputStream.close();
			inputStream.close();

			System.out.println("File downloaded");

			System.out.println("now adding hidden metadata");

			File officefile = new File(saveFilePath);

			String pathToFile = "/sai/path";

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
				System.out.println("added label for " + name);
			} else if (officefile.getName().endsWith(".docx") || officefile.getName().endsWith(".DOCX")) {

				System.out.println("inside docx");
				FileInputStream fileInputStream = new FileInputStream(officefile);

				XWPFDocument xWPFDocument = new XWPFDocument(fileInputStream);

				POIXMLProperties propsForDoc = xWPFDocument.getProperties();

				// propsForDoc.getCoreProperties().setDescription(driveId);

				String driveID = "driveId-" + driveId;

				String path2 = "-->path-" + path;

				String completeMetaData = driveID + path2;

				propsForDoc.getCoreProperties().setCategory(completeMetaData);

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

		//		poixmlPropertiesForXlsx.getCoreProperties().setDescription(driveId);

				String driveID = "driveId-" + driveId;

				String path2 = "-->path-" + path;

				String completeMetaData = driveID + path2;

				poixmlPropertiesForXlsx.getCoreProperties().setCategory(completeMetaData);

				poixmlPropertiesForXlsx.commit();
				FileOutputStream fileOutputStreamForLabeledfile = new FileOutputStream(labeledFilePath);
				workbook.write(fileOutputStreamForLabeledfile);
				fileOutputStreamForLabeledfile.close();
				fileInputStream.close();
				System.out.println("added label for " + name);
			}

			else if (officefile.getName().endsWith(".PPTX") || officefile.getName().endsWith(".pptx")) {

				System.out.println("inside PPTX");

				FileInputStream fileInputStream = new FileInputStream(officefile);

				XMLSlideShow ppt = new XMLSlideShow(fileInputStream);

				POIXMLProperties pptxFileProps = ppt.getProperties();
			//	pptxFileProps.getCoreProperties().setDescription(driveId);

				String driveID = "driveId-" + driveId;

				String path2 = "-->path-" + path;

				String completeMetaData = driveID + path2;

				pptxFileProps.getCoreProperties().setCategory(completeMetaData);
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

			//	si.setComments(driveId);

				String driveID = "driveId-" + driveId;

				String path2 = "-->path-" + path;

				String completeMetaData = driveID + path2;
				
				si.setKeywords(completeMetaData);

				FileOutputStream fileOutputStreamForLabeledfile = new FileOutputStream(labeledFilePath);
				doc.write(fileOutputStreamForLabeledfile);
				fileOutputStreamForLabeledfile.close();
				fileInputStream.close();
				System.out.println("added label for " + name);
			} else {
				System.err.println("Not a office file hence skipping");
			}

		} else {
			System.out.println("No file to download. Server replied HTTP code: " + responseCode);
		}
		httpConn.disconnect();
	}

	@Override
	public String toString() {
		return "MultiDownLoadExecutor [parentReference.getId()=" + parentReference.getId() + ", saveDir=" + saveDir
				+ "]";
	}

}