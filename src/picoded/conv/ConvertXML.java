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

// JavaCommons libs used
import picoded.conv.GenericConvert;

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
	protected static Document toDocument(String xmlStr) {
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
		} catch (Exception e) {
			throw new InvalidFormatXML(e);
		}
	}
	
	/// Dom format from XML string.
	///
	/// @param  XML string to use
	///
	/// @return   Document Element object to return
	protected static Element toElement(String xmlStr) {
		return toDocument(xmlStr).getDocumentElement();
	}
	
	/// #text string tag prefix
	protected static String TEXT_PREFIX_TAG = "#text: ";
	
	/// Map<String,Object>, from XML string. Where the "child" key word is reserved for child nodes
	///
	/// @param  XML string to use
	///
	/// @return   Result map
	protected static Map<String, Object> toMap(Element el) {
		Map<String, Object> ret = new HashMap<String, Object>();
		
		// Process element
		if (el != null) {
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
			ret.put("TagName", el.getTagName().toLowerCase());
			
			//
			// Processs children nodes
			//
			NodeList childList = el.getChildNodes();
			int childListLen = (childList != null) ? childList.getLength() : 0;
			if (len > 0) {
				ArrayList<Object> list = new ArrayList<Object>();
				String nodeValue = "";
				
				// Every child item
				for (int i = 0; i < childListLen; ++i) {
					Object item = childList.item(i);
					if (item instanceof Element) {
						Element itemEle = (Element) item;
						list.add(toMap(itemEle));
					} else if (item instanceof Node) {
						// Text value
						nodeValue = nodeValue + ((Node) item).getNodeValue();
					} else {
						throw new InvalidFormatXML("Unexpected item type : " + item.toString());
					}
				}
				
				if (list.size() > 0) {
					ret.put("ChildNodes", list);
				}
				if (nodeValue.length() > 0) {
					ret.put("NodeValue", nodeValue);
				}
			} else {
				String nodeValue = el.getNodeValue();
				if (nodeValue == null || nodeValue.length() <= 0) {
					nodeValue = el.getTextContent();
				}
				ret.put("NodeValue", nodeValue);
			}
		}
		return ret;
	}
	
	/// Map<String,Object>, from XML string. Where the "child" key word is reserved for child nodes
	///
	/// @param  XML string to use
	///
	/// @return   Result map
	public static Map<String, Object> toMap(String xmlStr) {
		return toMap(toElement(xmlStr));
	}
	
	/// Inserts a child map, in accordence to its type
	/// 
	/// @param  The base map to build on
	/// @param  The child node to add to base map
	///
	/// @return  The base map
	@SuppressWarnings("unchecked")
	protected static Map<String, Object> addChildNodeToBaseMap(Map<String, Object> baseMap,
		Map<String, Object> childMap) {
		//
		// Get the type key
		//
		String type = GenericConvert.toString(childMap.get("type"), null);
		if (type == null || type.length() <= 0) {
			type = "child";
		}
		
		//
		// Get existing value associated with the type key
		//
		Object baseMapOri = baseMap.get(type);
		
		//
		// Base map : if blank, assume first node
		//
		if (baseMapOri == null) {
			baseMap.put(type, childMap);
			return baseMap;
		}
		
		//
		// List map to save child nodes to?
		//
		ArrayList<Object> listToSaveTo = null;
		if (baseMapOri instanceof ArrayList) {
			listToSaveTo = (ArrayList<Object>) baseMapOri;
		} else {
			listToSaveTo = new ArrayList<Object>();
			listToSaveTo.add(baseMapOri);
		}
		
		//
		// Saving child map
		//
		listToSaveTo.add(childMap);
		baseMap.put(type, listToSaveTo);
		
		//
		// Return the base map
		//
		return baseMap;
	}
	
	/// Collapse an XML map into a more JSON concise format.
	/// Assuming no key and tag name conflict.
	///
	/// This collapse the items along the key words "ChildNodes", "NodeValue", "TagName"
	///
	/// @param  The Map to collapse
	///
	/// @return  Result map
	public static Map<String, Object> toCollapsedMap(Map<String, Object> inMap) {
		
		//
		// Normalize "NodeValue" as "value"
		//
		String nodeValue = GenericConvert.toString(inMap.get("NodeValue"), null);
		if (nodeValue != null && nodeValue.length() > 0) {
			inMap.put("value", nodeValue);
		}
		inMap.remove("NodeValue");
		
		//
		// Normalize "TagName" as "type"
		//
		String tagName = GenericConvert.toString(inMap.get("TagName"), null);
		if (tagName != null && tagName.length() > 0) {
			inMap.put("type", tagName);
		}
		inMap.remove("TagName");
		
		//
		// Normalize "ChildNodes"
		//
		List<Object> childList = GenericConvert.toList(inMap.get("ChildNodes"), null);
		inMap.remove("ChildNodes");
		if (childList != null && childList.size() > 0) {
			for (int i = 0; i < childList.size(); ++i) {
				// Child node to process
				Map<String, Object> childNode = GenericConvert.toStringMap(childList.get(i));
				
				// Recusion filter
				childNode = toCollapsedMap(childNode);
				
				// Child node adding
				addChildNodeToBaseMap(inMap, childNode);
			}
		}
		
		// Return normalized map
		return inMap;
	}
	
	/// Collapse an XML map into a more JSON concise format.
	/// Assuming no key and tag name conflict.
	///
	/// This collapse the items along the key words "ChildNodes", "NodeValue", "TagName"
	///
	/// @param  The XML String to collapse
	///
	/// @return  Result map
	public static Map<String, Object> toCollapsedMap(String xmlStr) {
		return toCollapsedMap(toMap(xmlStr));
	}
}
