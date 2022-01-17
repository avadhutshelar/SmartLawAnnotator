package in.edu.rvce.courtorder;

import in.edu.rvce.slanno.enums.OrderType;
import lombok.Data;

@Data
public class Order {

	String text;
	OrderType orderType;
		
	public Order(String text) {
		super();
		this.text = text;
	}

	public Order() {
		super();
	}

}
