package picoded.webTemplateEngines.FormGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import picoded.conv.ConvertJSON;
import picoded.conv.RegexUtils;
import picoded.conv.JMTE;
import picoded.struct.CaseInsensitiveHashMap;

/// FormWrapperTemplates
///
/// Default implmentation of various FormWrapperInterface used in FormGenerator.
///
public class FormWrapperTemplates {
	
	/// The standard div wrapper, with isDisplayMode parameter. This is used in both FormWrapperTemplates
	/// and DisplayWrapperTemplates.
	///
	/// @params {FormNode}  node           - The form node to build the html from
	/// @params {boolean}   isDisplayMode  - The display mode.
	///
	/// @returns {StringBuilder}  the resulting HTML
	protected static StringBuilder standardDivWrapper(FormNode node, boolean isDisplayMode) {
		/// Returning string builder
		StringBuilder ret = new StringBuilder();
		
		/// The overlaying wrapper
		StringBuilder[] wrapperArr = node.defaultHtmlWrapper( HtmlTag.DIV, node.prefix_standard()+"div", null );
		
		/// The wrapper start
		ret.append( wrapperArr[0] );
		
		/// The label, if given
		String label = node.label();
		
		if( label != null && label.length() > 0 ) {
			StringBuilder[] labelArr = node.defaultHtmlLabel( HtmlTag.DIV, node.prefix_standard()+"label", null );
			
			ret.append( labelArr[0] );
			ret.append( label );
			ret.append( labelArr[1] );
		}
		
		StringBuilder inputHtml = node.inputHtml(isDisplayMode);
		ret.append(inputHtml);
		
		/// The children wrapper if needed
		List<FormNode> childList = node.children();
		if(childList != null && childList.size() > 0) {
			StringBuilder[] childWrap = node.defaultHtmlChildWrapper( HtmlTag.DIV, node.prefix_standard()+"child", null );
			
			ret.append( childWrap[0] );
			ret.append( node.fullChildrenHtml(isDisplayMode) );
			ret.append( childWrap[1] );
		}
		
		ret.append( wrapperArr[1] );
		return ret;
	}
	
	protected static StringBuilder checkboxWrapper(FormNode node, boolean isDisplayMode) {
		/// Returning string builder
		StringBuilder ret = new StringBuilder();
		
		/// The overlaying wrapper
		StringBuilder[] wrapperArr = node.defaultHtmlWrapper( HtmlTag.DIV, node.prefix_standard()+"div", null );
		
		/// The wrapper start
		ret.append( wrapperArr[0] );
		
		/// The label, if given
		String label = node.label();
		
		if( label != null && label.length() > 0 ) {
			StringBuilder[] labelArr = node.defaultHtmlLabel( HtmlTag.DIV, node.prefix_standard()+"label", null );
			
			ret.append( labelArr[0] );
			ret.append( label );
			ret.append( labelArr[1] );
		}
		
		StringBuilder inputHtml = node.inputHtml(isDisplayMode);
		ret.append(inputHtml);
		
		/// The children wrapper if needed
		List<FormNode> childList = node.children();
		if(childList != null && childList.size() > 0) {
			StringBuilder[] childWrap = node.defaultHtmlChildWrapper( HtmlTag.DIV, node.prefix_standard()+"child", null );
			
			ret.append( childWrap[0] );
			ret.append( node.fullChildrenHtml(isDisplayMode) );
			ret.append( childWrap[1] );
		}
		
		ret.append( wrapperArr[1] );
		return ret;
	}
	
	/// Iterator for an array of values. This function nearly identical to standard div,
	/// however it uses the field key, to iterate the data map.
	/// 
	/// @params {FormNode}  node           - The form node to build the html from
	/// @params {boolean}   isDisplayMode  - The display mode.
	///
	/// @returns {StringBuilder}  the resulting HTML
	@SuppressWarnings("unchecked")
	protected static StringBuilder forListWrapper(FormNode node, boolean isDisplayMode) {
		/// Returning string builder
		StringBuilder ret = new StringBuilder();
		
		/// The overlaying wrapper
		StringBuilder[] wrapperArr = node.defaultHtmlWrapper( HtmlTag.DIV, node.prefix_standard()+"div", null );
		
		/// The wrapper start
		ret.append( wrapperArr[0] );
		
		/// The label, if given
		String label = node.label();
		if(label != null && label.length() > 0 ) {
			StringBuilder[] labelArr = node.defaultHtmlLabel( HtmlTag.DIV, node.prefix_standard()+"label", null );
			
			ret.append( labelArr[0] );
			ret.append( label );
			ret.append( labelArr[1] );
		}
		
		//-----------------------------------------------
		// Varient of child handling / iterator
		//-----------------------------------------------
		List<Object> childDefination = null;
		if( node.containsKey("children") ) {
			Object childrenRaw = node.get("children");
			
			if( !(childrenRaw instanceof List) ) {
				throw new IllegalArgumentException("'children' parameter found in defination was not a List: "+childrenRaw);
			}
			
			childDefination = (List<Object>)childrenRaw;
		}
		
		Object rawValue = ConvertJSON.toList(node.getFieldValue());
		List<Object> valuesList = null;
		if( rawValue != null && rawValue instanceof List ) {
			valuesList = (List<Object>)rawValue;
		}
		
		String subAutoClass = node.getString("class");
		if(subAutoClass != null && subAutoClass.length() > 0) {
			subAutoClass = "pff_childX pff_"+subAutoClass+"_forChild";
		} else {
			subAutoClass = "pff_childX";
		}
		
		//removal of child labels if needed
		boolean removeLabel = Boolean.parseBoolean(node.getString(JsonKeys.REMOVE_LABEL_FROM_SECOND_ITERATION, "false"));
		List<Object> childDefinitionsWithoutLabel = null;
		
		if(removeLabel){
			childDefinitionsWithoutLabel = new ArrayList<Object>();
			for(Object obj:childDefination){
				if(obj instanceof Map){
					Map<String, Object> objMap = (Map<String, Object>)obj;
					objMap.remove(JsonKeys.LABEL);
					childDefinitionsWithoutLabel.add(objMap);
				}
			}
		}
		
		/// The children wrapper if needed
		if(childDefination != null && childDefination.size() > 0 && valuesList != null && valuesList.size() > 0) {
			StringBuilder[] childWrap = node.defaultHtmlChildWrapper( HtmlTag.DIV, node.prefix_standard()+"child", null );
			
			ret.append( childWrap[0] );
			
			if(valuesList != null){
				int a=0;
				for(Object subValue : valuesList) {
					// Skip values that are not maps
					if( !(subValue instanceof Map) ) {
						continue;
					}
					
					Map<String,Object> subMapVal = (Map<String,Object>)subValue;
					
					Map<String,Object> nodeMap = new HashMap<String,Object>();
					nodeMap.put("type", "div");
					nodeMap.put("wrapperClass", subAutoClass+" pff_forChild"+a);
					if(removeLabel){
						nodeMap.put("children", childDefinitionsWithoutLabel);
					}else{
						nodeMap.put("children", childDefination);
					}
					
					FormNode childNode = new FormNode( node._formGenerator, nodeMap, subMapVal );
					ret.append( childNode.fullHtml(isDisplayMode) );
					
					++a;
				}
			}
			
			ret.append( childWrap[1] );
		}
		//-----------------------------------------------
		
		ret.append( wrapperArr[1] );
		return ret;
	}
	
	protected static StringBuilder tableWrapper(FormNode node, boolean isDisplayMode){
		StringBuilder ret = new StringBuilder();
		
		//wrapper
		StringBuilder[] wrapperArr = node.defaultHtmlWrapper( HtmlTag.DIV, "pf_div pfw_table", null );
		ret.append(wrapperArr[0]);
		
		String label = node.label();
		if( label != null && label.length() > 0 ) {
			StringBuilder[] labelArr = node.defaultHtmlLabel( HtmlTag.DIV, node.prefix_standard()+"label", null );
			
			ret.append( labelArr[0] );
			ret.append( label );
			ret.append( labelArr[1] );
		}
		
		StringBuilder inputHtml = node.inputHtml(isDisplayMode);
		
		ret.append(inputHtml);
		ret.append("</div>");
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	protected static List<Object> getChildren(FormNode node){
		if( node.containsKey("children")) {
			Object childrenRaw = node.get("children");
			
			if( !(childrenRaw instanceof List) ) {
				throw new IllegalArgumentException("'children' parameter found in defination was not a List: "+childrenRaw);
			}
			
			return (List<Object>)childrenRaw;
		}else{
			return null;
		}
	}
	
	/// divWrapper
	///
	/// Does a basic div wrapper
	protected static FormWrapperInterface divWrapper = (node)->{
		return FormWrapperTemplates.standardDivWrapper(node, false);
	};
	
	/// forWrapper
	///
	/// Does a basic div wrapper
	protected static FormWrapperInterface forWrapper = (node)->{
		return FormWrapperTemplates.forListWrapper(node, false);
	};
	
	protected static FormWrapperInterface tableWrapper = (node)->{
		return FormWrapperTemplates.tableWrapper(node, false);
	};
	
	/// noneWrapper
	///
	/// No wrappers
	protected static FormWrapperInterface none = (node)->{
		StringBuilder ret = new StringBuilder();
		ret.append( node.fullChildrenHtml(false) );
		return ret;
	};
	
	/// Reusable JMTE engine
	protected static JMTE jmteObj = new JMTE();
	
	/// JMTE based wrapper, used to include non standard HTML code
	/// 
	/// @params {FormNode}  node           - The form node to build the html from
	/// @params {boolean}   isDisplayMode  - The display mode.
	///
	/// @returns {StringBuilder}  the resulting HTML
	@SuppressWarnings("unchecked")
	protected static StringBuilder JMTEWrapper(FormNode node, boolean isDisplayMode) {
		/// Returning string builder
		StringBuilder ret = new StringBuilder();
		
		// Get the JMTE argument map
		Map<String,Object> jmte_arguments = null;
		
		// From the field?
		String fieldName = node.getFieldName();
		if( fieldName == null || fieldName.length() <= 0 ) {
			// Use the value map as the argument Map
			jmte_arguments = node.getValueMap();
		} else {
			// Get the sub argument
			Object subArgument = node.getValueMap().get(fieldName);
			
			if( subArgument != null ) {
				if( subArgument instanceof Map ) {
					jmte_arguments = new HashMap<String,Object>( (Map<String,Object>)subArgument );
				} else {
					throw new RuntimeException("@TODO: non map field argument handling");
				}
			} else {
				throw new RuntimeException("@TODO: JMTE wrapper default fallback implmentation");
			}
		}
		
		// Gets the JMTE template to use
		Object jmte_template_obj = null;
		
		if( isDisplayMode ) {
			jmte_template_obj = node.get( JsonKeys.JMTE_DISPLAY );
		} else {
			jmte_template_obj = node.get( JsonKeys.JMTE_INPUT );
		}
		
		if( jmte_template_obj == null ) {
			jmte_template_obj = node.get( JsonKeys.JMTE_GENERIC );
		}
		
		if( jmte_template_obj == null ) {
			if( isDisplayMode ) {
				throw new RuntimeException("JMTE Wrapper is missing JMTE_( DISPLAY / GENERIC ) implementation" );
			} else {
				throw new RuntimeException("JMTE Wrapper is missing JMTE_( INPUT/ GENERIC ) implementation" );
			}
		}
		
		// Generate the code
		ret.append( jmteObj.parseTemplate( jmte_template_obj.toString(), jmte_arguments ) );
		
		return ret;
	}
	
	/// JMTE wrapper implementation
	protected static FormWrapperInterface jmteWrapper = (node)->{
		return FormWrapperTemplates.JMTEWrapper(node, false);
	};
	
	/// Wrapper templates
	protected static Map<String, FormWrapperInterface> defaultWrapperTemplates() {
		Map<String, FormWrapperInterface> defaultTemplates = new HashMap<String, FormWrapperInterface>();
		
		defaultTemplates.put("*", FormWrapperTemplates.divWrapper);
		
		defaultTemplates.put("div", FormWrapperTemplates.divWrapper);
		defaultTemplates.put("for", FormWrapperTemplates.forWrapper);
		defaultTemplates.put("none", FormWrapperTemplates.none);
		defaultTemplates.put("table", FormWrapperTemplates.tableWrapper);
		
		defaultTemplates.put("jmte", FormWrapperTemplates.jmteWrapper);
		
		return defaultTemplates;
	}
}
