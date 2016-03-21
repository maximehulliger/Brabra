package brabra.game;


import java.util.HashMap;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import brabra.ProMaster;
import brabra.Brabra;
import brabra.game.physic.Body;
import brabra.game.physic.Collider;
import brabra.game.physic.geo.Quaternion;
import brabra.game.scene.Object;
import brabra.game.scene.Prefab;
import processing.core.PVector;
import brabra.game.scene.Camera.FollowMode;

/** Class responsible to load the scene file. */
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
	 * load the object from the file at @filename.
	 * supported attributes for objects:
	 * 	 name, dir, pos, parency, mass, life, color, stroke, impulse,
	 * 	 focus, force, camera, cameraDist, debug, displayCollider.
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
		private Stack<Attributes> attrStack = new Stack<>();
		
	    public void startElement(String namespaceURI, String localName,String qName, org.xml.sax.Attributes atts) 
	    		throws SAXException {
	    	if (localName.equals("scene"))
	    		return;
	    	else if (localName.equals("physic")) {
	    		final String gravity = atts.getValue("gravity");
	    		if (gravity != null)
				  	game.physic.gravity = Float.parseFloat(gravity);
	    		final String running = atts.getValue("running");
			  	if (running != null)
			  		game.setRunning(Boolean.parseBoolean(running));
	    	} else if (localName.equals("general")) {
	    		final String verbosity = atts.getValue("verbosity");
	    		if (verbosity != null) {
	    			if (verbosity.equals("max") || verbosity.equals("all"))
	    				Brabra.verbosity = Integer.MAX_VALUE;
	    			else if (verbosity.equals("min") || verbosity.equals("silence") || verbosity.equals("none"))
	    				Brabra.verbosity = Integer.MIN_VALUE;
	    			else
	    				Brabra.verbosity = Integer.parseInt(verbosity);
	    		}
	    		final String displayAllColliders =  atts.getValue("displayAllColliders");
	    		if (displayAllColliders != null)
	    			Collider.displayAllColliders = Boolean.parseBoolean(displayAllColliders);
	    	} else {
	    		Object newObj;
	    		// first set the parent
	    		final Object newParent = parentStack.empty() ? null : parentStack.peek();
	    		// pos & dir
	    		final String posString = atts.getValue("pos");
	    		final String dirString = atts.getValue("dir");
	    		final boolean posSet = posString != null;
	    		final boolean rotSet = dirString != null;
	    		final PVector pos = posSet ? vec(posString) : zero;
	    		final Quaternion rot = rotSet ? Quaternion.fromDirection(vec(dirString)) : identity;
	    		
	    		if (localName.equals("camera")) {
	    			if (newParent != null)
	    				game.camera.setParent(newParent);
	    			final String mode = atts.getValue("mode");
	    			if (mode != null) {
	    				final String distString = atts.getValue("dist");
	    				final PVector dist = distString != null ? vec(distString) : pos;
	    				if (dist == null)
		    				game.debug.err("for camera: dist (or pos) should be set with mode. ignoring mode.");
		    			else 
		    				game.camera.setDist(FollowMode.fromString(mode), dist);
		    		}
	    			final String displaySkybox = atts.getValue("displaySkybox");
		    		if (displaySkybox != null)
					  	game.camera.setSkybox(Boolean.parseBoolean(displaySkybox));
				  	newObj = game.camera;
	    		} else if (Prefab.supportedObjects.contains(localName)) {
	    			newObj = Prefab.add(localName, pos, rot);
	    			if (newObj != null) {
		    			newObj.setParentMaybe(newParent, atts.getValue("parentRel"));
	    			}
	    		} else {
	    			Body newBody = Prefab.addBody(localName, pos, rot);
					if (newBody != null) {
						newBody.setParentMaybe(newParent, atts.getValue("parentRel"));
						
						
					}
					newObj = newBody;
	    		}
				parentStack.push(newObj);
				attrStack.push(new Attributes(atts));
	    	}
	    }
	    
		public void endElement(String uri, String localName, String qName) 
				throws SAXException {
			if (localName.equals("scene") || localName.equals("physic") ||  localName.equals("general"))
	    		return;
	    	else {
	    		final Object obj = parentStack.pop();
	    		final Attributes atts = attrStack.pop();
	    		
	    		if (obj != null) {
	    			obj.validate(atts);
	    			
		    		// focus: here because we want to do that with the children set.
		    		final String focus = atts.getValue("focus");
					if (focus != null && Boolean.parseBoolean(focus)) {
						final String force = atts.getValue("force");
						game.physicInteraction.setFocused(obj, force != null ? Float.parseFloat(force) : -1);
					}
	    		}
	    	}
		}
	}
	
	public static class Attributes extends HashMap<String, String> {
		private static final long serialVersionUID = -6595200285921918122L;

		public Attributes(org.xml.sax.Attributes attr) {
			for (int i=0; i < attr.getLength(); i++) {
				final String field = attr.getQName(i);
				final String value = attr.getValue(i);
				super.put(field, value);
			}
		}
		
		public String getValue(String field) {
			return get(field);
		}
	}
}
