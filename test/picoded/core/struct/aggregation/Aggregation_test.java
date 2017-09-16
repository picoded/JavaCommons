package picoded.core.struct.aggregation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.*;

import java.math.BigDecimal;

public class Aggregation_test {
	private List<Map<String,Object>> fullDataSet = null;

	@Before
	public void setup(){
		initialiseFullDataSet();
	}

	private void initialiseFullDataSet(){
		fullDataSet = new ArrayList<Map<String, Object>>();

		fullDataSet.add(createSingleMap(0, 0.0, "0"));
		fullDataSet.add(createSingleMap(1, 1.1, "1"));
		fullDataSet.add(createSingleMap(2, 2.2, "2"));
		fullDataSet.add(createSingleMap(3, 3.3, "3"));
		fullDataSet.add(createSingleMap(4, 4.4, "4"));
		fullDataSet.add(createSingleMap(5, 5.5, "5"));
		fullDataSet.add(createSingleMap(6, 6.6, "6"));
		fullDataSet.add(createSingleMap(7, 7.7, "7"));
		fullDataSet.add(createSingleMap(8, 8.8, "8"));
		fullDataSet.add(createSingleMap(9, 9.9, "9"));
	}

	private Map<String, Object> createSingleMap(int intVal, double doubleVal, String stringVal){
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("intVal", intVal);
		ret.put("doubleVal", doubleVal);
		ret.put("stringVal", stringVal);
		return ret;
	}

	//------------------------------------------
	//
	// MULTIPLE
	//
	//------------------------------------------
	@Test
	public void testMultiple(){
		String countIntVal = Aggregation.COUNT+"(intVal)";
		String maxIntVal = Aggregation.MAX+"(intVal)";
		String minDoubleVal = Aggregation.MIN+"(doubleVal)";
		String sumDoubleVal = Aggregation.SUM+"(doubleVal)";
		String avgStringVal = Aggregation.AVG+"(stringVal)";

		String[] aggTermsAndArgs = new String[] {countIntVal, maxIntVal, minDoubleVal, sumDoubleVal, avgStringVal};
		Map<String, Object> res = Aggregation.aggregation(aggTermsAndArgs, fullDataSet);

		assertEquals(10, res.get(countIntVal));
		assertEquals(9, ((BigDecimal)res.get(maxIntVal)).intValue());
		assertEquals(0.0, ((BigDecimal)res.get(minDoubleVal)).doubleValue(), 0);
		assertEquals(49.5, ((BigDecimal)res.get(sumDoubleVal)).doubleValue(), 0);
		assertEquals(4.5, ((BigDecimal)res.get(avgStringVal)).doubleValue(), 0);
	}

	//------------------------------------------
	//
	// COUNT
	//
	//------------------------------------------
	@Test
	public void testCountIntVal(){
		String aggTermAndArg = Aggregation.COUNT+"(intVal)";
		Map<String, Object> res = Aggregation.aggregation(new String[]{ aggTermAndArg }, fullDataSet);
		assertEquals(10, res.get(aggTermAndArg));
	}

	@Test
	public void testCountDoubleVal(){
		String aggTermAndArg = Aggregation.COUNT+"(doubleVal)";
		Map<String, Object> res = Aggregation.aggregation(new String[]{ aggTermAndArg }, fullDataSet);
		assertEquals(10, res.get(aggTermAndArg));
	}

	@Test
	public void testCountStringVal(){
		String aggTermAndArg = Aggregation.COUNT+"(stringVal)";
		Map<String, Object> res = Aggregation.aggregation(new String[]{ aggTermAndArg }, fullDataSet);
		assertEquals(10, res.get(aggTermAndArg));
	}

	//------------------------------------------
	//
	// MAX
	//
	//------------------------------------------
	@Test
	public void testMaxIntVal(){
		String aggTermAndArg = Aggregation.MAX+"(intVal)";
		Map<String, Object> res = Aggregation.aggregation(new String[]{ aggTermAndArg }, fullDataSet);

		BigDecimal val = (BigDecimal)res.get(aggTermAndArg);
		assertEquals(9, val.intValue());
		assertEquals(9.0, val.doubleValue(), 0);
	}

	@Test
	public void testMaxDoubleVal(){
		String aggTermAndArg = Aggregation.MAX+"(doubleVal)";
		Map<String, Object> res = Aggregation.aggregation(new String[]{ aggTermAndArg }, fullDataSet);

		BigDecimal val = (BigDecimal)res.get(aggTermAndArg);
		assertEquals(9.9, val.doubleValue(), 0);
	}

	@Test
	public void testMaxStringVal(){
		String aggTermAndArg = Aggregation.MAX+"(stringVal)";
		Map<String, Object> res = Aggregation.aggregation(new String[]{ aggTermAndArg }, fullDataSet);

		BigDecimal val = (BigDecimal)res.get(aggTermAndArg);
		assertEquals(9, val.intValue());
		assertEquals(9.0, val.doubleValue(), 0);
	}

	//------------------------------------------
	//
	// MIN
	//
	//------------------------------------------
	@Test
	public void testMinIntVal(){
		String aggTermAndArg = Aggregation.MIN+"(intVal)";
		Map<String, Object> res = Aggregation.aggregation(new String[]{ aggTermAndArg }, fullDataSet);

		BigDecimal val = (BigDecimal)res.get(aggTermAndArg);
		assertEquals(0, val.intValue());
	}

	@Test
	public void testMinDoubleVal(){
		String aggTermAndArg = Aggregation.MIN+"(doubleVal)";
		Map<String, Object> res = Aggregation.aggregation(new String[]{ aggTermAndArg }, fullDataSet);

		BigDecimal val = (BigDecimal)res.get(aggTermAndArg);
		assertEquals(0.0, val.doubleValue(), 0);
	}

	@Test
	public void testMinStringVal(){
		String aggTermAndArg = Aggregation.MIN+"(stringVal)";
		Map<String, Object> res = Aggregation.aggregation(new String[]{ aggTermAndArg }, fullDataSet);

		BigDecimal val = (BigDecimal)res.get(aggTermAndArg);
		assertEquals(0, val.intValue());
		assertEquals(0.0, val.doubleValue(), 0);
	}

	//------------------------------------------
	//
	// AVG
	//
	//------------------------------------------
	@Test
	public void testAvgInt(){
		String aggTermAndArg = Aggregation.AVG+"(intVal)";
		Map<String, Object> res = Aggregation.aggregation(new String[]{ aggTermAndArg }, fullDataSet);

		BigDecimal val = (BigDecimal)res.get(aggTermAndArg);
		assertEquals(4.5, val.doubleValue(), 0);
	}

	@Test
	public void testAvgDouble(){
		String aggTermAndArg = Aggregation.AVG+"(doubleVal)";
		Map<String, Object> res = Aggregation.aggregation(new String[]{ aggTermAndArg }, fullDataSet);

		BigDecimal val = (BigDecimal)res.get(aggTermAndArg);
		assertEquals(4.95, val.doubleValue(), 0);
	}

	@Test
	public void testAvgString(){
		String aggTermAndArg = Aggregation.AVG+"(stringVal)";
		Map<String, Object> res = Aggregation.aggregation(new String[]{ aggTermAndArg }, fullDataSet);

		BigDecimal val = (BigDecimal)res.get(aggTermAndArg);
		assertEquals(4.5, val.doubleValue(), 0);
	}

	//------------------------------------------
	//
	// SUM
	//
	//------------------------------------------
	@Test
	public void testSumInt(){
		String aggTermAndArg = Aggregation.SUM+"(intVal)";
		Map<String, Object> res = Aggregation.aggregation(new String[]{ aggTermAndArg }, fullDataSet);

		BigDecimal val = (BigDecimal)res.get(aggTermAndArg);
		assertEquals(45, val.intValue());
	}

	@Test
	public void testSumDouble(){
		String aggTermAndArg = Aggregation.SUM+"(doubleVal)";
		Map<String, Object> res = Aggregation.aggregation(new String[]{ aggTermAndArg }, fullDataSet);

		BigDecimal val = (BigDecimal)res.get(aggTermAndArg);
		assertEquals(49.5, val.doubleValue(), 0);
	}

	@Test
	public void testSumString(){
		String aggTermAndArg = Aggregation.SUM+"(stringVal)";
		Map<String, Object> res = Aggregation.aggregation(new String[]{ aggTermAndArg }, fullDataSet);

		BigDecimal val = (BigDecimal)res.get(aggTermAndArg);
		assertEquals(45, val.intValue());
		assertEquals(45.0, val.doubleValue(), 0);
	}
}
