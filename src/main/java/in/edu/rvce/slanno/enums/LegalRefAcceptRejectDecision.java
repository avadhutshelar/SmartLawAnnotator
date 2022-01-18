package in.edu.rvce.slanno.enums;

public enum LegalRefAcceptRejectDecision {

	ACCEPT_SUGGESTION("Accept Suggestion"),
	REJECT_SUGGESTION("Reject Suggestion"),
	TBD("TBD");
		
	private final String displayValue;
	
	private LegalRefAcceptRejectDecision(String displayValue) {
        this.displayValue = displayValue;
    }
    
    public String getDisplayValue() {
        return displayValue;
    }
}
