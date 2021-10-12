package in.edu.rvce.slanno.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.edu.rvce.slanno.repositories.LegalActRepository;

@Service
public class SettingsService {

	@Autowired
	private LegalActRepository legalActRepository;

}
