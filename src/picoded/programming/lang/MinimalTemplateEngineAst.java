package picoded.programming.lang;

import picoded.programming.MultiStageAst.*;
import picoded.struct.*;
import picoded.conv.*;

import java.util.*;


///
/// Takes in the string, grow out (build) an abstract syntax tree.
///
/// This works by processing the given string through the various stages of AstNodeProcessor
///
public class MinimalTemplateEngineAst extends AstRoot { 
	
	//----------------------------------------------------------------
	//
	// Constructor setup
	//
	//----------------------------------------------------------------
	
	// Inheritance passing
	public MinimalTemplateEngineAst(String in) {
		super(in);
	}
	
	//----------------------------------------------------------------
	//
	// Unlikely to need modification configuration variables.
	//
	// Like seriously, this is here for those who want to extend,
	// this template engine for other crazy patterns.
	//
	//----------------------------------------------------------------
	
	///
	/// Template expression prefix / suffix set
	///
	/// By default this would be the JMTE, and Mustache expression blocks.
	///
	/// As followed (in JSON): [[ "${","}" ],[ "{{#","}}" ],[ "{{","}}" ],[ "{{{","}}}" ]]
	///
	/// (PS: You probably DO NOT need to modify this)
	///
	public String[][] expressionSet = new String[][] { new String[] { "${", "}" }, new String[] { "{{#", "}}" }, new String[] { "{{", "}}" }, new String[] { "{{{","}}}" } };
	
	///
	/// Template expresion prefix / suffix set for unescaped html strictly, this MUST be a subset of expressionSet
	///
	/// This is a Mustache exclusive, and is checked first (before expressionSet).
	///
	/// As followed (in JSON): [[ "{{{","}}}" ]]
	///
	/// (PS: You probably DO NOT need to modify this)
	///
	public String[][] unescapedExpressionSet = new String[][] { new String[] { "{{{", "}}}" } };
	
	///
	/// Escaped characters set
	/// 
	/// This allows the display of all those Mustache's if its intentional
	///
	/// As followed (in JSON): [ "\" ]
	///
	/// (PS: You probably DO NOT need to modify this)
	///
	public String[] escapeStrings = new String[] { "\\" };
	
	///
	/// Closing block statment
	///
	/// As followed (in JSON): [ "end" ]
	///
	/// (PS: You probably DO NOT need to modify this)
	///
	public String[] closingBlockStatements = new String[] { "end" };
	
	///
	/// Closing block statment indicator
	///
	/// As followed (in JSON): [ "/" ]
	///
	/// (PS: You probably DO NOT need to modify this)
	///
	public String[] closingBlockStrings = new String[] { "/" };
	
	//----------------------------------------------------------------
	//
	// Configuration setup
	//
	//----------------------------------------------------------------
	
	// Language specific constructor
	public void setupRootNode() {
		
	}
	
	//----------------------------------------------------------------
	//
	// Stage procesing layers
	//
	//----------------------------------------------------------------
	
	/// setup the AST processor function stack 
	public List<Map<String,AstNodeProcessor>> setupAstProcessorStages() {
		List<Map<String,AstNodeProcessor>> ret = new ArrayList<Map<String,AstNodeProcessor>>();
		
		HashMap<String,AstNodeProcessor> stage0 = new HashMap<String,AstNodeProcessor>();
		stage0.put("root", stage0_root);
		
		ret.add(stage0);
		
		return ret;
	}
	
	/// setup the stringify function map
	public Map<String,AstNodeStringify> setupAstStringifyMap() {
		Map<String,AstNodeStringify> ret = new HashMap<String,AstNodeStringify>();
		ret.put("*", AstCommonFunctions.children_stringify);
		ret.put("text", AstCommonFunctions.echo_stringify);
		return ret;
	}
	
	//----------------------------------------------------------------
	//
	// Stage procesing functions
	//
	//----------------------------------------------------------------
	
	/// Initial root node processing
	public AstNodeProcessor stage0_root = (root) -> {
		// Reset child nodes
		root.resetChildren();
		
		// Start scanning with reuse vars
		int pos = 0;
		int prvPos = 0;
		int end = root.nodeLength();
		
		// Iterate the chars
		for (; pos < end; ++pos) {
			
			//
			// Skip escaped characters
			//
			int idx = root.node_startsWith(escapeStrings, pos);
			if (idx >= 0) { 
				// Skip the escape string characters
				pos += escapeStrings[idx].length();
				
				// "skip" the character that was escaped
				// pos += 1 - 1; // Minus 1, as loop adds it on continue
				continue; //next
			}
		// 	
		// 	//
		// 	// Scans for escape character block
		// 	//
		// 	for(idx = 0; idx<expressionSet.length; ++idx) {
		// 		int exprStart = CharArray.startsWith_returnOffsetAfterNeedle(expressionSet[idx][0], templateChars, pos, end);
		// 		if (exprStart >= 0) {
		// 			int exprEnd = CharArray.indexOf_skipEscapedCharacters(escapeStrings, expressionSet[idx][1],
		// 				templateChars, pos, end);
		// 			
		// 			if (exprEnd < 0) {
		// 				throw invalidTemplateFormatException(pos, "Missing expected closing bracket: "
		// 					+ expressionSet[idx][1] + "");
		// 			}
		// 			
		// 			//
		// 			// Create text node, if there is characters from prvPos to pos.
		// 			// 
		// 			if(prvPos < pos) {
		// 				SyntaxTreeNode prefixNode = new SyntaxTreeNode(this, "text", prvPos);
		// 				prefixNode.text = templateString.substring(prvPos, pos);
		// 				childNodes.add(prefixNode);
		// 				prvPos = pos;
		// 			}
		// 			
		// 			//
		// 			// Create bracket node
		// 			//
		// 			SyntaxTreeNode expresssionNode = new SyntaxTreeNode(this, "expression", exprStart);
		// 			expresssionNode.storeExpression( templateString.substring(exprStart, exprEnd) );
		// 			expresssionNode.expressionBrackets = expressionSet[idx];
		// 			childNodes.add(expresssionNode);
		// 			
		// 			// Update pos index
		// 			prvPos = exprEnd + expressionSet[idx][1].length();
		// 			pos = prvPos - 1; //Deduct, as continue increment +1 already
		// 			continue;
		// 		}
		// 	}
		}
		 
		//
		// Create text node, if there is unprocessed characters from prvPos to pos.
		// 
		if(prvPos < pos) {
			root.addChildNode("text",prvPos);
		}
	};
}
