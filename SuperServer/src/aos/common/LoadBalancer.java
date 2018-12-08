package aos.common;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import com.sun.jmx.snmp.Timestamp;

import aos.listeners.ServerListener;
import sun.net.www.protocol.http.HttpURLConnection;


public class LoadBalancer implements Runnable{

	int Result;
	String Operation;
	private AsyncContext asyncContext;
	private int serverRequestType = 0;  // 1 = send message to WS and SS | 0 is only send to SS
	private String myURL="";

	private String myloadmessage="<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\"><Body><myload xmlns=\"http://aos\"/></Body></Envelope>";
	private String addmessage="<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\"><Body><add xmlns=\"http://aos\"/></Body></Envelope>";

	private Pattern loadpattern = Pattern.compile("<myloadReturn xsi:type=\"xsd:int\">(.+?)</myloadReturn>", Pattern.DOTALL);

	private ArrayList<String> LoadBalancerRecords;// Format of this string is LOADBALANCER | Message Type | Timestamp | Message 

	// Method to process a raq request from a client
	// Takes in an integer and then find the server with the lowest load to process
	// Sends a process request and then returns the result from that server
	public LoadBalancer(int startingValue, String oper,AsyncContext asynContext) {
		this.asyncContext=asynContext;
		Result=0;
		Operation = oper;
		this.serverRequestType = startingValue;
		this.LoadBalancerRecords = new ArrayList<String>();
		this.LoadBalancerRecords.add("LOADBALANCER | Creating | " + (new Timestamp(System.currentTimeMillis())) + " | " + this.asyncContext.getRequest() );

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


	private String communicateWS(String urlStr,String message,String action) {
		StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
		URL url;
		try {
			url = new URL(urlStr);
			HttpURLConnection con=(HttpURLConnection)url.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
			con.setRequestProperty("SOAPAction", action);
			con.setDoOutput(true);

			DataOutputStream wr = new DataOutputStream (
					con.getOutputStream());
			wr.writeBytes(message);
			wr.close();
			//Get Response  
			InputStream is = con.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));

			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			System.out.println("OUTPUT "+response.toString());
			con.disconnect();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response.toString();

	}
	/**
	 * Iterates over a list of servers with a given operation
	 * @return
	 */
	private ArrayList<String> findLowestServerLoads() {

		ArrayList<String> toReturn = null;
		// Get the current WSDLContainer and iterate over the loads of each 
		HashMap<String, Integer> currentLoads = WSDLContainer.getInstance().getAll();
		HashMap<Integer, ArrayList<String>> toSort = new HashMap<Integer,ArrayList<String>>();
		// Find the smallest load and parse the WSDL for the information to contact the server
		int min = Integer.MAX_VALUE;
		if(currentLoads.size()>0) {
			toReturn=new ArrayList<String>();
			//TODO:  If you want to find the different services offered by a servlet you can use ADDService look for wsdl:operation
			try {
				for (String key : currentLoads.keySet()) {

					String url = this.returnIP(key);
					// https://www.baeldung.com/java-http-request

					System.out.println("This is the load url to hit "+url);
					String res=communicateWS( url,this.myloadmessage,"myload");

					//URL connection = new URL(url+"/myLoad");
					//HttpURLConnection con = (HttpURLConnection) connection.openConnection();
					//con.setRequestMethod("GET");

					//Parse and figure the actual val returned
					Matcher matcher = loadpattern.matcher(res);
					matcher.find();
					//System.out.println();

					//int response = Integer.parseInt(con.getResponseMessage());
					int response = Integer.parseInt(matcher.group(1));
					System.out.println("What did i get for my load "+response);

					currentLoads.put(url, response);

					int load = currentLoads.get(key);
					if(toSort.get(load).isEmpty()) {
						toSort.put(load, new ArrayList<String>());
					}
					toSort.get(load).add(key);
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
			TreeSet<Integer> sortedSet = new TreeSet<Integer>();
			ArrayList<String> sortedLoads = new ArrayList<String>();
			sortedSet.addAll(toSort.keySet());
			for (Integer currKey : sortedSet) {
				sortedLoads.addAll(toSort.get(currKey));
			}

			if (sortedLoads.size() > 3) {
				for (int i = 0; i < 3; i++ ) {
					toReturn.add(sortedLoads.get(i));
				}
			}
			else 
				return sortedLoads;
		}
		return toReturn;

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.print("New Thread is spawning");


		// Send a request to all the WS
		ArrayList<String> lowestServer = this.findLowestServerLoads();
		// Get a response back from the server
		if(lowestServer!=null) {
			try {
				for (String server : lowestServer) {
					String serverAddress = returnIP(server);

					new Thread(new ExecuteWSThread(serverAddress,this.addmessage,this.asyncContext,"add")).start();

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

			// If the server request type is from another SS Process the SUPERSERver list and sent the request to them
			if(this.serverRequestType == 0) {

				//TODO: create the list of SS
				String superserver_list="/home/siddiqui/aos/ws_resolvers.txt";

				LinkedList<String> superserver_locs=null;

				try {
					File file = new File(superserver_list);
					FileReader fileReader = new FileReader(file);
					BufferedReader bufferedReader = new BufferedReader(fileReader);
					StringBuffer stringBuffer = new StringBuffer();
					String line;
					superserver_locs=new LinkedList<String>();

					while ((line = bufferedReader.readLine()) != null) {
						superserver_locs.add(line);
					}
					fileReader.close();

					System.out.println("Contents of file:"+superserver_locs);

				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("Superserver list for client is an issue");
					System.exit(0);
				}

				//Got the list of all superserver.. Iterate and call them -- then implement more complicated logic
				if(superserver_locs!=null && superserver_locs.size()>0) {
					URL url=null;
					HttpURLConnection con=null;
					for (int i=0;i<superserver_locs.size();i++) {
						//Iterating each of the superservers now
						try {
							String url_ss=superserver_locs.get(i);

							if(url_ss.equals(this.myURL)) {
								url_ss+="?client=1";

								//System.out.println("Using the url "+url_ss);
								url= new URL(url_ss);
								con=(HttpURLConnection)url.openConnection();
								con.setRequestMethod("GET");

								//Sends the message to the SS
								new Thread(new ExecuteWSThread(url_ss,this.addmessage,this.asyncContext,"add")).start();

								InputStream isi = con.getInputStream();
								InputStreamReader isr = new InputStreamReader(isi);
								BufferedReader in = new BufferedReader(isr);
								String str;
								StringBuilder sb = new StringBuilder();


								while((str = in.readLine()) != null){
									sb.append(str);
									sb.append("\n");
								}
								//content.append(inputLine);
								in.close();
								isr.close();
								isi.close();
								con.disconnect();

								System.out.println("I got response from SS:"+sb.toString());
							}
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}

					}
				}
			}
			// Make a call to the lowest server with the starting value
			// TODO: how do you make SOAP calls given the URL?
			// Set the result to what the server returns

		}


	}
}
