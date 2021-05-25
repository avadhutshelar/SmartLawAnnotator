package in.edu.rvce.slanno.services;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import in.edu.rvce.slanno.entities.LegalDocument;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.enums.AnnotationProcessingStage;
import in.edu.rvce.slanno.repositories.LegalDocumentRepository;
import in.edu.rvce.slanno.repositories.ProjectRepository;
import in.edu.rvce.slanno.utils.CommonUtils;
import in.edu.rvce.slanno.utils.PDFtoText;

@Service
public class ProjectService {

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private LegalDocumentRepository legalDocumentRepository;

	@Autowired
	private Environment env;

	public String getDatasetBaseDirectory() {
		return env.getProperty("slanno.dataset.basedir");
	}

	public Boolean createDirectories(Project project) throws Exception {

		File datasetBaseDir = new File(env.getProperty("slanno.dataset.basedir"));
		File projectDatasetBaseDir = new File(datasetBaseDir, project.getProjectDirectoryName());
		if (projectDatasetBaseDir.exists()) {
			return Boolean.FALSE;
		} else {
			projectDatasetBaseDir.mkdir();
			File projectImportFormat1Dir = new File(projectDatasetBaseDir,
					env.getProperty("slanno.dataset.import.format1"));
			projectImportFormat1Dir.mkdir();
			File projectOrigTextDir = new File(projectDatasetBaseDir, env.getProperty("slanno.dataset.dir.txt.orig"));
			projectOrigTextDir.mkdir();
			File projectProcessedTextDir = new File(projectDatasetBaseDir,
					env.getProperty("slanno.dataset.dir.txt.processed"));
			projectProcessedTextDir.mkdir();
			return Boolean.TRUE;
		}
	}

	public Boolean createProject(Project project) throws Exception {
		projectRepository.save(project);
		return Boolean.TRUE;
	}

	public List<Project> getAllProjects() throws Exception {
		List<Project> projectList = Lists.newArrayList(projectRepository.findAll());
		return projectList;
	}

	public Project getProjectById(Integer projectId) throws Exception {
		Project project = projectRepository.findById(projectId).get();
		return project;
	}

	public void importDocuments(Project project, MultipartFile[] files) throws Exception {

		Arrays.asList(files).stream().forEach(file -> {
			String filename = file.getOriginalFilename();

			if (filename.endsWith("." + env.getProperty("slanno.dataset.import.format1"))) {

				String pdfFilePath = env.getProperty("slanno.dataset.import.format1") + "\\" + filename;
				LegalDocument legalDocument = new LegalDocument(project, pdfFilePath);
				LegalDocument legalDocument2 = legalDocumentRepository.save(legalDocument);
				legalDocument2.setPdfFilePath(env.getProperty("slanno.dataset.import.format1") + "\\"
						+ legalDocument.getDocumentId() + "." + env.getProperty("slanno.dataset.import.format1"));

				String targetPDFFileNameWithPath = env.getProperty("slanno.dataset.basedir") + "\\"
						+ project.getProjectDirectoryName() + "\\" + legalDocument2.getPdfFilePath();

				copyFile(file, targetPDFFileNameWithPath);

				legalDocument2.setOrigTextFilePath(
						env.getProperty("slanno.dataset.dir.txt.orig") + "\\" + legalDocument.getDocumentId() + ".txt");

				String origTextFileNameWithPath = env.getProperty("slanno.dataset.basedir") + "\\"
						+ project.getProjectDirectoryName() + "\\" + legalDocument2.getOrigTextFilePath();
				
				String textOrder=convertPDFtoText(targetPDFFileNameWithPath);

				legalDocument.setProcessedTextFilePath(env.getProperty("slanno.dataset.dir.txt.processed") + "\\"
						+ legalDocument.getDocumentId() + ".txt");
		
				String processedTextFileNameWithPath = env.getProperty("slanno.dataset.basedir") + "\\"
						+ project.getProjectDirectoryName() + "\\" + legalDocument.getProcessedTextFilePath();
			
				try {
					Files.write(Paths.get(origTextFileNameWithPath), textOrder.getBytes());
					Files.write(Paths.get(processedTextFileNameWithPath), textOrder.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				// Mark Stage0 Complete
				legalDocument2.setAnnotationProcessingStage(AnnotationProcessingStage.STAGE0);
				legalDocumentRepository.save(legalDocument2);
			}
		});
	}

	private void copyFile(MultipartFile file, String targetFileNameWithPath) {
		try {
			Files.copy(file.getInputStream(), Paths.get(targetFileNameWithPath), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String convertPDFtoText(String sourcePDFFileNameWithPath) {
		
		String textCourtOrder="";
		try {
			byte[] fileBytes = Files.readAllBytes(Paths.get(sourcePDFFileNameWithPath));
			PDFtoText pdftotext = new PDFtoText();
			String temp = pdftotext.generateTxtFromByteArray(fileBytes);

			textCourtOrder=CommonUtils.removeUnnecessaryCharacters(temp);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return textCourtOrder;
	}

	public String getLegalDocumentOriginalText(Project project, LegalDocument legalDocument) {
		String originalText = "";

		String origTextFileNameWithPath = env.getProperty("slanno.dataset.basedir") + "\\"
				+ project.getProjectDirectoryName() + "\\" + legalDocument.getOrigTextFilePath();

		try {
			originalText = new String(Files.readAllBytes(Paths.get(origTextFileNameWithPath)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return originalText;
	}
	
	public String getLegalDocumentProcessedText(Project project, LegalDocument legalDocument) {
		String originalText = "";

		String processedTextFileNameWithPath = env.getProperty("slanno.dataset.basedir") + "\\"
				+ project.getProjectDirectoryName() + "\\" + legalDocument.getProcessedTextFilePath();

		try {
			originalText = new String(Files.readAllBytes(Paths.get(processedTextFileNameWithPath)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return originalText;
	}

	public List<LegalDocument> getAllLegalDocumentByProjectId(Integer projectId) throws Exception {
		List<LegalDocument> tempLegalDocumentList = Lists.newArrayList(legalDocumentRepository.findAll());
		List<LegalDocument> legalDocumentList = tempLegalDocumentList.stream()
				.filter(legDoc -> legDoc.getProject_doc().getProjectId() == projectId).collect(Collectors.toList());
		return legalDocumentList;
	}

	public LegalDocument getLegalDocumentByDocumentId(Long documentId) throws Exception {
		LegalDocument legalDocument = legalDocumentRepository.findById(documentId).get();
		return legalDocument;
	}
	
	public void saveUpdatedTextOrder(Project project, LegalDocument legalDocument, String textOrderHidden) {
		try {			
			legalDocument.setProcessedTextFilePath(env.getProperty("slanno.dataset.dir.txt.processed") + "\\"
					+ legalDocument.getDocumentId() + ".txt");
	
			String processedTextFileNameWithPath = env.getProperty("slanno.dataset.basedir") + "\\"
					+ project.getProjectDirectoryName() + "\\" + legalDocument.getProcessedTextFilePath();
		
			Files.write(Paths.get(processedTextFileNameWithPath), textOrderHidden.getBytes());
			
			legalDocumentRepository.save(legalDocument);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
