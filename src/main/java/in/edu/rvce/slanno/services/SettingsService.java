package in.edu.rvce.slanno.services;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import in.edu.rvce.courtorder.JsonCourtOrder;
import in.edu.rvce.slanno.dto.LegalActsDto;
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
	
	@Autowired
	private Environment env;
	
	public void createActs(LegalAct legalAct) throws Exception{
		//legalActRepository.save(legalAct);
		List<LegalAct> legalActList = getLegalActs();
		
		legalActList.add(legalAct);
		
		LegalActsDto legalActsDto= new LegalActsDto();
		legalActsDto.setLegalActList(legalActList);
		String legalActsFileNameWithPath = env.getProperty("slanno.settings.legalActsFileName");

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		// Java objects to File
		try (FileWriter writer = new FileWriter(legalActsFileNameWithPath)) {
			gson.toJson(legalActsDto, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<LegalAct> getLegalActs() throws Exception {
		//List<LegalAct> legalActList = Lists.newArrayList(legalActRepository.findAll());
		
		LegalActsDto legalActsDto= new LegalActsDto();		
		String jsonText = "";

		String legalActsFileNameWithPath = env.getProperty("slanno.settings.legalActsFileName");

		try {
			jsonText = new String(Files.readAllBytes(Paths.get(legalActsFileNameWithPath)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		// File to Java objects
		try {
			legalActsDto = gson.fromJson(jsonText, LegalActsDto.class);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return legalActsDto.getLegalActList();
	}
	
	public void createSystemSetting(SystemSetting systemSetting) {
		systemSettingRepository.save(systemSetting);
	}
	
	public List<SystemSetting> getSystemSettings() throws Exception{
		List<SystemSetting> systemSettingList = Lists.newArrayList(systemSettingRepository.findAll());
		return systemSettingList;
	}
	
}
