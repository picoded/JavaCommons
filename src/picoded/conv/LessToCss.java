package picoded.conv;

import org.lesscss.*;

public class LessToCss {
	
	///////////////////////////////////////////////////////
	// 
	// Constructor
	// 
	///////////////////////////////////////////////////////
	
	public LessToCss() {
		lessCompiler = new LessCompiler();
	}
	
	protected LessCompiler lessCompiler = null; //new LessCompiler();
	
	public String compile(String raw) {
		try {
			return lessCompiler.compile(raw);
		} catch(LessException e) {
			throw new RuntimeException(e);
		}
	}
	
}
