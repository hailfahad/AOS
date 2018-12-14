package aos.listeners;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

import aos.common.WSDLContainer;

public class AsyncRequestProcessor implements Runnable {

	private AsyncContext asyncContext;
	private int secs;
	int Result=1;
	String Operation="add";

	private String myloadmessage="<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\"><Body><myload xmlns=\"http://aos\"/></Body></Envelope>";
	private String addmessage="<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\"><Body><add xmlns=\"http://aos\"/></Body></Envelope>";

	private String tossmessage="<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + 
			"    <Body>\n" + 
			"        <toss xmlns=\"http://aos\"/>\n" + 
			"    </Body>\n" + 
			"</Envelope>";

	private String updatechainmsg="<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + 
			"    <Body>\n" + 
			"        <updateChain xmlns=\"http://aos\">\n" + 
			"            <update>[string]</update>\n" + 
			"        </updateChain>\n" + 
			"    </Body>\n" + 
			"</Envelope>";


	private Pattern loadpattern = Pattern.compile("<myloadReturn>(.+?)</myloadReturn>", Pattern.DOTALL);
	private Pattern addpattern = Pattern.compile("<addReturn>(.+?)</addReturn>", Pattern.DOTALL);

	private Pattern tosspattern = Pattern.compile("<tossReturn>(.+?)</tossReturn>", Pattern.DOTALL);

	private String ws_response=null;
	private ArrayList <String> all_ws_chains=new ArrayList();
	private boolean responseFlag=false;

	private String myURL = "";
	private int serverRequestType = 0;  // 1 = send message to WS and SS | 0 is only send to SS

	private String logFileLoc=null;

	private int howManyNeeded=0;
	int counter=0;
	private ArrayList <String> allLogs=new ArrayList();
	
	String final_chain=null;
	String to_write_msg=null;

	public AsyncRequestProcessor(AsyncContext asyncCtx, int secs, int serverRequestType) {
		this.asyncContext = asyncCtx;
		this.secs = secs;
		this.serverRequestType=serverRequestType;

		// https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
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
		String logFile=this.asyncContext.getRequest().getServletContext().getInitParameter("logFile");
		this.logFileLoc=logFile;
		
		String msg="LoadBalance,findLowestServerLoads,start,"+System.currentTimeMillis()+","+this.asyncContext.getRequest();
		appendStuff(msg,  logFile);

		this.allLogs.add(msg);
		
		ArrayList<String> toReturn = null;
		// Get the current WSDLContainer and iterate over the loads of each 
		ArrayList<String> currentLoads = WSDLContainer.getInstance().getAll();

		System.out.println("This is the current load "+currentLoads.size());
		//HashMap<String, Integer> dataLoaded=new HashMap<String, Integer>();
		// Find the smallest load and parse the WSDL for the information to contact the server

		int min = Integer.MAX_VALUE;
		if(currentLoads.size()>0) {
			LinkedHashMap<String, Integer> toSort = new LinkedHashMap<String, Integer>();
			
			toReturn=new ArrayList<String>();
			//TODO:  If you want to find the different services offered by a servlet you can use ADDService look for wsdl:operation
			try {
				for (String key : currentLoads) {
					String url = returnIP(key);
					System.out.println("This is the load url to hit "+url);
					String res=communicateWS( url,this.myloadmessage,"myload");

					if(res!=null) {
						Matcher matcher = loadpattern.matcher(res);
						matcher.find();
						int response = Integer.parseInt(matcher.group(1));
						System.out.println("What did i get for my load "+response);

						toReturn.add(key);
						toSort.put(key, response);
						
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
				
				Map<String, Integer> sortedResult = sortMap(toSort);
				/*TreeSet<Integer> sortedSet = new TreeSet<Integer>();
				ArrayList<String> sortedLoads = new ArrayList<String>();
				sortedSet.addAll(toSort.keySet());
				for (Integer currKey : sortedSet) {
					sortedLoads.addAll(toSort.get(currKey));
				}*/
		
				System.out.println("This is the return list "+toReturn);
				if(toReturn!=null) {
					this.howManyNeeded=((int)Math.ceil(toReturn.size()/2))+1; 	
				}
				
				ArrayList<String> toReturn2 = new ArrayList();
						
				//if (sortedResult.size() > 3) {
				int counter=0;
				for(String key:sortedResult.keySet()) {
					counter++;
					toReturn2.add(key);
					if(counter==3) {
						break;
					}
				}
					
				//}else {
				//	toReturn2=new ArrayList(sortedResult.keySet());
				//}
		
				msg="LoadBalance,findLowestServerLoads,end,"+System.currentTimeMillis()+","+this.asyncContext.getRequest();
				appendStuff(msg,  logFile);
				this.allLogs.add(msg);
				
				return toReturn2;	
				
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

	public LinkedHashMap<String, Integer> sortMap(LinkedHashMap<String, Integer> map) {
        List<Map.Entry<String, Integer>> capitalList = new LinkedList<>(map.entrySet());

        Collections.sort(capitalList, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));

        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : capitalList)
        {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

	public synchronized void appendStuff(String message, String logFile) {
		try(FileWriter fw = new FileWriter(logFile, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw))
		{
			//out.println("SS,ServerListener,"+startingTime+","+request);
			out.println(message+"\n");
			out.close();
		} catch (IOException e) {
			//exception handling left as an exercise for the reader
		}
	}



	@Override
	public void run() {
		String logFile=this.asyncContext.getRequest().getServletContext().getInitParameter("logFile");
		this.logFileLoc=logFile;
		long startingTime= System.currentTimeMillis();

		String msg="AsyncRequestProcessor,run,start,"+startingTime+","+this.asyncContext.getRequest();
		appendStuff(msg,  logFile);
		this.allLogs.add(msg);
		
		
		//In this one needs to spawn new threads 
		System.out.println("Async Supported? "
				+ asyncContext.getRequest().isAsyncSupported());

		ArrayList<String> lowestServer = this.findLowestServerLoads();
		// Get a response back from the server
		if(lowestServer!=null) {
			try {
				//final CountDownLatch latch = new CountDownLatch(this.howManyNeeded);
				final CountDownLatch latch = new CountDownLatch(1);

				for (String server : lowestServer) {
					System.out.println("This si the server "+server);
					String serverAddress = returnIP(server);
					System.out.println("This is the server I want to contact "+serverAddress);
					//AsyncContext asyncContextObj = this.asyncContext.getRequest().startAsync();

					//This part needs to be handled -- Spawn a new thread and then capture the first response only
					//Thread t= new Thread(new ExecuteWSThread(serverAddress,this.addmessage,"add"));
					//t.start();
					counter++;

					new Thread(new Runnable() {
						@Override
						public void run() {
							String msg="WSSpawn,run"+counter+",start,"+System.currentTimeMillis()+","+asyncContext.getRequest();
							appendStuff(msg,  logFile);
							allLogs.add(msg);
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
								//con.setRequestProperty("SOAPAction", "toss");
								con.setDoOutput(true);

								DataOutputStream wr = new DataOutputStream (
										con.getOutputStream());
								wr.writeBytes(addmessage);
								//wr.writeBytes(tossmessage);
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
							//Matcher matcher = tosspattern.matcher(res);
							matcher.find();

							ws_response=matcher.group(1);
							System.out.println("I got a match "+ws_response);
							all_ws_chains.add(ws_response);
							System.out.println("This si the arr list "+all_ws_chains);
							//then release the lock
							//5
							latch.countDown();

							msg="WSSpawn,run"+counter+",end,"+System.currentTimeMillis()+","+asyncContext.getRequest();
							appendStuff(msg,  logFile);
							allLogs.add(msg);
							System.out.println("Done the write part");
						}
					}).start();

					//I would need to spawn threads for Calling the other SS and then wait for the first guy
					// If the server request type is from another SS Process the SUPERSERver list and sent the request to them
					try {
						//3 this method will block the thread of latch untill its released later from thread#2
						latch.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				//this.asyncContext.complete();
				//Waiting for more than half guys to respond and take a consensus and send that response back.
				//Will collect all chains responded by WSs and then send to majority finder.

				//Due to countdown.. i shud reach here when i get a response from these many nodes
				//No need to wait anymore i suppose.
				// Send a message to all WS to update to newChain
				//System.out.println("Before majority chaint "+all_ws_chains);
				 this.final_chain=getMajorityChain(all_ws_chains);
				//System.out.println("after majority chaint "+final_chain);
				to_write_msg=getResponse(final_chain);
				System.out.println("got response  "+to_write_msg);
				System.out.println("My thread has completed and hence, I reach here. -- Maybe I reach here on first thread completion "+ws_response);
				if(!this.responseFlag) {
					//It is false... first timer.. send response
					//this.asyncContext.getRes
					ServletResponse response = this.asyncContext.getResponse();
					response.setContentType("text/plain");
					PrintWriter out = response.getWriter();
					//out.println(this.ws_response);
					out.println(to_write_msg);
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
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		msg="AsyncRequestProcessor,end,"+System.currentTimeMillis()+","+this.asyncContext.getRequest();
		appendStuff(msg,  logFile);
		allLogs.add(msg);
		
		try {
			createFile(logFileLoc+"AS", allLogs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		asyncContext.complete();
		//Can i send an update to all WS
/*		if(lowestServer!=null) {
			for (String server_key : lowestServer) {
			System.out.println("This si the server "+server_key);
			String serverAddress;
			try {
				serverAddress = returnIP(server_key);
				System.out.println("This is the server I want to contact "+serverAddress);
				new Thread(new Runnable() {
				@Override
				public void run() {

					//4
					//do your logic here in thread#2
					System.out.println("Started thread for WS");
					StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
					URL url;
					try {
						System.out.println("server for update chain "+serverAddress);
						url = new URL(serverAddress);
						HttpURLConnection con=(HttpURLConnection)url.openConnection();

						con.setRequestMethod("POST");
						con.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
						con.setRequestProperty("SOAPAction", "updateChain");
						//con.setRequestProperty("SOAPAction", "toss");
						con.setDoOutput(true);

						DataOutputStream wr = new DataOutputStream (
								con.getOutputStream());
						updatechainmsg.replaceAll("[string]", final_chain);
						System.out.println("My SOAP XML "+updatechainmsg);
						wr.writeBytes(updatechainmsg);
						//wr.writeBytes(tossmessage);
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
					
					//msg="SS,WSSpawn,end,"+System.currentTimeMillis()+","+asyncContext.getRequest();
					//appendStuff(msg,  logFile);
					System.out.println("Done the write part");
				}
			}).start();


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
				//AsyncContext asyncContextObj = this.asyncContext.getRequest().startAsync();
				//This part needs to be handled -- Spawn a new thread and then capture the first response only
				//Thread t= new Thread(new ExecuteWSThread(serverAddress,this.addmessage,"add"));
				//t.start();
				
			}
			//I would need to spawn threads for Calling the other SS and then wait for the first guy

		// If the server request type is from another SS Process the SUPERSERver list and sent the request to them
			msg="SS,AsyncRequestProcessor,end,"+System.currentTimeMillis()+","+this.asyncContext.getRequest();
			appendStuff(msg,  logFile);

			System.out.println("AsyncRequestProcesser, reqHASH"+(System.currentTimeMillis()-startingTime));
		}*/
	}
	
	private void createFile(String file, ArrayList<String> arrData)
            throws IOException {
        FileWriter writer = new FileWriter(file + ".txt",true);
        int size = arrData.size();
        for (int i=0;i<size;i++) {
            String str = arrData.get(i).toString();
            writer.write(str);
            if(i < size-1)
                writer.write("\n");
        }
        writer.close();
    }		

	public String getResponse(String chain) {

		String[] chainParts = chain.split("\\|");
		return chainParts[chainParts.length-2];
	}


				public String getMajorityChain(ArrayList<String> listofChains) {
					// Assuming that I get a list of Responses and their chains
					// 1 Map with (ServerName, Server Response)
					// 2 Map with (ServerName, Server Hash)
					// Assuming I know the total number of WS I have		
					HashMap<String, String> responseMap = new HashMap<String,String>(); //id result
					HashMap<String, String> chainMap = new HashMap<String,String>();// id chain	

					// Loads up the information given a chain
					try {
						System.out.println("Ph1");
						for (int i=0; i<listofChains.size(); i++){	
							String[] chainParts = listofChains.get(i).split("\\|");
							System.out.println("chain "+chainParts);
							responseMap.put(""+i, chainParts[chainParts.length-2]);
							chainMap.put(""+i, listofChains.get(i));
						}
						System.out.println("Ph2 "+responseMap);

						// Count number of heads and tails
						int numHeads = 0;
						int numTails = 0;
						String longestTailChain = "";
						String longestHeadChain = "";
						System.out.println("Ph3 "+chainMap);
						for(String key: responseMap.keySet()) {

							if(responseMap.get(key).equals("0")) {
								System.out.println(":");
								numHeads ++;	
								if(chainMap.get(key).length() > longestHeadChain.length()) {
									longestHeadChain = chainMap.get(key);
								}
							}
							else {
								numTails ++;
								if(chainMap.get(key).length() > longestTailChain.length()) {
									longestTailChain = chainMap.get(key);
								}
							}
						}

						System.out.println("Ph4");
						String newChain = numHeads > numTails ? longestHeadChain : longestTailChain;

						return newChain;
					}catch(Exception e) {
						e.printStackTrace();
					}
					return null;
				}

			}