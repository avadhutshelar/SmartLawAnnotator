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
import in.edu.rvce.courtorder.annotations.OrderTypeAnnotations;
import in.edu.rvce.slanno.dto.MLInputDto;
import in.edu.rvce.slanno.dto.MLOutputDto;
import in.edu.rvce.slanno.enums.ArgumentBy;
import in.edu.rvce.slanno.enums.ArgumentSentenceType;
import in.edu.rvce.slanno.enums.OrderType;

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
			
			argument.getArgumentSentences().forEach(sent->{
				sent.getArgumentSentenceTypeAnnotations().forEach(sentTypeAnno->{
					if(StringUtils.equalsIgnoreCase(loggedUserName, sentTypeAnno.getUsername())) {
						if(sentTypeAnno.getArgumentSentenceType().equals(ArgumentSentenceType.TBD)) {
							ArgumentSentenceType sentType = getSentenceTypeAnnotation(sent.getText());
							sentTypeAnno.setArgumentSentenceType(sentType);
							sent.setArgumentSentenceType(sentType);
						}
					}
				});				
			});
			
		});
		
		jsonCourtOrder.getOrder().getOrderTypeAnnotations().forEach(orderTypeAnno->{
			if(StringUtils.equalsIgnoreCase(loggedUserName, orderTypeAnno.getUsername())) {
				if(orderTypeAnno.getOrderType().equals(OrderType.TBD)) {
					OrderType orderType = getOrderTypeAnnotation(jsonCourtOrder.getOrder().getText());
					orderTypeAnno.setOrderType(orderType);
					jsonCourtOrder.getOrder().setOrderType(orderType);
				}
			}
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
	
	public ArgumentSentenceType getSentenceTypeAnnotation(String sentenceTypeText) {
		ArgumentSentenceType sentType = ArgumentSentenceType.TBD;
		
		MLInputDto mlInputDto = new MLInputDto();
		List<String> sentenceTypeTexts=new ArrayList<>();
		sentenceTypeTexts.add(sentenceTypeText);
		mlInputDto.setSentenceTypeTexts(sentenceTypeTexts);
		
		String result = restTemplate.postForObject("http://localhost:8050/predictSentenceType", mlInputDto, String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		MLOutputDto mlOutputDto;
		try {
			mlOutputDto = objectMapper.readValue(result, MLOutputDto.class);
			List<String> sentenceType = mlOutputDto.getSentenceType();
			
			sentType = getSentenceType(sentenceType.get(0));
			
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sentType;
	}
	
	private ArgumentSentenceType getSentenceType(String sentType) {
		ArgumentSentenceType sentenceType = ArgumentSentenceType.TBD;
		if(StringUtils.equalsIgnoreCase(sentType, ArgumentSentenceType.PREMISE.toString())) {
			sentenceType=ArgumentSentenceType.PREMISE;
		}else if(StringUtils.equalsIgnoreCase(sentType, ArgumentSentenceType.CONCLUSION.toString())) {
			sentenceType=ArgumentSentenceType.CONCLUSION;
		}
		return sentenceType;
	}
	
	public OrderType getOrderTypeAnnotation(String orderText) {
		OrderType oType = OrderType.TBD;
		
		MLInputDto mlInputDto = new MLInputDto();
		List<String> orderTypeTexts=new ArrayList<>();
		orderTypeTexts.add(orderText);
		mlInputDto.setOrderTypeTexts(orderTypeTexts);
		
		String result = restTemplate.postForObject("http://localhost:8050/predictOrderType", mlInputDto, String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		MLOutputDto mlOutputDto;
		try {
			mlOutputDto = objectMapper.readValue(result, MLOutputDto.class);
			List<String> orderType = mlOutputDto.getOrderType();
			
			oType = getOrderType(orderType.get(0));
			
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return oType;
	}
	
	private OrderType getOrderType(String oType) {
		OrderType orderType = OrderType.TBD;
		if(StringUtils.equalsIgnoreCase(oType, OrderType.ACCEPTED.toString())) {
			orderType=OrderType.ACCEPTED;
		}else if(StringUtils.equalsIgnoreCase(oType, OrderType.REJECTED.toString())) {
			orderType=OrderType.REJECTED;
		}
		return orderType;
	}
}
