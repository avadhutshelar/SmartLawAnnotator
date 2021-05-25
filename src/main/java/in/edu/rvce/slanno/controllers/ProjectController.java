package in.edu.rvce.slanno.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import in.edu.rvce.slanno.entities.LegalDocument;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.enums.AnnotationProcessingStage;
import in.edu.rvce.slanno.services.ProjectService;
import in.edu.rvce.slanno.utils.SessionMessage;

@Controller
public class ProjectController {

	@Autowired
	private ProjectService projectService;

	@GetMapping("/project/create")
	public String createProject(SessionMessage message, Model model) {
		message.setSuccessMessage("");
		message.setErrorMessage("");
		model.addAttribute("message", message);
		model.addAttribute("project", new Project());
		return "project-create";
	}

	@PostMapping("/project/create")
	public String createProject(@Valid Project project, BindingResult result, SessionMessage message, Model model) {
		String successMessage = "";
		String errorMessage = "";
		try {
			if (result.hasErrors()) {
				errorMessage = "One or more mandatory parameters missing. Please check";
			} else if (!projectService.createDirectories(project)) {
				errorMessage = "Given Project Directory Already Present. Type Some Other Name.";
			} else if (projectService.createProject(project)) {
				successMessage = "Project " + project.getProjectName() + " Created Successfully";
			}
		} catch (Exception e) {
			errorMessage = "Project Creation Failed with follwing error:\n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return "project-create";
	}

	@GetMapping("/project/view/all")
	public String veiwAllProject(SessionMessage message, Model model) {
		String successMessage = "";
		String errorMessage = "";
		try {
			List<Project> projectList = projectService.getAllProjects();
			if (!CollectionUtils.isEmpty(projectList)) {
				model.addAttribute("projectList", projectList);
			} else {
				successMessage = "No project present";
			}
		} catch (Exception e) {
			errorMessage = "Error in retriving the projects: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return "project-list";
	}

	@GetMapping("/project/view/{projectId}")
	public String viewProject(SessionMessage message, Model model, @PathVariable Integer projectId) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			model.addAttribute("project", project);
			model.addAttribute("datasetBaseDir", projectService.getDatasetBaseDirectory() + "\\");
		} catch (Exception e) {
			errorMessage = "Error in retriving the project: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return "project-view";
	}

	@GetMapping("/project/{projectId}/import")
	public String importDocumentsProject(SessionMessage message, Model model, @PathVariable Integer projectId) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			model.addAttribute("project", project);
		} catch (Exception e) {
			errorMessage = "Error in retriving the project: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
			model.addAttribute("datasetBaseDir", projectService.getDatasetBaseDirectory() + "\\");
		}
		return "project-import-documents";
	}

	@PostMapping("/project/{projectId}/import")
	public String importDocumentsProject(Project project,BindingResult result, SessionMessage message, Model model,
			@RequestPart(value = "files", required = true) MultipartFile[] files, @PathVariable Integer projectId)
			throws Exception {
		String successMessage = "";
		String errorMessage = "";
		try {
			if (result.hasErrors()) {
				errorMessage = "One or more mandatory parameters missing. Please check";
			} else {				
				project = projectService.getProjectById(projectId);
				projectService.importDocuments(project,files);
				project = projectService.getProjectById(projectId);
				model.addAttribute("project", project);
			}
		} catch (Exception e) {
			errorMessage = "Importing Documents Failed with follwing error:\n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
			model.addAttribute("datasetBaseDir", projectService.getDatasetBaseDirectory() + "\\");
		}
		return "project-import-documents";
	}	

}
