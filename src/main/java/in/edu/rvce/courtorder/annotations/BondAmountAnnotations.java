package in.edu.rvce.courtorder.annotations;

import lombok.Data;

@Data
public class BondAmountAnnotations {

	String username;
	String bondAmount;
	
	public BondAmountAnnotations() {
		
	}

	public BondAmountAnnotations(String username,
			String bondAmount) {
		this.username = username;
		this.bondAmount = bondAmount;
	}
}
