package com.onedrive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.hpsf.HPSFPropertiesOnlyDocument;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

public class ReadDriveIdAndPath {

	private String labeledFilePath= null;
	
	private String driveId;

	private String file;
	
	private FileInputStream fileInputStream;
	
	
	private String path;
	
	
	public ReadDriveIdAndPath( FileInputStream fileInputStream,  String file) {
		
		this.fileInputStream = fileInputStream;
		this.file = file;
	}
	
	public  String getDriveIdAndPath()
			throws FileNotFoundException, IOException, DocumentException {
		if (file.endsWith(".pdf") || file.endsWith(".PDF")) {

			System.out.println("inside pdf");

			System.out.println("Reading the file to get metaData " + file);

			
			PdfReader reader = new PdfReader(fileInputStream);

			

			// get and edit meta-data
			HashMap<String, String> info = reader.getInfo();

			driveId = info.get("driveId");

			System.out.println("driveId--->" + driveId);

			path = info.get("path");

			System.out.println("path--->" + path);

			return path;

			

		} else if (file.endsWith(".docx") || file.endsWith(".DOCX")) {

			System.out.println("inside docx");

			System.out.println("Reading the file to get metaData " );
			

			XWPFDocument xWPFDocument = new XWPFDocument(fileInputStream);

			POIXMLProperties propsForDoc = xWPFDocument.getProperties();

			String Category = propsForDoc.getCoreProperties().getCategory();

			System.out.println("Category-->" + Category);

			String[] metaDataArray = Category.split("-->");

			driveId = metaDataArray[0];
			path = metaDataArray[1];

			String[] extractDriveId = driveId.split("-");
			String[] extractpath = path.split("-");
			driveId = extractDriveId[1];
			path = extractpath[1];

			System.out.println("driveId--->" + driveId);

			System.out.println("path--->" + path);

			return path;

		}

		else if (file.endsWith(".xlsx") || file.endsWith(".XLSX")) {

			System.out.println("inside xlsx");

			System.out.println("Reading the file to get metaData " );

		

			XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);

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

			return path;

		}

		else if (file.endsWith(".PPTX") || file.endsWith(".pptx")) {

			System.out.println("inside PPTX");

			System.out.println("Reading the file to get metaData " );

		
			XMLSlideShow ppt = new XMLSlideShow(fileInputStream);

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
			return path;

		}

		else if (file.endsWith(".ppt") || file.endsWith(".PPT")
				|| (file.endsWith(".xls") || file.endsWith(".XLS"))
				|| (file.endsWith(".doc") || file.endsWith(".DOC"))) {

			System.out.println("inside doc,xls,ppt");

			System.out.println("Reading the file to get metaData " );
			

			NPOIFSFileSystem fs = new NPOIFSFileSystem(fileInputStream);

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

			return path;

		} else {
			System.err.println("Not a office file or application supported file hence skipping");
			return null;
		}
}
}