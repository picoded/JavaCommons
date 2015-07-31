package picoded.webTemplateEngines;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.amazonaws.util.StringUtils;
///
/// Web templating engine that helps define and convert a JSON styled template, into the actual web form
///
public class FormGenerator {
	private Map<String, FormWrapperInterface> customFormWrapperTemplates = new HashMap<String, FormWrapperInterface>();
	private Map<String, FormInputInterface> customFormInputTemplates = new HashMap<String, FormInputInterface>();
	
	private Map<String, FormWrapperInterface> customPDFWrapperTemplates = new HashMap<String, FormWrapperInterface>();
	private Map<String, FormInputInterface> customPDFInputTemplates = new HashMap<String, FormInputInterface>();
	
	public FormGenerator(){
		setupDefaultFormTemplates();
	}
	
	private void setupDefaultFormTemplates(){
		customFormWrapperTemplates = FormWrapperTemplates.defaultWrapperTemplates();
		customFormInputTemplates = FormInputTemplates.defaultInputTemplates();
		
		customPDFWrapperTemplates = PDFWrapperTemplates.defaultPDFWrapperTemplates();
		customPDFInputTemplates = PDFInputTemplates.defaultPDFInputTemplates();
	}
	
	public FormWrapperInterface addCustomFormWrapperTemplate(String key, FormWrapperInterface customWrapperTemplate){
		return customFormWrapperTemplates.put(key, customWrapperTemplate);
	}
	
	public FormInputInterface addCustomFormInputTemplate(String key, FormInputInterface customInputTemplate){
		return customFormInputTemplates.put(key, customInputTemplate);
	}
	
	public String generatePDFReadyHTML(List<FormNode> nodes){
		StringBuilder htmlBuilder = new StringBuilder();
		for(FormNode node : nodes){
			String nodeHtml = generatePDFReadyHTML(node);
			htmlBuilder.append(nodeHtml);
		}
		
		return htmlBuilder.toString();
	}
	
	public String generatePDFReadyHTML(FormNode node){
		String nodeType = node.getString(JsonKeys.TYPE, "div");
		String[] formWrappers = new String[]{"", ""};
		
		String wrapperType = node.getString("wrapper", "default");
		
		formWrappers = customFormWrapperTemplates.get(wrapperType).apply(node);
		
		String formTextData = customPDFInputTemplates.get(nodeType).apply(node);
		
		//get inner data for children
		StringBuilder innerData = new StringBuilder("");
		if(node.childCount() > 0){
			innerData.append(generatePDFReadyHTML(node.children()));
		}
		
		String finalNodeValue = formWrappers[0]+formTextData+innerData.toString()+formWrappers[1];
		return finalNodeValue;
	}
	
	public String applyTemplating(List<FormNode> nodes){
		StringBuilder htmlBuilder = new StringBuilder();
		for(FormNode node : nodes){
			htmlBuilder.append(applyTemplating(node));
		}
		
		return htmlBuilder.toString();
	}
	
	public String applyTemplating(FormNode node){
		String nodeType = node.getString(JsonKeys.TYPE, "div");
		String[] formWrappers = new String[]{"", ""};
		
		String wrapperType = node.getString("wrapper", "default");
		
		formWrappers = customFormWrapperTemplates.get(wrapperType).apply(node);
		String formTextData = customFormInputTemplates.get(nodeType).apply(node);
		
		//get inner data for children
		StringBuilder innerData = new StringBuilder("");
		if(node.childCount() > 0){
			innerData.append(applyTemplating(node.children()));
		}
		
		String finalNodeValue = formWrappers[0]+formTextData+innerData.toString()+formWrappers[1];
		return finalNodeValue;
	}
	
	public static String getWrapperClassString(FormNode node){
		StringBuilder sb = new StringBuilder(" class=\"");
		
		if(node.containsKey("wrapperClass")){
			sb.append(node.getString("wrapperClass") + "\"");
		} else {
			sb.append("pf_"+node.getString(JsonKeys.TYPE)+"Class\"");
		}
		
		return sb.toString();
	}
	
	public static String getLabelClassString(FormNode node){
		StringBuilder sb = new StringBuilder(" class=\"");
		
		if(node.containsKey("labelClass")){
			sb.append(node.getString("labelClass") + "\"");
		} else {
			sb.append("pf_labelClass\"");
		}
		
		return sb.toString();
	}
	
	public static String getInputClassString(FormNode node){
		StringBuilder sb = new StringBuilder(" class=\"");
		
		if(node.containsKey("inputClass")){
			sb.append(node.getString("inputClass") + "\"");
		} else {
			sb.append("pf_inputClass\"");
		}
		
		return sb.toString();
	}
	
	public static String getWrapperCssString(FormNode node){
		StringBuilder sb = new StringBuilder("");
		
		if(node.containsKey("wrapperCss")){
			sb.append(" style=\""+node.getString("wrapperCss")+"\"");
		}
		
		return sb.toString();
	}
	
	public static String getInputCssString(FormNode node){
		StringBuilder sb = new StringBuilder("");
		
		if(node.containsKey("inputCss")){
			sb.append(" style=\""+node.getString("inputCss")+"\"");
		}
		
		return sb.toString();
	}
	
	public static String getLabelCssString(FormNode node){
		StringBuilder sb = new StringBuilder("");
		
		if(node.containsKey("labelCss")){
			sb.append(" style=\""+node.getString("labelCss")+"\"");
		}
		
		return sb.toString();
	}
}
