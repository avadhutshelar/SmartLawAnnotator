package in.edu.rvce.slanno.dto;

import java.util.List;

import lombok.Data;

@Data
public class CVResult {
	String iterationNumber;
	String noOfSamples;
	String meanLOOCVAllModels;
	String mean10FoldCVAllModels;
	List<MLModelResult> mlModelResultList;
}
