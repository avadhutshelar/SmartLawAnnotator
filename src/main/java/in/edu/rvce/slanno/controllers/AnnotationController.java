package in.edu.rvce.slanno.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import in.edu.rvce.courtorder.Argument;
import in.edu.rvce.courtorder.JsonCourtOrder;
import in.edu.rvce.slanno.dto.AnnotationTaskDto;
import in.edu.rvce.slanno.dto.LegalActFound;
import in.edu.rvce.slanno.entities.LegalDocument;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.enums.AnnotationProcessingStage;
import in.edu.rvce.slanno.services.AnnotationService;
import in.edu.rvce.slanno.services.ArgumentAnnotationService;
import in.edu.rvce.slanno.services.LegalReferenceAnnotationService;
import in.edu.rvce.slanno.services.OrderAnnotationService;
import in.edu.rvce.slanno.services.ProjectService;
import in.edu.rvce.slanno.utils.SessionMessage;

@Controller
public class AnnotationController {

	@Autowired
	private ProjectService projectService;

	@Autowired
	private AnnotationService annotationService;
	
	@Autowired
	private LegalReferenceAnnotationService legalReferenceService;

	@Autowired
	private ArgumentAnnotationService argumentAnnotationService;
	
	@Autowired
	private OrderAnnotationService orderAnnotationService;

	@GetMapping("/annotationTask")
	public String annotationTask(SessionMessage message, Model model, Authentication authentication) {
		String successMessage = "";
		String errorMessage = "";
		try {
			List<Project> projectList = projectService.getAllProjects();
			List<AnnotationTaskDto> annotationTaskDtoList = new ArrayList<>();
			projectList.forEach(project->{
				String annotatorUserListString = project.getAnnotatorUserListString();
				List<String> annotatorUserList= Arrays.asList(annotatorUserListString.split(","));
				annotatorUserList.forEach(username->{
					if(StringUtils.equalsIgnoreCase(username, authentication.getName())) {
						AnnotationTaskDto annotationTaskDto = new AnnotationTaskDto();
						annotationTaskDto.setProject(project);
						annotationTaskDto.setTotalDocsAssigned(""+annotationService.calculateDocsAssigned(project, authentication));
						annotationTaskDto.setTotalDocsPending(""+annotationService.calculateDocsPending(project, authentication));
						annotationTaskDto.setTotalDocsComplete(""+annotationService.calculateDocsComplete(project, authentication));
						annotationTaskDtoList.add(annotationTaskDto);
					}
				});
			});

			if(CollectionUtils.isEmpty(annotationTaskDtoList)) {
				errorMessage = "No active annotation task for user - "+authentication.getName()+". Contact admin.";
			}else {
				model.addAttribute("annotationTaskDtoList",annotationTaskDtoList);
			}
		} catch (Exception e) {
			errorMessage = "Error in retriving the annotation task: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return "annotationTask";
	}
	
	@GetMapping("/project/{projectId}/annotate")
	public RedirectView annotate(SessionMessage message, Model model, @PathVariable Integer projectId,
			Authentication authentication) {
		String successMessage = "";
		String errorMessage = "";
		Long docId = null;
		try {
			Project project = projectService.getProjectById(projectId);
			List<LegalDocument> annotationDocumentList = annotationService.getAnnotationDocumentListByUser(project, authentication);
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
			@PathVariable Long docId, Authentication authentication) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument = projectService.getLegalDocumentByDocumentId(docId);
			if (!legalDocument.getAnnotationProcessingStage().equals(AnnotationProcessingStage.STAGE0)) {
				JsonCourtOrder jsonCourtOrder = annotationService.getJsonCourtOrder(project, legalDocument, authentication.getName());
				String textOrder = jsonCourtOrder.getProcessedText();
				message.setTextOrder(textOrder);
				model.addAttribute("jsonCourtOrder", jsonCourtOrder);
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
		return "annotate-document";
	}

	@PostMapping("/project/{projectId}/annotate/{docId}")
	public RedirectView updateJsonCourtOrder(SessionMessage message, Model model, @PathVariable Integer projectId,
			@PathVariable Long docId, JsonCourtOrder jsonCourtOrderIn, Authentication authentication) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument = projectService.getLegalDocumentByDocumentId(docId);
			JsonCourtOrder jsonCourtOrder = annotationService.getJsonCourtOrder(project, legalDocument, authentication.getName());

			//Update Legal Reference
			legalReferenceService.updateLegalRefsByUser(jsonCourtOrder, jsonCourtOrderIn, authentication);
			
			//Update argumentBy
			argumentAnnotationService.updateArgumentsByUser(jsonCourtOrder, jsonCourtOrderIn, authentication);
			
			//Update Order			
			orderAnnotationService.updateOrderByUser(jsonCourtOrder, jsonCourtOrderIn, authentication);
			
			annotationService.saveJsonOrder(project, legalDocument, jsonCourtOrder, authentication);

		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return new RedirectView("/project/" + projectId + "/annotate/" + docId);
	}
	
	@PostMapping("/project/{projectId}/annotate/{docId}/complete")
	public RedirectView markAnnotationComplete(SessionMessage message, Model model, @PathVariable Integer projectId,
			@PathVariable Long docId, JsonCourtOrder jsonCourtOrderIn, Authentication authentication) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument = projectService.getLegalDocumentByDocumentId(docId);
			JsonCourtOrder jsonCourtOrder = annotationService.getJsonCourtOrder(project, legalDocument, authentication.getName());
			
			legalDocument.setAnnotationProcessingStage(AnnotationProcessingStage.STAGE2);
			
			annotationService.saveJsonOrder(project, legalDocument, jsonCourtOrder, authentication);
			
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
		return new RedirectView("/project/" + projectId + "/annotate/" + docId);
	}
	
	@PostMapping("/project/{projectId}/annotate/{docId}/backToAnnotation")
	public String backToAnnotation(SessionMessage message, Model model, @PathVariable Integer projectId,
			@PathVariable Long docId, JsonCourtOrder jsonCourtOrderIn, Authentication authentication) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument = projectService.getLegalDocumentByDocumentId(docId);
			JsonCourtOrder jsonCourtOrder = annotationService.getJsonCourtOrder(project, legalDocument, authentication.getName());
			
			legalDocument.setAnnotationProcessingStage(AnnotationProcessingStage.STAGE1);
			
			annotationService.saveJsonOrder(project, legalDocument, jsonCourtOrder, authentication);
			
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
	
	@GetMapping("/project/{projectId}/annotate/{docId}/prev")
	public RedirectView annotatePreviousDocuments(SessionMessage message, Model model, @PathVariable Integer projectId,
			@PathVariable Long docId, Authentication authentication) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			List<LegalDocument> annotationDocumentList = annotationService.getAnnotationDocumentListByUser(project, authentication);
			

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
			@PathVariable Long docId, Authentication authentication) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			List<LegalDocument> annotationDocumentList = annotationService.getAnnotationDocumentListByUser(project, authentication);
			
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
			@PathVariable Long docId,  @PathVariable Integer argNum, Authentication authentication) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument = projectService.getLegalDocumentByDocumentId(docId);
			JsonCourtOrder jsonCourtOrder = annotationService.getJsonCourtOrder(project, legalDocument, authentication.getName());			
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
			@PathVariable Long docId, @PathVariable Integer argNum, Argument argument, JsonCourtOrder jsonCourtOrderIn, Authentication authentication) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument = projectService.getLegalDocumentByDocumentId(docId);
			JsonCourtOrder jsonCourtOrder = annotationService.getJsonCourtOrder(project, legalDocument, authentication.getName());
			//Update argumentBy
			jsonCourtOrder.getArguments().forEach(a -> {
				if (a.getArgumentNumber().equals(argNum)) {
					a.setArgumentBy(argument.getArgumentBy());
					a.setArgumentSentences(argument.getArgumentSentences());
				}
			});
			
			annotationService.saveJsonOrder(project, legalDocument, jsonCourtOrder, authentication);

		} catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return new RedirectView("/project/" + projectId + "/annotate/" + docId+ "/argument/" + argNum);
	}
	
	@PostMapping("/project/{projectId}/annotate/{docId}/legalReference/add")
	public RedirectView addLegalReference(SessionMessage message, Model model, @PathVariable Integer projectId,
			@PathVariable Long docId, JsonCourtOrder jsonCourtOrderIn, Authentication authentication,
			@RequestParam(value = "actNameMatched", required = true) String actNameMatched,
			@RequestParam(value = "sectionsMatched", required = true) String sectionsMatched,
			@RequestParam(value = "stringMatched", required = true) String stringMatched) {
	
		String successMessage = "";
		String errorMessage = "";		
		try {
			Project project = projectService.getProjectById(projectId);
			LegalDocument legalDocument = projectService.getLegalDocumentByDocumentId(docId);
			JsonCourtOrder jsonCourtOrderTemp = annotationService.getJsonCourtOrder(project, legalDocument, authentication.getName());
			
			LegalActFound legalActFound = new LegalActFound();
			legalActFound.setActNameMatched(actNameMatched);
			legalActFound.setSectionsMatched(sectionsMatched);
			legalActFound.setStringMatched(stringMatched);
			
			JsonCourtOrder jsonCourtOrder = legalReferenceService.addLegalActFound(jsonCourtOrderTemp, legalActFound, project, authentication);
			annotationService.saveJsonOrder(project, legalDocument, jsonCourtOrder, authentication);
			
		}catch (Exception e) {
			errorMessage = "Error in retriving the legal document: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return new RedirectView("/project/" + projectId + "/annotate/" + docId);
	}
}
