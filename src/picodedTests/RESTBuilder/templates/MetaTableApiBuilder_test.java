package picodedTests.RESTBuilder.templates;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import picoded.JStruct.JStruct;
import picoded.JStruct.MetaObject;
import picoded.JStruct.MetaTable;
import picoded.RESTBuilder.templates.MetaTableApiBuilder;
import picoded.conv.GUID;

public class MetaTableApiBuilder_test {
	private static List<String> _oids = null;
	
	/// Test object
	private static MetaTable mtObj = null;
	private static MetaTableApiBuilder mtApi = null;
	
	/// To override for implementation
	///------------------------------------------------------
	private static MetaTable implementationConstructor() {
		return (new JStruct()).getMetaTable("test");
	}
	
	private static void populateMetaTableDummyData(int min, int max) {
		Random rnd = new Random();
		int _max = rnd.nextInt(max);
		_max = _max > min ? _max : min;
		
		_oids = new ArrayList<String>();
		
		for (int i = 0; i < _max; ++i) {
			String oid = GUID.base58();
			_oids.add(oid);
			
			Map<String, Object> innerObj = new HashMap<String, Object>();
			innerObj.put("_oid", oid);
			innerObj.put("_name", "name" + i);
			innerObj.put("_age", "age" + i);
			
			mtObj.append(oid, innerObj).saveAll();
		}
	}
	
	@BeforeClass
	public static void setup() {
		mtObj = implementationConstructor();
		mtObj.systemSetup();
		populateMetaTableDummyData(4, 4);
		
		mtApi = new MetaTableApiBuilder(mtObj);
	}
	
	@AfterClass
	public static void tearDown() {
		if (mtObj != null) {
			mtObj.systemTeardown();
		}
		mtObj = null;
	}
	
	@Test
	public void constructorTest() {
		//not null check
		assertNotNull(mtObj);
		//run maintaince, no exception?
		// mtObj.maintenance();
	}
	
	@Test
	public void list_GET_and_POST_test() {
		String[] oidArgs = new String[] { "_oid" };
		String[] nameArgs = new String[] { "_name" };
		String[] ageArgs = new String[] { "_age" };
		
		List<List<Object>> oidData = mtApi.list_GET_and_POST_inner(0, 0, 0, oidArgs, "", null, "_oid", false);
		assertNotNull(oidData);
		
		List<List<Object>> nameData = mtApi.list_GET_and_POST_inner(0, 0, 0, nameArgs, "", null, "_oid", false);
		assertNotNull(nameData);
		
		List<List<Object>> ageData = mtApi.list_GET_and_POST_inner(0, 0, 0, ageArgs, "", null, "_oid", false);
		assertNotNull(ageData);
		
		String[] oidNameArgs = new String[] { "_oid", "_name" };
		String[] nameAgeArgs = new String[] { "_name", "_age" };
		String[] allArgs = new String[] { "_oid", "_name", "_age" };
		
		List<List<Object>> oidNameData = mtApi.list_GET_and_POST_inner(0, 0, 0, oidNameArgs, "", null, "_oid", false);
		assertNotNull(oidNameData);
		
		List<List<Object>> nameAgeData = mtApi.list_GET_and_POST_inner(0, 0, 0, nameAgeArgs, "", null, "_oid", false);
		assertNotNull(nameAgeData);
		
		List<List<Object>> allData = mtApi.list_GET_and_POST_inner(0, 0, 0, allArgs, "", null, "_oid", false);
		assertNotNull(allData);
		
		List<List<Object>> allDataWithQueryFilter = mtApi.list_GET_and_POST_inner(0, 0, 0, allArgs, "_name=? OR _age=?",
			new String[] { "name1", "age4" }, "", false);
		assertNotNull(allDataWithQueryFilter);
		
	}
	
	@Test
	public void list_GET_and_POST_queryTest() {
		//test query start and length
		String[] allArgs = new String[] { "_oid", "_name", "_age" };
		//		List<List<Object>> allData = mtApi.list_GET_and_POST_inner(0, 0, 0, allArgs, null, null, "_oid", false);
		List<List<Object>> allDataWithStartAndLengthFilter = mtApi.list_GET_and_POST_inner(0, 0, 2, allArgs, null, null,
			"_oid", false);
		assertNotNull(allDataWithStartAndLengthFilter);
	}
	
	@Test
	public void meta_GET_test() {
		for (String oid : _oids) {
			MetaObject mObj = mtApi.meta_GET_inner(oid, false);
			assertNotNull(mObj);
		}
	}
	
	@Test
	public void meta_POST_test() {
		//test delta
		//		Map<String, Object> metaTableDelta = new HashMap<String, Object>();
		MetaObject metaObjDelta = mtObj.newObject();
		metaObjDelta.replace("_oid", _oids.get(0));
		metaObjDelta.put("_name", "deltaReplacedName");
		metaObjDelta.put("_age", "deltaReplacedAge");
		MetaObject returnedMObj = mtApi.meta_POST_inner(_oids.get(0), metaObjDelta, "delta");
		assertNotNull(returnedMObj);
		
		//test full
		MetaObject metaObjFull = mtObj.newObject();
		metaObjFull.replace("_oid", _oids.get(1));
		metaObjFull.put("_name", "fullyReplacedName");
		returnedMObj = mtApi.meta_POST_inner(_oids.get(1), metaObjFull, "full");
		assertNotNull(returnedMObj);
		
		//test create
		MetaObject metaObjNew = mtObj.newObject();
		metaObjNew.replace("_oid", "new");
		metaObjNew.put("_name", "newMetaName");
		metaObjNew.put("_age", "newMetaAge");
		returnedMObj = mtApi.meta_POST_inner("new", metaObjNew, null);
		assertNotNull(returnedMObj);
		
	}
	
	@Test
	public void meta_DELETE_test() {
		mtApi.meta_DELETE_inner(_oids.get(0));
		
		assertNotNull(mtApi);
		assertNull(mtApi.meta_GET_inner(String.valueOf(_oids.get(0)), false));
	}
	
	@Test
	public void csv_exportTest() {
		int count = 0;
		List<String> data = new ArrayList<String>();
		
		try {
			FileWriter fw = new FileWriter("./test-files/test-specific/MetaTable/reportsCSV.csv");
			
			while ((data = mtApi.csv_list(0, count, 2, new String[] { "_oid", "_age", "_name" }, null, null, null)).size() >= 2) { //what if data is null?? oh nooooo
				count += data.size();
				for (String str : data) {
					fw.write(str);
					fw.write("\n");
				}
				fw.flush();
			}
		} catch (Exception e) {
			
		}
		
		//		mtApi.csv_export.apply(null, null);
	}
	
}
