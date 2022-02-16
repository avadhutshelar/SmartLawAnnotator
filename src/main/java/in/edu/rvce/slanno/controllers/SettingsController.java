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
import in.edu.rvce.slanno.entities.SystemSetting;
import in.edu.rvce.slanno.services.SettingsService;
import in.edu.rvce.slanno.utils.SessionMessage;

@Controller
public class SettingsController {

	@Autowired
	private SettingsService settingsService;

	@GetMapping("/legalActs")
	public String createlegalAct(SessionMessage message, Model model) {
		String successMessage = "";
		String errorMessage = "";		
		
		try {
			LegalAct legalAct=new LegalAct();
			model.addAttribute("legalAct",legalAct);
			
			List<LegalAct> legalActList = settingsService.getLegalActs();
			if (!CollectionUtils.isEmpty(legalActList)) {
				model.addAttribute("legalActList", legalActList);
			} else {
				successMessage = "No Legal acts added yet";
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
	public String createlegalAct(@Valid LegalAct legalAct, BindingResult result, SessionMessage message, Model model) {
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
	
	
	@GetMapping("/systemSettings")
	public String createSystemSetting(SessionMessage message, Model model) {
		String successMessage = "";
		String errorMessage = "";		
		
		try {
			SystemSetting systemSetting=new SystemSetting();
			model.addAttribute("systemSetting",systemSetting);
			
			List<SystemSetting> systemSettingList = settingsService.getSystemSettings();
			if (!CollectionUtils.isEmpty(systemSettingList)) {
				model.addAttribute("systemSettingList", systemSettingList);
			} else {
				successMessage = "No system settings added yet";
			}
		} catch (Exception e) {
			errorMessage = "Error in retriving the system settings: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return "systemSettings";		
	}

	@PostMapping("/systemSettings/add")
	public String createSystemSetting(@Valid SystemSetting systemSetting, BindingResult result, SessionMessage message, Model model) {
		String successMessage = "";
		String errorMessage = "";
		try {
			if (result.hasErrors()) {
				errorMessage = "One or more mandatory parameters missing. Please check";
			} else {
				settingsService.createSystemSetting(systemSetting);
				successMessage = "System Setting :: " + systemSetting.getKey() + " :: Created Successfully";
			}
		} catch (Exception e) {
			errorMessage = "System Setting Addition Failed with follwing error:\n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return "systemSettings";
	}
}
