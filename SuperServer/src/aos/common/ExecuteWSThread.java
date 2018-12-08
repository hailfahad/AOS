package aos.common;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.AsyncContext;
import javax.servlet.ServletResponse;

import sun.net.www.protocol.http.HttpURLConnection;

public class ExecuteWSThread implements Runnable{

	private String serverAddress;
	private String message;
	private AsyncContext asyncContext;
	private String action;
	private Pattern addpattern = Pattern.compile("<addReturn>(.+?)</addReturn>", Pattern.DOTALL);

	
	public ExecuteWSThread(String serverAddress, String message, AsyncContext asyncContext,String action) {
		this.serverAddress=serverAddress;
		this.message=message;
		this.asyncContext=asyncContext;
		this.action=action;
				
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
	@Override
	public void run() {
		System.out.println("Im in the add thread spawner "+serverAddress+this.action+"  "+this.message);
		String res=communicateWS( serverAddress,this.message,this.action);
		System.out.println("This time the response for add "+res);
		Matcher matcher = this.addpattern.matcher(res);
		matcher.find();
		
		//URL connection = new URL(serverAddress+"/add");
		//HttpURLConnection con = (HttpURLConnection) connection.openConnection();
		//con.setRequestMethod("GET");
		PrintWriter out;
		try {
			ServletResponse response = this.asyncContext.getResponse();
	        response.setContentType("text/plain");
			out = response.getWriter();
			out.println(matcher.group(1));
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
