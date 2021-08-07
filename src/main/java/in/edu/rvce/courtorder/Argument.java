package in.edu.rvce.courtorder;

import java.util.List;

import lombok.Data;

@Data
public class Argument {
	String text;
	String argumentBy;
	List<ArgumentSentence> argumentSentences;
	
	public Argument(String text) {
		super();
		this.text = text;
	}	
}
