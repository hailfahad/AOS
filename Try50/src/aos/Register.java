package aos;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.*;
import java.net.*;



public class Register implements javax.servlet.Servlet {

		@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ServletConfig getServletConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		System.out.println("In im init method and shall sent across my WSDL");
		System.out.println("WSDL "+config.getServletContext().getContextPath());
		
		System.out.println("Context name "+config.getServletContext().getServletContextName());
		System.out.println("Server Info "+config.getServletContext().getServerInfo());
		System.out.println("Server Name "+config.getServletContext().getVirtualServerName());
		
		String urlWSDL="http://localhost:8080/"+config.getServletContext().getServletContextName()+"/services/AddService?wsdl";
		
		String loc_superserver=config.getInitParameter("superserver");
		try {
			URL url=new URL(loc_superserver);
			URLConnection urlc=url.openConnection();
			urlc.setDoOutput(true);
			urlc.setAllowUserInteraction(false);
			
			PrintStream ps = new PrintStream(urlc.getOutputStream());
			ps.print(urlWSDL);
			ps.close();
			
			System.out.println("I have sent my stupid WSDL to Super server");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}
}
