package picoded.page.mte.internal;

import picoded.page.mte.*;
import picoded.struct.*;
import picoded.conv.*;

import java.util.*;

///
/// Takes in the template, build an abstract syntax tree (hahaha). And execute it!
///
/// This works by processing the template through the following process
///
/// 1) Split across string tokens, and bracket tokens
/// 2) Scan the tokens, and reorganize them in accordence to their if/for/while
/// 3) Scan for orphanced /closing blocks (assume its a syntax error)
/// 4) From deepest token first, does a scan for else/elseif
///
public class SyntaxTreeRoot extends SyntaxTreeNode {
	
	//----------------------------------------------------------------
	//
	// Constructor stuff
	//
	//----------------------------------------------------------------
	
	/// Refence to the calling MinimalTemplateEngine
	public final MinimalTemplateEngine parent;
	
	/// The template string / char array used
	public final String templateString;
	public final char[] templateChars;
	
	///
	/// Constructor setup call, done inside MinimalTemplateEngine.
	/// Made avaliable to facilitate function injection unit-testing.
	/// Avoid using this function, otherwise.
	///
	/// @Param  Parent template engine
	/// @Param  Template string to use for the session
	///
	public SyntaxTreeRoot(MinimalTemplateEngine inParent, String inTemplate) {
		super(null, "root", 0);
		parent = inParent;
		templateString = inTemplate;
		templateChars = inTemplate.toCharArray();
	}
	
	//----------------------------------------------------------------
	//
	// Stage by stage processing of template to AST
	//
	//----------------------------------------------------------------
	
	///
	/// Does stage 1 processing : Processing the template into string, and bracket tokens
	///
	public void processStageOne() {
		// Reset child nodes
		childNodes = new ArrayList<SyntaxTreeNode>();
		
		// Get expression configs
		String[][] expressionSet = parent.expressionSet;
		String[] escapeStrings = parent.escapeStrings;
		
		// Start scanning with reuse vars
		int prvPos = 0;
		int pos = 0;
		int end = templateChars.length;
		
		// Iterate the chars
		for (; pos < end; ++pos) {
			
			//
			// Skip escaped characters
			//
			int idx = CharArray.startsWith_returnNeedleIndex(escapeStrings, templateChars, pos, end);
			if (idx >= 0) {
				// Skip the escape string characters
				pos += escapeStrings[idx].length();
				
				// "skip" the character that was escaped
				// pos += 1 - 1; // Minus 1, as loop adds it on continue
				continue; //next
			}
			
			//
			// Scans for escape character block
			//
			for(idx = 0; idx<expressionSet.length; ++idx) {
				int exprStart = CharArray.startsWith_returnOffsetAfterNeedle(expressionSet[idx][0], templateChars, pos, end);
				if (exprStart >= 0) {
					int exprEnd = CharArray.indexOf_skipEscapedCharacters(escapeStrings, expressionSet[idx][1],
						templateChars, pos, end);
					
					if (exprEnd < 0) {
						throw invalidTemplateFormatException(pos, "Missing expected closing bracket: "
							+ expressionSet[idx][1] + "");
					}
					
					//
					// Create text node, if there is characters from prvPos to pos.
					// 
					if(prvPos < pos) {
						SyntaxTreeNode prefixNode = new SyntaxTreeNode(this, "text", prvPos);
						prefixNode.text = templateString.substring(prvPos, pos);
						childNodes.add(prefixNode);
						prvPos = pos;
					}
					
					//
					// Create bracket node
					//
					SyntaxTreeNode expresssionNode = new SyntaxTreeNode(this, "expression", exprStart);
					expresssionNode.storeExpression( templateString.substring(exprStart, exprEnd) );
					expresssionNode.expressionBrackets = expressionSet[idx];
					childNodes.add(expresssionNode);
					
					// Update pos index
					prvPos = exprEnd + expressionSet[idx][1].length();
					pos = prvPos - 1; //Deduct, as continue increment +1 already
					continue;
				}
			}
		}
		
		//
		// Create text node, if there is unprocessed characters from prvPos to pos.
		// 
		if(prvPos < pos) {
			SyntaxTreeNode prefixNode = new SyntaxTreeNode(this, "text", prvPos);
			prefixNode.text = templateString.substring(prvPos, pos);
			childNodes.add(prefixNode);
			//prvPos = pos;
		}
	}
	
	///
	/// Does stage 2 processing : processing bracket tokens, into their block expressions
	///
	public void processStageTwo() {
		List<String> stack = new ArrayList<String>();
		List<SyntaxTreeNode> nodeList = new ArrayList<SyntaxTreeNode>(childNodes);
		childNodes = new ArrayList<SyntaxTreeNode>();
		
		for(int pos = 0; pos < nodeList.size(); ++pos) {
			SyntaxTreeNode node = nodeList.get(pos);
			
			//
			// Expression processing
			//
			if( node.type.equalsIgnoreCase("expression") ) {
				int idx = blockKeyWords.indexOf(node.firstExpressionStatement);
				if( idx >= 0 ) { //Has block keyword
					// String blockWord = blockKeyWords.get(idx);
					// node.type = "block";
					childNodes.add(node);
					// idx = processBlock(node, nodeList, pos+1);
					// 
					// if( idx > pos ) {
					// 	
					// }
				}
			}
			
			//
			// If all else : Appends it to child list
			//
			childNodes.add(node);
		}
	}
	
	// /// Process a node block, and return the node index AFTER the closing block
	// protected int processBlock(SyntaxTreeNode block, List<SyntaxTreeNode> nodeList, int pos) {
	// 	
	// }
	
	//----------------------------------------------------------------
	//
	// Configuration stuff
	//
	//----------------------------------------------------------------
	
	/// Block expression keywords
	protected List<String> blockKeyWords = Arrays.asList(new String[] { "if", "while", "foreach" });
	
	//----------------------------------------------------------------
	//
	// Internal stuff =)
	//
	//----------------------------------------------------------------
	
	/// Throws an error, for invalid parsing
	protected RuntimeException invalidTemplateFormatException(int errorPosition, String errorMessage) {
		String finalMsg = "";
		finalMsg += "\n";
		
		String posPrefix = " ... ";
		String posSuffix = " ... ";
		
		int start = errorPosition - 10;
		int end = errorPosition + 20;
		
		if(start < 0) {
			start = 0;
			posPrefix = "";
		}
		if(end > templateChars.length) {
			end = templateChars.length;
			posSuffix = "";
		}
		
		String prefixSegment = templateString.substring(start, errorPosition);
		String suffixSegment = templateString.substring(errorPosition, end);
		String prefixFull = posPrefix + prefixSegment;
		
		finalMsg += prefixFull + suffixSegment + posSuffix + "\n";
		for(int i=0; i<prefixFull.length(); ++i) {
			finalMsg += " ";
		}
		finalMsg += "^\n";
		finalMsg += errorMessage;
		
		return new RuntimeException(finalMsg);
	}
}
