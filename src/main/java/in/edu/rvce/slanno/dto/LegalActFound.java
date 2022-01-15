package in.edu.rvce.slanno.dto;

import in.edu.rvce.slanno.entities.LegalAct;
import lombok.Getter;
import lombok.Setter;

public class LegalActFound extends LegalAct{

	@Getter @Setter
	String whatsMatched;
}
