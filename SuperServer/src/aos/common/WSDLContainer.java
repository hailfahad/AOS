package aos.common;

import java.io.Serializable;
import java.util.HashMap;

public class WSDLContainer implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5441040525275235593L;
	private HashMap<String, Integer> wsdl_table; // TODO: is this WSDL - Load
	
	private static WSDLContainer wsdlContainerObj=null;
	
	// Constructor for a new WSDLContainer
	public WSDLContainer() {
		if(wsdl_table == null) {
			wsdl_table=new HashMap();	
		}
	}
	
	// Returns the entire container
	public static WSDLContainer getInstance() {
		if(wsdlContainerObj==null) {
			wsdlContainerObj=new WSDLContainer();
		}
		return wsdlContainerObj;
		
	}
	
	// This method adds/updates a WSDL for a server
	public void add(String arg1, Integer arg2) {
		//Logic still needs to be fixed
		
		if(this.wsdl_table.containsKey(arg1)) {
			this.wsdl_table.put(arg1, this.wsdl_table.get(arg1));
		}else {
			this.wsdl_table.put(arg1,arg2);
		}
	}
	
	// This method is called when a server dies and needs to remove its WSDL from the MAP
	public void remove(String arg1) {
		this.wsdl_table.remove(arg1);
	}
	
	//  Returns the map to be referenced
	public HashMap getAll() {
		return this.wsdl_table;
	}
}
