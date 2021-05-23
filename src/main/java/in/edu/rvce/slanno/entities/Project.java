package in.edu.rvce.slanno.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "project")
@Getter @Setter
public class Project {

	@Id	
	@GeneratedValue(strategy = GenerationType.AUTO)
	Integer projectId;
	
	@NotBlank(message = "Project Name is mandatory")
	String projectName;
	
	@NotBlank(message = "Project Directory is mandatory")
	@Size(max = 20)
	String projectDirectoryName;
	
	@OneToMany(
			mappedBy = "project_doc",
			cascade = CascadeType.ALL,
			orphanRemoval = true
	)
	List<LegalDocument> legalDocuments=new ArrayList<>();
	
	public Project() {
		
	}
}
