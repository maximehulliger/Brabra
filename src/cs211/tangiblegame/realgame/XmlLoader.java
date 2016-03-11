package cs211.tangiblegame.realgame;


import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import cs211.tangiblegame.physic.Object;
import cs211.tangiblegame.physic.Object.ParentRelationship;
import cs211.tangiblegame.Color;
import cs211.tangiblegame.ProMaster;
import cs211.tangiblegame.geo.Quaternion;
import cs211.tangiblegame.physic.Body;

public final class XMLLoader extends ProMaster {
	private final String filename;
	private final XMLReader xmlreader;
	
	public XMLLoader() {
		filename = app.inputPath()+"scene.xml";
		XMLReader xmlreader = null;
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
		    SAXParser parser = factory.newSAXParser();
		    xmlreader = parser.getXMLReader();
		    xmlreader.setContentHandler(new PrefabHandler());
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.xmlreader = xmlreader;
	}
	
	/** 
	 * load the object from the file at @filename
	 * supported attributes for objects:
	 * 	 name, dir, pos, parency, mass, life, color, stroke,
	 * 	 impulse, focus, force, camera, cameraDist, debug.
	 *  */
	public void load() {
		try {
			xmlreader.parse(filename);
		} catch (Exception e) {
			game.debug.err("\nerreur dans scene.xml:");
			e.printStackTrace();
		}
	}
	
	private class PrefabHandler extends DefaultHandler {
		private Object parent = null;
		private int nullBodyParentCount = 0;
		
	    public void startElement(String namespaceURI, String localName,String qName, Attributes atts) 
	    		throws SAXException {
	    	if (localName.equals("scene"))
	    		return;
	    	else if (localName.equals("physic")) {
	    		String gravity = atts.getValue("gravity");
	    		if (gravity != null)
				  	game.physic.gravity = Float.parseFloat(gravity);
			  	String paused = atts.getValue("paused");
			  	if (paused != null)
			  		game.physic.paused = Boolean.parseBoolean(paused);
	    	} else if (localName.equals("camera")) {
	    		game.camera.setParent(parent);
    			parent = game.camera;
	    		game.camera.set(atts.getValue("mode"),atts.getValue("dist"),null);
			  	String displaySkybox = atts.getValue("displaySkybox");
			  	String debug = atts.getValue("debug");
	    		if (displaySkybox != null)
				  	game.camera.setSkybox(Boolean.parseBoolean(displaySkybox));
			  	if (debug != null && Boolean.parseBoolean(debug)) {
			  		game.debug.followed.add(game.camera);
			  	}
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
	    		String focus = atts.getValue("focus");
	    		String dir = atts.getValue("dir");
	    		String debug = atts.getValue("debug");
	    			
	    		Body b = (dir != null)
	    				? Prefab.add(localName, vec(pos), Quaternion.fromDirection(vec(dir)))
	    				: Prefab.add(localName, vec(pos));
	    				
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
				  	} if (focus != null && Boolean.parseBoolean(focus)) {
				  		String force = atts.getValue("force");
				  		if (force != null)
				  			game.physicInteraction.setFocused(b, Float.parseFloat(force));
				  		else
				  			game.physicInteraction.setFocused(b);
				  	}
				  	if (debug != null && Boolean.parseBoolean(debug)) {
				  		game.debug.followed.add(b);
				  	}
				  	if (parent != null) {
				  		String parentRel = atts.getValue("parentRel");
			    		b.setParentRel(ParentRelationship.fromString(parentRel));
			    		b.setParent(parent);
				  	}
				  	parent = b;
			  	} else
			  		nullBodyParentCount++;
	    	}
	    }
	    
		public void endElement(String uri, String localName, String qName) 
				throws SAXException {
			if (localName.equals("scene") || localName.equals("physic"))
	    		return;
	    	else {
	    		if (nullBodyParentCount <= 0) {
	    			parent = parent.parent();
	    		} else {
	    			nullBodyParentCount--;
	    		}
	    	}
		}
		
		//public void characters(char[] ch, int start, int length) {}
	}

	private void setLife(Body b, String lifeText) {
		String[] sub = lifeText.split("/");
		if (sub.length == 2)
			b.setLife(Integer.parseInt(sub[0]),Integer.parseInt(sub[1]));
		else if (sub.length == 1) {
			int life = Integer.parseInt(sub[0]);
			b.setLife(life, life);
		} else
			System.err.println("unsuported life format: \""+lifeText+"\"");
	}
	
}
