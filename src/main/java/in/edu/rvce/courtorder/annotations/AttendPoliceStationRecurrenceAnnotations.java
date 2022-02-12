package in.edu.rvce.courtorder.annotations;

import in.edu.rvce.slanno.enums.AttendPoliceStationRecurrence;
import lombok.Data;

@Data
public class AttendPoliceStationRecurrenceAnnotations {

	String username;
	AttendPoliceStationRecurrence attendPoliceStationRecurrence;
	
	public AttendPoliceStationRecurrenceAnnotations() {
		
	}

	public AttendPoliceStationRecurrenceAnnotations(String username,
			AttendPoliceStationRecurrence attendPoliceStationRecurrence) {
		this.username = username;
		this.attendPoliceStationRecurrence = attendPoliceStationRecurrence;
	}
}
