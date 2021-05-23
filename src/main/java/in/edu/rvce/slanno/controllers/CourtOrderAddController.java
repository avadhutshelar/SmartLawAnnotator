package in.edu.rvce.slanno.controllers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import in.edu.rvce.slanno.entities.CourtOrder;
import in.edu.rvce.slanno.repositories.CourtOrderRepository;
import in.edu.rvce.slanno.utils.CommonUtils;
import in.edu.rvce.slanno.utils.PDFtoText;
import in.edu.rvce.slanno.utils.SessionMessage;

@Controller
public class CourtOrderAddController {

	@Autowired
	private Environment env;

	@Autowired
	private CourtOrderRepository courtOrderRepository;

	@GetMapping("/addCourtOrder")
	public String addCourtOrder(CourtOrder courtOrder, SessionMessage message, Model model) {
		message.setSuccessMessage("");
		message.setErrorMessage("");
		model.addAttribute("message", message);
		return "addCourtOrder";
	}

	@GetMapping("/addAllCourtOrders")
	public String addAllCourtOrders(SessionMessage message, Model model) {
		String applicationType=env.getProperty("courtorders.applicationType");
		String pdfFilesFolderPath = env.getProperty("courtorders.pdfFiles.folderPath") + "\\"
				+ applicationType;
		model.addAttribute("pdfFilesFolderPath", pdfFilesFolderPath);
		message.setSuccessMessage("");
		message.setErrorMessage("");
		model.addAttribute("message", message);
		return "addAllCourtOrders";
	}

	@PostMapping("/addAllCourtOrders")
	public String addAllCourtOrders(SessionMessage message, BindingResult result, Model model) throws Exception {

		String applicationType=env.getProperty("courtorders.applicationType");
		String pdfFilesFolderPath = env.getProperty("courtorders.pdfFiles.folderPath") + "\\"
				+ applicationType;

		List<String> fileList = new ArrayList<String>();
		try (Stream<Path> walk = Files.walk(Paths.get(pdfFilesFolderPath), 1)) {

			fileList = walk.filter(Files::isRegularFile).map(x -> x.toString()).filter(f -> f.endsWith(".pdf"))
					.collect(Collectors.toList());

		} catch (Exception e) {
			// e.printStackTrace();
			if (fileList.size() <= 0) {
				model.addAttribute("pdfFilesFolderPath", pdfFilesFolderPath);
				message.setSuccessMessage("");
				message.setErrorMessage("No PDF files present at: " + pdfFilesFolderPath);
				model.addAttribute("message", message);
				return "addAllCourtOrders";
			}
			message.setSuccessMessage("");
			message.setErrorMessage(e.getMessage());
			model.addAttribute("message", message);
			return "addAllCourtOrders";
		}

		fileList.forEach((filePath) -> {
			try {
				byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
				CourtOrder courtOrder = new CourtOrder();
				courtOrder.setPdfOrder(fileBytes);

				PDFtoText pdftotext = new PDFtoText();
				String textCourtOrder = pdftotext.generateTxtFromByteArray(fileBytes);
				courtOrder.setRawTextOrder(CommonUtils.removeUnnecessaryCharacters(textCourtOrder));

				courtOrderRepository.save(courtOrder);

				String temp = filePath;
				String archivePath = StringUtils.replace(temp, applicationType, applicationType+"\\archive\\");
				Files.move(Paths.get(filePath), Paths.get(archivePath),
						java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		model.addAttribute("pdfFilesFolderPath", pdfFilesFolderPath);
		message.setSuccessMessage("Court Orders Uploaded successfully");
		message.setErrorMessage("");
		model.addAttribute("message", message);
		return "addAllCourtOrders";
	}
}
