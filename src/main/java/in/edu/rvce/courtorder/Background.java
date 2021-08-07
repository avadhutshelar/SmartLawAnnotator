package in.edu.rvce.courtorder;

import lombok.Data;

@Data
public class Background {
	String text;
	
	public Background(String text) {
		super();
		this.text=text;
	}
}
