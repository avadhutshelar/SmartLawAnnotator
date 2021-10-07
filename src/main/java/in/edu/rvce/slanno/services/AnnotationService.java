package in.edu.rvce.slanno.services;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import in.edu.rvce.courtorder.JsonCourtOrder;
import in.edu.rvce.slanno.entities.LegalDocument;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.repositories.LegalDocumentRepository;

@Service
public class AnnotationService {

	@Autowired
	private Environment env;

	@Autowired
	private LegalDocumentRepository legalDocumentRepository;

	public JsonCourtOrder getJsonCourtOrder(Project project, LegalDocument legalDocument) {
		JsonCourtOrder jsonCourtOrder = new JsonCourtOrder();

		String jsonText = "";

		String processedTextFileNameWithPath = env.getProperty("slanno.dataset.basedir") + "\\"
				+ project.getProjectDirectoryName() + "\\" + legalDocument.getJsonFilePath();

		try {
			jsonText = new String(Files.readAllBytes(Paths.get(processedTextFileNameWithPath)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		// File to Java objects
		try {
			jsonCourtOrder = gson.fromJson(jsonText, JsonCourtOrder.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jsonCourtOrder;
	}

	public void saveJsonOrder(Project project, LegalDocument legalDocument, JsonCourtOrder jsonCourtOrder) {
		try {

			legalDocument.setJsonFilePath(
					env.getProperty("slanno.dataset.dir.json") + "\\" + legalDocument.getDocumentId() + ".json");

			String jsonFileNameWithPath = env.getProperty("slanno.dataset.basedir") + "\\"
					+ project.getProjectDirectoryName() + "\\" + legalDocument.getJsonFilePath();

			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			// Java objects to File
			try (FileWriter writer = new FileWriter(jsonFileNameWithPath)) {
				gson.toJson(jsonCourtOrder, writer);
			} catch (IOException e) {
				e.printStackTrace();
			}

			legalDocumentRepository.save(legalDocument);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
