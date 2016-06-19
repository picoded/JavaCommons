package picoded.page.mte.internal;

import picoded.page.mte.*;

///
/// Takes in the template, build a syntax tree (hahaha). And execute it!
///
public class TemplateSyntaxTree {
	
	/// Refence to the calling MinimalTemplateEngine
	public final MinimalTemplateEngine parent;
	
	/// The template string / char array used
	public final String templateString;
	public final char[] templateChars;
	
}
