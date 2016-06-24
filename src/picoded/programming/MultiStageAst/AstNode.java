package picoded.programming.MultiStageAst;

import picoded.struct.*;
import picoded.conv.*;

import java.util.*;

///
/// ASTNode 
///
public class AstNode extends GenericConvertHashMap<String,Object> {
	
	//----------------------------------------------------------------
	//
	// Constructor stuff
	//
	//----------------------------------------------------------------
	
	/// The AST type 
	public String type = null;
	
	/// The AstRoot
	public AstRoot root = null;
	
	/// Constructor with type declaration
	public AstNode(AstRoot inRoot, String inType) {
		root = inRoot;
		type = inType;
	}
	
	/// Start and ending character position
	protected int _startPosition = 0;
	protected int _endingPosition = 0;
	
	/// Constructor with type declaration
	public AstNode(AstRoot inRoot, String inType, int inStart, int inEnding) {
		this(inRoot, inType);
		_startPosition = inStart;
		_endingPosition = inEnding;
	}
	
	/// The start position fetching
	public int startPosition() {
		return _startPosition;
	}
	
	/// The ending position
	public int endingPosition() {
		return _endingPosition;
	}
	
	//----------------------------------------------------------------
	//
	// Basic string handling
	//
	//----------------------------------------------------------------
	
	/// Function caching
	protected String _nodeString = null;
	protected char[] _nodeChars = null;
	
	/// Get the node string slice from the root node
	public String nodeString() {
		if(_nodeString == null) {
			_nodeString = root.rootString.substring(_startPosition, _endingPosition);
		}
		return _nodeString;
	}
	
	/// Get the node char array slice from the root node
	public char[] nodeChars() {
		if(_nodeChars == null) {
			_nodeChars = CharArray.slice(root.rootChars, _startPosition, _endingPosition - _startPosition);
		}
		return _nodeChars;
	}
	
	/// Gets and return the length of characters in this node
	public int nodeLength() {
		return _endingPosition - _startPosition;
	}
	
	//----------------------------------------------------------------
	//
	// Child nodes management
	//
	//----------------------------------------------------------------
	
	/// Children nodes
	public List<AstNode> children = new ArrayList<AstNode>();
	
	/// Reset the children nodes list
	public void resetChildren() {
		children = new ArrayList<AstNode>();
	}
	
	/// Add a child node with the relative starting offset
	public void addChildNode(String inType, int relativeOffset) {
		children.add( new AstNode(root, inType, _startPosition+relativeOffset, _endingPosition) );
	}
	
	/// Add a child node with the relative starting offset, and length
	public void addChildNode(String inType, int relativeOffset, int length) {
		children.add( new AstNode(root, inType, _startPosition+relativeOffset, _startPosition+relativeOffset+length) );
	}
	
	//----------------------------------------------------------------
	//
	// Stage map running
	//
	//----------------------------------------------------------------
	
	/// Takes a single stage map, and iterate itself and all child node
	protected void applyStageMap(Map<String,AstNodeProcessor> stageMap) {
		if(stageMap.get(type) != null) {
			stageMap.get(type).accept(this);
		} else if(stageMap.get("*") != null) {
			stageMap.get("*").accept(this);
		}
		
		for(AstNode child : children) {
			child.applyStageMap(stageMap);
		}
	}
	
	//----------------------------------------------------------------
	//
	// Stringify handling
	//
	//----------------------------------------------------------------
	
	public StringBuilder stringify() {
		Map<String, AstNodeStringify> stringifyMap = root.astStringifyMap();
		if(stringifyMap.get(type) != null) {
			return stringifyMap.get(type).apply(this);
		} else if(stringifyMap.get("*") != null) {
			return stringifyMap.get("*").apply(this);
		}
		return new StringBuilder();
	}
	
	public String toString() {
		return stringify().toString();
	}
	
	//----------------------------------------------------------------
	//
	// Node string searching
	//
	//----------------------------------------------------------------
	
	public int node_startsWith(String[] needleSet, int relativeOffset) {
		return CharArray.startsWith(needleSet, root.rootChars, _startPosition+relativeOffset, _endingPosition);
	}
	
	public int node_startsWith(String[] needleSet, int relativeOffset, int length) {
		return CharArray.startsWith(needleSet, root.rootChars, _startPosition+relativeOffset,  _startPosition+relativeOffset+length);
	}
}
