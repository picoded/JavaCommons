package picodedTests.RESTBuilder.templates;

import static org.junit.Assert.*;

import org.junit.*;

import java.io.*;
import java.util.*;
import java.io.IOException;

import org.apache.catalina.LifecycleException;
import org.apache.commons.io.IOUtils;

import com.amazonaws.util.StringUtils;

import picoded.conv.ConvertJSON;
import picoded.conv.GUID;
import picoded.fileUtils.FileUtils;
import picoded.servletUtils.EmbeddedServlet;
import picoded.struct.GenericConvertMap;
import picoded.struct.ProxyGenericConvertMap;
import picoded.RESTBuilder.*;
import picoded.JStack.*;
import picoded.JStruct.*;
import picoded.RESTBuilder.templates.*;
import picoded.webUtils.*;

public class MetaTableApiBuilder_test {

	private static String dummyDataFilePath = "./test-files/test-specific/MetaTable/MetaTableApiBuilderDummyData.json";
	private static Map<String, Object> dummyDataMap = null;
	private static List<String> _oids = null;
	
	/// Test object
	private static MetaTable mtObj = null;
	private static MetaTableApiBuilder mtApi = null;
			
	/// To override for implementation
	///------------------------------------------------------
	private static MetaTable implementationConstructor() {
		return (new JStruct()).getMetaTable("test");
	}
	
	private static void populateMetaTableDummyData(int max, int min){
		Random rnd = new Random();
		int _max = rnd.nextInt(max);
		_max = _max > min ? _max : min;
		
		_oids = new ArrayList<String>();
		
		for(int i = 0; i < _max; ++i){
			String oid = GUID.base58();
			_oids.add(oid);
			
			Map<String, Object> innerObj = new HashMap<String, Object>();
			innerObj.put("_oid", oid);
			innerObj.put("_name", "name"+i);
			innerObj.put("_age", "age"+i);
			
			mtObj.append(oid,  innerObj).saveAll();
		}
	}
	
	@BeforeClass
	public static void setup(){
		mtObj = implementationConstructor();
		mtObj.systemSetup();
		populateMetaTableDummyData(10, 5);
		
		mtApi = new MetaTableApiBuilder(mtObj);
	}
	
	@AfterClass
	public static void tearDown() {
		if( mtObj != null ) {
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
	public void list_GET_and_POST_test(){
		String[] oidArgs = new String[]{"_oid"};
		String[] nameArgs = new String[]{"_name"};
		String[] ageArgs = new String[]{"_age"};
		
		List<List<Object>> oidData = mtApi.list_GET_and_POST_inner(0,  0,  0,  oidArgs,  "", null, "_oid");
		assertNotNull(oidData);
		
		List<List<Object>> nameData = mtApi.list_GET_and_POST_inner(0,  0,  0,  nameArgs,  "",  null, "_oid");
		assertNotNull(nameData);
		
		List<List<Object>> ageData = mtApi.list_GET_and_POST_inner(0,  0,  0,  ageArgs,  "",  null, "_oid");
		assertNotNull(ageData);
		
		String[] oidNameArgs = new String[]{"_oid", "_name"};
		String[] nameAgeArgs = new String[]{"_name", "_age"};
		String[] allArgs = new String[]{"_oid", "_name", "_age"};
		
		List<List<Object>> oidNameData = mtApi.list_GET_and_POST_inner(0,  0,  0,  oidNameArgs,  "",  null, "_oid");
		assertNotNull(oidNameData);
		
		List<List<Object>> nameAgeData = mtApi.list_GET_and_POST_inner(0,  0,  0,  nameAgeArgs,  "",  null, "_oid");
		assertNotNull(nameAgeData);
		
		List<List<Object>> allData = mtApi.list_GET_and_POST_inner(0,  0,  0,  allArgs,  "",  null, "_oid");
		assertNotNull(allData);
		
		List<List<Object>> allDataWithQueryFilter = mtApi.list_GET_and_POST_inner(0,  0,  0,  allArgs,  "_name=? OR _age=?",  new String[]{"name1", "age4"}, "");
		assertNotNull(allDataWithQueryFilter);
	}
	
	@Test
	public void meta_GET_test(){
		for(String oid:_oids){
			MetaObject mObj = mtApi.meta_GET_inner(oid);
			assertNotNull(mObj);
		}
	}
	
	@Test
	public void meta_POST_test(){
		//test delta
		Map<String, Object> metaTableDelta = new HashMap<String, Object>();
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
	
}
