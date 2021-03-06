package in.edu.rvce.courtorder.annotations;

import in.edu.rvce.slanno.enums.ArgumentSentenceType;
import lombok.Data;

@Data
public class ArgumentSentenceTypeAnnotations {
	
	String username;
	ArgumentSentenceType argumentSentenceType;
	
	public ArgumentSentenceTypeAnnotations() {
		
	}

	public ArgumentSentenceTypeAnnotations(String username, ArgumentSentenceType argumentSentenceType) {
		this.username = username;
		this.argumentSentenceType = argumentSentenceType;
	}
}
