package aos.common;

import java.util.ArrayList;

public class Ledger {
	
	ArrayList<Block> chain;
	int strength;	

	public Ledger() {
		
		this.chain = new ArrayList<Block>();
		this.strength = 20;
	}
	
	//add a new message to the chain
	public void add(Block b) {
		
		this.chain.add(b);
		chain.get(chain.size()-1).mine(1);
	}
	
	//check to see if the current chain is valid
	public Boolean isValid() {
		
		Block curr, prev;
		String tar = new String(new char[strength]).replace('\0', '0');
		
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
}
