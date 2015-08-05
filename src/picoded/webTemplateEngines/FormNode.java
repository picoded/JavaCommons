package picoded.webTemplateEngines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import picoded.conv.ConvertJSON;
import picoded.conv.RegexUtils;
import picoded.struct.GenericConvertMap;

//may contain child tags, hence , the list of HtmlNodes
public class FormNode extends HashMap<String, Object> implements GenericConvertMap<String, Object> {
	static final long serialVersionUID = 1L;
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
