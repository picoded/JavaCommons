package picoded.programming.MultiStageAst;

import picoded.struct.*;
import picoded.conv.*;

import java.util.*;

///
/// ASTNode 
///
public class AstNode extends GenericConvertHashMap<String, Object> {
	
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
		if (_nodeString == null) {
			_nodeString = root.rootString.substring(_startPosition, _endingPosition);
		}
		return _nodeString;
	}
	
	/// Get the node char array slice from the root node
	public char[] nodeChars() {
		if (_nodeChars == null) {
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
	
	/// Previously added child node
	public AstNode addedChildNode = null;
	
	/// Reset the children nodes list
	public void resetChildren() {
		addedChildNode = null;
		children = new ArrayList<AstNode>();
	}
	
	/// Add a child node with the relative starting offset
	public void addChildNode(String inType, int relativeOffset) {
		children.add(addedChildNode = new AstNode(root, inType, _startPosition + relativeOffset, _endingPosition));
	}
	
	/// Add a child node with the relative starting offset, and length
	public void addChildNode(String inType, int relativeOffset, int length) {
		children.add(addedChildNode = new AstNode(root, inType, _startPosition + relativeOffset, _startPosition
			+ relativeOffset + length));
	}
	
	//----------------------------------------------------------------
	//
	// Stage map running
	//
	//----------------------------------------------------------------
	
	/// Takes a single stage map, and iterate itself and all child node
	protected void applyStageMap(Map<String, AstNodeProcessor> stageMap) {
		if (stageMap.get(type) != null) {
			stageMap.get(type).accept(this);
		} else if (stageMap.get("*") != null) {
			stageMap.get("*").accept(this);
		}
		
		for (AstNode child : children) {
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
		if (stringifyMap.get(type) != null) {
			return stringifyMap.get(type).apply(this);
		} else if (stringifyMap.get("*") != null) {
			return stringifyMap.get("*").apply(this);
		}
		return new StringBuilder();
	}
	
	public String toString() {
		return stringify().toString();
	}
	
	//----------------------------------------------------------------
	//
	// Common config variables : Used in various processors
	//
	//----------------------------------------------------------------
	
	// String prefix and suffix, used in echo_stringify
	public String prefix = null;
	public String suffix = null;
	
	// Mode string flag
	public String mode = null;
	
	//----------------------------------------------------------------
	//
	// Node string searching
	//
	//----------------------------------------------------------------
	
	public boolean startsWith(String needle, int relativeOffset) {
		return CharArray.startsWith(needle, root.rootChars, _startPosition + relativeOffset, _endingPosition);
	}
	
	public boolean startsWith(String needle, int relativeOffset, int length) {
		return CharArray.startsWith(needle, root.rootChars, _startPosition + relativeOffset, _startPosition
			+ relativeOffset + length);
	}
	
	public int startsWith(String[] needleArray, int relativeOffset) {
		return CharArray.startsWith(needleArray, root.rootChars, _startPosition + relativeOffset, _endingPosition);
	}
	
	public int startsWith(String[] needleArray, int relativeOffset, int length) {
		return CharArray.startsWith(needleArray, root.rootChars, _startPosition + relativeOffset, _startPosition
			+ relativeOffset + length);
	}
	
	public int startsWith(String[][] needleSet, int nestedPos, int relativeOffset) {
		return CharArray.startsWith(needleSet, nestedPos, root.rootChars, _startPosition + relativeOffset,
			_endingPosition);
	}
	
	public int startsWith(String[][] needleSet, int nestedPos, int relativeOffset, int length) {
		return CharArray.startsWith(needleSet, nestedPos, root.rootChars, _startPosition + relativeOffset, _startPosition
			+ relativeOffset + length);
	}
	
	public int indexOf(String needle, int relativeOffset, int length) {
		int res = CharArray.indexOf(needle, root.rootChars, _startPosition + relativeOffset, _startPosition
			+ relativeOffset + length);
		if (res >= _startPosition) {
			return res - _startPosition;
		}
		return -1;
	}
	
	public int indexOf(String needle, int relativeOffset) {
		int res = CharArray.indexOf(needle, root.rootChars, _startPosition + relativeOffset, _endingPosition);
		if (res >= _startPosition) {
			return res - _startPosition;
		}
		return -1;
	}
	
	public int indexOf_skipEscapedCharacters(String[] escapeStrings, String needle, int relativeOffset, int length) {
		int res = CharArray.indexOf_skipEscapedCharacters(escapeStrings, needle, root.rootChars, _startPosition
			+ relativeOffset, _startPosition + relativeOffset + length);
		if (res >= _startPosition) {
			return res - _startPosition;
		}
		return -1;
	}
	
	public int indexOf_skipEscapedCharacters(String[] escapeStrings, String needle, int relativeOffset) {
		int res = CharArray.indexOf_skipEscapedCharacters(escapeStrings, needle, root.rootChars, _startPosition
			+ relativeOffset, _endingPosition);
		if (res >= _startPosition) {
			return res - _startPosition;
		}
		return -1;
	}
	
	//----------------------------------------------------------------
	//
	// Creates a SyntaxException, to throw
	//
	//----------------------------------------------------------------
	
	public AstSyntaxException setupSyntaxException(int errorRelativeOffset, String errorMessage) {
		String finalMsg = "";
		finalMsg += "\n";
		
		String posPrefix = " ... ";
		String posSuffix = " ... ";
		
		int errorPosition = _startPosition + errorRelativeOffset;
		int start = errorPosition - 10;
		int end = errorPosition + 20;
		
		if (start < 0) {
			start = 0;
			posPrefix = "";
		}
		if (end > root.rootString.length()) {
			end = root.rootString.length();
			posSuffix = "";
		}
		
		String prefixSegment = root.rootString.substring(start, errorPosition);
		String suffixSegment = root.rootString.substring(errorPosition, end);
		String prefixFull = posPrefix + prefixSegment;
		
		finalMsg += prefixFull + suffixSegment + posSuffix + "\n";
		for (int i = 0; i < prefixFull.length(); ++i) {
			finalMsg += " ";
		}
		finalMsg += "^\n";
		finalMsg += errorMessage;
		
		return new AstSyntaxException(finalMsg);
	}
}
