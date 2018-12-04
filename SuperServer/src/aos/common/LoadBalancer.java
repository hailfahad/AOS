package aos.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import aos.listeners.ServerListener;


public class LoadBalancer implements Runnable{

	int Result;
	// Method to process a raq request from a client
	// Takes in an integer and then find the server with the lowest load to process
	// Sends a process request and then returns the result from that server
	public LoadBalancer(int startingValue) {
		
		Result=0;
		
		String lowestServer = this.findLowestServerLoad();
		// Get the IP of the lowest Server
		try {
			String serverAddress = returnIP(lowestServer);
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
	private String returnIP(String wsdl) throws ParserConfigurationException, MalformedURLException, SAXException, IOException {
		String toReturn = "";

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document xml = db.parse(new URL(wsdl).openStream());
		toReturn = xml.getElementById("wsdl:port").getAttribute("binding");
		
		return toReturn;
	}
	
	public String findLowestServerLoad() {
		String toReturn = "";
		// Get the current WSDLContainer and iterate over the loads of each 
		HashMap<String, Integer> currentLoads = WSDLContainer.getInstance().getAll();
		// Find the smallest load and parse the WSDL for the information to contact the server
		int min = Integer.MAX_VALUE;
		for (String key : currentLoads.keySet()) {
			int currVal = currentLoads.get(key);
			if (currVal < min) {
				min = currVal;
				toReturn = key;
			}
		}
		return toReturn;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.print("New Thread is spawning");
	}
}
