package picoded.webTemplateEngines.PagesBuilder;

import java.io.*;
import java.util.*;

import picoded.enums.*;
import picoded.conv.*;
import picoded.struct.*;
import picoded.fileUtils.*;
import picoded.servlet.*;
import picoded.servletUtils.*;

///
/// Core class that handle the conversion and copying process.
///
/// NOT the folder iteration
///
/// @TODO
/// + Minify the rawPageName/index.html
///
public class PagesBuilderCore extends picoded.page.builder.PageBuilderCore {
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	///
	public PagesBuilderCore(File inPagesFolder) {
		super(inPagesFolder);
	}
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	///
	public PagesBuilderCore(String inPagesFolder) {
		super(inPagesFolder);
	}
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	/// @param The target folder to build the result into
	///
	public PagesBuilderCore(File inPagesFolder, File inOutputFolder) {
		super(inPagesFolder, inOutputFolder);
	}
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	/// @param The target folder to build the result into
	///
	public PagesBuilderCore(String inPagesFolder, String inOutputFolder) {
		super(inPagesFolder, inOutputFolder);
	}
	
}
