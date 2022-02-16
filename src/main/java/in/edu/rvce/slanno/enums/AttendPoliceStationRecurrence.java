package in.edu.rvce.slanno.enums;

public enum AttendPoliceStationRecurrence {
	
	DAILY("Daily"),
	WEEKLY("Weekly"),
	MONTHLY("Monthly"),
	NA("NA");
		
	private final String displayValue;
	
	private AttendPoliceStationRecurrence(String displayValue) {
        this.displayValue = displayValue;
    }
    
    public String getDisplayValue() {
        return displayValue;
    }
}
