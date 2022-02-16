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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import in.edu.rvce.slanno.enums.UserDto;
import in.edu.rvce.slanno.services.UsersService;
import in.edu.rvce.slanno.utils.SessionMessage;

@Controller
public class UsersController {

	@Autowired
	private UsersService usersService;

	@GetMapping("/users")
	public String addUser(SessionMessage message, Model model) {
		String successMessage = "";
		String errorMessage = "";

		try {
			UserDto userDto = new UserDto();
			model.addAttribute("userDto", userDto);

			List<UserDto> userDtoList = usersService.getUserDtos();
			if (!CollectionUtils.isEmpty(userDtoList)) {
				model.addAttribute("userDtoList", userDtoList);
			} else {
				successMessage = "No users added yet";
			}
		} catch (Exception e) {
			errorMessage = "Error in retriving the users: \n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return "users";
	}

	@PostMapping("/users/add")
	public String addUser(@Valid UserDto userDto, BindingResult result, SessionMessage message, Model model) {
		String successMessage = "";
		String errorMessage = "";
		try {
			if (result.hasErrors()) {
				errorMessage = "One or more mandatory parameters missing. Please check";
			} else {
				usersService.createOrUpdateUser(userDto);
				successMessage = "User :: " + userDto.getUsername() + " :: Created Successfully";
			}
		} catch (Exception e) {
			errorMessage = "User Addition Failed with follwing error:\n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return "users";
	}

	@RequestMapping("/users/edit/{username}")
	@ResponseBody
	public UserDto showUpdateForm(@PathVariable("username") String username, SessionMessage message, Model model) {
		String successMessage = "";
		String errorMessage = "";
		UserDto userDto = new UserDto();
		try {
			userDto = usersService.getUserDto(username);
			model.addAttribute("userDto", userDto);
		} catch (Exception e) {
			errorMessage = "User edit failed with following error:\n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		
		return userDto;
	}
	
	@PostMapping("/users/edit/{username}")
	public String updateUser(@Valid UserDto userDto, BindingResult result, SessionMessage message, Model model) {
		String successMessage = "";
		String errorMessage = "";
		try {
			if (result.hasErrors()) {
				errorMessage = "One or more mandatory parameters missing. Please check";
			} else {
				usersService.createOrUpdateUser(userDto);
				successMessage = "User :: " + userDto.getUsername() + " :: Created Successfully";
			}
		} catch (Exception e) {
			errorMessage = "User edit failed with follwing error:\n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return "redirect:/users";
	}
	
	@PostMapping("/users/delete/{username}")
	public String deleteUser(@PathVariable("username") String username, SessionMessage message, Model model) {
		String successMessage = "";
		String errorMessage = "";
		try {
			usersService.deleteUser(username);
			successMessage = "User :: " + username + " :: Deleted Successfully";
		} catch (Exception e) {
			errorMessage = "User deletion Failed with follwing error:\n" + e.getMessage();
		} finally {
			message.setSuccessMessage(successMessage);
			message.setErrorMessage(errorMessage);
			model.addAttribute("message", message);
		}
		return "redirect:/users";
	}
	
}
