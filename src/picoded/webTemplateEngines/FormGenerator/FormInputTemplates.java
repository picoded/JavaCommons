package picoded.webTemplateEngines.FormGenerator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import picoded.conv.ListValueConv;
import picoded.conv.GenericConvert;
import picoded.conv.RegexUtils;
import picoded.struct.CaseInsensitiveHashMap;

public class FormInputTemplates {
	
	protected static FormInputInterface div = (node)->{
		String text = node.getString(JsonKeys.TEXT, "");
		String fieldValue = node.getFieldValue();
		
		String textAndField = text+fieldValue;
		if(textAndField == null || textAndField.length() <= 0) {
			return new StringBuilder();
		}
		
		StringBuilder[] sbArr = node.defaultHtmlInput( HtmlTag.DIV, "pf_div", null );
		return sbArr[0].append(textAndField).append(sbArr[1]);
	};
	
	protected static FormInputInterface header = (node)->{ 
		String text = node.getString(JsonKeys.TEXT, "");
		String fieldValue = node.getFieldValue() != null ? node.getFieldValue():"";
		StringBuilder[] sbArr = node.defaultHtmlInput( HtmlTag.HEADER, "pf_header", null );
		return sbArr[0].append(text).append(fieldValue).append(sbArr[1]);
	};
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface select = (node)->{ 
		StringBuilder[] sbArr = node.defaultHtmlInput( HtmlTag.SELECT, "pf_select", null );
		StringBuilder ret = sbArr[0];
		
		// Prepeare the option key value list
		List<String> keyList = null;
		List<String> nmeList = null;
		String key = null; //the key item
		String nme = null; //the display name
		
		// Generates the dropdown list, using either map or list
		//---------------------------------------------------------
		Object dropDownObject = node.get(JsonKeys.OPTIONS);
		if(dropDownObject instanceof List){
			nmeList = ListValueConv.objectToString( (List<Object>)dropDownObject );
			keyList = new ArrayList<String>();
			
			for(int a=0; a<nmeList.size(); ++a) {
				keyList.add( RegexUtils.removeAllNonAlphaNumeric( nmeList.get(a) ).toLowerCase() );
			}
		} else if(dropDownObject instanceof Map) {
			nmeList = new ArrayList<String>();
			keyList = new ArrayList<String>();
			
			Map<Object,Object> dropDownMap = (Map<Object,Object>)dropDownObject;
			for(Object keyObj : dropDownMap.keySet()) {
				key = RegexUtils.removeAllNonAlphaNumeric( GenericConvert.toString(keyObj, null) ).toLowerCase();
				
				// Skip blank keys 
				if(key == null || key.length() <= 0) {
					continue;
				}
				
				nme = GenericConvert.toString( dropDownMap.get(keyObj), null );
				
				// Skip blank values 
				if(nme == null || nme.length() <= 0) {
					continue;
				}
				
				// Insert key value pair
				keyList.add(key);
				nmeList.add(nme);
			}
		}
		
		// Use the generated list, to populate the option set
		//---------------------------------------------------------
		String selectedKey = node.getFieldValue(); 
		
		for(int a=0; a<keyList.size(); ++a) {
			
			key = keyList.get(a);
			nme = nmeList.get(a);
			
			ret.append("<"+HtmlTag.OPTION+" "+HtmlTag.VALUE+"=\""+key+"\"");
			
			// Value is selected
			if( selectedKey != null && selectedKey.equalsIgnoreCase( key ) ) {
				ret.append(" "+HtmlTag.SELECTED+"=\""+HtmlTag.SELECTED+"\"");
			}
			
			ret.append(">"+nme+"</"+HtmlTag.OPTION+">");
		}
		
		ret.append(sbArr[1]);
		
		return ret;
	};
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface input_text = (node)->{
		CaseInsensitiveHashMap<String,String> paramMap = new CaseInsensitiveHashMap<String, String>();
		String fieldValue = node.getFieldValue();
		
		paramMap.put(HtmlTag.TYPE, "text");
		if( fieldValue != null && fieldValue.length() >= 0 ) {
			paramMap.put(HtmlTag.VALUE, fieldValue);
		}
		
		StringBuilder[] sbArr = node.defaultHtmlInput( HtmlTag.INPUT, "pf_inputText", paramMap );
		return sbArr[0].append(sbArr[1]);
	};
	
	protected static FormInputInterface raw_html = (node)->{
		StringBuilder sb = new StringBuilder();
		sb.append(node.getString(JsonKeys.HTML_INJECTION));
		return sb;
	};
	
	protected static Map<String, FormInputInterface> defaultInputTemplates() {
		Map<String, FormInputInterface> defaultTemplates = new CaseInsensitiveHashMap<String, FormInputInterface>();
		
		// Wildcard fallback
		defaultTemplates.put("*", FormInputTemplates.div);
		
		// Standard divs
		defaultTemplates.put(JsonKeys.DIV, FormInputTemplates.div);
		defaultTemplates.put(JsonKeys.TITLE, FormInputTemplates.header);
		defaultTemplates.put(JsonKeys.DROPDOWN, FormInputTemplates.select);
		defaultTemplates.put(JsonKeys.TEXT, FormInputTemplates.input_text);
		defaultTemplates.put(JsonKeys.HTML_INJECTION, FormInputTemplates.raw_html);
		defaultTemplates.put(JsonKeys.DROPDOWN_WITHOTHERS, dropdownWithOthers);
		
		return defaultTemplates;
	}
	
	////////////////////////////////////////////////
	//
	//  TO-REFACTOR
	//
	////////////////////////////////////////////////
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface dropdownWithOthers = (node)->{
		StringBuilder sb = new StringBuilder();
		
		if(node.containsKey(JsonKeys.HTML_INJECTION)){
			sb.append(node.getString(JsonKeys.HTML_INJECTION));
			return sb;
		}else{
			//jscript here
			sb.append("<script>");
			sb.append(getDropDownOthersJavascriptFunction(node));
			sb.append("</script>");
			
			
			String labelValue = node.label();
			String fieldValue = node.getFieldName();
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
				String fieldHtmlString = " "+"name"+"=\""+fieldValue+"\"";
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
				sb.append(""+"name"+"=\""+inputTextFieldValue+"\">");
			}else{
				sb.append("></"+HtmlTag.INPUT+">\n");
			}
			
			
			return sb;
		}
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
	
}
