package aos.listeners;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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

public class ServerListener extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private String wsdl_register;
	public WSDLContainer wsdlConObjLocal=null;
	public ArrayList<String> ServerRecords;
	
    /**
     * @see HttpServlet#HttpServlet()
     * 
     */
    public ServerListener() {
        super();
        // TODO Auto-generated constructor stub       
        //this.wsdlConObjLocal = new WSDLContainer();
        //this.ServerRecords = new ArrayList<String>();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//this.ServerRecords.add("SERVER | Recieved from Server | " + (System.currentTimeMillis()) +" | " + response);
		
		response.getWriter().append("Served at: ").append(request.getContextPath());
		System.out.println("I get an incoming request from server -- need to extract the WSDL and save it");
		response.setContentType("text/html;charset=UTF-8");
		String server_wsdl_url=request.getParameter("WSDL");
		System.out.println("Got the wsdl as "+server_wsdl_url);
		this.wsdlConObjLocal.add(server_wsdl_url);
		
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
		// write object to file
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(this.wsdl_register);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this.wsdlConObjLocal);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		this.wsdl_register=config.getInitParameter("registryfile");
		System.out.println("got the path to wsdl file "+this.wsdl_register);
		//Load the object 
		FileInputStream fis;
		try {
			fis = new FileInputStream(this.wsdl_register);
			ObjectInputStream ois = new ObjectInputStream(fis);
			this.wsdlConObjLocal = (WSDLContainer) ois.readObject();
			System.out.println("this is the table "+this.wsdlConObjLocal.getAll().size());
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Error ");
			this.wsdlConObjLocal = WSDLContainer.getInstance();
			//e.printStackTrace();
		}
	
		
	}
}
