package in.edu.rvce.courtorder;

import java.util.List;

import in.edu.rvce.courtorder.annotations.LegalRefAcceptRejectDecisionAnnotations;
import in.edu.rvce.slanno.dto.LegalActFound;
import in.edu.rvce.slanno.enums.LegalRefAcceptRejectDecision;
import lombok.Data;

@Data
public class LegalReference {
	
	Integer refNumber;	
	LegalActFound legalActFound;
	LegalRefAcceptRejectDecision legalRefAcceptRejectDecision;
	List<LegalRefAcceptRejectDecisionAnnotations> legalRefAcceptRejectDecisionAnnotations;
	
	public LegalReference() {
		super();
	}

	public LegalReference(Integer refNumber, LegalActFound legalActFound, LegalRefAcceptRejectDecision legalRefAcceptRejectDecision,
			List<LegalRefAcceptRejectDecisionAnnotations> legalRefAcceptRejectDecisionAnnotations) {
		super();
		this.refNumber = refNumber;
		this.legalActFound=legalActFound;
		this.legalRefAcceptRejectDecision = legalRefAcceptRejectDecision;
		this.legalRefAcceptRejectDecisionAnnotations = legalRefAcceptRejectDecisionAnnotations;
	}
	
	
}
