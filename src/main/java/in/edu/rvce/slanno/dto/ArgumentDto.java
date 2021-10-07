package in.edu.rvce.slanno.dto;

import java.util.ArrayList;
import java.util.List;

import in.edu.rvce.courtorder.ArgumentSentence;

public class ArgumentDto {

	List<ArgumentSentence> argumentSentences;
	
	public ArgumentDto() {
		this.argumentSentences=new ArrayList<>();
	}
	
	public ArgumentDto(List<ArgumentSentence> argumentSentences) {
		this.argumentSentences=argumentSentences;
	}
	
	public List<ArgumentSentence> getArgumentSentences(){
		return argumentSentences;
	}
	
	public void setArgumentSentences(List<ArgumentSentence> argumentSentences) {
		this.argumentSentences=argumentSentences;
	}
	
	public void addArgumentSentence(ArgumentSentence argumentSentence) {
		this.argumentSentences.add(argumentSentence);
	}	
	
}
