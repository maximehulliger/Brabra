package cs211.tangiblegame.realgame;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.physic.Body;

public final class XmlLoader extends ProMaster {
	private static final String filename = "scene.xml";
	private XMLReader xmlreader;
	
	public XmlLoader() {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
		    SAXParser parser = factory.newSAXParser();
			xmlreader = parser.getXMLReader();
			xmlreader.setContentHandler(new PrefabHandler());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// load the object from the file at @location
	public void load() {
		try {
			xmlreader.parse(filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class PrefabHandler extends DefaultHandler {
	
	    @Override
	    public void startElement(String namespaceURI, String localName,String qName, Attributes atts) 
	    		throws SAXException {
	    	if (localName.equals("scene"))
	    		return;
	    	else {
			  String pos = atts.getValue("pos");
			  String impulse = atts.getValue("impulse");
			  
			  Body b = Prefab.add(localName, vec(pos));
			  if (impulse != null)
				  b.applyImpulse(vec(impulse));
		  	}
		}
		
	    /*@Override
	    public void endElement(String uri, String localName, String qName)
	    		throws SAXException {
	
	    }
	
	    @Override
	    public void characters(char[] ch, int start, int length) {
	    	//buffer.append(ch, start, length);
	    }*/
	}
}
