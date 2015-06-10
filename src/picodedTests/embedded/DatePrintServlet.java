package picodedTests.embedded;

import java.io.IOException;
import java.util.Date;
 
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DatePrintServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
 
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
		res.getWriter().append(String.format("It's %s now\n",new Date()));
		String userName = req.getParameter("user");
		
		if(userName.equals("Sam")){
			res.getWriter().append("Correct login");
		} else {
			res.getWriter().append("WRONG LOGIN");
		}
	}
}