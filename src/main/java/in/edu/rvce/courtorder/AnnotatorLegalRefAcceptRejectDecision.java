package in.edu.rvce.courtorder;

import in.edu.rvce.slanno.enums.LegalRefAcceptRejectDecision;
import lombok.Data;

@Data
public class AnnotatorLegalRefAcceptRejectDecision {
	Integer annotatorId;
	LegalRefAcceptRejectDecision legalRefAcceptRejectDecision;
	
	public AnnotatorLegalRefAcceptRejectDecision() {
		
	}
}
