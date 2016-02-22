package cs211.tangiblegame.realgame;


import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import cs211.tangiblegame.Camera.FollowMode;
import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.physic.Body;

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
	    		String mode = atts.getValue("mode");
			  	if (mode != null) 
				  	game.camera.followMode = FollowMode.fromString(mode);
			  	String dist = atts.getValue("dist");
				if (dist != null)
				  	game.camera.setAt(vec(dist));
				String displaySkybox = atts.getValue("displaySkybox");
				if (displaySkybox != null)
				  	game.camera.displaySkybox = Boolean.getBoolean(displaySkybox);
	    	} else { //body
	    		String pos = atts.getValue("pos");
	    		String impulse = atts.getValue("impulse");
			  	String camera = atts.getValue("camera");
			  
			  	Body b = Prefab.add(localName, vec(pos));
			  	if (b != null) {
				  	if (impulse != null)
					  	b.applyImpulse(vec(impulse));
				  	if (camera != null) {
				  		game.camera.toFollow = b;
					  	game.camera.followMode = FollowMode.fromString(camera);
					  	String dist = atts.getValue("cameraDist");
						if (dist != null)
							game.camera.setAt(vec(dist));
					  	System.out.println("camera now following "+b.toString());
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
