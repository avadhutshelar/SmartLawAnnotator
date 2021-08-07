package in.edu.rvce.courtorder;

import java.util.List;

import lombok.Data;

@Data
public class JsonCourtOrder {
	String processedText;
	String header;
	Background background;
	List<Argument> arguments;
	String order;
	String footer;
}