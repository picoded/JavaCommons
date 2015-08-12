package picoded.webTemplateEngines;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import picoded.conv.RegexUtils;

import com.amazonaws.services.datapipeline.model.Field;
import com.hazelcast.instance.Node;
import com.mysql.jdbc.StringUtils;

public class FormInputTemplates {
	
	protected static FormInputInterface div = (node)->{
		return node.collapseStringBuilderArray( node.defaultHtmlInput( JsonKeys.DIV, "pf_div", null ) ).toString();
	};
	
	protected static FormInputInterface header = (node)->{ 
		String text = node.getString(JsonKeys.TEXT, "");
		StringBuilder[] sbArr = node.defaultHtmlInput( JsonKeys.DOM_HEADER, "pf_header", null );
		
		return sbArr[0].toString() + text + sbArr[1].toString();
		
		
		// 
		// StringBuilder sb = new StringBuilder("");
		// 
		// if(node.containsKey(JsonKeys.HTML_INJECTION)){
		// 	sb.append(node.getString(JsonKeys.HTML_INJECTION));
		// 	return sb.toString();
		// }else{
		// 	String fieldValue = node.field();
		// 	
		// 	
		// 	String text = node.getString(JsonKeys.TEXT, "");
		// 	
		// 	StringBuilder classBuilder = new StringBuilder(" class=\"pf_header");
		// 	FormGenerator.getCustomClass(node, classBuilder, JsonKeys.CUSTOMCLASS, "pfl_");
		// 	FormGenerator.getCustomClass(node, classBuilder, JsonKeys.LABEL_CLASS, "");
		// 	FormGenerator.getCustomClass(node, classBuilder, JsonKeys.INPUT_CLASS, "");
		// 	classBuilder.append("\"");
		// 	
		// 	String inputCssString = FormGenerator.getInputCssString(node);
		// 	
		// 	sb.append("<h3"+classBuilder.toString() + inputCssString+">"+text+"</h3>\n");
		// 	
		// 	return sb.toString();
		// }
	};
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface select = (node)->{
		StringBuilder sb = new StringBuilder();
		
		if(node.containsKey(JsonKeys.HTML_INJECTION)){
			sb.append(node.getString(JsonKeys.HTML_INJECTION));
			return sb.toString();
		}else{
			String labelValue = node.label();
			String fieldValue = node.field();
			if(!labelValue.isEmpty()){
				StringBuilder labelClassBuilder = new StringBuilder(" class=\"pf_label");
				FormGenerator.getCustomClass(node, labelClassBuilder, JsonKeys.CUSTOMCLASS, "pfl_");
				FormGenerator.getCustomClass(node, labelClassBuilder, JsonKeys.LABEL_CLASS, "");
				labelClassBuilder.append("\"");
				
				sb.append("<"+HtmlTag.LABEL+labelClassBuilder.toString()+" for=\""+fieldValue+"\">"+labelValue+"</"+HtmlTag.LABEL+">\n");
			}
			
			StringBuilder classStringBuilder = new StringBuilder(" class=\"pf_select");
			FormGenerator.getCustomClass(node, classStringBuilder, JsonKeys.CUSTOMCLASS, "pfi_");
			FormGenerator.getCustomClass(node, classStringBuilder, JsonKeys.INPUT_CLASS, "");
			classStringBuilder.append("\"");
			String inputClassString = classStringBuilder.toString();
			
			String selectedOption = "";
			if(!fieldValue.isEmpty()){
				String fieldHtmlString = " "+HtmlTag.ID+"=\""+fieldValue+"\"";
				sb.append("<"+HtmlTag.SELECT+""+inputClassString+fieldHtmlString+">\n");
				selectedOption = (String)node.getDefaultValue(fieldValue);
				if(selectedOption != null){
					selectedOption = RegexUtils.removeAllNonAlphaNumeric(selectedOption).toLowerCase();
				}
			}
			
			if(node.containsKey(JsonKeys.OPTIONS)){
				Object dropDownObject = node.get(JsonKeys.OPTIONS);
				
				//if what is passed in is a Map, assume it is LinkedHashMap, to maintain insertion order
				if(dropDownObject instanceof HashMap<?, ?>){
					LinkedHashMap<String, String> dropDownListOptions = (LinkedHashMap<String, String>)dropDownObject;
					for(String key:dropDownListOptions.keySet()){
						sb.append("<"+HtmlTag.OPTION+" "+HtmlTag.VALUE+"=\""+key+"\"");
						if(key.equalsIgnoreCase(selectedOption)){
							sb.append(" "+HtmlTag.SELECTED+"=\"selected\"");
						}
						sb.append(">"+dropDownListOptions.get(key)+"</"+HtmlTag.OPTION+">\n");
					}
				} else if(dropDownObject instanceof List<?>){
					List<String> dropDownOptions = (List<String>)dropDownObject;
					for(String str:dropDownOptions){
						String key = RegexUtils.removeAllNonAlphaNumeric(str).toLowerCase();
						sb.append("<"+HtmlTag.OPTION+" "+HtmlTag.VALUE+"=\""+key+"\"");
						if(key.equalsIgnoreCase(selectedOption)){
							sb.append(" "+HtmlTag.SELECTED+"=\"selected\"");
						}
						sb.append(">"+str+"</"+HtmlTag.OPTION+">\n");
					}
				}
			}
			
			sb.append("</"+HtmlTag.SELECT+">\n");
			
			return sb.toString();
		}
	};
	
	protected static FormInputInterface input_text = (node)->{
		StringBuilder sb = new StringBuilder();
		
		if(node.containsKey(JsonKeys.HTML_INJECTION)){
			sb.append(node.getString(JsonKeys.HTML_INJECTION));
			return sb.toString();
		}else{
		
			String labelValue = node.label();
			String fieldValue = node.field();
			if(!labelValue.isEmpty()){
				StringBuilder labelClassBuilder = new StringBuilder(" class=\"pf_label");
				FormGenerator.getCustomClass(node, labelClassBuilder, JsonKeys.CUSTOMCLASS, "pfl_");
				FormGenerator.getCustomClass(node, labelClassBuilder, JsonKeys.LABEL_CLASS, "");
				labelClassBuilder.append("\"");
				
				sb.append("<"+HtmlTag.LABEL+" "+labelClassBuilder.toString()+" for=\""+fieldValue+"\">"+labelValue+"</"+HtmlTag.LABEL+">\n");
			}
			
			StringBuilder classStringBuilder = new StringBuilder(" class=\"pf_inputText");
			FormGenerator.getCustomClass(node, classStringBuilder, JsonKeys.CUSTOMCLASS, "pfi_");
			FormGenerator.getCustomClass(node, classStringBuilder, JsonKeys.LABEL_CLASS, "");
			classStringBuilder.append("\"");
			String inputClassString = classStringBuilder.toString();
			
			sb.append("<"+HtmlTag.INPUT+""+inputClassString+" "+HtmlTag.TYPE+"=\"text\" ");
			
			//id/field and value elements
			if(!fieldValue.isEmpty()){
				sb.append(""+HtmlTag.ID+"=\""+fieldValue+"\"");
				String fieldDefaultValue = (String)node.getDefaultValue(fieldValue);
				if(!StringUtils.isNullOrEmpty(fieldDefaultValue)){
					sb.append(" "+HtmlTag.VALUE+"=\""+fieldDefaultValue+"\"></"+HtmlTag.INPUT+">\n");
				}else{
					sb.append("></"+HtmlTag.INPUT+">\n");
				}
			}else{
				sb.append("></"+HtmlTag.INPUT+">\n");
			}
			
			return sb.toString();
		}
	};
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface dropdownWithOthers = (node)->{
		StringBuilder sb = new StringBuilder();
		
		if(node.containsKey(JsonKeys.HTML_INJECTION)){
			sb.append(node.getString(JsonKeys.HTML_INJECTION));
			return sb.toString();
		}else{
			//jscript here
			sb.append("<script>");
			sb.append(getDropDownOthersJavascriptFunction(node));
			sb.append("</script>");
			
			
			String labelValue = node.label();
			String fieldValue = node.field();
			if(!labelValue.isEmpty()){
				StringBuilder labelClassBuilder = new StringBuilder(" class=\"pf_label");
				FormGenerator.getCustomClass(node, labelClassBuilder, JsonKeys.CUSTOMCLASS, "pfl_");
				FormGenerator.getCustomClass(node, labelClassBuilder, JsonKeys.LABEL_CLASS, "");
				labelClassBuilder.append("\"");
				
				sb.append("<"+HtmlTag.LABEL+labelClassBuilder.toString()+" for=\""+fieldValue+"\">"+labelValue+"</"+HtmlTag.LABEL+">\n");
			}
			
			StringBuilder classStringBuilder = new StringBuilder(" class=\"pf_select\"");
			FormGenerator.getCustomClass(node, classStringBuilder, JsonKeys.CUSTOMCLASS, "pfi_");
			FormGenerator.getCustomClass(node, classStringBuilder, JsonKeys.LABEL_CLASS, "");
			String funcName = node.getString("functionName");
			classStringBuilder.append(" onchange=\""+funcName+"()\"");
			
			String inputClassString = classStringBuilder.toString();
			
			String selectedOption = "";
			if(!fieldValue.isEmpty()){
				String fieldHtmlString = " "+HtmlTag.ID+"=\""+fieldValue+"\"";
				sb.append("<"+HtmlTag.SELECT+""+inputClassString+fieldHtmlString+">\n");
				selectedOption = (String)node.getDefaultValue(fieldValue);
				if(selectedOption != null){
					selectedOption = RegexUtils.removeAllNonAlphaNumeric(selectedOption).toLowerCase();
				}
			}
			
			if(node.containsKey(JsonKeys.OPTIONS)){
				Object dropDownObject = node.get(JsonKeys.OPTIONS);
				
				//if what is passed in is a Map, assume it is LinkedHashMap, to maintain insertion order
				if(dropDownObject instanceof HashMap<?, ?>){
					LinkedHashMap<String, String> dropDownListOptions = (LinkedHashMap<String, String>)dropDownObject;
					for(String key:dropDownListOptions.keySet()){
						sb.append("<"+HtmlTag.OPTION+" "+HtmlTag.VALUE+"=\""+key+"\"");
						if(key.equals(selectedOption)){
							sb.append(" "+HtmlTag.SELECTED+"=\"selected\"");
						}
						sb.append(">"+dropDownListOptions.get(key)+"</"+HtmlTag.OPTION+">\n");
					}
				} else if(dropDownObject instanceof List<?>){
					List<String> dropDownOptions = (List<String>)dropDownObject;
					for(String str:dropDownOptions){
						String key = RegexUtils.removeAllNonAlphaNumeric(str).toLowerCase();
						sb.append("<"+HtmlTag.OPTION+" "+HtmlTag.VALUE+"=\""+key+"\"");
						if(key.equals(selectedOption)){
							sb.append(" "+HtmlTag.SELECTED+"=\"selected\"");
						}
						sb.append(">"+str+"</"+HtmlTag.OPTION+">\n");
					}
				}
			}
			
			sb.append("</"+HtmlTag.SELECT+">\n");
			
			//append input text field
			StringBuilder inputBuilder = new StringBuilder(" class=\"pf_inputText\"");
			FormGenerator.getCustomClass(node, inputBuilder, JsonKeys.CUSTOMCLASS, "pfi_");
			inputBuilder.append(" style=\"display:none\"");
			String inputBuilderString = inputBuilder.toString();
			
			sb.append("<"+HtmlTag.INPUT+""+inputBuilderString+" "+HtmlTag.TYPE+"=\"text\" ");
			
			//id/field and value elements
			String inputTextFieldValue = node.getString("textField");
			if(!inputTextFieldValue.isEmpty()){
				sb.append(""+HtmlTag.ID+"=\""+inputTextFieldValue+"\"");
			}else{
				sb.append("></"+HtmlTag.INPUT+">\n");
			}
			
			
			return sb.toString();
		}
	};
	
	protected static FormInputInterface raw_html = (node)->{
		StringBuilder sb = new StringBuilder();
		sb.append(node.getString(JsonKeys.HTML_INJECTION));
		
		return sb.toString();
	};
	
	protected static String getDropDownOthersJavascriptFunction(FormNode node){
		String dropDownField = node.getString(JsonKeys.FIELD);
		String inputField = node.getString(JsonKeys.DROPDOWN_WITHOTHERS_TEXTFIELD);
		String othersOptionToShowTextField = RegexUtils.removeAllNonAlphaNumeric(node.getString(JsonKeys.OTHERS_OPTION)).toLowerCase();
		String funcName = node.getString(JsonKeys.FUNCTION_NAME);
		
		String injectedScript = "function "+funcName+"() {"+
									"var dropDown = document.getElementById(\""+dropDownField+"\");"+
									"var inputField = document.getElementById(\""+inputField+"\");"+
									"if(dropDown.value == \""+othersOptionToShowTextField+"\"){"+//replace Others with val
										"inputField.style.display = \"inline\";"+ //replace element by id
									"}else{"+
										"inputField.style.display = \"none\";"+ //replace element by id
									"}"+
								"};";
		
		return injectedScript;
	}
	
	protected static Map<String, FormInputInterface> defaultInputTemplates() {
		Map<String, FormInputInterface> defaultTemplates = new HashMap<String, FormInputInterface>();
		defaultTemplates.put(JsonKeys.TITLE, FormInputTemplates.header);
		defaultTemplates.put(JsonKeys.DROPDOWN, FormInputTemplates.select);
		defaultTemplates.put(JsonKeys.TEXT, FormInputTemplates.input_text);
		defaultTemplates.put(JsonKeys.DIV, FormInputTemplates.div);
		defaultTemplates.put(JsonKeys.HTML_INJECTION, FormInputTemplates.raw_html);
		defaultTemplates.put(JsonKeys.DROPDOWN_WITHOTHERS, dropdownWithOthers);
		
		return defaultTemplates;
	}
}
