package in.edu.rvce.courtorder.annotations;

import in.edu.rvce.slanno.enums.LegalRefAcceptRejectDecision;
import lombok.Data;

@Data
public class LegalRefAcceptRejectDecisionAnnotations {

	String username;
	LegalRefAcceptRejectDecision legalRefAcceptRejectDecision;
	
	public LegalRefAcceptRejectDecisionAnnotations() {
		
	}

	public LegalRefAcceptRejectDecisionAnnotations(String username,
			LegalRefAcceptRejectDecision legalRefAcceptRejectDecision) {
		super();
		this.username = username;
		this.legalRefAcceptRejectDecision = legalRefAcceptRejectDecision;
	}
	
	
}
