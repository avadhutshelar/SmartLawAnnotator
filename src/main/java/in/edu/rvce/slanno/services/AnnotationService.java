package in.edu.rvce.slanno.services;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
				List<LegalAct> legalActFound = new ArrayList<>(); 
				
				for (LegalAct legalAct : legalActList) {
					if (StringUtils.containsIgnoreCase(backgroundText, legalAct.getActName())) {
						legalActFound.add(legalAct);
					}
					String[] legalActShortNameArray = legalAct.getActShortNameList().split(",");
					List<String> legalActShortNameList = Arrays.asList(legalActShortNameArray);
					for(String legalActShortName:legalActShortNameList) {
						if (StringUtils.containsIgnoreCase(backgroundText, legalActShortName)) {
							legalActFound.add(legalAct);
						}
					}
				}
				
				List<String> legalRefFound = new ArrayList<>();
				
				for (String sectionAbbr : sectionAbbrFound) {
					for(LegalAct legalAct: legalActFound) {
						String legalRef=StringUtils.substringBetween(backgroundText.toLowerCase(), sectionAbbr.toLowerCase(), legalAct.getActName().toLowerCase());
						if(StringUtils.isNotBlank(legalRef)) {
							legalRef=sectionAbbr.toLowerCase().concat(legalRef).concat(legalAct.getActName()).toLowerCase();
							legalRefFound.add(legalRef);
						}
						String[] legalActShortNameArray = legalAct.getActShortNameList().split(",");
						List<String> legalActShortNameList = Arrays.asList(legalActShortNameArray);						
						for(String legalActShortName:legalActShortNameList) {
							String legalRef1=StringUtils.substringBetween(backgroundText.toLowerCase(), sectionAbbr.toLowerCase(), legalActShortName.toLowerCase());							
							if(StringUtils.isNotBlank(legalRef1)) {
								legalRef1=sectionAbbr.toLowerCase().concat(legalRef1).concat(legalActShortName).toLowerCase();
								legalRefFound.add(legalRef1);
							}
						}
					}
				}
				
				List<String> legalRefFoundCopy = new ArrayList<>();
				legalRefFoundCopy.addAll(legalRefFound);
				List<String> legalRefDuplicate = new ArrayList<>();
				
				List<String> tempList = new ArrayList<>();
				for(String ref:legalRefFound) {
					for(String refCopy:legalRefFoundCopy) {
						if(StringUtils.containsIgnoreCase(ref, refCopy) && !StringUtils.equalsIgnoreCase(ref, refCopy)) {
							for(String  sectionAbbr : sectionAbbrFound) {
								if(StringUtils.containsIgnoreCase(ref, sectionAbbr)) {
									String temp=StringUtils.substringAfter(ref, refCopy);
									String temp1=StringUtils.substringAfter(temp, sectionAbbr);
									tempList.add(sectionAbbr.toLowerCase().concat(temp1));
								}
							}
							legalRefDuplicate.add(ref);
						}
					}
				}
				legalRefFound.addAll(tempList);
				legalRefFound.removeAll(legalRefDuplicate);
				
				List<LegalReference> legalRefSectionSeparated= new ArrayList<>();
				int count=0;
				for(String ref:legalRefFound) {
					String refTemp = ref.replaceAll("[^0-9]+", " ");
					List<String> sectionList = Arrays.asList(refTemp.trim().split(" "));
					
					String startsWithSectionAbbr="";
					for(String  sectionAbbr : sectionAbbrFound) {
						if(StringUtils.startsWith(ref, sectionAbbr)) {
							startsWithSectionAbbr=sectionAbbr;
						}
					}
					
					String endsWithLegalAct="";
					for(LegalAct legalAct: legalActFound) {
						if(ref.endsWith(legalAct.getActName().toLowerCase())) {
							endsWithLegalAct=legalAct.getActName();
						}
						String[] legalActShortNameArray = legalAct.getActShortNameList().split(",");
						List<String> legalActShortNameList = Arrays.asList(legalActShortNameArray);						
						for(String legalActShortName:legalActShortNameList) {
							if(StringUtils.endsWith(ref, legalActShortName.toLowerCase())) {
								endsWithLegalAct=legalActShortName;
							}
						}
					}					
					
					for(String section:sectionList) {
						String temp=startsWithSectionAbbr.concat(" ").concat(section).concat(" ").concat(endsWithLegalAct);
						legalRefSectionSeparated.add(new LegalReference(++count,temp,LegalRefAcceptRejectDecision.TBD));
					}
				}
				
				background.setLegalReferences(legalRefSectionSeparated);
				jsonCourtOrder.setBackground(background);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonCourtOrder;
	}
}
