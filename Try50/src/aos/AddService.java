package aos;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;



public class AddService implements AddInterface{
	
	int identifier = new Random().nextInt();
	
	//Should add code for performance logging perspective from WS server perspective
	
	public int add() {
		
		long startingTime = System.currentTimeMillis();
		String msg="SS,AddService:add,start,"+startingTime+","+identifier;
		appendStuff(msg,  MyTaskQueue.getInstance().getLogFile());
	
		// Storing the records for data usage
		/*try {
			FileOutputStream fos;
			fos = new FileOutputStream("..\\..\\..\\WSRecords.txt");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject("WSERVER | Recieved Add Request From Server | " + startingTime +" | " +"processing Request");
			oos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}*/

		MyTaskQueue.getInstance().addTask();
		
		//Spawn a thread to handle the incoming service.. else it is a blocking call
		//Leave it as is for now
		
		try {
			
			System.out.println("I am gonna sleep for a long time ");
			Thread.sleep((long) (Math.random()*100000));
			System.out.println("Now i am awake ");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//MyTaskQueue.getInstance().removeTask();
		//Should add some code for random exits as well
		
		
		//int retVal=(int) Math.pow(new Random().nextInt(),2);
		int retVal= ThreadLocalRandom.current().nextInt(0,1);
		
		// Storing the records for data usage
		System.out.println("This is the val being returned "+retVal);
		/*try {
			FileOutputStream fos;
			fos = new FileOutputStream("..\\..\\..\\WSRecords.txt");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject("WSERVER | Processed Add Request | " + (System.currentTimeMillis()-startingTime) +" | Returning " + retVal);
			oos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		// Check to see if my chain is empty if it is hash new string
		if(Chain.getInstance().chain.isEmpty()) {
			Chain.getInstance().add(new Block(""+retVal,"0"));
		}
		// Else get previous hash and hash message
		else {
			String lastHash = Chain.getInstance().chain.get(Chain.getInstance().chain.size()-1).hash;
			Chain.getInstance().add(new Block(""+retVal, lastHash));
		}
		
	
		msg="SS,AddService:add,end,"+startingTime+","+identifier;
		appendStuff(msg,  MyTaskQueue.getInstance().getLogFile());
		
		return retVal;
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
	
	public int myload() {
		
		long startingTime = System.currentTimeMillis();
		
		String msg="SS,AddService:myload,start,"+startingTime+","+identifier;
		appendStuff(msg,  MyTaskQueue.getInstance().getLogFile());
	
		// Storing the records for data usage
		int retVal=new Random().nextInt();
		System.out.println("Im in WS ... return my load "+MyTaskQueue.getInstance().getSize());
		
		// Storing the records for data usage
		System.out.println("This is the val being returned "+retVal);
		try {
			FileOutputStream fos;
			fos = new FileOutputStream("..\\..\\..\\WSRecords.txt");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject("WSERVER | Processed Load Request | " + (System.currentTimeMillis()-startingTime) +" | Returning " + retVal);
			oos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		msg="SS,AddService:myload,end,"+System.currentTimeMillis()+","+identifier;
		appendStuff(msg,  MyTaskQueue.getInstance().getLogFile());
	
		
		return MyTaskQueue.getInstance().getSize();
	}
	
	
}
