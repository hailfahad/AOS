package aos.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.sun.jmx.snmp.Timestamp;

public class Block {
	
	public String hash;
	
	public String previousHash;
	
	private String message;
	
	private Timestamp timestamp;
	
	private int nonce;
	
	public Block(String data, String previousHash) {
	
			this.message = data;
			
			this.previousHash = previousHash;
			
			this.timestamp = new Timestamp(System.currentTimeMillis());
			
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
		
		return hash(this.previousHash + this.timestamp +this.message );
	}
	
	public void mine(int strength) {
		String target = new String(new char[strength]).replace('\0', '0'); 
		while(!hash.substring( 0, strength).equals(target)) {
			nonce ++;
			hash = getHash();
		}
		System.out.println("Block Mined!!! : " + hash);
		System.out.println("Block Mined!!! : " + hash);
		System.out.println("Block Mined!!! : " + hash);
		System.out.println("Block Mined!!! : " + hash);
		System.out.println("Block Mined!!! : " + hash);
	}
		
	

}
