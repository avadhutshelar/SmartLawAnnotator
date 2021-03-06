package in.edu.rvce.slanno.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import in.edu.rvce.courtorder.JsonCourtOrder;
import in.edu.rvce.slanno.entities.LegalDocument;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.enums.AnnotationProcessingStage;
import in.edu.rvce.slanno.services.AnnotationService;
import in.edu.rvce.slanno.services.ProjectService;
import in.edu.rvce.slanno.utils.SessionMessage;

@Controller
public class ProjectPreProcessDocumentsController {

	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private AnnotationService annotationService;

	
	@GetMapping("/project/{projectId}/preprocess")
	public RedirectView preProcess(SessionMessage message, Model model, @PathVariable Integer projectId) {
		String successMessage = "";
		String errorMessage = "";
		Long docId=null;
		try {			
			List<LegalDocument> legalDocumentList= projectService.getAllLegalDocumentByProjectId(projectId);
			
			//TODO get 1st incomplete document
			LegalDocument legalDocument=legalDocumentList.get(0);
			
			docId=legalDocument.getDocumentId();
			
		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return new RedirectView("/project/"+projectId+"/preprocess/"+docId);
	}
	
	@GetMapping("/project/{projectId}/preprocess/{docId}")
	public String preProcessDocuments(SessionMessage message, Model model, @PathVariable Integer projectId, @PathVariable Long docId) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument= projectService.getLegalDocumentByDocumentId(docId);
			
			String textOrder=projectService.getLegalDocumentProcessedText(project,legalDocument);
			message.setTextOrder(textOrder);
			model.addAttribute("project", project);
			model.addAttribute("legalDocument", legalDocument);
		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);			
		}
		return "project-preprocess-document";
	}
	
	@PostMapping("/project/{projectId}/preprocess/{docId}")
	public String updateDocuments(SessionMessage message, Model model, @PathVariable Integer projectId, @PathVariable Long docId,
			@RequestParam(value = "textOrderHidden", required = true) String textOrderHidden) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument= projectService.getLegalDocumentByDocumentId(docId);
			projectService.saveUpdatedTextOrder(project, legalDocument, textOrderHidden);
			message.setTextOrder(textOrderHidden);
			model.addAttribute("project", project);
			model.addAttribute("legalDocument", legalDocument);
			successMessage="Update Successful";
		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);			
		}
		return "project-preprocess-document";
	}
	
	@PostMapping("/project/{projectId}/preprocess/{docId}/complete")
	public String markPreProcessComplete(SessionMessage message, Model model, @PathVariable Integer projectId, @PathVariable Long docId,
			@RequestParam(value = "textOrderHidden1", required = true) String textOrderHidden1, Authentication authentication) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			if(projectService.getNumberofAnnotatorsForProject(project)>=3) {
				LegalDocument legalDocument= projectService.getLegalDocumentByDocumentId(docId);
				legalDocument.setAnnotationProcessingStage(AnnotationProcessingStage.STAGE1);
				projectService.saveUpdatedTextOrder(project, legalDocument, textOrderHidden1);
				projectService.saveJsonOrder(project, legalDocument, authentication);
				message.setTextOrder(textOrderHidden1);
				model.addAttribute("project", project);
				model.addAttribute("legalDocument", legalDocument);
				successMessage="Marked Completed.";
			}else {
				errorMessage = "Error in completing pre-processing: Please assign minimum 3 annotators for the project";
			}
		} catch (Exception e) {
			errorMessage = "Error in completing pre-processing: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);			
		}
		return "project-preprocess-document";
	}
	
	@PostMapping("/project/{projectId}/preprocess/{docId}/backToPreprocess")
	public String backToPreprocess(SessionMessage message, Model model, @PathVariable Integer projectId, @PathVariable Long docId,
			@RequestParam(value = "textOrderHidden6", required = true) String textOrderHidden6, Authentication authentication) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument= projectService.getLegalDocumentByDocumentId(docId);
			legalDocument.setAnnotationProcessingStage(AnnotationProcessingStage.STAGE0);
			projectService.saveUpdatedTextOrder(project, legalDocument, textOrderHidden6);
			projectService.saveJsonOrder(project, legalDocument, authentication);
			message.setTextOrder(textOrderHidden6);
			model.addAttribute("project", project);
			model.addAttribute("legalDocument", legalDocument);
			successMessage="Back to Pre-Process now";
		} catch (Exception e) {
			errorMessage = "Error in going back to pre-processing: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);			
		}
		return "project-preprocess-document";
	}
	
	@GetMapping("/project/{projectId}/preprocess/{docId}/prev")
	public RedirectView preProcessPreviousDocuments(SessionMessage message, Model model, @PathVariable Integer projectId, @PathVariable Long docId) {
		String successMessage = "";
		String errorMessage = "";
		try {			
			List<LegalDocument> legalDocumentList= projectService.getAllLegalDocumentByProjectId(projectId);
			
			LegalDocument currentlegalDocument= projectService.getLegalDocumentByDocumentId(docId);
			Integer index=0;
			for(LegalDocument ld:legalDocumentList) {
				if(ld.getDocumentId()==currentlegalDocument.getDocumentId()) {
					index = legalDocumentList.indexOf(ld);
				}
			}
			if(index>0) {
				LegalDocument prevlegalDocument=legalDocumentList.get(index-1);
				docId=prevlegalDocument.getDocumentId();
			}else {
				docId=currentlegalDocument.getDocumentId();
			}
			
		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}		
		return new RedirectView("/project/"+projectId+"/preprocess/"+docId);
	}
	
	@GetMapping("/project/{projectId}/preprocess/{docId}/next")
	public RedirectView preProcessNextDocuments(SessionMessage message, Model model, @PathVariable Integer projectId, @PathVariable Long docId) {
		String successMessage = "";
		String errorMessage = "";
		try {
			List<LegalDocument> legalDocumentList= projectService.getAllLegalDocumentByProjectId(projectId);
			
			LegalDocument currentlegalDocument= projectService.getLegalDocumentByDocumentId(docId);
			Integer index=0;
			for(LegalDocument ld:legalDocumentList) {
				if(ld.getDocumentId()==currentlegalDocument.getDocumentId()) {
					index = legalDocumentList.indexOf(ld);
				}
			}
			if(index<legalDocumentList.size()) {
				LegalDocument prevlegalDocument=legalDocumentList.get(index+1);
				docId=prevlegalDocument.getDocumentId();
			}else {
				docId=currentlegalDocument.getDocumentId();
			}
			
		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}		
		return new RedirectView("/project/"+projectId+"/preprocess/"+docId);
	}
	
	@PostMapping("/project/{projectId}/preprocess/{docId}/insertHeaderEnd")
	public String insertHeaderEnd(SessionMessage message, Model model, @PathVariable Integer projectId, @PathVariable Long docId,
			@RequestParam(value = "textOrderHidden2", required = true) String textOrderHidden2) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument= projectService.getLegalDocumentByDocumentId(docId);
			projectService.saveUpdatedTextOrder(project, legalDocument, textOrderHidden2);
			message.setTextOrder(textOrderHidden2);
			model.addAttribute("project", project);
			model.addAttribute("legalDocument", legalDocument);
			successMessage="Update Successful";
		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);			
		}
		return "project-preprocess-document";
	}
	
	@PostMapping("/project/{projectId}/preprocess/{docId}/insertBackgroundEnd")
	public String insertBackgroundEnd(SessionMessage message, Model model, @PathVariable Integer projectId, @PathVariable Long docId,
			@RequestParam(value = "textOrderHidden3", required = true) String textOrderHidden3) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument= projectService.getLegalDocumentByDocumentId(docId);
			projectService.saveUpdatedTextOrder(project, legalDocument, textOrderHidden3);
			message.setTextOrder(textOrderHidden3);
			model.addAttribute("project", project);
			model.addAttribute("legalDocument", legalDocument);
			successMessage="Update Successful";
		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);			
		}
		return "project-preprocess-document";
	}
	
	@PostMapping("/project/{projectId}/preprocess/{docId}/insertArgumentEnd")
	public String insertArgumentEnd(SessionMessage message, Model model, @PathVariable Integer projectId, @PathVariable Long docId,
			@RequestParam(value = "textOrderHidden4", required = true) String textOrderHidden4) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument= projectService.getLegalDocumentByDocumentId(docId);
			projectService.saveUpdatedTextOrder(project, legalDocument, textOrderHidden4);
			message.setTextOrder(textOrderHidden4);
			model.addAttribute("project", project);
			model.addAttribute("legalDocument", legalDocument);
			successMessage="Update Successful";
		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);			
		}
		return "project-preprocess-document";
	}
	
	@PostMapping("/project/{projectId}/preprocess/{docId}/insertOrderEnd")
	public String insertOrderEnd(SessionMessage message, Model model, @PathVariable Integer projectId, @PathVariable Long docId,
			@RequestParam(value = "textOrderHidden5", required = true) String textOrderHidden5) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument= projectService.getLegalDocumentByDocumentId(docId);
			projectService.saveUpdatedTextOrder(project, legalDocument, textOrderHidden5);
			message.setTextOrder(textOrderHidden5);
			model.addAttribute("project", project);
			model.addAttribute("legalDocument", legalDocument);
			successMessage="Update Successful";
		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);			
		}
		return "project-preprocess-document";
	}

	@GetMapping("/project/{projectId}/veiwJson/{docId}")
	public String viewJson(SessionMessage message, Model model, @PathVariable Integer projectId, @PathVariable Long docId, Authentication authentication) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument= projectService.getLegalDocumentByDocumentId(docId);
			if (!legalDocument.getAnnotationProcessingStage().equals(AnnotationProcessingStage.STAGE0)) {				
				JsonCourtOrder jsonCourtOrder = annotationService.getJsonCourtOrder(project, legalDocument, authentication.getName());

				Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
				String json = "";
				try{
					json=gson.toJson(jsonCourtOrder);
				} catch (Exception e) {
					e.printStackTrace();
				}
				message.setTextOrder(json);
			}
			model.addAttribute("project", project);
			model.addAttribute("legalDocument", legalDocument);
		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);			
		}
		return "project-json-viewer";
	}
}
