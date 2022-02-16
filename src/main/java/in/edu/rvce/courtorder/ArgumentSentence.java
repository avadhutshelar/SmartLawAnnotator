package in.edu.rvce.courtorder;

import java.util.List;

import in.edu.rvce.courtorder.annotations.ArgumentSentenceTypeAnnotations;
import in.edu.rvce.slanno.enums.ArgumentSentenceType;
import lombok.Data;

@Data
public class ArgumentSentence {
	
	Integer sentenceNumber;
	String text;
	ArgumentSentenceType argumentSentenceType;
	List<ArgumentSentenceTypeAnnotations> argumentSentenceTypeAnnotations;
	
	public ArgumentSentence() {
	
	}
}
