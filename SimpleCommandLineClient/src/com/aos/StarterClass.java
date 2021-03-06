package com.aos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class StarterClass {
	
	int counter=0;
	
	public synchronized void  appendStuff(String message, String logFile) {
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
	
	public static void main(String[] args) {
		//Client starts from here.. it will take input of a file which will contain the list of superservers which Client knows about..
		//These could be one or many..
		//Trial for calling the WS from java which is to be put into the SS code:
		/*int retVal= ThreadLocalRandom.current().nextInt(0,1);
		System.out.println(retVal);
		System.exit(1);*/
		
		String clientLog="/home/siddiqui/aos/clientLog.txt";
		StarterClass stObj = new StarterClass();
		
		for(int i=0;i<1;i++) {
			//stObj.doClientThing();
			
			stObj.counter=i;
			new Thread(new Runnable() {
				@Override
				public void run() {
					String msg="SS,StarterClass,start,"+System.currentTimeMillis()+","+stObj.counter;
					stObj.appendStuff(msg, clientLog);
				
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
						//System.out.println("Superserver list for client is an issue");
						System.exit(0);
					}

					//Got the list of all superserver.. Iterate and call them -- then implement more complicated logic
					if(superserver_locs!=null && superserver_locs.size()>0) {
						URL url=null;
						HttpURLConnection con=null;
						
						//Random selection of list
						int randomNum=0;
						if(superserver_locs.size()>1) {
							 randomNum= ThreadLocalRandom.current().nextInt(0, superserver_locs.size() - 1);
						}
						//for (int i=0;i<superserver_locs.size();i++) {
							//Iterating each of the superservers now
							try {
								String url_ss=superserver_locs.get(randomNum);
								url_ss+="?client=1";
								
								System.out.println("Using the url "+url_ss);
								url= new URL(url_ss);
								con=(HttpURLConnection)url.openConnection();
								con.setRequestMethod("GET");
								con.setConnectTimeout(5*60*10000);
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
						//}
					}
					msg="SS,StarterClass,end,"+System.currentTimeMillis()+","+stObj.counter;
					stObj.appendStuff(msg, clientLog);
				
				}	
				}).start();
		}
		
	}
	
	public void doClientThing() {
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
