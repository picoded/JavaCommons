package picoded.webTemplateEngines;

import java.util.List;
import java.util.Map;

public class FormWrapperTemplates {
	
	public static String inputClassPrefix = "pf_";
	public static String inputCssPrefix = "pf_";
	public static String inputClassKey = "inputClass";
	public static String inputCssKey = "inputCss";
	
	public static FormWrapperInterface titleWrapper = (node)->{
		/*
		 * Title Specific Keys
		 * number = h number, example number 1 = h1, number 2 = h2
		 */
		
		String[] titleWrappers = new String[2]; //0 is opening tag, 1 is ending tag
		
		String tagName = "h";
		int headerNumber = node.getInt("number");
		tagName += headerNumber;
		
		String inputClassName = generateInputClass(node);
		String inputCss = generateInputCSS(node);
		
		titleWrappers[0] = "<"+tagName+" "+inputClassName+" "+inputCss+">";
		titleWrappers[1] = "</"+tagName+">";

		return titleWrappers;
	};
	
	public static FormWrapperInterface defaultWrapper = (node)->{
		String[] defaultWrapper = new String[2]; //0 is opening tag, 1 is ending tag
		
		String tagName = "div";
		String inputClassName = generateInputClass(node);
		String inputCss = generateInputCSS(node);
		
		defaultWrapper[0] = "<"+tagName+" "+inputClassName+" "+inputCss+">";
		defaultWrapper[1] = "</"+tagName+">";

		return defaultWrapper;
	};
	
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
		
		return cssString.toString();
	}
}
