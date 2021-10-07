package in.edu.rvce.courtorder;

import java.util.List;

import in.edu.rvce.slanno.enums.ArgumentBy;
import lombok.Data;

@Data
public class Argument{
		
	Integer argumentNumber;
	String text;
	ArgumentBy argumentBy;
	List<ArgumentSentence> argumentSentences;
	
	public Argument(String text) {
		super();
		this.text = text;
	}

	public Argument() {
		super();
	}

	
}
