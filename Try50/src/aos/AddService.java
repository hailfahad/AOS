package aos;

import java.util.Random;

public class AddService implements AddInterface{
	//Should add code for performance logging perspective from WS server perspective
	
	public int add() {
		MyTaskQueue.getInstance().addTask();
		
		//Spawn a thread to handle the incoming service.. else it is a blocking call
		//Leave it as is for now
		
		try {
			Thread.sleep((long) (Math.random()*1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		MyTaskQueue.getInstance().removeTask();
		//Should add some code for random exits as well
		
		
		int retVal=(int) Math.pow(new Random().nextInt(),2);
		System.out.println("This is the val being returned "+retVal);
		return retVal;
	}

	public double myload() {
		return Math.random();
	}
	
	
}
