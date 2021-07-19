package in.edu.rvce.slanno.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import in.edu.rvce.slanno.entities.LegalDocument;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.enums.AnnotationProcessingStage;
import in.edu.rvce.slanno.services.AnnotationService;
import in.edu.rvce.slanno.services.ProjectService;
import in.edu.rvce.slanno.utils.SessionMessage;

@Controller
public class AnnotationController {

	@Autowired
	private ProjectService projectService;

	@Autowired
	private AnnotationService annotationService;

	@GetMapping("/project/{projectId}/annotate")
	public RedirectView preProcess(SessionMessage message, Model model, @PathVariable Integer projectId) {
		String successMessage = "";
		String errorMessage = "";
		Long docId = null;
		try {
			List<LegalDocument> legalDocumentList = projectService.getAllLegalDocumentByProjectId(projectId);
			List<LegalDocument> annotationDocumentList = legalDocumentList.stream()
					.filter(doc -> doc.getAnnotationProcessingStage().equals(AnnotationProcessingStage.STAGE1))
					.collect(Collectors.toList());

			// TODO get 1st incomplete document
			LegalDocument legalDocument = annotationDocumentList.get(0);

			docId = legalDocument.getDocumentId();

		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return new RedirectView("/project/" + projectId + "/annotate/" + docId);
	}


	@GetMapping(value = "/project/{projectId}/annotate/{docId}", produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView annotateDocuments(SessionMessage message, Model model, @PathVariable Integer projectId,
			@PathVariable Long docId) {
		ModelAndView modelAndView = new ModelAndView("annotate-document");
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument = projectService.getLegalDocumentByDocumentId(docId);

			String textOrder = projectService.getLegalDocumentProcessedText(project, legalDocument);
			String styledtextOrder=annotationService.getStyledTextOrder(textOrder);
			message.setStyledtextOrder(styledtextOrder);
			modelAndView.addObject("project", project);
			modelAndView.addObject("legalDocument", legalDocument);
		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			modelAndView.addObject("message", message);
		}		
		return modelAndView;
	}
	
	@PostMapping("/project/{projectId}/addAnnotation/{docId}")
	public RedirectView addAnnotation(SessionMessage message, Model model, @PathVariable Integer projectId, @PathVariable Long docId,
			@RequestParam(value = "selectedText", required = true) String selectedText,
			@RequestParam(value = "selectedAnnotation", required = true) String selectedAnnotation) {
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument= projectService.getLegalDocumentByDocumentId(docId);
			
			String textOrder = projectService.getLegalDocumentProcessedText(project, legalDocument);
			String styledtextOrder=annotationService.getStyledTextOrder(textOrder);
			message.setStyledtextOrder(styledtextOrder);
			
			System.out.println(selectedText+selectedAnnotation);		
		} catch (Exception e) {
			
		}
		return new RedirectView("/project/"+projectId+"/annotate/"+docId);
	}
}
