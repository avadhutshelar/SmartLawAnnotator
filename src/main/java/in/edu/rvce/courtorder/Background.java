package in.edu.rvce.courtorder;

import java.util.List;

import lombok.Data;

@Data
public class Background {
	
	String text;
	List<LegalReference> legalReferences;
	
	public Background(String text) {
		super();
		this.text=text;
	}
	
	public Background(){
		
	}
}
