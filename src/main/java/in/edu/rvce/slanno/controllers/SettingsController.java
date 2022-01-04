package in.edu.rvce.slanno.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import in.edu.rvce.slanno.entities.LegalAct;
import in.edu.rvce.slanno.services.SettingsService;
import in.edu.rvce.slanno.utils.SessionMessage;

@Controller
public class SettingsController {

	@Autowired
	private SettingsService settingsService;

	@GetMapping("/legalActs")
	public String createProject(SessionMessage message, Model model) {
		String successMessage = "";
		String errorMessage = "";		
		
		try {
			LegalAct legalAct=new LegalAct();
			model.addAttribute("legalAct",legalAct);
			
			List<LegalAct> legalActList = settingsService.getLegalActs();
			if (!CollectionUtils.isEmpty(legalActList)) {
				model.addAttribute("legalActList", legalActList);
			} else {
				successMessage = "No project present";
			}
		} catch (Exception e) {
			errorMessage = "Error in retriving the Legal Acts: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return "legalActs";		
	}

	@PostMapping("/legalActs/add")
	public String createProject(@Valid LegalAct legalAct, BindingResult result, SessionMessage message, Model model) {
		String successMessage = "";
		String errorMessage = "";
		try {
			if (result.hasErrors()) {
				errorMessage = "One or more mandatory parameters missing. Please check";
			} else {
				settingsService.createActs(legalAct);
				successMessage = "Legal Act :: " + legalAct.getActName() + " :: Created Successfully";
			}
		} catch (Exception e) {
			errorMessage = "Legal Act Addition Failed with follwing error:\n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return "legalActs";
	}
}
