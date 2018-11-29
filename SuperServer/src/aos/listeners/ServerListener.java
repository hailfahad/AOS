package aos.listeners;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import aos.common.WSDLContainer;

/**
 * Servlet implementation class ServerListener
 */
@WebServlet("/ServerListener")
public class ServerListener extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ServerListener() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.getWriter().append("Served at: ").append(request.getContextPath());
		
		System.out.println("I get an incoming request from server -- need to extract the WSDL and save it");
		response.setContentType("text/html;charset=UTF-8");
		
		String server_wsdl_url=request.getParameter("WSDL");
		
		HashMap <String, Integer> globalMap = WSDLContainer.getInstance();
		
		if(globalMap.containsKey(server_wsdl_url)) {
			WSDLContainer.getInstance().put(server_wsdl_url, globalMap.get(server_wsdl_url));
		}else {
			WSDLContainer.getInstance().put(server_wsdl_url,0);
		}
		//Save this somewhere and persist it somewhere
		response.getWriter().write("SUCCESS");
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
