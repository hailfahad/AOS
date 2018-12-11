package aos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;



public class AddService implements AddInterface{
	
	int identifier = new Random().nextInt();
	
	//Should add code for performance logging perspective from WS server perspective
	
	public String toss() {
		long startingTime = System.currentTimeMillis();
		String msg="SS,AddService:toss,start,"+startingTime+","+identifier;
		appendStuff(msg,  MyTaskQueue.getInstance().getLogFile());
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
		int retVal= (int) Math.round( Math.random() );
		
		// Storing the records for data usage
		System.out.println("This is the val toss return "+retVal);
		
		// Check to see if my chain is empty if it is hash new string
		if(Chain.getInstance().chain.isEmpty()) {
			Chain.getInstance().add(new Block(""+retVal,"0"));
		}
		// Else get previous hash and hash message
		else {
			String lastHash = Chain.getInstance().chain.get(Chain.getInstance().chain.size()-1).hash;
			Chain.getInstance().add(new Block(""+retVal, lastHash));
		}
		
	
		msg="SS,AddService:toss,end,"+startingTime+","+identifier;
		appendStuff(msg,  MyTaskQueue.getInstance().getLogFile());
		System.out.println("return from toss "+Chain.getInstance().getChain());
		
		return Chain.getInstance().getChain();
	
	}
	
	public String add() {
		
		long startingTime = System.currentTimeMillis();
		String msg="SS,AddService:add,start,"+startingTime+","+identifier;
		appendStuff(msg,  MyTaskQueue.getInstance().getLogFile());
	
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
		int retVal=0;
		if(Math.random()>0.5) {
			retVal=1;
		}
		// Storing the records for data usage
		System.out.println("This is the val being returned inside add method "+retVal);
		
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
		
		System.out.println("Im returning in add )"+Chain.getInstance().getChain());
		//return retVal+"";
		return Chain.getInstance().getChain();
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
		//System.out.println("This is the val being returned "+retVal);
		
		msg="SS,AddService:myload,end,"+System.currentTimeMillis()+","+identifier;
		appendStuff(msg,  MyTaskQueue.getInstance().getLogFile());
	
		
		return MyTaskQueue.getInstance().getSize();
	}
	
	public void updateChain(String update){
		System.out.println("I get soemthign to update chain "+Chain.getInstance());
		
		boolean retVal = Chain.getInstance().updateChain(update);
		System.out.println("ret val bool "+retVal);
	}
	
	
}
