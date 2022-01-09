package in.edu.rvce.slanno.enums;

public enum LegalRefAcceptRejectDecision {

	ACCEPT("Accept"),
	REJECT("Reject"),
	TBD("TBD");
		
	private final String displayValue;
	
	private LegalRefAcceptRejectDecision(String displayValue) {
        this.displayValue = displayValue;
    }
    
    public String getDisplayValue() {
        return displayValue;
    }
}
