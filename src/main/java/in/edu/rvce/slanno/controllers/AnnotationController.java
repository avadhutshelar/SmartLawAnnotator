package in.edu.rvce.slanno.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import in.edu.rvce.courtorder.Argument;
import in.edu.rvce.courtorder.JsonCourtOrder;
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
	public RedirectView annotate(SessionMessage message, Model model, @PathVariable Integer projectId) {
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

	@GetMapping("/project/{projectId}/annotate/{docId}")
	public String annotateDocuments(SessionMessage message, Model model, @PathVariable Integer projectId,
			@PathVariable Long docId) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument = projectService.getLegalDocumentByDocumentId(docId);

			JsonCourtOrder jsonCourtOrder = annotationService.getJsonCourtOrder(project, legalDocument);
			String textOrder = jsonCourtOrder.getProcessedText();
			message.setTextOrder(textOrder);
			model.addAttribute("project", project);
			model.addAttribute("legalDocument", legalDocument);
			model.addAttribute("jsonCourtOrder", jsonCourtOrder);
		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return "annotate-document";
	}

	@PostMapping("/project/{projectId}/annotate/{docId}")
	public RedirectView updateJsonCourtOrder(SessionMessage message, Model model, @PathVariable Integer projectId,
			@PathVariable Long docId, JsonCourtOrder jsonCourtOrderIn) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument = projectService.getLegalDocumentByDocumentId(docId);
			JsonCourtOrder jsonCourtOrder = annotationService.getJsonCourtOrder(project, legalDocument);
			//Update argumentBy
			jsonCourtOrder.getArguments().forEach(a -> {
				
				jsonCourtOrderIn.getArguments().forEach(ain -> {
					
					if (a.getArgumentNumber().equals(ain.getArgumentNumber())) {
						
						a.setArgumentBy(ain.getArgumentBy());
						//a.setArgumentSentences(ain.getArgumentSentences());
						a.getArgumentSentences().forEach(s->{
							
							ain.getArgumentSentences().forEach(sin->{
							
								if(s.getSentenceNumber().equals(sin.getSentenceNumber())) {
									s.setArgumentSentenceType(sin.getArgumentSentenceType());
								}
								
							});
							
						});
					}
				
				});
				
			});
			
			jsonCourtOrder.getBackground().getLegalReferences().forEach( a ->{
				jsonCourtOrderIn.getBackground().getLegalReferences().forEach(ain->{
					if(a.getRefNumber().equals(ain.getRefNumber())) {
						a.setLegalRefAcceptRejectDecision(ain.getLegalRefAcceptRejectDecision());
					}
				});
			});
			
			annotationService.saveJsonOrder(project, legalDocument, jsonCourtOrder);

		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return new RedirectView("/project/" + projectId + "/annotate/" + docId);
	}
	
	@GetMapping("/project/{projectId}/annotate/{docId}/prev")
	public RedirectView annotatePreviousDocuments(SessionMessage message, Model model, @PathVariable Integer projectId,
			@PathVariable Long docId) {
		String successMessage = "";
		String errorMessage = "";
		try {
			List<LegalDocument> legalDocumentList = projectService.getAllLegalDocumentByProjectId(projectId);

			List<LegalDocument> annotationDocumentList = legalDocumentList.stream()
					.filter(doc -> doc.getAnnotationProcessingStage().equals(AnnotationProcessingStage.STAGE1))
					.collect(Collectors.toList());

			LegalDocument currentlegalDocument = projectService.getLegalDocumentByDocumentId(docId);
			Integer index = 0;
			for (LegalDocument ld : annotationDocumentList) {
				if (ld.getDocumentId() == currentlegalDocument.getDocumentId()) {
					index = annotationDocumentList.indexOf(ld);
				}
			}
			if (index > 0) {
				LegalDocument prevlegalDocument = annotationDocumentList.get(index - 1);
				docId = prevlegalDocument.getDocumentId();
			} else {
				docId = currentlegalDocument.getDocumentId();
			}

		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return new RedirectView("/project/" + projectId + "/annotate/" + docId);
	}

	@GetMapping("/project/{projectId}/annotate/{docId}/next")
	public RedirectView preProcessNextDocuments(SessionMessage message, Model model, @PathVariable Integer projectId,
			@PathVariable Long docId) {
		String successMessage = "";
		String errorMessage = "";
		try {
			List<LegalDocument> legalDocumentList = projectService.getAllLegalDocumentByProjectId(projectId);

			List<LegalDocument> annotationDocumentList = legalDocumentList.stream()
					.filter(doc -> doc.getAnnotationProcessingStage().equals(AnnotationProcessingStage.STAGE1))
					.collect(Collectors.toList());

			LegalDocument currentlegalDocument = projectService.getLegalDocumentByDocumentId(docId);
			Integer index = 0;
			for (LegalDocument ld : annotationDocumentList) {
				if (ld.getDocumentId() == currentlegalDocument.getDocumentId()) {
					index = annotationDocumentList.indexOf(ld);
				}
			}
			if (index < annotationDocumentList.size()) {
				LegalDocument prevlegalDocument = annotationDocumentList.get(index + 1);
				docId = prevlegalDocument.getDocumentId();
			} else {
				docId = currentlegalDocument.getDocumentId();
			}

		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return new RedirectView("/project/" + projectId + "/annotate/" + docId);
	}
	
	@GetMapping("/project/{projectId}/annotate/{docId}/argument/{argNum}")
	public String getArgument(SessionMessage message, Model model, @PathVariable Integer projectId,
			@PathVariable Long docId,  @PathVariable Integer argNum) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument = projectService.getLegalDocumentByDocumentId(docId);
			JsonCourtOrder jsonCourtOrder = annotationService.getJsonCourtOrder(project, legalDocument);			
			List<Argument> argumentList = jsonCourtOrder.getArguments().stream().filter(arg->arg.getArgumentNumber().equals(argNum)).collect(Collectors.toList());
			
			model.addAttribute("project", project);
			model.addAttribute("legalDocument", legalDocument);
			model.addAttribute("jsonCourtOrder", jsonCourtOrder);
			model.addAttribute("argument", argumentList.get(0));
		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return "annotate-argument";
	}

	@PostMapping("/project/{projectId}/annotate/{docId}/argument/{argNum}")
	public RedirectView updateArgBy(SessionMessage message, Model model, @PathVariable Integer projectId,
			@PathVariable Long docId, @PathVariable Integer argNum, Argument argument) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument = projectService.getLegalDocumentByDocumentId(docId);
			JsonCourtOrder jsonCourtOrder = annotationService.getJsonCourtOrder(project, legalDocument);
			//Update argumentBy
			jsonCourtOrder.getArguments().forEach(a -> {
				if (a.getArgumentNumber().equals(argNum)) {
					a.setArgumentBy(argument.getArgumentBy());
					a.setArgumentSentences(argument.getArgumentSentences());
				}
			});
			
			annotationService.saveJsonOrder(project, legalDocument, jsonCourtOrder);

		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return new RedirectView("/project/" + projectId + "/annotate/" + docId+ "/argument/" + argNum);
	}
}
