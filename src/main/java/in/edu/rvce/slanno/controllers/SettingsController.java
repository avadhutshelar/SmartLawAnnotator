package in.edu.rvce.slanno.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import in.edu.rvce.slanno.dto.SettingsDto;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.services.ProjectService;
import in.edu.rvce.slanno.utils.SessionMessage;

@Controller
public class SettingsController {

	@Autowired
	private ProjectService projectService;

	@GetMapping("/legalActs")
	public String createProject(SessionMessage message, Model model) {
		message.setSuccessMessage("");
		message.setErrorMessage("");
		model.addAttribute("message", message);
		model.addAttribute("settingsDto", new SettingsDto());
		return "legalActs";
	}

	@PostMapping("/legalActs/add")
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
}
