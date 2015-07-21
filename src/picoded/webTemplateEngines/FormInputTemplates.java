package picoded.webTemplateEngines;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.hazelcast.instance.Node;

public class FormInputTemplates {
	public static String selectOptionKey = "option";

	//-------------------new structure----------------
	protected static FormInputInterface titleInput = (node)->{ 
		String text = node.getString(FormGenerator.htmlTextKey, "");
		
		StringBuilder sb = new StringBuilder();
		sb.append("<h3>"+text+"</h3>");
		
		return sb.toString(); 
	};
	
	
//	<select class="pf_dropDownClass">
//		<option value="option1">Option 1</option>
//		<option value="option2">Option 2</option>
//	</select>
	@SuppressWarnings("unchecked")
	protected static FormInputInterface selectInput = (node)->{
		StringBuilder sb = new StringBuilder();
		
		String fieldName = "";
		if(node.containsKey("field")){
			fieldName = node.getString("field");
		}else{
			fieldName = node.getString("label").replaceAll("\\s", "").toLowerCase();
		}
		
		sb.append("<select name=\""+fieldName+"\">");
		
		if(node.containsKey("options")){
			Object dropDownObject = node.get("options");
			
			if(dropDownObject instanceof HashMap<?, ?>){
				LinkedHashMap<String, String> dropDownListOptions = (LinkedHashMap<String, String>)node.get("options");
				for(String key:dropDownListOptions.keySet()){
					sb.append("<option value=\""+key+"\">"+dropDownListOptions.get(key)+"</option>");
				}
			} else if(dropDownObject instanceof List<?>){
				List<String> dropDownOptions = (List<String>)dropDownObject;
				for(String str:dropDownOptions){
					String key = str.replaceAll("\\s","").toLowerCase();
					sb.append("<option value=\""+key+"\">"+str+"</option>");
				}
			}
		}
		
		sb.append("</select>");
		
		return sb.toString();
	};
	
	
	
	
	
	//-------------------old structure=----------------
	
//	protected static FormInputInterface titleInput = (node)->{ 
//		String text = node.getString(FormGenerator.htmlTextKey, "");
//		//do any additional stuff as needed
//		return text; 
//	};
	
	protected static FormInputInterface divInput = (node)->{
		//do any additional stuff as needed
		return ""; 
	};
	
//	@SuppressWarnings("unchecked")
//	protected static FormInputInterface selectInput = (node)->{
//		StringBuilder sb = new StringBuilder();
//		if(node.containsKey(selectOptionKey)){
//			Object dropDownObject = node.get(selectOptionKey);
//			
//			if(dropDownObject instanceof HashMap<?, ?>){
//				LinkedHashMap<String, String> dropDownListOptions = (LinkedHashMap<String, String>)node.get(selectOptionKey);
//				for(String key:dropDownListOptions.keySet()){
//					sb.append("<option value=\""+key+"\">"+dropDownListOptions.get(key)+"</option>");
//				}
//			} else if(dropDownObject instanceof List<?>){
//				List<String> dropDownOptions = (List<String>)dropDownObject;
//				for(String str:dropDownOptions){
//					sb.append("<option>"+str+"</option>");
//				}
//			}
//		}
//		
//		return sb.toString();
//	};
	
	//loltastic function name
	protected static FormInputInterface inputInput = (node)->{
		return "";
	};
	
	protected static Map<String, FormInputInterface> defaultInputTemplates() {
		Map<String, FormInputInterface> defaultTemplates = new HashMap<String, FormInputInterface>();
		defaultTemplates.put("title", FormInputTemplates.titleInput);
//		defaultTemplates.put(FormGenerator.divKey, FormInputTemplates.divInput);
		defaultTemplates.put("dropdown", FormInputTemplates.selectInput);
//		defaultTemplates.put(FormGenerator.inputFieldKey, FormInputTemplates.inputInput);
		
		return defaultTemplates;
	}
}
