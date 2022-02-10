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
import in.edu.rvce.courtorder.annotations.LegalRefAcceptRejectDecisionAnnotations;
import in.edu.rvce.slanno.dto.LegalActFound;
import in.edu.rvce.slanno.entities.LegalAct;
import in.edu.rvce.slanno.entities.LegalDocument;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.entities.SystemSetting;
import in.edu.rvce.slanno.enums.ArgumentBy;
import in.edu.rvce.slanno.enums.ArgumentSentenceType;
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
	private SettingsService settingsService;

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
			if(CollectionUtils.isEmpty(jsonCourtOrder.getBackground().getLegalReferences())) {
				updateSectionReference(jsonCourtOrder,project);
			}		
			getUpdatedLegalRefsByUser(jsonCourtOrder, authentication);
			getUpdatedArgumentsByUser(jsonCourtOrder, authentication);
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

	public JsonCourtOrder updateSectionReference(JsonCourtOrder jsonCourtOrder, Project project) {
		try {
			if (jsonCourtOrder.getBackground() != null) {
				Background background = jsonCourtOrder.getBackground();
				String backgroundText = background.getText();
				List<SystemSetting> systemSettingList = settingsService.getSystemSettings();
				SystemSetting sectionSetting = systemSettingList.stream()
						.filter(s -> s.getKey().equalsIgnoreCase(ApplicationConstants.SECTION_ABBR_LIST)).findFirst()
						.get();
				String[] sectionAbbrArray = sectionSetting.getValue().split(",");
				List<String> sectionAbbrList = Arrays.asList(sectionAbbrArray);
				
				List<String> sectionAbbrFound = new ArrayList<>();

				for (String sectionAbbr : sectionAbbrList) {
					if (StringUtils.containsIgnoreCase(backgroundText, sectionAbbr)) {
						sectionAbbrFound.add(sectionAbbr);
					}
				}

				List<LegalAct> legalActList = settingsService.getLegalActs();
				List<LegalActFound> legalActFound = new ArrayList<>(); 
				
				for (LegalAct legalAct : legalActList) {
					LegalActFound laFound = new LegalActFound();
					if (StringUtils.containsIgnoreCase(backgroundText, legalAct.getActName())) {
						laFound.setLegalAct(legalAct);
						laFound.setActNameMatched(legalAct.getActName());						
						legalActFound.add(laFound);
					}
					String[] legalActShortNameArray = legalAct.getActShortNameList().split(",");
					List<String> legalActShortNameList = Arrays.asList(legalActShortNameArray);
					for(String legalActShortName:legalActShortNameList) {
						if (StringUtils.containsIgnoreCase(backgroundText, legalActShortName)) {
							laFound.setLegalAct(legalAct);
							laFound.setActNameMatched(legalActShortName);						
							legalActFound.add(laFound);
						}
					}
				}
				
				Map<Integer, String> backgroundTextSplitByActFound = new HashMap<>();
				Map<Integer, LegalActFound> actPositionListMap = new HashMap<>();
				for(LegalActFound legalAct: legalActFound) {
					Integer actPosition=StringUtils.indexOf(backgroundText, legalAct.getActNameMatched());					
					if(actPosition!=-1) {
						actPositionListMap.put(actPosition, legalAct);
					}
				}
				List<Integer> actPositionListMapKeys= new ArrayList<Integer>(actPositionListMap.keySet());
				Collections.sort(actPositionListMapKeys);
				int position=0;
				int lastActEndPosition=0;
				for(Integer actPosition:actPositionListMapKeys) {
					int actEndPosition=(actPosition+actPositionListMap.get(actPosition).getActNameMatched().length());
					String temp=StringUtils.substring(backgroundText, lastActEndPosition, actEndPosition);
					backgroundTextSplitByActFound.put(++position, temp);
					lastActEndPosition=actEndPosition;
				}
				
				List<String> legalRefFound = new ArrayList<>();
				
				for(Integer key: backgroundTextSplitByActFound.keySet()) {
					String backText=backgroundTextSplitByActFound.get(key);
					
					for(String sectionAbbr:sectionAbbrFound) {
						if(StringUtils.containsAny(backText.toLowerCase(), sectionAbbr.toLowerCase())) {
							String legalRef = StringUtils.substringAfter(backText.toLowerCase(), sectionAbbr.toLowerCase());
							if(StringUtils.isNotBlank(legalRef)){
								legalRef=sectionAbbr.toLowerCase().concat(legalRef);
								if(!legalRefFound.contains(legalRef)) {
									legalRefFound.add(legalRef);
								}
							}
						}
					}
					
				}
				
				List<String> legalRefFoundCopy = new ArrayList<>();
				legalRefFoundCopy.addAll(legalRefFound);
				List<String> legalRefDuplicate = new ArrayList<>();
				
				
				for(String ref:legalRefFound) {
					for(String refCopy:legalRefFoundCopy) {
						if(StringUtils.containsIgnoreCase(ref, refCopy) && !StringUtils.equalsIgnoreCase(ref, refCopy)) {							
							legalRefDuplicate.add(refCopy);
						}
					}
				}
				legalRefFound.removeAll(legalRefDuplicate);
				
				List<LegalReference> legalRefSectionSeparated= new ArrayList<>();
				int count=0;
				for(String ref:legalRefFound) {
					String refTemp = ref.replaceAll("[^0-9]+", " ");
					List<String> sectionList = Arrays.asList(refTemp.trim().split(" "));
															
					LegalActFound currentLegalAct=null;
					for(LegalActFound legalAct: legalActFound) {
						if(ref.endsWith(legalAct.getLegalAct().getActName().toLowerCase())) {
							currentLegalAct=legalAct;
							currentLegalAct.setStringMatched(ref);
						}
						String[] legalActShortNameArray = legalAct.getLegalAct().getActShortNameList().split(",");
						List<String> legalActShortNameList = Arrays.asList(legalActShortNameArray);						
						for(String legalActShortName:legalActShortNameList) {
							if(StringUtils.endsWith(ref, legalActShortName.toLowerCase())) {
								currentLegalAct=legalAct;
								currentLegalAct.setStringMatched(ref);
							}
						}
					}					
					
					String sectionListString = sectionList.stream().map(i -> i.toString()).collect(Collectors.joining(","));
					currentLegalAct.setSectionsMatched(sectionListString);
					String annotatorUserListString=project.getAnnotatorUserListString();
					List<String> annotatorUserList= Arrays.asList(annotatorUserListString.split(",")); 
					List<LegalRefAcceptRejectDecisionAnnotations> legalRefAcceptRejectDecisionAnnotations = new ArrayList<>();
					annotatorUserList.forEach(username->{legalRefAcceptRejectDecisionAnnotations.add(
							new LegalRefAcceptRejectDecisionAnnotations(username,LegalRefAcceptRejectDecision.TBD));});
					legalRefSectionSeparated.add(new LegalReference(++count,currentLegalAct,LegalRefAcceptRejectDecision.TBD,legalRefAcceptRejectDecisionAnnotations));
					
				}
				
				background.setLegalReferences(legalRefSectionSeparated);
				jsonCourtOrder.setBackground(background);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonCourtOrder;
	}
	
	public JsonCourtOrder addLegalActFound(JsonCourtOrder jsonCourtOrder,LegalActFound legalActFound) {
		
		try {
			List<LegalAct> legalActList = settingsService.getLegalActs();
			Boolean flag=Boolean.FALSE;
			LegalAct legalActTemp= new LegalAct();
			for (LegalAct legalAct: legalActList) {
				String matched = legalActFound.getActNameMatched();
				if(StringUtils.equalsIgnoreCase(matched,legalAct.getActName())) {
					flag=Boolean.TRUE;
					legalActTemp = legalAct;
					break;
				}else if(StringUtils.equalsAnyIgnoreCase(matched, legalAct.getActShortNameList())) {
					flag=Boolean.TRUE;
					legalActTemp = legalAct;
					break;
				}
			}
			if (flag) {
				LegalReference legalReference = new LegalReference();
				legalReference.setLegalActFound(legalActFound);
				legalReference.setLegalRefAcceptRejectDecision(LegalRefAcceptRejectDecision.ACCEPT_SUGGESTION);
				legalActFound.setLegalAct(legalActTemp);
				legalReference.setLegalActFound(legalActFound);
				
				List<LegalReference> legalReferences=jsonCourtOrder.getBackground().getLegalReferences();
				Integer refNumber=1;
				if(CollectionUtils.isNotEmpty(legalReferences)) {					
					LegalReference maxLegalRef=legalReferences.stream().max(Comparator.comparing(LegalReference::getRefNumber)).orElseThrow(NoSuchElementException::new);
					refNumber = maxLegalRef.getRefNumber()+1;
					legalReference.setRefNumber(refNumber);
					legalReferences.add(legalReference);
				}else {
					legalReferences = new ArrayList<>();
					legalReference.setRefNumber(refNumber);
					legalReferences.add(legalReference);
					jsonCourtOrder.getBackground().setLegalReferences(legalReferences);
				}
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonCourtOrder;
	}
	
	private void getUpdatedLegalRefsByUser(JsonCourtOrder jsonCourtOrder, Authentication authentication) {
		jsonCourtOrder.getBackground().getLegalReferences().forEach(legalRef->{			
			List<LegalRefAcceptRejectDecisionAnnotations> legalRefAcceptRejectDecisionAnnotations = legalRef.getLegalRefAcceptRejectDecisionAnnotations();
			legalRefAcceptRejectDecisionAnnotations.forEach(decision->{
				if(StringUtils.equalsIgnoreCase(authentication.getName(), decision.getUsername())) {
					legalRef.setLegalRefAcceptRejectDecision(decision.getLegalRefAcceptRejectDecision());
				}				
			});			
			});
	}
	
	public void updateLegalRefsByUser(JsonCourtOrder jsonCourtOrder, JsonCourtOrder jsonCourtOrderIn, Authentication authentication) {
		//assign legal ref to specific user
		jsonCourtOrder.getBackground().getLegalReferences().forEach( a ->{
			jsonCourtOrderIn.getBackground().getLegalReferences().forEach(ain->{
				if(a.getRefNumber().equals(ain.getRefNumber())) {
					List<LegalRefAcceptRejectDecisionAnnotations> legalRefAcceptRejectDecisionAnnotations = a.getLegalRefAcceptRejectDecisionAnnotations();
					legalRefAcceptRejectDecisionAnnotations.forEach(decision->{
						if(StringUtils.equalsIgnoreCase(authentication.getName(), decision.getUsername())) {
							decision.setLegalRefAcceptRejectDecision(ain.getLegalRefAcceptRejectDecision());
						}
					});					
				}
			});
		});
		//re-calculate the legalref
		jsonCourtOrder.getBackground().getLegalReferences().forEach( a ->{
			List<LegalRefAcceptRejectDecisionAnnotations> legalRefAcceptRejectDecisionAnnotations = a.getLegalRefAcceptRejectDecisionAnnotations();
			Map<String,Integer> legalRefCountMap = new HashMap<>();
			legalRefAcceptRejectDecisionAnnotations.forEach(decision->{
				String refDecision = decision.getLegalRefAcceptRejectDecision().getDisplayValue();
				if (!legalRefCountMap.containsKey(refDecision)) {  // first time we've seen this string
					legalRefCountMap.put(refDecision, 1);
			    }
			    else {
			      int count = legalRefCountMap.get(refDecision);
			      legalRefCountMap.put(refDecision, count + 1);
			    }
			});	
			String maxRefDecision=Collections.max(legalRefCountMap.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
			a.setLegalRefAcceptRejectDecision(getLegalRefAcceptRejectDecisionForDisplayValue(maxRefDecision));
		});
		
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
	
	public void updateOrderByUser(JsonCourtOrder jsonCourtOrder, JsonCourtOrder jsonCourtOrderIn) {
		jsonCourtOrder.getOrder().setOrderType(jsonCourtOrderIn.getOrder().getOrderType());
		if(jsonCourtOrderIn.getOrder().getOrderType().equals(OrderType.ACCEPTED)) {
			jsonCourtOrder.getOrder().setBondAmount(jsonCourtOrderIn.getOrder().getBondAmount());
			jsonCourtOrder.getOrder().setAttendPoliceStationRecurrence(jsonCourtOrderIn.getOrder().getAttendPoliceStationRecurrence());
			jsonCourtOrder.getOrder().setAttendPoliceStationFrequency(jsonCourtOrderIn.getOrder().getAttendPoliceStationFrequency());
		}else {
			jsonCourtOrder.getOrder().setBondAmount(null);
			jsonCourtOrder.getOrder().setAttendPoliceStationRecurrence(null);
			jsonCourtOrder.getOrder().setAttendPoliceStationFrequency(null);
		}
	}
	
	private LegalRefAcceptRejectDecision getLegalRefAcceptRejectDecisionForDisplayValue(String displayValue) {
		LegalRefAcceptRejectDecision dec = LegalRefAcceptRejectDecision.TBD;
		if(StringUtils.equalsIgnoreCase(displayValue, LegalRefAcceptRejectDecision.ACCEPT_SUGGESTION.getDisplayValue())) {
			dec = LegalRefAcceptRejectDecision.ACCEPT_SUGGESTION;
		}else if(StringUtils.equalsIgnoreCase(displayValue, LegalRefAcceptRejectDecision.REJECT_SUGGESTION.getDisplayValue())) {
			dec = LegalRefAcceptRejectDecision.REJECT_SUGGESTION;
		}else if(StringUtils.equalsIgnoreCase(displayValue, LegalRefAcceptRejectDecision.TBD.getDisplayValue())) {
			dec = LegalRefAcceptRejectDecision.TBD;
		}
		return dec;
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
	
}
