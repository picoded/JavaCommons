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
		FormInputTemplates.getCustomClass(node, classBuilder);
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
		
		StringBuilder pdfOutputClassBuilder = new StringBuilder(" class=\"pf_pdfOutput");
		getPDFOutputClass(node, pdfOutputClassBuilder);
		pdfOutputClassBuilder.append("\"");
		
		String labelString = node.label();
		if(!labelString.isEmpty()){
			StringBuilder labelClassBuilder = new StringBuilder(" class=\"pf_label");
			FormInputTemplates.getLabelClass(node,  labelClassBuilder);
			labelClassBuilder.append("\"");
			sb.append("<"+HtmlTag.DIV+labelClassBuilder.toString()+">"+labelString+"</"+HtmlTag.DIV+">");
		}
		
		String fieldName = node.field();
		if(!fieldName.isEmpty()){
			String fieldValue = (String)node.getDefaultValue(fieldName);
			if(!StringUtils.isNullOrEmpty(fieldValue)){
				sb.append("<"+HtmlTag.DIV+pdfOutputClassBuilder.toString()+">"+fieldValue+"</"+HtmlTag.DIV+">");
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
		defaultTemplates.put("rawHtml", rawHtml_pdf);
	
		
		return defaultTemplates;
	}
}
