package in.edu.rvce.courtorder;

import java.util.List;

import in.edu.rvce.courtorder.annotations.AnnotationProcessingStageAnnotations;
import in.edu.rvce.slanno.enums.AnnotationProcessingStage;
import lombok.Data;

@Data
public class JsonCourtOrder {	
	String header;
	Background background;
	List<Argument> arguments;
	Order order;
	String footer;	
	AnnotationProcessingStage annotationProcessingStage;
	List<AnnotationProcessingStageAnnotations> annotationProcessingStageAnnotations;
	String processedText;
}