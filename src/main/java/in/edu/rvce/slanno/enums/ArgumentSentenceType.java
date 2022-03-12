package in.edu.rvce.slanno.enums;

public enum ArgumentSentenceType {
	
	PREMISE("Premise or Evidence"),
	CONCLUSION("Conclusion or Claim"),
	NA("NA"),
	TBD("TBD");
		
	private final String displayValue;
	
	private ArgumentSentenceType(String displayValue) {
        this.displayValue = displayValue;
    }
    
    public String getDisplayValue() {
        return displayValue;
    }
}
