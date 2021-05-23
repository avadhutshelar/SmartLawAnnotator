package in.edu.rvce.slanno.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import in.edu.rvce.slanno.entities.CourtOrder;
import in.edu.rvce.slanno.repositories.CourtOrderRepository;
import in.edu.rvce.slanno.utils.CommonUtils;
import in.edu.rvce.slanno.utils.SessionMessage;

@Controller
public class CourtOrderTextEditorController {

	@Autowired
	private CourtOrderRepository courtOrderRepository;

	@GetMapping("/courtOrder/edit/textEditor/{id}")
	public String editTextEditor(@PathVariable String id, SessionMessage message, Model model) {
		
		CourtOrder courtOrder = courtOrderRepository.findById(id).get();
		model.addAttribute("courtOrder", courtOrder);
		
		message.setSuccessMessage("");
		message.setErrorMessage("");
		model.addAttribute("message", message);
		
		return "CourtOrderEditTextEditor";
	}
	
	@PostMapping("/courtOrder/update/textEditor")
	public RedirectView updateTextEditor(@ModelAttribute("courtOrder") CourtOrder courtOrder) throws Exception {

		CourtOrder courtOrder1 = courtOrderRepository.findById(courtOrder.getId()).get();
		courtOrder1.setRawTextOrder(courtOrder.getRawTextOrder());
		courtOrderRepository.save(courtOrder1);
		
		return new RedirectView("/courtOrder/edit/textEditor/"+courtOrder.getId());
	}
}
