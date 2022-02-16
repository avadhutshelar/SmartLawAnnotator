package in.edu.rvce.slanno.enums;

public enum UserEnabled {

	YES("true"),
	NO("false");
		
	private final String displayValue;
	
	private UserEnabled(String displayValue) {
        this.displayValue = displayValue;
    }
    
    public String getDisplayValue() {
        return displayValue;
    }
}
