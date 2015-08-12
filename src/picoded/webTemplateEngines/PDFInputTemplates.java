package picoded.webTemplateEngines;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import picoded.conv.RegexUtils;
import picoded.struct.CaseInsensitiveHashMap;

import com.mysql.jdbc.StringUtils;

public class PDFInputTemplates {
	
	protected static FormInputInterface defaultOutput_pdf = (node)->{
		return "";
	};
	
	protected static FormInputInterface div_pdf = (node)->{
		return "";
	};
	
	protected static FormInputInterface header_pdf = (node)->{ 
		String text = node.getString(JsonKeys.LABEL, "");
		
		StringBuilder sb = new StringBuilder();
		
		StringBuilder classBuilder = new StringBuilder(" class=\"pf_header");
		FormGenerator.getCustomClass(node, classBuilder, JsonKeys.CUSTOMCLASS, "pfl_");
		FormGenerator.getCustomClass(node, classBuilder, JsonKeys.INPUT_CLASS, "");
		FormGenerator.getCustomClass(node, classBuilder, JsonKeys.LABEL_CLASS, "");
		classBuilder.append("\"");
		String inputClassString = classBuilder.toString();
		String inputCssString = FormGenerator.getInputCssString(node);
		
		sb.append("<h3"+inputClassString+inputCssString+">"+text+"</h3>\n");
		
		return sb.toString();
	};
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface default_pdf = (node)->{
		StringBuilder sb = new StringBuilder();
		
		StringBuilder pdfOutputClassBuilder = new StringBuilder(" class=\"pf_dOutput");
		getPDFOutputClass(node, pdfOutputClassBuilder);
		pdfOutputClassBuilder.append("\"");
		
		String labelString = node.label();
		if(!labelString.isEmpty()){
			StringBuilder labelClassBuilder = new StringBuilder(" class=\"pf_label");
			FormGenerator.getCustomClass(node, labelClassBuilder, JsonKeys.CUSTOMCLASS, "pfl_");
			labelClassBuilder.append("\"");
			sb.append("<"+HtmlTag.DIV+labelClassBuilder.toString()+">"+labelString+"</"+HtmlTag.DIV+">");
		}
		
		String fieldName = node.field();
		if(!fieldName.isEmpty()){
			String fieldValue = (String)node.getDefaultValue(fieldName);
			String finalFieldValue = fieldValue;
			if(!StringUtils.isNullOrEmpty(fieldValue)){
				if(node.getString(JsonKeys.TYPE).equals(JsonKeys.DROPDOWN)){
					//map the fieldvalue to the options
					Object optionsRaw = node.get(JsonKeys.OPTIONS);
					if(optionsRaw instanceof Map){
						Map<String, Object> options = (Map<String, Object>)optionsRaw;
						for(String key:options.keySet()){
							if(key.equalsIgnoreCase(fieldValue)){
								finalFieldValue = (String)options.get(key);
							}
						}
					}
				}
				
				sb.append("<"+HtmlTag.DIV+pdfOutputClassBuilder.toString()+">"+finalFieldValue+"</"+HtmlTag.DIV+">");
			}
		}
		return sb.toString();
	};
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface dropdownWithOthers = (node)->{
		StringBuilder sb = new StringBuilder();
		
		StringBuilder pdfOutputClassBuilder = new StringBuilder(" class=\"pf_dOutput");
		getPDFOutputClass(node, pdfOutputClassBuilder);
		pdfOutputClassBuilder.append("\"");
		
		String labelString = node.label();
		if(!labelString.isEmpty()){
			StringBuilder labelClassBuilder = new StringBuilder(" class=\"pf_label");
			FormGenerator.getCustomClass(node, labelClassBuilder, JsonKeys.CUSTOMCLASS, "pfi_");
			FormGenerator.getCustomClass(node, labelClassBuilder, JsonKeys.INPUT_CLASS, "");
			labelClassBuilder.append("\"");
			sb.append("<"+HtmlTag.DIV+labelClassBuilder.toString()+">"+labelString+"</"+HtmlTag.DIV+">");
		}
		
		String fieldName = node.field();
		if(!fieldName.isEmpty()){
			String fieldValue = (String)node.getDefaultValue(fieldName);
			fieldValue = RegexUtils.removeAllNonAlphaNumeric(fieldValue).toLowerCase();
			
			if(node.containsKey(JsonKeys.OPTIONS)){
				Object dropDownObject = node.get(JsonKeys.OPTIONS);
				
				//if what is passed in is a Map, assume it is LinkedHashMap, to maintain insertion order
				if(dropDownObject instanceof HashMap<?, ?>){
					LinkedHashMap<String, String> dropDownListOptions = (LinkedHashMap<String, String>)dropDownObject;
					Set<String> keys = dropDownListOptions.keySet();
					if(keys.contains(fieldValue.toLowerCase())){
						sb.append("<"+HtmlTag.DIV+pdfOutputClassBuilder.toString()+">"+dropDownListOptions.get(fieldValue.toLowerCase())+"</"+HtmlTag.DIV+">");
					}else{
						sb.append("<"+HtmlTag.DIV+pdfOutputClassBuilder.toString()+">Others: "+fieldValue+"</"+HtmlTag.DIV+">");
					}
				}else if(dropDownObject instanceof List<?>){
					List<String> dropDownOptions = (List<String>)dropDownObject;
					boolean valueFound = false;
					for(String str:dropDownOptions){
						String key = RegexUtils.removeAllNonAlphaNumeric(str).toLowerCase();
						if(key.equals(fieldValue.toLowerCase())){
							sb.append("<"+HtmlTag.DIV+pdfOutputClassBuilder.toString()+">"+str+"</"+HtmlTag.DIV+">");
							valueFound = true;
							break;
						}
					}
					if(!valueFound){
						sb.append("<"+HtmlTag.DIV+pdfOutputClassBuilder.toString()+">Others: "+fieldValue+"</"+HtmlTag.DIV+">");
					}
				}
			}
		}
		return sb.toString();
	};
	
	protected static FormInputInterface rawHtml_pdf = (node)->{
		StringBuilder sb = new StringBuilder();
		sb.append(node.getString(JsonKeys.HTML_INJECTION));
		return sb.toString();
	};
	
	protected static void getPDFOutputClass(FormNode node, StringBuilder sb){
		if(node.containsKey(JsonKeys.PDFOUTPUT_CLASS)){
			String wrapperClass = node.getString(JsonKeys.PDFOUTPUT_CLASS);
			String[] wrapperClassSplit = null;
			if(wrapperClass.contains(" ")){
				wrapperClassSplit = wrapperClass.split(" ");
				for(String str:wrapperClassSplit){
					if(!str.equals(" ")){
						sb.append(" "+str);
					}
				}
			}else{
				sb.append(" "+wrapperClass);
			}
		}
	}
	
	public static Map<String, FormInputInterface> defaultPDFInputTemplates(){
		Map<String, FormInputInterface> defaultTemplates = new HashMap<String, FormInputInterface>();
		
		defaultTemplates.put("dropdown", default_pdf);
		defaultTemplates.put("text", default_pdf);
		defaultTemplates.put("div", div_pdf);
		defaultTemplates.put("title", header_pdf);
		defaultTemplates.put("rawHtml", rawHtml_pdf);
		defaultTemplates.put("dropdownWithOthers", dropdownWithOthers);
		
		return defaultTemplates;
	}
}
