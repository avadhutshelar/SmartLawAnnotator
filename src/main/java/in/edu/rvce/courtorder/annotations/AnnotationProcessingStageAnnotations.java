package in.edu.rvce.courtorder.annotations;

import in.edu.rvce.slanno.enums.AnnotationProcessingStage;
import lombok.Data;

@Data
public class AnnotationProcessingStageAnnotations {

	String username;
	AnnotationProcessingStage annotationProcessingStage;
	
	public AnnotationProcessingStageAnnotations() {
		
	}

	public AnnotationProcessingStageAnnotations(String username,
			AnnotationProcessingStage annotationProcessingStage) {
		this.username = username;
		this.annotationProcessingStage = annotationProcessingStage;
	}
}
