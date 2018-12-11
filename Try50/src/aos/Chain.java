package aos;

import java.util.ArrayList;



public class Chain {
	
	static ArrayList<Block> chain;
	int strength;	
	private static Chain myChainObj = null;

	private Chain() {
		
		this.chain = new ArrayList<Block>();
	}
	
	//add a new message to the chain
	public void add(Block b) {
		
		this.chain.add(b);
		chain.get(chain.size()-1);
	}
	
	public static Chain getInstance() {
		if(myChainObj == null) {
			myChainObj = new Chain();
		}
		return myChainObj;
	}
	
	//check to see if the current chain is valid
	public Boolean isValid() {
		
		Block curr, prev;
		
		for(int i=1; i< chain.size(); i++) {
			curr = chain.get(i);
			prev = chain.get(i-1);
			
			if(!curr.hash.equals(curr.getHash())) {
				System.out.println("u messed up");
				return false;
			}
			if(!prev.hash.equals(curr.previousHash)) {
				System.out.println("u still meessed up");
				return false;
			}
		}
		
		return true;
	}
	
	// Returns the current chain as well as the result
	public String getChain() {
		String toReturn = "";
		
		for(Block curr : chain) {
			toReturn += curr.toString() + "|";
		}
	
		return toReturn;
	}
	
	// Given a new chain to replace with, regenerate the chain and set as current chain
	public boolean updateChain(String newChain) {
	
		String[] chainParts = newChain.split("\\|");
		ArrayList<Block> chainList = new ArrayList<Block>();
		Block prev = null;
		for(int i = 0; i< chainParts.length; i+=2) {
			System.out.println("chainPArt "+chainParts);
			String message = chainParts[i];
			System.out.println("messg a "+message);
			long time = Long.parseLong(chainParts[i+1]);
			System.out.println("time "+time);
			if(i == 0) { // initial Hash
				Block currBlock = new Block(message, "0", time);
				prev = currBlock;
				chainList.add(currBlock);
			}
			else {
				Block currBlock = new Block(message, prev.hash, time);
				chainList.add(currBlock);
			}
		}
		if (!chainList.isEmpty()) {
			this.chain = chainList;
			return true;
		}
		return false;
	
	}
}
