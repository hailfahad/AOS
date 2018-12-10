package aos.listeners;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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

import com.sun.jmx.snmp.Timestamp;

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

	private String myURL = "";
	private int serverRequestType = 0;  // 1 = send message to WS and SS | 0 is only send to SS

	

	public AsyncRequestProcessor(AsyncContext asyncCtx, int secs, int serverRequestType) {
		this.asyncContext = asyncCtx;
		this.secs = secs;
		this.serverRequestType=serverRequestType;
		
		// https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
				try(final DatagramSocket socket = new DatagramSocket())
				{
					socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
					this.myURL = socket.getLocalAddress().getHostAddress();
					System.out.println("What is my URL in the constructor "+this.myURL);
					
				} catch (UnknownHostException e) {
					System.out.println("There is something wrong with my configuration");
					e.printStackTrace();
				} catch (SocketException e1) {
					System.out.println("could not create a new datagram socket");
					e1.printStackTrace();
				}
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
			return null;
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
		ArrayList<String> currentLoads = WSDLContainer.getInstance().getAll();
		
		System.out.println("This is the current load "+currentLoads.size());
		//HashMap<String, Integer> dataLoaded=new HashMap<String, Integer>();
		// Find the smallest load and parse the WSDL for the information to contact the server

		HashMap<Integer, ArrayList<String>> toSort = new HashMap<Integer,ArrayList<String>>();
		int min = Integer.MAX_VALUE;
		if(currentLoads.size()>0) {
			toReturn=new ArrayList<String>();
			//TODO:  If you want to find the different services offered by a servlet you can use ADDService look for wsdl:operation
			try {
				for (String key : currentLoads) {
					String url = this.returnIP(key);
					System.out.println("This is the load url to hit "+url);
					String res=communicateWS( url,this.myloadmessage,"myload");

					if(res!=null) {
						Matcher matcher = loadpattern.matcher(res);
						matcher.find();
						int response = Integer.parseInt(matcher.group(1));
						System.out.println("What did i get for my load "+response);

						toReturn.add(key);
					}
					//URL connection = new URL(url+"/myLoad");
					//HttpURLConnection con = (HttpURLConnection) connection.openConnection();
					//con.setRequestMethod("GET");
					//Parse and figure the actual val returned
					//int response = Integer.parseInt(con.getResponseMessage());

					//currentLoads.put(url, response);
					/*
					int load = dataLoaded.get(key);
					
					if(toSort.get(load).isEmpty()) {
						toSort.put(load, new ArrayList<String>());
					}
					toSort.get(load).add(key);*/
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
			/*TreeSet<Integer> sortedSet = new TreeSet<Integer>();
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
				return sortedLoads;*/
		}
		return toReturn;

	}


	@Override
	public void run() {
		long startTime= System.currentTimeMillis();
		//In this one needs to spawn new threads 

		System.out.println("Async Supported? "
				+ asyncContext.getRequest().isAsyncSupported());


		ArrayList<String> lowestServer = this.findLowestServerLoads();
		System.out.println("Found lowest guys "+lowestServer.size());
		// Get a response back from the server
		if(lowestServer!=null) {
			try {
				final CountDownLatch latch = new CountDownLatch(1);
				for (String server : lowestServer) {
					System.out.println("This si the server "+server);
					String serverAddress = returnIP(server);
					System.out.println("This is the server I want to contact "+serverAddress);
					//AsyncContext asyncContextObj = this.asyncContext.getRequest().startAsync();
					//This part needs to be handled -- Spawn a new thread and then capture the first response only
					//Thread t= new Thread(new ExecuteWSThread(serverAddress,this.addmessage,"add"));
					//t.start();

					new Thread(new Runnable() {
						@Override
						public void run() {
							//4
							//do your logic here in thread#2
							System.out.println("Started thread for WS");
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
					
					//I would need to spawn threads for Calling the other SS and then wait for the first guy
					
					// If the server request type is from another SS Process the SUPERSERver list and sent the request to them
					if(this.serverRequestType == 5) {

						// TODO: create the list of SS change location of 
						// String superserver_list="/home/siddiqui/aos/ws_resolvers.txt";
						
						//This should come from web.xml -- 
						String superserver_list="E:\\git\\AOS\\Try50\\SuperServerList.txt";
						LinkedList<String> superserver_locs=null;

						try {
							File file = new File(superserver_list);
							FileReader fileReader = new FileReader(file);
							BufferedReader bufferedReader = new BufferedReader(fileReader);
							StringBuffer stringBuffer = new StringBuffer();
							String line;
							superserver_locs=new LinkedList<String>();

							while ((line = bufferedReader.readLine()) != null) {
								
								//Check and dont add the SS which is the same as self
								superserver_locs.add(line);
							}
							fileReader.close();

							System.out.println("Contents of file:"+superserver_locs);

							//Got the list of all superserver.. Iterate and call them -- then implement more complicated logic
							if(superserver_locs!=null && superserver_locs.size()>0) {
								URL url_SS=null;
								HttpURLConnection con_SS=null;
								for (int i=0;i<superserver_locs.size();i++) {
									//Iterating each of the superservers now
									try {
										String url_ss=superserver_locs.get(i);

										if(!url_ss.contains(this.myURL)) {
											url_ss+="?client=1";

											//System.out.println("Using the url "+url_ss);
											//Spawn new threads to execute the shoot requests to the 
											
											url_SS= new URL(url_ss);
											con_SS=(HttpURLConnection)url_SS.openConnection();
											con_SS.setRequestMethod("GET");

											//Sends the message to the SS -- 
											new Thread(new ExecuteWSThread(url_ss,this.addmessage,"add")).start();

											InputStream isi = con_SS.getInputStream();
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
											con_SS.disconnect();

											System.out.println("I got response from SS:"+sb.toString());
										}
									} catch (MalformedURLException e) {
										e.printStackTrace();
									} catch (IOException e) {
										e.printStackTrace();
									}

								}
							}
							
						} catch (Exception e) {
							// TODO: handle exception
							System.out.println("Superserver list for client is an issue");
							System.exit(0);
						}

					}					

					try {
						//3 this method will block the thread of latch untill its released later from thread#2
						latch.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				//Waiting for more than half guys to respond and take a consensus and send that response back.
				
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
				
		System.out.println("AsyncRequestProcesser, reqHASH"+(System.currentTimeMillis()-startTime));
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