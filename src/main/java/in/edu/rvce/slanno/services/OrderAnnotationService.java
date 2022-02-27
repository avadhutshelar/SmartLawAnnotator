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
import in.edu.rvce.courtorder.annotations.AttendPoliceStationFrequencyAnnotations;
import in.edu.rvce.courtorder.annotations.AttendPoliceStationRecurrenceAnnotations;
import in.edu.rvce.courtorder.annotations.BondAmountAnnotations;
import in.edu.rvce.courtorder.annotations.OrderTypeAnnotations;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.enums.AttendPoliceStationRecurrence;
import in.edu.rvce.slanno.enums.OrderType;

@Service
public class OrderAnnotationService {

	public JsonCourtOrder initializeArgumentBySentenceType(JsonCourtOrder jsonCourtOrder, Project project) {
		try {	
			String annotatorUserListString=project.getAnnotatorUserListString();
			List<String> annotatorUserList= Arrays.asList(annotatorUserListString.split(",")); 
		
			List<OrderTypeAnnotations> orderTypeAnnotations = new ArrayList<>();
			List<BondAmountAnnotations> bondAmountAnnotations = new ArrayList<>();
			List<AttendPoliceStationRecurrenceAnnotations> attendPoliceStationRecurrenceAnnotations = new ArrayList<>();
			List<AttendPoliceStationFrequencyAnnotations> attendPoliceStationFrequencyAnnotations= new ArrayList<>();
					
			annotatorUserList.forEach(username->{
				orderTypeAnnotations.add(new OrderTypeAnnotations(username, OrderType.TBD));
				bondAmountAnnotations.add(new BondAmountAnnotations(username, "0"));
				attendPoliceStationRecurrenceAnnotations.add(new AttendPoliceStationRecurrenceAnnotations(username, AttendPoliceStationRecurrence.NA));
				attendPoliceStationFrequencyAnnotations.add(new AttendPoliceStationFrequencyAnnotations(username, "0"));
			});
			
			jsonCourtOrder.getOrder().setOrderTypeAnnotations(orderTypeAnnotations);
			jsonCourtOrder.getOrder().setBondAmountAnnotations(bondAmountAnnotations);
			jsonCourtOrder.getOrder().setAttendPoliceStationRecurrenceAnnotations(attendPoliceStationRecurrenceAnnotations);
			jsonCourtOrder.getOrder().setAttendPoliceStationFrequencyAnnotations(attendPoliceStationFrequencyAnnotations);
			
		}catch(Exception e) {
			System.out.println(e.getStackTrace());
		}
		return jsonCourtOrder;
	}
	
	public void getUpdatedOrderByUser(JsonCourtOrder jsonCourtOrder, String loggedUserName) {
		List<OrderTypeAnnotations> orderTypeAnnotations = jsonCourtOrder.getOrder().getOrderTypeAnnotations();
		orderTypeAnnotations.forEach(oType->{
			if(StringUtils.equalsIgnoreCase(loggedUserName, oType.getUsername())) {
				jsonCourtOrder.getOrder().setOrderType(oType.getOrderType());				
			}
		});
		List<BondAmountAnnotations> bondAmountAnnotations = jsonCourtOrder.getOrder().getBondAmountAnnotations();
		bondAmountAnnotations.forEach(bAmount->{
			if(StringUtils.equalsIgnoreCase(loggedUserName, bAmount.getUsername())) {
				jsonCourtOrder.getOrder().setBondAmount(bAmount.getBondAmount());
			}
		});
		List<AttendPoliceStationRecurrenceAnnotations> attendPoliceStationRecurrenceAnnotations = 
				jsonCourtOrder.getOrder().getAttendPoliceStationRecurrenceAnnotations();
		attendPoliceStationRecurrenceAnnotations.forEach(attendRecurrence->{
			if(StringUtils.equalsIgnoreCase(loggedUserName, attendRecurrence.getUsername())) {
				jsonCourtOrder.getOrder().setAttendPoliceStationRecurrence(attendRecurrence.getAttendPoliceStationRecurrence());
			}
		});
		List<AttendPoliceStationFrequencyAnnotations> attendPoliceStationFrequencyAnnotations=
				jsonCourtOrder.getOrder().getAttendPoliceStationFrequencyAnnotations();
		attendPoliceStationFrequencyAnnotations.forEach(attendFrequency->{
			if(StringUtils.equalsIgnoreCase(loggedUserName, attendFrequency.getUsername())) {
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
