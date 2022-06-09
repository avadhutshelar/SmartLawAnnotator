package in.edu.rvce.slanno.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MLOutputDto {
	
	private List<String> argumentBy;
	private List<String> sentenceType;
	private List<String> orderType;
	private List<DatasetStatistics> datasetStatisticsList;
	private List<CVResult> cvResultList;
}
