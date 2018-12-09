package aos.listeners;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.jmx.snmp.Timestamp;

import aos.common.LoadBalancer;

@WebServlet(urlPatterns = "/ClientListener", asyncSupported = true)
public class ClientListener extends HttpServlet {

	private ArrayList<String> ClientRecords; //Format of this string is SERVER | Message Type | Timestamp | Message 

	public ClientListener() {
		this.ClientRecords = new ArrayList<String>();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Client has called me with a request -- Fork a thread which will go through the WSDLs, "
				+ "call myload and figure out who to send the req and wait for response and then send the response back to client.");

		long startTime=System.currentTimeMillis();
		
		request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
		// Value that the client wants a number to be added to
		String client_code=request.getParameter("client");

		System.out.println("Got the client code "+client_code);
		this.ClientRecords.add("SERVER | Recieved from Client | " + (new Timestamp(System.currentTimeMillis())) +" | " + client_code );


		if(client_code.equals("1")) {
			//Request from client ..must spawn WS threads and SS threads

			AsyncContext asyncCtx = request.startAsync();

			asyncCtx.addListener(new AppAsyncListener());
			asyncCtx.setTimeout(5*60*1000);

			System.out.println("Spawning a LoadBalancer ");
			this.ClientRecords.add("SERVER | Sending to WS (1) | " + (new Timestamp(System.currentTimeMillis())) +" | " + client_code );
			this.ClientRecords.add("SERVER | Sending to SS (0) | " + (new Timestamp(System.currentTimeMillis())) +" | " + client_code );
			new AsyncRequestProcessor(asyncCtx, 5*60*1000,1).run();
			
			

		}else {
			//Only spawn WS threads
			AsyncContext asyncCtx = request.startAsync();

			asyncCtx.addListener(new AppAsyncListener());
			asyncCtx.setTimeout(5*60*1000);
			this.ClientRecords.add("SERVER | Sending to WS (0) | " + (new Timestamp(System.currentTimeMillis())) +" | " + client_code );
			new AsyncRequestProcessor(asyncCtx, 5*60*1000,0).run();

		}

		System.out.println("ClientListener, reqHASH,"+(System.currentTimeMillis()-startTime));
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//doGet(request, response);
	}
	
	@Override
	public void destroy() {
		// write object to file
		FileOutputStream fos;
		try {
			fos = new FileOutputStream("E:\\git\\AOS\\SuperServer\\ServerRecords.txt");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			Iterator<String> iter = this.ClientRecords.iterator();
			while(iter.hasNext())
				oos.writeObject(iter.next());
			oos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
	}

	@Override
	public void init(ServletConfig config) throws ServletException {

	}
	
	
}
