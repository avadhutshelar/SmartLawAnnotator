package in.edu.rvce.slanno.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class Users {
	
	@Id
	String username;
	String password;
	Boolean enabled;
	
	public Users() {
		
	}
}
