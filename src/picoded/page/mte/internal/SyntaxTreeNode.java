package picoded.page.mte.internal;

import picoded.page.mte.*;

import java.util.*;

///
/// Takes in the template, build a syntax tree (hahaha). And execute it!
///
/// This works by processing the template through the following process
///
/// 1) Split across string tokens, and bracket tokens
/// 2) Scan the tokens, and reorganize them in accordence to their if/elseif/else/for/while
///
public class SyntaxTreeNode {
	
	//----------------------------------------------------------------
	//
	// Constructor stuff
	//
	//----------------------------------------------------------------
	
	/// Refence to the calling MinimalTemplateEngine
	public final SyntaxTreeRoot root;
	
	///
	/// Constructor setup call, done inside SyntaxTreeRoot.
	///
	/// @Param  root node
	///
	public SyntaxTreeNode(SyntaxTreeRoot inRoot, String inType, int inPos) {
		root = inRoot;
		type = inType;
		textPosition = inPos;
	}
	
	//----------------------------------------------------------------
	//
	// Variable tracking
	//
	//----------------------------------------------------------------
	
	/// The character position
	public int textPosition = -1;
	
	/// The node type
	public String type = "text";
	
	/// The text node value (if applicable)
	public String text = null;
	
	/// Expression brackets
	public String[] expressionBrackets = null;
	
	/// Expression statements
	public String[] expressionStatements = null;
	
	/// First expression statement keyword
	public String firstExpressionStatement = null;
	
	/// The child nodes
	public List<SyntaxTreeNode> childNodes = new ArrayList<SyntaxTreeNode>();
	
	/// Closing block node
	public SyntaxTreeNode closingNode = null;
	
	//----------------------------------------------------------------
	//
	// Setup functions
	//
	//----------------------------------------------------------------
	public void storeExpression(String expr) {
		text = expr.trim();
		expressionStatements = text.split("\\s+");
		firstExpressionStatement = expressionStatements[0].trim();
	}
	
	//----------------------------------------------------------------
	//
	// To String conversion
	//
	//----------------------------------------------------------------
	public String toString() {
		StringBuilder ret = new StringBuilder();
		
		if( expressionBrackets != null ) {
			ret.append(expressionBrackets[0]);
		}
		
		if( text != null ) {
			ret.append(text);
		}
		
		if( expressionBrackets != null ) {
			ret.append(expressionBrackets[1]);
		}
		
		for(int i=0; i<childNodes.size(); ++i) {
			ret.append( childNodes.get(i).toString() );
		}
		
		if( closingNode != null ) {
			closingNode.toString();
		}
		
		return ret.toString();
	}
}
