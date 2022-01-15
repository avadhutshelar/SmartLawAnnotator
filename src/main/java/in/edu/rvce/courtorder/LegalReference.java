package in.edu.rvce.courtorder;

import in.edu.rvce.slanno.dto.LegalActFound;
import in.edu.rvce.slanno.enums.LegalRefAcceptRejectDecision;
import lombok.Data;

@Data
public class LegalReference {
	
	Integer refNumber;	
	LegalActFound legalActFound;
	LegalRefAcceptRejectDecision legalRefAcceptRejectDecision;
	
	public LegalReference() {
		super();
	}

	public LegalReference(Integer refNumber, LegalActFound legalActFound, LegalRefAcceptRejectDecision legalRefAcceptRejectDecision) {
		super();
		this.refNumber = refNumber;
		this.legalActFound=legalActFound;
		this.legalRefAcceptRejectDecision = legalRefAcceptRejectDecision;
	}
	
	
}
