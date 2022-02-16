package in.edu.rvce.slanno.dto;

import java.util.List;

import in.edu.rvce.slanno.enums.UserDto;
import lombok.Data;

@Data
public class UserProjectDto {
	
	List<UserDto> userDtoList;
	
}
