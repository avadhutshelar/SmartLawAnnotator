package in.edu.rvce.slanno.enums;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class UserDto {

	@NotBlank(message = "Username is mandatory")
	String username;
	
	@NotBlank(message = "Password is mandatory")
	String password;
	
	@NotNull(message = "Enabled is mandatory")
	UserEnabled enabled;
	
	@NotNull(message = "Role is mandatory")
	UserAuthorities authority;
	
	Boolean isAnnotatorForProject;

	Boolean isAdminForProject;
}
