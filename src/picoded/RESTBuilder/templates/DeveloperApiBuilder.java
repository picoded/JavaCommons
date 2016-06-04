package picoded.RESTBuilder.templates;

import java.util.*;
import java.util.Map.Entry;

import picoded.RESTBuilder.*;
import picoded.JStack.*;
import picoded.JStruct.*;
import picoded.servlet.*;
import picoded.conv.ConvertJSON;
import picoded.conv.GenericConvert;
import picoded.conv.RegexUtils;
import picoded.enums.HttpRequestType;

public class DeveloperApiBuilder {
	
	/// The base builder to use
	protected RESTBuilder builder = null;
	
	/// Constructor for the development api.
	/// @param The RESTBuilder object to do "development" on
	public DeveloperApiBuilder(RESTBuilder inBuilder) {
		builder = inBuilder;
	}
	
}
