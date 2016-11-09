package picoded.conv;

// Java libs
import java.io.StringReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

// XML library used
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException; 
import org.xml.sax.InputSource;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

///
/// XML simplification helpers. When you do not need custom object / array structures
///
/// Which is frankly speaking should be 99.99% of the time. Seriously just use Map, 
/// instead of custom classes. It will save you alot of headache in the future.
///
public class ConvertXML {
	
	/// Invalid constructor (throws exception)
	protected ConvertXML() {
		throw new IllegalAccessError("Utility class");
	}
	
	/// Illegal JSON format type. Used to handle all format exceptions in this class
	///
	/// Can be treated as a RuntimeException, and IllegalArgumentException
	public static class InvalidFormatXML extends IllegalArgumentException {
		/// Common message
		public InvalidFormatXML(Throwable cause) {
			this("Invalid Format XML", cause);
		}
		
		/// Cloning the constructor
		public InvalidFormatXML(String message, Throwable cause) {
			super(message, cause);
		}
		
		/// Cloning the constructor
		public InvalidFormatXML(String message) {
			super(message);
		}
	}
	
	/// Dom format from XML string.
	///
	/// @param  XML string to use
	///
	/// @return   Document object to return
	public static Document toDocument(String xmlStr) {
		try {
			// Document builder
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlStr));
			
			// Normalize and return
			Document ret = builder.parse(is);
			ret.getDocumentElement().normalize();
			ret.normalizeDocument();
			return ret;
		} catch(Exception e) {
			throw new InvalidFormatXML(e);
		}
	}
	
	/// Dom format from XML string.
	///
	/// @param  XML string to use
	///
	/// @return   Document Element object to return
	public static Element toElement(String xmlStr) {
		return toDocument(xmlStr).getDocumentElement();
	}
	
	/// #text string tag prefix
	protected static String TEXT_PREFIX_TAG = "#text: ";
	
	/// Map<String,Object>, from XML string. Where the "child" key word is reserved for child nodes
	///
	/// @param  XML string to use
	///
	/// @return   Result map
	public static Map<String,Object> toMap(Element el) {
		Map<String,Object> ret = new HashMap<String,Object>();
		
		// Process element
		if(el != null) {
			//
			// Process attributes
			//
			NamedNodeMap attributeMap = el.getAttributes();
			int len = attributeMap.getLength();
			for (int i = 0; i < len; ++i) {
				Node attr = attributeMap.item(i);
				ret.put(attr.getNodeName(), attr.getNodeValue());
			}
			
			//
			// Process tag name
			//
			ret.put("TagName", el.getTagName());
			
			//
			// Processs children nodes
			//
			NodeList childList = el.getChildNodes();
			int childListLen = (childList != null)? childList.getLength() : 0;
			if( len > 0 ) {
				ArrayList<Object> list = new ArrayList<Object>();
				String nodeValue = "";
				
				// Every child item
				for(int i=0; i< childListLen; ++i) {
					Object item = childList.item(i);
					if( item instanceof Element ) {
						Element itemEle = (Element)item;
						list.add( toMap(itemEle) );
					} else if( item instanceof Node ) {
						// Text value
						nodeValue = nodeValue + ((Node)item).getNodeValue();
					} else {
						throw new InvalidFormatXML("Unexpected item type : "+item.toString());
					}
				}
				
				if( list.size() > 0 ) {
					ret.put("ChildNodes", list);
				}
				
				if( nodeValue.length() > 0 ) {
					ret.put("NodeValue", nodeValue);
				}
			}
			
		}
		
		return ret;
	}
	
	/// Map<String,Object>, from XML string. Where the "child" key word is reserved for child nodes
	///
	/// @param  XML string to use
	///
	/// @return   Result map
	public static Map<String,Object> toMap(String xmlStr) {
		return toMap( toElement(xmlStr) );
	}
	
}
