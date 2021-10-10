package in.edu.rvce.slanno.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
	
	String sectionNumber;
	String actName;
	String actYear;

	@Override
	public String toString() {
		return "section " + sectionNumber + " of " + actName + ", " + actYear;
	}

	public LegalAct(String sectionNumber, String actName, String actYear) {
		super();
		this.sectionNumber = sectionNumber;
		this.actName = actName;
		this.actYear = actYear;
	}

}
