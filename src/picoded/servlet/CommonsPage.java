package picoded.servlet;

import java.util.*;

public class CommonsPage extends BasePage{
  @Override
	public void initializeContext() throws Exception {
		super.initializeContext();
		// boolean skipSystemSetup = DConfig().getBoolean("sys.DStack.skipSystemSetup", false);
		// if (!skipSystemSetup) {
		// 	DStack().systemSetup();
		// } else {
		// 	System.out.println("Skipping systemSetup in JStackPage");
		// }
	}

	@Override
	public boolean doAuth(Map<String, Object> templateData) throws Exception {
		return super.doAuth(templateData);
	}
}
