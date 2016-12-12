package picoded.page.mte;

import java.util.Map;
//import picoded.page.mte.internal.*;

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
	public MinimalTemplateEngine() {
	}
	
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
	public String parseTemplate(String inTemplate, Map<String, Object> varMap) {
		TemplateSession ts = new TemplateSession(this, inTemplate, varMap);
		return ts.parseRaw(new StringBuilder(), 0, inTemplate.length()).toString();
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
	public String[][] expressionSet = new String[][] { new String[] { "${", "}" },
		new String[] { "{{#", "}}" }, new String[] { "{{", "}}" }, new String[] { "{{{", "}}}" } };
	
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
	// Utility functions, 
	// may get extracted out to external class in future
	//
	//----------------------------------------------------------------
	
	//----------------------------------------------------------------
	//
	// Internal stuff =)
	//
	//----------------------------------------------------------------
	
	/// Gets and returned the combined Map stack for variable substitution
}
