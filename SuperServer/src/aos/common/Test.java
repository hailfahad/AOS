package aos.common;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;

public class Test {

	public static void main(String[] args) {
		
		File xmlFile = new File("E:\\git\\AOS\\Try50\\WebContent\\wsdl\\AddService.wsdl");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		Document xml;
		TreeSet<String> unique = new TreeSet<String>();
		try {
			db = dbf.newDocumentBuilder();
			xml = db.parse(xmlFile);
			System.out.println("root:" + xml.getDocumentElement().getNodeName());
			NodeList list = xml.getElementsByTagName("wsdl:operation");
			for(int i = 0; i < list.getLength(); i++) {
				Node curr = list.item(i);
				System.out.println("CURR ELEMENT: "+ curr.getNodeName());
				Element e = (Element)curr;
				unique.add(e.getAttribute("name"));
				
			}
			System.out.println(unique.contains("add"));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		String test = "hello|world|how|are";
		String[] chainParts = test.split("\\|");
		for(int i = 0; i< chainParts.length; i+=2) {
			String message = chainParts[i];
			String time = chainParts[i+1];
			System.out.println(message + time);
		}
		
		Block inital  = new Block("test","0");
		//System.out.println("first block" + inital.hash);
		
		Block inital2  = new Block("test2",inital.hash);
		//System.out.println("second block" + inital2.hash);
		
		Block inital3  = new Block("test3",inital2.hash);
		//System.out.println("thrid block" + inital3.hash);
		
		ArrayList<Block> chain = new ArrayList<Block>();
		chain.add(inital);
		chain.get(chain.size()-1).mine(1);
		chain.add(inital2);
		chain.get(chain.size()-1).mine(1);
		chain.add(inital3);
		chain.get(chain.size()-1).mine(1);
		
		Block curr, prev;
		
		String tar = new String(new char[1]).replace('\0', '0');
		
		System.out.println("target " + tar);
		for(int i=1; i< chain.size(); i++) {
			curr = chain.get(i);
			prev = chain.get(i-1);
			
			if(!curr.hash.equals(curr.getHash())) {
				System.out.println("u messed up");
				break;
			}
			if(!prev.hash.equals(curr.previousHash)) {
				System.out.println("u still meessed up");
				break;
			}
		}
		

		
		

	}

}
