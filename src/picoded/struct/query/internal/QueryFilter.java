package picoded.struct.query.internal;

import java.util.*;
import picoded.struct.*;

/// Internal utilty function for string query filtering,
/// used before breaking by whitespace and tokenizing it.
///
/// The process goes through the following steps for tokenizing
///
/// 1) Replace out the ? numeric arguments with its named equivalent :x
/// 2) Enforce spaces before ":", before and after <=,=,!=,>=,(,)
///    Also, remove redundent whitespace, and normalize them to spaces
/// 4) Split by whitespace : into string tokens, and build the query object
///
/// @TODO: Optimize this class haha, generally this whole set of 
///        filters are NOT string modification optimized
public class QueryFilter {
	
	/// Searches and replace the query string ? with :QueryNumber
	///
	/// @params  the query string to filter out
	///
	/// @returns  the filtered string, and the amount of ? filtered to query number (last QueryNumber+1)
	public static MutablePair<String,Integer> filterQueryArguments(String query) {
		int queryCount = 0;
		int strPos = 0;
		
		String resString = query;
		while( (strPos = resString.indexOf("?")) >= 0 ) {
			resString = resString.substring(0,strPos) + ":"+queryCount+ resString.substring(strPos+1);
			++queryCount;
		}
		
		MutablePair<String,Integer> res = new MutablePair<String,Integer>();
		res.setLeft(resString);
		res.setRight(new Integer(queryCount));
		
		return res;
	}
	
	/// Converts the argument array to its named map format
	///
	/// @params  named map to build on and return, creates a HashMap if null
	/// @params  arguments array to convert from
	///
	/// @returns  the returned named map
	public static Map<String,Object> argumentsArrayToMap( Map<String,Object> baseMap, Object[] argArr ) {
		if( baseMap == null ) {
			baseMap = new HashMap<String,Object>();
		}
		
		for(int a=0; a<argArr.length; ++a) {
			baseMap.put(""+a, argArr[a]);
		}
		return baseMap;
	}
	
	/// Enforcing spaces before & after the critical characters.
	/// Also, remove redundent whitespace, and normalize them to spaces
	/// 
	/// @params  query string to filter
	/// 
	/// @returns  query string filtered
	public static String enforceRequiredWhitespace(String query) {
		query = query.replaceAll("\\s+"," "); //remove redundent whitespace
		
		// Add a space before named arguments
		query = query.replaceAll(":", " :");
		
		// Inefficently add extra whitespaces
		String[] replaceRegex = new String[] { "\\<\\=", "\\>\\=", "\\!\\=", "\\(", "\\)", "([^<|>|!|\\s])(\\=)" };
		String[] replaceString = new String[] { "<=", ">=", "!=", "(", ")", "$1 = " };
		for(int a=0; a<replaceRegex.length; ++a) {
			query = query.replaceAll(replaceRegex[a], " "+replaceString[a]+" ");
		}
		
		query = query.replaceAll("\\s+"," ").trim(); //remove redundent whitespace
		return query;
	}
	
	/// Refactor the query to one that is easily parsed by a tokenizer
	public static MutablePair<String,Map<String,Object>> refactorQuery( //
		String query, //
		Map<String,Object> baseMap, //
		Object[] argArr //
	) { //
		
		// Ensures argument map
		//----------------------------------------------------------
		if(baseMap == null) {
			baseMap = new HashMap<String,Object>();
		}
		
		// Prepare conversion of argument array to argument map
		//----------------------------------------------------------
		MutablePair<String,Integer> argReplacment = filterQueryArguments(query);
		
		int argReplacmentCount = argReplacment.getRight().intValue();
		int argArrCount = (argArr != null)? argArr.length : 0;
		if( argArrCount != argReplacmentCount ) {
			throw new RuntimeException("Query string argument count ("+argReplacmentCount+"), and argument array length mismatched ("+argArrCount+")");
		}
		if( argReplacmentCount > 0 ) {
			baseMap = argumentsArrayToMap(baseMap, argArr);
		}
		
		// Refactoring the query, and enfocting its whitespace
		//----------------------------------------------------------
		String resQuery = argReplacment.getLeft();
		resQuery = enforceRequiredWhitespace( resQuery );
		
		// Result return
		//----------------------------------------------------------
		MutablePair<String,Map<String,Object>> res = new MutablePair<String,Map<String,Object>>();
		res.setLeft(resQuery);
		res.setRight(baseMap);
		
		return res;
	}
}
