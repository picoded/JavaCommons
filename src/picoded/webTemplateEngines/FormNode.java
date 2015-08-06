package picoded.webTemplateEngines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import picoded.conv.ConvertJSON;
import picoded.conv.RegexUtils;
import picoded.conv.GenericConvert;
import picoded.struct.GenericConvertMap;

/// FormNode serves as a map accessor to the form defination structure,
/// with various utility functions, for Wrapper, and Input interface writers
///
/// @TODO Change class extension to use AbstractMapDecorator, so that it proxy the valeus from the soruce instead
public class FormNode extends HashMap<String, Object> implements GenericConvertMap<String, Object> {
    
    // Utility helper functions
    //------------------------------------------------------------------------
    
    /// Helps escape html dom parameter quotes, in an "optimal" way
    ///
    /// @params {String}   val   - The string to be quote escaped
    ///
    /// @returns {String}  - The quote escaped value, with either single or double quotes, or quotes with escaped quotes
    protected static String escapeParameterQuote( String val ) {
        boolean hasSingleQuote = val.contains("\'");
        boolean hasDoubleQuote = val.contains("\"");
        
        if( hasSingleQuote && hasDoubleQuote ) {
        	//No choice, escape double quotes, and use them
        	return "\""+val.replaceAll("\\\\", "\\\\").replaceAll("\"","\\\"")+"\"";
        } else if( hasDoubleQuote ) {
        	return "\'"+val+"\'";
        } else if( hasSingleQuote ) {
        	return "\""+val+"\"";
        } //else { //quoteless, use single quotes
        return "\'"+val+"\'";
    }

    /// Generates a HTML node, with its prefix and suffix. Using its type, and parameters
    ///
    /// @params {String}              nodeType            - HTML DOM type to generate, such as DIV, or INPUT
    /// @params {Map<String,Object>}  parameterMap        - Parameters map to input into the DOM
    /// @params {String}              rawParameterString  - Additional raw parameters to be added (optional)
    ///
    /// @returns {StringBuilder[2]}  - A pair of StringBuilder representing the prefix and suffix nodes
	public static StringBuilder[] htmlNodeGenerator( String nodeType, Map<String,String> parameterMap, String rawParameterString ) {
	    StringBuilder domNode = new StringBuilder("<"+nodeType);
    
		if( parameterMap != null ) {
			for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
				domNode
					.append(" ")
					.append(entry.getKey())
					.append("=")
					.append( escapeParameterQuote( GenericConvert.toString(entry.getValue(), "") ) );
			}
		}

		if( rawParameterString != null && rawParameterString.length() > 0 ) {
			domNode.append(" ").append(rawParameterString);
		}
		
		domNode.append(">");
		
		return new StringBuilder[] { domNode,  new StringBuilder("</"+nodeType+">") };
	}

    
    private Map<String, Object> _prefilledData = null;
	
	private List<FormNode> _children = null;
	
	public FormNode(){
		_children = new ArrayList<FormNode>();
		_prefilledData = new HashMap<String, Object>();
	}
	
	public FormNode(Map<String, Object> mapObject, Map<String, Object> prefilledJSONData){
		_children = new ArrayList<FormNode>();
		FormNode newNode = innerConstructor(mapObject, prefilledJSONData);
		this.putAll(newNode);
		this.setChildren(newNode.children());
	}
	
	@SuppressWarnings("unchecked")
	public static List<FormNode> createFromList(List<Object> listObject, Map<String, Object> prefilledJSONData){
		List<FormNode> formNodes = new ArrayList<FormNode>();
		
		for(Object obj:listObject){
			Map<String, Object> nodeMapObject = (Map<String, Object>)obj;
			formNodes.add(new FormNode(nodeMapObject, prefilledJSONData));
		}
		
		return formNodes;
	}
	
	public static List<FormNode> createFromJSONString(String jsonString, Map<String, Object> prefilledJSONData){
		List<FormNode> formNodes = new ArrayList<FormNode>();
		
		if(jsonString.charAt(0) == '['){
			List<Object> nodeList = ConvertJSON.toList(jsonString);
			return createFromList(nodeList, prefilledJSONData);
		}else{
			Map<String, Object> nodeMap = ConvertJSON.toMap(jsonString);
			FormNode newNode = new FormNode(nodeMap, prefilledJSONData);
			formNodes.add(newNode);
			return formNodes;
		}
	}
	
	public void setPrefilledData(Map<String, Object> prefilledJSONData){
		_prefilledData = prefilledJSONData;
	}
	
	public void setChildren(List<FormNode> children){
		_children = new ArrayList<FormNode>(children); //copy constructor behaviour
	}
	
	public void addChild(FormNode child){
		_children.add(child);
	}
	
	public int childCount(){
		return _children.size();
	}
	
	public List<FormNode> children(){
		return _children;
		//return new ArrayList<HtmlNode>(_children); //possible change so caller doesnt get the actual list object
	}
	
	public Object getDefaultValue(String fieldName){
		if(_prefilledData != null && _prefilledData.containsKey(fieldName)){
			return _prefilledData.get(fieldName);
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private FormNode innerConstructor(Map<String, Object> mapObject, Map<String, Object> prefilledJSONData){
		FormNode formNode = new FormNode();
		formNode.setPrefilledData(prefilledJSONData);
		formNode.putAll(mapObject); //changed from this to formNode
		
		//the commented out code is the "safe" version of the recursion loop
		
//		if(mapObject.containsKey("children")){
//			formNode.remove("children");
//			
//			Object children = mapObject.get("children");
//			if(children instanceof List){
//				List<Object> childrenList = (List<Object>)children;
//				for(Object obj:childrenList){
//					if(obj instanceof Map){
//						Map<String, Object> childObject = (Map<String, Object>)obj;
//						String type = (String)childObject.get("type");
//						formNode.addChild(innerConstructor(childObject, prefilledJSONData));
//					}
//				}
//			}
//		}
		
		if(mapObject.containsKey("children")){
			formNode.remove("children");
			List<Object> children = (List<Object>)mapObject.get("children");
			for(Object child:children){
				Map<String, Object> nodeObj = (Map<String, Object>)child;
				formNode.addChild(innerConstructor(nodeObj, prefilledJSONData));
			}
		}
		
		return formNode;
	}
	
	//helper functions
	public String label(){
		return this.containsKey(JsonKeys.LABEL) ? this.getString(JsonKeys.LABEL) : "";
	}
	
	//return label() lowercased
	public String field(){
		return this.containsKey(JsonKeys.FIELD) ? this.getString(JsonKeys.FIELD) : RegexUtils.removeAllNonAlphaNumeric(this.label()).toLowerCase();
	}
}
