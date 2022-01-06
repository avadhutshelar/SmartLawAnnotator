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
@Table(name = "systemSetting")
@Getter
@Setter
public class SystemSetting {

	@Id	
	@GeneratedValue(strategy = GenerationType.AUTO)
	Long settingsId;
	
	@NotBlank(message = "Key is mandatory")	
	String key;
	
	@NotBlank(message = "Value is mandatory")	
	String value;
	
	@NotBlank(message = "Description is mandatory")	
	String description;
	
	public SystemSetting(){
		
	}
}
