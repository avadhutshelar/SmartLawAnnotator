package in.edu.rvce.courtorder;

import java.util.List;

import in.edu.rvce.courtorder.annotations.AttendPoliceStationFrequencyAnnotations;
import in.edu.rvce.courtorder.annotations.AttendPoliceStationRecurrenceAnnotations;
import in.edu.rvce.courtorder.annotations.BondAmountAnnotations;
import in.edu.rvce.courtorder.annotations.OrderTypeAnnotations;
import in.edu.rvce.slanno.enums.AttendPoliceStationRecurrence;
import in.edu.rvce.slanno.enums.OrderType;
import lombok.Data;

@Data
public class Order {

	String text;
	OrderType orderType;
	List<OrderTypeAnnotations> orderTypeAnnotations;
	String bondAmount;
	List<BondAmountAnnotations> bondAmountAnnotations;
	AttendPoliceStationRecurrence attendPoliceStationRecurrence;
	List<AttendPoliceStationRecurrenceAnnotations> attendPoliceStationRecurrenceAnnotations;
	String attendPoliceStationFrequency;
	List<AttendPoliceStationFrequencyAnnotations> attendPoliceStationFrequencyAnnotations;
		
	public Order(String text, OrderType orderType) {
		this.text = text;
		this.orderType=orderType;
	}

	public Order() {
	}

}
