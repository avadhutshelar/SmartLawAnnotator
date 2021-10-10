package in.edu.rvce.courtorder;

import java.util.List;

import in.edu.rvce.slanno.entities.LegalAct;
import in.edu.rvce.slanno.entities.LegalProcedure;
import lombok.Data;

@Data
public class Background {
	
	String text;
	List<LegalProcedure> legalProcedureReference;
	List<LegalAct> legalActReference;
	
	public Background(String text) {
		super();
		this.text=text;
	}
}
