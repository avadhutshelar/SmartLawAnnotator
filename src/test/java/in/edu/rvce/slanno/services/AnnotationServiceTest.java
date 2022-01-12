package in.edu.rvce.slanno.services;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import in.edu.rvce.courtorder.Background;
import in.edu.rvce.courtorder.JsonCourtOrder;
import in.edu.rvce.slanno.entities.LegalAct;
import in.edu.rvce.slanno.entities.SystemSetting;
import in.edu.rvce.slanno.utils.ApplicationConstants;

@SpringBootTest
public class AnnotationServiceTest {

	@Autowired
	private AnnotationService annotationService;

	@MockBean
	private SettingsService settingsService;

	@Test
	public void updateSectionReferenceTest() throws Exception {
		SystemSetting systemSetting = new SystemSetting(ApplicationConstants.SECTION_ABBR_LIST,
				"under section,under sections,u.s.,u/s,read with,r/w,r.w.,read with section,read with sections,r/w. s.,r.w.s.,under sec/s.");
		List<SystemSetting> systemSettingList = new ArrayList<>();
		systemSettingList.add(systemSetting);

		when(settingsService.getSystemSettings()).thenReturn(systemSettingList);
		
		List<LegalAct> legalActList = new ArrayList<>();
		LegalAct la1=new LegalAct(1L,"Indian Penal Code","IPC,I.P.C.","1978","1","400");
		LegalAct la2=new LegalAct(1L,"Code of Criminal Procedure","crpc,cr.p.c.","1978","1","400");
		legalActList.add(la1);
		legalActList.add(la2);
		
		when(settingsService.getLegalActs()).thenReturn(legalActList);
		
		JsonCourtOrder jsonCourtOrder = new JsonCourtOrder();
		//jsonCourtOrder.setBackground(new Background("ORDER BELOW EXH. 1 Applicant ­ Rahul Sakharam Ingle has filed this application "
		//		+ "under Section 438 of the Code of Criminal Procedure for the grant of Anticipatory Bail in connection with C.R.No. 217/2018"
		//		+ " under Section 323, 326, 336, 337, 504 and 506 of the Indian Penal Code."));

		jsonCourtOrder.setBackground(new Background("ORDER BELOW EXH. 1 1] This is an application by Pravin Bharat Deokar for regular bail "
				+ "under Section 439 of Code of Criminal Procedure in respect of Crime No.124/2018 registered "
				+ "under Sections 498­A, 306 read with Section 34 of Indian Penal Code at Police Station, Chikhali, Pimpri, Pune."
				));
		
		annotationService.updateSectionReference(jsonCourtOrder);

	}
}
