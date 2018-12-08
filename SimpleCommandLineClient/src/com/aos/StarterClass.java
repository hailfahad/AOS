package com.aos;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.soap.SOAPFaultException;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.model.iface.Operation;

public class StarterClass {
	public static void main(String[] args) {
		//Client starts from here.. it will take input of a file which will contain the list of superservers which Client knows about..
		//These could be one or many..
		
		//Trial for calling the WS from java which is to be put into the SS code:
		
		String ws_url="http://localhost:8080/Try50/services/AddService/add";
		
		//hardcoding file path for now
		String superserver_list="/home/siddiqui/aos/ws_resolvers.txt";
		
		List<String> superserver_locs=null;
		
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
					url_ss+="?client=1";
					
					//System.out.println("Using the url "+url_ss);
					url= new URL(url_ss);
					con=(HttpURLConnection)url.openConnection();
					con.setRequestMethod("GET");
					con.setConnectTimeout(5*60*1000);
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
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
		
	}
	
}
