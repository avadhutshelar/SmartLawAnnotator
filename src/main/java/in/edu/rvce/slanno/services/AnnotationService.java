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
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import in.edu.rvce.courtorder.Background;
import in.edu.rvce.courtorder.JsonCourtOrder;
import in.edu.rvce.courtorder.LegalReference;
import in.edu.rvce.slanno.dto.LegalActFound;
import in.edu.rvce.slanno.entities.LegalAct;
import in.edu.rvce.slanno.entities.LegalDocument;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.entities.SystemSetting;
import in.edu.rvce.slanno.enums.LegalRefAcceptRejectDecision;
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

	public JsonCourtOrder getJsonCourtOrder(Project project, LegalDocument legalDocument) {
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
				updateSectionReference(jsonCourtOrder);
			}			
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

	public JsonCourtOrder updateSectionReference(JsonCourtOrder jsonCourtOrder) {
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
					legalRefSectionSeparated.add(new LegalReference(++count,currentLegalAct,LegalRefAcceptRejectDecision.TBD));
					
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
}
