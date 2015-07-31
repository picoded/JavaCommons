package picoded.webTemplateEngines;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import picoded.conv.RegexUtils;

import com.hazelcast.instance.Node;
import com.mysql.jdbc.StringUtils;

public class FormInputTemplates {
	
	//TODO
	/*
	 * radio check box
	 * 
	 */
	protected static FormInputInterface div = (node)->{
		return "";
	};
	
	protected static FormInputInterface header = (node)->{ 
		String text = node.getString("text", "");
		
		StringBuilder sb = new StringBuilder();
		
		String inputClassString = FormGenerator.getInputClassString(node);
		String inputCssString = FormGenerator.getInputCssString(node);
		
		sb.append("<h3"+inputClassString+inputCssString+">"+text+"</h3>\n");
		
		return sb.toString(); 
	};
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface select = (node)->{
		StringBuilder sb = new StringBuilder();
		
		if(node.containsKey("label")){
			String labelString = node.getString("label");
			String id = node.getString("id", RegexUtils.removeAllNonAlphaNumeric(labelString).toLowerCase());
			sb.append("<label for=\""+id+"\">"+labelString+"</label>\n");
		}
		
		String inputClassString = FormGenerator.getInputClassString(node);
		
		String fieldName = "";
		String selectedOption = "";
		if(node.containsKey("id")){
			String idKey = node.getString("id");
			fieldName = " id=\""+idKey+"\"";
			selectedOption = (String)node.getDefaultValue(idKey);
			if(selectedOption != null){
				selectedOption = RegexUtils.removeAllNonAlphaNumeric(selectedOption).toLowerCase();
			}
		}else if(node.containsKey("label")){
			fieldName = " id=\""+RegexUtils.removeAllNonAlphaNumeric(node.getString("label")).toLowerCase()+"\"";
		}
		
		sb.append("<select"+inputClassString+fieldName+">\n");
		
		if(node.containsKey("options")){
			Object dropDownObject = node.get("options");
			
			//if what is passed in is a Map, assume it is LinkedHashMap, to maintain insertion order
			if(dropDownObject instanceof HashMap<?, ?>){
				LinkedHashMap<String, String> dropDownListOptions = (LinkedHashMap<String, String>)node.get("options");
				for(String key:dropDownListOptions.keySet()){
					sb.append("<option value=\""+key+"\"");
					if(key.equals(selectedOption)){
						sb.append(" selected=\"selected\"");
					}
					sb.append(">"+dropDownListOptions.get(key)+"</option>\n");
				}
			} else if(dropDownObject instanceof List<?>){
				List<String> dropDownOptions = (List<String>)dropDownObject;
				for(String str:dropDownOptions){
					String key = RegexUtils.removeAllNonAlphaNumeric(str).toLowerCase();
					sb.append("<option value=\""+key+"\"");
					if(key.equals(selectedOption)){
						sb.append(" selected=\"selected\"");
					}
					sb.append(">"+str+"</option>\n");
				}
			}
		}
		
		sb.append("</select>\n");
		
		return sb.toString();
	};
	
	protected static FormInputInterface input_text = (node)->{
		StringBuilder sb = new StringBuilder();
		
		if(node.containsKey("label")){
			String labelString = node.getString("label");
			String id = node.getString("id", RegexUtils.removeAllNonAlphaNumeric(labelString).toLowerCase());
			sb.append("<label for=\""+id+"\">"+labelString+"</label>\n");
		}
		
		String inputClassString = FormGenerator.getInputClassString(node);
		 
		sb.append("<input"+inputClassString+" type=\"text\" ");
		
		//id/field and value elements
		if(node.containsKey("id")){
			String idKey = node.getString("id");
			sb.append("id=\""+idKey+"\"");
			String idValue = (String)node.getDefaultValue(idKey);
			if(!StringUtils.isNullOrEmpty(idValue)){
				sb.append(" value=\""+idValue+"\"></input>\n");
			}else{
				sb.append("></input>\n");
			}
		}else if(node.containsKey("label")){
			String derivedFieldName = RegexUtils.removeAllNonAlphaNumeric(node.getString("label")).toLowerCase();
			sb.append("id=\""+derivedFieldName+"\"");
		}else{
			sb.append("></input>\n");
		}
		
		//if form key is available, add form tags
		//uncomment if needed
//		if(node.containsKey("form")){
//			String formName = node.getString("form");
//			sb.insert(0, "<form action=\""+formName+"\">");
//			sb.append("</form>");
//		}
		
		return sb.toString();
	};
	
	protected static Map<String, FormInputInterface> defaultInputTemplates() {
		Map<String, FormInputInterface> defaultTemplates = new HashMap<String, FormInputInterface>();
		defaultTemplates.put("title", FormInputTemplates.header);
		defaultTemplates.put("dropdown", FormInputTemplates.select);
		defaultTemplates.put("text", FormInputTemplates.input_text);
		defaultTemplates.put("div", FormInputTemplates.div);
		
		return defaultTemplates;
	}
}
