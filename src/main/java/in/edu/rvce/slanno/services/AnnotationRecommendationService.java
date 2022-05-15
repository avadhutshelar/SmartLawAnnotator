package in.edu.rvce.slanno.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.edu.rvce.courtorder.JsonCourtOrder;
import in.edu.rvce.slanno.dto.MLInputDto;
import in.edu.rvce.slanno.dto.MLOutputDto;
import in.edu.rvce.slanno.enums.ArgumentBy;

@Service
public class AnnotationRecommendationService {
	
	@Autowired
	private RestTemplate restTemplate;

	public void updateAnnotationRecommendations(JsonCourtOrder jsonCourtOrder, String loggedUserName) throws Exception{
		
		jsonCourtOrder.getArguments().forEach(argument->{
		
			argument.getArgumentByAnnotations().forEach(argByAnnotation->{
				if(StringUtils.equalsIgnoreCase(loggedUserName,argByAnnotation.getUsername())) {
					if(argByAnnotation.getArgumentBy().equals(ArgumentBy.TBD)) {
						ArgumentBy argBy=getArgumentByAnnotation(argument.getText());
						argByAnnotation.setArgumentBy(argBy);
						argument.setArgumentBy(argBy);
					}					
				}
			});
		});
		
	}
	
	public ArgumentBy getArgumentByAnnotation(String argumentText) {
		ArgumentBy argBy = ArgumentBy.TBD;
		
		MLInputDto mlInputDto = new MLInputDto();
		List<String> argumentTexts=new ArrayList<>();
		argumentTexts.add(argumentText);
		mlInputDto.setArgumentTexts(argumentTexts);
		
		String result = restTemplate.postForObject("http://localhost:8050/predictArgumentBy", mlInputDto, String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		MLOutputDto mlOutputDto;
		try {
			mlOutputDto = objectMapper.readValue(result, MLOutputDto.class);
			List<String> argumentBy = mlOutputDto.getArgumentBy();
			
			argBy = getArgumentBy(argumentBy.get(0));
			
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return argBy;
	}
	
	private ArgumentBy getArgumentBy(String argBy) {
		ArgumentBy argumentBy = ArgumentBy.TBD;
		if(StringUtils.equalsIgnoreCase(argBy, ArgumentBy.APPLICANT.toString())) {
			argumentBy=ArgumentBy.APPLICANT;
		}else if(StringUtils.equalsIgnoreCase(argBy, ArgumentBy.RESPONDENT.toString())) {
			argumentBy=ArgumentBy.RESPONDENT;
		}else if(StringUtils.equalsIgnoreCase(argBy, ArgumentBy.JUDGE.toString())) {
			argumentBy=ArgumentBy.JUDGE;
		}
		return argumentBy;
	}
}
