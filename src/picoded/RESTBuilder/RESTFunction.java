
package picoded.RESTBuilder;

import java.util.function.BiFunction;
import java.util.Map;

///
/// Represents a single function which will recieve the REST request, this is used as a Java 8 functional interface
///
@FunctionalInterface
public interface RESTFunction extends BiFunction<RESTRequest, Map<String,Object>, Map<String,Object>> {
	Map<String,Object> apply(RESTRequest request, Map<String,Object> result );
} 
