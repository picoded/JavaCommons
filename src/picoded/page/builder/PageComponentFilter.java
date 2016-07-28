package picoded.page.builder;

import java.io.*;
import java.util.*;

// JMTE inner functions add-on
import com.floreysoft.jmte.*;

// Sub modules useds
import picoded.enums.*;
import picoded.conv.*;
import picoded.struct.*;
import picoded.fileUtils.*;
import picoded.servlet.*;
import picoded.servletUtils.*;

// mmm... jsoup modules
import org.jsoup.*;
import org.jsoup.helper.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

///
/// Using a PageBuilder, filters out the various HTML for their respective components
///
public class PageComponentFilter {
	
	/// Page builder core to use
	public PageBuilderCore core = null;
	
	/// Constructor setting up the page builder core
	public PageComponentFilter(PageBuilderCore inCore) {
		core = inCore;
	}
	
	
	/// Filtering a html string, and resolving all the page components
	public String resolve(Document doc) {
		resolveElements( doc.select("page-*") );
		resolveElements( doc.select("PAGE-*") );
		return doc.toString();
	}
	
	/// Filtering a html string, and resolving all the page components
	public String resolve(String inHTML) {
		return resolve( Jsoup.parse(inHTML) );
	}
	
	/// Filtering a html string, and resolving all the page components
	public String resolveParts(String inHTML) {
		return resolve( Jsoup.parseBodyFragment(inHTML) );
	}
	
	/// Process page elements 
	protected void resolveElements(Elements eArr) {
		for(Element e : eArr) {
			String tagname = e.tagName();
			String innerHTML = e.html();
			e.replaceWith( createElementComponent(tagname, innerHTML) );
		}
	}
	
	/// Create the element node, to inject
	protected Element createElementComponent(String tagname, String innerHTML) {
		if(tagname.startsWith("page-") || tagname.startsWith("PAGE-")) {
			tagname = tagname.substring(5);
		}
		tagname = tagname.replaceAll("-","/");
		
		String rawHtml = core.buildPageInnerRawHTML(tagname);
		GenericConvertMap<String,Object> genericJMTE = GenericConvertMap.build( core.pageJMTEvars(tagname) );
		
		String jmteProcessedHtml = core.getJMTE().parseTemplate( rawHtml, genericJMTE );
		String resolvedHtml = resolveParts(jmteProcessedHtml);
		
		Document newDom = Jsoup.parse(resolvedHtml);
		Elements elementSet = newDom.children();
		
		if( elementSet.size() == 1 ) {
			return elementSet.get(0);
		} else {
			// To do a container wrapper
			throw new RuntimeException("Unexpected element children count for : "+tagname+" - "+elementSet.size());
		}
	}
}
