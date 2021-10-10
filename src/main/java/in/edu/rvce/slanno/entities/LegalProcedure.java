package in.edu.rvce.slanno.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "legalProcedure")
@Getter
@Setter
public class LegalProcedure {
	
	@Id	
	@GeneratedValue(strategy = GenerationType.AUTO)
	Long procedureId;
	
	String sectionNumber;
	String procedureName;
	String procedureYear;
	
	@Override
	public String toString() {
		return "section " + sectionNumber + " of " + procedureName + ", " + procedureYear;
	}

	public LegalProcedure(String sectionNumber, String procedureName, String procedureYear) {
		super();
		this.sectionNumber = sectionNumber;
		this.procedureName = procedureName;
		this.procedureYear = procedureYear;
	}
	
	
}
