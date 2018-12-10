package aos;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class AddService implements AddInterface{
	//Should add code for performance logging perspective from WS server perspective
	
	public int add() {
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
		System.out.println("This is the val being returned "+retVal);
		return retVal;
	}

	public int myload() {
		int retVal=new Random().nextInt();
		System.out.println("Im in WS ... return my load "+MyTaskQueue.getInstance().getSize());
		return MyTaskQueue.getInstance().getSize();
	}
	
	
}
