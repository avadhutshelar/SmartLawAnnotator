package in.edu.rvce.courtorder;

import java.util.List;

import lombok.Data;

@Data
public class JsonCourtOrder {	
	String header;
	Background background;
	List<Argument> arguments;
	Order order;
	String footer;
	String processedText;
}