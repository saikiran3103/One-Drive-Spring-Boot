package com.onedrive;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import com.snowtide.PDF;
import com.snowtide.pdf.Document;
import com.snowtide.pdf.OutputTarget;

public class ParallelConverter implements Runnable {

	final static Logger logger = Logger.getLogger(ParallelConverter.class);

	StringBuffer statusOfAllFiles= null;
	
	private String home = System.getProperty("user.home");
	private File officefile;

	private String originalFolderName;

	public ParallelConverter(File officefile, String originalFolderName) {
		this.officefile = officefile;
		this.originalFolderName = originalFolderName;
	}

	@Override
	public void run() {

		ParallelConverter.officeFilesConverter(officefile, originalFolderName);
	}

	private static void officeFilesConverter(File officefile, String originalFolderName) {

		try {
			
			System.out.println("Reading file " + officefile.getName());
			officefile.getName();
			logger.info("converting the file _" + officefile.getAbsolutePath());

			int nameIndex = officefile.getName().lastIndexOf(".");

			String textNaming1 = officefile.getName().substring(0, nameIndex);

			textNaming1.concat(".txt");

			// File f = new File(officefile.getPath());
			String path = officefile.getParent();
			//String textFolderName = path.substring(path.lastIndexOf("\\") + 1, path.length());

			File textdirectory = new File(officefile.getParent());
			textdirectory.mkdir();
			int index = officefile.getAbsolutePath().lastIndexOf(".");
			String textdirectoryString = textdirectory.getPath() + File.separator+ textNaming1.concat(".txt");

			StringBuilder seperateTextFolderBuilder = new StringBuilder(textdirectoryString);

			int indexToAppendTextFolderName = path.lastIndexOf("Downloads") + 10;

			textdirectoryString = seperateTextFolderBuilder
					.insert(indexToAppendTextFolderName, "TextFolder" + File.separator).toString();

			int indexFortextDirectory = textdirectoryString.lastIndexOf(File.separator);

			String textfolderExtract = textdirectoryString.substring(0, indexFortextDirectory);

			File textDirectory = new File(textfolderExtract);
			textDirectory.mkdirs();

			System.out.println(
					"officefile.getAbsolutePath().substring(index)" + officefile.getAbsolutePath().substring(index));

			final String FILENAME = textdirectoryString;
			if (!officefile.exists()) {
				System.out.println("Sorry does not Exists!");
			} else {
				if (officefile.getName().endsWith(".docx") || officefile.getName().endsWith(".DOCX")) {
					// FileInputStream in=new FileInputStream(officefile);

					String content;
					try (XWPFWordExtractor xWPFWordExtractor = new XWPFWordExtractor(
							new XWPFDocument(new FileInputStream(officefile)))) {

						content = xWPFWordExtractor.getText();
						// Content = (new XWPFWordExtractor(new XWPFDocument(new
						// FileInputStream(officefile))).getText());
						if (content.isEmpty()) {
							logger.error("content for the file-->" + officefile.getAbsolutePath() + "_ is empty");
						}

						System.out.println(" This is the content of the file " + officefile.getName() + content);

						try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME))) {

							bw.write(content);

							// no need to close it.
							// bw.close();

							System.out.println("Done");

						} catch (IOException e) {
							logger.error(" error occured while extracting and converting   "
									+ officefile.getAbsolutePath() + "     " + e.getMessage());
							e.printStackTrace();

						}
					} catch (IOException e1) {

						logger.error(" error occured while extracting and converting   " + officefile.getAbsolutePath()
								+ "     " + e1.getMessage());
						e1.printStackTrace();

					}

					// XWPFDocument doc = new XWPFDocument(in);
					// XWPFWordExtractor ex = new XWPFWordExtractor(doc);
					// out.write(ex.getText());
					// out.write(new XWPFWordExtractor(new XWPFDocument(new
					// FileInputStream(officefile))).getText());

				}

				else if (officefile.getName().endsWith(".doc") || officefile.getName().endsWith(".DOC")) {
					// FileInputStream in=new FileInputStream(officefile);
					try (WordExtractor wd = new WordExtractor(new FileInputStream(officefile))) {
						// WordExtractor wd= new WordExtractor(new
						// FileInputStream(officefile));

						// wd = new WordExtractor(new
						// FileInputStream(officefile));

						String content = wd.getText();

						if (content.isEmpty()) {
							logger.error("content for the file-->" + officefile.getAbsolutePath() + "_ is empty");
						}

						try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME))) {

							bw.write(content);

							// no need to close it.
							// bw.close();

							System.out.println("Done");

						} catch (IOException e) {
							logger.error(" error occured while extracting and converting   "
									+ officefile.getAbsolutePath() + "     " + e.getMessage());
							e.printStackTrace();

						}

						// XWPFDocument doc = new XWPFDocument(in);
						// XWPFWordExtractor ex = new XWPFWordExtractor(doc);
						// out.write(ex.getText());
						// out.write(new XWPFWordExtractor(new XWPFDocument(new
						// FileInputStream(officefile))).getText());
					} catch (IOException e) {
						logger.error(" error occured while extracting and converting   " + officefile.getAbsolutePath()
								+ "     " + e.getMessage());
						e.printStackTrace();

					}
				} else if (officefile.getName().endsWith(".xlsx") || officefile.getName().endsWith(".XLSX")) {
					String content;
					try (XSSFExcelExtractor xSSFExcelExtractor = new XSSFExcelExtractor(
							new XSSFWorkbook(new FileInputStream(officefile)))) {

						content = xSSFExcelExtractor.getText();
						if (content.isEmpty()) {
							logger.error("content for the file-->" + officefile.getAbsolutePath() + "_ is empty");
						}

						// Content = new XSSFExcelExtractor(new XSSFWorkbook(new
						// FileInputStream(officefile))).getText();

						try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME))) {

							bw.write(content);

							// no need to close it.
							// bw.close();

							System.out.println("Done");

						} catch (IOException e) {
							logger.error(" error occured while extracting and converting   "
									+ officefile.getAbsolutePath() + "     " + e.getMessage());
							e.printStackTrace();

						}
					} catch (IOException e1) {
						logger.error(" error occured while extracting and converting   " + officefile.getAbsolutePath()
								+ "     " + e1.getMessage());
						e1.printStackTrace();
					}
				}

				else if (officefile.getName().endsWith(".xls") || officefile.getName().endsWith(".XLS")) {

					try (ExcelExtractor wd = new ExcelExtractor(new HSSFWorkbook(new FileInputStream(officefile)))) {
						// wd = new ExcelExtractor(new HSSFWorkbook(new
						// FileInputStream(officefile)));

						String content = wd.getText();
						wd.close();

						if (content.isEmpty()) {
							logger.error("content for the file-->" + officefile.getAbsolutePath() + "_ is empty");
						}
						try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME))) {

							bw.write(content);

							// no need to close it.
							// bw.close();

							System.out.println("Done");

						} catch (IOException e) {
							logger.error(" error occured while extracting and converting   "
									+ officefile.getAbsolutePath() + "     " + e.getMessage());
							e.printStackTrace();

						}
					} catch (IOException e1) {
						logger.error(" error occured while extracting and converting   " + officefile.getAbsolutePath()
								+ "     " + e1.getMessage());
						e1.printStackTrace();
					}
				} else if (officefile.getName().endsWith(".PPTX") || officefile.getName().endsWith(".pptx")) {

					try {
						XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(officefile));

						CoreProperties props = ppt.getProperties().getCoreProperties();
						String title = props.getTitle();
						System.out.println("Title: " + title);

						for (XSLFSlide slide : ppt.getSlides()) {
							System.out.println("Starting slide...");
							XSLFShape[] shapes = slide.getShapes();

							for (XSLFShape shape : shapes) {
								if (shape instanceof XSLFTextShape) {
									XSLFTextShape textShape = (XSLFTextShape) shape;
									String content = " " + textShape.getText();
									if (content.isEmpty()) {
										logger.error("content for the file-->" + officefile.getAbsolutePath()
												+ "_ is empty");
									}
									System.out.println("Text: " + content);
									try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME, true))) {

										bw.write(content);

										// no need to close it.
										// bw.close();

										System.out.println("Done");

									} catch (IOException e) {
										logger.error(" error occured while extracting and converting   "
												+ officefile.getAbsolutePath() + "     " + e.getMessage());
										e.printStackTrace();

									}

								}
							}

						}
					} catch (IOException e1) {
						logger.error(" error occured while extracting and converting   " + officefile.getAbsolutePath()
								+ "     " + e1.getMessage());
						e1.printStackTrace();
					}
				}

				else if (officefile.getName().endsWith(".ppt") || officefile.getName().endsWith(".PPT")) {
					try {
						InputStream fis = new FileInputStream(officefile);
						HSLFSlideShow show = new HSLFSlideShow(fis);
						SlideShow ss = new SlideShow(show);

						org.apache.poi.hslf.model.Slide[] slides = ss.getSlides();
						StringBuilder builder = new StringBuilder();
						for (int x = 0; x < slides.length; x++) {
							TextRun[] runs = slides[x].getTextRuns();
							for (int j = 0; j < runs.length; j++) {
								TextRun run = runs[j];
								if (run != null) {
									String content = " " + run.getText();
									if (content.isEmpty()) {
										logger.error("content for the file-->" + officefile.getAbsolutePath()
												+ "_ is empty");
									}
									try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME, true))) {

										bw.write(content);

										// no need to close it.
										// bw.close();

										System.out.println("Done");

									} catch (IOException e) {
										logger.error(" error occured while extracting and converting   "
												+ officefile.getAbsolutePath() + "     " + e.getMessage());
										e.printStackTrace();

									}
									builder.append(content);
									System.out.println(content);
								}
							}
						}
					}

					catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (officefile.getName().endsWith(".pdf") || officefile.getName().endsWith(".PDF")) {
					// use file.renameTo() to rename the file

					// PdfReader pdfReader;
					// try(FileInputStream inputStream = new FileInputStream(new
					// File(officefile.getAbsolutePath()))) {
					//
					//
					//
					//
					//
					// pdfReader = new PdfReader(inputStream);
					//
					// inputStream.close();
					// PdfReaderContentParser parser = new
					// PdfReaderContentParser(pdfReader);
					//
					// TextExtractionStrategy strategy;
					//
					//
					//
					// for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
					// strategy = parser.processContent(i, new
					// SimpleTextExtractionStrategy());
					// System.out.println("strategy.getResultantText()"+strategy.getResultantText());
					// String content=(strategy.getResultantText());
					// pdfReader.close();
					//
					// if(content.isEmpty()){
					// logger.error("content for the
					// file-->"+officefile.getAbsolutePath()+"_ is empty");
					// }
					// try (BufferedWriter bw = new BufferedWriter(new
					// FileWriter(FILENAME,true))) {
					//
					//
					//
					// bw.write(content);
					//
					// // no need to close it.
					// //bw.close();
					//
					// System.out.println("Done");
					//
					// } catch (IOException e) {
					// logger.error(" error occured while extracting and
					// converting "+officefile.getAbsolutePath()+" " +
					// e.getMessage());
					//
					// e.printStackTrace();
					//
					// }
					// pdfReader.close();

					// pdfReader.close();
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

					try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME, true))) {

						bw.write(text.toString());

						// no need to close it.
						// bw.close();

						System.out.println("Done");

					}

					catch (IOException e1) {
						logger.error(" error occured while extracting and converting   " + officefile.getAbsolutePath()
								+ "     " + e1.getMessage());
						Thread.currentThread().interrupt();

						e1.printStackTrace();
						return;
					}

				}
			}

		} catch (Exception common) {
			logger.error(" error occured while extracting and converting   " + officefile.getAbsolutePath() + "     "
					+ common.getMessage());
			common.printStackTrace();
		} finally {

		}
	}
}
