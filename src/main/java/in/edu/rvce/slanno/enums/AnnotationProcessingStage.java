package in.edu.rvce.slanno.enums;

public enum AnnotationProcessingStage {

	STAGE0("Original Text Extraction Completed"),
	STAGE1("Preprocessing Completed"),
	STAGE2("Annotation Completed");
		
	private final String displayValue;
	
	private AnnotationProcessingStage(String displayValue) {
        this.displayValue = displayValue;
    }
    
    public String getDisplayValue() {
        return displayValue;
    }
}
