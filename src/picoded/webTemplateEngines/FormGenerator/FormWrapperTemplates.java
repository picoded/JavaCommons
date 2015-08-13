package picoded.webTemplateEngines.FormGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			StringBuilder[] labelArr = node.defaultHtmlWrapper( HtmlTag.DIV, node.prefix_standard()+"label", null );
			
			ret.append( labelArr[0] );
			ret.append( label );
			ret.append( labelArr[1] );
		}
		
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
		
		if( label != null && label.length() > 0 ) {
			StringBuilder[] labelArr = node.defaultHtmlWrapper( HtmlTag.DIV, node.prefix_standard()+"label", null );
			
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
		
		Object rawValue = node.getFieldValue();
		List<Object> valuesList = null;
		if( rawValue != null && rawValue instanceof List ) {
			valuesList = (List<Object>)rawValue;
		}
		
		String subAutoClass = node.getString("class");
		if(subAutoClass != null && subAutoClass.length() > 0) {
			subAutoClass = subAutoClass+"_node";
		}
		
		/// The children wrapper if needed
		if(childDefination != null && childDefination.size() > 0 && valuesList != null && valuesList.size() > 0) {
			StringBuilder[] childWrap = node.defaultHtmlChildWrapper( HtmlTag.DIV, node.prefix_standard()+"child", null );
			
			ret.append( childWrap[0] );
			
			for(Object subValue : valuesList) {
				// Skip values that are not maps
				if( !(subValue instanceof Map) ) {
					continue;
				}
				
				Map<String,Object> subMapVal = (Map<String,Object>)subValue;
				
				Map<String,Object> nodeMap = new HashMap<String,Object>();
				nodeMap.put("class", subAutoClass);
				nodeMap.put("children", childDefination);
				
				FormNode childNode = new FormNode( node._formGenerator, nodeMap, subMapVal );
				ret.append( childNode.fullHtml(isDisplayMode) );
			}
			
			ret.append( childWrap[1] );
		}
		//-----------------------------------------------
		
		ret.append( wrapperArr[1] );
		return ret;
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
	
	/// noneWrapper
	///
	/// No wrappers
	protected static FormWrapperInterface none = (node)->{
		StringBuilder ret = new StringBuilder();
		ret.append( node.fullChildrenHtml(false) );
		return ret;
	};
	
	protected static Map<String, FormWrapperInterface> defaultWrapperTemplates() {
		Map<String, FormWrapperInterface> defaultTemplates = new HashMap<String, FormWrapperInterface>();
		
		defaultTemplates.put("*", FormWrapperTemplates.divWrapper);
		
		defaultTemplates.put("div", FormWrapperTemplates.divWrapper);
		defaultTemplates.put("for", FormWrapperTemplates.forWrapper);
		defaultTemplates.put("none", FormWrapperTemplates.none);
		
		return defaultTemplates;
	}
}
