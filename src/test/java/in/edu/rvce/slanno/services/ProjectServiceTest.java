package in.edu.rvce.slanno.services;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.Assert;

import in.edu.rvce.courtorder.ArgumentSentence;
import in.edu.rvce.slanno.entities.SystemSetting;
import in.edu.rvce.slanno.utils.ApplicationConstants;

@SpringBootTest
public class ProjectServiceTest{

	@Autowired
	private ProjectService projectService;

	@MockBean
	private SettingsService settingsService;

	@Test
	public void splitArgumentSentencesTest()  throws Exception{

		SystemSetting systemSetting = new SystemSetting(ApplicationConstants.ABBRS_IN_SENTENCE_LIST, "Ld.");
		List<SystemSetting> systemSettingList = new ArrayList<>();
		systemSettingList.add(systemSetting);

		when(settingsService.getSystemSettings()).thenReturn(systemSettingList);

		List<ArgumentSentence> argumentSentences = projectService.splitArgumentSentences(
				"[2] Perused the application and say filed by the I.O. Heard the Ld. Counsel for the Applicant and Ld. APP for the State.");

		List<String> tempList = new ArrayList<>();
		argumentSentences.forEach(sent -> tempList.add(sent.getText()));
		Assert.isTrue(tempList.contains(
				"[2] Perused the application and say filed by the I.O. Heard the Ld. Counsel for the Applicant and Ld. APP for the State."));
	}
}
