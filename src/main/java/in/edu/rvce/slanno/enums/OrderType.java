package in.edu.rvce.slanno.enums;

public enum OrderType {

	ACCEPTED("Accepted"),
	REJECTED("Rejected");
		
	private final String displayValue;
	
	private OrderType(String displayValue) {
        this.displayValue = displayValue;
    }
    
    public String getDisplayValue() {
        return displayValue;
    }
}
