package aos.common;

import java.util.HashMap;

import aos.listeners.ServerListener;

public class LoadBalancer implements Runnable{

	int Result;
	// Method to process a raq request from a client
	// Takes in an integer and then find the server with the lowest load to process
	// Sends a process request and then returns the result from that server
	public LoadBalancer(int startingValue) {
		
		Result=0;
		
		String lowestServer = this.findLowestServerLoad();
		
		// Make a call to the lowest server with the starting value
		
		// Set the result to what the server returns
		
		
				
	}
	
	public String findLowestServerLoad() {
		String toReturn = "";
		// Get the current WSDLContainer and iterate over the loads of each 
		HashMap<String, Integer> currentLoads = WSDLContainer.getInstance().getAll();
		// Find the smallest load and parse the WSDL for the information to contact the server
		int min = Integer.MAX_VALUE;
		for (String key : currentLoads.keySet()) {
			int currVal = currentLoads.get(key);
			if (currVal < min) {
				min = currVal;
				toReturn = key;
			}
		}
		return toReturn;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.print("New Thread is spawning");
	}
}
