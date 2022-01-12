package in.edu.rvce.courtorder;

import in.edu.rvce.slanno.entities.LegalAct;
import in.edu.rvce.slanno.enums.LegalRefAcceptRejectDecision;
import lombok.Data;

@Data
public class LegalReference {
	
	Integer refNumber;
	String text;
	LegalAct legalAct;
	LegalRefAcceptRejectDecision legalRefAcceptRejectDecision;
	
	public LegalReference() {
		super();
	}

	public LegalReference(Integer refNumber, String text, LegalAct legalAct, LegalRefAcceptRejectDecision legalRefAcceptRejectDecision) {
		super();
		this.refNumber = refNumber;
		this.text = text;
		this.legalAct=legalAct;
		this.legalRefAcceptRejectDecision = legalRefAcceptRejectDecision;
	}
	
	
}
