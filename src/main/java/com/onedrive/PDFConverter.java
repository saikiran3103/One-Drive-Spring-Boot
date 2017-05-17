package com.onedrive;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.snowtide.PDF;
import com.snowtide.pdf.Document;
import com.snowtide.pdf.OutputTarget;

public class PDFConverter implements Runnable {
	

	final static Logger logger = Logger.getLogger(PDFConverter.class);
	
	private File officefile;
	private String originalFolderName;

	public PDFConverter(File officefile, String originalFolderName) {
		this.originalFolderName=originalFolderName;
		this.officefile = officefile;
	}
	
	@Override
	public void run()  {
		
		
		logger.info("converting the file _"+officefile.getAbsolutePath());
		
		

		
		
		
		int nameIndex=officefile.getName().lastIndexOf(".");
	
		String textNaming1=officefile.getName().substring(0, nameIndex);
		
		textNaming1.concat(".txt");

		
		
		 File f = new File(officefile.getPath());
		    String path = f.getParent();
		    String textFolderName =path.substring(path.lastIndexOf("\\")+1,path.length()); 
		    
		File textdirectory= new File(officefile.getParent()+"\\"+textFolderName+" TextFolder\\");
		textdirectory.mkdir();
		int index = officefile.getAbsolutePath().lastIndexOf(".");
		String textdirectoryString =textdirectory.getPath()+"\\"+textNaming1.concat(".txt");   

		System.out.println("officefile.getAbsolutePath().substring(index)"+officefile.getAbsolutePath().substring(index));

		final String FILENAME = textdirectoryString;
		if (!officefile.exists()) {
			System.out.println("Sorry does not Exists!");
		}
		
		
		
		 Document pdf = PDF.open(officefile);
		    StringBuilder text = new StringBuilder(1024);
		    pdf.pipe(new OutputTarget(text));
		    try {
				pdf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    System.out.println(text);
		    
		    try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME,true))) {



				bw.write(text.toString());

				// no need to close it.
				//bw.close();

				System.out.println("Done");

			} catch (IOException e) {
				logger.error(" error occured while extracting and converting   "+officefile.getAbsolutePath()+"     " + e.getMessage());

				e.printStackTrace();

			}
		    
		    
	}	
}
