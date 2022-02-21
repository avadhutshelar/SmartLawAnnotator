package in.edu.rvce.slanno.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonEncoding;

import in.edu.rvce.courtorder.JsonCourtOrder;
import in.edu.rvce.courtorder.annotations.AnnotationProcessingStageAnnotations;
import in.edu.rvce.courtorder.annotations.ArgumentByAnnotations;
import in.edu.rvce.courtorder.annotations.ArgumentSentenceTypeAnnotations;
import in.edu.rvce.courtorder.annotations.AttendPoliceStationFrequencyAnnotations;
import in.edu.rvce.courtorder.annotations.AttendPoliceStationRecurrenceAnnotations;
import in.edu.rvce.courtorder.annotations.BondAmountAnnotations;
import in.edu.rvce.courtorder.annotations.OrderTypeAnnotations;
import in.edu.rvce.slanno.entities.LegalDocument;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.enums.AnnotationProcessingStage;
import in.edu.rvce.slanno.enums.ArgumentSentenceType;
import in.edu.rvce.slanno.enums.AttendPoliceStationRecurrence;
import in.edu.rvce.slanno.enums.OrderType;

@Service
public class AnnotationProcessingStageService {

	public JsonCourtOrder initializeAnnotationProcessingStage(JsonCourtOrder jsonCourtOrder, Project project, 
			AnnotationProcessingStage annotationProcessingStage) {
		try {	
			String annotatorUserListString=project.getAnnotatorUserListString();
			List<String> annotatorUserList= Arrays.asList(annotatorUserListString.split(",")); 
		
			jsonCourtOrder.setAnnotationProcessingStage(annotationProcessingStage);
			List<AnnotationProcessingStageAnnotations> annotationProcessingStageAnnotations = new ArrayList<>();			
			annotatorUserList.forEach(username->{
				annotationProcessingStageAnnotations.add(new AnnotationProcessingStageAnnotations(username, annotationProcessingStage));				
			});
			
			jsonCourtOrder.setAnnotationProcessingStageAnnotations(annotationProcessingStageAnnotations);
			
		}catch(Exception e) {
			System.out.println(e.getStackTrace());
		}
		return jsonCourtOrder;
	}
	
	public void getUpdatedAnnotationProcessingStageByUser(JsonCourtOrder jsonCourtOrder, Authentication authentication) {
		List<AnnotationProcessingStageAnnotations> annotationProcessingStageAnnotations = jsonCourtOrder.getAnnotationProcessingStageAnnotations();
		annotationProcessingStageAnnotations.forEach(stage->{
			if(StringUtils.equalsIgnoreCase(authentication.getName(), stage.getUsername())) {
				jsonCourtOrder.setAnnotationProcessingStage(stage.getAnnotationProcessingStage());				
			}
		});
	}

	public JsonCourtOrder setAnnotationProcessingStage(JsonCourtOrder jsonCourtOrder, Project project, 
			Authentication authentication, AnnotationProcessingStage annotationProcessingStage) {
		try {	
		
			jsonCourtOrder.getAnnotationProcessingStageAnnotations().forEach(stage->{
				if(StringUtils.equalsIgnoreCase(stage.getUsername(), authentication.getName())) {
					stage.setAnnotationProcessingStage(annotationProcessingStage);
				}
			});		
			
			Map<String,Integer> stageCountMap = new HashMap<>();			
			jsonCourtOrder.getAnnotationProcessingStageAnnotations().forEach(stage->{
				String stageIn = stage.getAnnotationProcessingStage().getDisplayValue();
				if (!stageCountMap.containsKey(stageIn)) {  // first time we've seen this string
					stageCountMap.put(stageIn, 1);
			    }
			    else {
			      int count = stageCountMap.get(stageIn);
			      stageCountMap.put(stageIn, count + 1);
			    }
			});
				
			String maxstageIn=Collections.max(stageCountMap.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
			int count = Collections.max(stageCountMap.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getValue();
			AnnotationProcessingStage stageToCompare = jsonCourtOrder.getAnnotationProcessingStageAnnotations().get(0).getAnnotationProcessingStage();
			if(StringUtils.equalsIgnoreCase(stageToCompare.getDisplayValue(), maxstageIn)
					&& jsonCourtOrder.getAnnotationProcessingStageAnnotations().size() == count) {
				jsonCourtOrder.setAnnotationProcessingStage(stageToCompare);
			}else {
				String minstageIn=Collections.min(stageCountMap.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
				if(minstageIn.equals(AnnotationProcessingStage.STAGE1.getDisplayValue())) {
					jsonCourtOrder.setAnnotationProcessingStage(getAnnotationProcessingStageForDisplayValue(minstageIn));
				}else if(maxstageIn.equals(AnnotationProcessingStage.STAGE2.getDisplayValue())) {
					jsonCourtOrder.setAnnotationProcessingStage(getAnnotationProcessingStageForDisplayValue(maxstageIn));
				}
			}
			
		}catch(Exception e) {
			System.out.println(e.getStackTrace());
		}
		return jsonCourtOrder;
	}
	
	private AnnotationProcessingStage getAnnotationProcessingStageForDisplayValue(String displayValue) {
		AnnotationProcessingStage stage = AnnotationProcessingStage.TBD;
		if(StringUtils.equalsIgnoreCase(displayValue, AnnotationProcessingStage.STAGE0.getDisplayValue())) {
			stage = AnnotationProcessingStage.STAGE0;
		}else if(StringUtils.equalsIgnoreCase(displayValue, AnnotationProcessingStage.STAGE1.getDisplayValue())) {
			stage = AnnotationProcessingStage.STAGE1;
		}else if(StringUtils.equalsIgnoreCase(displayValue, AnnotationProcessingStage.STAGE2.getDisplayValue())) {
			stage = AnnotationProcessingStage.STAGE2;
		}else if(StringUtils.equalsIgnoreCase(displayValue, AnnotationProcessingStage.TBD.getDisplayValue())) {
			stage = AnnotationProcessingStage.TBD;
		}
		return stage;
	}
}
