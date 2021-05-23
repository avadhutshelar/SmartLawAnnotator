package in.edu.rvce.slanno.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.thymeleaf.util.StringUtils;

//Reference : https://www.baeldung.com/pdf-conversions-java
public class PDFtoText {

	public String generateTxtFromPDF(String filename) throws IOException {
		File f = new File(filename);
		String parsedText;
		PDFParser parser = new PDFParser(new RandomAccessFile(f, "r"));
		parser.parse();

		COSDocument cosDoc = parser.getDocument();

		PDFTextStripper pdfStripper = new PDFTextStripper();
		PDDocument pdDoc = new PDDocument(cosDoc);

		parsedText = pdfStripper.getText(pdDoc);

		if (cosDoc != null)
			cosDoc.close();
		if (pdDoc != null)
			pdDoc.close();

		return parsedText;
	}
	
	public String generateTxtFromByteArray(byte[] byteArray) throws IOException {
		File f = new File("temp.pdf");
		OutputStream os = new FileOutputStream(f);
		os.write(byteArray);
		os.close();
		String parsedText;
		PDFParser parser = new PDFParser(new RandomAccessFile(f, "r"));
		parser.parse();

		COSDocument cosDoc = parser.getDocument();

		PDFTextStripper pdfStripper = new PDFTextStripper();
		PDDocument pdDoc = new PDDocument(cosDoc);

		parsedText = pdfStripper.getText(pdDoc);

		if (cosDoc != null)
			cosDoc.close();
		if (pdDoc != null)
			pdDoc.close();

		return parsedText;
	}

	/*
	private static final String PDF = "D:/PhD/Dataset/CourtOrders/DistrictandSessionCourtPune/CriBailAppln/2-2019.pdf";
	public static void main(String[] args) {

		try {
			String parsedText = generateTxtFromPDF(PDF);

			PrintWriter pw = new PrintWriter(
					"D:/PhD/Dataset/CourtOrders/DistrictandSessionCourtPune/CriBailAppln/2-2019.txt");
			pw.print(parsedText);
			pw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}*/

	public static void main(String[] args) {
		String folderPath="D:/PhD/Dataset/CourtOrders/DistrictandSessionCourtPune/CriBailAppln";
		List<String> fileList=new ArrayList<String>();
		try (Stream<Path> walk = Files.walk(Paths.get(folderPath),1)) {

			fileList = walk.filter(Files::isRegularFile).map(x -> x.toString())
					.filter(f -> f.endsWith(".pdf")).collect(Collectors.toList());
			
		}catch (Exception e) {
			e.printStackTrace();			
		}
		fileList.forEach((filePath) -> {
			try {					
				byte[] fileBytes=Files.readAllBytes(Paths.get(filePath));			

				PDFtoText pdftotext = new PDFtoText();
				String textCourtOrder = pdftotext.generateTxtFromByteArray(fileBytes);			
				
				PrintWriter pw = new PrintWriter(
						"D:/PhD/Dataset/Inception/SampleDataset/"+StringUtils.replace(Paths.get(filePath).getFileName(),".pdf",".txt"));
				pw.print(textCourtOrder);
				pw.close();
				
			}catch (Exception e) {
				e.printStackTrace();
			}
		});	
	}
	
}
