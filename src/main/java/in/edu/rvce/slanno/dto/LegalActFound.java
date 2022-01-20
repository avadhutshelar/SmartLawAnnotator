package in.edu.rvce.slanno.dto;

import in.edu.rvce.slanno.entities.LegalAct;
import lombok.Data;

@Data
public class LegalActFound {

	String actNameMatched;

	String sectionsMatched;

	String stringMatched;

	LegalAct legalAct;

	public LegalActFound(String actNameMatched, String sectionsMatched, String stringMatched, LegalAct legalAct) {
		this.actNameMatched = actNameMatched;
		this.sectionsMatched = sectionsMatched;
		this.stringMatched = stringMatched;
		this.legalAct = legalAct;
	}

	public LegalActFound() {

	}
}
