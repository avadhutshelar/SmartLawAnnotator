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

import in.edu.rvce.courtorder.JsonCourtOrder;
import in.edu.rvce.courtorder.annotations.ArgumentByAnnotations;
import in.edu.rvce.courtorder.annotations.ArgumentSentenceTypeAnnotations;
import in.edu.rvce.courtorder.annotations.LegalRefAcceptRejectDecisionAnnotations;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.enums.ArgumentBy;
import in.edu.rvce.slanno.enums.ArgumentSentenceType;
import in.edu.rvce.slanno.enums.LegalRefAcceptRejectDecision;
import in.edu.rvce.slanno.enums.OrderType;

@Service
public class ArgumentAnnotationService {
	
	public JsonCourtOrder initializeArgumentBySentenceType(JsonCourtOrder jsonCourtOrder, Project project) {
		try {
			
			String annotatorUserListString=project.getAnnotatorUserListString();
			List<String> annotatorUserList= Arrays.asList(annotatorUserListString.split(",")); 
			
			jsonCourtOrder.getArguments().forEach(a -> {
				List<ArgumentByAnnotations> argumentByAnnotations = new ArrayList<>();
				annotatorUserList.forEach(username->{
					argumentByAnnotations.add(new ArgumentByAnnotations(username,ArgumentBy.TBD));
				});			
				a.setArgumentByAnnotations(argumentByAnnotations);
				a.getArgumentSentences().forEach(sent->{
					List<ArgumentSentenceTypeAnnotations> argumentSentenceTypeAnnotations = new ArrayList<>();
					annotatorUserList.forEach(username->{
						argumentSentenceTypeAnnotations.add(new ArgumentSentenceTypeAnnotations(username, ArgumentSentenceType.TBD));
					});
					sent.setArgumentSentenceTypeAnnotations(argumentSentenceTypeAnnotations);
				});
			});
			
		}catch(Exception e) {
			System.out.println(e.getStackTrace());
		}
		return jsonCourtOrder;
	}
	
	public void getUpdatedArgumentsByUser(JsonCourtOrder jsonCourtOrder, String loggedUserName) {
		jsonCourtOrder.getArguments().forEach(a -> {
			List<ArgumentByAnnotations> argumentByAnnotations = a.getArgumentByAnnotations();
			argumentByAnnotations.forEach(argumentByAnno->{
				if(StringUtils.equalsIgnoreCase(loggedUserName, argumentByAnno.getUsername())) {
					a.setArgumentBy(argumentByAnno.getArgumentBy());					
				}
			});			
			a.getArgumentSentences().forEach(sent->{
				List<ArgumentSentenceTypeAnnotations> argumentSentenceTypeAnnotations = sent.getArgumentSentenceTypeAnnotations();
				argumentSentenceTypeAnnotations.forEach(sentType->{
					if(StringUtils.equalsIgnoreCase(loggedUserName, sentType.getUsername())) {
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
								
								List<ArgumentSentenceTypeAnnotations> argumentSentenceTypeAnnotations = s.getArgumentSentenceTypeAnnotations();
								argumentSentenceTypeAnnotations.forEach(sentTypeAnno->{
									if(StringUtils.equalsIgnoreCase(authentication.getName(), sentTypeAnno.getUsername())) {
										sentTypeAnno.setArgumentSentenceType(sin.getArgumentSentenceType());
									}
								});
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
	
	public List<String> getIncompleteAnnotationList(JsonCourtOrder jsonCourtOrder, String loggedUserName) {
		List<String> incompleteAnnotationList = new ArrayList<>();
		//background complete?
		jsonCourtOrder.getBackground().getLegalReferences().forEach(legRef->{
			legRef.getLegalRefAcceptRejectDecisionAnnotations().forEach(decision->{
				if(StringUtils.equals(decision.getUsername(), loggedUserName)
						&& StringUtils.equalsIgnoreCase(decision.getLegalRefAcceptRejectDecision().getDisplayValue(), LegalRefAcceptRejectDecision.TBD.getDisplayValue())) {
					incompleteAnnotationList.add("Background - LegalRef - "+legRef.getRefNumber());
				}
			});
		});
		//Argument annotations complete?
		jsonCourtOrder.getArguments().forEach(arg->{
			arg.getArgumentByAnnotations().forEach(argby->{
				if(StringUtils.equals(argby.getUsername(), loggedUserName)
						&& StringUtils.equalsIgnoreCase(argby.getArgumentBy().getDisplayValue(), ArgumentBy.TBD.getDisplayValue())) {
					incompleteAnnotationList.add("Aggument "+arg.getArgumentNumber()+" - ArgumentBy");
				}
			});
			arg.getArgumentSentences().forEach(sent->{
				sent.getArgumentSentenceTypeAnnotations().forEach(sentType->{
					if(StringUtils.equals(sentType.getUsername(), loggedUserName)
							&& StringUtils.equalsIgnoreCase(sentType.getArgumentSentenceType().getDisplayValue(), ArgumentSentenceType.TBD.getDisplayValue())) {
						incompleteAnnotationList.add("Argument " + arg.getArgumentNumber() + " - Sentence "+ sent.getSentenceNumber() + " - Sentence Type");
					}
				});
			});
		});
		//Order annotatations complete?
		jsonCourtOrder.getOrder().getOrderTypeAnnotations().forEach(orderType->{
			if(StringUtils.equals(orderType.getUsername(), loggedUserName)
					&& StringUtils.equalsIgnoreCase(orderType.getOrderType().getDisplayValue(), OrderType.TBD.getDisplayValue())) {
				incompleteAnnotationList.add("Order - OrderType");
			}
		});
		return incompleteAnnotationList;
	}

	private ArgumentSentenceType getArgumentSentenceTypeForDisplayValue(String displayValue) {
		ArgumentSentenceType sentType= ArgumentSentenceType.TBD;
		if(StringUtils.equalsIgnoreCase(displayValue, ArgumentSentenceType.PREMISE.getDisplayValue())) {
			sentType = ArgumentSentenceType.PREMISE;
		}else if(StringUtils.equalsIgnoreCase(displayValue, ArgumentSentenceType.CONCLUSION.getDisplayValue())) {
			sentType = ArgumentSentenceType.CONCLUSION;
		}else if(StringUtils.equalsIgnoreCase(displayValue, ArgumentSentenceType.NA.getDisplayValue())) {
			sentType = ArgumentSentenceType.NA;
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
