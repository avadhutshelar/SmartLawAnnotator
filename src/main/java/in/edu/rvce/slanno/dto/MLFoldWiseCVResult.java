package in.edu.rvce.slanno.dto;

import lombok.Data;

@Data
public class MLFoldWiseCVResult {

	String fold;
	String meanAccuracy;
	String minAccuracy;
	String maxAccuracy;
	
}
