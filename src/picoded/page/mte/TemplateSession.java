package picoded.page.mte;

import java.util.*;

//import picoded.page.mte.internal.*;
import picoded.struct.*;
import picoded.conv.*;

///
/// A TemplateSession is initiated internally by MinimalTemplateEngine, and refers to an instance 
/// where a single call to MinimalTemplateEngine.parseTemplate or similar functions is called. 
///
/// This is made avaliable for those who wish to create, and attach custom template funciton calls
///
public class TemplateSession {
	
	/// Refence to the calling MinimalTemplateEngine
	public final MinimalTemplateEngine parent;
	
	/// The template string / char array used
	public final String templateString;
	public final char[] templateChars;
	
	/// Variable map used
	public final GenericConvertMap<String, Object> modelMap;
	
	///
	/// Constructor setup call, done inside MinimalTemplateEngine.
	/// Made avaliable to facilitate function injection unit-testing.
	/// Avoid using this function, otherwise.
	///
	/// @Param  Parent template engine
	/// @Param  Template string to use for the session
	/// @Param  Session unique var map called - will be merged with parent map
	///
	public TemplateSession(MinimalTemplateEngine inParent, String inTemplate, Map<String, Object> inModelMap) {
		parent = inParent;
		templateString = inTemplate;
		templateChars = inTemplate.toCharArray();
		
		// @TODO : merge and do a layered map with the parent maps
		if (inModelMap != null) {
			// Gets the generic convert map
			modelMap = ProxyGenericConvertMap.ensureGenericConvertMap(inModelMap);
		} else {
			// Uses a blank object, so that the code is less error prone
			modelMap = new GenericConvertHashMap<String, Object>();
		}
	}
	
	///
	/// Utility function to perform HTML character escaping. Uses: StringEscape.escapeHtml4
	///
	/// @param  The raw string to escape
	///
	/// @return  The escaped string
	///
	public String escapeHtml(String inStr) {
		return StringEscape.escapeHtml4(inStr);
	}
	
	//----------------------------------------------------------------
	//
	// Critical Internal stuff =)
	//
	//----------------------------------------------------------------
	
	///
	/// The internal recursive raw templating function
	///
	/// The decision to use char array internally was based on the following stack overflow answer
	/// http://stackoverflow.com/questions/8894258/fastest-way-to-iterate-over-all-the-chars-in-a-string
	///
	/// As templates had a very high chance of being beyond 256 characters. The internal core function is 
	/// designed to primarily use char[] in its scan and parse.
	///
	/// @param  The StringBuilder to return
	/// @param  Starting char position to scan from
	/// @param  Ending char position to scan
	///
	/// @returns  StringBuilder containing the final output
	///
	protected StringBuilder parseRaw(StringBuilder ret, int start, int end) {
		// Iterate the chars
		for (; start < end; ++start) {
			
			//
			// Skip escaped characters
			//
			int idx = CharArray.startsWith(parent.escapeStrings, templateChars, start, end);
			if (idx >= 0) {
				// Add the escaped characters
				ret.append(parent.escapeStrings[idx]);
				start += parent.escapeStrings[idx].length();
				
				// Add the character that was escaped
				ret.append(templateChars[start]);
				continue; //next
			}
			
			//
			// Scan for unescaped expressionSet
			//
			idx = evaluateIfExpressionSet(parent.unescapedExpressionSet, ret, start, end, false);
			if (idx >= 0) {
				// -1 offset added, as the continue call does an additional +1
				start = idx - 1;
				continue;
			}
			
			//
			// Scan for expressionSet
			//
			idx = evaluateIfExpressionSet(parent.expressionSet, ret, start, end, true);
			if (idx >= 0) {
				// -1 offset added, as the continue call does an additional +1
				start = idx - 1;
				continue;
			}
			
			//
			// No match / action taken, append it to result 
			//
			if (start < end) {
				ret.append(templateChars[start]);
			}
		}
		
		return ret;
	}
	
	///
	/// Fetches and return the expression string array, if an expression set is present
	///
	/// @returns [ [rawStatement, fullStatement, prefix, suffix], [statments...] ]
	///
	protected String[][] fetchStatementSet(String[][] expressionSet, int start, int end) {
		for (int idx = 0; idx < expressionSet.length; ++idx) {
			// int exprStart = CharArray.startsWith_returnOffsetAfterNeedle(expressionSet[idx][0], templateChars, start, end);
			// if (exprStart >= 0) {
			// 	int exprEnd = CharArray.indexOf_skipEscapedCharacters(parent.escapeStrings, expressionSet[idx][1],
			// 		templateChars, start, end);
			// 	
			// 	if (exprEnd < 0) {
			// 		throw invalidTemplateFormatException(start, "Missing expected closing bracket: "
			// 			+ expressionSet[idx][1] + "");
			// 	}
			// 	
			// 	// Get the expression string, remove uneeded spaces and return it statements
			// 	String rawStatement = templateString.substring(start, exprEnd+expressionSet[idx][1].length());
			// 	String fullStatement = templateString.substring(exprStart, exprEnd).trim();
			// 	
			// 	return new String[][] {
			// 		new String[] { rawStatement, fullStatement, expressionSet[idx][0], expressionSet[idx][1] },
			// 		fullStatement.split("\\s+")
			// 	};
			// }
		}
		return null; //nothing fouund
	}
	
	///
	/// Does the expression set scanning, execute it, push to return StringBuilder, 
	/// and does automatic HTML escaping for variable substitution if found.
	///
	protected int evaluateIfExpressionSet(String[][] expressionSet, StringBuilder ret, int start, int end,
		boolean autoEscape) {
		String[][] statementSet = fetchStatementSet(expressionSet, start, end);
		if(statementSet == null) {
			return -1;
		}
		
		// VALID STATEMENT FOUND?
		//------------------------------------------------------------------------
		String[] statements = statementSet[1];
		String functionString = statements[0];
		
		int postExpressionOffset = start + statementSet[0][0].length();
		
		// Check if its a BLOCK expression set : IF / FOR
		//------------------------------------------------------------------------
		
		// IF expression block
		if( functionString.equalsIgnoreCase("if") ) {
			int closingBlock = scanForClosingBlock("if", postExpressionOffset, end, false);
			
			if( closingBlock < 0 ) {
				throw invalidTemplateFormatException(start, "Missing expected if closing block: "+
				statementSet[0][2]+"/if"+statementSet[0][3]+" OR "+statementSet[0][2]+"end"+statementSet[0][3]+" OR "+statementSet[0][2]+"else"+statementSet[0][3]);
			}
			
			int elseBlock = scanForClosingBlock("if", postExpressionOffset, end, true);
			
			
		}
		
		// Checks for functional expression
		//------------------------------------------------------------------------
		
		// Fallsback variable expression (using fullStatement)
		//------------------------------------------------------------------------
		if(ret != null) {
			ret.append(evaluateVariableExpression(/*fullStatement*/ statementSet[0][1], autoEscape));
		}
		
		// Regardless, continue AFTER the expression block
		return postExpressionOffset;
	}
	
	///
	/// Scans for a closing block. This assumes the given start position is AFTER the initial expression block
	/// This pretty much just scan for statements, ignoring its output processing
	///
	protected int scanForClosingBlock(String blockType, int start, int end, boolean acceptElseBlock) {
		// Iterate the chars
		for (; start < end; ++start) {
			
			//
			// Skip escaped characters
			//
			int idx = CharArray.startsWith(parent.escapeStrings, templateChars, start, end);
			if (idx >= 0) {
				// Skipped the escaped chars
				start += parent.escapeStrings[idx].length();
				continue; //next
			}
			
			// Fetch the statement set (if found)
			//------------------------------------------------------------------------
			String[][] statementSet = fetchStatementSet(parent.expressionSet, start, end);
			
			// No statment, next!
			if( statementSet == null ) {
				continue;
			}
			
			// VALID STATEMENT FOUND?
			//------------------------------------------------------------------------
			String[] statements = statementSet[1];
			String functionString = statements[0];
			
			// RECURSION ? (get into next statement nesting)
			
			// ELSE block found???
			if( acceptElseBlock && functionString.equalsIgnoreCase("else") ) {
				return start;
			}
			
			// Closing statement block found???
			for( idx = 0; idx < parent.closingBlockStatements.length; ++idx ) {
				if( functionString.equalsIgnoreCase(parent.closingBlockStatements[idx]) ) {
					return start;
				}
			}
			
			// Closing statement block string + type found???
			for( idx = 0; idx < parent.closingBlockStrings.length; ++idx ) {
				if( functionString.equalsIgnoreCase(parent.closingBlockStrings[idx]+blockType) ) {
					return start;
				}
			}
		}
		
		return -1;
	}
	
	///
	/// Assumes a variable expression, try to fetch it =)
	///
	protected String evaluateVariableExpression(String expression, boolean autoEscape) {
		// Fetch the value
		Object value = modelMap.getNestedObject(expression);
		String strValue = GenericConvert.toString(value, "");
		
		if(autoEscape) {
			return escapeHtml( strValue );
		} else { 
			return strValue;
		}
	}
	
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
