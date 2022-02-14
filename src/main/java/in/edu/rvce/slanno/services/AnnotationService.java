package in.edu.rvce.slanno.services;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import in.edu.rvce.courtorder.ArgumentSentence;
import in.edu.rvce.courtorder.Background;
import in.edu.rvce.courtorder.JsonCourtOrder;
import in.edu.rvce.courtorder.LegalReference;
import in.edu.rvce.courtorder.annotations.ArgumentByAnnotations;
import in.edu.rvce.courtorder.annotations.ArgumentSentenceTypeAnnotations;
import in.edu.rvce.courtorder.annotations.AttendPoliceStationFrequencyAnnotations;
import in.edu.rvce.courtorder.annotations.AttendPoliceStationRecurrenceAnnotations;
import in.edu.rvce.courtorder.annotations.BondAmountAnnotations;
import in.edu.rvce.courtorder.annotations.LegalRefAcceptRejectDecisionAnnotations;
import in.edu.rvce.courtorder.annotations.OrderTypeAnnotations;
import in.edu.rvce.slanno.dto.LegalActFound;
import in.edu.rvce.slanno.entities.LegalAct;
import in.edu.rvce.slanno.entities.LegalDocument;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.entities.SystemSetting;
import in.edu.rvce.slanno.enums.ArgumentBy;
import in.edu.rvce.slanno.enums.ArgumentSentenceType;
import in.edu.rvce.slanno.enums.AttendPoliceStationRecurrence;
import in.edu.rvce.slanno.enums.LegalRefAcceptRejectDecision;
import in.edu.rvce.slanno.enums.OrderType;
import in.edu.rvce.slanno.repositories.LegalDocumentRepository;
import in.edu.rvce.slanno.utils.ApplicationConstants;

@Service
public class AnnotationService {

	@Autowired
	private Environment env;

	@Autowired
	private LegalDocumentRepository legalDocumentRepository;
	
	@Autowired
	private LegalReferenceService legalReferenceService;

	public JsonCourtOrder getJsonCourtOrder(Project project, LegalDocument legalDocument, Authentication authentication) {
		JsonCourtOrder jsonCourtOrder = new JsonCourtOrder();

		String jsonText = "";

		String processedTextFileNameWithPath = env.getProperty("slanno.dataset.basedir") + "\\"
				+ project.getProjectDirectoryName() + "\\" + legalDocument.getJsonFilePath();

		try {
			jsonText = new String(Files.readAllBytes(Paths.get(processedTextFileNameWithPath)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		// File to Java objects
		try {
			jsonCourtOrder = gson.fromJson(jsonText, JsonCourtOrder.class);
			legalReferenceService.getUpdatedLegalRefsByUser(jsonCourtOrder, authentication);
			getUpdatedArgumentsByUser(jsonCourtOrder, authentication);
			getUpdatedOrderByUser(jsonCourtOrder,authentication);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jsonCourtOrder;
	}

	public void saveJsonOrder(Project project, LegalDocument legalDocument, JsonCourtOrder jsonCourtOrder) {
		try {

			legalDocument.setJsonFilePath(
					env.getProperty("slanno.dataset.dir.json") + "\\" + legalDocument.getDocumentId() + ".json");

			String jsonFileNameWithPath = env.getProperty("slanno.dataset.basedir") + "\\"
					+ project.getProjectDirectoryName() + "\\" + legalDocument.getJsonFilePath();

			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			// Java objects to File
			try (FileWriter writer = new FileWriter(jsonFileNameWithPath)) {
				gson.toJson(jsonCourtOrder, writer);
			} catch (IOException e) {
				e.printStackTrace();
			}

			legalDocumentRepository.save(legalDocument);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	private void getUpdatedArgumentsByUser(JsonCourtOrder jsonCourtOrder, Authentication authentication) {
		jsonCourtOrder.getArguments().forEach(a -> {
			List<ArgumentByAnnotations> argumentByAnnotations = a.getArgumentByAnnotations();
			argumentByAnnotations.forEach(argumentByAnno->{
				if(StringUtils.equalsIgnoreCase(authentication.getName(), argumentByAnno.getUsername())) {
					a.setArgumentBy(argumentByAnno.getArgumentBy());					
				}
			});			
			a.getArgumentSentences().forEach(sent->{
				List<ArgumentSentenceTypeAnnotations> argumentSentenceTypeAnnotations = sent.getArgumentSentenceTypeAnnotations();
				argumentSentenceTypeAnnotations.forEach(sentType->{
					if(StringUtils.equalsIgnoreCase(authentication.getName(), sentType.getUsername())) {
						sent.setArgumentSentenceType(sentType.getArgumentSentenceType());
					}
				});
			});
		});
	}
	
	public void updateArgumentsByUser(JsonCourtOrder jsonCourtOrder, JsonCourtOrder jsonCourtOrderIn, Authentication authentication) {
		
		jsonCourtOrder.getArguments().forEach(a -> {
			
			jsonCourtOrderIn.getArguments().forEach(ain -> {
				
				if (a.getArgumentNumber().equals(ain.getArgumentNumber())) {
					
					//assign argumentBy to specific user
					List<ArgumentByAnnotations> argumentByAnnotations = a.getArgumentByAnnotations();
					argumentByAnnotations.forEach(argumentByAnno->{
						if(StringUtils.equalsIgnoreCase(authentication.getName(), argumentByAnno.getUsername())) {
							argumentByAnno.setArgumentBy(ain.getArgumentBy());
						}
					});
					//a.setArgumentBy(ain.getArgumentBy());
					a.getArgumentSentences().forEach(s->{
						
						ain.getArgumentSentences().forEach(sin->{
						
							if(s.getSentenceNumber().equals(sin.getSentenceNumber())) {
								s.setArgumentSentenceType(sin.getArgumentSentenceType());
							}
							
						});
						
					});
				}
			
			});
			
		});
		//re-calculate argumentBy and sentenceType
		jsonCourtOrder.getArguments().forEach(a -> {
			
			jsonCourtOrderIn.getArguments().forEach(ain -> {
				
				if (a.getArgumentNumber().equals(ain.getArgumentNumber())) {
					
					//re-calculate argumentBy
					List<ArgumentByAnnotations> argumentByAnnotations = a.getArgumentByAnnotations();
					Map<String,Integer> argumentByCountMap = new HashMap<>();
					argumentByAnnotations.forEach(argumentByAnno->{
						String argBy = argumentByAnno.getArgumentBy().getDisplayValue();
						if (!argumentByCountMap.containsKey(argBy)) {  // first time we've seen this string
							argumentByCountMap.put(argBy, 1);
					    }
					    else {
					      int count = argumentByCountMap.get(argBy);
					      argumentByCountMap.put(argBy, count + 1);
					    }
					});	
					String maxArgBy=Collections.max(argumentByCountMap.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
					a.setArgumentBy(getArgumentByForDisplayValue(maxArgBy));
					
					//re-calculate sentence type
					a.getArgumentSentences().forEach(sent->{
						List<ArgumentSentenceTypeAnnotations> argumentSentenceTypeAnnotations = sent.getArgumentSentenceTypeAnnotations();
						Map<String,Integer> argumentSentenceTypeCountMap = new HashMap<>();
						argumentSentenceTypeAnnotations.forEach(sentTypeAnno->{
							String sentType = sentTypeAnno.getArgumentSentenceType().getDisplayValue();
							if (!argumentSentenceTypeCountMap.containsKey(sentType)) {  // first time we've seen this string
								argumentSentenceTypeCountMap.put(sentType, 1);
						    }
						    else {
						      int count = argumentSentenceTypeCountMap.get(sentType);
						      argumentSentenceTypeCountMap.put(sentType, count + 1);
						    }
						});	
						String maxsentType=Collections.max(argumentSentenceTypeCountMap.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
						sent.setArgumentSentenceType(getArgumentSentenceTypeForDisplayValue(maxsentType));
					});
				}
			
			});
			
		});
	}
	
	private void getUpdatedOrderByUser(JsonCourtOrder jsonCourtOrder, Authentication authentication) {
		List<OrderTypeAnnotations> orderTypeAnnotations = jsonCourtOrder.getOrder().getOrderTypeAnnotations();
		orderTypeAnnotations.forEach(oType->{
			if(StringUtils.equalsIgnoreCase(authentication.getName(), oType.getUsername())) {
				jsonCourtOrder.getOrder().setOrderType(oType.getOrderType());				
			}
		});
		List<BondAmountAnnotations> bondAmountAnnotations = jsonCourtOrder.getOrder().getBondAmountAnnotations();
		bondAmountAnnotations.forEach(bAmount->{
			if(StringUtils.equalsIgnoreCase(authentication.getName(), bAmount.getUsername())) {
				jsonCourtOrder.getOrder().setBondAmount(bAmount.getBondAmount());
			}
		});
		List<AttendPoliceStationRecurrenceAnnotations> attendPoliceStationRecurrenceAnnotations = 
				jsonCourtOrder.getOrder().getAttendPoliceStationRecurrenceAnnotations();
		attendPoliceStationRecurrenceAnnotations.forEach(attendRecurrence->{
			if(StringUtils.equalsIgnoreCase(authentication.getName(), attendRecurrence.getUsername())) {
				jsonCourtOrder.getOrder().setAttendPoliceStationRecurrence(attendRecurrence.getAttendPoliceStationRecurrence());
			}
		});
		List<AttendPoliceStationFrequencyAnnotations> attendPoliceStationFrequencyAnnotations=
				jsonCourtOrder.getOrder().getAttendPoliceStationFrequencyAnnotations();
		attendPoliceStationFrequencyAnnotations.forEach(attendFrequency->{
			if(StringUtils.equalsIgnoreCase(authentication.getName(), attendFrequency.getUsername())) {
				jsonCourtOrder.getOrder().setAttendPoliceStationFrequency(attendFrequency.getAttendPoliceStationFrequency());
			}
		});
	}
	
	public void updateOrderByUser(JsonCourtOrder jsonCourtOrder, JsonCourtOrder jsonCourtOrderIn, Authentication authentication) {
		
		List<OrderTypeAnnotations> jsonCourtOrderOrderTypeAnnotations = jsonCourtOrder.getOrder().getOrderTypeAnnotations();
		jsonCourtOrderOrderTypeAnnotations.forEach(a->{
			if(StringUtils.equalsIgnoreCase(authentication.getName(), a.getUsername())) {
				a.setOrderType(jsonCourtOrderIn.getOrder().getOrderType());
			}			
		});
		List<BondAmountAnnotations> jsonCourtOrderbondAmountAnnotations = jsonCourtOrder.getOrder().getBondAmountAnnotations();
		jsonCourtOrderbondAmountAnnotations.forEach(a->{
			if(StringUtils.equalsAnyIgnoreCase(authentication.getName(), a.getUsername())) {
				if(jsonCourtOrderIn.getOrder().getOrderType().equals(OrderType.ACCEPTED)) {					
					a.setBondAmount(jsonCourtOrderIn.getOrder().getBondAmount());
				}
				else {
					a.setBondAmount("0");
				}
			}
		});
		List<AttendPoliceStationRecurrenceAnnotations> jsonCourtOrderAttendPoliceStationRecurrenceAnnotations = 
				jsonCourtOrder.getOrder().getAttendPoliceStationRecurrenceAnnotations();
		jsonCourtOrderAttendPoliceStationRecurrenceAnnotations.forEach(a->{
			if(StringUtils.equalsAnyIgnoreCase(authentication.getName(), a.getUsername())) {
				if(jsonCourtOrderIn.getOrder().getOrderType().equals(OrderType.ACCEPTED)) {					
					a.setAttendPoliceStationRecurrence(jsonCourtOrderIn.getOrder().getAttendPoliceStationRecurrence());
				}
				else {
					a.setAttendPoliceStationRecurrence(AttendPoliceStationRecurrence.NA);
				}
			}
		});
		List<AttendPoliceStationFrequencyAnnotations> jsonCourtOrderAttendPoliceStationFrequencyAnnotations=
				jsonCourtOrder.getOrder().getAttendPoliceStationFrequencyAnnotations();
		jsonCourtOrderAttendPoliceStationFrequencyAnnotations.forEach(a->{
			if(StringUtils.equalsAnyIgnoreCase(authentication.getName(), a.getUsername())) {
				if(jsonCourtOrderIn.getOrder().getOrderType().equals(OrderType.ACCEPTED)) {					
					a.setAttendPoliceStationFrequency(jsonCourtOrderIn.getOrder().getAttendPoliceStationFrequency());
				}
				else {
					a.setAttendPoliceStationFrequency("0");
				}
			}
		});
				
				
		//re-calculate order type
		List<OrderTypeAnnotations> orderTypeAnnotations = jsonCourtOrder.getOrder().getOrderTypeAnnotations();
		Map<String,Integer> orderTypeCountMap = new HashMap<>();
		orderTypeAnnotations.forEach(oType->{
			String orderType = oType.getOrderType().getDisplayValue();
			if (!orderTypeCountMap.containsKey(orderType)) {  // first time we've seen this string
				orderTypeCountMap.put(orderType, 1);
		    }
		    else {
		      int count = orderTypeCountMap.get(orderType);
		      orderTypeCountMap.put(orderType, count + 1);
		    }
		});	
		String maxOrderType=Collections.max(orderTypeCountMap.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
		jsonCourtOrder.getOrder().setOrderType(getOrderTypeForDisplayValue(maxOrderType));
		
		//re-calculate bond Amount
		List<BondAmountAnnotations> bondAmountAnnotations = jsonCourtOrder.getOrder().getBondAmountAnnotations();
		Map<String,Integer> bondAmountCountMap = new HashMap<>();
		bondAmountAnnotations.forEach(bAmount->{
			String bondAmount = bAmount.getBondAmount();
			if (!bondAmountCountMap.containsKey(bondAmount)) {  // first time we've seen this string
				bondAmountCountMap.put(bondAmount, 1);
		    }
		    else {
		      int count = bondAmountCountMap.get(bondAmount);
		      bondAmountCountMap.put(bondAmount, count + 1);
		    }
		});	
		String maxBondAmount=Collections.max(bondAmountCountMap.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
		jsonCourtOrder.getOrder().setBondAmount(maxBondAmount);
		
		//re-calculate attend police station recurrence
		List<AttendPoliceStationRecurrenceAnnotations> attendPoliceStationRecurrenceAnnotations = 
				jsonCourtOrder.getOrder().getAttendPoliceStationRecurrenceAnnotations();
		Map<String,Integer> attendRecurrenceCountMap = new HashMap<>();
		attendPoliceStationRecurrenceAnnotations.forEach(attReccurrence->{
			String attendReccurrence = attReccurrence.getAttendPoliceStationRecurrence().getDisplayValue();
			if (!attendRecurrenceCountMap.containsKey(attendReccurrence)) {  // first time we've seen this string
				attendRecurrenceCountMap.put(attendReccurrence, 1);
		    }
		    else {
		      int count = attendRecurrenceCountMap.get(attendReccurrence);
		      attendRecurrenceCountMap.put(attendReccurrence, count + 1);
		    }
		});	
		String maxAttendRecurrence=Collections.max(attendRecurrenceCountMap.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
		jsonCourtOrder.getOrder().setAttendPoliceStationRecurrence(getAttendPoliceStationRecurrenceForDisplayValue(maxAttendRecurrence));
		
	
		//re-calculate attend police station frequency
		List<AttendPoliceStationFrequencyAnnotations> attendPoliceStationFrequencyAnnotations = 
				jsonCourtOrder.getOrder().getAttendPoliceStationFrequencyAnnotations();
		Map<String,Integer> attendFrequencyCountMap = new HashMap<>();
		attendPoliceStationFrequencyAnnotations.forEach(attFrequency->{
			String attendFrequency = attFrequency.getAttendPoliceStationFrequency();
			if (!attendFrequencyCountMap.containsKey(attendFrequency)) {  // first time we've seen this string
				attendFrequencyCountMap.put(attendFrequency, 1);
		    }
		    else {
		      int count = attendFrequencyCountMap.get(attendFrequency);
		      attendFrequencyCountMap.put(attendFrequency, count + 1);
		    }
		});	
		String maxAttendFrequency=Collections.max(attendFrequencyCountMap.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
		jsonCourtOrder.getOrder().setAttendPoliceStationFrequency(maxAttendFrequency);
		
	}
	
	private ArgumentSentenceType getArgumentSentenceTypeForDisplayValue(String displayValue) {
		ArgumentSentenceType sentType= ArgumentSentenceType.TBD;
		if(StringUtils.equalsIgnoreCase(displayValue, ArgumentSentenceType.PREMISE.getDisplayValue())) {
			sentType = ArgumentSentenceType.PREMISE;
		}else if(StringUtils.equalsIgnoreCase(displayValue, ArgumentSentenceType.CONCLUSION.getDisplayValue())) {
			sentType = ArgumentSentenceType.CONCLUSION;
		}else if(StringUtils.equalsIgnoreCase(displayValue, ArgumentSentenceType.TBD.getDisplayValue())) {
			sentType = ArgumentSentenceType.TBD;
		}
		return sentType;
	}
	
	private ArgumentBy getArgumentByForDisplayValue(String displayValue) {
		ArgumentBy argBy = ArgumentBy.TBD;
		if(StringUtils.equalsIgnoreCase(displayValue, ArgumentBy.JUDGE.getDisplayValue())) {
			argBy=ArgumentBy.JUDGE;
		}else if (StringUtils.equalsIgnoreCase(displayValue, ArgumentBy.APPLICANT.getDisplayValue())) {
			argBy=ArgumentBy.APPLICANT;
		}else if (StringUtils.equalsIgnoreCase(displayValue, ArgumentBy.RESPONDENT.getDisplayValue())) {
			argBy=ArgumentBy.RESPONDENT;
		}else if (StringUtils.equalsIgnoreCase(displayValue, ArgumentBy.TBD.getDisplayValue())) {
			argBy=ArgumentBy.TBD;
		}
		return argBy;
	}
	
	private OrderType getOrderTypeForDisplayValue(String displayValue) {
		OrderType orderType = OrderType.TBD;
		if(StringUtils.equalsIgnoreCase(displayValue, OrderType.ACCEPTED.getDisplayValue())) {
			orderType=OrderType.ACCEPTED;
		}else if(StringUtils.equalsIgnoreCase(displayValue, OrderType.REJECTED.getDisplayValue())) {
			orderType=OrderType.REJECTED;
		}else if(StringUtils.equalsIgnoreCase(displayValue, OrderType.TBD.getDisplayValue())) {
			orderType=OrderType.TBD;
		}
		return orderType;
	}
	
	private AttendPoliceStationRecurrence getAttendPoliceStationRecurrenceForDisplayValue(String displayValue) {
		AttendPoliceStationRecurrence attendPoliceStationRecurrence = AttendPoliceStationRecurrence.NA;
		if(StringUtils.equalsIgnoreCase(displayValue, AttendPoliceStationRecurrence.DAILY.getDisplayValue())) {
			attendPoliceStationRecurrence=AttendPoliceStationRecurrence.DAILY;
		}else if(StringUtils.equalsIgnoreCase(displayValue, AttendPoliceStationRecurrence.WEEKLY.getDisplayValue())) {
			attendPoliceStationRecurrence=AttendPoliceStationRecurrence.WEEKLY;
		}else if(StringUtils.equalsIgnoreCase(displayValue, AttendPoliceStationRecurrence.MONTHLY.getDisplayValue())) {
			attendPoliceStationRecurrence=AttendPoliceStationRecurrence.MONTHLY;
		}else if(StringUtils.equalsIgnoreCase(displayValue, AttendPoliceStationRecurrence.NA.getDisplayValue())) {
			attendPoliceStationRecurrence=AttendPoliceStationRecurrence.NA;
		}
		return attendPoliceStationRecurrence;
	}

}
