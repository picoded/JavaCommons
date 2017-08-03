package picoded.servlet.util;

import static org.junit.Assert.*;
import picoded.dstack.*;

import picoded.servlet.util.EmbeddedServlet;
import picoded.TestConfig;

public class DStackConfigLoad_test{

  public static class DStackPage_test extends CorePage {
    @Override
    public void doSharedSetup() throws Exception {

    }
    @Override
    public void initializeContext() throws Exception {
      super.initializeContext();
      DConfig dconfig = null;
  		if ((new File(getConfigsPath())).exists()) {
  			dconfig = new DConfig(getConfigsPath());
  		} else {
  			dconfig = new DConfig();
  		}

      List<Object> stackOptions = dconfig.getObjectList("sys.DStack.stack", null);
      if (stackOptions != null)
        DStackConfigLoader.generateDStack(stackOptions).;
    }
  }

  public CorePage setupServlet(){
    return new DStackPage_test();
  }

	//
	// Standard setup and teardown
	//
	@Before
	public void setUp() {
		// Setup the servlet, this will call the required builder setup
		testPort = TestConfig.issuePortNumber();
		testPage = setupServlet();
		testServlet = new EmbeddedServlet(testPort, testPage);
		testBaseUrl = "http://localhost:" + testPort + "/api/";
	}

  @Test
  public void testDStackConfig() {
    DConfig();
  }


}
