package picoded.webTemplateEngines.FormGenerator;

import java.util.HashMap;
import java.util.Map;

public class DisplayWrapperTemplates {
	
	public static FormWrapperInterface default_pdf=(node)->{
		/// Returning string builder
		StringBuilder ret = new StringBuilder();
		
		/// The overlaying wrapper
		StringBuilder[] wrapperArr = node.defaultHtmlWrapper( HtmlTag.DIV, node.prefix_standard()+"div", null );
		
		/// The wrapper start
		ret.append( wrapperArr[0] );
		
		/// The label, if given
		String label = node.label();
		
		if( label != null && label.size() > 0 ) {
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
			ret.append( node.fullChildrenHtml(false) );
			ret.append( childWrap[1] );
		}
		
		ret.append( wrapperArr[1] );
		
		return ret;
	};
	
	public static FormWrapperInterface none_pdf=(node)->{
		StringBuilder ret = new StringBuilder();
		ret.append( node.fullChildrenHtml(true) );
		return ret;
	};
	
	public static Map<String, FormWrapperInterface> defaultWrapperTemplates(){
		Map<String, FormWrapperInterface> defaultTemplates = new HashMap<String, FormWrapperInterface>();
		
		defaultTemplates.put("*", default_pdf);
		
		defaultTemplates.put("div", default_pdf);
		defaultTemplates.put("none", none_pdf);
		
		return defaultTemplates;
	}
}
