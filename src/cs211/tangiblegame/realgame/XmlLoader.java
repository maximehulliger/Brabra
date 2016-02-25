package cs211.tangiblegame.realgame;


import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.TangibleGame;
import cs211.tangiblegame.physic.Body;
import cs211.tangiblegame.physic.Physic;

public final class XmlLoader extends ProMaster {
	public static RealGame game;
	private static final String filename = TangibleGame.inputPath+"scene.xml";
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
				  	game.camera.setSkybox(Boolean.parseBoolean(displaySkybox));
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
	    		String cameraDist = atts.getValue("cameraDist");
	    		String mass = atts.getValue("mass");
	    		String name = atts.getValue("name");
	    		String life = atts.getValue("life");
	    		String color = atts.getValue("color");
	    		String stroke = atts.getValue("stroke");
			  	
	    		Body b = Prefab.add(localName, vec(pos));
	    		if (b != null) {
			  		if (color != null)
			  			b.setColor( new Color(color, stroke) );
			  		if (impulse != null)
					  	b.applyImpulse(vec(impulse));
			  		if (mass != null)
					  	b.setMass(Float.parseFloat(mass));
			  		if (name != null)
					  	b.setName(name);
			  		if (life != null)
			  			setLife(b, life);
				  	if (camera != null) {
				  		game.camera.set(camera,cameraDist,b);
				  	}
			  	}
	    	}
	    }
	    
	    /*
		public void endElement(String uri, String localName, String qName) throws SAXException {}
		public void characters(char[] ch, int start, int length) {}
		*/
	}
	

	private void setLife(Body alive, String lifeText) {
		String[] sub = lifeText.split("/");
		if (sub.length == 2)
			alive.setLife(Integer.parseInt(sub[0]),Integer.parseInt(sub[1]));
		else if (sub.length == 1) {
			int life = Integer.parseInt(sub[0]);
			alive.setLife(life, life);
		} else
			System.out.println("unsuported life format: \""+lifeText+"\"");
		
	}
}
