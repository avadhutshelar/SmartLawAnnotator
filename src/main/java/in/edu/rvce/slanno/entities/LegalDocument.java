package in.edu.rvce.slanno.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import in.edu.rvce.slanno.enums.AnnotationProcessingStage;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "legalDocument")
@Getter @Setter
public class LegalDocument {

	@Id	
	@GeneratedValue(strategy = GenerationType.AUTO)
	Long documentId;

	@ManyToOne
	@JoinColumn(name="project_id")
	private Project project_doc;
	
	String pdfFilePath;
	String origTextFilePath;
	String processedTextFilePath;
	String jsonFilePath;
		
	private AnnotationProcessingStage annotationProcessingStage;
	
	public LegalDocument(Project project, String pdfFilePath) {
		super();
		this.project_doc = project;
		this.pdfFilePath = pdfFilePath;
	}	
	
	public LegalDocument() {
		
	}
}
