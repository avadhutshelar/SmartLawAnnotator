package in.edu.rvce.slanno.dto;

import in.edu.rvce.slanno.entities.Project;
import lombok.Data;

@Data
public class AnnotationTaskDto {
	
	Project project;
	String totalDocsAssigned;
	String totalDocsPending;	
	String totalDocsComplete;	
}
