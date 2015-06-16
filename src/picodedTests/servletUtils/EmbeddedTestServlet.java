package picodedTests.servletUtils;

import java.io.IOException;
import java.util.Date;
 

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class EmbeddedTestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
 
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		String getValue = req.getParameter("getValue");
		System.out.println("Get Request param : " +getValue);
		resp.getWriter().append(getValue);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
		String postValue = req.getParameter("postValue");
		System.out.println("Post Request param : " +postValue);
		resp.getWriter().append(postValue);
	}
}