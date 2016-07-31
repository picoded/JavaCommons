package picoded.conv;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;
import com.floreysoft.jmte.Renderer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;

/// jmte provides a convinence wrapper around the core functionality of "Java Minimal Template Engine"
///
/// ---------------------------------------------------------------------------------------------------
///
/// TO-DO:
/// + testCase for unixTime conversion namedRenderer output testing
/// + remove commons.io.FileUtils dependency, use native file handling instead
/// + Detect absolute path (starting with "/") and use that instead
///
/// ---------------------------------------------------------------------------------------------------
///
/// Java Minimal Template Engine project
/// - https://code.google.com/p/jmte/
/// - getting started:[https://code.google.com/p/jmte/wiki/GettingStarted]
/// - additional API:[https://code.google.com/p/jmte/wiki/BestOfAPI]
/// - Apache License 2.0
///
public class JMTE {
	
	////////////////////////////////////////////////////////////////////
	// JMTE Core functionalities
	////////////////////////////////////////////////////////////////////
	
	/// The jmte backend
	private Engine engine = new Engine();
	
	/// The baseDataModel, that is used to populate the template variables
	public HashMap<String, Object> baseDataModel = new HashMap<String, Object>();
	
	/// Directly parse the template without modifying the data obj stack
	protected String parseTemplateRaw(String template, Map<String,Object>dataObj) {
		// Yes the many "\" slashes is to make sure all slash is escaped once before engine.transform, because the engine will escape them again X_X
		return engine.transform(template.replaceAll("\\\\","\\\\\\\\"), dataObj); 
	}
	
	/// Parses the template, with the baseDataModel data
	public String parseTemplate(String template) {
		return parseTemplateRaw(template, baseDataModel);
	}
	
	/// Parses the template, with the provided data & baseDataModel
	public String parseTemplate(String template, Map<String, Object> dataObj) {
		
		//Combines the 2 HashMap, with the provided data taking priority
		HashMap<String, Object> tempObj = new HashMap<String, Object>();
		tempObj.putAll(baseDataModel);
		tempObj.putAll(dataObj);
		
		return parseTemplateRaw(template, tempObj); 
	}
	
	/// Registers a class renderer. Note that you will need to import
	/// 'com.floreysoft.jmte.Renderer' to use this functionality.
	/// See https://code.google.com/p/jmte/wiki/BestOfAPI for more info
	///
	/// [TODO: Low Priority] registerRenderer test case
	public synchronized <C> JMTE registerRenderer(Class<C> clazz, Renderer<C> renderer) {
		engine.registerRenderer(clazz, renderer);
		return this;
	}
	
	/// Registers a named renderer. Note that you will need to import
	/// 'com.floreysoft.jmte.NamedRenderer' to use this functionality.
	/// See https://code.google.com/p/jmte/wiki/BestOfAPI for more info
	///
	/// [TODO: Low Priority] registerNamedRenderer test case
	public JMTE registerNamedRenderer(NamedRenderer renderer) {
		engine.registerNamedRenderer(renderer);
		return this;
	}
	
	/// Returns the currently used engine for jmte. Note that you will need to import
	/// 'com.floreysoft.jmte.Engine' to use this functionality.
	public com.floreysoft.jmte.Engine jmteEngine() {
		return engine;
	}
	
	////////////////////////////////////////////////////////////////////
	// JMTE UnixTimeToString (and vice-visa) display format helper functions
	////////////////////////////////////////////////////////////////////
	
	/// [Protected] unixTime to full text string formater
	protected static SimpleDateFormat fullTimeFormat = null;
	
	/// [Protected] unixTime to display text string formater
	protected static SimpleDateFormat displayTimeFormat = null;
	
	/// Assumed time zone used in the system
	public static TimeZone unixTimeAssumedTimeZone = TimeZone.getDefault();
	
	/// Project standardised means to display full time stamp (with seconds and timezone)
	/// This is integrated as a namedRenderer by default with the same name in JMTE
	public static String unixTimeToFullString(long unixSeconds) {
		if (fullTimeFormat == null) {
			fullTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss zzz"); //Date format
		}
		fullTimeFormat.setTimeZone(TimeZone.getTimeZone(String.valueOf(unixTimeAssumedTimeZone)));
		
		return "" + fullTimeFormat.format(new Date(unixSeconds * 1000L));
	}
	
	/// Project standardised means to display timestamp (date, and hour/min, without seconds/timezone)
	/// This is integrated as a namedRenderer by default with the same name in JMTE
	public static String unixTimeToDisplayTime(long unixSeconds) {
		if (displayTimeFormat == null) {
			displayTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm"); //Date format
		}
		displayTimeFormat.setTimeZone(TimeZone.getTimeZone(String.valueOf(unixTimeAssumedTimeZone)));
		
		return "" + displayTimeFormat.format(new Date(unixSeconds * 1000L));
	}
	
	/// UnixTime to display timestamp formetter: Example format 'dd/MM/yyyy HH:mm:ss zzz'
	/// Allows multiple formats type, however format isnt cached.
	public static String unixTimeToFormat(long unixSeconds, String format) {
		SimpleDateFormat tFormat = new SimpleDateFormat(format); //Date format
		tFormat.setTimeZone(unixTimeAssumedTimeZone);
		
		return "" + tFormat.format(new Date(unixSeconds * 1000L));
	}
	
	/////////////////////////////////////////////
	// JMTE namedRenderer template classes
	/////////////////////////////////////////////
	
	/// [protected] internal unixTimeToDisplayTime NamedRanderer for jmte implementation
	protected static class unixTimeToDisplayTime implements NamedRenderer {
		@Override
		public RenderFormatInfo getFormatInfo() {
			return null;
		}
		
		@Override
		public String getName() {
			return "unixTimeToDisplayTime";
		}
		
		@Override
		public Class<?>[] getSupportedClasses() {
			return new Class<?>[] { long.class, int.class, Number.class };
		}
		
		@Override
		public String render(Object o, String format, Locale L) {
			return JMTE.unixTimeToDisplayTime(((Number) o).longValue());
		}
	}
	
	/// [protected] internal unixTimeToDisplayTime NamedRanderer for jmte implementation
	protected static class unixTimeToFullString implements NamedRenderer {
		@Override
		public RenderFormatInfo getFormatInfo() {
			return null;
		}
		
		@Override
		public String getName() {
			return "unixTimeToFullString";
		}
		
		@Override
		public Class<?>[] getSupportedClasses() {
			return new Class<?>[] { long.class, int.class, Number.class };
		}
		
		@Override
		public String render(Object o, String format, Locale L) {
			return JMTE.unixTimeToFullString(((Number) o).longValue());
		}
	}
	
	/// [protected] internal unixTimeToFormat NamedRanderer for jmte implementation
	protected static class unixTimeToFormat implements NamedRenderer {
		@Override
		public RenderFormatInfo getFormatInfo() {
			return null;
		}
		
		@Override
		public String getName() {
			return "unixTimeToFormat";
		}
		
		@Override
		public Class<?>[] getSupportedClasses() {
			return new Class<?>[] { long.class, int.class, Number.class };
		}
		
		@Override
		public String render(Object o, String format, Locale L) {
			return JMTE.unixTimeToFormat(((Number) o).longValue(), format);
		}
	}
	
	/// [protected] internal unixTimeToFormat NamedRanderer for jmte implementation
	protected static class toString implements NamedRenderer {
		@Override
		public RenderFormatInfo getFormatInfo() {
			return null;
		}
		
		@Override
		public String getName() {
			return "toString";
		}
		
		@Override
		public Class<?>[] getSupportedClasses() {
			return new Class<?>[] { Object.class };
		}
		
		@Override
		public String render(Object o, String format, Locale L) {
			return GenericConvert.toString(o, format);
		}
	}
	
	/// [protected] internal unixTimeToDisplayTime NamedRanderer for jmte implementation
	protected static class escapeHtml implements NamedRenderer {
		@Override
		public RenderFormatInfo getFormatInfo() {
			return null;
		}
		
		@Override
		public String getName() {
			return "escapeHtml";
		}
		
		@Override
		public Class<?>[] getSupportedClasses() {
			return new Class<?>[] { String.class };
		}
		
		@Override
		public String render(Object o, String format, Locale L) {
			return picoded.conv.StringEscape.escapeHtml((String) o);
		}
	}
	
	/// [protected] internal encodeURI NamedRanderer for jmte implementation
	protected static class encodeURI implements NamedRenderer {
		@Override
		public RenderFormatInfo getFormatInfo() {
			return null;
		}
		
		@Override
		public String getName() {
			return "encodeURI";
		}
		
		@Override
		public Class<?>[] getSupportedClasses() {
			return new Class<?>[] { String.class };
		}
		
		@Override
		public String render(Object o, String format, Locale L) {
			return picoded.conv.StringEscape.encodeURI((String) o);
		}
	}
	
	/////////////////////////////////////////////
	// JMTE constructor with default templates
	/////////////////////////////////////////////
	public JMTE() {
		registerNamedRenderer(new unixTimeToFullString());
		registerNamedRenderer(new unixTimeToDisplayTime());
		registerNamedRenderer(new unixTimeToFormat());
		
		registerNamedRenderer(new toString());
		registerNamedRenderer(new escapeHtml());
		registerNamedRenderer(new encodeURI());
	}
	
	public JMTE(String dir) {
		this();
		baseDirectory = dir;
	}
	
	/////////////////////////////////////////////
	// JMTE html parts templating system
	/////////////////////////////////////////////
	
	/// Base directory in which the html parts are extracted from
	public String baseDirectory = "./";
	
	/// Gets and returns the raw file data, inside the base directory folder
	/// TODO: Remove fileUtils dependency
	/// TODO: Detect absolute path (starting with "/") and use that instead
	///
	public String rawHtmlPart(String fileName) throws java.io.IOException {
		File tFile = new File(baseDirectory + fileName);
		
		if (!tFile.exists()) {
			throw new java.io.IOException("Template file does not exists : " + baseDirectory + fileName);
		}
		if (!tFile.canRead()) {
			throw new java.io.IOException("Template file is unreadable : " + baseDirectory + fileName);
		}
		
		return readFileToString(tFile);
		//return FileUtils.readFileToString( tFile, "UTF_8" ); //,  StandardCharsets.UTF_8
	}
	
	/// Fetches and return the template output
	public String htmlTemplatePart(String fileName) throws java.io.IOException {
		String rawStr = rawHtmlPart(fileName);
		return parseTemplate(rawStr);
	}
	
	/// Fetches and return the template output, with the data object
	public String htmlTemplatePart(String fileName, Map<String, Object> dataObj) throws java.io.IOException {
		String rawStr = rawHtmlPart(fileName);
		return parseTemplate(rawStr, dataObj);
	}
	
	/////////////////////////////////////////////
	/// JMTE readFileToString workaround
	/// This was done as certain deployment environment has issues with the FileUtil
	/// library (possibly encoding, reasons unknown). By reading the file "RAW" it
	/// runs the risk of being at the mercy of the OS encoding settings. While working with such systems
	/////////////////////////////////////////////
	public static String readFileToString(String fullFilePath) throws IOException {
		return readFileToString(new File(fullFilePath));
	}
	
	public static String readFileToString(File inFile) throws IOException {
		return readFileToString(new FileReader(inFile));
	}
	
	public static String readFileToString(FileReader fReader) throws IOException {
		BufferedReader br = new BufferedReader(fReader);
		
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			
			if (line != null) {
				sb.append(line);
				line = br.readLine();
			}
			
			while (line != null) {
				sb.append("\n");
				sb.append(line);
				line = br.readLine();
			}
			
			br.close();
			br = null;
			
			return sb.toString();
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}
}
