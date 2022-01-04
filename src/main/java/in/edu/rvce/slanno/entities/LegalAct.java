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
	@GeneratedValue(strategy = GenerationType.AUTO)
	Long actId;
	
	@NotBlank(message = "Section Number is mandatory")	
	String sectionNumber;
	
	@NotBlank(message = "Act Name is mandatory")	
	String actName;
	
	@NotBlank(message = "Act Year is mandatory")	
	String actYear;

	@Override
	public String toString() {
		return "section " + sectionNumber + " of " + actName + ", " + actYear;
	}

	public LegalAct() {
		
	}

}
