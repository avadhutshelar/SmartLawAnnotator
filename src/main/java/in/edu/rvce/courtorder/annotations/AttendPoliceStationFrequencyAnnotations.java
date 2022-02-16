package in.edu.rvce.courtorder.annotations;

import lombok.Data;

@Data
public class AttendPoliceStationFrequencyAnnotations {

	String username;
	String attendPoliceStationFrequency;

	public AttendPoliceStationFrequencyAnnotations() {

	}

	public AttendPoliceStationFrequencyAnnotations(String username, String attendPoliceStationFrequency) {
		this.username = username;
		this.attendPoliceStationFrequency = attendPoliceStationFrequency;
	}
}
