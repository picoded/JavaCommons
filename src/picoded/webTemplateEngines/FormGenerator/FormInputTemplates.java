package picoded.webTemplateEngines.FormGenerator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.hazelcast.instance.Node;

import picoded.conv.ListValueConv;
import picoded.conv.GenericConvert;
import picoded.conv.RegexUtils;
import picoded.struct.CaseInsensitiveHashMap;

public class FormInputTemplates {
	
	public static StringBuilder displayDiv( FormNode node, String pfiClass ) {
		String text = node.getString(JsonKeys.TEXT, "");
		String fieldValue = node.getFieldValue();
		
		String textAndField = text+fieldValue;
		if(textAndField == null || textAndField.length() <= 0) {
			return new StringBuilder();
		}
		
		StringBuilder[] sbArr = node.defaultHtmlInput( HtmlTag.DIV, pfiClass, null );
		return sbArr[0].append(textAndField).append(sbArr[1]);
	}
	
	protected static FormInputInterface div = (node)->{
		return FormInputTemplates.displayDiv(node, "pfi_div pfi_input");
	};
	
	protected static FormInputInterface header = (node)->{ 
		String text = node.getString(JsonKeys.TEXT, "");
		String fieldValue = node.getFieldValue() != null ? node.getFieldValue():"";
		StringBuilder[] sbArr = node.defaultHtmlInput( HtmlTag.HEADER, "pfi_header pfi_input", null );
		return sbArr[0].append(text).append(fieldValue).append(sbArr[1]);
	};
	
	protected static FormInputInterface select = (node)->{ 
		StringBuilder[] sbArr = node.defaultHtmlInput( HtmlTag.SELECT, "pfi_select pfi_input", null );
		StringBuilder ret = sbArr[0];
		
		// Prepeare the option key value list
		List<String> keyList = new ArrayList<String>();
		List<String> nmeList = new ArrayList<String>();
//		String key = null; //the key item
//		String nme = null; //the display name
		
		// Generates the dropdown list, using either map or list
		//---------------------------------------------------------
		Object dropDownObject = node.get(JsonKeys.OPTIONS);
		nmeList = dropdownNameList(dropDownObject);
		keyList = dropdownKeyList(dropDownObject);
		
		// Use the generated list, to populate the option set
		//---------------------------------------------------------
		String selectedKey = node.getFieldValue(); 
		createDropdownHTMLString(ret, keyList, nmeList, selectedKey);
		
		ret.append(sbArr[1]);
		
		return ret;
	};
	
	protected static FormInputInterface input_text = (node)->{
		CaseInsensitiveHashMap<String,String> paramMap = new CaseInsensitiveHashMap<String, String>();
		String fieldValue = node.getFieldValue();
		
		paramMap.put(HtmlTag.TYPE, "text");
		if( fieldValue != null && fieldValue.length() >= 0 ) {
			paramMap.put(HtmlTag.VALUE, fieldValue);
		}
		
		StringBuilder[] sbArr = node.defaultHtmlInput( HtmlTag.INPUT, "pfi_inputText pfi_input", paramMap );
		return sbArr[0].append(sbArr[1]);
	};
	
	
	protected static FormInputInterface dropdownWithOthers = (node)->{
		Map<String, String> funcMap = new HashMap<String, String>();
		String funcName = node.getString(JsonKeys.FUNCTION_NAME, "OnChangeDefaultFuncName");
		funcMap.put("onchange", funcName+"()"); //get this value from map
		
		StringBuilder[] sbArr = node.defaultHtmlInput( HtmlTag.SELECT, "pf_select", funcMap );
		StringBuilder ret = new StringBuilder(getDropDownOthersJavascriptFunction(node) + sbArr[0].toString());
		
		// Prepeare the option key value list
		List<String> keyList = new ArrayList<String>();
		List<String> nmeList = new ArrayList<String>();
		
		// Generates the dropdown list, using either map or list
		//---------------------------------------------------------
		Object dropDownObject = node.get(JsonKeys.OPTIONS);
		nmeList = dropdownNameList(dropDownObject);
		keyList = dropdownKeyList(dropDownObject);
		
		// Use the generated list, to populate the option set
		//---------------------------------------------------------
		String selectedKey = node.getFieldValue(); 
		createDropdownHTMLString(ret, keyList, nmeList, selectedKey);
		
		ret.append(sbArr[1]);
		
		//append inputtexthere
		String inputTextFieldName = node.getString("textField", "dropdownfieldname");
		Map<String, String> inputParamMap = new HashMap<String, String>();
		inputParamMap.put("style", "display:none");
		inputParamMap.put("type", "text");
		inputParamMap.put("name", inputTextFieldName);
		StringBuilder[] inputTextArr = node.defaultHtmlInput( HtmlTag.INPUT, "pf_inputText", inputParamMap );
		ret.append(inputTextArr[0].toString() + inputTextArr[1].toString());
		
		return ret;
	};
	
	protected static FormInputInterface checkbox = (node)->{
		CaseInsensitiveHashMap<String,String> paramMap = new CaseInsensitiveHashMap<String, String>();
		paramMap.put(HtmlTag.TYPE, JsonKeys.CHECKBOX);
		String selection = "";
		if(node.containsKey("selected")){
			selection = RegexUtils.removeAllNonAlphaNumeric(node.getString("selected"));
		}
		
//		List<String> keyList = new ArrayList<String>();
//		List<String> nameList = new ArrayList<String>();
		Map<String, String> keyNamePair = new HashMap<String, String>();
		Object optionsObject = node.get(JsonKeys.OPTIONS);
		keyNamePair = optionsKeyNamePair(optionsObject);
//		nameList = dropdownNameList(dropDownObject);
//		keyList = dropdownKeyList(dropDownObject);
		
		StringBuilder ret = new StringBuilder();
		for(String key:keyNamePair.keySet()){
			CaseInsensitiveHashMap<String,String> tempMap = new CaseInsensitiveHashMap<String, String>(paramMap);
			tempMap.put("value", key);
			
			
			if(key.equalsIgnoreCase(selection)){
				tempMap.put("checked", "checked");
			}
			
			StringBuilder[] sbArr = node.defaultHtmlInput( HtmlTag.INPUT, "pfi_inputCheckbox pfi_input", tempMap );
			ret.append(sbArr[0]);
			ret.append(keyNamePair.get(key));
			ret.append(sbArr[1]);
		}
		return ret;
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
		defaultTemplates.put(JsonKeys.CHECKBOX, checkbox);
		
		return defaultTemplates;
	}
	
	//////////////////////
	//
	//	Helper Functions
	//
	//////////////////////
	
	@SuppressWarnings("unchecked")
	private static List<String> dropdownNameList(Object dropDownObject){
		List<String> nameList = null;
		if(dropDownObject instanceof List){
			nameList = ListValueConv.objectToString( (List<Object>)dropDownObject );
		}else if(dropDownObject instanceof Map){
			nameList = new ArrayList<String>();
			
			Map<Object,Object> dropDownMap = (Map<Object,Object>)dropDownObject;
			for(Object keyObj : dropDownMap.keySet()) {
				String name = GenericConvert.toString( dropDownMap.get(keyObj), null );
				
				// Skip blank values 
				if(name == null || name.length() <= 0) {
					continue;
				}
				
				//insert name
				nameList.add(name);
			}
		}
		
		return nameList;
	}
	
	@SuppressWarnings("unchecked")
	private static List<String> dropdownKeyList(Object dropDownObject){
		List<String> nameList = dropdownNameList(dropDownObject);
		List<String> keyList = new ArrayList<String>();
		
		if(dropDownObject instanceof List){
			for(int a=0; a<nameList.size(); ++a) {
				keyList.add( RegexUtils.removeAllNonAlphaNumeric( nameList.get(a) ).toLowerCase() );
			}
		}else if(dropDownObject instanceof Map){
			Map<Object,Object> dropDownMap = (Map<Object,Object>)dropDownObject;
			for(Object keyObj : dropDownMap.keySet()) {
				String key = RegexUtils.removeAllNonAlphaNumeric( GenericConvert.toString(keyObj, null) ).toLowerCase();
				
				// Skip blank keys 
				if(key == null || key.length() <= 0) {
					continue;
				}
	
				// Insert key
				keyList.add(key);
			}
		}
		
		return keyList;
	}
	
	@SuppressWarnings("unchecked")
	private static Map<String, String> optionsKeyNamePair(Object optionsObject){
		Map<String, String> ret = new HashMap<String, String>();
		
		if(optionsObject instanceof List){
			List<String> nameList = ListValueConv.objectToString( (List<Object>)optionsObject );
			for(String name:nameList){
				ret.put(RegexUtils.removeAllNonAlphaNumeric(name).toLowerCase(), name);
			}
		}else if(optionsObject instanceof Map){
			Map<String,Object> optionsMap = (Map<String,Object>)optionsObject;
			for(String key : optionsMap.keySet()){
				String sanitisedKey = RegexUtils.removeAllNonAlphaNumeric(key).toLowerCase();
				ret.put(sanitisedKey, (String)optionsMap.get(key));
			}
			
		}
		
		return ret;
	}
	
	private static void createDropdownHTMLString(StringBuilder sb, List<String> keyList, List<String> nameList, String selectedKey){
		for(int a=0; a<keyList.size(); ++a) {
			
			String key = keyList.get(a);
			String nme = nameList.get(a);
			
			sb.append("<"+HtmlTag.OPTION+" "+HtmlTag.VALUE+"=\""+key+"\"");
			
			// Value is selected
			if( selectedKey != null && selectedKey.equalsIgnoreCase( key ) ) {
				sb.append(" "+HtmlTag.SELECTED+"=\""+HtmlTag.SELECTED+"\"");
			}
			
			sb.append(">"+nme+"</"+HtmlTag.OPTION+">");
		}
	}
	
	protected static String getDropDownOthersJavascriptFunction(FormNode node){
		String dropDownField = node.getString(JsonKeys.FIELD);
		String inputField = node.getString(JsonKeys.DROPDOWN_WITHOTHERS_TEXTFIELD);
		String othersOptionToShowTextField = RegexUtils.removeAllNonAlphaNumeric(node.getString(JsonKeys.OTHERS_OPTION)).toLowerCase();
		String funcName = node.getString(JsonKeys.FUNCTION_NAME, "OnChangeDefaultFuncName");
		
		String injectedScript = "<script>"+
									"function "+funcName+"() {"+
										"var dropDown = document.getElementById(\""+dropDownField+"\");"+
										"var inputField = document.getElementById(\""+inputField+"\");"+
										"if(dropDown.value == \""+othersOptionToShowTextField+"\"){"+//replace Others with val
											"inputField.style.display = \"inline\";"+ //replace element by id
										"}else{"+
											"inputField.style.display = \"none\";"+ //replace element by id
										"}"+
									"};"+
								"</script>";
		
		return injectedScript;
	}
}
