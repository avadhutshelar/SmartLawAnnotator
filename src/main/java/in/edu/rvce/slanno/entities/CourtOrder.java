package in.edu.rvce.slanno.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "courtOrder")
@Getter @Setter
public class CourtOrder {
	
	@Id	
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	String id;
	
	@Lob String rawTextOrder;
	@Lob String preProcessedTextOrder;
	@Lob byte[] pdfOrder;
	
	public CourtOrder() {

	}
}
