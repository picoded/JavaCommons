package picoded.programming.MultiStageAst;

import picoded.struct.*;
import picoded.conv.*;

import java.util.*;

///
/// Takes in the string, grow out (build) an abstract syntax tree.
///
/// This works by processing the given string through the various stages of AstNodeProcessor
///
public class AstRoot extends AstNode {
	
	//----------------------------------------------------------------
	//
	// Constructor stuff
	//
	//----------------------------------------------------------------
	
	/// The template string / char array used
	public final String rootString;
	public final char[] rootChars;
	
	///
	/// Constructor for in template 
	///
	/// @Param  Template string to use for the session
	///
	public AstRoot(String rawString) {
		// Node setup
		super(null, "root", 0, rawString.length());
		
		// Root specific setup
		root = this;
		rootString = rawString;
		rootChars = rawString.toCharArray();
		
		// Language specific root setup process
		setupRootNode(); 
	}
	
	//----------------------------------------------------------------
	//
	// [TO-Overide] Multistage AST code processing structure
	//
	//----------------------------------------------------------------
	
	/// Root setup, used to setup config within its own hashmap
	public void setupRootNode() {
		// Does nothing for now : override for language implmentation
	}
	
	/// setup the AST processor function stack 
	public List<Map<String,AstNodeProcessor>> setupAstProcessorStages() {
		List<Map<String,AstNodeProcessor>> ret = new ArrayList<Map<String,AstNodeProcessor>>();
		
		return ret;
	}
	
	/// setup the stringify function map
	public Map<String,AstNodeStringify> setupAstStringifyMap() {
		Map<String,AstNodeStringify> ret = new HashMap<String,AstNodeStringify>();
		ret.put("*", AstCommonFunctions.children_stringify);
		return ret;
	}
	
	//----------------------------------------------------------------
	//
	// Multistage AST code processing structure
	//
	//----------------------------------------------------------------
	
	/// Internal cached copy
	protected List<Map<String,AstNodeProcessor>> _astProcessorStages = null;
	
	/// Internal cached copy
	protected Map<String,AstNodeStringify> _astStringifyMap = null;
	
	/// Get the cached setupAstProcessorStages result
	public List<Map<String,AstNodeProcessor>> astProcessorStages() {
		if(_astProcessorStages == null) {
			_astProcessorStages = setupAstProcessorStages();
		}
		return _astProcessorStages;
	}
	
	/// Get the cached setupAstProcessorStages result
	public Map<String,AstNodeStringify> astStringifyMap() {
		if(_astStringifyMap == null) {
			_astStringifyMap = setupAstStringifyMap();
		}
		return _astStringifyMap;
	}
	
	//----------------------------------------------------------------
	//
	// Stage by stage processing of template to AST
	//
	//----------------------------------------------------------------
	
	/// Parses a single selected stage
	public void applySingleStage(int stageNumber) {
		Map<String,AstNodeProcessor> stageMap = astProcessorStages().get(stageNumber);
		if(stageMap != null) {
			applyStageMap(stageMap);
		}
	}
	
	
	// 
	// ///
	// /// Does stage 1 processing : Processing the template into string, and bracket tokens
	// ///
	// public void processStageOne() {
	// 	// Reset child nodes
	// 	childNodes = new ArrayList<SyntaxTreeNode>();
	// 	
	// 	// Get expression configs
	// 	String[][] expressionSet = parent.expressionSet;
	// 	String[] escapeStrings = parent.escapeStrings;
	// 	
	// 	// Start scanning with reuse vars
	// 	int prvPos = 0;
	// 	int pos = 0;
	// 	int end = rootChars.length;
	// 	
	// 	// Iterate the chars
	// 	for (; pos < end; ++pos) {
	// 		
	// 		//
	// 		// Skip escaped characters
	// 		//
	// 		int idx = CharArray.startsWith(escapeStrings, rootChars, pos, end);
	// 		if (idx >= 0) {
	// 			// Skip the escape string characters
	// 			pos += escapeStrings[idx].length();
	// 			
	// 			// "skip" the character that was escaped
	// 			// pos += 1 - 1; // Minus 1, as loop adds it on continue
	// 			continue; //next
	// 		}
	// 		
	// 		//
	// 		// Scans for escape character block
	// 		//
	// 		for(idx = 0; idx<expressionSet.length; ++idx) {
	// 			int exprStart = CharArray.startsWith_returnOffsetAfterNeedle(expressionSet[idx][0], rootChars, pos, end);
	// 			if (exprStart >= 0) {
	// 				int exprEnd = CharArray.indexOf_skipEscapedCharacters(escapeStrings, expressionSet[idx][1],
	// 					rootChars, pos, end);
	// 				
	// 				if (exprEnd < 0) {
	// 					throw invalidTemplateFormatException(pos, "Missing expected closing bracket: "
	// 						+ expressionSet[idx][1] + "");
	// 				}
	// 				
	// 				//
	// 				// Create text node, if there is characters from prvPos to pos.
	// 				// 
	// 				if(prvPos < pos) {
	// 					SyntaxTreeNode prefixNode = new SyntaxTreeNode(this, "text", prvPos);
	// 					prefixNode.text = rootString.substring(prvPos, pos);
	// 					childNodes.add(prefixNode);
	// 					prvPos = pos;
	// 				}
	// 				
	// 				//
	// 				// Create bracket node
	// 				//
	// 				SyntaxTreeNode expresssionNode = new SyntaxTreeNode(this, "expression", exprStart);
	// 				expresssionNode.storeExpression( rootString.substring(exprStart, exprEnd) );
	// 				expresssionNode.expressionBrackets = expressionSet[idx];
	// 				childNodes.add(expresssionNode);
	// 				
	// 				// Update pos index
	// 				prvPos = exprEnd + expressionSet[idx][1].length();
	// 				pos = prvPos - 1; //Deduct, as continue increment +1 already
	// 				continue;
	// 			}
	// 		}
	// 	}
	// 	
	// 	//
	// 	// Create text node, if there is unprocessed characters from prvPos to pos.
	// 	// 
	// 	if(prvPos < pos) {
	// 		SyntaxTreeNode prefixNode = new SyntaxTreeNode(this, "text", prvPos);
	// 		prefixNode.text = rootString.substring(prvPos, pos);
	// 		childNodes.add(prefixNode);
	// 		//prvPos = pos;
	// 	}
	// }
	// 
	// ///
	// /// Does stage 2 processing : processing bracket tokens, into their block expressions
	// ///
	// public void processStageTwo() {
	// 	List<String> stack = new ArrayList<String>();
	// 	List<SyntaxTreeNode> nodeList = new ArrayList<SyntaxTreeNode>(childNodes);
	// 	childNodes = new ArrayList<SyntaxTreeNode>();
	// 	
	// 	for(int pos = 0; pos < nodeList.size(); ++pos) {
	// 		SyntaxTreeNode node = nodeList.get(pos);
	// 		
	// 		//
	// 		// Expression processing
	// 		//
	// 		if( node.type.equalsIgnoreCase("expression") ) {
	// 			int idx = blockKeyWords.indexOf(node.firstExpressionStatement);
	// 			if( idx >= 0 ) { //Has block keyword
	// 				// String blockWord = blockKeyWords.get(idx);
	// 				// node.type = "block";
	// 				childNodes.add(node);
	// 				// idx = processBlock(node, nodeList, pos+1);
	// 				// 
	// 				// if( idx > pos ) {
	// 				// 	
	// 				// }
	// 			}
	// 		}
	// 		
	// 		//
	// 		// If all else : Appends it to child list
	// 		//
	// 		childNodes.add(node);
	// 	}
	// }
	// 
	// // /// Process a node block, and return the node index AFTER the closing block
	// // protected int processBlock(SyntaxTreeNode block, List<SyntaxTreeNode> nodeList, int pos) {
	// // 	
	// // }
	// 
	// //----------------------------------------------------------------
	// //
	// // Configuration stuff
	// //
	// //----------------------------------------------------------------
	// 
	// /// Block expression keywords
	// protected List<String> blockKeyWords = Arrays.asList(new String[] { "if", "while", "foreach" });
	// 
	// //----------------------------------------------------------------
	// //
	// // Internal stuff =)
	// //
	// //----------------------------------------------------------------
	// 
	// /// Throws an error, for invalid parsing
	// protected RuntimeException invalidTemplateFormatException(int errorPosition, String errorMessage) {
	// 	String finalMsg = "";
	// 	finalMsg += "\n";
	// 	
	// 	String posPrefix = " ... ";
	// 	String posSuffix = " ... ";
	// 	
	// 	int start = errorPosition - 10;
	// 	int end = errorPosition + 20;
	// 	
	// 	if(start < 0) {
	// 		start = 0;
	// 		posPrefix = "";
	// 	}
	// 	if(end > rootChars.length) {
	// 		end = rootChars.length;
	// 		posSuffix = "";
	// 	}
	// 	
	// 	String prefixSegment = rootString.substring(start, errorPosition);
	// 	String suffixSegment = rootString.substring(errorPosition, end);
	// 	String prefixFull = posPrefix + prefixSegment;
	// 	
	// 	finalMsg += prefixFull + suffixSegment + posSuffix + "\n";
	// 	for(int i=0; i<prefixFull.length(); ++i) {
	// 		finalMsg += " ";
	// 	}
	// 	finalMsg += "^\n";
	// 	finalMsg += errorMessage;
	// 	
	// 	return new RuntimeException(finalMsg);
	// }
}
