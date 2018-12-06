package aos.common;
import org.w3c.dom.Element;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

import javax.servlet.AsyncContext;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import aos.listeners.ServerListener;
import sun.net.www.protocol.http.HttpURLConnection;


public class LoadBalancer implements Runnable{

	int Result;
	String Operation;
	private AsyncContext asyncContext;
	
	// Method to process a raq request from a client
	// Takes in an integer and then find the server with the lowest load to process
	// Sends a process request and then returns the result from that server
	public LoadBalancer(int startingValue, String oper,AsyncContext asynContext) {
		this.asyncContext=asynContext;
		Result=0;
		Operation = oper;

	}
	/**
	 * Method to return the Location of the WSDL to perform a soap call with given operations
	 * 
	 * @param wsdl
	 * @return
	 * @throws ParserConfigurationException
	 * @throws MalformedURLException
	 * @throws SAXException
	 * @throws IOException
	 */
	public String returnIP(String wsdl) throws ParserConfigurationException, MalformedURLException, SAXException, IOException {
		String url = "";
		// XML parser via DocumentBuilderFactory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document xml = db.parse(new URL(wsdl).openStream());
		NodeList list = xml.getElementsByTagName("wsdlsoap:address");
		// https://alvinalexander.com/blog/post/java/how-to-call-web-service-from-browser
		NodeList oper_list = xml.getElementsByTagName("wsdl:operation");
		TreeSet<String> unique = new TreeSet<String>();
		
		
		for(int i = 0; i < oper_list.getLength(); i++) {
			Node curr_op = oper_list.item(i);
			System.out.println("CURR ELEMENT: "+ curr_op.getNodeName());
			Element e = (Element)curr_op;
			unique.add(e.getAttribute("name"));
			
		}
		
		if(list.getLength() > 0 && unique.contains(this.Operation)) {
			Element curr = (Element) list.item(0);
			url = curr.getAttribute("location");
		}
		
		return url;
	}
	/**
	 * Iterates over a list of servers with a given operation
	 * @return
	 */
	private ArrayList<String> findLowestServerLoads() {

		ArrayList<String> toReturn = null;
		// Get the current WSDLContainer and iterate over the loads of each 
		HashMap<String, Integer> currentLoads = WSDLContainer.getInstance().getAll();
		// Find the smallest load and parse the WSDL for the information to contact the server
		int min = Integer.MAX_VALUE;
		if(currentLoads.size()>0) {
			toReturn=new ArrayList<String>();
			//TODO:  If you want to find the different services offered by a servlet you can use ADDService look for wsdl:operation
			try {
				for (String key : currentLoads.keySet()) {
	
					String url = this.returnIP(key);
					// https://www.baeldung.com/java-http-request
					
					
					URL connection = new URL(url+"/myLoad");
					HttpURLConnection con = (HttpURLConnection) connection.openConnection();
					con.setRequestMethod("GET");
	
					int response = Integer.parseInt(con.getResponseMessage());
					currentLoads.put(url, response);
					
					// This allows for multiple servers to have the same load and to recieve the request
					if (response == min) {
						toReturn.add(key);
					}
					
					if (response < min) {
						min = response;
						toReturn = new ArrayList<String>();
						toReturn.add(key);
					}
					
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return toReturn;
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.print("New Thread is spawning");

		ArrayList<String> lowestServer = this.findLowestServerLoads();
		// Get a response back from the server
		if(lowestServer!=null) {
			try {
				for (String server : lowestServer) {
					String serverAddress = returnIP(server);
					URL connection = new URL(serverAddress+"/add");
					HttpURLConnection con = (HttpURLConnection) connection.openConnection();
					con.setRequestMethod("GET");
					//TODO:  MAKE A HTTP REQUEST FOR add
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Make a call to the lowest server with the starting value
			// TODO: how do you make SOAP calls given the URL?
			// Set the result to what the server returns
		}
		PrintWriter out;
		try {
			ServletResponse response = this.asyncContext.getResponse();
	        response.setContentType("text/plain");
			out = response.getWriter();
			out.println("SUCCESS: " + 5);
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
	}
}
