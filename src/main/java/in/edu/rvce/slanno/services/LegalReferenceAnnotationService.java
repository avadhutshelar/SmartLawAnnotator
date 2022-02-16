package in.edu.rvce.slanno.services;

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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import in.edu.rvce.courtorder.Background;
import in.edu.rvce.courtorder.JsonCourtOrder;
import in.edu.rvce.courtorder.LegalReference;
import in.edu.rvce.courtorder.annotations.LegalRefAcceptRejectDecisionAnnotations;
import in.edu.rvce.slanno.dto.LegalActFound;
import in.edu.rvce.slanno.entities.LegalAct;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.entities.SystemSetting;
import in.edu.rvce.slanno.enums.LegalRefAcceptRejectDecision;
import in.edu.rvce.slanno.utils.ApplicationConstants;

@Service
public class LegalReferenceAnnotationService {
	
	@Autowired
	private SettingsService settingsService;


	public JsonCourtOrder initializeSectionReference(JsonCourtOrder jsonCourtOrder, Project project) {
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

	public void getUpdatedLegalRefsByUser(JsonCourtOrder jsonCourtOrder, Authentication authentication) {
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
}
