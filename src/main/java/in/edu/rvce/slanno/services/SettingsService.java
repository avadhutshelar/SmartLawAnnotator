package in.edu.rvce.slanno.services;

import java.util.List;

import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.edu.rvce.slanno.entities.LegalAct;
import in.edu.rvce.slanno.repositories.LegalActRepository;

@Service
public class SettingsService {

	@Autowired
	private LegalActRepository legalActRepository;
	
	public void createActs(LegalAct legalAct) {
		legalActRepository.save(legalAct);
	}

	public List<LegalAct> getLegalActs() throws Exception {
		List<LegalAct> legalActList = Lists.newArrayList(legalActRepository.findAll());
		return legalActList;
	}
}
