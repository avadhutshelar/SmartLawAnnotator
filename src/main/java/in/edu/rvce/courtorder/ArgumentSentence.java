package in.edu.rvce.courtorder;

import in.edu.rvce.slanno.enums.ArgumentSentenceType;
import lombok.Data;

@Data
public class ArgumentSentence {
	Integer sentenceNumber;
	String text;
	ArgumentSentenceType argumentSentenceType;
}
