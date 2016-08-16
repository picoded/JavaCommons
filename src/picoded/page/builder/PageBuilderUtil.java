package picoded.page.builder;

import java.io.*;
import java.util.*;

// JMTE inner functions add-on
import com.floreysoft.jmte.*;

// Sub modules useds
import picoded.enums.*;
import picoded.conv.*;
import picoded.struct.*;
import picoded.fileUtils.*;
import picoded.servlet.*;
import picoded.servletUtils.*;

///
/// Page builder utility functions
///
public class PageBuilderUtil {
	
	/// Parent usage reference
	public PageBuilderCore core = null;
	
	/// Constructor
	public PageBuilderUtil(PageBuilderCore inCore) {
		core = inCore;
	}
	
	/// Compiles a source file, into the output file if its deemed necesary. 
	///
	/// Compiling, occurs only if the source file is newer then the output file
	/// Or was modified within the last 3 seconds. Else it silently ends.
	///
	/// Even after compilation, write only occurs if the file differ.
	/// 
	/// This approach was taken to conservatively reduce the compilation time during developers mode,
	/// while preventing race conditions from uneedingly marking output files as "newer"
	///
	/// @param  Either "less" or "es6" is supported
	/// @param  The source file to compile from
	/// @param  The output file to compile to
	///
	public void compileFileIfNewer(String fileExtn, File srcFile, File outFile) throws IOException {
		long srcModified = srcFile.lastModified();
		long threshold = 3 * 1000; //3 seconds threshold
		
		// Source file was modified within the threshold (past 3 seconds)
		if (srcModified > (System.currentTimeMillis() - threshold)) {
			// blank if clause : the ^ above was Intentionally structured for readability
		} else {
			// file was not modified within the threshold
			if (outFile.lastModified() > srcFile.lastModified()) {
				// Skipped, as output file is newer then src file 
				return;
			}
		}
		
		// Get source file
		String fileVal = FileUtils.readFileToString_withFallback(srcFile, "");
		
		// Less and es6 specific conversion
		if (fileExtn.equalsIgnoreCase("less")) {
			fileVal = core.less.compile(fileVal);
		} else if (fileExtn.equalsIgnoreCase("es6")) {
			fileVal = CompileES6.compile(fileVal, srcFile.getName());
		}
		
		// Output file
		FileUtils.writeStringToFile_ifDifferant(outFile, fileVal);
	}
	
}
