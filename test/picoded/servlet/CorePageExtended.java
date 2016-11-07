package picoded.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;

public class CorePageExtended extends CorePage {
	
	private static final long serialVersionUID = 1L;
	
	public CorePageExtended() {
		//super();
	}
	
	@Override
	public void initializeContext() throws Exception {
		super.initializeContext();
	}
	
	@Override
	public void doSharedTeardown() throws Exception {
		super.doSharedTeardown();
	}
	
	//	@Override
	//	public boolean processChain() throws ServletException {
	//		httpResponse.setContentType("application/text");
	////		try {
	////			httpResponse.getWriter().append(super.getContextURI());
	////		} catch (IOException e) {
	////			throw new ServletException(e);
	////		}
	//		return true;
	//	}
	
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
