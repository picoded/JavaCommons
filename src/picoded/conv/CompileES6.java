package picoded.conv;

import java.lang.String;
import java.lang.Number;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.SourceFile;
//import com.google.javascript.jscomp.JSSourceFile;

/// ES6 to ES5 javascript closure compilation
///
/// ---------------------------------------------------------------------------------------------------
///
/// Technical notes: Uses closure compiler internally
///
public class CompileES6 {
	/**
	 * @param code JavaScript source code to compile.
	 * @return The compiled version of the code.
	 */
	public static String compile(String code) {
		return compile(code, "es6-input.js");
	}
	
	public static String compile(String code, String fileName) {
		if (fileName == null) {
			fileName = "es6-input.js";
		}
		
		try {
			Compiler compiler = new Compiler();
			
			// Simple optimization is used
			CompilerOptions options = new CompilerOptions();
			CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
			
			// Set language option
			options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT6);
			options.setLanguageOut(CompilerOptions.LanguageMode.ECMASCRIPT5);
			
			// To get the complete set of externs, the logic in
			// CompilerRunner.getDefaultExterns() should be used here.
			List<SourceFile> externList = CommandLineRunner.getBuiltinExterns(CompilerOptions.Environment.BROWSER);
			
			// The dummy input name "input.js" is used here so that any warnings or
			// errors will cite line numbers in terms of input.js.
			List<SourceFile> inputList = new ArrayList<SourceFile>();
			SourceFile input = (new SourceFile.Builder()).buildFromCode(fileName, code);
			inputList.add(input);
			
			// compile() returns a Result, but it is not needed here.
			compiler.compile(externList, inputList, options);
			
			// The compiler is responsible for generating the compiled code; it is not
			// accessible via the Result.
			return compiler.toSource();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
