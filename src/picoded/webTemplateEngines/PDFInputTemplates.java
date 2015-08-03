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
		String text = node.getString("text", "");
		
		StringBuilder sb = new StringBuilder();
		
		String inputClassString = FormGenerator.getInputClassString(node);
		String inputCssString = FormGenerator.getInputCssString(node);
		
		sb.append("<h3"+inputClassString+inputCssString+">"+text+"</h3>\n");
		
		return sb.toString();
	};
	
	protected static FormInputInterface default_pdf = (node)->{
		StringBuilder sb = new StringBuilder();
		
		String labelClassName = getLabelClassName(node);
		String pdfOutputClassName = getPdfOutputClassName(node);
		
		if(node.containsKey("label")){
			String labelString = node.getString("label");
			sb.append("<div class=\""+labelClassName+"\">"+labelString+"</div>");
		}
		
		String idValue = "";
		if(node.containsKey("id")){
			String idKey = node.getString("id");
			idValue = (String)node.getDefaultValue(idKey);
			if(!StringUtils.isNullOrEmpty(idValue)){
				sb.append("<div class=\""+pdfOutputClassName+"\">"+idValue+"</div>");
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
