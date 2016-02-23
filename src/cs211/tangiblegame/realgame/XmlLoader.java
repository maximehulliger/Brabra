package cs211.tangiblegame.realgame;


import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.physic.Body;
import cs211.tangiblegame.physic.Physic;

public final class XmlLoader extends ProMaster {
	public static RealGame game;
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
	    	else if (localName.equals("camera")) {
	    		game.camera.set(atts.getValue("mode"),atts.getValue("dist"),null);
			  	String displaySkybox = atts.getValue("displaySkybox");
				if (displaySkybox != null)
				  	game.camera.setSkybox(Boolean.getBoolean(displaySkybox));
	    	} else if (localName.equals("physic")) {
	    		String gravity = atts.getValue("gravity");
	    		if (gravity != null)
				  	Physic.gravity = Float.parseFloat(gravity);
			  	String deltaTime = atts.getValue("deltaTime");
			  	if (deltaTime != null)
			  		Physic.deltaTime = Float.parseFloat(deltaTime);
	    	} else {
	    		String pos = atts.getValue("pos");
	    		String impulse = atts.getValue("impulse");
			  	String camera = atts.getValue("camera");
			  
			  	Body b = Prefab.add(localName, vec(pos));
			  	if (b != null) {
				  	if (impulse != null)
					  	b.applyImpulse(vec(impulse));
				  	if (camera != null) {
				  		game.camera.set(camera,atts.getValue("dist"),b);
				  	}
			  	}
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
