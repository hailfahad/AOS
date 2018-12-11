package aos.listeners;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
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
        this.ServerRecords = new ArrayList<String>();
    }

    public synchronized void appendStuff(String message, String logFile) {
    	try(FileWriter fw = new FileWriter(logFile, true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw))
			{
			    //out.println("SS,ServerListener,"+startingTime+","+request);
    		out.println(message);
			   
			} catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String logFile=request.getServletContext().getInitParameter("logFile");
		
		
		// Storing the records for data usage
		long startingTime=System.currentTimeMillis();
		String msg="SS,ServerListener,start,"+startingTime+","+request;
		appendStuff(msg,  logFile);
		
		/*try {
			FileOutputStream fos;
			fos = new FileOutputStream("..\\..\\..\\ServerRecords.txt");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject("SS | Recieved Request From Server | " + startingTime +" | " + request);
			oos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}*/
				
		response.getWriter().append("Served at: ").append(request.getContextPath());
		System.out.println("I get an incoming request from server -- need to extract the WSDL and save it");
		response.setContentType("text/html;charset=UTF-8");
		String server_wsdl_url=request.getParameter("WSDL");
		System.out.println("Got the wsdl as "+server_wsdl_url);
		this.wsdlConObjLocal.add(server_wsdl_url);
		
		//Save this somewhere and persist it somewhere
		
		msg="SS,ServerListener,end,"+(System.currentTimeMillis())+","+request;
		appendStuff(msg,  logFile);
		
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
