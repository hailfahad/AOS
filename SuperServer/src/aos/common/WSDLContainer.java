package aos.common;

import java.io.Serializable;
import java.util.HashMap;

public class WSDLContainer implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5441040525275235593L;
	private HashMap<String, Integer> wsdl_table;
	
	private WSDLContainer() {
		if(wsdl_table == null) {
			wsdl_table=new HashMap();	
		}
	}
	
	public static HashMap<String, Integer> getInstance() {
		return new WSDLContainer().wsdl_table;
	}
}
