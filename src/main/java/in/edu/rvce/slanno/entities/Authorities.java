package in.edu.rvce.slanno.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "authorities")
@Getter
@Setter
public class Authorities {
	
	@Id
	String username;
	String authority;

	public Authorities() {
		
	}
}
