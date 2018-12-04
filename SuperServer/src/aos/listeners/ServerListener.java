package aos.listeners;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import javax.servlet.ServletConfig;
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
    
	private String wsdl_register;
	public static WSDLContainer wsdlConObjLocal=null;
	
    /**
     * @see HttpServlet#HttpServlet()
     * 
     */
    public ServerListener() {
        super();
        // TODO Auto-generated constructor stub       
        this.wsdlConObjLocal = new WSDLContainer();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.getWriter().append("Served at: ").append(request.getContextPath());
		
		System.out.println("I get an incoming request from server -- need to extract the WSDL and save it");
		response.setContentType("text/html;charset=UTF-8");
		
		String server_wsdl_url=request.getParameter("WSDL");
		
		this.wsdlConObjLocal.add(server_wsdl_url, 0);
		
		//Save this somewhere and persist it somewhere
		response.getWriter().write("SUCCESS");
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	

	@Override
	public void destroy() {
	// TODO Auto-generated method stub
		
		// write object to file
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(this.wsdl_register);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this.wsdlConObjLocal);
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		this.wsdl_register=config.getInitParameter("registryfile");
		//Load the object 
		FileInputStream fis;
		try {
			fis = new FileInputStream(this.wsdl_register);
			ObjectInputStream ois = new ObjectInputStream(fis);
			this.wsdlConObjLocal = (WSDLContainer) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			this.wsdlConObjLocal = WSDLContainer.getInstance();
			e.printStackTrace();
		}
	
		
	}

	
	

}
