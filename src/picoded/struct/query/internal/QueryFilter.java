package picoded.struct.query.internal;

import java.util.*;
import picoded.struct.*;
import picoded.struct.query.*;
import picoded.struct.query.condition.*;

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
	
	//---------------------------------
	//
	// String processors
	//
	//---------------------------------
	
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
		
		return new MutablePair<String,Integer>(resString, new Integer(queryCount));
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
		String[] replaceRegex = new String[] { 
			"(\\(|\\))", //brackets
			"(\\<|\\>)([^\\=])", //Lesser or more, without equals
			"(\\<|\\>|\\!)\\=", //Less, More, Not equals
			 "([^<|>|!|\\s])(\\=)" //Matching equals sign WITHOUT comparision prefixes
		};
		
		String[] replaceString = new String[] { 
			"$1", //brackets
			"$1 $2", //Lesser or more, without equals
			"$1=", //Less, More, Not equals
			"$1 =" //Matching equals sign WITHOUT comparision prefixes
		};
		
		for(int a=0; a<replaceRegex.length; ++a) {
			query = query.replaceAll(replaceRegex[a], " "+replaceString[a]+" ");
		}
		
		query = query.replaceAll("\\s+"," ").trim(); //remove redundent whitespace
		return query;
	}
	
	/// Refactor the query to one that is easily parsed by a tokenizer
	///
	/// @params  the query string to filter out
	/// @params  named map to build on and return, creates a HashMap if null
	/// @params  arguments array to convert from
	///
	/// @returns  the filtered string, and the named argument map
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
		return new MutablePair<String,Map<String,Object>>(resQuery, baseMap);

	}
	
	//---------------------------------
	//
	// Query processors
	//
	//---------------------------------
	
	/// Standard query tokens
	public static List<String> basicOperators = Arrays.asList(new String[] { //
		"=", "<", ">", "<=", ">="
	}); //
	
	/// Extended query tokens
	public static List<String> combinationOperators = Arrays.asList(new String[] { //
		"AND", "OR", "NOT"
	}); //
	
	/// Builds and return query
	public static Query basicQueryFromTokens( Map<String,Object> params, String before, String operator, String after ) {
		String field = before;
		String namedParam = after;
		
		if(namedParam == null || !namedParam.startsWith(":")) {
			throw new RuntimeException("Unexpected named parameter set: "+before+" "+operator+" "+after);
		}
		
		if(operator.equals("=")) {
			return new Equals(field, namedParam, params);
			
		} else if(operator.equals("<")) {
			return new LessThan(field, namedParam, params);
			
		} else if(operator.equals("<=")) {
			return new LessThanOrEquals(field, namedParam, params);
			
		} else if(operator.equals(">")) {
			return new MoreThan(field, namedParam, params);
			
		} else if(operator.equals(">=")) {
			return new MoreThanOrEquals(field, namedParam, params);	
		}
		
		throw new RuntimeException("Unknown operator set found: "+before+" "+operator+" "+after);
	}
	
	/// Extract out the string, and build the basic query
	public static List<Object> buildBasicQuery( String[] token, Map<String,Object> paramMap ) {
		List<Object> ret = new ArrayList<Object>();
		
		for(int a=0; (a+2)<token.length; ++a) {
			
			/// Found an operator, pushes it
			if( basicOperators.contains(token[a+1]) ) {
				// Add query
				ret.add( basicQueryFromTokens(paramMap, token[a], token[a+1], token[a+2]) );
				a += 2; // Skip next 2 tokens
				continue; // next
			} 
			
			/// Failed operator find, push token to return list
			ret.add( token[a] );
		}
		
		return ret;
	}
	
	
	// /// Refactor the query to one that is easily parsed by a tokenizer
	// ///
	// /// @params  the query string to filter out
	// /// @params  named map to build on and return, creates a HashMap if null
	// /// @params  arguments array to convert from
	// ///
	// /// @returns  the query to be built
	// public static MutablePair<String,Map<String,Object>> refactorQuery( //
	// 	String query, //
	// 	Map<String,Object> baseMap, //
	// 	Object[] argArr //
	// ) { //
	// 	MutablePair<String,Map<String,Object>> refac = refactorQuery(query, baseMap, argArr);
	// 	
	// 	String[] tokens = refac.getLeft().split(" ");
	// 	for(int a=0; a<tokens.length; ++a) {
	// 		
	// 		
	// 	}
	// 	
	// 	return null;
	//}
}
