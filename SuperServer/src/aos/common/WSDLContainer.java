package aos.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class WSDLContainer implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5441040525275235593L;
	private ArrayList<String> wsdl_table; // TODO: is this WSDL - Load
	
	private static WSDLContainer wsdlContainerObj=null;
	
	// Constructor for a new WSDLContainer
	private WSDLContainer() {
		//if(wsdl_table == null) {
			wsdl_table=new ArrayList();	
		//}
	}
	
	// Returns the entire container
	public static WSDLContainer getInstance() {
		if(wsdlContainerObj==null) {
			wsdlContainerObj=new WSDLContainer();
		}
		System.out.println("calling the getinstance "+wsdlContainerObj.wsdl_table);
		return wsdlContainerObj;
		
	}
	
	// This method adds/updates a WSDL for a server
	public void add(String arg1) {
		//Logic still needs to be fixed
		
		//if(this.wsdl_table.containsKey(arg1)) {
		//	this.wsdl_table.put(arg1, this.wsdl_table.get(arg1));
		//}else {
			this.wsdl_table.add(arg1);
		//}
		System.out.println("This is the table "+this.wsdl_table);
	}
	
	// This method is called when a server dies and needs to remove its WSDL from the MAP
	public void remove(String arg1) {
		this.wsdl_table.remove(arg1);
	}
	
	//  Returns the map to be referenced
	public ArrayList getAll() {
		return this.wsdl_table;
	}
}
