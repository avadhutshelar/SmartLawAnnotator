package in.edu.rvce.slanno.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.edu.rvce.slanno.dto.InterAnnotatorAgreementDto;
import in.edu.rvce.slanno.dto.MLInputDto;
import in.edu.rvce.slanno.dto.MLOutputDto;
import in.edu.rvce.slanno.dto.UserProjectDto;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.enums.UserAuthorities;
import in.edu.rvce.slanno.enums.UserDto;
import in.edu.rvce.slanno.services.InterAnnotatorAgreementService;
import in.edu.rvce.slanno.services.ProjectService;
import in.edu.rvce.slanno.services.UsersService;
import in.edu.rvce.slanno.utils.SessionMessage;

@Controller
public class ProjectController {

	@Autowired
	private ProjectService projectService;
	
	@Autowired
	UsersService usersService;

	@Autowired
	InterAnnotatorAgreementService interAnnotatorAgreementService;

	@Autowired
	private RestTemplate restTemplate;
	
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
			} else if (projectService.createOrUpdateProject(project)) {
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
	
	@GetMapping("/project/{projectId}/annotators")
	public String projectAnnotators(SessionMessage message, Model model, @PathVariable Integer projectId) {
		String successMessage = "";
		String errorMessage = "";
		if(StringUtils.isNotBlank(message.getErrorMessage())) {errorMessage=message.getErrorMessage();}
		try {
			Project project = projectService.getProjectById(projectId);
			
			List<UserDto> userDtoList = usersService.getUserDtos();
			UserProjectDto userProjectDto = new UserProjectDto();
			List<UserDto> userDtoListTemp = new ArrayList<>();
			userDtoList.forEach(userDto->{
				if(StringUtils.isNotBlank(project.getAnnotatorUserListString())) {
					String[] usernamesArray =  project.getAnnotatorUserListString().split(",");
					List<String> usernames = Arrays.asList(usernamesArray);
					if(usernames.contains(userDto.getUsername())) {
						userDto.setIsAnnotatorForProject(Boolean.TRUE);
					}					
				}
				userDtoListTemp.add(userDto);
			});
			userProjectDto.setUserDtoList(userDtoListTemp);
			
			model.addAttribute("userProjectDto", userProjectDto);
			model.addAttribute("project", project);
		} catch (Exception e) {
			errorMessage = "Error in retriving the project annotators: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);			
		}
		return "project-annotators";
	}

	@PostMapping("/project/{projectId}/annotators")
	public String updateProjectAnnotators(UserProjectDto userProjectDto,
				BindingResult result, SessionMessage message, Model model, @PathVariable Integer projectId)
			throws Exception {
		String successMessage = "";
		String errorMessage = "";
		try {		
				
			Project project = projectService.getProjectById(projectId);
				
			List<String> annotatorUserWithAnnotatorRole = new ArrayList<>();
			List<String> annotatorUserList = new ArrayList<>();
			userProjectDto.getUserDtoList().forEach(user->{
				if(user.getIsAnnotatorForProject().equals(Boolean.TRUE)) {
					annotatorUserList.add(user.getUsername());
					if(user.getAuthority().equals(UserAuthorities.ANNOTATOR)) {
						annotatorUserWithAnnotatorRole.add(user.getUsername());
					}
				}					
			});
			if(annotatorUserWithAnnotatorRole.size()>=3) {
				String annotatorUserListString=annotatorUserList.stream().map(a -> String.valueOf(a))
						.collect(Collectors.joining(","));			
				
				project.setAnnotatorUserListString(annotatorUserListString);
				projectService.createOrUpdateProject(project);
				successMessage= "Update Success";
			}else {
				errorMessage = "Updating Annotators Failed - Minimum 3 annotators with ANNOTATOR role should be selected";
			}
			model.addAttribute("userProjectDto", userProjectDto);
			model.addAttribute("project", project);
			
		} catch (Exception e) {
			errorMessage = "Updating Annotators Failed with follwing error:\n" + e.getMessage();			
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return "project-annotators";
	}
	
	
	@GetMapping("/project/{projectId}/interAnnotatorAgreement")
	public String getInterAnnotatorAgreement(SessionMessage message, Model model, @PathVariable Integer projectId) {
		String successMessage = "";
		String errorMessage = "";
		if(StringUtils.isNotBlank(message.getErrorMessage())) {errorMessage=message.getErrorMessage();}
		try {
			Project project = projectService.getProjectById(projectId);
			
			String[] usernamesArray = project.getAnnotatorUserListString().split(",");
			List<String> usernamesList = Arrays.asList(usernamesArray);
			
			List<InterAnnotatorAgreementDto> interAnnotatorAgreementDtoList = interAnnotatorAgreementService.calculate(usernamesList, project);
			
			model.addAttribute("project", project);
			model.addAttribute("interAnnotatorAgreementDtoList", interAnnotatorAgreementDtoList);
			model.addAttribute("usernamesList", usernamesList);
		} catch (Exception e) {
			errorMessage = "Error in retriving the inter annotator agreement: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);			
		}
		return "interAnnotatorAgreement";
	}
	
	@GetMapping("/project/{projectId}/export")
	public String exportDocuments(SessionMessage message, Model model, @PathVariable Integer projectId) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);
			successMessage = projectService.exportDocumentsStage2(project);
			successMessage += "<br />" + projectService.exportDocumentsStage1(project);	
			model.addAttribute("project", project);
		} catch (Exception e) {
			errorMessage = "Error in exporting documents: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
			model.addAttribute("datasetBaseDir", projectService.getDatasetBaseDirectory() + "\\");
		}
		return "project-export-documents";
	}
	
	@GetMapping("/project/{projectId}/mltest/test")
	public String mlTest(SessionMessage message, Model model, @PathVariable Integer projectId, MLInputDto mlInputDto) {
		String successMessage = "";
		String errorMessage = "";
		try {
			Project project = projectService.getProjectById(projectId);				
			model.addAttribute("project", project);
			model.addAttribute("mlInputDto", mlInputDto);
					
			mlInputDto.setInput1("i1");
			mlInputDto.setInput2("i2");
			String result = restTemplate.postForObject("http://localhost:8050/predictArgumentBy", mlInputDto, String.class);
			ObjectMapper objectMapper = new ObjectMapper();
			MLOutputDto mlOutputDto = objectMapper.readValue(result, MLOutputDto.class);
			
			
			model.addAttribute("mlOutputDto", mlOutputDto);			
		} catch (Exception e) {
			errorMessage = "Error in retriving the project: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return "ml-test";
	}
}
