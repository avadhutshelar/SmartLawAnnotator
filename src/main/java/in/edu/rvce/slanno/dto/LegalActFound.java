package in.edu.rvce.slanno.dto;

import in.edu.rvce.slanno.entities.LegalAct;
import lombok.Data;

@Data
public class LegalActFound {
	
	String actNameMatched;
	
	String sectionsMatched;
	
	LegalAct legalAct;
	
	public LegalActFound(String actNameMatched, String sectionsMatched, LegalAct legalAct) {
		this.actNameMatched=actNameMatched;
		this.sectionsMatched=sectionsMatched;
		this.legalAct=legalAct;
	}
	
	public LegalActFound() {
		
	}
}
