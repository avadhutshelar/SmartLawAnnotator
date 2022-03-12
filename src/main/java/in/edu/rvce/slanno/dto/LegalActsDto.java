package in.edu.rvce.slanno.dto;


import java.util.List;

import in.edu.rvce.slanno.entities.LegalAct;
import lombok.Data;

@Data
public class LegalActsDto {

	List<LegalAct> legalActList;
	
}
