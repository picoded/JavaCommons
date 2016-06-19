package picoded.page.mte;

import java.util.*;

import picoded.page.mte.internal.*;
import picoded.struct.*;
import picoded.conv.*;

///
/// A (somewhat) minimilistic, highly extensible markup engine package.
///
/// This is designed to be backwards compatible in templating (not code) with JMTE / Mustache Engine.
/// As we were already using JMTE in production.
///
/// And I really (really) wanted to add certain features, and fix several things we found annoying
///
/// The downside, is this library was not designed for breakneck blazing speed. Its goal is simply to be fast enough.
/// and that it would be exteremly easy to extend and use.
///
/// Besides in our use case: this is used for one time building process. So 100ms, to 1000ms does not matter much.
/// That being said, I will be extremly glad if someone were to do benchmakrs and tweak this =)
///
/// The following highlights the key features (which isnt much)
///
/// + Variable substitution with(out) HTML safe encoding, with silent fallback
/// + Escape character ignoring (for when you really need to show the {{}} blocks)
/// + IF / ELSE block
/// + FOREACH / WHLIE iteration blcok
/// + Custom function blocks (its what builds the IF/ELSE blocks)
///
public class MinimalTemplateEngine {
	
	//----------------------------------------------------------------
	//
	// Constructor(s), what do i say. It builds things
	//
	//----------------------------------------------------------------
	
	/// Blank constructor
	public MinimalTemplateEngine() { }
	
	//----------------------------------------------------------------
	//
	// The main template functions : What your probably here for.
	//
	//----------------------------------------------------------------
	
	/// 
	/// Takes in template, add variable map. Poof magic! 
	/// 
	/// @param  The Minimal Template Engine markup string
	/// @param  The variable map to pull subsitute values from
	/// 
	public String parseTemplate(String inTemplate, Map<String,Object> varMap) {
		return parseTemplate(inTemplate.toCharArray(), varMap);
	}
	
	/// 
	/// Takes in template, add variable map. Poof magic! 
	/// 
	/// @param  The Minimal Template Engine markup string
	/// @param  The variable map to pull subsitute values from
	/// 
	public String parseTemplate(char[] inTemplate, Map<String,Object> varMap) {
		return parseTemplate_raw(new StringBuilder(), inTemplate, 0, inTemplate.length, varMap).toString();
	}
	
	
	///
	/// The internal raw templating function
	///
	/// This is without the various preperation step for the LayeredMap,
	/// or function blocks map. This function recursively as it processes a single char[] array.
	///
	/// The decision to use char array was based on the following stack overflow answer
	/// http://stackoverflow.com/questions/8894258/fastest-way-to-iterate-over-all-the-chars-in-a-string
	///
	/// As templates had a very high chance of being beyond 256 characters. The internal core function is 
	/// designed to primarily use char[] in its scan and parse.
	///
	/// @param  The template char array, to recursively scan on.
	/// @param  Starting char to scan
	/// @param  Ending char to scan
	/// @param  Variable map to use
	///
	/// @returns  StringBuilder containing the final output
	///
	protected StringBuilder parseTemplate_raw(StringBuilder ret, char[] inTemplate, int start, int end, Map<String,Object> varMap) {
		
		GenericConvertMap<String,Object> genericVarMap = null; 
		if(varMap != null) {
			genericVarMap = ProxyGenericConvertMap.ensureGenericConvertMap(varMap);
		}
		
		// Some vars we need to track
		// Iterate the chars
		for(; start < end; ++start) {
			
			// Skip escaped characters
			int idx = CharArray.startsWith_returnNeedleIndex(escapedSet, inTemplate, start, end);
			if(idx >= 0) {
				// Add the escaped characters
				ret.append(escapedSet[idx]);
				start += escapedSet[idx].length();
				
				// Add the character after the escaped chars
				ret.append(inTemplate[start]);
				continue; //next
			}
			
			// Scan for unescapedExpressionSet
			
			// Scan for expressionSet
			for(idx = 0; idx<expressionSet.length; ++idx) {
				int exprStart = CharArray.startsWith_returnOffsetAfterNeedle(expressionSet[idx][0], inTemplate, start, end);
				if(exprStart >= 0) {
					int exprEnd = CharArray.indexOf_skipEscapedCharacters(escapedSet, expressionSet[idx][1], inTemplate, start, end);
					
					if(exprEnd < 0) {
						throw invalidTemplateFormatException(inTemplate, start, "Missing expected closing brakcet: "+expressionSet[idx][1]+"");
					}
					
					// Get the expression string, remove uneeded spaces?
					String expression = String.valueOf( CharArray.slice(inTemplate, exprStart, exprEnd) ).trim();
					
					// Check if its a BLOCK expression set : IF / ELSE / FOR / WHILE
					
					// Assumes either functional expression, 
					
					// OR variable expression
					if(genericVarMap != null) {
						if(genericVarMap.containsKey(expression)) {
							ret.append(genericVarMap.getString(expression)); 
						}
					}
					
					// Regardless, continue AFTER the expression block
					start = exprEnd + expressionSet[idx][1].length();
				}
			}
			
			// No match / action taken, append it to result 
			ret.append(inTemplate[start]);
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
	/// As followed (in JSON): [[ "${","}" ],[ "{{","}}" ]]
	///
	/// (PS: You probably DO NOT need to modify this)
	///
	protected String[][] expressionSet = new String[][] { new String[] { "${", "}" }, new String[] { "{{", "}}"} };
	
	///
	/// Template expresion prefix / suffix set for unescaped html
	///
	/// This is a Mustache exclusive, and is checked first.
	///
	/// As followed (in JSON): [[ "{{{","}}}" ]]
	///
	/// (PS: You probably DO NOT need to modify this)
	///
	protected String[][] unescapedExpressionSet = new String[][] { new String[] { "{{{","}}}" } };
	
	///
	/// Escaped characters set
	/// 
	/// This allows the display of all those Mustache's if its intentional
	///
	/// As followed (in JSON): [ "\" ]
	///
	/// (PS: You probably DO NOT need to modify this)
	///
	protected String[] escapedSet = new String[] { "\\" };
	
	//----------------------------------------------------------------
	//
	// Utility functions, 
	// may get extracted out to external class in future
	//
	//----------------------------------------------------------------
	
	
	//----------------------------------------------------------------
	//
	// Internal stuff =)
	//
	//----------------------------------------------------------------
	
	/// Throws an error, for invalid parsing
	protected RuntimeException invalidTemplateFormatException(char[] inTemplate, int errorPosition, String errorMessage) {
		return new RuntimeException(errorMessage);
	}
	
	/// Gets and returned the combined Map stack for variable substitution
}
