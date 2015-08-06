package picoded.webTemplateEngines;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import picoded.conv.RegexUtils;

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
		FormInputTemplates.getInputClass(node, classBuilder);
		FormInputTemplates.getLabelClass(node, classBuilder);
		classBuilder.append("\"");
		String inputClassString = classBuilder.toString();
		String inputCssString = FormGenerator.getInputCssString(node);
		
		sb.append("<h3"+inputClassString+inputCssString+">"+text+"</h3>\n");
		
		return sb.toString();
	};
	
	protected static FormInputInterface default_pdf = (node)->{
		StringBuilder sb = new StringBuilder();
		
		String labelClassName = getLabelClassName(node);
		String pdfOutputClassName = getPdfOutputClassName(node);
		
		String labelString = node.label();
		if(!labelString.isEmpty()){
			sb.append("<"+HtmlTag.DIV+" class=\""+labelClassName+"\">"+labelString+"</"+HtmlTag.DIV+">");
		}
		
		String fieldName = node.field();
		if(!fieldName.isEmpty()){
			String fieldValue = (String)node.getDefaultValue(fieldName);
			if(!StringUtils.isNullOrEmpty(fieldValue)){
				sb.append("<"+HtmlTag.DIV+" class=\""+pdfOutputClassName+"\">"+fieldValue+"</"+HtmlTag.DIV+">");
			}
		}
		return sb.toString();
	};
	
	private static String getLabelClassName(FormNode node){
		if(node.containsKey("labelClass")){
			return node.getString("labelClass");
		}else{
			return "pf_labelClass";
		}
	}
	
	private static String getPdfOutputClassName(FormNode node){
		if(node.containsKey("pdfOutputClass")){
			return node.getString("pdfOutputClass");
		}else{
			return "pf_pdfOutputClass";
		}
	}
	
	public static Map<String, FormInputInterface> defaultPDFInputTemplates(){
		Map<String, FormInputInterface> defaultTemplates = new HashMap<String, FormInputInterface>();
		
		defaultTemplates.put("dropdown", default_pdf);
		defaultTemplates.put("text", default_pdf);
		defaultTemplates.put("div", div_pdf);
		defaultTemplates.put("title", header_pdf);
		
		return defaultTemplates;
	}
}
