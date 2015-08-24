package picoded.struct.query.internal;

import picoded.struct.*;

/// Internal utilty function for string query filtering,
/// used before breaking by whitespace and tokenizing it.
///
/// @TODO: Optimize this class haha, generally this whole set of 
///        filters are NOT string modification optimized
public class QueryFilter {
	
	/// Searches and replace the query string ? with :QueryNumber
	///
	/// @params  the query string to filter out
	///
	/// @returns  the filtered string, and the amount of ? filtered to query number (last QueryNumber+1)
	public MutablePair<String,Integer> filterQueryArguments(String query) {
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
}
