package picoded.webTemplateEngines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		_prefilledData = prefilledJSONData;
		
		this.putAll(innerConstructor(mapObject, prefilledJSONData));
	}
	
//	public FormNode(List<FormNode> children){
//		this.setChildren(children);
//	}
	
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
	
	//conversion functions
//	public static List<FormNode> fromString(String jsonString){
//		return null;
//	}
	
//	public static List<FormNode> parse(Map<String, Object> jsonMap){
//		List<FormNode> formNodes = new ArrayList<FormNode>();
//		
//		for(String key:jsonMap.keySet()){
//			Object jsonVal = jsonMap.get(key);
//		}
//		
//		return null;
//	}
	
	public Object getDefaultValue(String fieldName){
		if(_prefilledData.containsKey(fieldName)){
			return _prefilledData.get(fieldName);
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private FormNode innerConstructor(Map<String, Object> mapObject, Map<String, Object> prefilledJSONData){
		FormNode formNode = new FormNode();
		formNode.setPrefilledData(prefilledJSONData);
		formNode.putAll(mapObject); //changed from this to formNode
		
		if(mapObject.containsKey("children")){
			formNode.remove("children");
			
			Object children = mapObject.get("children");
			if(children instanceof List){
				List<Object> childrenList = (List<Object>)children;
				for(Object obj:childrenList){
					if(obj instanceof Map){
						Map<String, Object> childObject = (Map<String, Object>)obj;
						this.addChild(innerConstructor(childObject, prefilledJSONData));
					}
				}
			}
		}
		
		return formNode;
	}
	
//	@SuppressWarnings("unchecked")
//	public static FormNode fromMap(Map<String, Object> jsonMap){
//		FormNode formNode = new FormNode();
//		formNode.putAll(jsonMap);
//		
//		//vomits
//		if(jsonMap.containsKey("children")){
//			formNode.remove("children");
//			
//			Object children = jsonMap.get("children");
//			if(children instanceof List){
//				List<Object> childrenList = (List<Object>)children;
//				for(Object obj:childrenList){
//					if(obj instanceof Map){
//						Map<String, Object> childObject = (Map<String, Object>)obj;
//						formNode.addChild(fromMap(childObject));
//					}
//				}
//			}
//		}
//		
//		return formNode;
//	}
}
