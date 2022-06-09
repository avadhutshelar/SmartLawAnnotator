package in.edu.rvce.slanno.dto;

import lombok.Data;

@Data
public class MLModelResult {

	String modelName;
	String meanLOOCV;
	String mean10FoldCV;
	
}
