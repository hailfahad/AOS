package aos;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Block {
	
	public String hash;
	
	public String previousHash;
	
	public String message;
	
	public long timestamp;
		
	public Block(String data, String previousHash) {
	
			this.message = data;
			
			this.previousHash = previousHash;
			
			this.timestamp = System.currentTimeMillis();
			
			this.hash = getHash();
			
	}
	
	public Block(String data, String previousHash, long timestamp) {
		
		this.message = data;
		
		this.previousHash = previousHash;
		
		this.timestamp = timestamp;
		
		this.hash = getHash();
	}
	
	public String hash(String message) {
		
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(message.getBytes());
			return new String(md.digest());
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
		
	}
	
	public String getHash() {
		
		return hash(this.previousHash + this.timestamp + this.message );
	}
	
	public String toString() {
		
		String toReturn = "";
		toReturn += this.message;
		toReturn += "|";
		toReturn += this.timestamp;
		return toReturn;
	}
	
	public void Update(String newMessage, long newTime ) {
		
		
	}
	
	
}
