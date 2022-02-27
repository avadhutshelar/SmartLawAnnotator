package in.edu.rvce.slanno.dto;

import lombok.Data;

@Data
public class InterAnnotatorAgreementDto {
	String user1;
	String user2;
	Double agreementScore;
	
	public InterAnnotatorAgreementDto(String user1, String user2, Double agreementScore) {
		this.user1 = user1;
		this.user2 = user2;
		this.agreementScore = agreementScore;
	}	
	
}
