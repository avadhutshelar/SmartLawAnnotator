package in.edu.rvce.slanno.services;

import java.util.List;

import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.edu.rvce.slanno.entities.LegalAct;
import in.edu.rvce.slanno.entities.SystemSetting;
import in.edu.rvce.slanno.repositories.LegalActRepository;
import in.edu.rvce.slanno.repositories.SystemSettingRepository;

@Service
public class SettingsService {

	@Autowired
	private LegalActRepository legalActRepository;
	
	@Autowired
	private SystemSettingRepository systemSettingRepository;
	
	public LegalAct createActs(LegalAct legalAct) {
		return legalActRepository.save(legalAct);
	}

	public List<LegalAct> getLegalActs() throws Exception {
		List<LegalAct> legalActList = Lists.newArrayList(legalActRepository.findAll());
		return legalActList;
	}
	
	public void createSystemSetting(SystemSetting systemSetting) {
		systemSettingRepository.save(systemSetting);
	}
	
	public List<SystemSetting> getSystemSettings() throws Exception{
		List<SystemSetting> systemSettingList = Lists.newArrayList(systemSettingRepository.findAll());
		return systemSettingList;
	}
	
}
