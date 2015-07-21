package picoded.webTemplateEngines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import picoded.struct.GenericConvertMap;

/*
 * each node has 
 * type
 * text
 * subnodes (List)
 */



//Represents a single enclosed html tag
//may contain child tags, hence , the list of HtmlNodes
public class FormNode extends HashMap<String, Object> implements GenericConvertMap<String, Object> {
	static final long serialVersionUID = 1L;
	
	private List<FormNode> _children = null;
	
	public FormNode(){
		_children = new ArrayList<FormNode>();
	}
	
	public FormNode(List<FormNode> children){
		this.setChildren(children);
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
}
