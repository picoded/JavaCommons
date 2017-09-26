package picoded.core.struct.aggregation;

import java.util.*;
import java.lang.Math;
import java.math.BigDecimal;

import picoded.core.conv.GenericConvert;

/**
* Aggregation library that build on top of query library.
* To extract aggregation metrics across larger dataset.
*
* As such filtering of data similarly uses the SQL where clause
*
* Aggregation to include
* + count(fieldname)
* + max(fieldname)
* + min(fieldname)
* + avg(fieldname)
* + sum(fieldname)
*
**/
public class Aggregation {

	public static String COUNT = "count";
	public static String MAX = "max";
	public static String MIN = "min";
	public static String AVG = "avg";
	public static String SUM = "sum";

	// this variable is here temporarily to make the avg function not crash if the result has a repeating non terminating decimal portion
	// will limit the result to 2 decimal places
	public static int AVG_RESULT_MAX_DECIMAL_PLACES = 2;

	/**
	*
	* Terms [ "count(*)", "max(investment_total)", "sum(investment_count)" ]
	* Query
	*/
	public static Map<String, Object> aggregation(String[] inAggregationTermsAndParameters, Collection<Map<String, Object>> dataSet) {
		// 1. Ensure input safety
		if(inAggregationTermsAndParameters == null || inAggregationTermsAndParameters.length <= 0){
			throw new RuntimeException("No aggregation terms given.");
		}

		if(dataSet == null){
			throw new RuntimeException("No dataSet given.");
		}

		// 2. Extract the terms and arguments
		String[] aggTerms = extractAggregationTerms(inAggregationTermsAndParameters);
		String[] aggArguments = extractAggregationArguments(inAggregationTermsAndParameters);

		if(aggTerms.length <= 0){
			throw new RuntimeException("Unable to extract aggregation terms.");
		}

		if(aggArguments.length <= 0){
			throw new RuntimeException("Unable to extract aggregation arguments.");
		}

		if(aggTerms.length != aggArguments.length){
			throw new RuntimeException("Aggregation terms and arguments lengths differ.");
		}

		// 3. Based on term, run the correct aggregation function
		Map<String, Object> aggResult = new HashMap<String, Object>();
		for(int i = 0; i < aggTerms.length; ++i){
			if(!defaultAggFunctionDefinitions().containsKey(aggTerms[i])){
				aggResult.put(inAggregationTermsAndParameters[i], "Unable to fetch AggregationFunction for " + inAggregationTermsAndParameters[i]);
			}else{
				aggResult.put(inAggregationTermsAndParameters[i], defaultAggFunctionDefinitions().get(aggTerms[i]).apply(aggArguments[i], dataSet));
			}
		}

		return aggResult;
	}

	public static String[] extractAggregationTerms(String[] aggregationTerms){
		String[] result = new String[aggregationTerms.length];

		for(int i = 0; i < aggregationTerms.length; ++i){
			result[i] = aggregationTerms[i].substring(0, aggregationTerms[i].indexOf('('));
		}

		return result;
	}

	public static String[] extractAggregationArguments(String[] aggregationTerms){
		String[] result = new String[aggregationTerms.length];

		for(int i = 0; i < aggregationTerms.length; ++i){
			result [i] = aggregationTerms[i].substring(aggregationTerms[i].indexOf('(') + 1, aggregationTerms[i].lastIndexOf(')'));
		}

		return result;
	}

	//-------------------
	//
	// Aggregation Implementations
	//
	//-------------------
	private static Map<String, AggregationFunction> aggFuncDefinitions = null;
	public synchronized static Map<String, AggregationFunction> defaultAggFunctionDefinitions(){
		if(aggFuncDefinitions != null){
			return aggFuncDefinitions;
		}

		aggFuncDefinitions = new HashMap<String, AggregationFunction>();
		aggFuncDefinitions.put("count", count);
		aggFuncDefinitions.put("max", max);
		aggFuncDefinitions.put("min", min);
		aggFuncDefinitions.put("avg", avg);
		aggFuncDefinitions.put("sum", sum);

		return aggFuncDefinitions;
	}

	public static AggregationFunction count = (fieldName, inputDataSet) -> {
		int countResult = 0;
		for(Map<String, Object> mapData : inputDataSet){
			if(mapData.get(fieldName) == null)
				continue;
			if(mapData.containsKey(fieldName)){
				++countResult;
			}
		}
		return new BigDecimal(Integer.toString(countResult));
	};

	public static AggregationFunction max = (fieldName, inputDataSet) -> {
		BigDecimal maxResult = null;
		for(Map<String, Object> mapData : inputDataSet){
			if(mapData.get(fieldName) == null)
				continue;
			BigDecimal currentNumber = new BigDecimal(mapData.get(fieldName).toString());
			if(maxResult == null){
				maxResult = currentNumber;
			}

			maxResult = currentNumber.max(maxResult);
		}
		return maxResult;
	};

	public static AggregationFunction min = (fieldName, inputDataSet) -> {
		BigDecimal minResult = null;
		for(Map<String, Object> mapData : inputDataSet){
			if(mapData.get(fieldName) == null)
				continue;
			BigDecimal currentNumber = new BigDecimal(mapData.get(fieldName).toString());
			if(minResult == null){
				minResult = currentNumber;
			}

			minResult = currentNumber.min(minResult);
		}
		return minResult;
	};

	public static AggregationFunction avg = (fieldName, inputDataSet) -> {
		BigDecimal avgResult = new BigDecimal(0);
		BigDecimal sumResult = new BigDecimal(0);
		int valueCount = 0;

		for(Map<String, Object> mapData : inputDataSet){
			if(mapData.get(fieldName) == null)
				continue;
			BigDecimal currentNumber = new BigDecimal(mapData.get(fieldName).toString());
			sumResult = sumResult.add(currentNumber);
			++valueCount;
		}

		avgResult = sumResult.divide(new BigDecimal(valueCount), AVG_RESULT_MAX_DECIMAL_PLACES, BigDecimal.ROUND_HALF_UP);
		return avgResult;
	};

	public static AggregationFunction sum = (fieldName, inputDataSet) -> {
		BigDecimal sumResult = new BigDecimal(0);
		for(Map<String, Object> mapData : inputDataSet){
			if(mapData.get(fieldName) == null)
				continue;
			BigDecimal currentNumber = new BigDecimal(mapData.get(fieldName).toString());
			sumResult = sumResult.add(currentNumber);
		}

		return sumResult;
	};
}