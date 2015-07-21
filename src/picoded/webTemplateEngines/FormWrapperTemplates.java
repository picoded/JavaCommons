package picoded.webTemplateEngines;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormWrapperTemplates {
	
	public static String inputClassPrefix = "pf_";
	public static String inputCssPrefix = "pf_";
	public static String inputClassKey = "inputClass";
	public static String inputCssKey = "inputCss";
	
	public static String inputLabelKey = "inputLabel";
//	public static String inputParamsKey = "inputParams";
	
	public static String inputAttributeMapKey = "inputAttributeMap";
	
	private static List<String> standardParameters = Arrays.asList(FormGenerator.htmlTypeKey, FormGenerator.htmlTextKey, inputClassKey, inputCssKey, inputLabelKey, "option", "inputType");
	
	//----------------------------new structure----------------------------------
	protected static FormWrapperInterface divWrapper = (node)->{
		String[] prefixSuffix = new String[2];
		
		//generating prefix
//		String classString = FormGenerator.generateInputClass(node);
		StringBuilder prefix = new StringBuilder();
		prefix.append("<div>"); //do the class later
//		prefix.append("<div "+classString+">");		
		
		if(node.containsKey("label")){
			String labelName = node.getString("label");
			prefix.append("<div>"+labelName+"</div>");
		}
		
		//generating suffix
		StringBuilder suffix = new StringBuilder();
		suffix.append("</div>");
		
		prefixSuffix[0] = prefix.toString();
		prefixSuffix[1] = suffix.toString();
		
		return prefixSuffix;
	};
	
	protected static FormWrapperInterface noneWrapper = (node)->{
		String[] prefixSuffix = new String[]{"", ""};
		
		return prefixSuffix;
	};
	
	
	
	
	
	
	
	
	
	//----------------------------old structure------------------------------------
	
//	protected static FormWrapperInterface titleWrapper = (node)->{
//		String[] titleWrappers = new String[2]; //0 is opening tag, 1 is ending tag
//		
//		String tagName = FormGenerator.htmlTitleTagName;
//		int headerNumber = node.getInt("number");
//		tagName += headerNumber;
//		
//		String inputClassName = generateInputClass(node);
//		String inputCss = generateInputCSS(node);
//		titleWrappers = generateTags(tagName, inputClassName, inputCss, "");
//
//		return titleWrappers;
//	};
	
//	protected static FormWrapperInterface divWrapper = (node)->{
//		String[] divWrapper = new String[2]; //0 is opening tag, 1 is ending tag
//		
//		String tagName = FormGenerator.htmlDivTagName;
//		String inputClassName = generateInputClass(node);
//		String inputCss = generateInputCSS(node);
//		
//		divWrapper = generateTags(tagName, inputClassName, inputCss, "");
//		return divWrapper;
//	};
	
//	protected static FormWrapperInterface selectWrapper = (node)->{
//		String[] selectWrapper = new String[2];
//		
//		String tagName = FormGenerator.htmlDropDownTagName;
//		String inputClassName = generateInputClass(node);
//		String inputCss = generateInputCSS(node);
//		String attributes = generateTagAttributes(node);
//		
//		selectWrapper = generateTags(tagName, inputClassName, inputCss, attributes);
//		return selectWrapper;
//	};
	
	//special case - needs to handle all in 1 function - nasty
	protected static FormWrapperInterface inputWrapper = (node)->{
		String[] inputWrapper = new String[2];
		
		String tagName = FormGenerator.htmlInputFieldTagName;
		String inputLabel = node.getString(inputLabelKey);
		String inputClassName = generateInputClass(node);
		String inputCss = generateInputCSS(node);
		String attributes = generateTagAttributes(node);
		
		String inputType = "";
		if(node.containsKey("inputType")){
			inputType = "type="+node.get("inputType");
		}
		
		inputWrapper[0] = inputLabel;
		
		StringBuilder sb = new StringBuilder();
		sb.append("<"+tagName+" ");
		sb.append(inputClassName+" ");
		sb.append(inputCss);
		sb.append(attributes);
		sb.append(inputType);//this should be moved to attributes!!
		sb.append(">");
		
		if(node.containsKey("lineBreak")){
			if(node.getBoolean("lineBreak")){
				sb.append("<br>");
			}
		}
		
		inputWrapper[1] = sb.toString();
		inputWrapper[1] = inputWrapper[1].replace("  ", " ");
		inputWrapper[1] = inputWrapper[1].replace(" >", ">");
		
		return inputWrapper;
	};
	
	private static String generateTagAttributes(FormNode node){
		return generateTagAttributes(node, standardParameters);
	}
	
	@SuppressWarnings("unchecked")
	private static String generateTagAttributes(FormNode node, List<String> blockedParameters){
		StringBuilder sb = new StringBuilder();
		
		if(node.containsKey(inputAttributeMapKey)){
			Map<String, String> attributes = (Map<String, String>)node.get(inputAttributeMapKey);
			for(String key:attributes.keySet()){
				sb.append(key+"="+attributes.get(key)+" ");
			}
		} else {
			for(String key : node.keySet()){
				if(!blockedParameters.contains(key)){
					sb.append(key+"="+node.getString(key)+" ");
				}
			}
		}
		
		return sb.toString();
	}
	
	private static String generateInputClass(FormNode node){
		StringBuilder sb = new StringBuilder("class=\"");
		if(node.containsKey(inputClassKey)){
			sb.append(node.getString(inputClassKey));
		} else {
			sb.append(inputClassPrefix+node.getString("type")+"Class");
		}
		sb.append("\"");
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	private static String generateInputCSS(FormNode node){
		StringBuilder cssString = new StringBuilder();
		if(node.containsKey(inputCssKey)){
			cssString.append("style=\"");
			Object inputCssValue = node.get(inputCssKey);
			if(inputCssValue instanceof String){
				cssString.append((String)inputCssValue);
			} else {
				Map<String, Object> inputCssMap = (Map<String, Object>)inputCssValue;
				int count = inputCssMap.size();
				for(String key : inputCssMap.keySet()){
					String terminator = "";
					--count;
					if(count == 0){
						terminator = ";";
					}else{
						terminator = "; ";
					}
					
					cssString.append(key+": "+inputCssMap.get(key)+terminator);
				}
			}
			cssString.append("\"");
		}
		
		return cssString.toString().trim();
	}
	
	private static String[] generateTags(String tagName, String inputClassName, String inputCss, String attributes){
		String[] tags = new String[2];
		
		String classNameWhiteSpace = (inputCss.isEmpty()) ? "" : " ";
		String inputCssWhiteSpace = (attributes.isEmpty()) ? "" : " ";
		
		tags[0] = "<"+tagName+" "+inputClassName+classNameWhiteSpace+inputCss+inputCssWhiteSpace+attributes+">";
		tags[1] = "</"+tagName+">";
		
		return tags;
	}
	
	protected static Map<String, FormWrapperInterface> defaultWrapperTemplates() {
		Map<String, FormWrapperInterface> defaultTemplates = new HashMap<String, FormWrapperInterface>();
//		defaultTemplates.put("none", FormWrapperTemplates.titleWrapper);
		defaultTemplates.put("div", FormWrapperTemplates.divWrapper);
		defaultTemplates.put("none", FormWrapperTemplates.noneWrapper);
//		defaultTemplates.put(FormGenerator.dropDownListKey, FormWrapperTemplates.selectWrapper);
//		defaultTemplates.put(FormGenerator.inputFieldKey, FormWrapperTemplates.inputWrapper);
		
		return defaultTemplates;
	}
}
