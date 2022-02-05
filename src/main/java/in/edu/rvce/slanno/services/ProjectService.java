package in.edu.rvce.slanno.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import in.edu.rvce.courtorder.Argument;
import in.edu.rvce.courtorder.ArgumentSentence;
import in.edu.rvce.courtorder.Background;
import in.edu.rvce.courtorder.JsonCourtOrder;
import in.edu.rvce.courtorder.Order;
import in.edu.rvce.slanno.entities.LegalDocument;
import in.edu.rvce.slanno.entities.Project;
import in.edu.rvce.slanno.entities.SystemSetting;
import in.edu.rvce.slanno.enums.AnnotationProcessingStage;
import in.edu.rvce.slanno.enums.ArgumentBy;
import in.edu.rvce.slanno.enums.ArgumentSentenceType;
import in.edu.rvce.slanno.enums.OrderType;
import in.edu.rvce.slanno.repositories.LegalDocumentRepository;
import in.edu.rvce.slanno.repositories.ProjectRepository;
import in.edu.rvce.slanno.utils.ApplicationConstants;
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

	@Autowired
	private SettingsService settingsService;

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
			File projectJsonDir = new File(projectDatasetBaseDir, env.getProperty("slanno.dataset.dir.json"));
			projectJsonDir.mkdir();
			return Boolean.TRUE;
		}
	}

	public Boolean createOrUpdateProject(Project project) throws Exception {
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

				String textOrder = convertPDFtoText(targetPDFFileNameWithPath);

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

		String textCourtOrder = "";
		try {
			byte[] fileBytes = Files.readAllBytes(Paths.get(sourcePDFFileNameWithPath));
			PDFtoText pdftotext = new PDFtoText();
			String temp = pdftotext.generateTxtFromByteArray(fileBytes);

			textCourtOrder = CommonUtils.removeUnnecessaryCharacters(temp);

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
				.filter(legDoc -> legDoc.getProject().getProjectId() == projectId).collect(Collectors.toList());
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

	public void saveJsonOrder(Project project, LegalDocument legalDocument) {
		try {

			String processedText = "";
			String processedTextFileNameWithPath = env.getProperty("slanno.dataset.basedir") + "\\"
					+ project.getProjectDirectoryName() + "\\" + legalDocument.getProcessedTextFilePath();

			try {
				processedText = new String(Files.readAllBytes(Paths.get(processedTextFileNameWithPath)));
			} catch (IOException e) {
				e.printStackTrace();
			}

			JsonCourtOrder co = getJsonCourtOrder(processedText);

			legalDocument.setJsonFilePath(
					env.getProperty("slanno.dataset.dir.json") + "\\" + legalDocument.getDocumentId() + ".json");

			String jsonFileNameWithPath = env.getProperty("slanno.dataset.basedir") + "\\"
					+ project.getProjectDirectoryName() + "\\" + legalDocument.getJsonFilePath();

			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			// Java objects to File
			try (FileWriter writer = new FileWriter(jsonFileNameWithPath)) {
				gson.toJson(co, writer);
			} catch (IOException e) {
				e.printStackTrace();
			}

			legalDocumentRepository.save(legalDocument);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JsonCourtOrder getJsonCourtOrder(String processedText) {
		JsonCourtOrder co = new JsonCourtOrder();
		co.setProcessedText(processedText);

		String headerEnds = ApplicationConstants.HEADER_ENDS;
		String header = StringUtils.substring(processedText, 0,
				StringUtils.indexOfIgnoreCase(processedText, headerEnds));
		co.setHeader(header.trim());

		String backgroundEnds = ApplicationConstants.BACKGROUND_ENDS;
		String background = StringUtils.substring(processedText,
				StringUtils.indexOfIgnoreCase(processedText, headerEnds) + headerEnds.length(),
				StringUtils.indexOfIgnoreCase(processedText, backgroundEnds));
		Background back = new Background(background.trim());
		co.setBackground(back);

		String argumentEnds = ApplicationConstants.ARGUMENT_ENDS;
		String argument = StringUtils.substring(processedText,
				StringUtils.indexOfIgnoreCase(processedText, backgroundEnds) + backgroundEnds.length(),
				StringUtils.lastIndexOfIgnoreCase(processedText, argumentEnds));
		String[] argumentTextArray = argument.split(argumentEnds, -1);
		List<String> argumentTextList = Arrays.asList(argumentTextArray);
		List<Argument> argumentList = new ArrayList<>();
		int count = 0;
		for (String argumentText : argumentTextList) {
			Argument arg = new Argument();
			arg.setArgumentNumber(++count);
			arg.setText(argumentText.trim());
			List<ArgumentSentence> argumentSentences = splitArgumentSentences(argumentText);
			arg.setArgumentSentences(argumentSentences);
			arg.setArgumentBy(ArgumentBy.TBD);
			argumentList.add(arg);
		}
		co.setArguments(argumentList);

		String orderEnds = ApplicationConstants.ORDER_ENDS;
		String orderText = StringUtils.substring(processedText,
				StringUtils.lastIndexOfIgnoreCase(processedText, argumentEnds) + argumentEnds.length(),
				StringUtils.indexOfIgnoreCase(processedText, orderEnds));
		Order order= new Order(orderText.trim(),OrderType.TBD);
		co.setOrder(order);

		String footer = StringUtils.substring(processedText,
				StringUtils.lastIndexOfIgnoreCase(processedText, orderEnds) + orderEnds.length(),
				processedText.length());
		co.setFooter(footer.trim());

		return co;
	}

	public List<ArgumentSentence> splitArgumentSentences(String text) {
		List<ArgumentSentence> argumentSentences = new ArrayList<ArgumentSentence>(0);

		try {
			// set up pipeline properties
			Properties props = new Properties();
			// set the list of annotators to run
			props.setProperty("annotators", "tokenize,ssplit");
			props.setProperty("rulesFiles", "G:/git/SmartLawAnnotator/src/main/resources/basic_ner.rules");
			// build pipeline
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
			// create a document object
			CoreDocument doc = new CoreDocument(text);
			// annotate
			pipeline.annotate(doc);
			// display sentences
			int count = 0;
			ArgumentSentence argumentSentenceLast = new ArgumentSentence();

			List<SystemSetting> systemSettingList = settingsService.getSystemSettings();
			SystemSetting abbrInSentenceSetting = systemSettingList.stream()
					.filter(s -> s.getKey().equalsIgnoreCase(ApplicationConstants.ABBRS_IN_SENTENCE_LIST)).findFirst()
					.get();
			String[] abbrInSentenceArray = abbrInSentenceSetting.getValue().split(",");
			List<String> abbrInSentenceList = Arrays.asList(abbrInSentenceArray);

			for (CoreSentence sent : doc.sentences()) {
				// System.out.println(sent.text());
				String lastSentText = argumentSentenceLast.getText();
				if (count > 0 && abbrInSentenceList.stream().anyMatch(abbr -> lastSentText.endsWith(abbr))) {
					argumentSentences.forEach(argSent -> {
						if (StringUtils.equalsIgnoreCase(lastSentText, argSent.getText())) {
							argSent.setText(argSent.getText().concat(" ").concat(sent.text()));
						}
					});
				} else {
					ArgumentSentence argumentSentenceCurrent = new ArgumentSentence();
					argumentSentenceCurrent.setSentenceNumber(++count);
					argumentSentenceCurrent.setText(sent.text());
					argumentSentenceCurrent.setArgumentSentenceType(ArgumentSentenceType.TBD);
					argumentSentences.add(argumentSentenceCurrent);
					argumentSentenceLast = argumentSentenceCurrent;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return argumentSentences;
	}
	
	/*
	 * public List<ProjectAnnotator> getAllProjectAnnotators() throws Exception {
	 * List<ProjectAnnotator> projectAnnotatorList =
	 * Lists.newArrayList(projectAnnotatorRepository.findAll()); return
	 * projectAnnotatorList; }
	 * 
	 * public void addOrUpdateProjectAnnotator(ProjectAnnotator projectAnnotator) {
	 * projectAnnotatorRepository.save(projectAnnotator); }
	 */
}
