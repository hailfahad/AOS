package aos.listeners;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.*;

@WebServlet(urlPatterns = "/ClientListener", asyncSupported = true)
public class ClientListener extends HttpServlet {
	public ArrayList<String> ClientRecords;
	
	private String logFileLoc=null;

	private String myURL = "";

	
	public ClientListener() {
		this.ClientRecords = new ArrayList<String>();
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
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Client has called me with a request -- Fork a thread which will go through the WSDLs, "
				+ "call myload and figure out who to send the req and wait for response and then send the response back to client.");
		// Storing the records for data usage
		String logFile=request.getServletContext().getInitParameter("logFile");
		
		this.logFileLoc=logFile;
		long startTime=System.currentTimeMillis();
		String msg="ClientListener,doGet,start,"+startTime+","+request;
		appendStuff(msg,  logFile);
		ClientRecords.add(msg);
		
		request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
		// Value that the client wants a number to be added to
		String client_code=request.getParameter("client");

		System.out.println("Got the client code "+client_code);
		

		if(client_code.equals("1")) {
			//Request from client ..must spawn WS threads and SS threads
			//This should come from web.xml -- 

			System.out.println("This is the path "+request.getRealPath("/"));
			String superserver_list=request.getRealPath("/")+"/files/ServerRecords.txt";
			try {
				File f=new File(superserver_list);
				System.out.println("does fiel exist "+f.exists());
			}catch (Exception e) {
				e.printStackTrace();
			}
			LinkedList<String> superserver_locs=null;

			try {
				File file = new File(superserver_list);
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				//StringBuffer stringBuffer = new StringBuffer();
				String line;
				superserver_locs=new LinkedList<String>();

				while ((line = bufferedReader.readLine()) != null) {
					//Check and dont add the SS which is the same as self
					System.out.println("Thjis sit he address "+line);
					if(!line.contains(this.myURL))
						superserver_locs.add(line);
				}
				fileReader.close();
				System.out.println("Contents of file:"+superserver_locs);

			
			
			AsyncContext asyncCtx = request.startAsync();

			asyncCtx.addListener(new AppAsyncListener());
			asyncCtx.setTimeout(5*60*1000);

			System.out.println("Spawning a LoadBalancer ");
			this.ClientRecords.add("SERVER | Sending to WS (1) | " + (System.currentTimeMillis()) +" | " + client_code );
			this.ClientRecords.add("SERVER | Sending to SS (0) | " + (System.currentTimeMillis()) +" | " + client_code );
			new AsyncRequestProcessor(asyncCtx, 5*60*1000,Integer.parseInt(client_code)).run();
			//Spawn threads for SS here itself
			// String superserver_list="/home/siddiqui/aos/ws_resolvers.txt";
					
			//Got the list of all superserver.. Iterate and call them -- then implement more complicated logic
			/*if(superserver_locs!=null && superserver_locs.size()>0) {
				final CountDownLatch latch = new CountDownLatch(superserver_locs.size()-1);

				for (int i=0;i<superserver_locs.size();i++) {
					//Iterating each of the superservers now
						String url_ss=superserver_locs.get(i);
						//if(!url_ss.contains(this.myURL)) {
							//url_ss+="?client=0";
						
							System.out.println("Using the url FOR SS  "+url_ss);
							
							new Thread(new Runnable() {
								@Override
								public void run() {
									try {
									URL url_SS=null;
									HttpURLConnection con_SS=null;
										
									url_SS= new URL(url_ss);
									con_SS=(HttpURLConnection)url_SS.openConnection();
									con_SS.setRequestMethod("GET");
									//Sends the message to the SS -- 
									//new Thread(new ExecuteWSThread(url_ss,this.addmessage,"add")).start();
									System.out.println("Am i blocking ");
									
									//response.getOutputStream();
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
									System.out.println("I Completed connection to SS in a thread :"+sb.toString());

									latch.countDown();
									} catch (MalformedURLException e) {
										e.printStackTrace();
									} catch (IOException e) {
										e.printStackTrace();
									}
									
								}
							}).start();
							//Spawn new threads to execute the shoot requests to the
							
							try {
								//3 this method will block the thread of latch untill its released later from thread#2
								latch.await();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							
							System.out.println("Must send response back over from here");
							
						//}
				}
			}
			*/
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Superserver list for client is an issue");
				//System.exit(0);
			}
		
			
		}else {
			//Only spawn WS threads
			AsyncContext asyncCtx = request.startAsync();

			asyncCtx.addListener(new AppAsyncListener());
			asyncCtx.setTimeout(5*60*1000);
			
			// Used to store information for data collection
			
			new AsyncRequestProcessor(asyncCtx, 5*60*1000,0).run();

		}

		
		msg="ClientListener,doGet,end,"+(System.currentTimeMillis())+","+request;
		appendStuff(msg,  logFile);
		ClientRecords.add(msg);
		
		System.out.println("ClientListener, reqHASH,"+(System.currentTimeMillis()-startTime));
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//doGet(request, response);
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
	
	@Override
	public void destroy() {
		try {
			createFile(logFileLoc+"CL", this.ClientRecords);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
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
	
	
}
