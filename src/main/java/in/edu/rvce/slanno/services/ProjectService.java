package in.edu.rvce.slanno.services;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

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
				
				legalDocument2.setOrigTextFilePath(env.getProperty("slanno.dataset.dir.txt.orig") + "\\"
						+ legalDocument.getDocumentId() + ".txt");
				
				String targetTextFileNameWithPath = env.getProperty("slanno.dataset.basedir") + "\\"
						+ project.getProjectDirectoryName() + "\\" + legalDocument2.getOrigTextFilePath();
				convertPDFtoTextAndSave(targetPDFFileNameWithPath,targetTextFileNameWithPath);
				
				//Mark Stage0 Complete
				legalDocument2.setAnnotationProcessingStage(AnnotationProcessingStage.STAGE0);
				legalDocumentRepository.save(legalDocument2);
			}
		});
	}
	
	private void copyFile(MultipartFile file, String targetFileNameWithPath) {
		try {
			Files.copy(file.getInputStream(), Paths.get(targetFileNameWithPath),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void convertPDFtoTextAndSave(String sourcePDFFileNameWithPath,String targetTextFileNameWithPath) {
		try {
			byte[] fileBytes = Files.readAllBytes(Paths.get(sourcePDFFileNameWithPath));
			PDFtoText pdftotext = new PDFtoText();
			String textCourtOrder = pdftotext.generateTxtFromByteArray(fileBytes);
			
			PrintWriter pw = new PrintWriter(new File(targetTextFileNameWithPath));
			pw.print(CommonUtils.removeUnnecessaryCharacters(textCourtOrder));
			pw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
