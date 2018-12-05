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

		
		
		

	}

}
