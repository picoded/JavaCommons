package picoded.webTemplateEngines;

import java.util.Map;

public class FormInputTemplates {
	public static String inputTextKey = "text";

	public static FormInputInterface titleInput = (node)->{ 
		String text = node.getString(inputTextKey, "");
		//do any additional stuff as needed
		return text; 
	};
	
	public static FormInputInterface divInput = (node)->{
		//do any additional stuff as needed
		return ""; 
	};
	
	public static Map<String, FormInputInterface> defaultInputInterfaces() {
		return null;
	}
}
