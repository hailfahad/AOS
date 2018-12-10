package aos;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.sun.jmx.snmp.Timestamp;

public class AddService implements AddInterface{
	//Should add code for performance logging perspective from WS server perspective
	
	public int add() {
		
		long startingTime = System.currentTimeMillis();
		
		// Storing the records for data usage
		try {
			FileOutputStream fos;
			fos = new FileOutputStream("..\\..\\..\\WSRecords.txt");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject("WSERVER | Recieved Add Request From Server | " + startingTime +" | " +"processing Request");
			oos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		try {
			FileOutputStream fos;
			fos = new FileOutputStream("..\\..\\..\\WSRecords.txt");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject("WSERVER | Processed Add Request | " + (System.currentTimeMillis()-startingTime) +" | Returning " + retVal);
			oos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Check to see if my chain is empty if it is hash new string
		if(Chain.getInstance().chain.isEmpty()) {
			Chain.getInstance().add(new Block(""+retVal,"0"));
		}
		// Else get previous hash and hash message
		else {
			String lastHash = Chain.getInstance().chain.get(Chain.getInstance().chain.size()-1).hash;
			Chain.getInstance().add(new Block(""+retVal, lastHash));
		}
		
		
		return retVal;
	}

	public int myload() {
		
		long startingTime = System.currentTimeMillis();
		
		// Storing the records for data usage
		try {
			FileOutputStream fos;
			fos = new FileOutputStream("..\\..\\..\\WSRecords.txt");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject("WSERVER | Recieved Load Request From Server | " + startingTime +" | " +"processing Request");
			oos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
		
		return MyTaskQueue.getInstance().getSize();
	}
	
	
}
