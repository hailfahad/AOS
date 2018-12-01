package aos.common;

import java.io.Serializable;
import java.util.HashMap;

public class WSDLContainer implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5441040525275235593L;
	private HashMap<String, Integer> wsdl_table;
	
	private static WSDLContainer wsdlContainerObj=null;
	
	
	private WSDLContainer() {
		if(wsdl_table == null) {
			wsdl_table=new HashMap();	
		}
	}
	
	public static WSDLContainer getInstance() {
		if(wsdlContainerObj==null) {
			wsdlContainerObj=new WSDLContainer();
		}
		return wsdlContainerObj;
		
	}
	
	public void add(String arg1, Integer arg2) {
		//Logic still needs to be fixed
		
		if(this.wsdl_table.containsKey(arg1)) {
			this.wsdl_table.put(arg1, this.wsdl_table.get(arg1));
		}else {
			this.wsdl_table.put(arg1,arg2);
		}
	}
	
	public HashMap getAll() {
		return this.wsdl_table;
	}
}
