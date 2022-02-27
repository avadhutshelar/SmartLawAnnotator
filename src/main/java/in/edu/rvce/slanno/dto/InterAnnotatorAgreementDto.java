package in.edu.rvce.slanno.dto;

import lombok.Data;

@Data
public class InterAnnotatorAgreementDto {
	String user1;
	String user2;
	String agreementPercentage;
	
	public InterAnnotatorAgreementDto(String user1, String user2, String agreementPercentage) {
		this.user1 = user1;
		this.user2 = user2;
		this.agreementPercentage = agreementPercentage;
	}	
	
}
