package picoded.core.struct.aggregation;

import java.util.function.BiFunction;
import java.util.Map;
import java.util.Collection;

//
// Represents a single aggregation function
// The actual max, count, sum etc is t be implemented separately
//
// If an aggregation function returns null it means that no logical aggregation could be done, for example fieldname not found, or value isnt a numeric value
@FunctionalInterface
public interface AggregationFunction extends
	BiFunction<String, Collection<Map<String, Object>>, Object> {
	Object apply(String fieldName, Collection<Map<String, Object>> dataSet);
}
