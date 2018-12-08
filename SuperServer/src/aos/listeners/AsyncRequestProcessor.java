package aos.listeners;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.AsyncContext;
import javax.servlet.ServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import aos.common.ExecuteWSThread;
import aos.common.WSDLContainer;
import sun.net.www.protocol.http.HttpURLConnection;

public class AsyncRequestProcessor implements Runnable {

	private AsyncContext asyncContext;
	private int secs;
	int Result=1;
	String Operation="add";
	
	private String myloadmessage="<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\"><Body><myload xmlns=\"http://aos\"/></Body></Envelope>";
	private String addmessage="<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\"><Body><add xmlns=\"http://aos\"/></Body></Envelope>";
	
	private Pattern loadpattern = Pattern.compile("<myloadReturn xsi:type=\"xsd:int\">(.+?)</myloadReturn>", Pattern.DOTALL);
	private Pattern addpattern = Pattern.compile("<addReturn>(.+?)</addReturn>", Pattern.DOTALL);

	private String ws_response=null; 
	private boolean responseFlag=false;
	
	public AsyncRequestProcessor() {
	}

	public AsyncRequestProcessor(AsyncContext asyncCtx, int secs) {
		this.asyncContext = asyncCtx;
		this.secs = secs;
	}

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
			con.setConnectTimeout(2*60*1000);
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
					
					// This allows for multiple servers to have the same load and to recieve the request
					
					//Write a logic to return more than 1 WS servers in the list
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
		
		//In this one needs to spawn new threads 
		
		System.out.println("Async Supported? "
				+ asyncContext.getRequest().isAsyncSupported());
		

		ArrayList<String> lowestServer = this.findLowestServerLoads();
		// Get a response back from the server
		if(lowestServer!=null) {
			try {
				final CountDownLatch latch = new CountDownLatch(1);
				for (String server : lowestServer) {
					String serverAddress = returnIP(server);
					//AsyncContext asyncContextObj = this.asyncContext.getRequest().startAsync();
					
					//This part needs to be handled -- Spawn a new thread and then capture the first response only
					
					
					
					//Thread t= new Thread(new ExecuteWSThread(serverAddress,this.addmessage,"add"));
					//t.start();
					
					new Thread(new Runnable() {
					    @Override
					    public void run() {
					        //4
					        //do your logic here in thread#2

							StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
							URL url;
							try {
								url = new URL(serverAddress);
								HttpURLConnection con=(HttpURLConnection)url.openConnection();
								
								con.setRequestMethod("POST");
								con.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
								con.setRequestProperty("SOAPAction", "add");
								con.setDoOutput(true);
								
								DataOutputStream wr = new DataOutputStream (
								        con.getOutputStream());
								    wr.writeBytes(addmessage);
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

					    	String res=response.toString();
							System.out.println("This time the response for add "+res);
							Matcher matcher = addpattern.matcher(res);
							matcher.find();
							
							ws_response=matcher.group(1);
							
					        //then release the lock
					        //5
					        latch.countDown();
					    }
					}).start();
					
					try {
					    //3 this method will block the thread of latch untill its released later from thread#2
					    latch.await();
					} catch (InterruptedException e) {
					    e.printStackTrace();
					}
				
				}
				
				System.out.println("My thread has completed and hence, I reach here. -- Maybe I reach here on first thread completion "+ws_response);
				if(!this.responseFlag) {
					//It is false... first timer.. send response
					//this.asyncContext.getRes
					
					ServletResponse response = this.asyncContext.getResponse();
			        response.setContentType("text/plain");
			        PrintWriter out = response.getWriter();
					out.println(this.ws_response);
					out.flush();
					out.close();
					
					this.responseFlag=true;
				}else {
					//Its over.. becoz others have sent the rezponse before 
					System.out.println("Doing nothing");
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

		
		//longProcessing(secs);
		/*try {
			PrintWriter out = asyncContext.getResponse().getWriter();
			out.write("Processing done for " + secs + " milliseconds!!");
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		//complete the processing
		asyncContext.complete();
	}

	private void longProcessing(int secs) {
		// wait for given time before finishing
		try {
			Thread.sleep(secs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}