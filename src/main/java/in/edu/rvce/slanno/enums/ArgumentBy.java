package in.edu.rvce.slanno.enums;

public enum ArgumentBy {

	APPLICANT("Applicant Lawyer"),
	RESPONDENT("Respondent Lawyer"),
	JUDGE("Judge"),
	TBD("TBD");
		
	private final String displayValue;
	
	private ArgumentBy(String displayValue) {
        this.displayValue = displayValue;
    }
    
    public String getDisplayValue() {
        return displayValue;
    }
}
