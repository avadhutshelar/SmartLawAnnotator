package in.edu.rvce.slanno.dto;

import java.util.List;

import lombok.Data;

@Data
public class MLModelResult {

	String modelName;
	String meanLOOCV;
	String mean10FoldCV;
	List<MLFoldWiseCVResult> foldWiseResult;
}
