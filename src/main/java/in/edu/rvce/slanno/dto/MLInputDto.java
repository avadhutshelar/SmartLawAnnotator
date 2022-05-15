package in.edu.rvce.slanno.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MLInputDto {

	private List<String> argumentTexts;
	private List<String> sentenceTypeTexts;
	private List<String> orderTypeTexts;
	
}
