package picoded.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;

public class CorePageExtended extends CorePage {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean doPostRequest(Map<String, Object> templateData) throws Exception {
		try {
			responseOutputStream.write(super.getContextURI().getBytes());
		} catch (IOException e) {
			throw new ServletException(e);
		}
		return true;
	}
}
