package picoded.webTemplateEngines.PagesBuilder;

import java.io.*;
import java.util.*;
import javax.servlet.http.HttpServletResponse;

import picoded.enums.*;
import picoded.conv.*;
import picoded.struct.*;
import picoded.fileUtils.*;
import picoded.servlet.*;
import picoded.servletUtils.*;

///
/// Rapid pages prototyping, support single and multipage mode, caching, etc.
/// Basically lots of good stuff =)
///
/// This is extended from the templating format previously used in ServletCommons
///
public class PagesBuilder extends picoded.page.builder.PageBuilder {
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	/// @param The target folder to build the result into
	///
	public PagesBuilder(File inPagesFolder, File inOutputFolder) {
		super(inPagesFolder, inOutputFolder);
	}
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	/// @param The target folder to build the result into
	///
	public PagesBuilder(String inPagesFolder, String inOutputFolder) {
		super(inPagesFolder, inOutputFolder);
	}
	
}
