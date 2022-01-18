package in.edu.rvce.slanno.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.Assert;

import in.edu.rvce.courtorder.Background;
import in.edu.rvce.courtorder.JsonCourtOrder;
import in.edu.rvce.courtorder.LegalReference;
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
		LegalAct la1=new LegalAct(1L,"Indian Penal Code","IPC,I.P.C.,I.P.C","1978","1","400");
		LegalAct la2=new LegalAct(2L,"Code of Criminal Procedure","crpc,cr.p.c.","1978","1","400");
		LegalAct la3=new LegalAct(3L,"Maharashtra Money Lending Act","MMLA","2014","1","400");
		legalActList.add(la1);
		legalActList.add(la2);
		legalActList.add(la3);
		
		when(settingsService.getLegalActs()).thenReturn(legalActList);
		
		JsonCourtOrder jsonCourtOrder = new JsonCourtOrder();
		jsonCourtOrder.setBackground(new Background("\r\n"
				+ "1] This is second bail application filed by the accused namely, Bembatya (accused no.4) with request to grant him bail,"
				+ " u/s. 439 of the Code of Criminal Procedure-1973 against whom Crime No. 457 of 2018, is"
				+ " registered in the Baramati City police station (District – Pune ), for having committed offence punishable"
				+ " U/ss. 395, 364A, 386, 504, 506 of I.P.C and 39 of Maharashtra Money Lending Act. First bail application was filed before filing of the charge-sheet."));
		
		/*
		 * jsonCourtOrder.setBackground(new
		 * Background("ORDER BELOW EXH. 1 1] This is an application by Pravin Bharat Deokar for regular bail "
		 * +
		 * "under Section 439 of Code of Criminal Procedure in respect of Crime No.124/2018 registered "
		 * +
		 * "under Sections 498­A, 306 read with Section 34 of Indian Penal Code at Police Station, Chikhali, Pimpri, Pune."
		 * ));
		 */
		
		annotationService.updateSectionReference(jsonCourtOrder);
		
		Background updatedBackground= jsonCourtOrder.getBackground();
		List<LegalReference> legalReferenceList = updatedBackground.getLegalReferences();
		List<String> tempList= new ArrayList<>();
		legalReferenceList.forEach(ref->tempList.add("Section/s "+ref.getLegalActFound().getSectionsMatched()+" of the "+ref.getLegalActFound().getLegalAct().getActName()));
		Assert.isTrue(tempList.contains("Section/s 439 of the Code of Criminal Procedure"));
		Assert.isTrue(tempList.contains("Section/s 498,306,34 of the Indian Penal Code"));
	}
}
