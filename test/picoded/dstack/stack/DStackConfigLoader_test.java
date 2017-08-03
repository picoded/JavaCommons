package picoded.dstack.stack;

import static org.junit.Assert.*;
import org.junit.*;

import java.io.File;

import picoded.dstack.*;
import picoded.servlet.util.EmbeddedServlet;
import picoded.TestConfig;
import picoded.file.FileUtil;

public class DStackConfigLoader_test{

	//
	// The test folders to use
	//
	File testCollection = new File("./test-files/test-specific/dstack/stack/DStackConfigLoader/");
	
	/**
	 * Load the defined file from the test folder, and returns its string value
	 * 
	 * @param  filename to load, this automatically includes the testCollection file path
	 * 
	 * @return  the file content, if not null
	 */
	public String getTestConfigFile(String filename) {
		String ret = FileUtil.readFileToString(new File(testCollection, filename));
		assertNotNull(ret);
		return ret;
	}

	@Test
	public void testDStackConfig() {
		DStack dstack = DStackConfigLoader.generateDStack( getTestConfigFile("singleLayerDStruct.json"));
		assertNotNull(dstack);

		// Generate a table object, to force stack validation
		dstack.getMetaTable("test");
	}


}
