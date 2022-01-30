package in.edu.rvce.slanno.enums;

public enum UserAuthorities {

	ADMIN("ROLE_ADMIN"),
	ANNOTATOR("ROLE_ANNOTATOR");
		
	private final String displayValue;
	
	private UserAuthorities(String displayValue) {
        this.displayValue = displayValue;
    }
    
    public String getDisplayValue() {
        return displayValue;
    }
}
