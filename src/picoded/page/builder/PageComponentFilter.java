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
/// Using a PageBuilder, filters out the various HTML for their respective component
///
public class PageComponentFilter {
	
	/// Page builder core to use
	public PageBuilderCore core = null;
	
	/// Constructor setting up the page builder core
	public PageComponentFilter(PageBuilderCore inCore) {
		core = inCore;
	}
	
	/// Filtering a html string, and resolving all the page component
	public String resolve(Document doc) {
		//resolveElements( doc.select("page-*") );
		resolveElements(doc.select("*"));
		return doc.toString();
	}
	
	/// Filtering a html string, and resolving all the page component
	public String resolve(String inHTML) {
		return resolve(Jsoup.parse(inHTML));
	}
	
	/// Filtering a html string, and resolving all the page component
	public String resolveParts(String inHTML) {
		return resolve(Jsoup.parseBodyFragment(inHTML));
	}
	
	/// Process page elements
	protected void resolveElements(Elements eArr) {
		for (Element e : eArr) {
			String tagname = e.tagName();
			String innerHTML = e.html();
			
			if (tagname.startsWith("page-") || tagname.startsWith("PAGE-")) {
				//Extract attributes from tag
				List<Attribute> attributes = e.attributes().asList();
				Map<String, Object> tagArgs = new HashMap<String, Object>();
				if (attributes != null && attributes.size() > 0) {
					for (Attribute attr : attributes) {
						tagArgs.put(attr.getKey(), attr.getValue());
					}
				}
				
				e.replaceWith(createElementComponent(tagname, innerHTML, tagArgs));
			}
		}
	}
	
	/// Gets and returns a comopnent.html strictly without going through the JMTE parser
	protected String getRawComponentHtml(String rawPageName) {
		// Depenency chain tracking
		rawPageName = core.filterRawPageName(rawPageName);
		core.addDependencyTracking(rawPageName);
		
		// Get the component html
		String indexFileStr = FileUtils.readFileToString_withFallback(new File(core.pageFolder, rawPageName
			+ "/component.html"), "");
		if ((indexFileStr = indexFileStr.trim()).length() == 0) {
			return null;
		}
		return indexFileStr.toString();
	}
	
	/// Gets the component raw html using the component path, fallsback to index.html for legacy behaviour
	protected String getComponentHtml(String tagname) {
		String ret = getRawComponentHtml(tagname);
		if (ret != null) {
			return ret;
		}
		return core.buildPageInnerRawHTML(tagname);
	}
	
	/// Normalize case sensitivity of path, returns null if no match found
	public String normalizeComponentPath_strict(String tagname) {
		tagname = tagname.replaceAll("-", ".");
		
		String normalized = GenericConvert.normalizeObjectPath(core.buildPageComponentMap(), tagname);
		if (normalized != null && normalized.length() > 0) {
			//System.out.println("createElementComponent.normalized - "+normalized);
			return normalized.replaceAll("\\.", "/");
		}
		
		return null;
	}
	
	/// Normalize case sensitivity of path
	public String normalizeComponentPath(String tagname) {
		tagname = tagname.replaceAll("-", ".");
		
		String normalized = normalizeComponentPath_strict(tagname);
		if (normalized != null && normalized.length() > 0) {
			return normalized;
		}
		
		return tagname.replaceAll("\\.", "/");
	}
	
	/// Create the element node, to inject
	protected Element createElementComponent(String tagname, String innerHTML, Map<String, Object> tagArgs) {
		if (tagname.startsWith("page-") || tagname.startsWith("PAGE-")) {
			tagname = tagname.substring(5);
		}
		
		String componentPath = normalizeComponentPath_strict(tagname);
		if (componentPath == null) {
			throw new RuntimeException("Invalid component path name, Unable to generate : " + tagname);
		}
		
		//System.out.println("createElementComponent.tagname - "+tagname);
		//System.out.println("createElementComponent.componentPath - "+componentPath); 
		componentPath = core.filterRawPageName(componentPath);
		
		String rawHtml = getComponentHtml(componentPath);
		if (rawHtml == null) {
			rawHtml = "";
		}
		GenericConvertMap<String, Object> genericJMTE = GenericConvertMap.build(core.pageJMTEvars(componentPath));
		
		//Add user defined html tags first
		if (componentPath != null && componentPath.length() > 0) {
			// Case insensitive component args
			tagArgs = new CaseInsensitiveHashMap<String, Object>(tagArgs);
			
			// Fix a wierd bug in JMTE when an object is "invalidated", 
			// if it has no, or only a single property 
			while (tagArgs.size() < 2) {
				tagArgs.put("_bug" + tagArgs.size(), 0);
			}
			
			// Component "this" reference
			genericJMTE.put("this", tagArgs);
		}
		
		//then add the component protected keywords
		Map<String, Object> componentProtectedArgs = new HashMap<String, Object>();
		componentProtectedArgs.put("uniqueNumber", newUniqueNumber());
		componentProtectedArgs.put("innerHTML", innerHTML);
		genericJMTE.put("Component", componentProtectedArgs);
		
		String jmteProcessedHtml = core.getJMTE().parseTemplate(rawHtml, genericJMTE);
		String resolvedHtml = resolveParts(jmteProcessedHtml);
		
		Document newDom = Jsoup.parse(resolvedHtml);
		Elements elementSet = newDom.children();
		
		if (elementSet.size() == 1) {
			return elementSet.get(0);
		} else { //container wrapper
			newDom = Jsoup.parse("<div PageComponent='" + tagname.replaceAll("\\.", "-") + "'>" + resolvedHtml + "</div>");
			return newDom.children().get(0);
		}
	}
	
	//-----------------------------------------------------------
	//
	// Component unique number handling
	//
	//-----------------------------------------------------------
	
	/// Running number
	public long componentUniqueNumber = 1;
	
	/// Returns and issue a unique number for a component
	public String newUniqueNumber() {
		return String.valueOf(++componentUniqueNumber);
	}
	
}
