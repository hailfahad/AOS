package aos;

import java.io.IOException;


import java.io.*;
import java.net.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;



public class Register implements javax.servlet.Servlet {

		@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ServletConfig getServletConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		System.out.println("In im init method and shall sent across my WSDL");
		System.out.println("WSDL "+config.getServletContext().getContextPath());
		
		System.out.println("Context name "+config.getServletContext().getServletContextName());
		//System.out.println("Server Info "+config.getServletContext().getServerInfo());
		System.out.println("Server Name "+config.getServletContext().getVirtualServerName());
		
		System.out.println("Will i fail now also ");
		String urlWSDL="http://127.0.0.1:8080/"+config.getServletContext().getServletContextName()+"/services/AddService?wsdl";
		
		//String loc_superserver=config.getInitParameter("superserver");
		String loc_superserver="http://127.0.0.1:8081/SuperServer/ServerListener";	
		System.out.println("------- What is the url hit before---------- "+loc_superserver);
		
		loc_superserver+="?WSDL="+urlWSDL;
		System.out.println("------- What is the url hit---------- "+loc_superserver);
		
		try {
			URL url=new URL(loc_superserver);
			HttpURLConnection urlc=(HttpURLConnection)url.openConnection();
			urlc.setDoOutput(true);
			urlc.setAllowUserInteraction(false);
			urlc.setRequestMethod("GET");
			urlc.connect();
			if(urlc.getResponseCode()==HttpURLConnection.HTTP_OK) {
				System.out.println("Was able to send my WSDL to SS");
			}else {
				System.out.println("Didnt send.. need to write a loop to try again");
			}
			
			//PrintStream ps = new PrintStream(urlc.getOutputStream());
			//ps.print(urlWSDL);
			//ps.close();
			
			System.out.println("I have sent my stupid WSDL to Super server");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	}

	
}
