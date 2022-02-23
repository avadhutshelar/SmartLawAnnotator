package in.edu.rvce.slanno.services;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import in.edu.rvce.courtorder.JsonCourtOrder;
import in.edu.rvce.slanno.entities.LegalDocument;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.enums.AnnotationProcessingStage;
import in.edu.rvce.slanno.repositories.LegalDocumentRepository;

@Service
public class AnnotationService {

	@Autowired
	private Environment env;

	@Autowired
	private LegalDocumentRepository legalDocumentRepository;
	
	@Autowired
	private LegalReferenceAnnotationService legalReferenceService;
	
	@Autowired
	private ArgumentAnnotationService argumentAnnotationService;
	
	@Autowired
	private OrderAnnotationService orderAnnotationService;
	
	@Autowired
	AnnotationProcessingStageService annotationProcessingStageService;


	public JsonCourtOrder getJsonCourtOrder(Project project, LegalDocument legalDocument, Authentication authentication) {
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
			legalReferenceService.getUpdatedLegalRefsByUser(jsonCourtOrder, authentication);
			argumentAnnotationService.getUpdatedArgumentsByUser(jsonCourtOrder, authentication);
			orderAnnotationService.getUpdatedOrderByUser(jsonCourtOrder,authentication);
			if(CollectionUtils.isNotEmpty(jsonCourtOrder.getAnnotationProcessingStageAnnotations())) {
				annotationProcessingStageService.getUpdatedAnnotationProcessingStageByUser(jsonCourtOrder, authentication);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jsonCourtOrder;
	}

	public void saveJsonOrder(Project project, LegalDocument legalDocument, JsonCourtOrder jsonCourtOrder, Authentication authentication) {
		try {

			annotationProcessingStageService.setAnnotationProcessingStage(jsonCourtOrder, project, authentication, legalDocument.getAnnotationProcessingStage());
			//update recalculated annotation processing stage
			legalDocument.setAnnotationProcessingStage(jsonCourtOrder.getAnnotationProcessingStage());
			
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
	
	public Integer calculateDocsAssigned(Project project, Authentication authentication) {
		
		
		List<LegalDocument> tempLegalDocumentList = Lists.newArrayList(legalDocumentRepository.findAll());
		List<LegalDocument> legalDocumentList = tempLegalDocumentList.stream()
				.filter(legDoc -> legDoc.getProject().getProjectId() == project.getProjectId()).collect(Collectors.toList());
		
		Integer totalDocsAssigned = legalDocumentList.size();
		return totalDocsAssigned;
	}
	
	public Integer calculateDocsPending(Project project, Authentication authentication) {
		
		
		List<LegalDocument> tempLegalDocumentList = Lists.newArrayList(legalDocumentRepository.findAll());
		List<LegalDocument> legalDocumentList = tempLegalDocumentList.stream()
				.filter(legDoc -> 
					legDoc.getProject().getProjectId() == project.getProjectId()
					&& (legDoc.getAnnotationProcessingStage().equals(AnnotationProcessingStage.STAGE0)
							|| legDoc.getAnnotationProcessingStage().equals(AnnotationProcessingStage.STAGE1))
						).collect(Collectors.toList());
		
	
		Integer totalDocsPending = legalDocumentList.size();
		
		return totalDocsPending;
	}
	
	public Integer calculateDocsComplete(Project project, Authentication authentication) {
		
		
		List<LegalDocument> tempLegalDocumentList = Lists.newArrayList(legalDocumentRepository.findAll());
		List<LegalDocument> legalDocumentList = tempLegalDocumentList.stream()
				.filter(legDoc -> 
					legDoc.getProject().getProjectId() == project.getProjectId()
					&& legDoc.getAnnotationProcessingStage().equals(AnnotationProcessingStage.STAGE2))
				.collect(Collectors.toList());
		
	
		Integer totalDocsComplete = legalDocumentList.size();
		
		return totalDocsComplete;
	}

}
