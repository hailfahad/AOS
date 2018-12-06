package aos.listeners;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import aos.common.LoadBalancer;

@WebServlet(urlPatterns = "/ClientListener", asyncSupported = true)
public class ClientListener extends HttpServlet {

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Client has called me with a request -- Fork a thread which will go through the WSDLs, "
				+ "call myload and figure out who to send the req and wait for response and then send the response back to client.");
		
		// Value that the client wants a number to be added to
		//int input = (int) request.getAttribute("input");
		
		//  Creation of a thread to find the lowest load of and process the operation and return the message to the client
		
		 final AsyncContext asyncContext = request.startAsync(request, response);
		 
		 
		LoadBalancer loadBalance = new LoadBalancer(1,"add",asyncContext);
		// Generates 1 thread to send a request to servers with the lowest loads
		new Thread(loadBalance).start();
		
		
	
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
