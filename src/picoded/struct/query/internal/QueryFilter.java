package picoded.struct.query.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import picoded.struct.MutablePair;
import picoded.struct.query.Query;
import picoded.struct.query.condition.And;
import picoded.struct.query.condition.Equals;
import picoded.struct.query.condition.LessThan;
import picoded.struct.query.condition.LessThanOrEquals;
import picoded.struct.query.condition.Like;
import picoded.struct.query.condition.MoreThan;
import picoded.struct.query.condition.MoreThanOrEquals;
import picoded.struct.query.condition.Not;
import picoded.struct.query.condition.NotEquals;
import picoded.struct.query.condition.Or;

/// Internal utilty function for string query filtering,
/// used before breaking by whitespace and tokenizing it.
///
/// # String processing
///
/// The process goes through the following steps for tokenizing th string
///
/// 1) Replace out the ? numeric arguments with its named equivalent :x
/// 2) Enforce spaces before ":", before and after <=,=,!=,>=,(,)
///    Also, remove redundent whitespace, and normalize them to spaces
/// 3) Split by whitespace : into string tokens, and build the query object
///
/// # Token processing
///
/// After which the process goes through the steps processing these tokens into Query tokens,
/// which are linked and reduced till there is only 1 query token.
///
/// 1) Scan for basic comparision operators, this is found using the compare operators such as =, <=, >
///    and build the Query tokens to replace the string tokens.
/// 2) Scans for an isolated enclosed bracket ( query ), that has no inner enclosed bracket string tokens.
///    In event the the whole list does not contains inner bracket, the whole token set is chosen
/// 3) Inside the isolated token set, does a combination merger of all the various Query tokens,
///    Forming a single large token, per isolated enclosed bracket token set.
/// 4) Step 2 & 3 is looped till there is only 1 token left, which is returned
///
/// @TODO: Optimize this class haha, generally this whole set of 
///        filters are NOT string modification optimized.
///
public class QueryFilter {
	
	//---------------------------------
	//
	// String processors
	//
	//---------------------------------
	
	//
	// Step 1) 
	//
	
	/// Searches and replace the query string ? with :QueryNumber
	///
	/// @params  the query string to filter out
	///
	/// @returns  the filtered string, and the amount of ? filtered to query number (last QueryNumber+1)
	public static MutablePair<String, Integer> filterQueryArguments(String query) {
		int queryCount = 0;
		int strPos = 0;
		
		String resString = query;
		while ((strPos = resString.indexOf("?")) >= 0) {
			resString = (resString.substring(0, strPos) + ":" + queryCount + resString
				.substring(strPos + 1));
			++queryCount;
		}
		
		return new MutablePair<String, Integer>(resString, new Integer(queryCount));
	}
	
	/// Converts the argument array to its named map format
	///
	/// @params  named map to build on and return, creates a HashMap if null
	/// @params  arguments array to convert from
	///
	/// @returns  the returned named map
	public static Map<String, Object> argumentsArrayToMap(Map<String, Object> baseMap,
		Object[] argArr) {
		if (baseMap == null) {
			baseMap = new HashMap<String, Object>();
		}
		
		if (argArr != null && argArr.length > 0) {
			for (int a = 0; a < argArr.length; ++a) {
				baseMap.put("" + a, argArr[a]);
			}
		}
		
		return baseMap;
	}
	
	//
	// Step 2) 
	//
	
	/// Enforcing spaces before & after the critical characters.
	/// Also, remove redundent whitespace, and normalize them to spaces
	/// 
	/// @params  query string to filter
	/// 
	/// @returns  query string filtered
	public static String enforceRequiredWhitespace(String query) {
		query = query.replaceAll("\\s+", " "); //remove redundent whitespace
		
		// Add a space before named arguments
		query = query.replaceAll(":", " :");
		
		// Inefficently add extra whitespaces
		String[] replaceRegex = new String[] { //
		"(\\(|\\))", //brackets
			"(\\<|\\>)([^\\=])", //Lesser or more, without equals
			"(\\<|\\>|\\!)\\=", //Less, More, Not equals
			"([^<|>|!|\\s])(\\=)" //Matching equals sign WITHOUT comparision prefixes
		};
		
		String[] replaceString = new String[] { //
		" $1 ", //brackets
			" $1 $2 ", //Lesser or more, without equals
			" $1= ", //Less, More, Not equals
			"$1 = " //Matching equals sign WITHOUT comparision prefixes
		};
		
		for (int a = 0; a < replaceRegex.length; ++a) {
			query = query.replaceAll(replaceRegex[a], replaceString[a]);
		}
		
		query = query.replaceAll("\\s+", " ").trim(); //remove redundent whitespace
		return query;
	}
	
	//
	// Step 1 & 2 Combined) 
	//
	
	/// Refactor the query to one that is easily parsed by a tokenizer
	///
	/// @params  the query string to filter out
	/// @params  named map to build on and return, creates a HashMap if null
	/// @params  arguments array to convert from
	///
	/// @returns  the filtered string, and the named argument map
	public static MutablePair<String, Map<String, Object>> refactorQuery( //
		String query, //
		Map<String, Object> baseMap, //
		Object[] argArr //
	) { //
	
		// Ensures argument map
		//----------------------------------------------------------
		if (baseMap == null) {
			baseMap = new HashMap<String, Object>();
		}
		
		// Prepare conversion of argument array to argument map
		//----------------------------------------------------------
		MutablePair<String, Integer> argReplacment = filterQueryArguments(query);
		
		int argReplacmentCount = argReplacment.getRight().intValue();
		int argArrCount = (argArr != null) ? argArr.length : argReplacmentCount;
		if (argArrCount != argReplacmentCount) {
			throw new RuntimeException("Query string argument count (" + argReplacmentCount
				+ "), and argument array length mismatched (" + argArrCount + ")");
		}
		if (argReplacmentCount > 0) {
			baseMap = argumentsArrayToMap(baseMap, argArr);
		}
		
		// Refactoring the query, and enfocting its whitespace
		//----------------------------------------------------------
		String resQuery = argReplacment.getLeft();
		resQuery = enforceRequiredWhitespace(resQuery);
		
		// Result return
		//----------------------------------------------------------
		return new MutablePair<String, Map<String, Object>>(resQuery, baseMap);
		
	}
	
	//
	// Step 3) 
	//
	
	/// Splits the refactoed query into string tokens
	///
	/// @params  the query string from refactorQuery
	///
	/// @returns  string tokens array
	public static String[] splitRefactoredQuery(String query) {
		//System.out.println("splitRefactoredQuery: "+query);
		return query.split("\\s+");
	}
	
	//---------------------------------
	//
	// Query processors
	//
	//---------------------------------
	
	//
	// Step 1) 
	//
	
	/// Basic query operator tokens to search for
	public static List<String> basicOperators = Arrays.asList(new String[] { //
		"=", "<", ">", "<=", ">=", "LIKE", "!=" }); //
	
	/// Extended query tokens to search for
	public static List<String> combinationOperators = Arrays.asList(new String[] { //
		"AND", "OR", "NOT" }); //
	
	/// Extract out the string, and build the basic query
	/// 
	/// @params  token array containing the original query
	/// @params  parameter map to use as default
	///
	/// @returns  list of objects of which consist either of built operator Query, and string tokens
	public static List<Object> buildBasicQuery(String[] token, Map<String, Object> paramMap) {
		List<Object> ret = new ArrayList<Object>();
		
		//System.out.println( "buildBasicQuery (start)" + ConvertJSON.fromList( Arrays.asList(token) ) );
		
		int tokenLength = token.length;
		for (int a = 0; a < tokenLength; ++a) {
			
			/// Found an operator, pushes it
			if ((a + 1) < tokenLength && basicOperators.contains(token[a + 1])) {
				
				// Check for unexpected end of token
				if ((a + 2) >= tokenLength) {
					throw new RuntimeException("Unexpected end of operator token : " + token[a + 1]);
				}
				
				// Add query
				ret.add(basicQueryFromTokens(paramMap, token[a], token[a + 1], token[a + 2]));
				a += 2; // Skip next 2 tokens
				continue; // next
			}
			
			/// Failed operator find, push token to return list
			ret.add(token[a]);
		}
		
		//System.out.println( "buildBasicQuery (end)" + ConvertJSON.fromList( ret ) );
		
		return ret;
	}
	
	/// Refactor the query to one that is easily parsed by a tokenizer
	///
	/// @params  parameter map to use as default
	/// @params  field name token before the operator used 
	/// @params  operator token used to choose the query
	/// @params  named argument used after the operator
	///
	/// @returns  built query
	public static Query basicQueryFromTokens(Map<String, Object> paramsMap, String before,
		String operator, String after) {
		String field = QueryUtils.unwrapFieldName(before);
		String namedParam = after;
		
		if (namedParam == null || !namedParam.startsWith(":")) {
			throw new RuntimeException("Unexpected named parameter set: " + before + " " + operator
				+ " " + after);
		} else {
			namedParam = namedParam.substring(1);
		}
		
		if (operator.equals("=")) {
			return new Equals(field, namedParam, paramsMap);
			
		} else if (operator.equals("<")) {
			return new LessThan(field, namedParam, paramsMap);
			
		} else if (operator.equals("<=")) {
			return new LessThanOrEquals(field, namedParam, paramsMap);
			
		} else if (operator.equals(">")) {
			return new MoreThan(field, namedParam, paramsMap);
			
		} else if (operator.equals(">=")) {
			return new MoreThanOrEquals(field, namedParam, paramsMap);
		} else if (operator.equals("LIKE")) {
			return new Like(field, namedParam, paramsMap);
		} else if (operator.equals("!=")) {
			return new NotEquals(field, namedParam, paramsMap);
		}
		
		throw new RuntimeException("Unknown operator set found: " + before + " " + operator + " "
			+ after);
	}
	
	//
	// Step 2) 
	//
	
	/// Scans the token set for an isolated set
	///
	/// @params  Current list of Query and string tokens
	///
	/// @returns  {int[2]}  an array consisting of the left and right position. -1 if not found
	@SuppressWarnings("unused")
	public static int[] findCompleteEnclosure(List<Object> queryTokens) {
		
		// Gets the start and end
		int start = -1;
		int end = -1;
		
		//System.out.println(queryTokens.toString());
		
		// Iterates the query token
		for (int a = 0; a < queryTokens.size(); ++a) {
			
			// Gets the token, and skip if not a string
			Object token = queryTokens.get(a);
			if (!(token instanceof String)) {
				continue;
			}
			
			if (token.equals("(")) {
				start = a;
			} else if (token.equals(")")) {
				if (start == -1) {
					throw new RuntimeException("Found closing bracket ')' without opening bracket");
				} else {
					return new int[] { start, a };
				}
			}
		}
		
		if (start >= 0) {
			throw new RuntimeException("Found starting bracket '(' without closing bracket");
		}
		
		return new int[] { -1, -1 };
	}
	
	//
	// Step 3) 
	//
	
	/// Builds a combination query and returns
	///
	/// @params  The combination type string
	/// @params  list of child query to join in the combination
	/// @params  default parameter map of query
	///
	/// @returns  The combined query
	public static Query combinationQuery(String combinationType, List<Query> childQuery,
		Map<String, Object> paramsMap) {
		if (combinationType.equals("AND")) {
			return new And(childQuery, paramsMap);
		} else if (combinationType.equals("OR")) {
			return new Or(childQuery, paramsMap);
		} else if (combinationType.equals("NOT")) {
			return new Not(childQuery, paramsMap);
		}
		
		throw new RuntimeException("Unknown combination set found: " + combinationType + " "
			+ childQuery);
	}
	
	/// Builds a combined query given an isolated token set
	///
	/// @params  The isolated token set
	/// @params  default parameter map of query
	///
	/// @returns  The combined query
	public static Query collapseQueryTokensWithoutBrackets(List<Object> tokens,
		Map<String, Object> paramMap) {
		if (tokens.size() == 1) {
			Object t = tokens.get(0);
			if (t instanceof Query) {
				return (Query) t;
			}
		}
		
		List<Query> childList = new ArrayList<Query>();
		String combinationType = null;
		
		for (int a = 0; a < tokens.size(); ++a) {
			Object singleToken = tokens.get(a);
			if (singleToken instanceof Query) {
				childList.add((Query) singleToken);
			} else if (singleToken instanceof String) {
				String op = singleToken.toString().toUpperCase();
				
				if (!(combinationOperators.contains(op))) {
					throw new RuntimeException("Unable to process combination token: " + op);
				}
				
				// Setup combination type
				if (combinationType == null) {
					combinationType = op;
					continue;
				}
				
				// Continue the combination processing
				if (combinationType.equals(op)) {
					continue;
				}
				
				// Compiles to list till the current point
				List<Object> subList = new ArrayList<Object>();
				
				// Check child list
				if (childList.size() <= 0) {
					throw new RuntimeException("Unexpected blank child list: " + childList);
				}
				
				// Adds everything up till the current point
				subList.add(combinationQuery(combinationType, childList, paramMap));
				
				// Chain in to the next combination type
				subList.addAll(tokens.subList(a, tokens.size()));
				
				// Recursive
				return collapseQueryTokensWithoutBrackets(subList, paramMap);
				
			} else {
				throw new RuntimeException("Unknown token type: " + singleToken);
			}
		}
		
		if (combinationType == null) {
			
			// Empty token handling
			if (tokens == null || tokens.size() == 0) {
				if (childList == null || childList.size() == 0) {
					throw new RuntimeException("Missing combination token: Empty tokens and child list");
				}
				
				// Single nested child, promote it
				if (childList.size() == 1) {
					return childList.get(0);
				} else {
					return combinationQuery("AND", childList, paramMap);
				}
			}
			throw new RuntimeException("Missing combination token: " + tokens);
		}
		
		return combinationQuery(combinationType, childList, paramMap);
	}
	
	//
	// Step 4) 
	//
	
	/// Collapses the query token into a single query,
	/// This first isolates out the brackets, before calling collapseQueryTokensWithoutBrackets
	/// and does step 4), which does step 2 & 3) till a single query remains
	///
	/// @params  list of tokens to combine
	/// @params  default parameter map of query
	///
	/// @returns  The combined query
	public static Query collapseQueryTokens(List<Object> tokens, Map<String, Object> paramMap) {
		
		while (tokens.size() > 1) {
			int[] brackets = findCompleteEnclosure(tokens);
			
			// Full collapse finally
			if (brackets[0] == -1 || brackets[1] == -1) {
				return collapseQueryTokensWithoutBrackets(tokens, paramMap);
			}
			
			// Collapse segment by segment
			List<Object> newList = new ArrayList<Object>();
			
			newList.addAll(tokens.subList(0, brackets[0]));
			
			List<Object> isolatedList = tokens.subList(brackets[0] + 1, brackets[1]);
			//System.out.println( isolatedList );
			
			Query isolatedQuery = collapseQueryTokensWithoutBrackets(isolatedList, paramMap);
			//System.out.println( isolatedQuery.toString() );
			
			newList.add(isolatedQuery);
			newList.addAll(tokens.subList(brackets[1] + 1, tokens.size()));
			
			tokens = newList;
		}
		
		Object singleToken = tokens.get(0);
		if (singleToken instanceof Query) {
			return (Query) singleToken;
		}
		
		throw new RuntimeException("Unexpected collapseQueryTokens end -> " + singleToken);
	}
	
	//
	// All the string, tokenizer steps combined.
	//
	
	/// Refactor the query to one that is easily parsed by a tokenizer
	///
	/// @params  the query string to filter out
	/// @params  named map to build on and return, creates a HashMap if null
	/// @params  arguments array to convert from
	///
	/// @returns  the query to be built
	public static Query buildQuery( //
		String query, //
		Map<String, Object> baseMap, //
		Object[] argArr //
	) { //
		MutablePair<String, Map<String, Object>> refac = refactorQuery(query, baseMap, argArr);
		String[] querySplit = splitRefactoredQuery(refac.getLeft());
		List<Object> tokenArr = buildBasicQuery(querySplit, refac.getRight());
		return collapseQueryTokens(tokenArr, refac.getRight());
	}
}
