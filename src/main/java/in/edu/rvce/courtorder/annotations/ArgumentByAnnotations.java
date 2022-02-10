package in.edu.rvce.courtorder.annotations;

import in.edu.rvce.slanno.enums.ArgumentBy;
import lombok.Data;

@Data
public class ArgumentByAnnotations{
		
	String username;
	ArgumentBy argumentBy;
	
	public ArgumentByAnnotations() {
		
	}

	public ArgumentByAnnotations(String username, ArgumentBy argumentBy) {
		this.username = username;
		this.argumentBy = argumentBy;
	}	
}
