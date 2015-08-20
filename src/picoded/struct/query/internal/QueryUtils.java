package picoded.struct.query.internal;

import java.text.NumberFormat;
import java.util.function.*;
import java.util.*;

public class QueryUtils {
	
	public static Double normalizeNumber(Object number){
		Double val = null;
		if(number instanceof Integer){
			val = (Double)((Integer)number * 1.0);
		}else if(number instanceof Float){
			val = (Double)((Float)number * 1.0);
		}else if(number instanceof Double){
			val = (Double)number;
		}
		return val;
	}
	
	//returns String only, and ONLY if it should be compared as a string
	//if its a number, will return as a double
	public static Object normalizeObject(Object source){
		if(source instanceof String){
			if(((String) source).matches("[0-9]+") || ((String)source).contains(".")){ //extremely rudimentary check for a number, needs to be improved
				try{
					Number sourceAsNumber = NumberFormat.getNumberInstance(Locale.ENGLISH).parse((String)source);
					Double sourceAsDouble = sourceAsNumber.doubleValue();
					return sourceAsDouble;
				}catch(Exception ex){
					throw new RuntimeException("exception in normalizeObject-> " + ex.getMessage());
				}
			}else{
				return (String)source;
			}
		}else{
			return normalizeNumber(source);
		}
	}
}
