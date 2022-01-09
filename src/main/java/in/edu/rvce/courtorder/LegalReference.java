package in.edu.rvce.courtorder;

import in.edu.rvce.slanno.enums.LegalRefAcceptRejectDecision;
import lombok.Data;

@Data
public class LegalReference {
	
	Integer refNumber;
	String text;
	LegalRefAcceptRejectDecision legalRefAcceptRejectDecision;
	
	public LegalReference() {
		super();
	}

	public LegalReference(Integer refNumber, String text, LegalRefAcceptRejectDecision legalRefAcceptRejectDecision) {
		super();
		this.refNumber = refNumber;
		this.text = text;
		this.legalRefAcceptRejectDecision = legalRefAcceptRejectDecision;
	}
	
	
}
