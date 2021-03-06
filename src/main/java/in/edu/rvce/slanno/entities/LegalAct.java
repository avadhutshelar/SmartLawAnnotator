package in.edu.rvce.slanno.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "legalAct")
@Getter
@Setter
public class LegalAct {

	@Id	
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long actId;
	
	@NotBlank(message = "Act Name is mandatory")	
	String actName;
			
	String actShortNameList;
	
	@NotBlank(message = "Act Year is mandatory")	
	String actYear;

	@NotBlank(message = "Min Section Number is mandatory")	
	String minSectionNumber;
	
	@NotBlank(message = "Max Section Number is mandatory")	
	String maxSectionNumber;
	
	public LegalAct() {
		
	}

	public LegalAct(Long actId, @NotBlank(message = "Act Name is mandatory") String actName, String actShortNameList,
			@NotBlank(message = "Act Year is mandatory") String actYear,
			@NotBlank(message = "Min Section Number is mandatory") String minSectionNumber,
			@NotBlank(message = "Max Section Number is mandatory") String maxSectionNumber) {
		super();
		this.actId = actId;
		this.actName = actName;
		this.actShortNameList = actShortNameList;
		this.actYear = actYear;
		this.minSectionNumber = minSectionNumber;
		this.maxSectionNumber = maxSectionNumber;
	}
	
	

}
