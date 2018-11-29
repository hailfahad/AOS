package aos;

import org.apache.tomcat.jni.Time;

public class AddService implements AddInterface{
	
	
	//Have to add code base for maintaining a queue and adding counters to queue on receipt and then randomly sleep for a while
	
	public int add() {
		
		try {
			Thread.sleep((long) (Math.random()*1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return 5;
	}

	public double myload() {
		return Math.random();
	}
}
