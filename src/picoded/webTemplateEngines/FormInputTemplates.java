package picoded.webTemplateEngines;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import picoded.conv.RegexUtils;

import com.hazelcast.instance.Node;

public class FormInputTemplates {
	
	protected static FormInputInterface header = (node)->{ 
		String text = node.getString("text", "");
		
		StringBuilder sb = new StringBuilder();
		
		String inputClassString = FormGenerator.getInputClassString(node);
		String inputCssString = FormGenerator.getInputCssString(node);
		
		sb.append("<h3"+inputClassString+inputCssString+">"+text+"</h3>");
		
		return sb.toString(); 
	};
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface select = (node)->{
		StringBuilder sb = new StringBuilder();
		
		String inputClassString = FormGenerator.getInputClassString(node);
		
		String fieldName = "";
		if(node.containsKey("field")){
			fieldName = " name=\""+node.getString("field")+"\"";
		}else if(node.containsKey("label")){
			fieldName = " name=\""+RegexUtils.removeAllNonAlphaNumeric(node.getString("label")).toLowerCase()+"\"";
		}
		
		sb.append("<select"+inputClassString+fieldName+">");
		
		if(node.containsKey("options")){
			Object dropDownObject = node.get("options");
			
			//if what is passed in is a Map, assume it is LinkedHashMap, to maintain insertion order
			if(dropDownObject instanceof HashMap<?, ?>){
				LinkedHashMap<String, String> dropDownListOptions = (LinkedHashMap<String, String>)node.get("options");
				for(String key:dropDownListOptions.keySet()){
					sb.append("<option value=\""+key+"\">"+dropDownListOptions.get(key)+"</option>");
				}
			} else if(dropDownObject instanceof List<?>){
				List<String> dropDownOptions = (List<String>)dropDownObject;
				for(String str:dropDownOptions){
					String key = RegexUtils.removeAllNonAlphaNumeric(str).toLowerCase();
					sb.append("<option value=\""+key+"\">"+str+"</option>");
				}
			}
		}
		
		sb.append("</select>");
		
		return sb.toString();
	};
	
	protected static FormInputInterface input_text = (node)->{
		StringBuilder sb = new StringBuilder();
		
		String inputClassString = FormGenerator.getInputClassString(node);
		
		sb.append("<input"+inputClassString+" type=\"text\" ");
		
		if(node.containsKey("field")){
			sb.append("name=\""+node.getString("field")+"\"");
		}else if(node.containsKey("label")){
			String derivedFieldName = RegexUtils.removeAllNonAlphaNumeric(node.getString("label")).toLowerCase();
			sb.append("name=\""+derivedFieldName+"\"");
		}
		
		sb.append(">");
		
		//if form key is available, add form tags
		if(node.containsKey("form")){
			//do form stuff
			String formName = node.getString("form");
			sb.insert(0, "<form action=\""+formName+"\">");
			sb.append("</form>");
		}
		
		return sb.toString();
	};
	
	protected static Map<String, FormInputInterface> defaultInputTemplates() {
		Map<String, FormInputInterface> defaultTemplates = new HashMap<String, FormInputInterface>();
		defaultTemplates.put("title", FormInputTemplates.header);
		defaultTemplates.put("dropdown", FormInputTemplates.select);
		defaultTemplates.put("text", FormInputTemplates.input_text);
		
		return defaultTemplates;
	}
}
