package aos;

import java.util.LinkedList;
import java.util.Queue;

public class MyTaskQueue{

	private Queue<Integer> mytask=null;
	private static MyTaskQueue myTaskQueueObj=null;
	
	private String logFile=null;
	
	private MyTaskQueue() {
		mytask=new LinkedList<>();
		
	}
	
	public static MyTaskQueue getInstance() {
		if(myTaskQueueObj==null) {
			myTaskQueueObj=new MyTaskQueue();
		}
		return myTaskQueueObj;
	}
	
	public void addTask() {
		mytask.add(1);
	}
	
	public void removeTask() {
		mytask.remove();
	}
	
	public int getSize() {
		return mytask.size();
	}
	
	public void setLogFile(String logFile) {
		this.logFile=logFile;
	}
	
	public String getLogFile() {
		return this.logFile;
	}
}
