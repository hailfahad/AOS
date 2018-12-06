package com.aos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class StarterClass {

	public static void main(String[] args) {
		//Client starts from here.. it will take input of a file which will contain the list of superservers which Client knows about..
		//These could be one or many..
		
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
					url= new URL(superserver_locs.get(i));
					con=(HttpURLConnection)url.openConnection();
					con.setRequestMethod("GET");
					
					InputStream is = con.getInputStream();
		            InputStreamReader isr = new InputStreamReader(is);
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
		            is.close();
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
