package cs211.tangiblegame.realgame;


import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import cs211.tangiblegame.physic.Object;
import cs211.tangiblegame.physic.Object.ParentRelationship;
import cs211.tangiblegame.realgame.Camera.FollowMode;
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
		private Stack<Object> parentStack = new Stack<>();
		
	    public void startElement(String namespaceURI, String localName,String qName, Attributes atts) 
	    		throws SAXException {
	    	if (localName.equals("scene"))
	    		return;
	    	else if (localName.equals("physic")) {
	    		String gravity = atts.getValue("gravity");
	    		if (gravity != null)
				  	game.physic.gravity = Float.parseFloat(gravity);
			  	String running = atts.getValue("running");
			  	if (running != null)
			  		game.physic.running = Boolean.parseBoolean(running);
	    	} else {
	    		// new object into scene: camera or prefab.
	    		Object newObj;
	    		// first set the parent
	    		Object newParent = parentStack.empty() ? null : parentStack.peek();
	    		
	    		if (localName.equals("camera")) {
	    			if (newParent != null)
	    				game.camera.setParent(newParent);
	    			String mode = atts.getValue("mode");
	    			if (mode != null) {
		    			String dist = atts.getValue("dist");
		    			if (dist == null)
		    				game.debug.err("for camera: dist should be set with mode. ignoring mode.");
		    			else 
		    				game.camera.setDist(FollowMode.fromString(mode), vec(dist));
		    		}
				  	String displaySkybox = atts.getValue("displaySkybox");
		    		if (displaySkybox != null)
					  	game.camera.setSkybox(Boolean.parseBoolean(displaySkybox));
				  	newObj = game.camera;
	    		} else {
	    			String pos = atts.getValue("pos");
	    			String dir = atts.getValue("dir");
	    			Body newBody = Prefab.add(localName, vec(pos), 
	    					dir != null ? Quaternion.fromDirection(vec(dir)) : identity);

					if (newBody != null) {
						if (newParent != null) {
							newBody.setParent(newParent);
							String parentRel = atts.getValue("parentRel");
							newBody.setParentRel(ParentRelationship.fromString(parentRel));
						}
						String color = atts.getValue("color");
						if (color != null) {
							String stroke = atts.getValue("stroke");
							newBody.setColor( new Color(color, stroke) );
						}
						String mass = atts.getValue("mass");
						if (mass != null)
							newBody.setMass(Float.parseFloat(mass));
						String name = atts.getValue("name");
						if (name != null)
							newBody.setName(name);
						String life = atts.getValue("life");
						if (life != null)
							setLife(newBody, life);
						String cameraMode = atts.getValue("camera");
						if (cameraMode != null) {
							String cameraDist = atts.getValue("cameraDist");
							game.camera.set(newBody, cameraMode, cameraDist);
						}
						String impulse = atts.getValue("impulse");
						if (impulse != null)
							newBody.applyImpulse(vec(impulse));
						String focus = atts.getValue("focus");
						if (focus != null && Boolean.parseBoolean(focus)) {
							String force = atts.getValue("force");
							if (force != null)
								game.physicInteraction.setFocused(newBody, Float.parseFloat(force));
							else
								game.physicInteraction.setFocused(newBody);
						}
					}
					newObj = newBody;
	    		}
				parentStack.push(newObj);
	    		String debug = atts.getValue("debug");
			  	if (debug != null && Boolean.parseBoolean(debug)) {
			  		game.debug.followed.add(newObj);
			  	}
	    	}
	    }
	    
		public void endElement(String uri, String localName, String qName) 
				throws SAXException {
			if (localName.equals("scene") || localName.equals("physic"))
	    		return;
	    	else {
	    		parentStack.pop();
	    	}
		}
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
