package picoded.webTemplateEngines;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import picoded.conv.RegexUtils;

import com.amazonaws.services.datapipeline.model.Field;
import com.hazelcast.instance.Node;
import com.mysql.jdbc.StringUtils;

public class FormInputTemplates {
	
	protected static FormInputInterface div = (node)->{
		StringBuilder sb = new StringBuilder("");
		
		if(node.containsKey(JsonKeys.HTML_INJECTION)){
			sb.append(node.getString(JsonKeys.HTML_INJECTION));
			return sb.toString();
		}
		
		return sb.toString();
		
	};
	
	protected static FormInputInterface header = (node)->{ 
		StringBuilder sb = new StringBuilder("");
		
		if(node.containsKey(JsonKeys.HTML_INJECTION)){
			sb.append(node.getString(JsonKeys.HTML_INJECTION));
			return sb.toString();
		}else{
			String text = node.getString(JsonKeys.LABEL, "");
			
			StringBuilder classBuilder = new StringBuilder(" class=\"pf_header");
			getCustomClass(node, classBuilder);
			getInputClass(node, classBuilder);
			getLabelClass(node, classBuilder);
			classBuilder.append("\"");
			
			String inputCssString = FormGenerator.getInputCssString(node);
			
			sb.append("<h3"+classBuilder.toString() + inputCssString+">"+text+"</h3>\n");
			
			return sb.toString();
		}
	};
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface select = (node)->{
		StringBuilder sb = new StringBuilder();
		
		if(node.containsKey(JsonKeys.HTML_INJECTION)){
			sb.append(node.getString(JsonKeys.HTML_INJECTION));
			return sb.toString();
		}else{
			String labelValue = node.label();
			String fieldValue = node.field();
			if(!labelValue.isEmpty()){
				StringBuilder labelClassBuilder = new StringBuilder(" class=\"pf_label");
				getLabelClass(node, labelClassBuilder);
				labelClassBuilder.append("\"");
				
				sb.append("<"+HtmlTag.LABEL+labelClassBuilder.toString()+" for=\""+fieldValue+"\">"+labelValue+"</"+HtmlTag.LABEL+">\n");
			}
			
			StringBuilder classStringBuilder = new StringBuilder(" class=\"pf_select");
			getInputClass(node, classStringBuilder);
			getCustomClass(node, classStringBuilder);
			classStringBuilder.append("\"");
			String inputClassString = classStringBuilder.toString();
			
			String selectedOption = "";
			if(!fieldValue.isEmpty()){
				String fieldHtmlString = " "+HtmlTag.ID+"=\""+fieldValue+"\"";
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
			
			return sb.toString();
		}
	};
	
	protected static FormInputInterface input_text = (node)->{
		StringBuilder sb = new StringBuilder();
		
		if(node.containsKey(JsonKeys.HTML_INJECTION)){
			sb.append(node.getString(JsonKeys.HTML_INJECTION));
			return sb.toString();
		}else{
		
			String labelValue = node.label();
			String fieldValue = node.field();
			if(!labelValue.isEmpty()){
				StringBuilder labelClassBuilder = new StringBuilder(" class=\"pf_label");
				getLabelClass(node, labelClassBuilder);
				labelClassBuilder.append("\"");
				
				sb.append("<"+HtmlTag.LABEL+" "+labelClassBuilder.toString()+" for=\""+fieldValue+"\">"+labelValue+"</"+HtmlTag.LABEL+">\n");
			}
			
			StringBuilder classStringBuilder = new StringBuilder(" class=\"pf_inputText");
			getInputClass(node, classStringBuilder);
			getCustomClass(node, classStringBuilder);
			classStringBuilder.append("\"");
			String inputClassString = classStringBuilder.toString();
			
			sb.append("<"+HtmlTag.INPUT+""+inputClassString+" "+HtmlTag.TYPE+"=\"text\" ");
			
			//id/field and value elements
			if(!fieldValue.isEmpty()){
				sb.append(""+HtmlTag.ID+"=\""+fieldValue+"\"");
				String fieldDefaultValue = (String)node.getDefaultValue(fieldValue);
				if(!StringUtils.isNullOrEmpty(fieldDefaultValue)){
					sb.append(" "+HtmlTag.VALUE+"=\""+fieldDefaultValue+"\"></"+HtmlTag.INPUT+">\n");
				}else{
					sb.append("></"+HtmlTag.INPUT+">\n");
				}
			}else{
				sb.append("></"+HtmlTag.INPUT+">\n");
			}
			
			return sb.toString();
		}
	};
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface dropdownWithOthers = (node)->{
		StringBuilder sb = new StringBuilder();
		
		if(node.containsKey(JsonKeys.HTML_INJECTION)){
			sb.append(node.getString(JsonKeys.HTML_INJECTION));
			return sb.toString();
		}else{
			String labelValue = node.label();
			String fieldValue = node.field();
			if(!labelValue.isEmpty()){
				StringBuilder labelClassBuilder = new StringBuilder(" class=\"pf_label");
				getLabelClass(node, labelClassBuilder);
				labelClassBuilder.append("\"");
				
				sb.append("<"+HtmlTag.LABEL+labelClassBuilder.toString()+" for=\""+fieldValue+"\">"+labelValue+"</"+HtmlTag.LABEL+">\n");
			}
			
			StringBuilder classStringBuilder = new StringBuilder(" class=\"pf_select");
			getInputClass(node, classStringBuilder);
			getCustomClass(node, classStringBuilder);
			classStringBuilder.append("\"");
			String inputClassString = classStringBuilder.toString();
			
			String selectedOption = "";
			if(!fieldValue.isEmpty()){
				String fieldHtmlString = " "+HtmlTag.ID+"=\""+fieldValue+"\"";
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
			
			return sb.toString();
		}
	};
	
	protected static FormInputInterface raw_html = (node)->{
		StringBuilder sb = new StringBuilder();
		sb.append(node.getString(JsonKeys.HTML_INJECTION));
		
		return sb.toString();
	};
	
	protected static void getInputClass(FormNode node, StringBuilder sb){
		if(node.containsKey(JsonKeys.INPUT_CLASS)){
			String wrapperClass = node.getString(JsonKeys.INPUT_CLASS);
			String[] wrapperClassSplit = null;
			if(wrapperClass.contains(" ")){
				wrapperClassSplit = wrapperClass.split(" ");
				for(String str:wrapperClassSplit){
					if(!str.equals(" ")){
						sb.append(" pfi_"+str);
					}
				}
			}else{
				sb.append(" pfi_"+wrapperClass);
			}
		}
	}
	
	protected static void getCustomClass(FormNode node, StringBuilder sb){
		if(node.containsKey(JsonKeys.CUSTOMCLASS)){
			String wrapperClass = node.getString(JsonKeys.CUSTOMCLASS);
			String[] wrapperClassSplit = null;
			if(wrapperClass.contains(" ")){
				wrapperClassSplit = wrapperClass.split(" ");
				for(String str:wrapperClassSplit){
					if(!str.equals(" ")){
						sb.append(" pfc_"+str);
					}
				}
			}else{
				sb.append(" pfc_"+wrapperClass);
			}
		}
	}
	
	protected static void getLabelClass(FormNode node, StringBuilder sb){
		if(node.containsKey(JsonKeys.LABEL_CLASS)){
			String wrapperClass = node.getString(JsonKeys.LABEL_CLASS);
			String[] wrapperClassSplit = null;
			if(wrapperClass.contains(" ")){
				wrapperClassSplit = wrapperClass.split(" ");
				for(String str:wrapperClassSplit){
					if(!str.equals(" ")){
						sb.append(" pfl_"+str);
					}
				}
			}else{
				sb.append(" pfl_"+wrapperClass);
			}
		}
	}
	
	protected static Map<String, FormInputInterface> defaultInputTemplates() {
		Map<String, FormInputInterface> defaultTemplates = new HashMap<String, FormInputInterface>();
		defaultTemplates.put(JsonKeys.TITLE, FormInputTemplates.header);
		defaultTemplates.put(JsonKeys.DROPDOWN, FormInputTemplates.select);
		defaultTemplates.put(JsonKeys.TEXT, FormInputTemplates.input_text);
		defaultTemplates.put(JsonKeys.DIV, FormInputTemplates.div);
		defaultTemplates.put(JsonKeys.HTML_INJECTION, FormInputTemplates.raw_html);
		defaultTemplates.put(JsonKeys.DROPDOWN_WITHOTHERS, dropdownWithOthers);
		
		return defaultTemplates;
	}
}
