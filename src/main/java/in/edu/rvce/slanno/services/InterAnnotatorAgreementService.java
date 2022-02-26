package in.edu.rvce.slanno.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.edu.rvce.courtorder.JsonCourtOrder;
import in.edu.rvce.slanno.entities.LegalDocument;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.enums.ArgumentBy;
import in.edu.rvce.slanno.enums.OrderType;

@Service
public class InterAnnotatorAgreementService {

	@Autowired
	private ProjectService projectService;

	@Autowired
	private AnnotationService annotationService;

	public Double calculate() throws Exception {
		
		Project project = projectService.getProjectById(3);
		LegalDocument legalDocument = projectService.getLegalDocumentByDocumentId(14l);

		String[] usernamesArray = project.getAnnotatorUserListString().split(",");
		List<String> usernames = Arrays.asList(usernamesArray);
		
		Map<String, Map<String, Integer>> userAnnoLabelCountMap = new HashMap<>();
		usernames.forEach(username -> {
			Map<String, Integer> annoLabelCountMap = initializeAnnoLabelCountMap();
			JsonCourtOrder jsonCourtOrder = annotationService.getJsonCourtOrder(project, legalDocument, username);
			Map<String, Integer> updatedAnnoLabelCountMap = updateAnnoLabelCountMap(annoLabelCountMap, jsonCourtOrder);
			userAnnoLabelCountMap.put(username, updatedAnnoLabelCountMap);
		});

		getCohensKappa (userAnnoLabelCountMap.get("a2"),userAnnoLabelCountMap.get("a3")) ;
		
		Double agreement = 0.0;
		return agreement;
	}
	
	private Double getCohensKappa(Map<String, Integer> user1AnnoLabelCountMap, Map<String, Integer> user2AnnoLabelCountMap) {
		Double kappa, pObserved, pExpected = 0.0;
		
		Double totalRecords=0.0, agreedRecords=0.0;
		for(Map.Entry<String, Integer> user1Entry : user1AnnoLabelCountMap.entrySet()) {
			for(Map.Entry<String, Integer> user2Entry : user2AnnoLabelCountMap.entrySet()) {
				if(user1Entry.getKey().equals(user2Entry.getKey())) {
					totalRecords++;
					if(user1Entry.getValue().equals(user2Entry.getValue())) {
						agreedRecords++;
					}
				}
			}
		}
		
		pObserved = agreedRecords/totalRecords;
		
		Map<String, Double> annotationWiseProbRandomAgreement = new HashMap<>();
		for(Map.Entry<String, Integer> user1Entry : user1AnnoLabelCountMap.entrySet()) {
			for(Map.Entry<String, Integer> user2Entry : user2AnnoLabelCountMap.entrySet()) {
				if(user1Entry.getKey().equals(user2Entry.getKey())) {					
					Double user1LabelTotal = Double.valueOf(user1Entry.getValue());
					Double user2LabelTotal = Double.valueOf(user2Entry.getValue());
					Double probRandomAgreement = (user1LabelTotal/totalRecords) * (user2LabelTotal/totalRecords);
					annotationWiseProbRandomAgreement.put(user1Entry.getKey(), probRandomAgreement);
				}
			}
		}
		
		for (Map.Entry<String, Double> entry : annotationWiseProbRandomAgreement.entrySet()) {
			pExpected = pExpected + entry.getValue();	
		}
		
		//kappa = (pObserved-pExpected)/(1-pExpected)
		kappa = (pObserved-pExpected)/(1-pExpected);
		
		return kappa;
	}

	private Map<String, Integer> initializeAnnoLabelCountMap() {

		Map<String, Integer> annoLabelCountMap = new HashMap<>();

		for (OrderType orderType : OrderType.values()) {
			String oType = "OrderType." + orderType.getDisplayValue();

			if (!annoLabelCountMap.containsKey(oType)) {
				annoLabelCountMap.put(oType, 0);
			} else {
				int count = annoLabelCountMap.get(oType);
				annoLabelCountMap.put(oType, count + 1);
			}
		}
		
		for(ArgumentBy argumentBy : ArgumentBy.values()) {
			String argBy = "ArgumentBy." + argumentBy.getDisplayValue();

			if (!annoLabelCountMap.containsKey(argBy)) {
				annoLabelCountMap.put(argBy, 0);
			} else {
				int count = annoLabelCountMap.get(argBy);
				annoLabelCountMap.put(argBy, count + 1);
			}
		}

		return annoLabelCountMap;
	}

	private Map<String, Integer> updateAnnoLabelCountMap(Map<String, Integer> annoLabelCountMap,
			JsonCourtOrder jsonCourtOrder) {
		Map<String, Integer> updatedAnnoLabelCountMap = annoLabelCountMap;
		String orderTypeLabel = "OrderType."+jsonCourtOrder.getOrder().getOrderType().getDisplayValue();

		if (!updatedAnnoLabelCountMap.containsKey(orderTypeLabel)) {
			//TODO - Logger - System.out.println("Annotation label " + annotationLabel + " not found");
		} else {
			int count = updatedAnnoLabelCountMap.get(orderTypeLabel);
			updatedAnnoLabelCountMap.put(orderTypeLabel, count + 1);
		}
		
		jsonCourtOrder.getArguments().forEach(arg->{
			
			String argumentByLabel = "ArgumentBy."+arg.getArgumentBy().getDisplayValue();

			if (!updatedAnnoLabelCountMap.containsKey(argumentByLabel)) {
				//TODO - Logger - System.out.println("Annotation label " + annotationLabel + " not found");
			} else {
				int count = updatedAnnoLabelCountMap.get(argumentByLabel);
				updatedAnnoLabelCountMap.put(argumentByLabel, count + 1);
			}

		});
		
		
		return updatedAnnoLabelCountMap;
	}
}
