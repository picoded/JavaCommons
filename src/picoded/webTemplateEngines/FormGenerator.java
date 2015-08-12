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
	
	public String generatePDFReadyHTML(String jsonString, Map<String, Object> prefilledJSONData){
		List<FormNode> formNodes = FormNode.createFromJSONString(jsonString, prefilledJSONData);
		String htmlString = generatePDFReadyHTML(formNodes);
//		htmlString = "<div class=\"pf_root\">"+htmlString+"</div>";
		return htmlString;
	}
	
	protected String generatePDFReadyHTML(List<FormNode> nodes){
		StringBuilder htmlBuilder = new StringBuilder();
		for(FormNode node : nodes){
			String nodeHtml = generatePDFReadyHTML(node);
			htmlBuilder.append(nodeHtml);
		}
		
		return htmlBuilder.toString();
	}
	
	protected String generatePDFReadyHTML(FormNode node){
		String nodeType = node.getString(JsonKeys.TYPE, HtmlTag.DIV);
		String[] formWrappers = new String[]{"", ""};
		
		String wrapperType = node.getString(JsonKeys.WRAPPER, HtmlTag.DIV);
		
		formWrappers = customPDFWrapperTemplates.get(wrapperType).apply(node);
		
		String formTextData = customPDFInputTemplates.get(nodeType).apply(node);
		
		//get inner data for children
		StringBuilder innerData = new StringBuilder("");
		if(node.childCount() > 0){
			innerData.append(generatePDFReadyHTML(node.children()));
		}
		
		String finalNodeValue = formWrappers[0]+formTextData+innerData.toString()+formWrappers[1];
		return finalNodeValue;
	}
	
	public String applyTemplating(String jsonString, Map<String, Object> prefilledJSONData){
		List<FormNode> formNodes = FormNode.createFromJSONString(jsonString, prefilledJSONData);
		String htmlString = applyTemplating(formNodes);
//		htmlString = "<div class=\"pf_root\">"+htmlString+"</div>";
		return htmlString;
	}
	
	protected String applyTemplating(List<FormNode> nodes){
		StringBuilder htmlBuilder = new StringBuilder();
		for(FormNode node : nodes){
			htmlBuilder.append(applyTemplating(node));
		}
		
		return htmlBuilder.toString();
	}
	
	protected String applyTemplating(FormNode node){
		String nodeType = node.getString(JsonKeys.TYPE, HtmlTag.DIV);
		String[] formWrappers = new String[]{"", ""};
		
		String wrapperType = node.getString(JsonKeys.WRAPPER, HtmlTag.DIV);
		
		formWrappers = customFormWrapperTemplates.get(wrapperType).apply(node);
		if(node.containsKey(JsonKeys.HTML_INJECTION)){
			String rawHtml = node.getString(JsonKeys.HTML_INJECTION);
			String finalNodeValue = formWrappers[0]+rawHtml+formWrappers[1];
			return finalNodeValue;
		}else{
			String formTextData = customFormInputTemplates.get(nodeType).apply(node);
			
			//get inner data for children
			StringBuilder innerData = new StringBuilder("");
			if(node.childCount() > 0){
				innerData.append("<div class=\"pf_childDiv");
				getCustomClass(node, innerData, JsonKeys.CUSTOMCLASS, "pfc_");
				getCustomClass(node, innerData, JsonKeys.CHILD_CLASS, "");
				innerData.append("\">\n");
				innerData.append(applyTemplating(node.children()));
				innerData.append("</div>\n");
			}
			
			String finalNodeValue = formWrappers[0]+formTextData+innerData.toString()+formWrappers[1];
			return finalNodeValue;
		}
	}
	
	public static void getCustomClass(FormNode node, StringBuilder sb, String jsonKey, String prefix){
		if(node.containsKey(jsonKey)){
			String wrapperClass = node.getString(jsonKey);
			String[] wrapperClassSplit = null;
			if(wrapperClass.contains(" ")){
				wrapperClassSplit = wrapperClass.split(" ");
				for(String str:wrapperClassSplit){
					if(!str.equals(" ")){
						sb.append(" "+prefix+str);
					}
				}
			}else{
				sb.append(" "+prefix+wrapperClass);
			}
		}
	}
	
	public static String getWrapperCssString(FormNode node){
		StringBuilder sb = new StringBuilder("");
		
		if(node.containsKey(JsonKeys.WRAPPER_CSS)){
			sb.append(" style=\""+node.getString(JsonKeys.WRAPPER_CSS)+"\"");
		}
		
		return sb.toString();
	}
	
	public static String getInputCssString(FormNode node){
		StringBuilder sb = new StringBuilder("");
		
		if(node.containsKey(JsonKeys.INPUT_CSS)){
			sb.append(" style=\""+node.getString(JsonKeys.INPUT_CSS)+"\"");
		}
		
		return sb.toString();
	}
	
	public static String getLabelCssString(FormNode node){
		StringBuilder sb = new StringBuilder("");
		
		if(node.containsKey(JsonKeys.LABEL_CSS)){
			sb.append(" style=\""+node.getString(JsonKeys.LABEL_CSS)+"\"");
		}
		
		return sb.toString();
	}
}
