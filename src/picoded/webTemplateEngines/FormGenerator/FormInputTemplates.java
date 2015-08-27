package picoded.webTemplateEngines.FormGenerator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.hazelcast.instance.Node;

import picoded.conv.ConvertJSON;
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
		return dropdownWithOthers(node, false);
	};
	
	protected static StringBuilder dropdownWithOthers(FormNode node, boolean displayMode){
		if(!displayMode){
			Map<String, String> funcMap = new HashMap<String, String>();
			String funcName = node.getString(JsonKeys.FUNCTION_NAME, "OnChangeDefaultFuncName");
			String nodeName = RegexUtils.removeAllNonAlphaNumeric_allowUnderscoreAndDash(node.getString(JsonKeys.FIELD)).toLowerCase();
			funcMap.put("onchange", funcName+"()"); //get this value from map
			funcMap.put("id", nodeName);
			
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
			inputParamMap.put("id", inputTextFieldName);
			StringBuilder[] inputTextArr = node.defaultHtmlInput( HtmlTag.INPUT, "pf_inputText", inputParamMap );
			ret.append(inputTextArr[0].toString() + inputTextArr[1].toString());
			
			return ret;
		}else{
			StringBuilder ret = new StringBuilder();
			Map<String, String> funcMap = new HashMap<String, String>();
			String nodeName = RegexUtils.removeAllNonAlphaNumeric_allowUnderscoreAndDash(node.getString(JsonKeys.FIELD)).toLowerCase();
			funcMap.put("id", nodeName);
			StringBuilder[] sbArr = node.defaultHtmlInput( HtmlTag.DIV, "pf_select", funcMap );
			
			ret.append(sbArr[0]);
			
			String val = node.getFieldValue();
			String valLowercased = RegexUtils.removeAllNonAlphaNumeric_allowUnderscoreAndDash(val).toLowerCase();
			
			Object dropDownObject = node.get(JsonKeys.OPTIONS);
			List<String> keyList = dropdownKeyList(dropDownObject);
			List<String> nameList = dropdownNameList(dropDownObject);
			
			if(!keyList.contains(valLowercased)){
				ret.append("Others: "+val);
			}else{
				ret.append(val);
			}
			
			ret.append(sbArr[1]);
			return ret;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface checkbox = (node)->{
		return createCheckbox(node, false, "");
	};
	
	@SuppressWarnings("unchecked")
	protected static StringBuilder createCheckbox(FormNode node, boolean displayMode, String pfiClass){
		
		CaseInsensitiveHashMap<String,String> paramMap = new CaseInsensitiveHashMap<String, String>();
		paramMap.put(HtmlTag.TYPE, JsonKeys.CHECKBOX);
		
		List<String> checkboxSelections = new ArrayList<String>();
		if(!node.getFieldName().isEmpty()){
			Object nodeDefaultVal = node.getDefaultValue(node.getFieldName());
			if(nodeDefaultVal != null){
				if(nodeDefaultVal instanceof String){
					if(((String)nodeDefaultVal).contains("[")){
						List<Object> nodeValMap = ConvertJSON.toList((String)nodeDefaultVal);
						for(Object obj : nodeValMap){
							checkboxSelections.add(RegexUtils.removeAllNonAlphaNumeric_allowUnderscoreAndDash((String)obj).toLowerCase());
						}
					}else{
						checkboxSelections.add(RegexUtils.removeAllNonAlphaNumeric_allowUnderscoreAndDash((String)nodeDefaultVal).toLowerCase());
					}
				}else if(nodeDefaultVal instanceof List){
					for(String str : (List<String>)nodeDefaultVal){
						checkboxSelections.add(RegexUtils.removeAllNonAlphaNumeric_allowUnderscoreAndDash(str).toLowerCase());
					}
				}
			}
		}
		
		Map<String, String> keyNamePair = new HashMap<String, String>();
		Object optionsObject = node.get(JsonKeys.OPTIONS);
		keyNamePair = optionsKeyNamePair(optionsObject);
		
		StringBuilder ret = new StringBuilder();
		for(String key:keyNamePair.keySet()){
			if(!displayMode){
				CaseInsensitiveHashMap<String,String> tempMap = new CaseInsensitiveHashMap<String, String>(paramMap);
				tempMap.put("value", key);
				
				for(String selection : checkboxSelections){
					if(key.equalsIgnoreCase(selection)){
						tempMap.put("checked", "checked");
					}
				}
				
				StringBuilder[] sbArr = node.defaultHtmlInput( HtmlTag.INPUT, "pfi_inputCheckbox pfi_input", tempMap );
				ret.append(sbArr[0]);
				ret.append(keyNamePair.get(key));
				ret.append(sbArr[1]);
			}else{
				StringBuilder[] sbArr = node.defaultHtmlInput( HtmlTag.DIV, "pfi_inputCheckbox pfi_input", null );
				
				boolean found = false;
				for(String selection : checkboxSelections){
					if(key.equalsIgnoreCase(selection)){
						sbArr[0].append("<div class=\"pf_displayCheckbox pf_displayCheckbox_selected\"></div>");
						found = true;
					}
				}
				
				if(!found){
					sbArr[0].append("<div class=\"pf_displayCheckbox pf_displayCheckbox_unselected\"></div>");
				}
				
				sbArr[0].append("<div class=\"pf_displayCheckbox_text\">"+keyNamePair.get(key)+"</div>");
				
				ret.append(sbArr[0]);
				ret.append(sbArr[1]);
			}
		}
		
		StringBuilder[] wrapper = node.defaultHtmlInput( HtmlTag.DIV, pfiClass, null );
		ret = wrapper[0].append(ret);
		ret.append(wrapper[1]);
		
		return ret;
	}
	
	protected static FormInputInterface table = (node)->{
		return tableWrapper(node, false);
	};

	@SuppressWarnings("unchecked")
	protected static StringBuilder tableWrapper(FormNode node, boolean displayMode){
		StringBuilder ret = new StringBuilder();
		
		//<table> tags
		StringBuilder[] wrapperArr = node.defaultHtmlWrapper( HtmlTag.TABLE, node.prefix_standard()+"div", null );
		
		//table header/label
		ret.append("<thead>");
		if(node.containsKey("tableHeader")){
			ret.append("<tr><th>"+node.getString("tableHeader")+"</th></tr>");
		}
		
		List<Object> tableHeaders = getTableHeaders(node);
		if(tableHeaders != null && tableHeaders.size() > 0){
			ret.append("<tr>");
			for(Object header:tableHeaders){
				ret.append("<th>"+(String)header+"</th>");
			}
			ret.append("</tr>");
		}
		
		ret.append("</thead>");
		ret.append("<tbody>");
		
		boolean removeLabelFromSecondIteration = false;
		boolean firstIteration = true;
		
		//data
		List<Map<String, String>> childData = getTableChildrenData(node);
		CaseInsensitiveHashMap<String, Object> nodeValues = node._inputValue;
		List<CaseInsensitiveHashMap<String, Object>> clientsValues = (List<CaseInsensitiveHashMap<String, Object>>)node._inputValue.get(node.getFieldName());
		
		
		for(Map<String, Object> childValues : clientsValues){
			ret.append("<tr>");
			for(Map<String, String> childMap : childData){
				String childNodeType = childMap.get(JsonKeys.TYPE);
				
				FormNode childNode = new FormNode();
				childNode._inputValue = new CaseInsensitiveHashMap<String, Object>(childValues);
				childNode.putAll(childMap);
				childNode._formGenerator = node._formGenerator;
				
				if( removeLabelFromSecondIteration && !firstIteration) {
					//remove labels
					//TODO maybe
				}
				
				FormWrapperInterface func = node._formGenerator.wrapperInterface(displayMode, childNodeType);
				
				StringBuilder sb = func.apply(childNode);
				
				ret.append("<td>");
				ret.append(sb);
				ret.append("</td>");
			}
			ret.append("</tr>");
			
			firstIteration = false;
		}
		
		ret.append("</tbody>");
		
		//final squashing
		ret = wrapperArr[0].append(ret);
		ret.append(wrapperArr[1]);
		return ret;
	}
	
	protected static FormInputInterface verticalTable = (node)->{
		return verticalTable(node, false);
	};
	
	@SuppressWarnings("unchecked")
	protected static StringBuilder verticalTable(FormNode node, boolean displayMode){
		StringBuilder ret = new StringBuilder();
		
		//<table> tags
		StringBuilder[] wrapperArr = node.defaultHtmlWrapper( HtmlTag.TABLE, node.prefix_standard()+"div", null );
		
		//table header/label
		ret.append("<thead>");
		if(node.containsKey("tableHeader")){
			ret.append("<tr><th>"+node.getString("tableHeader")+"</th></tr>");
		}
		ret.append("</thead>");
		ret.append("<tbody>");
		
		//
		List<Object> tableHeaders = getTableHeaders(node);
		
		//data
		List<Map<String, String>> childData = getTableChildrenData(node);
		CaseInsensitiveHashMap<String, Object> nodeValues = node._inputValue;
		List<CaseInsensitiveHashMap<String, Object>> clientsValues = (List<CaseInsensitiveHashMap<String, Object>>)node._inputValue.get(node.getFieldName());
		
		//for each header type, loop through child data values and pull out the corresponding data
		int childDataCounter = 0;
		for(Object tableHeaderRaw : tableHeaders){
			String tableHeader = (String)tableHeaderRaw;
			
			ret.append("<tr>");
			ret.append("<th>"+tableHeader+"</th>");
			
			Map<String, String> childMap = childData.get(childDataCounter); //childmap is in declare
			String type = childMap.get(JsonKeys.TYPE);
			String field = childMap.get("field");
			for(Map<String, Object> dataValues : clientsValues){ //data values is in define
				if(dataValues.containsKey(field)){
					FormNode childNode = new FormNode();
					childNode._inputValue = new CaseInsensitiveHashMap<String, Object>(dataValues);
					childNode.putAll(childMap);
					childNode._formGenerator = node._formGenerator;
					
					FormWrapperInterface func = node._formGenerator.wrapperInterface(displayMode, type);
					
					StringBuilder sb = func.apply(childNode);
					
					ret.append("<td>");
					ret.append(sb.toString());
					ret.append("</td>");
				}
			}
			++childDataCounter;
			
			ret.append("</tr>");
		}
		
		ret.append("</tbody>");
		
		//final squashing
		ret = wrapperArr[0].append(ret);
		ret.append(wrapperArr[1]);
		return ret;
	}
	
	protected static FormInputInterface image = (node)->{
		return image(node, false);
	};
	
	//image has no data passed in
	protected static StringBuilder image(FormNode node, boolean displayMode){
		StringBuilder ret = new StringBuilder();
		
		String imgPath = node.getString("relativePath");
		
		ret.append("<img src=\""+imgPath+"\"></img>");
		
		return ret;
	}
	
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
		defaultTemplates.put(JsonKeys.DROPDOWN_WITHOTHERS, FormInputTemplates.dropdownWithOthers);
		defaultTemplates.put(JsonKeys.CHECKBOX, FormInputTemplates.checkbox);
		defaultTemplates.put("table", FormInputTemplates.table);
		defaultTemplates.put("verticalTable", FormInputTemplates.verticalTable);
		defaultTemplates.put("image", FormInputTemplates.image);
		
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
				keyList.add( RegexUtils.removeAllNonAlphaNumeric_allowUnderscoreAndDash( nameList.get(a) ).toLowerCase() );
			}
		}else if(dropDownObject instanceof Map){
			Map<Object,Object> dropDownMap = (Map<Object,Object>)dropDownObject;
			for(Object keyObj : dropDownMap.keySet()) {
				String key = RegexUtils.removeAllNonAlphaNumeric_allowUnderscoreAndDash( GenericConvert.toString(keyObj, null) ).toLowerCase();
				
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
		Map<String, String> ret = new LinkedHashMap<String, String>();
		
		if(optionsObject instanceof List){
			List<String> nameList = ListValueConv.objectToString( (List<Object>)optionsObject );
			for(String name:nameList){
				ret.put(RegexUtils.removeAllNonAlphaNumeric_allowUnderscoreAndDash(name).toLowerCase(), name);
			}
		}else if(optionsObject instanceof Map){
			Map<String,Object> optionsMap = (Map<String,Object>)optionsObject;
			for(String key : optionsMap.keySet()){
				String sanitisedKey = RegexUtils.removeAllNonAlphaNumeric_allowUnderscoreAndDash(key).toLowerCase();
				ret.put(sanitisedKey, (String)optionsMap.get(key));
			}
			
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private static List<Map<String, String>> getTableChildrenData(FormNode node){
		List<Map<String, String>> ret = new ArrayList<Map<String, String>>();
		
		if(node.containsKey("children")){
			List<Object> childrenList = (List<Object>)node.get("children");
			for(Object obj : childrenList){
				if(obj instanceof Map){
					ret.add((Map<String, String>)obj);
				}
			}
		}else{
			return null;
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
	
	@SuppressWarnings("unchecked")
	private static List<String> getTableFields(List<Object> children){
		List<String> ret = new ArrayList<String>();
		for(Object childRaw : children){
			if(childRaw instanceof Map){
				Map<String, Object> childMap = (Map<String, Object>)childRaw;
				if(childMap.containsKey("field")){
					ret.add(RegexUtils.removeAllNonAlphaNumeric_allowUnderscoreAndDash((String)childMap.get("field")).toLowerCase());
				}
			}
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private static List<Object> getTableHeaders(FormNode node){
		if( node.containsKey("headers")) {
			Object tableHeadersRaw = node.get("headers");
			
			if( !(tableHeadersRaw instanceof List) ) {
				throw new IllegalArgumentException("'tableHeader' parameter found in defination was not a List: "+tableHeadersRaw);
			}
			
			return (List<Object>)tableHeadersRaw;
		}else{
			return null;
		}
	}
	
	protected static String getDropDownOthersJavascriptFunction(FormNode node){
		String dropDownField = RegexUtils.removeAllNonAlphaNumeric_allowUnderscoreAndDash(node.getString(JsonKeys.FIELD)).toLowerCase();
		String inputField = node.getString(JsonKeys.DROPDOWN_WITHOTHERS_TEXTFIELD);
		String othersOptionToShowTextField = RegexUtils.removeAllNonAlphaNumeric_allowUnderscoreAndDash(node.getString(JsonKeys.OTHERS_OPTION)).toLowerCase();
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
