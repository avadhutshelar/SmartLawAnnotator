package in.edu.rvce.courtorder;

import in.edu.rvce.slanno.entities.LegalAct;
import in.edu.rvce.slanno.enums.LegalRefAcceptRejectDecision;
import lombok.Data;

@Data
public class LegalReference {
	
	Integer refNumber;
	String sectionListString;
	LegalAct legalAct;
	LegalRefAcceptRejectDecision legalRefAcceptRejectDecision;
	
	public LegalReference() {
		super();
	}

	public LegalReference(Integer refNumber, String sectionListString, LegalAct legalAct, LegalRefAcceptRejectDecision legalRefAcceptRejectDecision) {
		super();
		this.refNumber = refNumber;
		this.sectionListString = sectionListString;
		this.legalAct=legalAct;
		this.legalRefAcceptRejectDecision = legalRefAcceptRejectDecision;
	}
	
	
}
