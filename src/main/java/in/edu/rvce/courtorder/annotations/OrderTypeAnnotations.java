package in.edu.rvce.courtorder.annotations;

import in.edu.rvce.slanno.enums.OrderType;
import lombok.Data;

@Data
public class OrderTypeAnnotations {

	String username;
	OrderType orderType;
	
	public OrderTypeAnnotations() {
		
	}

	public OrderTypeAnnotations(String username,
			OrderType orderType) {
		this.username = username;
		this.orderType = orderType;
	}
}
