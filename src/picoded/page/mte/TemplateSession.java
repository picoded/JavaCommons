package picoded.page.mte;

import java.util.*;

import picoded.page.mte.internal.*;
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
	public final GenericConvertMap<String,Object> varMap;
	
	///
	/// Constructor setup call, done inside MinimalTemplateEngine.
	/// Made avaliable to facilitate function injection unit-testing.
	/// Avoid using this function, otherwise.
	///
	/// @Param  Parent template engine
	/// @Param  Template string to use for the session
	/// @Param  Session unique var map called - will be merged with parent map
	///
	public TemplateSession(MinimalTemplateEngine inParent, String inTemplate, Map<String,Object> inVarMap) {
		parent = inParent;
		templateString = inTemplate;
		templateChars = inTemplate.toCharArray();
		
		// @TODO : merge and do a layered map with the parent maps
		if(inVarMap != null) {
			// Gets the generic convert map
			varMap = ProxyGenericConvertMap.ensureGenericConvertMap(inVarMap);
		} else {
			// Uses a blank object, so that the code is less error prone
			varMap = new GenericConvertHashMap<String,Object>();
		}
	}
	
	
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
		for(; start < end; ++start) {
			
			//
			// Skip escaped characters
			//
			int idx = CharArray.startsWith_returnNeedleIndex(parent.escapedSet, templateChars, start, end);
			if(idx >= 0) {
				// Add the escaped characters
				ret.append(parent.escapedSet[idx]);
				start += parent.escapedSet[idx].length();
				
				// Add the character that was escaped
				ret.append(templateChars[start]);
				continue; //next
			}
			
			//
			// Scan for unescaped expressionSet
			//
			
			//
			// Scan for expressionSet
			//
			for(idx = 0; idx<parent.expressionSet.length; ++idx) {
				int exprStart = CharArray.startsWith_returnOffsetAfterNeedle(parent.expressionSet[idx][0], templateChars, start, end);
				if(exprStart >= 0) {
					int exprEnd = CharArray.indexOf_skipEscapedCharacters(parent.escapedSet, parent.expressionSet[idx][1], templateChars, start, end);
					
					if(exprEnd < 0) {
						throw invalidTemplateFormatException(templateChars, start, "Missing expected closing brakcet: "+parent.expressionSet[idx][1]+"");
					}
					
					// Get the expression string, remove uneeded spaces?
					String expression = String.valueOf( CharArray.slice(templateChars, exprStart, exprEnd) ).trim();
					
					// Check if its a BLOCK expression set : IF / ELSE / FOR / WHILE
					
					// Assumes either functional expression, 
					
					// OR variable expression
					if(varMap.containsKey(expression)) {
						ret.append(varMap.getString(expression)); 
					}
					
					// Regardless, continue AFTER the expression block
					start = exprEnd + parent.expressionSet[idx][1].length();
					continue;
				}
			}
			
			//
			// No match / action taken, append it to result 
			//
			if(start < end) {
				ret.append(templateChars[start]);
			}
		}
		
		return ret; 
	}
	
	//----------------------------------------------------------------
	//
	// Critical Internal stuff =)
	//
	//----------------------------------------------------------------
	
	// ///
	// /// Scans for expression set, and add processed result to StringBuilder ret if found.
	// ///
	// ///
	// ///
	// protected int scanForExpressionSet(StringBuilder ret, char[] inTemplate, int start, int end, boolean autoEscape, Map<String,Object> varMap) {
	// 	
	// }

	//----------------------------------------------------------------
	//
	// Internal stuff =)
	//
	//----------------------------------------------------------------
	
	/// Throws an error, for invalid parsing
	protected RuntimeException invalidTemplateFormatException(char[] inTemplate, int errorPosition, String errorMessage) {
		return new RuntimeException(errorMessage);
	}
	
}
